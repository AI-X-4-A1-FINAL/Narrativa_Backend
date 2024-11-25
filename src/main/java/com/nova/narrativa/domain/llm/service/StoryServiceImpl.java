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

    // genre에 맞는 프롬프트 파일 불러오기
    private String readPromptFromFileByGenre(String genre, List<String> tags) {
        String fileName = genre + ".txt"; // 예: "Survival.txt" 또는 "Romance.txt"
        String filePath = promptFilePath + "/" + fileName;

        try {
            return Files.readString(Paths.get(filePath)); // 텍스트 파일 읽기
        } catch (Exception e) {
            throw new RuntimeException("프롬프트 파일 읽기 실패: " + e.getMessage());
        }
    }

    @Override
    public String startGame(String genre, List<String> tags) {

        // FastAPI로 전달할 데이터 생성
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("genre", genre);
        requestPayload.put("tags", tags);
        //        requestPayload.put("survivalProbability", survivalProbabilityMap.get(currentStage)); // 생존 확률 추가

        String prompt = readPromptFromFileByGenre(genre,tags);
        requestPayload.put("prompt", prompt);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(fastApiUrl + "/api/story/start", requestPayload, String.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("FastAPI 요청 중 오류 발생: " + e.getMessage());
        }
    }

    @Override
    public String continueStory(int currentStage, String genre, String initialStory, String previousStory, String userInput ) {
        // 이전 대화 내용을 업데이트
        String previousUserInput = previousUserInputMap.getOrDefault(currentStage, "");

        // FastAPI로 전달할 데이터 생성
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("currentStage", currentStage);  // 현재 스테이지 값 설정
        requestPayload.put("genre", genre);  // 장르값 설정
        requestPayload.put("initialStory", initialStory);  // 초기 스토리 값 설정
        requestPayload.put("previousUserInput", previousUserInput);  // 이전 사용자 입력값 설정
        requestPayload.put("userInput", userInput);  // 유저의 입력값 설정

        // 이전 입력 저장
        previousUserInputMap.put(currentStage, userInput);

        // 로그 출력: FastAPI로 보내는 데이터 확인
//        logger.info("Sending data to FastAPI: {}", requestPayload);

        // HttpHeaders 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

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
