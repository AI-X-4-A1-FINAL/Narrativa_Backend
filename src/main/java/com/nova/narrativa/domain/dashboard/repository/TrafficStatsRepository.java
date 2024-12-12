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
}
