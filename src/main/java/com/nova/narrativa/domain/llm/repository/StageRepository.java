package com.nova.narrativa.domain.llm.repository;

import com.nova.narrativa.domain.llm.entity.Stage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StageRepository extends JpaRepository<Stage, Long> {

}