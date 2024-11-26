package com.nova.narrativa.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.InputStreamResource;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.InputStream;
import java.util.Properties;

public class S3PropertySourceLoader implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private final S3Client s3Client;

    private final String bucketName = "narrativa-backend-yml";

    public S3PropertySourceLoader(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        String activeProfile = event.getEnvironment().getProperty("spring.profiles.active", "default");
        String key = "application-" + activeProfile + ".yml"; // 동적 프로파일 파일 선택

        try (InputStream inputStream = s3Client.getObject(GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build())) {
            YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
            yamlFactory.setResources(new InputStreamResource(inputStream));
            Properties properties = yamlFactory.getObject();

            StandardEnvironment environment = (StandardEnvironment) event.getEnvironment();
            environment.getPropertySources().addFirst(new PropertiesPropertySource("s3Properties", properties));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
