package org.example.curddemo.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import org.example.curddemo.dto.StudentDTO;
import org.example.curddemo.entity.Student;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface StudentService extends IService<Student> {

    List<StudentDTO> getAllStudents();

    StudentDTO getStudentById(Long id);

    Long addNewStudent(StudentDTO studentDTO);

    void deleteStudentById(Long id);

    StudentDTO updateStudentById(Long id, String name, Integer age, String email, Integer gender, String phone, String addr, String studentNo);

    IPage<Student> getStudentPage(int pageNum ,int pageSize, String name);

    ImportResult importStudents(MultipartFile file);

    ImportResult saveBatchWithCheck(List<Student> students, List<String> rowErrors);

    void exportStudents(HttpServletResponse response) throws IOException;

    @Data
    class ImportResult {
        private static final int MAX_ERROR_DETAIL = 1000;
        private int totalCount;
        private int successCount;
        private int failCount;
        private int truncatedErrorCount;
        private String summary;
        private List<String> errors = new ArrayList<>();

        public void addError(String error) {
            if (this.errors.size() < MAX_ERROR_DETAIL) {
                this.errors.add(error);
            } else {
                this.truncatedErrorCount++;
            }
        }

        public void addSuccess(int count) {
            this.successCount += count;
        }

        public void incrementFailCount(int rows) {
            this.failCount += Math.max(rows, 0);
        }

        public void setTotal(int total) {
            this.totalCount = total;
        }

        public String getSummary() {
            return buildSummary();
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        private String buildSummary() {
            if (this.summary != null && !this.summary.isBlank()) {
                return this.summary;
            }
            if (this.totalCount == 0) {
                return "未解析到任何数据";
            }
            StringBuilder sb = new StringBuilder()
                    .append("导入完成，共").append(this.totalCount).append("条：")
                    .append("成功").append(this.successCount).append("条")
                    .append("，失败").append(this.failCount).append("条");
            if (!this.errors.isEmpty() || this.truncatedErrorCount > 0) {
                sb.append("，错误详情 ").append(this.errors.size()).append("条");
                if (this.truncatedErrorCount > 0) {
                    sb.append("（另有 ").append(this.truncatedErrorCount)
                      .append(" 条错误已省略，仅保留前 ").append(MAX_ERROR_DETAIL).append(" 条详情）");
                }
            }
            if (this.failCount > 0) {
                sb.append("（请修正错误数据后重新上传）");
            } else {
                sb.append("，全部通过校验并入库");
            }
            return sb.toString();
        }

        public void merge(ImportResult batchResult) {
            if (batchResult == null) return;
            this.successCount += batchResult.getSuccessCount();
            this.failCount    += batchResult.getFailCount();
            if (batchResult.getErrors() != null && !batchResult.getErrors().isEmpty()) {
                for (String err : batchResult.getErrors()) {
                    addError(err);
                }
            }
            this.truncatedErrorCount += batchResult.getTruncatedErrorCount();
        }
    }
}


