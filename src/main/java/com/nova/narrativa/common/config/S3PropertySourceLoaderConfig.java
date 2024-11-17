package com.nova.narrativa.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3PropertySourceLoaderConfig {

    private final S3Client s3Client;

    public S3PropertySourceLoaderConfig(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Bean
    public S3PropertySourceLoader s3PropertySourceLoader() {
        return new S3PropertySourceLoader(s3Client);
    }
}