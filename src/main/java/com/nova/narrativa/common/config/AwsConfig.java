package com.nova.narrativa.common.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsConfig {

    @Bean
    public AmazonS3 amazonS3() {
        return AmazonS3ClientBuilder.defaultClient();  // 기본 클라이언트 사용
    }
}