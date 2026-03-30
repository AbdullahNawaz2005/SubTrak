package com.subtrak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SubTrakApplication {
    public static void main(String[] args) {
        SpringApplication.run(SubTrakApplication.class, args);
    }
}
