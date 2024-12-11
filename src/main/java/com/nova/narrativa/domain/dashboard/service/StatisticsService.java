package com.nova.narrativa.domain.dashboard.service;

import com.nova.narrativa.domain.dashboard.entity.TrafficStats;
import com.nova.narrativa.domain.dashboard.repository.StatsQueryRepository;
import com.nova.narrativa.domain.dashboard.repository.TrafficStatsRepository;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StatisticsService {
    private final StatsQueryRepository statsQueryRepository;
    private final TrafficStatsRepository trafficStatsRepository;
    private Long currentVisitCount = 0L;

    public void incrementVisitCount() {
        currentVisitCount++;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void saveTrafficStats() {
        TrafficStats stats = TrafficStats.builder()
                .timestamp(LocalDateTime.now())
                .visitCount(currentVisitCount)
                .build();

        trafficStatsRepository.save(stats);
        currentVisitCount = 0L;
    }

    public Map<String, Object> getBasicStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();

        // 현재 트래픽 (최근 1분)
        Long currentTraffic = trafficStatsRepository.findFirstByOrderByTimestampDesc()
                .map(TrafficStats::getVisitCount)
                .orElse(0L);

        // 오늘의 총 트래픽
        Long totalDailyTraffic = trafficStatsRepository.sumVisitCountBetween(startOfDay, now);

        // 총 사용자 수
        Long totalUsers = statsQueryRepository.countTotalUsers();

        // 장르별 게임 실행 횟수
        Map<String, Long> genreStats = statsQueryRepository.countGamesByGenre()
                .stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1],
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        stats.put("currentTraffic", currentTraffic);
        stats.put("totalDailyTraffic", totalDailyTraffic != null ? totalDailyTraffic : 0L);
        stats.put("totalUsers", totalUsers);
        stats.put("gamesByGenre", genreStats);
        stats.put("timestamp", now);

        return stats;
    }
}