package com.nova.narrativa.domain.llm.service;

import com.nova.narrativa.domain.llm.dto.ChatRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

@Service
public class StoryServiceImpl implements StoryService {

    private final RestTemplate restTemplate;
    private static final Logger logger = LoggerFactory.getLogger(StoryServiceImpl.class);

    @Value("${ml.url}")
    private String fastApiUrl;

    @Autowired
    public StoryServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String startGame(String genre, List<String> tags) {
        // FastAPI로 전달할 데이터 생성
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("genre", genre);
        requestPayload.put("tags", tags);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(fastApiUrl + "/api/story/start", requestPayload, String.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("FastAPI 요청 중 오류 발생: " + e.getMessage());
        }
    }

    @Override
    public String continueStory(String initialStory, String userInput, int currentStage, String genre) {

        // FastAPI로 전달할 데이터 생성
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("genre", genre);
        requestPayload.put("currentStage", currentStage);
        requestPayload.put("initialStory", initialStory);
        requestPayload.put("userInput", userInput);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(fastApiUrl + "/api/story/chat", requestPayload, String.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("FastAPI 요청 중 오류 발생: " + e.getMessage());
        }
    }

}
