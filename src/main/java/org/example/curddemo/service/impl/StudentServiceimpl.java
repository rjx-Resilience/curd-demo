package org.example.curddemo.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
//import jakarta.transaction.Transactional;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.curddemo.common.exception.BusinessException;
import org.example.curddemo.common.exception.ResponseEnum;
import org.example.curddemo.converter.studentConverter;
//import org.example.curddemo.dao.StudentRepository;
import org.example.curddemo.dto.StudentDTO;
import org.example.curddemo.dto.importDTO.StudentExcelDTO;
import org.example.curddemo.entity.Student;
import org.example.curddemo.listener.StudentReadListener;
import org.example.curddemo.mapper.StudentMapper;
import org.example.curddemo.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Wrapper;
import java.util.List;
@Slf4j
@Service
public class StudentServiceimpl extends ServiceImpl<StudentMapper,Student> implements StudentService {

//    @Autowired
//    StudentRepository studentRepository;

    @Autowired
    studentConverter studentConverter;

    @Autowired
    StudentMapper studentMapper;



    @Override
    public List<StudentDTO> getAllStudents() {
//        List<Student> students = studentRepository.findAll();
        List<Student> students = studentMapper.selectList(null);
        return studentConverter.getStudentList(students);
    }

    @Override
    public StudentDTO getStudentById(Long id) {
//        Student student = studentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("为找到该ID对应的学生"));
        Student student = studentMapper.selectById(id);
        if (student == null){
            throw new IllegalArgumentException("学生不存在,ID: " +id);
        }
        return  studentConverter.converterStudent(student);

    }

    @Override
    public Long addNewStudent(StudentDTO studentDTO) {
        //校验邮箱
        if (StringUtils.hasLength(studentDTO.getEmail())) {
//            if (studentRepository.existsByEmail(studentDTO.getEmail())) {
//                throw new BusinessException(ResponseEnum.EMAIL_DUPLICATE);
//            }
            Long count = studentMapper.selectCount(Wrappers.<Student>lambdaQuery().eq(Student :: getEmail, studentDTO.getEmail()));
            if (count > 0){
                throw new BusinessException(ResponseEnum.EMAIL_DUPLICATE);
            }
        }

        // 2. 校验学号 (学号通常是必填且唯一的)
        if (StringUtils.hasLength(studentDTO.getStudentNo())){
//        if (studentRepository.existsByStudentNo(studentDTO.getStudentNo())) {
//            throw new BusinessException(ResponseEnum.STUDENT_NO_DUPLICATE);
//        }
            Long countNo = studentMapper.selectCount(Wrappers.<Student>lambdaQuery().eq(Student::getStudentNo,studentDTO.getStudentNo()));
            if (countNo > 0){
                throw new BusinessException(ResponseEnum.STUDENT_NO_DUPLICATE);
            }
        }

        // 3. 校验手机号 (假设手机号非空)
        if (StringUtils.hasLength(studentDTO.getPhone())) {
//            if (studentRepository.existsByPhone(studentDTO.getPhone())) {
//                throw new BusinessException(ResponseEnum.PHONE_DUPLICATE);
//            }
            Long countPhone = studentMapper.selectCount(Wrappers.<Student>lambdaQuery().eq(Student::getPhone,studentDTO.getPhone()));
            if (countPhone > 0){
                throw new BusinessException(ResponseEnum.PHONE_DUPLICATE);
            }
        }
//        Student save = studentRepository.save(studentConverter.converterStudent(studentDTO));
        Student student = studentConverter.converterStudent(studentDTO);
        studentMapper.insert(student);
        return student.getId();
    }

    @Override
    public void deleteStudentById(Long id) {
//        studentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("id:"+ id + "doesn't exist!" ));
        Student student = studentMapper.selectById(id);
        if (student == null){
            throw new IllegalArgumentException("id:"+ id + "doesn't exist!");
        }
        studentMapper.deleteById(id);
    }

//    @Transactional
    @Override
    public StudentDTO updateStudentById(Long id, String name, Integer age, String email, Integer gender, String phone, String addr, String studentNo) {
//        Student student = studentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("id:" + id + "doesn't exist!"));
        Student student = studentMapper.selectById(id);
        if (student == null){
            throw new IllegalArgumentException("id:" + id + "doesn't exist!");
        }
        if (StringUtils.hasLength(email) && !student.getEmail().equals(email)){
            if (studentMapper.countByEmail(email) > 0){
                throw new BusinessException(ResponseEnum.EMAIL_DUPLICATE);
            }
            student.setEmail(email);
        }
        if (StringUtils.hasLength(phone) && !student.getPhone().equals(phone)){
            if (studentMapper.countByPhone(phone) > 0){
                throw new BusinessException(ResponseEnum.PHONE_DUPLICATE);
            }
            student.setPhone(phone);
        }

        if (StringUtils.hasLength(studentNo) && !student.getStudentNo().equals(studentNo)){
            if (studentMapper.countByStudentNo(studentNo) > 0){
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
//        Student save = studentRepository.save(student);
        studentMapper.updateById(student);
        return studentConverter.converterStudent(student);
    }

    @Override
    public IPage<Student> getStudentPage(int pageNum, int pageSize, String name) {
        //构造分页对象
        Page<Student> page = new Page<>(pageNum,pageSize);

        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(name),Student::getName,name)
                .orderByDesc(Student::getId);
        return studentMapper.selectPage(page,wrapper);
    }

    @Override
    public void importStudents(MultipartFile file)  {
        try {
            EasyExcel.read(file.getInputStream(), StudentExcelDTO.class,
                            new StudentReadListener(this,studentConverter))
                    .sheet()
                    .doRead();
        } catch (IOException e) {
            throw new RuntimeException("文件读取失败",e);
        }
    }

    @Override
    public void exportStudents(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName;
        try {
            fileName = URLEncoder.encode("学生信息表", "UTF-8").replaceFirst("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            log.error("文件编码格式错误", e);
            throw new RuntimeException("文件编码格式错误");
        }

        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        int pageNum = 1;
        int pageSize = 5000;
        ExcelWriter excelWriter = null;
        try {
            excelWriter = EasyExcel.write(response.getOutputStream(), StudentExcelDTO.class).build();

            WriteSheet writeSheet = EasyExcel.writerSheet("学生列表").build();

            while (true) {
                Page<Student> page = this.page(new Page<>(pageNum, pageSize));
                List<Student> records = page.getRecords();

                if (records.isEmpty()) {
                    break;
                }

                List<StudentExcelDTO> dtoList = studentConverter.converterStudent(records);
                excelWriter.write(dtoList, writeSheet);

                if (records.size() < pageSize) {
                    break;
                }
                pageNum++;
            }
        } finally {
            if (excelWriter != null){
                excelWriter.finish();
            }
        }
    }

}
