package com.nova.narrativa.domain.admin.controller;

import com.nova.narrativa.domain.admin.dto.*;
import com.nova.narrativa.domain.admin.service.FirebaseAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final FirebaseAnalyticsService analyticsService;

    @GetMapping("/summary")
    @CrossOrigin(origins = "${environments.narrativa-admin.url}", allowCredentials = "true")
    public ResponseEntity<AnalyticsSummaryDto> getAnalyticsSummary() {
        Map<String, Object> genreStats = analyticsService.getGameStatsByGenre();
        int activeUsers = analyticsService.getActiveUsers();
        Map<String, Integer> pageVisits = analyticsService.getPageVisits();
        Map<String, Integer> hourlyStats = analyticsService.getHourlyActiveUsers();

        return ResponseEntity.ok(AnalyticsSummaryDto.builder()
                .totalActiveUsers(activeUsers)
                .totalPageViews(pageVisits.values().stream().mapToInt(Integer::intValue).sum())
                .genreStats(convertGenreStats(genreStats))
                .pageVisits(convertPageVisits(pageVisits))
                .hourlyStats(convertHourlyStats(hourlyStats))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/genre-stats")
    @CrossOrigin(origins = "${environments.narrativa-admin.url}", allowCredentials = "true")
    public ResponseEntity<List<GenreStatsDto>> getGenreStats() {
        Map<String, Object> stats = analyticsService.getGameStatsByGenre();
        return ResponseEntity.ok(convertGenreStats(stats));
    }

    @GetMapping("/active-users")
    @CrossOrigin(origins = "${environments.narrativa-admin.url}", allowCredentials = "true")
    public ResponseEntity<Integer> getActiveUsers() {
        return ResponseEntity.ok(analyticsService.getActiveUsers());
    }

    @GetMapping("/page-visits")
    @CrossOrigin(origins = "${environments.narrativa-admin.url}", allowCredentials = "true")
    public ResponseEntity<List<PageVisitDto>> getPageVisits() {
        Map<String, Integer> visits = analyticsService.getPageVisits();
        return ResponseEntity.ok(convertPageVisits(visits));
    }

    @GetMapping("/hourly-stats")
    @CrossOrigin(origins = "${environments.narrativa-admin.url}", allowCredentials = "true")
    public ResponseEntity<List<HourlyStatsDto>> getHourlyStats() {
        Map<String, Integer> stats = analyticsService.getHourlyActiveUsers();
        return ResponseEntity.ok(convertHourlyStats(stats));
    }

    // 변환 메서드들
    private List<GenreStatsDto> convertGenreStats(Map<String, Object> stats) {
        // Firebase Analytics 응답 데이터를 GenreStatsDto 리스트로 변환
        return stats.entrySet().stream()
                .map(entry -> GenreStatsDto.builder()
                        .genre(entry.getKey())
                        .playCount(((Map<String, Integer>) entry.getValue()).get("playCount"))
                        .averagePlayTime(((Map<String, Double>) entry.getValue()).get("averagePlayTime"))
                        .build())
                .collect(Collectors.toList());
    }

    private List<PageVisitDto> convertPageVisits(Map<String, Integer> visits) {
        return visits.entrySet().stream()
                .map(entry -> PageVisitDto.builder()
                        .pagePath(entry.getKey())
                        .visitCount(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    private List<HourlyStatsDto> convertHourlyStats(Map<String, Integer> stats) {
        return stats.entrySet().stream()
                .map(entry -> HourlyStatsDto.builder()
                        .hour(entry.getKey())
                        .activeUsers(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }
}