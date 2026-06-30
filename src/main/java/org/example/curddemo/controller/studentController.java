package org.example.curddemo.controller;

import org.example.curddemo.Response;
import org.example.curddemo.dto.StudentDTO;
import org.example.curddemo.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class studentController {

    @Autowired
    StudentService studentService;

    @GetMapping("/students")//获取学生列表
    Response<List<StudentDTO>> getAllStudent(){
        List<StudentDTO> allStudents = studentService.getAllStudents();
        return Response.newSuccess(allStudents);
    }

    @GetMapping("/student/{id}")//获取单个学生信息
    Response<StudentDTO> getStudent(@PathVariable("id") Long id){
        StudentDTO student = studentService.getStudentById(id);
        return Response.newSuccess(student);
    }


    @PostMapping("/student")//新增学生信息
    Response<Long> addStudent(@RequestBody StudentDTO studentDTO){
        Long l = studentService.addNewStudent(studentDTO);
        return Response.newSuccess(l);
    }

    @DeleteMapping("/student/{id}")//删除学生信息
    public void deleteStudent(@PathVariable Long id){
        studentService.deleteStudentById(id);
    }


    @PutMapping("/student/{id}")//更新学生信息
    public Response<StudentDTO> updateStudent(@PathVariable Long id,
                                              @RequestParam("name") String name,
                                              @RequestParam("age") Integer age,
                                              @RequestParam("email") String email,
                                              @RequestParam("gender") Integer gender,
                                              @RequestParam("phone") String phone,
                                              @RequestParam("addr") String addr,
                                              @RequestParam("studentNo") String studentNo){
        return Response.newSuccess(studentService.updateStudentById(id,name,age,email,gender,phone,addr,studentNo));
    }
}
