package com.nova.narrativa;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import io.github.cdimascio.dotenv.Dotenv;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class EnvConfig {
    @PostConstruct
    public void init() {
        try {
            Path dotenv = Paths.get("src/main/resources/.env");
            if (Files.exists(dotenv)) {
                Dotenv.configure()
                        .directory("src/main/resources")
                        .load();
            }
        } catch (Exception e) {
            System.out.println("Could not load .env file: " + e.getMessage());
        }
    }
}