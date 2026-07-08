package org.example.curddemo.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.read.metadata.holder.ReadRowHolder;
import lombok.extern.slf4j.Slf4j;
import org.example.curddemo.converter.studentConverter;
import org.example.curddemo.dto.importDTO.StudentExcelDTO;
import org.example.curddemo.entity.Student;
import org.example.curddemo.service.StudentService;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class StudentReadListener extends AnalysisEventListener<StudentExcelDTO> {

    private final StudentService studentService;
    private final studentConverter studentConverter;

    private static final int BATCH_COUNT = 100;

    private final List<Student> cacheDataList = new ArrayList<>(BATCH_COUNT);
    private final List<String> cacheRowErrors = new ArrayList<>();

    private final StudentService.ImportResult importResult = new StudentService.ImportResult();
    private int totalReadCount = 0;

    public StudentReadListener(StudentService studentService, studentConverter studentConverter) {
        this.studentService = studentService;
        this.studentConverter = studentConverter;
    }

    @Override
    public void invoke(StudentExcelDTO data, AnalysisContext context) {
        totalReadCount++;
        int rowIndex = getRowIndex(context);
        if (log.isDebugEnabled()) {
            log.debug("解析第{}行数据:{}", rowIndex, data);
        } else if (totalReadCount % 1000 == 0) {
            log.info("已解析 {} 行…", totalReadCount);
        }

        List<String> fieldErrors = validateRow(data, rowIndex);
        if (!fieldErrors.isEmpty()) {
            for (String err : fieldErrors) {
                importResult.addError(err);
            }
            importResult.incrementFailCount(1);
            log.warn("第{}行行校验未通过，失败原因: {}", rowIndex, fieldErrors);
            return;
        }

        Student student = studentConverter.converterStudent(data);
        cacheDataList.add(student);

        if (cacheDataList.size() >= BATCH_COUNT) {
            flushAndSave();
        }
    }

    private List<String> validateRow(StudentExcelDTO data, int rowIndex) {
        List<String> errors = new ArrayList<>();
        if (data == null) {
            errors.add("第" + rowIndex + "行: 数据为空");
            return errors;
        }
        if (!StringUtils.hasText(data.getName())) {
            errors.add("第" + rowIndex + "行: 姓名不能为空");
        }
        if (!StringUtils.hasText(data.getStudentNo())) {
            errors.add("第" + rowIndex + "行: 学号不能为空");
        }
        if (!StringUtils.hasText(data.getEmail())) {
            errors.add("第" + rowIndex + "行: 邮箱不能为空");
        }
        if (!StringUtils.hasText(data.getPhone())) {
            errors.add("第" + rowIndex + "行: 手机号不能为空");
        }
        if (data.getGender() != null && data.getGender() != 0 && data.getGender() != 1) {
            errors.add("第" + rowIndex + "行: 性别值非法，只能为0(女)或1(男)，当前值=" + data.getGender());
        }
        if (data.getAge() != null && (data.getAge() < 0 || data.getAge() > 150)) {
            errors.add("第" + rowIndex + "行: 年龄值非法，当前值=" + data.getAge());
        }
        return errors;
    }

    private int getRowIndex(AnalysisContext context) {
        try {
            ReadRowHolder rowHolder = context.readRowHolder();
            if (rowHolder != null) {
                return rowHolder.getRowIndex() + 1;
            }
        } catch (Exception ignore) {
        }
        return totalReadCount + 1;
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        flushAndSave();
        importResult.setTotal(totalReadCount);
        log.info("所有数据处理完成！总计解析 {} 行，成功 {} 条，失败 {} 条，错误详情 {} 条",
                totalReadCount,
                importResult.getSuccessCount(),
                importResult.getFailCount(),
                importResult.getErrors().size());
    }

    private void flushAndSave() {
        if (cacheDataList.isEmpty() && cacheRowErrors.isEmpty()) {
            return;
        }
        log.info("处理批次数据：待校验入库 {} 条，行级校验错误 {} 条", cacheDataList.size(), cacheRowErrors.size());

        List<Student> batchStudents = new ArrayList<>(cacheDataList);
        List<String> batchErrors = new ArrayList<>(cacheRowErrors);

        StudentService.ImportResult batchResult = studentService.saveBatchWithCheck(batchStudents, batchErrors);

        importResult.merge(batchResult);

        cacheDataList.clear();
        cacheRowErrors.clear();
    }

    public StudentService.ImportResult getImportResult() {
        return importResult;
    }
}
