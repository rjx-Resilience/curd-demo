package org.example.curddemo.service.impl;

import jakarta.transaction.Transactional;
import org.example.curddemo.common.exception.BusinessException;
import org.example.curddemo.common.exception.ResponseEnum;
import org.example.curddemo.converter.studentConverter;
import org.example.curddemo.dao.StudentRepository;
import org.example.curddemo.dto.StudentDTO;
import org.example.curddemo.entity.Student;
import org.example.curddemo.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class StudentServiceimpl implements StudentService {

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    studentConverter studentConverter;


    @Override
    public List<StudentDTO> getAllStudents() {
        List<Student> students = studentRepository.findAll();
        return studentConverter.getStudentList(students);
    }

    @Override
    public StudentDTO getStudentById(Long id) {
        Student student = studentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("为找到该ID对应的学生"));
        return  studentConverter.converterStudent(student);

    }

    @Override
    public Long addNewStudent(StudentDTO studentDTO) {
        //校验邮箱
        if (StringUtils.hasLength(studentDTO.getEmail())) {
            if (studentRepository.existsByEmail(studentDTO.getEmail())) {
                throw new BusinessException(ResponseEnum.EMAIL_DUPLICATE);
            }
        }

        // 2. 校验学号 (学号通常是必填且唯一的)
        if (studentRepository.existsByStudentNo(studentDTO.getStudentNo())) {
            throw new BusinessException(ResponseEnum.STUDENT_NO_DUPLICATE);
        }

        // 3. 校验手机号 (假设手机号非空)
        if (StringUtils.hasLength(studentDTO.getPhone())) {
            if (studentRepository.existsByPhone(studentDTO.getPhone())) {
                throw new BusinessException(ResponseEnum.PHONE_DUPLICATE);
            }
        }
        Student save = studentRepository.save(studentConverter.converterStudent(studentDTO));
        return save.getId();
    }

    @Override
    public void deleteStudentById(Long id) {
        studentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("id:"+ id + "doesn't exist!" ));
        studentRepository.deleteById(id);
    }

    @Transactional
    @Override
    public StudentDTO updateStudentById(Long id, String name, Integer age, String email, Integer gender, String phone, String addr, String studentNo) {
        Student student = studentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("id:" + id + "doesn't exist!"));

        if (StringUtils.hasLength(email) && !student.getEmail().equals(email)){
            if (studentRepository.existsByEmail(email)){
                throw new BusinessException(ResponseEnum.EMAIL_DUPLICATE);
            }
            student.setEmail(email);
        }
        if (StringUtils.hasLength(phone) && !student.getPhone().equals(phone)){
            if (studentRepository.existsByPhone(phone)){
                throw new BusinessException(ResponseEnum.PHONE_DUPLICATE);
            }
            student.setPhone(phone);
        }

        if (StringUtils.hasLength(studentNo) && !student.getStudentNo().equals(studentNo)){
            if (studentRepository.existsByStudentNo(studentNo)){
                throw new BusinessException(ResponseEnum.STUDENT_NO_DUPLICATE);
            }
            student.setStudentNo(studentNo);
        }

        if (StringUtils.hasLength(name)){
            student.setName(name);
        }

        if (StringUtils.hasLength(addr)){
            student.setAddr(addr);
        }

        if (gender != null){
            student.setGender(gender);
        }

        if (age != null){
            student.setAge(age);
        }
        Student save = studentRepository.save(student);
        return studentConverter.converterStudent(save);
    }
}
