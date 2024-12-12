package com.nova.narrativa.domain.dashboard.controller;

import com.nova.narrativa.domain.dashboard.dto.GamePlaytimeDTO;
import com.nova.narrativa.domain.dashboard.dto.GenrePlaytimeDTO;
import com.nova.narrativa.domain.dashboard.service.GamePlaytimeService;
import com.nova.narrativa.domain.dashboard.util.TimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class GamePlaytimeController {
    private final GamePlaytimeService gamePlaytimeService;

    @GetMapping("/games/playtime")
    public List<Map<String, Object>> getGamePlaytimes() {
        List<GamePlaytimeDTO> playTimes = gamePlaytimeService.getAveragePlaytimePerGame();
        return playTimes.stream()
                .map(pt -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("gameId", pt.getGameId());
                    result.put("averagePlaytimeInSeconds", pt.getAveragePlaytimeInSeconds());
                    result.put("formattedPlaytime", TimeFormatter.formatSeconds(pt.getAveragePlaytimeInSeconds()));
                    return result;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/games/playtime/genre")
    public List<GenrePlaytimeDTO> getPlaytimesByGenre() {
        return gamePlaytimeService.getAveragePlaytimePerGenre();
    }
}