package com.nova.narrativa.domain.llm.controller;

import com.nova.narrativa.domain.llm.dto.ChatRequest;
import com.nova.narrativa.domain.llm.dto.StoryStartRequest;
import com.nova.narrativa.domain.llm.service.StoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/generate-story")
public class StoryController {

    private final StoryService storyService;

    @Autowired
    public StoryController(StoryService storyService) {
        this.storyService = storyService;
    }

    @PostMapping("/start")
    public ResponseEntity<String> startGame(@Valid @RequestBody StoryStartRequest request) {
        try {
            String storyResponse = storyService.startGame(
                    request.getGenre(),
                    request.getTags(),
                    request.getUserId()
            );
            return ResponseEntity.ok(storyResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/chat")
    public ResponseEntity<String> continueStory(@Valid @RequestBody ChatRequest request) {
        try {
            // StoryService 호출
            String storyResponse = storyService.continueStory(
                    request.getGameId().toString(),  // story_id
                    request.getGenre(),
                    request.getUserSelect()  // user_choice
            );
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
