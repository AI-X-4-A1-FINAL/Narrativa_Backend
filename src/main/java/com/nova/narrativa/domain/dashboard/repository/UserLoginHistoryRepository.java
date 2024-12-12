package com.nova.narrativa.domain.dashboard.repository;

import com.nova.narrativa.domain.dashboard.dto.ActiveUsersDTO;
import com.nova.narrativa.domain.dashboard.entity.UserLoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface UserLoginHistoryRepository extends JpaRepository<UserLoginHistory, Long> {

    @Query(nativeQuery = true, value = """
        SELECT 
            COUNT(DISTINCT CASE WHEN login_date = :today THEN user_id END) as dau,
            COUNT(DISTINCT CASE WHEN login_date BETWEEN :monthStart AND :today THEN user_id END) as mau
        FROM user_login_history 
        WHERE login_date BETWEEN :monthStart AND :today
    """)
    ActiveUsersDTO getActiveUsersStats(
            @Param("today") LocalDate today,
            @Param("monthStart") LocalDate monthStart
    );
}
