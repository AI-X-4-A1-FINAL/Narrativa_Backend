package com.nova.narrativa.domain.prompt.repository;

import com.nova.narrativa.domain.prompt.entity.Prompt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromptRepository extends JpaRepository<Prompt, Long> {
    List<Prompt> findRandomPromptByGenre(String genre);
    List<Prompt> findByGenreAndIsActiveTrue(String genre);
    List<Prompt> findByIsActiveTrue();  // 추가
    List<Prompt> findByGenreContainingAndIsActiveTrue(String genre);

    List<Prompt> findByGenreContaining(String genre);
}