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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
public class StoryServiceImpl implements StoryService {

    private final S3Client s3Client;
    private final RestTemplate restTemplate;
    private static final Logger logger = LoggerFactory.getLogger(StoryServiceImpl.class);

    @Value("${ml.url}")
    private String fastApiUrl;

    @Value("${aws.s3.prompt-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    private Map<Integer, String> previousUserInputMap = new HashMap<>(); // 스테이지마다 이전 대화 내용 관리

    @Autowired
    public StoryServiceImpl(RestTemplate restTemplate, S3Client s3Client) {
        this.restTemplate = restTemplate;
        this.s3Client = s3Client;
    }

    // genre에 맞는 프롬프트 파일 불러오기 (랜덤 파일)
    private String readRandomPromptFromFileByGenre(String genre, List<String> tags) {
        String folderKey = genre + "/"; // 예: "Survival/" 또는 "Romance/"

        try {
            // S3에서 파일 목록을 가져오기
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(folderKey)  // 해당 genre 폴더 안의 파일들만 가져옴
                    .build();

            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
            List<String> fileKeys = new ArrayList<>();

            // 파일 목록을 수집
            listResponse.contents().forEach(s3Object -> fileKeys.add(s3Object.key()));

            if (fileKeys.isEmpty()) {
                throw new RuntimeException("해당 폴더에 파일이 없습니다.");
            }

            // 파일 목록에서 랜덤으로 파일 선택
            Random random = new Random();
            String randomFileKey = fileKeys.get(random.nextInt(fileKeys.size())); // 랜덤 파일 선택

            // 선택된 파일 읽기
            InputStream inputStream = s3Client.getObject(
                    GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(randomFileKey)
                            .build(),
                    ResponseTransformer.toInputStream()
            );

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder fileContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                fileContent.append(line).append("\n");
            }
            return fileContent.toString();
        } catch (SdkException | IOException e) {
            logger.error("S3에서 파일 읽기 실패: ", e);
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

        String prompt = readRandomPromptFromFileByGenre(genre,tags);
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
