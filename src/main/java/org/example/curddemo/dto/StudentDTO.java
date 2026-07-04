package org.example.curddemo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Schema(description = "学生参数")
@Data
public class StudentDTO {
    @NotBlank(message = "学号不能为空")
    @Schema(description = "id,主键，自增",example = "1")
    private Long id;
    @NotBlank(message = "姓名不能为空")
    @Schema(description = "姓名",example = "张三")
    private String name;
    @Schema(description = "年龄",example = "18")
    private Integer age;
    @NotBlank(message = "邮箱不能为空")
    @Schema(description = "邮箱",example = "zhangsan@qq.com")
    private String email;
    @Schema(description = "性别(0代表女，1代表男)",example = "1")
    private Integer gender;
    @NotBlank(message = "手机号不能为空")
    @Schema(description = "手机号",example = "13800138000")
    private String phone;
    @Schema(description = "地址",example = "北京海淀")
    private String addr;
    @NotBlank(message = "学号不能为空")
    @Schema(description = "学号",example = "252410601")
    private String studentNo;
}
