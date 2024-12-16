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
import org.springframework.transaction.annotation.Transactional;
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

    public List<String> getImageFiles(String genre) {

        // 장르에 맞는 prefix 설정
        String prefix;
        if ("Survival".equalsIgnoreCase(genre)) {
            prefix = "survival_images/";
        } else if ("Romance".equalsIgnoreCase(genre)) {
            prefix = "romance_images/";
        } else if("Simulation".equalsIgnoreCase(genre)) {
            prefix = "simulation_images/";
        }
        else {
            // 다른 장르에 대해서는 기본값 설정 또는 예외 처리
            throw new IllegalArgumentException("Unsupported genre: " + genre);
        }

        var request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
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
    public String getRandomImage(String genre) {
        List<String> imageFiles = getImageFiles(genre);

        if (imageFiles.isEmpty()) {
            throw new NoImageFileFoundException("No image files found in the bucket.");
        }

        String randomImageFile = imageFiles.get(new Random().nextInt(imageFiles.size()));
        return generatePresignedUrl(randomImageFile);
    }

    @Transactional
    public ResponseEntity<byte[]> generateImage(Long gameId, int stageNumber, String prompt, String size, int n, String genre) {
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

            // 해당 gameId와 stageNumber를 기준으로 Stage 엔터티 조회
            Stage stage = stageRepository.findByGame_GameIdAndStageNumber(gameId, stageNumber)
                    .orElseGet(() -> {
                        // Stage가 없으면 새로 생성
                        Stage newStage = new Stage();
                        newStage.setGame(gameRepository.findById(gameId).orElseThrow(() -> new EntityNotFoundException("Game not found")));
                        newStage.setStageNumber(stageNumber);
                        newStage.setImageUrl(responseData); // 새로 생성된 Stage에 imageUrl 추가
                        return stageRepository.save(newStage);  // 새 Stage 저장
                    });

            // imageUrl이 이미 저장되어 있지 않다면 업데이트
            if (stage.getImageUrl() == null || stage.getImageUrl().length == 0) {
                stage.setImageUrl(responseData);
                stageRepository.save(stage);
            }
            // S3에 업로드하여 URL 생성
            String s3ImageUrl = uploadImageToS3(responseData, gameId, stageNumber);
            // 프론트에는 원래의 이미지 URL을 반환
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(responseData);
        }  catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Failed to process request: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
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
            //System.out.println(s3Url);

            return s3Url;

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image to S3", e);
        }
    }

}