package com.miku.cmd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@ComponentScan(basePackages = "com.miku")
public class MikuApplication {

    public static void main(String[] args) {
        SpringApplication.run(MikuApplication.class, args);
    }

}
