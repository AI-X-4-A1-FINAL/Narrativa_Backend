package com.nova.narrativa.domain.tti.service;

import com.nova.narrativa.common.exception.NoImageFileFoundException;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final RestTemplate restTemplate;

    @Value("${ml.url}")
    private String fastApiUrl;  // FastAPI 서버의 URL

    @Value("${aws.s3.images-storage-buckets}")
    private String bucketName;

    @PreDestroy
    public void closeS3Client() {
        s3Client.close();
    }

    // Get a list of image files from S3 bucket
    public List<String> getImageFiles() {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix("survival_images/")  // Optional: you can filter images by a specific prefix, if needed
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);

        List<String> imageFiles = new ArrayList<>();

        for (S3Object s3Object : response.contents()) {
            String key = s3Object.key();
            // Assuming images have extensions like .jpg, .png, etc.
            if (key.endsWith(".jpg") || key.endsWith(".png") || key.endsWith(".jpeg")) {
                imageFiles.add(key);
            }
        }

        return imageFiles;
    }

    // Generate a presigned URL for accessing an image file
    public String generatePresignedUrl(String key) {
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))  // URL validity duration
                .getObjectRequest(getRequest -> getRequest.bucket(bucketName).key(key))
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    // Get a random image file from the list of image files
    public String getRandomImage() {
        List<String> imageFiles = getImageFiles();

        if (imageFiles.isEmpty()) {
            throw new NoImageFileFoundException("No image files found in the bucket.");
        }

        String randomImageFile = imageFiles.get(new Random().nextInt(imageFiles.size()));
        return generatePresignedUrl(randomImageFile);
    }

    public byte[] generateImage(String prompt, String size, int n) {
        String generateImageUrl = fastApiUrl + "/api/images/generate-image";  // FastAPI에서 이미지를 생성하는 URL

        // FastAPI로 전달할 데이터 준비 (예: 프롬프트, 크기, 생성할 이미지 수)
        Map<String, Object> requestPayload = Map.of(
                "prompt", prompt,
                "size", size,
                "n", n
        );

        // HttpHeaders 설정 (Content-Type을 JSON으로 지정)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // HttpEntity 생성 (본문과 헤더를 포함)
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestPayload, headers);

        // FastAPI로 POST 요청을 보내고 이미지 생성
        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    generateImageUrl,       // FastAPI 이미지 생성 URL
                    HttpMethod.POST,        // HTTP 메서드: POST
                    entity,                 // 요청 본문과 헤더
                    byte[].class            // 응답 타입: byte[] (이미지 데이터)
            );

            // FastAPI로부터 받은 이미지 데이터
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("FastAPI 이미지 생성 요청 중 오류 발생: " + e.getMessage());
        }
    }
}
