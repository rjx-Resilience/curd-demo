package org.example.curddemo.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;
import org.example.curddemo.dto.StudentDTO;
import org.example.curddemo.entity.Student;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface StudentService extends IService<Student> {

    List<StudentDTO> getAllStudents();

    StudentDTO getStudentById(Long id);

    Long addNewStudent(StudentDTO studentDTO);

    void deleteStudentById(Long id);

    StudentDTO updateStudentById(Long id, String name, Integer age, String email, Integer gender, String phone, String addr, String studentNo);

    IPage<Student> getStudentPage(int pageNum ,int pageSize, String name);

    void importStudents(MultipartFile file);

    void exportStudents(HttpServletResponse response) throws IOException;
}


