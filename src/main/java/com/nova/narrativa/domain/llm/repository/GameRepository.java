package com.nova.narrativa.domain.llm.repository;

import com.nova.narrativa.domain.llm.entity.GameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<GameEntity, Long> {
    List<GameEntity> findByUser_Id(Long userId);
}