package com.nova.narrativa.domain.llm.controller;

import com.nova.narrativa.domain.llm.dto.ChatRequest;
import com.nova.narrativa.domain.llm.dto.StoryStartRequest;
import com.nova.narrativa.domain.llm.service.StoryService;
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
    public ResponseEntity<String> startGame(@RequestBody StoryStartRequest request) {
        try {
            String storyResponse = storyService.startGame(request.getGenre(), request.getTags());
            return ResponseEntity.ok(storyResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/chat")
    public ResponseEntity<String> continueStory(@RequestBody ChatRequest request) {
        try {
            String storyResponse = storyService.continueStory(request.getInitialStory(), request.getUserInput(),request.getCurrentStage(), request.getGenre());
            return ResponseEntity.ok(storyResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

}