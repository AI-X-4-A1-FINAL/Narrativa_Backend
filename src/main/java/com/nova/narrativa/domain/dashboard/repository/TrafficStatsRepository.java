package com.nova.narrativa.domain.dashboard.repository;

import com.nova.narrativa.domain.dashboard.entity.TrafficStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TrafficStatsRepository extends JpaRepository<TrafficStats, Long> {
    Optional<TrafficStats> findFirstByOrderByTimestampDesc();

    // 오늘의 총 트래픽
    @Query("SELECT SUM(t.visitCount) FROM TrafficStats t " +
            "WHERE t.timestamp >= :startTime AND t.timestamp < :endTime")
    Long sumVisitCountBetween(@Param("startTime") LocalDateTime startTime,
                              @Param("endTime") LocalDateTime endTime);

    // 특정 날짜의 시간별 트래픽
    @Query("SELECT t FROM TrafficStats t " +
            "WHERE t.timestamp >= :startTime AND t.timestamp < :endTime " +
            "ORDER BY t.timestamp DESC")
    List<TrafficStats> findAllByTimestampBetween(@Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);

    @Query("SELECT DATE(t.timestamp) as date, SUM(t.visitCount) as total " +
            "FROM TrafficStats t " +
            "WHERE t.timestamp >= :startDate AND t.timestamp <= :endDate " +
            "GROUP BY DATE(t.timestamp) " +
            "ORDER BY DATE(t.timestamp) DESC")
    List<Object[]> findDailyTrafficForDateRange(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT HOUR(t.timestamp) as hour, SUM(t.visitCount) as count " +
            "FROM TrafficStats t " +
            "WHERE t.timestamp >= :startOfDay AND t.timestamp <= :now " +
            "GROUP BY HOUR(t.timestamp) " +
            "ORDER BY HOUR(t.timestamp)")
    List<Object[]> findHourlyTrafficForToday(LocalDateTime startOfDay, LocalDateTime now);

    @Query("SELECT SUM(t.visitCount) " +
            "FROM TrafficStats t " +
            "WHERE t.timestamp >= :startOfWeek AND t.timestamp <= :now")
    Long sumVisitCountForWeek(LocalDateTime startOfWeek, LocalDateTime now);
}
