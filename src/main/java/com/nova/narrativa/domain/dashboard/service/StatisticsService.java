package com.nova.narrativa.domain.dashboard.service;

import com.nova.narrativa.domain.dashboard.entity.TrafficStats;
import com.nova.narrativa.domain.dashboard.repository.StatsQueryRepository;
import com.nova.narrativa.domain.dashboard.repository.TrafficStatsRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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
        LocalDateTime startOfWeek = now.minusDays(6).toLocalDate().atStartOfDay();

        // 현재 트래픽 (최근 1분)
        Long currentTraffic = trafficStatsRepository.findFirstByOrderByTimestampDesc()
                .map(TrafficStats::getVisitCount)
                .orElse(0L);

        // 오늘의 총 트래픽
        Long totalDailyTraffic = trafficStatsRepository.sumVisitCountBetween(startOfDay, now);

        // 주간 총 트래픽
        Long totalWeeklyTraffic = trafficStatsRepository.sumVisitCountForWeek(startOfWeek, now);

        // 시간별 트래픽 데이터
        List<Object[]> hourlyTrafficData = trafficStatsRepository.findHourlyTrafficForToday(startOfDay, now);
        List<Map<String, Object>> hourlyTraffic = new ArrayList<>();

        // 0-23시까지 모든 시간대 초기화
        for (int hour = 0; hour < 24; hour++) {
            Map<String, Object> hourData = new LinkedHashMap<>();
            hourData.put("hour", String.format("%02d", hour));
            hourData.put("count", 0L);
            hourlyTraffic.add(hourData);
        }

        // 실제 데이터로 업데이트
        for (Object[] row : hourlyTrafficData) {
            int hour = ((Number) row[0]).intValue();
            Long count = (Long) row[1];
            hourlyTraffic.get(hour).put("count", count);
        }

        // 최근 7일간의 일별 트래픽
        List<Object[]> weeklyTraffic = trafficStatsRepository.findDailyTrafficForDateRange(startOfWeek, now);
        Map<String, Long> dailyTrafficMap = new LinkedHashMap<>();

        // 최근 7일 모든 날짜에 대해 데이터 초기화 (트래픽이 0인 날도 표시하기 위함)
        for (int i = 0; i < 7; i++) {
            LocalDate date = now.minusDays(i).toLocalDate();
            dailyTrafficMap.put(date.toString(), 0L);
        }

        // 실제 트래픽 데이터로 업데이트
        for (Object[] row : weeklyTraffic) {
            LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
            Long count = (Long) row[1];
            dailyTrafficMap.put(date.toString(), count);
        }

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
        stats.put("totalWeeklyTraffic", totalWeeklyTraffic != null ? totalWeeklyTraffic : 0L);
        stats.put("hourlyTraffic", hourlyTraffic);
        stats.put("weeklyTraffic", dailyTrafficMap);
        stats.put("totalUsers", totalUsers);
        stats.put("gamesByGenre", genreStats);
        stats.put("timestamp", now);

        return stats;
    }
}