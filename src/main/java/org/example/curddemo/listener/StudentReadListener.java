package org.example.curddemo.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import lombok.extern.slf4j.Slf4j;
import org.example.curddemo.converter.studentConverter;
import org.example.curddemo.dto.StudentDTO;
import org.example.curddemo.dto.importDTO.StudentExcelDTO;
import org.example.curddemo.entity.Student;
import org.example.curddemo.mapper.StudentMapper;
import org.example.curddemo.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class StudentReadListener extends AnalysisEventListener<StudentExcelDTO> {



    private final StudentService studentService;
    private final studentConverter studentConverter;

    private static final int BATCH_COUNT=100;

    private List<Student> cacheDataList = new ArrayList<>(BATCH_COUNT);

    public StudentReadListener(StudentService studentService, studentConverter studentConverter) {
        this.studentService = studentService;
        this.studentConverter = studentConverter;
    }




    @Override
    public void invoke(StudentExcelDTO data, AnalysisContext context) {
        log.info("解析到一条新数据:{}",data);
        Student student = studentConverter.converterStudent(data);
        cacheDataList.add(student);

        if (cacheDataList.size() >= BATCH_COUNT){
            saveData();
            cacheDataList.clear();
        }
    }



    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        saveData();
        log.info("所有数据处理完成！");
    }

    private void saveData(){
        if (cacheDataList.isEmpty()){
            return;
        }
        log.info("{}条数据，开始存储数据",cacheDataList.size());

        studentService.saveBatch(cacheDataList,1000);
        log.info("存储成功");
    }
}
