package com.onehourjob;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.onehourjob.mapper")
public class OneHourJobApplication {
    public static void main(String[] args) {
        SpringApplication.run(OneHourJobApplication.class, args);
    }
}