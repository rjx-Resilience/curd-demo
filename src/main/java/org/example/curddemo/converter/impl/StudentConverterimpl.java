package org.example.curddemo.converter.impl;


import org.example.curddemo.converter.studentConverter;
import org.example.curddemo.dto.StudentDTO;
import org.example.curddemo.dto.importDTO.StudentExcelDTO;
import org.example.curddemo.entity.Student;
import org.springframework.stereotype.Component;


import java.util.List;
import java.util.stream.Collectors;
@Component
public class StudentConverterimpl  implements studentConverter {
    public StudentDTO converterStudent(Student student){
        if (student == null) {
            return null;
        }
        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setId(student.getId());
        studentDTO.setName(student.getName());
        studentDTO.setAge(student.getAge());
        studentDTO.setEmail(student.getEmail());
        studentDTO.setGender(student.getGender());
        studentDTO.setPhone(student.getPhone());
        studentDTO.setAddr(student.getAddr());
        studentDTO.setStudentNo(student.getStudentNo());
        return studentDTO;
    }
    //用于新增用户
    public  Student converterStudent(StudentDTO studentDTO){
        if (studentDTO == null) {
            return null;
        }
        Student student = new Student();
        student.setId(studentDTO.getId());
        student.setName(studentDTO.getName());
        student.setAge(studentDTO.getAge());
        student.setEmail(studentDTO.getEmail());
        student.setGender(studentDTO.getGender());
        student.setPhone(studentDTO.getPhone());
        student.setAddr(studentDTO.getAddr());
        student.setStudentNo(studentDTO.getStudentNo());
        return student;
    }

    public StudentExcelDTO converterToExcelDTO(Student student){
        if (student == null) {
            return null;
        }
        StudentExcelDTO studentExcelDTO = new StudentExcelDTO();
        studentExcelDTO.setId(student.getId());
        studentExcelDTO.setName(student.getName());
        studentExcelDTO.setAge(student.getAge());
        studentExcelDTO.setEmail(student.getEmail());
        studentExcelDTO.setGender(student.getGender());
        studentExcelDTO.setPhone(student.getPhone());
        studentExcelDTO.setAddr(student.getAddr());
        studentExcelDTO.setStudentNo(student.getStudentNo());
        return studentExcelDTO;
    }

    @Override
    public List<StudentDTO> getStudentList(List<Student> students) {
        if (students == null || students.isEmpty()) {
            return List.of(); // 返回空列表而不是 null，防止前端报错
        }
        // 使用 Stream API 批量转换
        return students.stream()
                .map(this::converterStudent)
                .collect(Collectors.toList());
    }

    @Override
    public Student converterStudent(StudentExcelDTO studentExcelDTO) {
        if (studentExcelDTO == null) {
            return null;
        }
        Student student = new Student();
        student.setId(studentExcelDTO.getId());
        student.setName(studentExcelDTO.getName() != null ? studentExcelDTO.getName().trim() : null);

        student.setAge(studentExcelDTO.getAge());
        student.setEmail(studentExcelDTO.getEmail());
        student.setGender(studentExcelDTO.getGender());
        student.setPhone(studentExcelDTO.getPhone());
        student.setAddr(studentExcelDTO.getAddr() != null ? studentExcelDTO.getAddr().trim() : null);
        student.setStudentNo(studentExcelDTO.getStudentNo());
        return student;
    }

    @Override
    public List<StudentExcelDTO> converterStudent(List<Student> records) {
        if (records == null || records.isEmpty()) {
            return List.of(); // 返回空列表而不是 null，防止前端报错
        }
        // 使用 Stream API 批量转换
        return records.stream()
                .map(this::converterToExcelDTO)
                .collect(Collectors.toList());
    }

}
