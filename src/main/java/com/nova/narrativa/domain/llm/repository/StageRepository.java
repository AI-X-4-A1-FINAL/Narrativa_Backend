package com.nova.narrativa.domain.llm.repository;

import com.nova.narrativa.domain.llm.entity.Stage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StageRepository extends JpaRepository<Stage, Long> {

    // 특정 게임의 모든 스테이지 조회
    List<Stage> findByGame_GameId(Long gameId);

    // 가장 높은 스테이지 번호를 가진 스테이지 조회 (특정 게임)
    Optional<Stage> findTopByGame_GameIdOrderByStageNumberDesc(Long gameId);
}
