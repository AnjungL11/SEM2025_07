package com.g07;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@MapperScan("com.g07.mapper")
public class G07Application {
    public static void main(String[] args) {
        SpringApplication.run(G07Application.class, args);
    }
}