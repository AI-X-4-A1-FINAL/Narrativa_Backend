package com.nova.narrativa.domain.llm.service;

import java.util.List;

public interface StoryService {
    String startGame(String genre, List<String> tags);
    String continueStory(String previousStory, String userInput, int currentStage, String genre);
}

