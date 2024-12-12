package com.nova.narrativa.domain.llm.controller;

import com.nova.narrativa.domain.llm.dto.ContinueStoryRequest;
import com.nova.narrativa.domain.llm.dto.GenerateEndingRequest;
import com.nova.narrativa.domain.llm.dto.GetUserGameStagesRequest;
import com.nova.narrativa.domain.llm.dto.StoryStartRequest;
import com.nova.narrativa.domain.llm.service.StoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "게임 스토리 컨트롤러", description = "스토리 생성과 히스토리 조회 관련 컨트롤러입니다.")
public class StoryController {

    private final StoryService storyService;
    private static final Logger logger = LoggerFactory.getLogger(StoryController.class);

    @Autowired
    public StoryController(StoryService storyService) {
        this.storyService = storyService;
    }

    @Operation(
            summary = "게임 시작",
            description = "초기 세계관 생성 엔드포인트입니다. 장르, 테그, 유저Id는 필수 값입니다.")
    @PostMapping("/start")
    public Mono<Map<String, Object>> startGame(@Valid @RequestBody StoryStartRequest request) {
        return storyService.startGame(
                request.getGenre(),
                request.getTags(),
                request.getUserId()
        );
    }

    @Operation(
            summary = "게임 페이지 진행",
            description = "초기 세계관 이후 게임 진행 엔드포인트입니다. 장르, 게임ID, 유저의 선택은 필수입니다.")
    @PostMapping("/chat")
    public Mono<String> continueStory(@Valid @RequestBody ContinueStoryRequest request) {
        return storyService.continueStory(
                request.getGameId().toString(),
                request.getGenre(),
                request.getUserSelect()
        );
    }

    @Operation(
            summary = "게임 종료",
            description = "게임 엔딩페이지 엔드포인트입니다. 장르, 게임ID, 유저의 선택은 필수입니다.")
    @PostMapping("/end")
    public Mono<String> generateEnding(@Valid @RequestBody GenerateEndingRequest request) {
        return storyService.generateEnding(
                request.getGameId().toString(),
                request.getGenre(),
                request.getUserSelect()
        );
    }

    // 히스토리 조회용 메서드
    @Operation(
            summary = "히스토리",
            description = "히스토리 조회 엔드포인트입니다. 유저Id는 필수 값입니다.")
    @PostMapping("/history")
    public ResponseEntity<?> getUserGameStages(@Valid @RequestBody GetUserGameStagesRequest request) {
        try {
            Long userId = request.getUserId();
            List<Map<String, Object>> result = storyService.getGameStagesForUser(userId);
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