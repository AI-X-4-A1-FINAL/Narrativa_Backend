package com.nova.narrativa.domain.llm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nova.narrativa.domain.llm.entity.Game;
import com.nova.narrativa.domain.llm.entity.GameUser;
import com.nova.narrativa.domain.llm.entity.Stage;
import com.nova.narrativa.domain.llm.repository.GameRepository;
import com.nova.narrativa.domain.llm.repository.GameUserRepository;
import com.nova.narrativa.domain.llm.repository.StageRepository;
import com.nova.narrativa.domain.user.entity.User;
import com.nova.narrativa.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StoryServiceImpl implements StoryService {
    private final RestTemplate restTemplate;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final StageRepository stageRepository;
    private final GameUserRepository gameUserRepository;
    private final ObjectMapper objectMapper;

    @Value("${environments.narrativa-ml.url}")
    private String mlServerUrl;

    @Value("${environments.narrativa-ml.api-key}")
    private String apiKey;

    @Autowired
    public StoryServiceImpl(RestTemplate restTemplate, GameRepository gameRepository,
                            UserRepository userRepository, StageRepository stageRepository,
                            GameUserRepository gameUserRepository, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.stageRepository = stageRepository;
        this.gameUserRepository = gameUserRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public String startGame(String genre, List<String> tags, Long userId) {
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

            Map<String, Object> mlResponse = objectMapper.readValue(response.getBody(), Map.class);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Game game = new Game();
            game.setUser(user);
            game.setGenre(genre);
            game.setInitialStory((String) mlResponse.get("story"));
            game = gameRepository.save(game);

            Stage stage = new Stage();
            stage.setGame(game);
            stage.setStageNumber(1);
            stage.setConversationHistory((String) mlResponse.get("story"));
            stageRepository.save(stage);

            GameUser gameUser = new GameUser();
            gameUser.setGame(game);
            gameUser.setUser(user);
            gameUser.setCurrentStage(1);
            gameUserRepository.save(gameUser);

            return objectMapper.writeValueAsString(Map.of(
                    "story", mlResponse.get("story"),
                    "choices", mlResponse.get("choices"),
                    "story_id", mlResponse.get("story_id"),
                    "gameId", game.getGameId()
            ));
        } catch (Exception e) {
            throw new RuntimeException("Error starting game: " + e.getMessage());
        }
    }

    @Override
    public String continueStory(String storyId, String genre, String userChoice) {
        // ML 서버 요청 데이터 구성
        Map<String, Object> request = Map.of(
                "genre", genre,
                "user_choice", userChoice,
                "story_id", storyId
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            // ML 서버 호출
            ResponseEntity<String> response = restTemplate.exchange(
                    mlServerUrl + "/api/story/continue",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // 응답 반환
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Error communicating with ML server: " + e.getMessage());
        }
    }

    @Override
    public Game saveGame(Game game) {
        return gameRepository.save(game);
    }

    @Override
    public List<Game> getGamesByUserId(Long userId) {
        return gameRepository.findByUser_Id(userId);
    }

    @Override
    public Game getGameById(Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found with id: " + gameId));
    }
}
