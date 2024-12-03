package com.nova.narrativa.domain.llm.controller;

import com.nova.narrativa.common.codes.ErrorCode;
import com.nova.narrativa.common.codes.SuccessCode;
import com.nova.narrativa.common.response.ApiResponse;
import com.nova.narrativa.domain.llm.dto.ChatRequest;
import com.nova.narrativa.domain.llm.dto.StoryStartRequest;
import com.nova.narrativa.domain.llm.entity.Game;
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
            // userId를 포함하여 서비스 메서드 호출
            String storyResponse = storyService.startGame(
                    request.getGenre(),
                    request.getTags(),
                    request.getUserId()  // userId 추가
            );
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
                    request.getGameId(),  // gameId 추가
                    request.getGenre(),
                    request.getCurrentStage(),
                    request.getInitialStory(),
                    request.getUserSelect(),
                    request.getPreviousUserInput(),
                    conversationHistory
            );

            return ResponseEntity.ok(storyResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}
