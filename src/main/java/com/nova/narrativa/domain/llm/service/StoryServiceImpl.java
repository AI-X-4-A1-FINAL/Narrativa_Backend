package com.nova.narrativa.domain.llm.service;

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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class StoryServiceImpl implements StoryService {

    private final RestTemplate restTemplate;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final StageRepository stageRepository;
    private final GameUserRepository gameUserRepository;

    @Value("${environments.narrativa-ml.url}")
    private String fastApiUrl;

    @Value("${environments.narrativa-ml.api-key}")
    private String apiKey;

    @Autowired
    public StoryServiceImpl(RestTemplate restTemplate, GameRepository gameRepository,
                            UserRepository userRepository, StageRepository stageRepository,
                            GameUserRepository gameUserRepository) {
        this.restTemplate = restTemplate;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.stageRepository = stageRepository;
        this.gameUserRepository = gameUserRepository;
    }

    @Override
    public String startGame(String genre, List<String> tags, Long userId) {
        // FastAPI 요청 데이터 구성
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("genre", genre);
        requestPayload.put("tags", tags);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestPayload, headers);

        try {
            // FastAPI 요청
            ResponseEntity<String> response = restTemplate.exchange(
                    fastApiUrl + "/api/story/start",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // 응답 확인
            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("FastAPI 요청 실패: " + response.getStatusCode());
            }

            // Game 엔티티 생성 및 저장
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다: " + userId));

            Game game = new Game();
            game.setUser(user);
            game.setGenre(genre);
            game.setInitialStory(response.getBody());
            gameRepository.save(game);

            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("FastAPI 요청 오류: " + e.getMessage());
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("사용자를 찾을 수 없습니다: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("잘못된 인자: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("FastAPI 요청 중 오류 발생: " + e.getMessage());
        }
    }

    @Override
    public String continueStory(Long gameId, String genre, int currentStage,
                                String initialStory, String userInput,
                                String previousStory, String conversationHistory) {
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("genre", genre);
        requestPayload.put("currentStage", currentStage);
        requestPayload.put("initialStory", initialStory);
        requestPayload.put("userInput", userInput);
        requestPayload.put("previousUserInput", previousStory);
        requestPayload.put("conversationHistory", conversationHistory);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestPayload, headers);

        try {
            // FastAPI 요청
            ResponseEntity<String> response = restTemplate.exchange(
                    fastApiUrl + "/api/story/chat",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("FastAPI 요청 실패: " + response.getStatusCode());
            }

            Game game = gameRepository.findById(gameId)
                    .orElseThrow(() -> new NoSuchElementException("게임을 찾을 수 없습니다: " + gameId));

            // Stage 저장
            Stage stage = new Stage();
            stage.setGame(game);
            stage.setStageNumber(currentStage);
            stage.setPreviousChoice(previousStory);
            stage.setUserChoice(userInput);
            stage.setConversationHistory(conversationHistory + "\n" + response.getBody());
            stageRepository.save(stage);

            // GameUser 저장
            GameUser gameUser = new GameUser();
            gameUser.setGame(game);
            gameUser.setUser(game.getUser());
            gameUser.setCurrentStage(currentStage);
            gameUserRepository.save(gameUser);

            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("FastAPI 요청 오류: " + e.getMessage());
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("게임 상태를 찾을 수 없습니다: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("잘못된 인자: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("FastAPI 요청 중 오류 발생: " + e.getMessage());
        }
    }

    @Override
    public Game saveGame(Game game) {
        try {
            return gameRepository.save(game);
        } catch (Exception e) {
            throw new RuntimeException("게임 저장 중 오류 발생: " + e.getMessage());
        }
    }

    @Override
    public List<Game> getGamesByUserId(Long userId) {
        try {
            return gameRepository.findByUser_Id(userId);
        } catch (Exception e) {
            throw new RuntimeException("사용자의 게임 목록을 가져오는 중 오류 발생: " + e.getMessage());
        }
    }

    @Override
    public Game getGameById(Long gameId) {
        try {
            return gameRepository.findById(gameId)
                    .orElseThrow(() -> new NoSuchElementException("게임을 찾을 수 없습니다: " + gameId));
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("게임을 찾을 수 없습니다: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("게임을 가져오는 중 오류 발생: " + e.getMessage());
        }
    }
}
