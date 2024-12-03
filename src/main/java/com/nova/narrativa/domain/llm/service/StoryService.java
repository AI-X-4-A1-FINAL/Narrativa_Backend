package com.nova.narrativa.domain.llm.service;

import com.nova.narrativa.domain.llm.entity.Game;
import java.util.List;

public interface StoryService {
    String startGame(String genre, List<String> tags, Long userId);
    String continueStory(Long gameId, String genre, int currentStage,
                         String initialStory, String userInput,
                         String previousStory, String conversationHistory);
    Game saveGame(Game game);
    List<Game> getGamesByUserId(Long userId);
    Game getGameById(Long gameId);
}