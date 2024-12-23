package com.nova.narrativa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NarrativaApplication {

    public static void main(String[] args) {
        SpringApplication.run(NarrativaApplication.class, args);
    }

}