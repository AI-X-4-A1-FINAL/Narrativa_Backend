package com.nova.narrativa.domain.llm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class StoryServiceImpl implements StoryService {

    private final RestTemplate restTemplate;
    private static final Logger logger = LoggerFactory.getLogger(StoryServiceImpl.class);

    @Value("${environments.narrativa-ml.url}")
    private String fastApiUrl;

    @Value("${environments.narrativa-ml.api-key}")  // application.yml에 API 키 설정 추가
    private String apiKey;

    private Map<Integer, String> previousUserInputMap = new HashMap<>(); // 스테이지마다 이전 대화 내용 관리

    @Autowired
    public StoryServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // FastAPI로 전달할 데이터 생성 및 스토리 시작
    @Override
    public String startGame(String genre, List<String> tags) {
        // FastAPI로 전달할 데이터 생성
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("genre", genre);
        requestPayload.put("tags", tags);

        // 헤더 설정 추가
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", apiKey);

        // HttpEntity 생성
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestPayload, headers);

        try {
            // FastAPI로 스토리 생성 요청
            ResponseEntity<String> response = restTemplate.exchange(
                    fastApiUrl + "/api/story/start",
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("FastAPI 요청 중 오류 발생: " + e.getMessage());
        }
    }

    // 스토리 이어가기 (대화 내용 포함)
    @Override
    public String continueStory(String genre, int currentStage, String initialStory, String userInput, String previousStory, String conversationHistory) {
        String previousUserInput = previousUserInputMap.getOrDefault(currentStage, "");

        // conversationHistory가 빈 배열로 설정 (null이 아닌 빈 배열을 전달)
        if (conversationHistory == null || conversationHistory.isEmpty()) {
            conversationHistory = "[]";  // 빈 배열을 문자열로 설정
        }

        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("genre", genre);
        requestPayload.put("currentStage", currentStage);
        requestPayload.put("initialStory", initialStory);
        requestPayload.put("userInput", userInput);
        requestPayload.put("previousUserInput", previousUserInput);
        requestPayload.put("conversationHistory", conversationHistory);  // 빈 배열이 아닌 null로 처리

        // 이전 입력 저장
        previousUserInputMap.put(currentStage, userInput);

        // 로그 출력: FastAPI로 보내는 데이터 확인
//        logger.info("Sending data to FastAPI: {}", requestPayload);

        // HttpHeaders 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", apiKey);

        // HttpEntity 생성 (RequestPayload와 헤더 포함)
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestPayload, headers);

        // FastAPI 요청 (exchange 사용)
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    fastApiUrl + "/api/story/chat", // FastAPI URL
                    HttpMethod.POST,               // POST 메서드
                    entity,                        // 요청 데이터와 헤더
                    String.class                   // 응답 타입
            );
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("FastAPI 요청 중 오류 발생: " + e.getMessage());
        }
    }
}

