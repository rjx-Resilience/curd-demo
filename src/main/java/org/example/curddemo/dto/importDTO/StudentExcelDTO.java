package org.example.curddemo.dto.importDTO;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "利用excel导入学生参数")
@Data
public class StudentExcelDTO {
    @Schema(description = "id,主键，自增", example = "1")
    @ExcelProperty(index = 0, value = "序号")
    private Long id;
    @NotBlank(message = "学号不能为空")
    @Schema(description = "学号", example = "252410601")
    @ExcelProperty(index = 1, value = "学号")
    private String studentNo;
    @NotBlank(message = "姓名不能为空")
    @Schema(description = "姓名", example = "张三")
    @ExcelProperty(index = 2, value = "姓名")
    private String name;
    @Schema(description = "年龄", example = "18")
    @ExcelProperty(index = 3, value = "年龄")
    private Integer age;
    @Schema(description = "性别(0代表女，1代表男)", example = "1")
    @ExcelProperty(index = 4, value = "性别")
    private Integer gender;
    @NotBlank(message = "邮箱不能为空")
    @Schema(description = "邮箱", example = "zhangsan@qq.com")
    @ExcelProperty(index = 5, value = "邮箱")
    private String email;
    @NotBlank(message = "手机号不能为空")
    @Schema(description = "手机号", example = "13800138000")
    @ExcelProperty(index = 6, value = "电话号码")
    private String phone;
    @Schema(description = "地址", example = "北京海淀")
    @ExcelProperty(index = 7, value = "地址")
    private String addr;


}
