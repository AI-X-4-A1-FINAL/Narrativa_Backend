package com.nova.narrativa.domain.llm.service;

import com.nova.narrativa.domain.llm.entity.Game;

import java.util.List;

public interface StoryService {
    String startGame(String genre, List<String> tags, Long userId);

    String continueStory(String storyId, String genre, String userChoice);

    String generateEnding(String storyId);

    Game saveGame(Game game);

    List<Game> getGamesByUserId(Long userId);

    Game getGameById(Long gameId);
}