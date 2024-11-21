package com.nova.narrativa.domain.llm.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/generate-story")
public class StoryController {



    @PostMapping
    public StoryResponse generateStory(@RequestBody StoryRequest request) {


        // Mock 응답 생성
        String generatedStory = "Generated story for " + request.getGenre()
                + ", affection: " + request.getAffection()
                + ", cut: " + request.getCut()
                + ", userInput: " + request.getUserInput();



        // 응답 반환
        return new StoryResponse(generatedStory);
    }
}
