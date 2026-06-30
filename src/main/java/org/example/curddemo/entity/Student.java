package org.example.curddemo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Generated;
@Entity
@Data
@Table(name = "student")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="name")
    private String name;

    @Column(name = "age")
    private Integer age;

    @Column(name = "email")
    private String email;

    @Column(name = "gender")
    private Integer gender;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address")
    private String addr;

    @Column(name = "student_no")
    private String studentNo;
}
