package org.example.curddemo.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
//import jakarta.transaction.Transactional;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.curddemo.common.exception.BusinessException;
import org.example.curddemo.common.exception.ResponseEnum;
import org.example.curddemo.converter.studentConverter;
//import org.example.curddemo.dao.StudentRepository;
import org.example.curddemo.dto.StudentDTO;
import org.example.curddemo.dto.importDTO.StudentExcelDTO;
import org.example.curddemo.entity.Student;
import org.example.curddemo.listener.StudentReadListener;
import org.example.curddemo.mapper.StudentMapper;
import org.example.curddemo.service.StudentService;
import org.example.curddemo.service.StudentUniqCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;
@Slf4j
@Service
public class StudentServiceimpl extends ServiceImpl<StudentMapper,Student> implements StudentService {

//    @Autowired
//    StudentRepository studentRepository;

    @Autowired
    studentConverter studentConverter;

    @Autowired
    StudentMapper studentMapper;

    @Autowired
    StudentUniqCacheService uniqCache;



    @Override
    public List<StudentDTO> getAllStudents() {
//        List<Student> students = studentRepository.findAll();
        List<Student> students = studentMapper.selectList(null);
        return studentConverter.getStudentList(students);
    }

    @Override
    public StudentDTO getStudentById(Long id) {
//        Student student = studentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("为找到该ID对应的学生"));
        Student student = studentMapper.selectById(id);
        if (student == null){
            throw new IllegalArgumentException("学生不存在,ID: " +id);
        }
        return  studentConverter.converterStudent(student);

    }

    @Override
    public Long addNewStudent(StudentDTO studentDTO) {
        //校验邮箱
        if (StringUtils.hasLength(studentDTO.getEmail())) {
//            if (studentRepository.existsByEmail(studentDTO.getEmail())) {
//                throw new BusinessException(ResponseEnum.EMAIL_DUPLICATE);
//            }
            Long count = studentMapper.selectCount(Wrappers.<Student>lambdaQuery().eq(Student :: getEmail, studentDTO.getEmail()));
            if (count > 0){
                throw new BusinessException(ResponseEnum.EMAIL_DUPLICATE);
            }
        }

        // 2. 校验学号 (学号通常是必填且唯一的)
        if (StringUtils.hasLength(studentDTO.getStudentNo())){
//        if (studentRepository.existsByStudentNo(studentDTO.getStudentNo())) {
//            throw new BusinessException(ResponseEnum.STUDENT_NO_DUPLICATE);
//        }
            Long countNo = studentMapper.selectCount(Wrappers.<Student>lambdaQuery().eq(Student::getStudentNo,studentDTO.getStudentNo()));
            if (countNo > 0){
                throw new BusinessException(ResponseEnum.STUDENT_NO_DUPLICATE);
            }
        }

        // 3. 校验手机号 (假设手机号非空)
        if (StringUtils.hasLength(studentDTO.getPhone())) {
//            if (studentRepository.existsByPhone(studentDTO.getPhone())) {
//                throw new BusinessException(ResponseEnum.PHONE_DUPLICATE);
//            }
            Long countPhone = studentMapper.selectCount(Wrappers.<Student>lambdaQuery().eq(Student::getPhone,studentDTO.getPhone()));
            if (countPhone > 0){
                throw new BusinessException(ResponseEnum.PHONE_DUPLICATE);
            }
        }
//        Student save = studentRepository.save(studentConverter.converterStudent(studentDTO));
        Student student = studentConverter.converterStudent(studentDTO);
        studentMapper.insert(student);
        uniqCache.addOne(student);
        return student.getId();
    }

    @Override
    public void deleteStudentById(Long id) {
//        studentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("id:"+ id + "doesn't exist!" ));
        Student student = studentMapper.selectById(id);
        if (student == null){
            throw new IllegalArgumentException("id:"+ id + "doesn't exist!");
        }
        uniqCache.removeOne(student);
        studentMapper.deleteById(id);
    }

//    @Transactional
    @Override
    public StudentDTO updateStudentById(Long id, String name, Integer age, String email, Integer gender, String phone, String addr, String studentNo) {
//        Student student = studentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("id:" + id + "doesn't exist!"));
        Student student = studentMapper.selectById(id);

        Student old = student;
        if (old != null){
            boolean emailChanged = StringUtils.hasLength(email) && !email.equals(old.getEmail());
            boolean phoneChanged = StringUtils.hasLength(phone) && !phone.equals(old.getPhone());
            boolean snoChanged   = StringUtils.hasLength(studentNo) && !studentNo.equals(old.getStudentNo());
            if (emailChanged || phoneChanged || snoChanged) {
                Student forRemove = new Student();
                if (emailChanged) forRemove.setEmail(old.getEmail());
                if (phoneChanged) forRemove.setPhone(old.getPhone());
                if (snoChanged)   forRemove.setStudentNo(old.getStudentNo());
                uniqCache.removeOne(forRemove);
            }
        }

        if (student == null){
            throw new IllegalArgumentException("id:" + id + "doesn't exist!");
        }
        if (StringUtils.hasLength(email) && !email.equals(student.getEmail())){
            Long count = studentMapper.selectCount(Wrappers.<Student>lambdaQuery().eq(Student::getEmail,email));
            if (count > 0){
                throw new BusinessException(ResponseEnum.EMAIL_DUPLICATE);
            }
            student.setEmail(email);
        }
        if (StringUtils.hasLength(phone) && !phone.equals(student.getPhone())){
            Long count = studentMapper.selectCount(Wrappers.<Student>lambdaQuery().eq(Student::getPhone,phone));
            if (count > 0){
                throw new BusinessException(ResponseEnum.PHONE_DUPLICATE);
            }
            student.setPhone(phone);
        }

        if (StringUtils.hasLength(studentNo) && !studentNo.equals(student.getStudentNo())){
            Long count = studentMapper.selectCount(Wrappers.<Student>lambdaQuery().eq(Student::getStudentNo,studentNo));
            if (count > 0){
                throw new BusinessException(ResponseEnum.STUDENT_NO_DUPLICATE);
            }
            student.setStudentNo(studentNo);
        }

        if (StringUtils.hasLength(name)){
            student.setName(name);
        }

        if (StringUtils.hasLength(addr)){
            student.setAddr(addr);
        }

        if (gender != null){
            student.setGender(gender);
        }

        if (age != null){
            student.setAge(age);
        }


//        Student save = studentRepository.save(student);
        studentMapper.updateById(student);
        uniqCache.addOne(student);
        return studentConverter.converterStudent(student);
    }

    @Override
    public IPage<Student> getStudentPage(int pageNum, int pageSize, String name) {
        //构造分页对象
        Page<Student> page = new Page<>(pageNum,pageSize);

        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(name),Student::getName,name)
                .orderByAsc(Student::getId);
        return studentMapper.selectPage(page,wrapper);
    }

    @Override
    public ImportResult importStudents(MultipartFile file)  {
        StudentReadListener listener = new StudentReadListener(this, studentConverter);
        ImportResult result;
        try {
            EasyExcel.read(file.getInputStream(), StudentExcelDTO.class, listener)
                    .sheet()
                    .doRead();
            result = listener.getImportResult();
        } catch (IOException e) {
            log.error("Excel文件读取失败", e);
            result = listener.getImportResult();
            result.addError("文件读取失败: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
        } catch (Exception e) {
            log.error("Excel解析过程发生异常，已解析的数据将保留并返回", e);
            result = listener.getImportResult();
            String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            if (msg != null && msg.length() > 200) msg = msg.substring(0, 200) + "...";
            result.addError("解析中止（后续行未处理）: " + msg);
        }
        return result;
    }

    private static final int LEN_NAME   = 50;
    private static final int LEN_EMAIL  = 100;
    private static final int LEN_PHONE  = 30;
    private static final int LEN_ADDR   = 500;
    private static final int LEN_STUNO  = 40;
    private static final java.util.regex.Pattern EMAIL_PATTERN =
            java.util.regex.Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final java.util.regex.Pattern PHONE_PATTERN =
            java.util.regex.Pattern.compile("^[+]?\\d{7,20}$");

    @Override
    public ImportResult saveBatchWithCheck(List<Student> students, List<String> rowErrors) {
        ImportResult result = new ImportResult();
        result.setTotal(students.size());
        if (rowErrors != null && !rowErrors.isEmpty()) {
            for (String err : rowErrors) result.addError(err);
            result.incrementFailCount(rowErrors.size());
        }

        if (students.isEmpty()) {
            return result;
        }

        Set<String> batchEmails = new HashSet<>();
        Set<String> batchPhones = new HashSet<>();
        Set<String> batchStudentNos = new HashSet<>();

        List<Student> toSave = new ArrayList<>();
        for (Student s : students) {
            List<String> fieldErrors = validateField(s);
            if (!fieldErrors.isEmpty()) {
                result.addError("字段校验失败: 姓名[" + s.getName() + "] " + String.join(",", fieldErrors));
                result.incrementFailCount(1);
                continue;
            }

            List<String> dupInBatch = new ArrayList<>();
            if (StringUtils.hasText(s.getEmail())) {
                if (batchEmails.contains(s.getEmail())) {
                    dupInBatch.add("邮箱[" + s.getEmail() + "]");
                } else {
                    batchEmails.add(s.getEmail());
                }
            }
            if (StringUtils.hasText(s.getPhone())) {
                if (batchPhones.contains(s.getPhone())) {
                    dupInBatch.add("手机号[" + s.getPhone() + "]");
                } else {
                    batchPhones.add(s.getPhone());
                }
            }
            if (StringUtils.hasText(s.getStudentNo())) {
                if (batchStudentNos.contains(s.getStudentNo())) {
                    dupInBatch.add("学号[" + s.getStudentNo() + "]");
                } else {
                    batchStudentNos.add(s.getStudentNo());
                }
            }
            if (!dupInBatch.isEmpty()) {
                result.addError("Excel内重复: 姓名[" + s.getName() + "] " + String.join(",", dupInBatch) + " 与本批次前面的行重复");
                result.incrementFailCount(1);
            } else {
                toSave.add(s);
            }
        }

        if (toSave.isEmpty()) {
            return result;
        }

        List<String> emailList = toSave.stream().map(Student::getEmail).filter(StringUtils::hasText).collect(Collectors.toList());
        List<String> phoneList = toSave.stream().map(Student::getPhone).filter(StringUtils::hasText).collect(Collectors.toList());
        List<String> studentNoList = toSave.stream().map(Student::getStudentNo).filter(StringUtils::hasText).collect(Collectors.toList());

        boolean redisReady = true;
        Set<String> dbEmails = new HashSet<>();
        Set<String> dbPhones = new HashSet<>();
        Set<String> dbStudentNos = new HashSet<>();

        List<String> needDbEmail = new ArrayList<>();
        List<String> needDbPhone = new ArrayList<>();
        List<String> needDbStuNo = new ArrayList<>();


        for (String v : emailList){
            Boolean hit = uniqCache.existsEmail(v);
            if (hit == null){
                redisReady = false;
                needDbEmail.add(v);
                needDbPhone.addAll(phoneList);
                needDbStuNo.addAll(studentNoList);
                break;
            } else if (hit) {
                dbEmails.add(v);
            }
        }
        if (redisReady){
            for (String v : phoneList){
                Boolean hit = uniqCache.existsPhone(v);
                if (hit == null){
                    redisReady = false;
                    needDbEmail.addAll(emailList);
                    needDbPhone.clear();
                    needDbPhone.addAll(phoneList);
                    needDbStuNo.addAll(studentNoList);
                    break;
                } else if (hit) {
                    dbPhones.add(v);
                }
            }
        }
        if (redisReady){
            for (String v : studentNoList){
                Boolean hit = uniqCache.existsStudentNo(v);
                if (hit == null){
                    redisReady = false;
                    needDbEmail.addAll(emailList);
                    needDbPhone.addAll(phoneList);
                    needDbStuNo.clear();
                    needDbStuNo.addAll(studentNoList);
                    break;
                } else if (hit) {
                    dbStudentNos.add(v);
                }
            }
        }

        if (redisReady) {
            log.info("✅ 查重模式：REDIS（本批次待查重候选 email={} 条 / phone={} 条 / studentNo={} 条；命中重复：email {} / phone {} / studentNo {}）",
                    emailList.size(), phoneList.size(), studentNoList.size(),
                    dbEmails.size(), dbPhones.size(), dbStudentNos.size());
            // 如果 Redis 里命中了重复（非空），说明它已经拦截成功，不再需要走 DB IN 查询 → 保持 needDb 空即可
        } else if (!redisReady) {
            log.info("ℹ️  查重模式：MYSQL_FALLBACK（本次走 3 次 SELECT IN，不影响导入正确性）");
            if (!needDbEmail.isEmpty()) {
                List<Student> dbEmailList = studentMapper.selectList(Wrappers.<Student>lambdaQuery()
                        .select(Student::getEmail).in(Student::getEmail, needDbEmail));
                dbEmails.addAll(dbEmailList.stream().map(Student::getEmail).filter(StringUtils::hasText).collect(Collectors.toSet()));
            }
            if (!needDbPhone.isEmpty()) {
                List<Student> dbPhoneList = studentMapper.selectList(Wrappers.<Student>lambdaQuery()
                        .select(Student::getPhone).in(Student::getPhone, needDbPhone));
                dbPhones.addAll(dbPhoneList.stream().map(Student::getPhone).filter(StringUtils::hasText).collect(Collectors.toSet()));
            }
            if (!needDbStuNo.isEmpty()) {
                List<Student> dbStuList = studentMapper.selectList(Wrappers.<Student>lambdaQuery()
                        .select(Student::getStudentNo).in(Student::getStudentNo, needDbStuNo));
                dbStudentNos.addAll(dbStuList.stream().map(Student::getStudentNo).filter(StringUtils::hasText).collect(Collectors.toSet()));
            }
        }

//        if (!emailList.isEmpty()) {
//            List<Student> dbEmailList = studentMapper.selectList(Wrappers.<Student>lambdaQuery()
//                    .select(Student::getEmail)
//                    .in(Student::getEmail, emailList));
//            dbEmails = dbEmailList.stream().map(Student::getEmail).filter(StringUtils::hasText).collect(Collectors.toSet());
//        }
//        if (!phoneList.isEmpty()) {
//            List<Student> dbPhoneList = studentMapper.selectList(Wrappers.<Student>lambdaQuery()
//                    .select(Student::getPhone)
//                    .in(Student::getPhone, phoneList));
//            dbPhones = dbPhoneList.stream().map(Student::getPhone).filter(StringUtils::hasText).collect(Collectors.toSet());
//        }
//        if (!studentNoList.isEmpty()) {
//            List<Student> dbStudentNoList = studentMapper.selectList(Wrappers.<Student>lambdaQuery()
//                    .select(Student::getStudentNo)
//                    .in(Student::getStudentNo, studentNoList));
//            dbStudentNos = dbStudentNoList.stream().map(Student::getStudentNo).filter(StringUtils::hasText).collect(Collectors.toSet());
//        }

        List<Student> finalSave = new ArrayList<>();
        for (Student s : toSave) {
            List<String> dbDup = new ArrayList<>();
            if (StringUtils.hasText(s.getEmail()) && dbEmails.contains(s.getEmail())) {
                dbDup.add("邮箱[" + s.getEmail() + "]");
            }
            if (StringUtils.hasText(s.getPhone()) && dbPhones.contains(s.getPhone())) {
                dbDup.add("手机号[" + s.getPhone() + "]");
            }
            if (StringUtils.hasText(s.getStudentNo()) && dbStudentNos.contains(s.getStudentNo())) {
                dbDup.add("学号[" + s.getStudentNo() + "]");
            }
            if (!dbDup.isEmpty()) {
                result.addError("数据库重复: 姓名[" + s.getName() + "] " + String.join(",", dbDup) + " 已在数据库中存在");
                result.incrementFailCount(1);
            } else {
                finalSave.add(s);
            }
        }

        if (!finalSave.isEmpty()) {
            try {
                boolean saved = saveBatch(finalSave, 1000);
                if (saved) {
                    result.addSuccess(finalSave.size());
                    uniqCache.addSavedBatch(finalSave);
                    log.info("批量入库成功，实际写入 {} 条", finalSave.size());
                } else {
                    result.addError("数据库保存未生效（saveBatch 返回 false），共 " + finalSave.size() + " 条未写入");
                    result.incrementFailCount(finalSave.size());
                }
            } catch (Exception dbEx) {
                log.error("批量入库失败，批次条数=" + finalSave.size(), dbEx);
                String simplified = simplifyDbException(dbEx);
                result.addError("数据库写入失败: " + simplified + "（受影响批次 " + finalSave.size() + " 条，请修正数据后重试）");
                result.incrementFailCount(finalSave.size());
            }
        }
        return result;
    }

    private List<String> validateField(Student s) {
        List<String> errors = new ArrayList<>();
        if (StringUtils.hasLength(s.getName()) && s.getName().length() > LEN_NAME) {
            errors.add("姓名字段长度" + s.getName().length() + "超出上限" + LEN_NAME);
        }
        if (StringUtils.hasLength(s.getEmail())) {
            if (s.getEmail().length() > LEN_EMAIL) {
                errors.add("邮箱长度" + s.getEmail().length() + "超出上限" + LEN_EMAIL);
            } else if (!EMAIL_PATTERN.matcher(s.getEmail()).matches()) {
                errors.add("邮箱格式非法[" + s.getEmail() + "]");
            }
        }
        if (StringUtils.hasLength(s.getPhone())) {
            if (s.getPhone().length() > LEN_PHONE) {
                errors.add("手机号长度" + s.getPhone().length() + "超出上限" + LEN_PHONE);
            } else if (!PHONE_PATTERN.matcher(s.getPhone()).matches()) {
                errors.add("手机号格式非法[" + s.getPhone() + "]（仅允许数字与前导+号）");
            }
        }
        if (StringUtils.hasLength(s.getStudentNo()) && s.getStudentNo().length() > LEN_STUNO) {
            errors.add("学号长度" + s.getStudentNo().length() + "超出上限" + LEN_STUNO);
        }
        if (StringUtils.hasLength(s.getAddr()) && s.getAddr().length() > LEN_ADDR) {
            errors.add("地址长度" + s.getAddr().length() + "超出上限" + LEN_ADDR);
        }
        if (s.getAge() != null && (s.getAge() < 0 || s.getAge() > 150)) {
            errors.add("年龄值[" + s.getAge() + "]超出范围0-150");
        }
        if (s.getGender() != null && s.getGender() != 0 && s.getGender() != 1) {
            errors.add("性别值[" + s.getGender() + "]非法(仅允许0/1)");
        }
        return errors;
    }

    private String simplifyDbException(Exception dbEx) {
        Throwable t = dbEx;
        while (t.getCause() != null && t.getCause() != t) {
            t = t.getCause();
        }
        String msg = t.getMessage() == null ? dbEx.getClass().getSimpleName() : t.getMessage();
        if (msg.length() > 200) {
            msg = msg.substring(0, 200) + "...";
        }
        return msg.replaceAll("\\s+", " ");
    }

    @Override
    public void exportStudents(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName;
        try {
            fileName = URLEncoder.encode("学生信息表", "UTF-8").replaceFirst("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            log.error("文件编码格式错误", e);
            throw new RuntimeException("文件编码格式错误");
        }

        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        int pageNum = 1;
        int pageSize = 5000;
        ExcelWriter excelWriter = null;
        try {
            excelWriter = EasyExcel.write(response.getOutputStream(), StudentExcelDTO.class).build();

            WriteSheet writeSheet = EasyExcel.writerSheet("学生列表").build();

            while (true) {
                Page<Student> page = this.page(new Page<>(pageNum, pageSize));
                List<Student> records = page.getRecords();

                if (records.isEmpty()) {
                    break;
                }

                List<StudentExcelDTO> dtoList = studentConverter.converterStudent(records);
                excelWriter.write(dtoList, writeSheet);

                if (records.size() < pageSize) {
                    break;
                }
                pageNum++;
            }
        } finally {
            if (excelWriter != null){
                excelWriter.finish();
            }
        }
    }

}
