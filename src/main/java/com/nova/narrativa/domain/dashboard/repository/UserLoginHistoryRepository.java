package com.nova.narrativa.domain.dashboard.repository;

import com.nova.narrativa.domain.dashboard.entity.UserLoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UserLoginHistoryRepository extends JpaRepository<UserLoginHistory, Long> {

    // 특정 월의 모든 일별 활성 사용자 수 (없는 날짜는 0으로)
    @Query(value =
            "WITH RECURSIVE DateSequence AS (" +
                    "    SELECT DATE_FORMAT(:baseDate, '%Y-%m-01') as date " +
                    "    UNION ALL " +
                    "    SELECT DATE_ADD(date, INTERVAL 1 DAY) " +
                    "    FROM DateSequence " +
                    "    WHERE date < LAST_DAY(:baseDate) " +
                    "), DailyStats AS (" +
                    "    SELECT d.date, " +
                    "           COALESCE(COUNT(DISTINCT h.user_id), 0) as count " +
                    "    FROM DateSequence d " +
                    "    LEFT JOIN user_login_history h ON DATE(h.login_date) = d.date " +
                    "    GROUP BY d.date " +
                    ") " +
                    "SELECT date as date, count as count " +
                    "FROM DailyStats " +
                    "ORDER BY date",
            nativeQuery = true)
    List<DailyStatsProjection> getDailyActiveUsers(@Param("baseDate") LocalDate baseDate);

    // 특정 연도의 모든 월별 활성 사용자 수 (없는 월은 0으로)
    @Query(value =
            "WITH RECURSIVE MonthSequence AS (" +
                    "    SELECT DATE_FORMAT(:baseDate, '%Y-01-01') as date " +
                    "    UNION ALL " +
                    "    SELECT DATE_ADD(date, INTERVAL 1 MONTH) " +
                    "    FROM MonthSequence " +
                    "    WHERE date < DATE_FORMAT(:baseDate, '%Y-12-01') " +
                    "), MonthlyStats AS (" +
                    "    SELECT m.date, " +
                    "           COALESCE(COUNT(DISTINCT h.user_id), 0) as count " +
                    "    FROM MonthSequence m " +
                    "    LEFT JOIN user_login_history h ON " +
                    "        DATE_FORMAT(h.login_date, '%Y-%m') = DATE_FORMAT(m.date, '%Y-%m') " +
                    "    GROUP BY m.date " +
                    ") " +
                    "SELECT date as date, count as count " +
                    "FROM MonthlyStats " +
                    "ORDER BY date",
            nativeQuery = true)
    List<DailyStatsProjection> getMonthlyActiveUsers(@Param("baseDate") LocalDate baseDate);

    interface DailyStatsProjection {
        LocalDate getDate();
        Long getCount();
    }
}
