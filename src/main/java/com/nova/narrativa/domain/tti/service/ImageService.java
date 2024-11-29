package com.nova.narrativa.domain.tti.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.nova.narrativa.common.exception.NoImageFileFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final RestTemplate restTemplate;

    @Value("${ml.url}")
    private String fastApiUrl;  // FastAPI 서버의 URL

    @Value("${aws.s3.images-bucket}")
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

//    // Generate an image from FastAPI
//    public byte[] generateImage(String prompt, String size, int n) {
//        String generateImageUrl = fastApiUrl + "/api/images/generate-image";
//
//        // Create payload for FastAPI
//        Map<String, Object> requestPayload = Map.of(
//                "prompt", prompt,
//                "size", size,
//                "n", n
//        );
//
//        // Set up HTTP headers (application/json)
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestPayload, headers);
//
//        try {
//            // Send POST request to FastAPI
//            ResponseEntity<byte[]> response = restTemplate.exchange(
//                    generateImageUrl,
//                    HttpMethod.POST,
//                    entity,
//                    byte[].class
//            );
//
//            // Return generated image
//            return response.getBody();
//        } catch (Exception e) {
//            // Throw exception with specific error message
//            throw new RuntimeException("Error occurred while calling FastAPI: " + e.getMessage());
//        }
//    }

    public ResponseEntity<String> generateImage(String prompt, String size, int n) {
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

            byte[] imageBytes = response.getBody(); //받아온 이미지

            if (imageBytes == null || imageBytes.length == 0) {
                throw new RuntimeException("Received empty image from FastAPI");
            }

            // Convert image bytes to Base64 string
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            // Return the Base64 image string in the response body
            return ResponseEntity.ok(base64Image); // 이곳에서 base64 문자열 반환

        } catch (Exception e) {
            // Handle exceptions (e.g., FastAPI request failure)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate image: " + e.getMessage());
        }
    }
}



//            // Generate a unique image name (e.g., based on timestamp or UUID)
//            String imageName = UUID.randomUUID().toString() + ".jpg";
//
//            // Upload image to S3 and get the URL
//            return uploadImageToS3(imageBytes, imageName);
//        } catch (Exception e) {
//            // Throw exception with specific error message
//            throw new RuntimeException("Error occurred while calling FastAPI: " + e.getMessage());
//        }
//    }
//
//    private String uploadImageToS3(byte[] imageBytes, String imageName) {
//        try {
//            // PutObjectRequest 객체 생성 (버킷명, 파일명, 콘텐츠 타입 설정)
//            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
//                    .bucket(bucketName)
//                    .key(imageName) // S3에 저장될 파일의 경로 및 이름
//                    .contentType("image/png") // 이미지 콘텐츠 타입 (예: image/png, image/jpeg)
//                    .build();
//
//            // S3에 동기적으로 파일 업로드
//            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(imageBytes));
//
//            // 업로드된 파일에 대한 S3 URL 반환
//            return s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(imageName)).toExternalForm();
//        } catch (AwsServiceException e) {
//            throw new RuntimeException(e);
//        } catch (SdkClientException e) {
//            throw new RuntimeException(e);
//        }
//    }

