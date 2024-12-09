package com.nova.narrativa.domain.tti.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nova.narrativa.common.exception.NoImageFileFoundException;
import com.nova.narrativa.domain.llm.entity.Game;
import com.nova.narrativa.domain.llm.entity.Stage;
import com.nova.narrativa.domain.llm.repository.GameRepository;
import com.nova.narrativa.domain.llm.repository.StageRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

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
        // gameId를 String으로 변환
        String gameIdStr = String.valueOf(gameId);

        Map<String, Object> requestPayload = Map.of(
                "gameId", gameIdStr,  // gameId 추가
                "stageNumber", stageNumber,  // stageNumber 추가
                "prompt", prompt,
                "size", size,
                "n", n,
                "genre", genre
        );
        System.out.println("Request Payload: " + requestPayload); // 이 부분에서 요청 본문 확인

        // HTTP 헤더에 API 키 추가
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
            String imageUrl = jsonNode.get("imageUrl").asText();
            logger.info("Generated Image URL: {}", imageUrl); // 로그 출력

            // 해당 gameId와 stageNumber를 기준으로 Stage 엔터티 조회
            Stage stage = stageRepository.findByGame_GameIdAndStageNumber(gameId, stageNumber)
                    .orElseGet(() -> {
                        // Stage가 없으면 새로 생성
                        Stage newStage = new Stage();
                        newStage.setGame(gameRepository.findById(gameId).orElseThrow(() -> new EntityNotFoundException("Game not found")));
                        newStage.setStageNumber(stageNumber);
                        newStage.setImageUrl(imageUrl); // 새로 생성된 Stage에 imageUrl 추가
                        return stageRepository.save(newStage);  // 새 Stage 저장
                    });

            // imageUrl이 이미 저장되어 있지 않다면 업데이트
            if (stage.getImageUrl() == null || stage.getImageUrl().isEmpty()) {
                stage.setImageUrl(imageUrl);
                stageRepository.save(stage);  // Stage 엔티티 업데이트
            }

            String s3Key = uploadJsonToS3(jsonString);
            // Stage 엔티티에 imageUrl 업데이트
            return ResponseEntity.ok(imageUrl);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process request: " + e.getMessage());
        }
    }

    private String uploadJsonToS3(String jsonString) {
        try {
            // S3 파일 이름 생성
            String fileName = "json-data/" + UUID.randomUUID() + ".json";

            // S3에 업로드 요청 생성
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType("application/json")
                    .build();

            // S3 클라이언트를 통해 JSON 문자열 업로드
            S3Client s3Client = S3Client.create();
            s3Client.putObject(putObjectRequest, RequestBody.fromString(jsonString));

            System.out.println("Uploaded JSON to S3 with key: " + fileName);
            return fileName;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload JSON to S3", e);
        }
    }
}