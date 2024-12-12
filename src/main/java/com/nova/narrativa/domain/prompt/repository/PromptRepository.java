package com.nova.narrativa.domain.prompt.repository;


import com.nova.narrativa.domain.prompt.entity.Prompt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.method.P;


import java.util.List;
import java.util.Optional;


public interface PromptRepository extends JpaRepository<Prompt, Long> {
    List<Prompt> findRandomPromptByGenre(String genre);
    List<Prompt> findByGenreAndIsActiveTrue(String genre);
    List<Prompt> findByIsActiveTrue();  // 추가
    List<Prompt> findByGenreContainingAndIsActiveTrue(String genre);  // 추가
}