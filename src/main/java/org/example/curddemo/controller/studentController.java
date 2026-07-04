package org.example.curddemo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.example.curddemo.Response;
import org.example.curddemo.dto.StudentDTO;
import org.example.curddemo.dto.importDTO.StudentExcelDTO;
import org.example.curddemo.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
@Tag(name = "学生管理模块", description = "包含学生的增删改查接口")
@RestController

public class studentController {

    @Autowired
    StudentService studentService;

    @Operation(summary = "获取所有学生的列表")
    @GetMapping("/students")//获取学生列表
    Response<List<StudentDTO>> getAllStudent(){
        try {
            List<StudentDTO> allStudents = studentService.getAllStudents();
            return Response.newSuccess(allStudents);
        } catch (Exception e) {
            return Response.newFail("获取学生列表是失败: " + e.getMessage());
        }
    }
    @Operation(summary = "根据Id查询学生的信息")
    @GetMapping("/student/{id}")//获取单个学生信息
    Response<StudentDTO> getStudent(@PathVariable("id") Long id){
        try {
            StudentDTO student = studentService.getStudentById(id);
            return Response.newSuccess(student);
        }catch (Exception e){
            return Response.newFail("获取学生ID:" + id + "失败: " + e.getMessage());
        }
    }

    @Operation(summary = "新增学生信息" ,description = "会对id,邮箱,学号,手机号，进行查重")
    @PostMapping("/student")//新增学生信息
    Response<Long> addStudent(@RequestBody StudentDTO studentDTO){
        try {
            Long l = studentService.addNewStudent(studentDTO);
            return Response.newSuccess(l);
        } catch (Exception e) {
            return Response.newFail("添加失败: " + e.getMessage());
        }
    }
    @Operation(summary = "根据id删除学生信息")
    @DeleteMapping("/student/{id}")//删除学生信息
    public void deleteStudent(@PathVariable Long id){
        studentService.deleteStudentById(id);
    }

    @Operation(summary = "根据id更新学生信息")
    @PutMapping("/student/{id}")//更新学生信息
    public Response<StudentDTO> updateStudent(@PathVariable Long id,
                                              @RequestParam("name") String name,
                                              @RequestParam("age") Integer age,
                                              @RequestParam("email") String email,
                                              @RequestParam("gender") Integer gender,
                                              @RequestParam("phone") String phone,
                                              @RequestParam("addr") String addr,
                                              @RequestParam("studentNo") String studentNo) {

        try {
            return Response.newSuccess(studentService.updateStudentById(id, name, age, email, gender, phone, addr, studentNo));
        } catch (Exception e) {
            return Response.newFail("修改失败: " + e.getMessage());
        }
    }

    @Operation(summary = "批量导入学生",description = "支持xlsx格式")
    @PostMapping("/import")
    public Response<String> importStudent(@Parameter(description = "学生信息Excel文件",required = true)
                                          @RequestParam("file") MultipartFile file){
        studentService.importStudents(file);
        return Response.newSuccess("导入完成");
    }

    @GetMapping("/export")
    @Operation(summary = "导出学生报表", description = "将当前所有学生信息导出为 Excel 文件")
    public void exportStudent(HttpServletResponse response) throws IOException {
        studentService.exportStudents(response);
    }
}
