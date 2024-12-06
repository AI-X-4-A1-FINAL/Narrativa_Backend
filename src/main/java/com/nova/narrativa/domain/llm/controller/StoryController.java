package com.nova.narrativa.domain.llm.controller;

import com.nova.narrativa.domain.llm.dto.ChatRequest;
import com.nova.narrativa.domain.llm.dto.StoryStartRequest;
import com.nova.narrativa.domain.llm.service.StoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/generate-story")
public class StoryController {

    private final StoryService storyService;

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
        // 요청 데이터 로그
        System.out.println("[CHAT REQUEST] Request body: " + request);

        try {
            // Map에서 데이터 추출
            Long gameId = Long.valueOf(String.valueOf(request.get("gameId")));
            String genre = String.valueOf(request.get("genre"));
            String userChoice = String.valueOf(request.get("userSelect"));

            // 요청 데이터 로그
            System.out.println("[CHAT REQUEST] Game ID: " + gameId);
            System.out.println("[CHAT REQUEST] Genre: " + genre);
            System.out.println("[CHAT REQUEST] User Choice: " + userChoice);

            // StoryService 호출
            String storyResponse = storyService.continueStory(
                    gameId.toString(),
                    genre,
                    userChoice
            );
            System.out.println("[프론트로 보내는 값]: " + storyResponse); // 응답 로그 추가

            return ResponseEntity.ok(storyResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/end")
    public ResponseEntity<String> generateEnding(@Valid @RequestBody ChatRequest request) {
        try {
            System.out.println("[Controller] Received request: " + request);
            String storyResponse = storyService.generateEnding(request.getGameId().toString());
            System.out.println("[Controller] Response from service: " + storyResponse);
            return ResponseEntity.ok(storyResponse);
        } catch (Exception e) {
            System.err.println("[Controller] Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

}
