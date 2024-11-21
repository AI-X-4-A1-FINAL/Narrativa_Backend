package com.nova.narrativa.domain.llm.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class StoryController {

    @PostMapping("/generate-story")
    public StoryResponse generateStory(@RequestBody StoryRequest request) {
        // 요청 데이터를 기반으로 간단한 Mock 응답 생성
        String generatedStory = "Generated story for " + request.getGenre()
                + ", affection: " + request.getAffection()
                + ", cut: " + request.getCut()
                + ", userInput: " + request.getUserInput();

        // 응답 반환
        return new StoryResponse(generatedStory);
    }
}
