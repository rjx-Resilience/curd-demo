package org.example.curddemo.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.example.curddemo.entity.Student;
import org.example.curddemo.mapper.StudentMapper;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 学生唯一字段（email/phone/studentNo）查重缓存
 *
 * 设计原则：
 *  1. 懒加载：第一次查重时才从 DB 全量加载三列到 Redis Set，不占用启动时间
 *  2. 可降级：任何 Redis 异常自动 fallback 为 null（让业务走原来的 MySQL IN 查询）
 *  3. 可自愈：每个 Set 设 10 分钟 TTL，到期自动回源重建
 *  4. 高并发安全：初始化用 Lua SETNX + EXPIRE，多线程同时触发只加载一次
 *  5. 可观测：应用启动就打印 Redis 自检结果；每批导入明确打印 REDIS / MYSQL_FALLBACK 模式
 */
@Slf4j
@Component
public class StudentUniqCacheService {

    public static final String KEY_EMAIL      = "student:uniq:email";
    public static final String KEY_PHONE      = "student:uniq:phone";
    public static final String KEY_STUDENT_NO = "student:uniq:student_no";

    /** 初始化锁（SETNX+EXPIRE 原子执行） */
    private static final String KEY_INIT_LOCK = "student:uniq:init_lock";
    private static final int    LOCK_SECONDS  = 30;

    private static final int TTL_MINUTES = 10;

    private final StringRedisTemplate redis;
    private final StudentMapper studentMapper;

    /**
     * Lua 脚本：原子执行 SETNX + EXPIRE，避免 "SETNX 成功但 EXPIRE 没执行" 的死锁。
     * 注意：ARGV[1] 是 SETNX 的值（随便填个 1），ARGV[2] 是过期秒数。
     */
    private static final DefaultRedisScript<Long> SCRIPT_LOCK = new DefaultRedisScript<>(
            "if redis.call('SETNX', KEYS[1], ARGV[1]) == 1 then " +
            "  redis.call('EXPIRE', KEYS[1], ARGV[2]); " +
            "  return 1; " +
            "else return 0; end",
            Long.class
    );

    /** 占位成员：保证空集合也能在 Redis 创建真正的 Set 结构（后面会删掉，不影响 SISMEMBER 判断） */
    private static final String EMPTY_SET_MARKER = "__EMPTY_MARKER__";

    public StudentUniqCacheService(StringRedisTemplate redis, StudentMapper studentMapper) {
        this.redis = redis;
        this.studentMapper = studentMapper;
    }

    // ================================================================
    // 启动自检：应用一跑起来就在控制台打印「Redis 能不能用」，用户不用猜
    // ================================================================
    @EventListener(ApplicationReadyEvent.class)
    public void selfCheckOnStartup() {
        try {
            String pong = redis.execute((org.springframework.data.redis.core.RedisCallback<String>) c -> {
                Object rsp = c.ping();
                return rsp == null ? null : rsp.toString();
            });
            log.info("Redis 连通性自检：OK (PING -> {})，已启用查重缓存加速", pong);
        } catch (DataAccessException e) {
            log.warn("Redis 连通性自检：FAIL ({}).  【已自动降级为纯 DB 查重模式，导入功能完全可用，只是没有 Redis 加速】",
                    e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        } catch (Exception e) {
            log.warn("Redis 连通性自检：WARN ({}).  导入功能正常，必要时会降级 DB 模式",
                    e.getMessage());
        }
    }

    // ================================================================
    // 对外：查重判断
    // 返回: Boolean.TRUE = 重复；Boolean.FALSE = 不重复；null = Redis 不可用，请走数据库
    // ================================================================
    public Boolean existsEmail(String email) {
        return existsInternal(KEY_EMAIL, email);
    }
    public Boolean existsPhone(String phone) {
        return existsInternal(KEY_PHONE, phone);
    }
    public Boolean existsStudentNo(String studentNo) {
        return existsInternal(KEY_STUDENT_NO, studentNo);
    }

    /**
     * 返回当前缓存是否已就绪（给 Service 层打日志用，让用户一眼看到每批走的是 Redis 还是 DB）。
     */
    public boolean isReady() {
        try {
            Boolean has = redis.hasKey(KEY_EMAIL);
            return Boolean.TRUE.equals(has);
        } catch (Exception ignore) {
            return false;
        }
    }

    // ================================================================
    // 对外：saveBatch 成功后，把这批入库成功的字段同步到 Redis
    // ================================================================
    public void addSavedBatch(List<Student> savedList) {
        if (savedList == null || savedList.isEmpty()) return;
        if (!ensureLoaded()) {
            log.info("Redis 未就绪，跳过本次 {} 条成功记录的缓存同步（不影响功能，DB 唯一索引兜底）", savedList.size());
            return;
        }
        try {
            Set<String> emails = new HashSet<>();
            Set<String> phones = new HashSet<>();
            Set<String> stus   = new HashSet<>();
            for (Student s : savedList) {
                if (StringUtils.hasText(s.getEmail()))    emails.add(s.getEmail());
                if (StringUtils.hasText(s.getPhone()))    phones.add(s.getPhone());
                if (StringUtils.hasText(s.getStudentNo()))stus.add(s.getStudentNo());
            }
            if (!emails.isEmpty()) redis.opsForSet().add(KEY_EMAIL,      emails.toArray(new String[0]));
            if (!phones.isEmpty()) redis.opsForSet().add(KEY_PHONE,      phones.toArray(new String[0]));
            if (!stus.isEmpty())   redis.opsForSet().add(KEY_STUDENT_NO, stus.toArray(new String[0]));
            if (log.isDebugEnabled()) {
                log.debug("同批次同步至 Redis 缓存：email={} 条, phone={} 条, studentNo={} 条",
                        emails.size(), phones.size(), stus.size());
            }
        } catch (DataAccessException e) {
            log.warn("同步批次到 Redis 失败（不影响功能）：{}", e.getMessage());
        }
    }

    // ================================================================
    // 对外：单条 CUD 维护 Redis
    // ================================================================
    public void addOne(Student s) {
        addSavedBatch(Collections.singletonList(s));
    }

    public void removeOne(Student old) {
        if (old == null) return;
        try {
            if (StringUtils.hasText(old.getEmail()))     redis.opsForSet().remove(KEY_EMAIL,      old.getEmail());
            if (StringUtils.hasText(old.getPhone()))     redis.opsForSet().remove(KEY_PHONE,      old.getPhone());
            if (StringUtils.hasText(old.getStudentNo())) redis.opsForSet().remove(KEY_STUDENT_NO, old.getStudentNo());
        } catch (DataAccessException e) {
            log.warn("从 Redis 删除旧值失败（不影响功能，TTL 到期会自愈）：{}", e.getMessage());
        }
    }

    /** 手动强制重建缓存（比如你手动改了 DB 后可以调） */
    public void forceReload() {
        try {
            redis.delete(Arrays.asList(KEY_EMAIL, KEY_PHONE, KEY_STUDENT_NO, KEY_INIT_LOCK));
            log.info("已清空 Redis 学生唯一字段缓存，接下来首次查重会自动重新加载");
        } catch (Exception e) {
            log.warn("清空 Redis 学生唯一字段缓存失败：{}（不影响功能）", e.getMessage());
            return;
        }
        ensureLoaded();
    }

    // =========================================================================================
    // 内部：真正查重 + 懒加载（含：并发初始化锁 + 空集合也保证建 Key + 统一 TTL）
    // =========================================================================================
    private Boolean existsInternal(String key, String value) {
        if (!StringUtils.hasText(value)) return Boolean.FALSE; // 空值不查（DB 允许多个 NULL，不视为冲突）
        if (!ensureLoaded()) return null;                      // Redis 没装好 / 加载失败 → 让调用方走 DB fallback
        try {
            Boolean hit = redis.opsForSet().isMember(key, value);
            return hit == null ? Boolean.FALSE : hit;
        } catch (DataAccessException e) {
            log.warn("Redis 查询 {} 失败，本批剩余数据自动降级走 DB 查重：{}", key, e.getMessage());
            return null;
        }
    }

    /**
     * 确保 3 个 Set 都加载好了（Redis 挂了自动返回 false 走 DB fallback，绝对不阻塞）
     */
    private boolean ensureLoaded() {
        // ------- 1) 先看缓存是不是已经建好了 -------
        try {
            Boolean hasEmail = redis.hasKey(KEY_EMAIL);
            if (Boolean.TRUE.equals(hasEmail)) return true;
        } catch (DataAccessException e) {
            log.info("Redis 暂不可用（下面本批次查重走 MySQL IN 查询兜底，不影响功能）。原因：{}",
                    e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            return false;
        }

        // ------- 2) 没建好 → 拿初始化锁（Lua 原子 SETNX+EXPIRE，只允许 1 个线程灌数据） -------
        Long locked;
        try {
            locked = redis.execute(
                    SCRIPT_LOCK,
                    Collections.singletonList(KEY_INIT_LOCK),
                    "1", String.valueOf(LOCK_SECONDS)
            );
        } catch (DataAccessException e) {
            log.warn("拿 Redis 初始化锁失败（走 DB 查重兜底，不影响功能）：{}",
                    e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            return false;
        }

        if (locked == null || locked != 1L) {
            // 没抢到锁：说明其他线程正在加载 → 等 500ms 再看一眼；加载好了就用，还没好就本请求先走 DB（不阻塞用户）
            try { Thread.sleep(500); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            try {
                return Boolean.TRUE.equals(redis.hasKey(KEY_EMAIL));
            } catch (Exception ignore) {
                return false;
            }
        }

        // ------- 3) 抢到锁 → 从 DB 全量加载（异常时自动降级走 DB，锁 30s 后自动释放） -------
        try {
            loadFromDb();
            log.info("✅ 学生唯一字段已灌入 Redis：email={} 条, phone={} 条, studentNo={} 条 (TTL={} 分钟)",
                    sizeOf(KEY_EMAIL), sizeOf(KEY_PHONE), sizeOf(KEY_STUDENT_NO), TTL_MINUTES);
            return true;
        } catch (Exception e) {
            log.error("从 DB 加载唯一字段到 Redis 失败（已自动降级走 DB 查重，不影响功能）。原因：{}",
                    e.getCause() != null ? e.getCause().getMessage() : e.getMessage(), e);
            return false;
        } finally {
            // 不管成功/失败，都尝试删锁（失败也没事，30s TTL 兜底）
            try { redis.delete(KEY_INIT_LOCK); } catch (Exception ignore) {}
        }
    }

    /**
     * 从 DB 加载 3 个唯一列到 Redis Set。
     * 关键点：即便 DB 3 列全是空集合也要在 Redis 创建真正的 Set Key 并设 TTL，
     *        否则每次 import 都会 hasKey=false → 重复加载，浪费 DB 资源。
     */
    private void loadFromDb() {
        List<Student> allEmail   = studentMapper.selectList(Wrappers.<Student>lambdaQuery()
                .select(Student::getEmail).isNotNull(Student::getEmail));
        List<Student> allPhone   = studentMapper.selectList(Wrappers.<Student>lambdaQuery()
                .select(Student::getPhone).isNotNull(Student::getPhone));
        List<Student> allStuNo   = studentMapper.selectList(Wrappers.<Student>lambdaQuery()
                .select(Student::getStudentNo).isNotNull(Student::getStudentNo));

        Set<String> emails = new HashSet<>();
        for (Student s : allEmail) if (StringUtils.hasText(s.getEmail())) emails.add(s.getEmail());
        Set<String> phones = new HashSet<>();
        for (Student s : allPhone) if (StringUtils.hasText(s.getPhone())) phones.add(s.getPhone());
        Set<String> stus   = new HashSet<>();
        for (Student s : allStuNo) if (StringUtils.hasText(s.getStudentNo())) stus.add(s.getStudentNo());

        final long ttlSeconds = TTL_MINUTES * 60L;

        redis.executePipelined((org.springframework.data.redis.core.RedisCallback<Object>) connection -> {
            RedisSerializer<String> s = redis.getStringSerializer();
            writeSet(connection, s, KEY_EMAIL,      emails, ttlSeconds);
            writeSet(connection, s, KEY_PHONE,      phones, ttlSeconds);
            writeSet(connection, s, KEY_STUDENT_NO, stus,   ttlSeconds);
            return null;
        });
    }

    /**
     * 写单个 Set 到 Redis（集合为空也保证 Key 被创建，后面 EXPIRE 能成功，hasKey 才返回 true）。
     */
    private void writeSet(org.springframework.data.redis.connection.RedisConnection connection,
                          RedisSerializer<String> s, String key, Set<String> values, long ttlSeconds) {
        byte[] rawKey = s.serialize(key);
        if (!values.isEmpty()) {
            connection.setCommands().sAdd(rawKey, toByteArrays(values, s));
        } else {
            // 空集合：先加一个占位标记让 Set 结构被创建，再立刻把占位符删掉
            byte[] marker = s.serialize(EMPTY_SET_MARKER);
            connection.setCommands().sAdd(rawKey, marker);
            connection.setCommands().sRem(rawKey, marker);
        }
        connection.keyCommands().expire(rawKey, ttlSeconds);
    }

    private long sizeOf(String key) {
        try { Long n = redis.opsForSet().size(key); return n == null ? 0 : n; }
        catch (Exception ignore) { return 0; }
    }

    private static byte[][] toByteArrays(Collection<String> c, RedisSerializer<String> s) {
        byte[][] out = new byte[c.size()][];
        int i = 0;
        for (String v : c) out[i++] = s.serialize(v);
        return out;
    }
}
