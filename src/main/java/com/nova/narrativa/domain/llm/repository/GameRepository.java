package com.nova.narrativa.domain.llm.repository;

import com.nova.narrativa.domain.llm.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findByUser_Id(Long userId);
}