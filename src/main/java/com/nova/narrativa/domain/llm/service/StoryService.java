package com.nova.narrativa.domain.llm.service;

import com.nova.narrativa.domain.llm.entity.GameEntity;
import java.util.List;

public interface StoryService {
    String startGame(String genre, List<String> tags);
    String continueStory(
            String genre,
            int currentStage,
            String initialStory,
            String userInput,
            String previousStory,
            String conversationHistory
    );
    GameEntity saveGame(GameEntity gameEntity);
    List<GameEntity> getGamesByUserId(Long userId);
    GameEntity getGameById(Long gameId);
}