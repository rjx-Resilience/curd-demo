package org.example.curddemo.service;


import org.example.curddemo.dto.StudentDTO;

import java.util.List;

public interface StudentService {

    List<StudentDTO> getAllStudents();

    StudentDTO getStudentById(Long id);

    Long addNewStudent(StudentDTO studentDTO);

    void deleteStudentById(Long id);

    StudentDTO updateStudentById(Long id, String name, Integer age, String email, Integer gender, String phone, String addr, String studentNo);
}


