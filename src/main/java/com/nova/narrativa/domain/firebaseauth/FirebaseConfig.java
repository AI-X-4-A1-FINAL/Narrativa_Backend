package com.nova.narrativa.domain.firebaseauth;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {
    @Value("${spring.firebase.type}")
    private String type;

    @Value("${spring.firebase.project-id}")
    private String projectId;

    @Value("${spring.firebase.private-key-id}")
    private String privateKeyId;

    @Value("${spring.firebase.private-key}")
    private String privateKey;

    @Value("${spring.firebase.client-email}")
    private String clientEmail;

    @Value("${spring.firebase.client-id}")
    private String clientId;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(createFirebaseCredentialStream()))
                .setProjectId(projectId)
                .build();

        // 이미 초기화되어 있다면 기존 인스턴스 반환
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        // 새로 초기화
        return FirebaseApp.initializeApp(options);
    }

    private InputStream createFirebaseCredentialStream() throws IOException {
        String firebaseCredential = String.format("""
                {
                  "type": "%s",
                  "project_id": "%s",
                  "private_key_id": "%s",
                  "private_key": "%s",
                  "client_email": "%s",
                  "client_id": "%s",
                  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
                  "token_uri": "https://oauth2.googleapis.com/token",
                  "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
                  "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/%s"
                }""",
                type, projectId, privateKeyId, privateKey, clientEmail, clientId, clientEmail);

        return new ByteArrayInputStream(firebaseCredential.getBytes(StandardCharsets.UTF_8));
    }
}