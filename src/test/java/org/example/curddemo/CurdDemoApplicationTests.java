package org.example.curddemo;

import org.example.curddemo.entity.Student;
import org.example.curddemo.mapper.StudentMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class CurdDemoApplicationTests {

    @Autowired
    StudentMapper studentMapper;

    @Test
    void contextLoads() {
        List<Student> all = studentMapper.getAll();
        System.out.println(all);
    }

    @Test
    void test(){
        Student student = studentMapper.selectById(1);
        System.out.println(student);
    }

}
