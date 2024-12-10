package com.nova.narrativa.domain.llm.controller;

import com.nova.narrativa.domain.llm.dto.ChatRequest;
import com.nova.narrativa.domain.llm.dto.StoryStartRequest;
import com.nova.narrativa.domain.llm.service.StoryService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/generate-story")
public class StoryController {

    private final StoryService storyService;
    private static final Logger logger = LoggerFactory.getLogger(StoryController.class);

    @Autowired
    public StoryController(StoryService storyService) {
        this.storyService = storyService;
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startGame(@Valid @RequestBody StoryStartRequest request) {
        try {
            Map<String, Object> storyResponse = storyService.startGame(
                    request.getGenre(),
                    request.getTags(),
                    request.getUserId()
            );
            return ResponseEntity.ok(storyResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error: " + e.getMessage()));
        }
    }

    @PostMapping("/chat")
    public ResponseEntity<String> continueStory(@RequestBody Map<String, Object> request) {
//        System.out.println("[프론트에서 /chat으로 보낸 값]: " + request);
        try {
            // Map에서 데이터 추출
            Long gameId = Long.valueOf(String.valueOf(request.get("gameId")));
            String genre = String.valueOf(request.get("genre"));
            String userChoice = String.valueOf(request.get("userSelect"));

            // StoryService 호출
            String storyResponse = storyService.continueStory(
                    gameId.toString(),
                    genre,
                    userChoice
            );
//            System.out.println("[프론트 보내는 값]" + storyResponse); // 응답 로그 추가

            return ResponseEntity.ok(storyResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/end")
    public ResponseEntity<String> generateEnding(@RequestBody Map<String, Object> request) {
        try {
            // 로그 출력해서 데이터 확인
//            System.out.println("[Controller] Received request: " + request);

            // Map에서 데이터 추출
            String gameId = String.valueOf(request.get("gameId"));
            String genre = String.valueOf(request.get("genre"));
            String userChoice = String.valueOf(request.get("userSelect"));

            // StoryService 호출
            String storyResponse = storyService.generateEnding(gameId, genre, userChoice);

//            System.out.println("[Controller] Response from service: " + storyResponse); // 응답 로그 추가

            return ResponseEntity.ok(storyResponse);
        } catch (Exception e) {
            System.err.println("[Controller] Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    // 히스토리 조회용 메서드
    @PostMapping("/history")
    public ResponseEntity<?> getUserGameStages(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(String.valueOf(request.get("userId")));
            logger.info("[컨트롤러] 요청받은 userId: {}", userId);

            // 서비스 호출
            List<Map<String, Object>> result = storyService.getGameStagesForUser(userId);

            logger.info("[컨트롤러] 서비스에서 받은 {} entries", result.size());
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            logger.warn("[컨트롤러] Entity not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Not Found: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("[컨트롤러] Error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error: " + e.getMessage()));
        }
    }

}