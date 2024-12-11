package com.nova.narrativa.domain.dashboard.repository;

import com.nova.narrativa.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatsQueryRepository extends JpaRepository<User, Long> {
    // 총 사용자 수
    @Query("SELECT COUNT(u) FROM User u")
    Long countTotalUsers();

    // 장르별 게임 실행 횟수
    @Query("SELECT g.genre as genre, COUNT(g) as count " +
            "FROM User u JOIN u.games g " +
            "GROUP BY g.genre")
    List<Object[]> countGamesByGenre();
}
