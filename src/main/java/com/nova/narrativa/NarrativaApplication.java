package com.nova.narrativa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.nova.narrativa", "com.nova.narrativa.domain.llm.service"})
public class NarrativaApplication {

    public static void main(String[] args) {
        SpringApplication.run(NarrativaApplication.class, args);
    }

}