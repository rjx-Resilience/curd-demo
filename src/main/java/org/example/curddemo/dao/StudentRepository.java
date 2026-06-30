package org.example.curddemo.dao;


import org.example.curddemo.dto.StudentDTO;
import org.example.curddemo.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findByEmail(String email);


    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByStudentNo(String studentNo);
}
