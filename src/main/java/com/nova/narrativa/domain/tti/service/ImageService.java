package com.nova.narrativa.domain.tti.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nova.narrativa.common.exception.NoImageFileFoundException;
import com.nova.narrativa.domain.llm.entity.Stage;
import com.nova.narrativa.domain.llm.repository.GameRepository;
import com.nova.narrativa.domain.llm.repository.StageRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;




import java.time.Duration;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final StageRepository stageRepository;
    private final GameRepository gameRepository;
    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);
    private final AmazonS3 amazonS3;

    @Value("${environments.narrativa-ml.url}")
    private String fastApiUrl;  // FastAPI 서버의 URL

    @Value("${environments.narrativa-ml.api-key}")  // application.yml에 API 키 설정 추가
    private String apiKey;

    @Value("${aws.s3.images-bucket}")
    private String bucketName;

    public List<String> getImageFiles() {

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
                .signatureDuration(Duration.ofMinutes(60))
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

    public ResponseEntity<String> generateImage(Long gameId, int stageNumber, String prompt, String size, int n, String genre) {
        String generateImageUrl = fastApiUrl + "/api/images/generate-image";
        String gameIdStr = String.valueOf(gameId);

        Map<String, Object> requestPayload = Map.of(
                "gameId", gameIdStr,
                "stageNumber", stageNumber,
                "prompt", prompt,
                "size", size,
                "n", n,
                "genre", genre
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestPayload, headers);

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    generateImageUrl,
                    HttpMethod.POST,
                    entity,
                    byte[].class
            );

            byte[] responseData = response.getBody();
            if (responseData == null || responseData.length == 0) {
                throw new RuntimeException("Received empty response from FastAPI");
            }

            String jsonString = new String(responseData, StandardCharsets.UTF_8);
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            String imageUrl = jsonNode.get("imageUrl").asText(); // 프론트에 반환할 값
            System.out.println(imageUrl);

            //byte[] 변환
            byte[] imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
            if (imageBytes == null || imageBytes.length == 0) {
                throw new RuntimeException("Failed to download image from URL");
            }

            // S3에 업로드하여 URL 생성
            String s3ImageUrl = uploadImageToS3(imageBytes, gameId, stageNumber);

            // Stage 엔터티 업데이트
            Stage stage = stageRepository.findByGame_GameIdAndStageNumber(gameId, stageNumber)
                    .orElseGet(() -> {
                        Stage newStage = new Stage();
                        newStage.setGame(gameRepository.findById(gameId).orElseThrow(() -> new EntityNotFoundException("Game not found")));
                        newStage.setStageNumber(stageNumber);
                        newStage.setImageUrl(s3ImageUrl); // S3 URL로 저장
                        return stageRepository.save(newStage);  // 새 Stage 저장
                    });

            if (stage.getImageUrl() == null || stage.getImageUrl().isEmpty()) {
                stage.setImageUrl(s3ImageUrl);
                stageRepository.save(stage);  // Stage 엔티티 업데이트
            }

            // 프론트에는 원래의 이미지 URL을 반환
            return ResponseEntity.ok(imageUrl);  // 원래의 imageUrl을 프론트로 반환

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process request: " + e.getMessage());
        }
    }

    public String uploadImageToS3(byte[] imageBytes, Long gameId, int stageNumber) {
        try {
            // S3 파일 이름 생성 (gameId와 stageNumber를 파일명에 포함)
            String fileName = "game-images/" + gameId + "-stage" + stageNumber + ".jpg";

            // InputStream으로 변환
            InputStream inputStream = new ByteArrayInputStream(imageBytes);

            // S3 클라이언트 생성
            S3Client s3Client = S3Client.create();

            // S3 업로드 요청 생성
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType("image/jpeg") // JPEG 형식
                    .build();

            // S3에 이미지 업로드
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, imageBytes.length));

            // 업로드된 파일의 S3 URL 반환
            String s3Url = "https://" + bucketName + ".s3.amazonaws.com/" + fileName;
            return s3Url;

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image to S3", e);
        }
    }


    // S3 버켓에 있는 이미지 json 가져와서 그 안에 있는 이미지파일 읽어오는 메서드임.
    public String getImageUrlFromS3(String fullFilePath) throws IOException {

        String filePath = fullFilePath.substring(bucketName.length() + 1); // 버킷 이름을 제외한 경로 부분만 분리

//        logger.info("[Service] Attempting to fetch file from S3 - Bucket: {}, Path: {}", bucketName, filePath);

        // S3 객체가 존재하는지 확인
        HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(filePath)  // 경로에서 버킷 이름을 제외한 부분만 사용
                .build();

        try {
            // 객체가 존재하면 헤드 요청이 성공
            s3Client.headObject(headObjectRequest);
        } catch (NoSuchKeyException e) {
            // 파일이 존재하지 않으면 예외 처리
            throw new FileNotFoundException("File does not exist in S3 at path: " + filePath);
        }

        // 파일이 존재하면 S3에서 가져오기
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(filePath)  // 경로에서 버킷 이름을 제외한 부분만 사용
                .build();

        try (InputStream inputStream = s3Client.getObject(getObjectRequest)) {
            // JSON 파싱
            Map<String, Object> jsonData = objectMapper.readValue(inputStream, Map.class);
            if (jsonData.containsKey("imageUrl")) {
                String imageUrl = (String) jsonData.get("imageUrl");
//                logger.info("[Service] Found imageUrl in S3 JSON: {}", imageUrl);
                return imageUrl;
            } else {
                throw new IllegalArgumentException("The JSON file does not contain 'imageUrl' key");
            }
        }
    }


}