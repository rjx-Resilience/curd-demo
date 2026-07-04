package org.example.curddemo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@MapperScan("org.example.curddemo.mapper")
//@EnableJpaRepositories("org.example.curddemo.dao")
public class CurdDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CurdDemoApplication.class, args);
    }

}
