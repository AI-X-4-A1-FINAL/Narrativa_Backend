package com.nova.narrativa.domain.llm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nova.narrativa.domain.llm.entity.Game;
import com.nova.narrativa.domain.llm.entity.GameUser;
import com.nova.narrativa.domain.llm.entity.Story;
import com.nova.narrativa.domain.llm.repository.GameRepository;
import com.nova.narrativa.domain.llm.repository.GameUserRepository;
import com.nova.narrativa.domain.llm.repository.StoryRepository;
import com.nova.narrativa.domain.user.entity.User;
import com.nova.narrativa.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StoryService {

    private final RestTemplate restTemplate;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final StoryRepository storyRepository;
    private final GameUserRepository gameUserRepository;
    private final ObjectMapper objectMapper;

    @Value("${environments.narrativa-ml.url}")
    private String mlServerUrl;

    @Value("${environments.narrativa-ml.api-key}")
    private String apiKey;

    @Autowired
    public StoryService(RestTemplate restTemplate, GameRepository gameRepository,
                        UserRepository userRepository, StoryRepository storyRepository,
                        GameUserRepository gameUserRepository, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.storyRepository = storyRepository;
        this.gameUserRepository = gameUserRepository;
        this.objectMapper = objectMapper;
    }

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

            Story story = new Story();
            story.setGame(game);
            story.setStageNumber(1);
            story.setConversationHistory((String) mlResponse.get("story"));
            storyRepository.save(story);

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
        }  catch (HttpClientErrorException e) {
            throw new RuntimeException("ML 서버에서 클라이언트 요청 오류가 발생했습니다: " + e.getMessage());
        } catch (HttpServerErrorException e) {
            throw new RuntimeException("ML 서버에서 서버 오류가 발생했습니다: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException("응답 데이터를 처리하는 중 오류가 발생했습니다: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("게임 시작 중 알 수 없는 오류가 발생했습니다: " + e.getMessage());
        }
    }

    public String continueStory(String storyId, String genre, String userChoice) {
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
            ResponseEntity<String> response = restTemplate.exchange(
                    mlServerUrl + "/api/story/continue",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Error communicating with ML server: " + e.getMessage());
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
}
