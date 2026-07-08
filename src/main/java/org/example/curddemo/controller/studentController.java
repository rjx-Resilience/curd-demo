package org.example.curddemo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.curddemo.Response;
import org.example.curddemo.dto.StudentDTO;
import org.example.curddemo.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.curddemo.entity.Student;
@Slf4j
@Tag(name = "学生管理模块", description = "包含学生的增删改查接口")
@RestController

public class studentController {

    private static final int PAGE_SIZE_MIN = 1;
    private static final int PAGE_SIZE_MAX = 1000;
    private static final int PAGE_NUM_MIN = 1;
    private static final int ALL_LIST_MAX = 1000;

    @Autowired
    StudentService studentService;

    @Operation(summary = "获取所有学生的列表（最多前1000条）", description = "大数量请使用 /students/page 分页接口")
    @GetMapping("/students")//获取学生列表
    Response<List<StudentDTO>> getAllStudent(){
        try {
            List<StudentDTO> allStudents = studentService.getAllStudents();
            if (allStudents != null && allStudents.size() > ALL_LIST_MAX) {
                return Response.newSuccess(allStudents.subList(0, ALL_LIST_MAX));
            }
            return Response.newSuccess(allStudents);
        } catch (Exception e) {
            return Response.newFail("获取学生列表是失败: " + e.getMessage());
        }
    }

    @Operation(summary = "分页查询学生", description = "pageSize 范围 1-1000，超出会自动截断到上限")
    @GetMapping("/students/page")
    Response<IPage<Student>> pageStudents(
            @Parameter(description = "页码，从 1 开始", example = "1") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数，范围 1-1000", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "姓名模糊查询（可选）") @RequestParam(required = false) String name) {
        try {
            if (pageNum < PAGE_NUM_MIN) pageNum = PAGE_NUM_MIN;
            if (pageSize < PAGE_SIZE_MIN) pageSize = PAGE_SIZE_MIN;
            if (pageSize > PAGE_SIZE_MAX) pageSize = PAGE_SIZE_MAX;
            return Response.newSuccess(studentService.getStudentPage(pageNum, pageSize, name));
        } catch (Exception e) {
            return Response.newFail("分页查询失败: " + e.getMessage());
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

    @Operation(summary = "批量导入学生",description = "支持xlsx格式，自动校验必填字段及邮箱/手机号/学号唯一性，返回导入成功/失败详情（通过data.summary获取汇总）")
    @PostMapping("/import")
    public Response<StudentService.ImportResult> importStudent(@Parameter(description = "学生信息Excel文件",required = true)
                                          @RequestParam("file") MultipartFile file){
        try {
            StudentService.ImportResult result = studentService.importStudents(file);
            return Response.newSuccess(result);
        } catch (Exception e) {
            log.error("导入接口不可恢复异常", e);
            StudentService.ImportResult partial = new StudentService.ImportResult();
            partial.addError("导入失败: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
            partial.setSummary("接口异常，未确认数据是否入库，请先查库确认");
            return Response.newFail(partial, "导入失败: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
        }
    }

    @GetMapping("/export")
    @Operation(summary = "导出学生报表", description = "将当前所有学生信息导出为 Excel 文件")
    public void exportStudent(HttpServletResponse response) throws IOException {
        studentService.exportStudents(response);
    }
}
