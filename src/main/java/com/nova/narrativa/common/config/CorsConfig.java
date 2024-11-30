package com.nova.narrativa.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${environments.narrativa-front.url}")
    private String frontUrl;

    @Value("${environments.server.url}")
    private String serverUrl;

    @Value("${environments.narrativa-admin.url}")
    private String adminUrl;

    @Value("${environments.narrativa-ml.url}")
    private String mlUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(frontUrl, serverUrl, adminUrl, mlUrl)
                .allowedHeaders("*")
                .allowCredentials(true)
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .maxAge(3600);
    }

}