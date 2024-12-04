package com.nova.narrativa.domain.llm.service;

import com.nova.narrativa.domain.llm.entity.Game;

import java.util.List;

public interface StoryService {
    String startGame(String genre, List<String> tags, Long userId);

    // ML 서버 요청 형식에 맞게 파라미터 간소화
    String continueStory(String storyId, String genre, String userChoice);

    Game saveGame(Game game);

    List<Game> getGamesByUserId(Long userId);

    Game getGameById(Long gameId);
}
