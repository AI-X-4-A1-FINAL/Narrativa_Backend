package com.nova.narrativa.domain.llm.service;

import java.util.List;

public interface StoryService {
    String startGame(String genre, List<String> tags);
    String continueStory(
            int currentStage,
            String genre,
            String initialStory,
            String previousStory,
            String userInput
    );
}


