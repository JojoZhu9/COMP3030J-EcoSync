package com.example.bibilabo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.example.bibilabo.mapper")
@EnableScheduling
public class BibilaboApplication {

    public static void main(String[] args) {
        SpringApplication.run(BibilaboApplication.class, args);
    }

}