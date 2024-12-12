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
        Map<String, Object> request = Map.of(
                "genre", genre,
                "user_choice", userChoice,
                "game_id", gameId
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

                        // 마지막 스테이지를 조회하여 EndTime 설정
                        Optional<Stage> lastStageOptional = stageRepository.findTopByGame_GameIdOrderByStageNumberDesc(Long.parseLong(gameId));
                        lastStageOptional.ifPresent(lastStage -> {
                            lastStage.setEndTime(LocalDateTime.now());
                            stageRepository.save(lastStage);
                        });

                        int stageNumber = stageRepository.findTopByGame_GameIdOrderByStageNumberDesc(Long.parseLong(gameId))
                                .map(Stage::getStageNumber)
                                .orElse(0) + 1;

                        Stage stage = new Stage();
                        stage.setGame(game);
                        stage.setStageNumber(stageNumber);
                        stage.setUserChoice(userChoice);
                        stage.setChoices(String.join(", ", choices));
                        stage.setStory(story);
                        stage.setStartTime(LocalDateTime.now());
                        stageRepository.save(stage);

                        return objectMapper.writeValueAsString(Map.of(
                                "story", story,
                                "choices", choices,
                                "game_id", gameId
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
                            byte[] endingImage = stage.getImageUrl();
                            try {
                                System.out.println(Arrays.toString(endingImage));
                                result.put("imageUrl", endingImage);
                            } catch (IllegalArgumentException e) {
                                logger.error("[Service] Error reading S3 file at path: {}. Error: {}", endingImage, e.getMessage());
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