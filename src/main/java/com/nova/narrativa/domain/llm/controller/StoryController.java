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
import reactor.core.publisher.Mono;

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
    public Mono<Map<String, Object>> startGame(@Valid @RequestBody StoryStartRequest request) {
        return storyService.startGame(
                request.getGenre(),
                request.getTags(),
                request.getUserId()
        );
    }

    @PostMapping("/chat")
    public Mono<String> continueStory(@RequestBody Map<String, Object> request) {
        Long gameId = Long.valueOf(String.valueOf(request.get("gameId")));
        String genre = String.valueOf(request.get("genre"));
        String userChoice = String.valueOf(request.get("userSelect"));

        return storyService.continueStory(gameId.toString(), genre, userChoice);
    }

    @PostMapping("/end")
    public Mono<String> generateEnding(@RequestBody Map<String, Object> request) {
        String gameId = String.valueOf(request.get("gameId"));
        String genre = String.valueOf(request.get("genre"));
        String userChoice = String.valueOf(request.get("userSelect"));

        return storyService.generateEnding(gameId, genre, userChoice);
    }

    // 히스토리 조회용 메서드
    @PostMapping("/history")
    public ResponseEntity<?> getUserGameStages(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(String.valueOf(request.get("userId")));
//            logger.info("[컨트롤러] 요청받은 userId: {}", userId);

            // 서비스 호출
            List<Map<String, Object>> result = storyService.getGameStagesForUser(userId);

//            logger.info("[컨트롤러] 서비스에서 받은 {}", result);

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