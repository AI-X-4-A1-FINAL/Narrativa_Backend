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

}
