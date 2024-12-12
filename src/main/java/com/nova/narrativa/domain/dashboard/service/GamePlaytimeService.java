package com.nova.narrativa.domain.dashboard.service;

import com.nova.narrativa.domain.dashboard.dto.GamePlaytimeDTO;
import com.nova.narrativa.domain.dashboard.dto.GenrePlaytimeDTO;
import com.nova.narrativa.domain.llm.repository.StageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GamePlaytimeService {
    
    private final StageRepository stageRepository;

    @Transactional(readOnly = true)
    public List<GamePlaytimeDTO> getAveragePlaytimePerGame() {
        return stageRepository.getAveragePlaytimePerGame();
    }

    @Transactional(readOnly = true)
    public List<GenrePlaytimeDTO> getAveragePlaytimePerGenre() {
        return stageRepository.getAveragePlaytimePerGenre();
    }
}