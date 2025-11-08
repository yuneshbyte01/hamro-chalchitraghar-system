package com.hamrochalchitraghar.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HamroChalchitragharSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(HamroChalchitragharSystemApplication.class, args);
    }

}
