package com.nova.narrativa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = {"com.nova.narrativa", "com.nova.narrativa.domain.llm.service"})
@EnableScheduling
public class NarrativaApplication {

    public static void main(String[] args) {
        SpringApplication.run(NarrativaApplication.class, args);
    }

}