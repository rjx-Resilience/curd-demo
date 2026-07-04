package org.example.curddemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.curddemo.entity.Student;

import java.util.List;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {
    List<Student> getAll();

    Student getUserById(@Param("id") Long id);

    Student findByName(@Param("name") String name);

    Student findByCondition(@Param("name") String name ,@Param("studentNo") String studentNo);

    @Select("select COUNT(*) from student where email = #{email}")
    int countByEmail(String email);

    @Select("select COUNT(*) from student where student_no = #{studentNo}")
    int countByStudentNo(String studentNo);

    @Select("select COUNT(*) from student where phone = #{phone}")
    int countByPhone(String phone);

}
