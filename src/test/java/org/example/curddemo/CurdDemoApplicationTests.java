package org.example.curddemo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.curddemo.entity.Student;
import org.example.curddemo.mapper.StudentMapper;
import org.example.curddemo.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import java.util.List;

@SpringBootTest
class CurdDemoApplicationTests {

    @Autowired
    StudentService studentService;

    @Autowired
    StudentMapper studentMapper;
    @Autowired
    private NamedParameterJdbcOperations namedParameterJdbcOperations;

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

    @Test
    void testPage(){
        IPage<Student> page = studentService.getStudentPage(1, 10, null);
        System.out.println(page);
    }

}
