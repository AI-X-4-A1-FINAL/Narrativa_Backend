package com.nova.narrativa.domain.llm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nova.narrativa.domain.llm.entity.Game;
import com.nova.narrativa.domain.llm.entity.Stage;
import com.nova.narrativa.domain.llm.repository.GameRepository;
import com.nova.narrativa.domain.llm.repository.StageRepository;
import com.nova.narrativa.domain.tti.service.ImageService;
import com.nova.narrativa.domain.user.entity.User;
import com.nova.narrativa.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class StoryService {

    private final RestTemplate restTemplate;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final StageRepository stageRepository;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(StoryService.class);
    private final ImageService imageService;

    @Value("${environments.narrativa-ml.url}")
    private String mlServerUrl;

    @Value("${environments.narrativa-ml.api-key}")
    private String apiKey;

    @Autowired
    public StoryService(RestTemplate restTemplate, GameRepository gameRepository,
                        UserRepository userRepository, StageRepository stageRepository,
                        ObjectMapper objectMapper, ImageService imageService) {
        this.restTemplate = restTemplate;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.stageRepository = stageRepository;
        this.objectMapper = objectMapper;
        this.imageService = imageService;
    }
    @Transactional
    public Map<String, Object> startGame(String genre, List<String> tags, Long userId) {
        Map<String, Object> request = new HashMap<>();
        request.put("genre", genre);
        request.put("tags", tags);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    mlServerUrl + "/api/story/start",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

//            logger.info("Received response from ML server: {}", response.getBody()); // 응답 로그

            // 응답 처리
            Map<String, Object> mlResponse = objectMapper.readValue(response.getBody(), Map.class);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Game 엔티티 저장
            Game game = new Game();
            game.setUser(user);
            game.setGenre(genre);
            game.setInitialStory((String) mlResponse.get("story"));
            game.setPrompt((String) mlResponse.get("file_name"));
            game = gameRepository.save(game);

            // Stage 엔티티 저장
            Stage stage = new Stage();
            stage.setGame(game);
            stage.setStageNumber(1);
            stage.setChoices(objectMapper.writeValueAsString(mlResponse.get("choices")));
            stage.setStartTime(LocalDateTime.now());
            stageRepository.save(stage);

            // 반환 데이터 구성
            Map<String, Object> result = Map.of(
                    "story", mlResponse.get("story"),
                    "choices", mlResponse.get("choices"),
                    "gameId", game.getGameId()
            );

//            logger.info("Returning response: {}", result); // 최종 반환 로그

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error starting game: " + e.getMessage());
        }
    }

    @Transactional
    public String continueStory(String gameId, String genre, String userChoice) {
//        System.out.println("[StoryService] Continuing story with game_id: " + gameId);

        // FastAPI로 보낼 요청 데이터 준비
        Map<String, Object> request = Map.of(
                "genre", genre,
                "user_choice", userChoice,
                "game_id", gameId
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            // FastAPI 서버로 요청 보내기
            ResponseEntity<String> response = restTemplate.exchange(
                    mlServerUrl + "/api/story/continue",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // FastAPI에서 응답받은 JSON 파싱
            Map<String, Object> mlResponse = new ObjectMapper().readValue(response.getBody(), Map.class);
//            System.out.println("[StoryService] Response from ML server: " + mlResponse);

            // Story와 choices 값 추출
            String story = (String) mlResponse.get("story");
            List<String> choices = (List<String>) mlResponse.get("choices");

            // stage_number 값 처리
            String stageNumberStr = (String) mlResponse.get("stage_number");
            int stageNumber = 1;  // 기본값 설정
            if (stageNumberStr != null) {
                try {
                    stageNumber = Integer.parseInt(stageNumberStr);
                } catch (NumberFormatException e) {
                    System.err.println("[StoryService] Invalid stage_number value: " + stageNumberStr);
                }
            }

            // 게임의 마지막 스테이지 처리
            if (stageNumber > 5) {
                System.out.println("[StoryService] Game is completed. No further stages allowed.");
                return new ObjectMapper().writeValueAsString(Map.of("message", "Game Over"));
            }

            // 이전 스테이지 정보 가져오기 (conversationHistory에 사용)
            Stage previousStage = stageRepository.findTopByGame_GameIdOrderByStageNumberDesc(Long.parseLong(gameId))
                    .orElse(null);  // previousStage가 없으면 null을 할당

            // 새로운 stageNumber 계산: 이전 스테이지가 있으면 그 번호에 +1
            if (previousStage != null) {
                stageNumber = previousStage.getStageNumber() + 1;
            }

            String conversationHistory = "";
            if (previousStage != null) {
                // 이전 스테이지의 story와 userChoice를 가져와서 conversationHistory에 저장
                conversationHistory = previousStage.getUserChoice() + " " + previousStage.getStory();  // 이전 스테이지의 story와 userChoice 합침
            }

            // Game 엔티티 조회 (gameId로)
            Optional<Game> gameOptional = gameRepository.findById(Long.parseLong(gameId));
            if (gameOptional.isEmpty()) {
                throw new RuntimeException("gameId를 찾을 수 없음.: " + gameId);
            }
            Game game = gameOptional.get();

            // Stage 엔티티 저장
            Stage stage = new Stage();
            stage.setGame(game);  // Game 객체 설정
            stage.setStageNumber(stageNumber);  // stage_number 설정
            stage.setUserChoice(userChoice);
            stage.setConversationHistory(conversationHistory);  // 이전 스테이지의 선택과 대화 기록
            stage.setImageUrl("");  // 이미지 URL (필요에 따라 처리)
            stage.setChoices(String.join(", ", choices));  // 선택지를 문자열로 저장
            stage.setStory(story);

            // 스테이지 시작 시간 기록
            stage.setStartTime(LocalDateTime.now());
            stage.setEndTime(null);  // 종료 시간은 아직 설정되지 않음

            // Stage 저장
            stageRepository.save(stage);

            // 반환 데이터 구성
            Map<String, Object> result = new HashMap<>();
            result.put("story", story);
            result.put("choices", choices);
            result.put("game_id", gameId);

//            System.out.println("[프론트로 보내는 값]" + result);

            return new ObjectMapper().writeValueAsString(result);  // JSON으로 변환하여 반환

        } catch (Exception e) {
//            System.err.println("[StoryService] Error: " + e.getMessage());
            throw new RuntimeException("Error communicating with ML server: " + e.getMessage());
        }
    }

    @Transactional
    public String generateEnding(String gameId, String genre, String userChoice) {
//        System.out.println("[StoryService] Generating ending for game_id: " + gameId);

        // 요청 파라미터 설정
        Map<String, Object> request = Map.of("game_id", gameId, "genre", genre,"user_choice", userChoice);

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", apiKey);

        // 요청 엔티티 설정
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            // FastAPI로부터 응답 받기
            ResponseEntity<String> response = restTemplate.exchange(
                    mlServerUrl + "/api/story/end",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // 응답 바디를 Map으로 파싱
            Map<String, Object> mlResponse = objectMapper.readValue(response.getBody(), Map.class);

//            System.out.println("엔딩 받아오는 값" + mlResponse);
            // 응답에 'story'가 없는 경우 예외 처리
            if (!mlResponse.containsKey("story")) {
                throw new RuntimeException("ML server response is missing 'story' field");
            }


            String story = (String) mlResponse.get("story");
            Integer probability = (Integer) mlResponse.getOrDefault("survival_rate", 0);
            String gameIdFromResponse = String.valueOf(mlResponse.get("game_id"));

            // Game 객체 찾기 (gameId로 조회)
            Game game = gameRepository.findById(Long.valueOf(gameIdFromResponse))
                    .orElseThrow(() -> new RuntimeException("Game not found"));

            // 해당 게임에 대한 스테이지 정보 생성
            Stage stage = new Stage();
            stage.setGame(game);
            stage.setUserChoice(userChoice);
            stage.setStageNumber(6);
            stage.setStory(story);
            stage.setProbability(probability);
            stage.setEndTime(LocalDateTime.now());

            // Stage 테이블에 저장
            stageRepository.save(stage);

            // 응답 맵 작성
            Map<String, Object> responseMap = Map.of(
                    "story", story,
                    "survival_rate", probability,
                    "game_id", gameId
            );

            // 응답 맵을 JSON 문자열로 변환하여 반환
            return objectMapper.writeValueAsString(responseMap);

        } catch (Exception e) {
            throw new RuntimeException("Error generating story ending: " + e.getMessage());
        }
    }

    public Game saveGame(Game game) {
        return gameRepository.save(game);
    }

    public List<Game> getGamesByUserId(Long userId) {
        return gameRepository.findByUser_Id(userId);
    }

    public Game getGameById(Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found with id: " + gameId));
    }

    // 히스토리 조회
    public List<Map<String, Object>> getGameStagesForUser(Long id) {
        try {
            // 유저 조회 및 게임 리스트 가져오기
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("User not found with userId: " + id));

            List<Game> userGames = gameRepository.findByUser_Id(user.getId());
            if (userGames.isEmpty()) {
//                logger.warn("[Service] No games found for userId: {}", id);
                throw new EntityNotFoundException("No games found for the given userId: " + id);
            }

            List<Map<String, Object>> resultList = new ArrayList<>();

            // 각 게임의 스테이지 처리
            for (Game game : userGames) {
                List<Stage> stages = stageRepository.findByGame_GameId(game.getGameId());

                // stageNumber가 6까지 저장된 게임인지 확인
                boolean hasStage6 = stages.stream().anyMatch(stage -> stage.getStageNumber() == 6);

                if (hasStage6) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("gameId", game.getGameId());
                    result.put("genre", game.getGenre());

                    for (Stage stage : stages) {
                        if (stage.getStageNumber() == 6) {
                            result.put("story", stage.getStory());
                        } else if (stage.getStageNumber() == 5) {
                            String fullFilePath = stage.getImageUrl(); // full path including bucket name
                            try {
                                // S3 경로에서 파일을 가져오기
                                String imageUrl = imageService.getImageUrlFromS3(fullFilePath);
                                result.put("imageUrl", imageUrl);
                            } catch (FileNotFoundException e) {
                                logger.warn("[Service] File not found in S3 at path: {}", fullFilePath);
                                result.put("imageUrl", "/default-thumbnail.jpg");
                            } catch (IOException | IllegalArgumentException e) {
                                logger.error("[Service] Error reading S3 file at path: {}. Error: {}", fullFilePath, e.getMessage());
                                result.put("imageUrl", "/default-thumbnail.jpg");
                            }
                        }
                    }

                    resultList.add(result);
                } else {
                    logger.info("[Service] GameId {} does not have stageNumber 6", game.getGameId());
                }
            }

            return resultList;
        } catch (Exception e) {
            logger.error("[Service] Error fetching game stages for userId: {}. Details: {}", id, e.getMessage());
            throw new RuntimeException("Error fetching game stages: " + e.getMessage());
        }
    }


}
