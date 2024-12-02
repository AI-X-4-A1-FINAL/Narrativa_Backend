package com.nova.narrativa.domain.llm.controller;

import com.nova.narrativa.domain.llm.dto.ChatRequest;
import com.nova.narrativa.domain.llm.dto.StoryStartRequest;
import com.nova.narrativa.domain.llm.entity.GameEntity;
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
            String storyResponse = storyService.startGame(request.getGenre(), request.getTags());

            // 새로운 게임 엔티티 생성 및 저장
            GameEntity gameEntity = new GameEntity();
            gameEntity.setGenre(request.getGenre());
            gameEntity.setCurrentStage(0); // 시작 스테이지
            gameEntity.setInitialStory(storyResponse);

            // 저장
            storyService.saveGame(gameEntity);

            return ResponseEntity.ok(storyResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/chat")
    public ResponseEntity<String> continueStory(@Valid @RequestBody ChatRequest request) {
        try {
            // List<String>을 String으로 변환
            String conversationHistory = String.join("\n", request.getConversationHistory());

            // 이전 사용자 입력이 null일 경우 빈 문자열로 설정
            String previousUserInput = request.getPreviousUserInput() == null ? "" : request.getPreviousUserInput();

            String storyResponse = storyService.continueStory(
                    request.getGenre(),
                    request.getCurrentStage(),
                    request.getInitialStory(),
                    request.getUserSelect(),
                    request.getPreviousUserInput(),
                    conversationHistory
            );

            // 게임 상태 업데이트 및 저장
            GameEntity gameEntity = new GameEntity();
            gameEntity.setGenre(request.getGenre());
            gameEntity.setCurrentStage(request.getCurrentStage());
            gameEntity.setInitialStory(request.getInitialStory());
            gameEntity.setUserSelect(request.getUserSelect());
            gameEntity.setPreviousUserInput(previousUserInput);
            gameEntity.setConversationHistory(request.getConversationHistory());

            // 저장
            storyService.saveGame(gameEntity);

            return ResponseEntity.ok(storyResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}