package com.nova.narrativa.common.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

    private static final Dotenv dotenv = Dotenv.configure()
            .directory("./src/main/resources") // .env 파일 위치
            .load();

    @Value("${aws.s3.region:ap-northeast-2}")
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
    public AmazonS3 s3ImgClient() {
        AWSCredentials credentials = new BasicAWSCredentials(dotenv.get("AWS_ACCESS_KEY_ID"), dotenv.get("AWS_SECRET_ACCESS_KEY"));
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(dotenv.get("AWS_REGION"))
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
        String env = dotenv.get("ENV", "DEPLOYMENT"); // 기본값 DEPLOYMENT
        System.out.println("Detected ENV variable: " + env); // 디버깅 메시지 추가
        return env.equalsIgnoreCase("LOCAL");
    }
}
