package com.nova.narrativa.domain.llm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StoryServiceImpl implements StoryService {

    private final RestTemplate restTemplate;
    private static final Logger logger = LoggerFactory.getLogger(StoryServiceImpl.class);

    @Value("${ml.url}")
    private String fastApiUrl;

    @Value("${prompt.file.path}")
    private String promptFilePath;

    private Map<Integer, Integer> userInputCountMap = new HashMap<>(); // 스테이지마다 유저 입력 횟수 카운트
    private Map<Integer, Double> survivalProbabilityMap = new HashMap<>(); // 스테이지마다 생존 확률 관리
    private Map<Integer, String> previousUserInputMap = new HashMap<>(); // 스테이지마다 이전 대화 내용 관리

    @Autowired
    public StoryServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // 프롬프트 파일 읽기 메서드
    private String readPromptFromFile() {
        try {
            return Files.readString(Paths.get(promptFilePath)); // 텍스트 파일 읽기
        } catch (Exception e) {
            throw new RuntimeException("프롬프트 파일 읽기 실패: " + e.getMessage());
        }
    }

    @Override
    public String startGame(String genre, List<String> tags) {

// 생존 확률 초기화 (최초 20~100% 랜덤으로 설정)
//        if (!survivalProbabilityMap.containsKey(currentStage)) {
//            double initialSurvivalProbability = Math.random() * 80 + 20; // 20~100% 사이의 랜덤 값
//            survivalProbabilityMap.put(currentStage, initialSurvivalProbability);
//        }

        // FastAPI로 전달할 데이터 생성
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("genre", genre);
        requestPayload.put("tags", tags);
//        requestPayload.put("survivalProbability", survivalProbabilityMap.get(currentStage)); // 생존 확률 추가


        // 프롬프트 추가
        String prompt = readPromptFromFile();
        requestPayload.put("prompt", prompt);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(fastApiUrl + "/api/story/start", requestPayload, String.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("FastAPI 요청 중 오류 발생: " + e.getMessage());
        }
    }

    @Override
    public String continueStory(String initialStory, String userInput, int currentStage, String genre) {

        // 이전 대화 내용 (첫 입력일 경우 빈 값 전달)
        String previousUserInput = previousUserInputMap.getOrDefault(currentStage, "");

        // FastAPI로 전달할 데이터 생성
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("genre", genre);
        requestPayload.put("currentStage", currentStage);
        requestPayload.put("initialStory", initialStory);
        requestPayload.put("userInput", userInput);
        requestPayload.put("previousUserInput", previousUserInput); // 이전 대화 내용 추가

        // 이전 대화 내용 업데이트 (현재 유저 입력을 저장)
        previousUserInputMap.put(currentStage, userInput);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(fastApiUrl + "/api/story/chat", requestPayload, String.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("FastAPI 요청 중 오류 발생: " + e.getMessage());
        }
    }

}

