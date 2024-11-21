package com.nova.narrativa.domain.llm.service;

import com.nova.narrativa.domain.llm.controller.StoryRequest;
import org.springframework.stereotype.Service;

@Service
public class StoryServiceImpl implements StoryService {

    @Override
    public String generateStory(StoryRequest request) {
        // 요청 데이터를 처리하여 스토리 생성 (간단한 Mock 예제)
        return "Generated story for " + request.getGenre()
                + ", affection: " + request.getAffection()
                + ", cut: " + request.getCut()
                + ", userInput: " + request.getUserInput();
    }
}
