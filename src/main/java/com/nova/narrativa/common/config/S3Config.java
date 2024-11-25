package com.nova.narrativa.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

    @Value("${aws.s3.region}")
    private String region;

    @Bean
    public S3Client s3Client() {
        AwsCredentialsProvider credentialsProvider = getCredentialsProvider();

        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        AwsCredentialsProvider credentialsProvider = getCredentialsProvider();

        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();
    }

    private AwsCredentialsProvider getCredentialsProvider() {
        if (isLocalEnvironment()) {
            System.out.println("Using ProfileCredentialsProvider for local environment.");
            return ProfileCredentialsProvider.create("default");
        } else {
            System.out.println("Using EnvironmentVariableCredentialsProvider for deployment environment.");
            return EnvironmentVariableCredentialsProvider.create();
        }
    }

    private boolean isLocalEnvironment() {
        String env = System.getenv("ENV");
        if (env == null) {
            System.out.println("ENV variable is not set. Defaulting to DEPLOYMENT environment.");
            return false; // Default to deployment
        }
        return env.equalsIgnoreCase("LOCAL");
    }
}
