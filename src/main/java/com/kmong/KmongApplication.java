package com.kmong;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KmongApplication {

    public static void main(String[] args) {
        SpringApplication.run(KmongApplication.class, args);
    }

}
