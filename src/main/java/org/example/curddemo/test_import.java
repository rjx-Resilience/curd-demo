package org.example.curddemo;

import com.alibaba.excel.EasyExcel;
import org.example.curddemo.dto.importDTO.StudentExcelDTO;

import java.util.ArrayList;
import java.util.List;

public class test_import {
    public static void main(String[] args) {
        String fileName = "test_import_students.xlsx";

        List<StudentExcelDTO> list = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            StudentExcelDTO data = new StudentExcelDTO();
            data.setName("测试学生" + i);
            data.setAge(18 + (i % 5)); // 随机年龄在 18-22 之间
            data.setEmail("student" + i + "@test.com");
            data.setStudentNo("202400" + i);
            list.add(data);
        }

        // 写入文件
        EasyExcel.write(fileName, StudentExcelDTO.class).sheet("学生列表").doWrite(list);
        System.out.println("测试文件已生成：" + fileName);
    }
}
