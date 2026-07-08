package org.example.curddemo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
//import jakarta.persistence.*;
import lombok.Data;

//@Entity
@Data
//@Table(name = "student")
@TableName("student")
public class Student {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long id;

//    @Column(name="name")
    @TableField("name")
    private String name;

//    @Column(name = "age")
    @TableField("age")
    private Integer age;

//    @Column(name = "email")

    @TableField("email")
    private String email;

//    @Column(name = "gender")
    @TableField("gender")
    private Integer gender;

//    @Column(name = "phone")
    @TableField("phone")
    private String phone;

//    @Column(name = "address")
    @TableField("address")
    private String addr;

//    @Column(name = "student_no")
    @TableField("student_no")
    private String studentNo;
}
