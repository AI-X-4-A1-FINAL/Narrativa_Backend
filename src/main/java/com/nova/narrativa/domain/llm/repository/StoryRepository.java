package com.nova.narrativa.domain.llm.repository;

import com.nova.narrativa.domain.llm.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {
    // 특정 게임의 모든 스테이지 조회
    List<Story> findByGame_GameId(Long gameId);

    // 가장 최신의 스테이지 조회
    Story findTopByGame_GameIdOrderByStageNumberDesc(Long gameId);
}
