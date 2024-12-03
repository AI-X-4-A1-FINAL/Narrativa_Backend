package com.nova.narrativa.domain.llm.controller;

import com.nova.narrativa.domain.llm.dto.ChatRequest;
import com.nova.narrativa.domain.llm.dto.StoryStartRequest;
import com.nova.narrativa.domain.llm.service.StoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;

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
    public ResponseEntity<String> startGame(@Valid @RequestBody StoryStartRequest request) {
        try {
            String storyResponse = storyService.startGame(
                    request.getGenre(),
                    request.getTags(),
                    request.getUserId()
            );
            return ResponseEntity.ok(storyResponse);

        } catch (IllegalArgumentException e) {
            logger.error("잘못된 파라미터: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("잘못된 요청: " + e.getMessage());

        } catch (NoSuchElementException e) {
            logger.error("데이터를 찾을 수 없음: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("게임을 찾을 수 없습니다: " + e.getMessage());

        } catch (Exception e) {
            logger.error("예상치 못한 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("예상치 못한 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/chat")
    public ResponseEntity<String> continueStory(@Valid @RequestBody ChatRequest request) {
        try {
            String conversationHistory = String.join("\n", request.getConversationHistory());

            String previousUserInput = request.getPreviousUserInput() == null ? "" : request.getPreviousUserInput();

            String storyResponse = storyService.continueStory(
                    request.getGameId(),
                    request.getGenre(),
                    request.getCurrentStage(),
                    request.getInitialStory(),
                    request.getUserSelect(),
                    previousUserInput,
                    conversationHistory
            );

            return ResponseEntity.ok(storyResponse);

        } catch (IllegalArgumentException e) {
            logger.error("잘못된 인자: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("잘못된 입력: " + e.getMessage());

        } catch (NoSuchElementException e) {
            logger.error("데이터를 찾을 수 없음: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("게임 상태를 찾을 수 없습니다: " + e.getMessage());

        } catch (NullPointerException e) {
            logger.error("NullPointer 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("빈 값이 있습니다: " + e.getMessage());

        } catch (Exception e) {
            logger.error("예상치 못한 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("예상치 못한 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
