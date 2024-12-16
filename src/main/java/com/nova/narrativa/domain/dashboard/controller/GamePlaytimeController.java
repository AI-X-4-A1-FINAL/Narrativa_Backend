package com.nova.narrativa.domain.dashboard.controller;

import com.nova.narrativa.domain.admin.util.AdminAuth;
import com.nova.narrativa.domain.dashboard.service.GamePlaytimeService;
import com.nova.narrativa.domain.dashboard.util.TimeFormatter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class GamePlaytimeController {
    private final GamePlaytimeService gamePlaytimeService;

    public GamePlaytimeController(GamePlaytimeService gamePlaytimeService) {
        this.gamePlaytimeService = gamePlaytimeService;
    }

    @AdminAuth
    @GetMapping("/games/playtime")
    public ResponseEntity<?> getGamePlaytimes() {
        List<Map<String, Object>> result = gamePlaytimeService.getAveragePlaytimePerGame().stream()
                .map(pt -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("gameId", pt.getGameId());
                    map.put("averagePlaytimeInSeconds", pt.getAveragePlaytimeInSeconds());
                    map.put("formattedPlaytime", TimeFormatter.formatSeconds(pt.getAveragePlaytimeInSeconds()));
                    return map;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @AdminAuth
    @GetMapping("/games/playtime/genre")
    public ResponseEntity<?> getPlaytimesByGenre() {
        return ResponseEntity.ok(gamePlaytimeService.getAveragePlaytimePerGenre());
    }
}