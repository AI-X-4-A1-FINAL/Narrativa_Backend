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
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class StoryService {

    private final WebClient webClient;
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
    public StoryService(WebClient webClient, GameRepository gameRepository,
                        UserRepository userRepository, StageRepository stageRepository,
                        ObjectMapper objectMapper, ImageService imageService) {
        this.webClient = webClient;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.stageRepository = stageRepository;
        this.objectMapper = objectMapper;
        this.imageService = imageService;
    }

    @Transactional
    public Mono<Map<String, Object>> startGame(String genre, List<String> tags, Long userId) {
        Map<String, Object> request = new HashMap<>();
        request.put("genre", genre);
        request.put("tags", tags);

        return webClient.post()
                .uri(mlServerUrl + "/api/story/start")
                .header("X-API-Key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .map(responseBody -> {
                    try {
                        Map<String, Object> mlResponse = objectMapper.readValue(responseBody, Map.class);

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

                        return Map.of(
                                "story", mlResponse.get("story"),
                                "choices", mlResponse.get("choices"),
                                "gameId", game.getGameId()
                        );
                    } catch (Exception e) {
                        throw new RuntimeException("Error parsing response: " + e.getMessage());
                    }
                });
    }

    @Transactional
    public Mono<String> continueStory(String gameId, String genre, String userChoice) {
        // 현재 스테이지 번호를 조회 (기본값은 1)
        int currentStage = stageRepository.findTopByGame_GameIdOrderByStageNumberDesc(Long.parseLong(gameId))
                .map(Stage::getStageNumber)
                .orElse(0) + 1;  // 다음 스테이지 번호 계산

        // FastAPI로 보낼 요청 객체
        Map<String, Object> request = Map.of(
                "genre", genre,
                "user_choice", userChoice,
                "game_id", gameId,
                "stage", currentStage
        );

        return webClient.post()
                .uri(mlServerUrl + "/api/story/continue")
                .header("X-API-Key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .map(responseBody -> {
                    try {
                        Map<String, Object> mlResponse = objectMapper.readValue(responseBody, Map.class);
                        String story = (String) mlResponse.get("story");
                        List<String> choices = (List<String>) mlResponse.get("choices");

                        Game game = gameRepository.findById(Long.parseLong(gameId))
                                .orElseThrow(() -> new RuntimeException("Game not found"));

                        // 이전 스테이지의 EndTime 설정
                        stageRepository.findTopByGame_GameIdOrderByStageNumberDesc(Long.parseLong(gameId))
                                .ifPresent(lastStage -> {
                                    lastStage.setEndTime(LocalDateTime.now());
                                    stageRepository.save(lastStage);
                                });

                        // 새로운 스테이지 생성 및 저장
                        Stage stage = new Stage();
                        stage.setGame(game);
                        stage.setStageNumber(currentStage);
                        stage.setUserChoice(userChoice);
                        stage.setChoices(String.join(", ", choices));
                        stage.setStory(story);
                        stage.setStartTime(LocalDateTime.now());
                        stageRepository.save(stage);

                        return objectMapper.writeValueAsString(Map.of(
                                "story", story,
                                "choices", choices,
                                "game_id", gameId,
                                "stage", currentStage  // FastAPI에 반환할 stage 값
                        ));
                    } catch (Exception e) {
                        throw new RuntimeException("Error processing story: " + e.getMessage());
                    }
                });
    }

    @Transactional
    public Mono<String> generateEnding(String gameId, String genre, String userChoice) {
        Map<String, Object> request = Map.of("game_id", gameId, "genre", genre, "user_choice", userChoice);

        return webClient.post()
                .uri(mlServerUrl + "/api/story/end")
                .header("X-API-Key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .map(responseBody -> {
                    try {
                        Map<String, Object> mlResponse = objectMapper.readValue(responseBody, Map.class);
                        String story = (String) mlResponse.get("story");
                        Integer probability = (Integer) mlResponse.getOrDefault("survival_rate", 0);

                        Game game = gameRepository.findById(Long.parseLong(gameId))
                                .orElseThrow(() -> new RuntimeException("Game not found"));

                        Stage stage = new Stage();
                        stage.setGame(game);
                        stage.setStageNumber(6);
                        stage.setStory(story);
                        stage.setProbability(probability);
                        stage.setUserChoice(userChoice);
                        stage.setEndTime(LocalDateTime.now());
                        stageRepository.save(stage);

                        return objectMapper.writeValueAsString(Map.of(
                                "story", story,
                                "survival_rate", probability,
                                "game_id", gameId
                        ));
                    } catch (Exception e) {
                        throw new RuntimeException("Error processing ending: " + e.getMessage());
                    }
                });
    }

    // 히스토리 조회
    public List<Map<String, Object>> getGameStagesForUser(Long userId) {
        try {
            // 새로운 쿼리를 호출하여 한 번의 조회로 데이터를 가져옴
            List<Map<String, Object>> gameStages = stageRepository.findGameStagesWithUserId(userId);

            if (gameStages.isEmpty()) {
                throw new EntityNotFoundException("No games found for the given userId: " + userId);
            }

            return gameStages;
        } catch (Exception e) {
            logger.error("[Service] Error fetching game stages for userId: {}. Details: {}", userId, e.getMessage());
            throw new RuntimeException("Error fetching game stages: " + e.getMessage());
        }
    }

}