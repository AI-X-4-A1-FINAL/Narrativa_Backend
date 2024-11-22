package com.nova.narrativa.domain.llm.service;

import com.nova.narrativa.domain.llm.dto.StoryRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StoryServiceImpl implements StoryService {

    private final RestTemplate restTemplate;

    public StoryServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String startGame(String genre, List<String> tags) {
        // FastAPI로 전달할 데이터 생성
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("genre", genre);
        requestPayload.put("tags", tags);

        // FastAPI URL
        String fastApiUrl = "http://<FASTAPI_SERVER>:8000/api/story/start";

        try {
            // FastAPI로 POST 요청
            ResponseEntity<String> response = restTemplate.postForEntity(fastApiUrl, requestPayload, String.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("FastAPI 요청 중 오류 발생: " + e.getMessage());
        }
    }
}
