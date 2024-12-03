package com.nova.narrativa.domain.llm.service;

import com.nova.narrativa.domain.llm.entity.Game;
import com.nova.narrativa.domain.llm.entity.Stage;
import com.nova.narrativa.domain.llm.repository.GameRepository;
import com.nova.narrativa.domain.llm.repository.StageRepository;
import com.nova.narrativa.domain.user.entity.User;
import com.nova.narrativa.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;  // 추가
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @Value("${environments.narrativa-ml.url}")
    private String fastApiUrl;

    @Value("${environments.narrativa-ml.api-key}")  // application.yml에 API 키 설정 추가
    private String apiKey;

    private Map<Integer, String> previousUserInputMap = new HashMap<>(); // 스테이지마다 이전 대화 내용 관리


    @Autowired
    public StoryServiceImpl(RestTemplate restTemplate, GameRepository gameRepository, UserRepository userRepository, StageRepository stageRepository) {
        this.restTemplate = restTemplate;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.stageRepository = stageRepository;
    }


    // FastAPI로 전달할 데이터 생성 및 스토리 시작
    @Override
    public String startGame(String genre, List<String> tags, Long userId) {
        // FastAPI로 전달할 데이터 생성 (userId 제외)
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("genre", genre);
        requestPayload.put("tags", tags);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestPayload, headers);

        try {
            // FastAPI로 스토리 생성 요청
            ResponseEntity<String> response = restTemplate.exchange(
                    fastApiUrl + "/api/story/start",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // User 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

            // 게임 생성 및 저장
            Game game = new Game();
            game.setGenre(genre);
            game.setInitialStory(response.getBody());
            game.setUser(user);  // User 설정
            gameRepository.save(game);

            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("FastAPI 요청 중 오류 발생: " + e.getMessage());
        }
    }

    // 스토리 이어가기 (대화 내용 포함)
    @Override
    public String continueStory(Long gameId, String genre, int currentStage,
                                String initialStory, String userInput, String previousStory,
                                String conversationHistory) {

        // 게임 조회
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found with id: " + gameId));

        // FastAPI로 전달할 데이터 생성
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
            ResponseEntity<String> response = restTemplate.exchange(
                    fastApiUrl + "/api/story/chat",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // Stage 엔티티 생성 및 저장
            Stage stage = new Stage();
            stage.setGame(game);
            stage.setStageNumber(currentStage);
            stage.setPreviousChoice(previousStory);
            stage.setUserChoice(userInput);
            stage.setConversationHistory(conversationHistory + "\n" + response.getBody());
            stageRepository.save(stage);

            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("FastAPI 요청 중 오류 발생: " + e.getMessage());
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