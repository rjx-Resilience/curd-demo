package org.example.curddemo.converter;


import org.example.curddemo.dto.StudentDTO;
import org.example.curddemo.dto.importDTO.StudentExcelDTO;
import org.example.curddemo.entity.Student;

import java.util.List;


public interface studentConverter {
    StudentDTO converterStudent(Student student);

    Student converterStudent(StudentDTO studentDTO);

    List<StudentDTO> getStudentList(List<Student> students);

    Student converterStudent(StudentExcelDTO studentExcelDTO);

    List<StudentExcelDTO> converterStudent(List<Student> records);
}
