package com.nova.narrativa.domain.tti.service;

import com.nova.narrativa.common.exception.NoImageFileFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

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

    // Get a list of image files from S3 bucket
    public List<String> getImageFiles() {
        // List all image files from the S3 bucket
        var request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix("survival_images/")
                .build();

        var response = s3Client.listObjectsV2(request);

        List<String> imageFiles = new ArrayList<>();
        for (var s3Object : response.contents()) {
            String key = s3Object.key();
            // Filter image types: jpg, png, jpeg
            if (key.endsWith(".jpg") || key.endsWith(".png") || key.endsWith(".jpeg")) {
                imageFiles.add(key);
            }
        }

        return imageFiles;
    }

    // Generate a presigned URL for accessing an image file
    public String generatePresignedUrl(String key) {
        var presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
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

    // Generate an image from FastAPI
    public byte[] generateImage(String prompt, String size, int n) {
        String generateImageUrl = fastApiUrl + "/api/images/generate-image";

        // Create payload for FastAPI
        Map<String, Object> requestPayload = Map.of(
                "prompt", prompt,
                "size", size,
                "n", n
        );

        // Set up HTTP headers (application/json)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestPayload, headers);

        try {
            // Send POST request to FastAPI
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    generateImageUrl,
                    HttpMethod.POST,
                    entity,
                    byte[].class
            );

            // Return generated image
            return response.getBody();
        } catch (Exception e) {
            // Throw exception with specific error message
            throw new RuntimeException("Error occurred while calling FastAPI: " + e.getMessage());
        }
    }

}
