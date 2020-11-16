package com.ddkolesnik.avitotraiding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AvitoTraidingApplication {

    public static void main(String[] args) {
        SpringApplication.run(AvitoTraidingApplication.class, args);
    }

}
