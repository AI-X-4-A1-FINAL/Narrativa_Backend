package com.nova.narrativa.domain.prompt.service;

import com.nova.narrativa.domain.prompt.dto.PromptDTO;
import com.nova.narrativa.domain.prompt.entity.Prompt;
import com.nova.narrativa.domain.prompt.repository.PromptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PromptService {
    private final PromptRepository promptRepository;

    @Autowired
    public PromptService(PromptRepository promptRepository) {
        this.promptRepository = promptRepository;
    }

    // 1. 조회 관련 메서드들
    public PromptDTO getPrompt(Long id) {
        Prompt prompt = promptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No prompt found with id: " + id));
        return PromptDTO.fromEntity(prompt);
    }

    public List<PromptDTO> getAllPrompts() {
        return promptRepository.findByIsActiveTrue().stream()
                .map(PromptDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<PromptDTO> getPrompts() {
        return promptRepository.findAll().stream()
                .map(PromptDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public PromptDTO getRandomPromptByGenre(String genre) {
        List<Prompt> prompts = promptRepository.findByGenreAndIsActiveTrue(genre);
        if (prompts.isEmpty()) {
            throw new RuntimeException("No prompt found for genre: " + genre);
        }
        return PromptDTO.fromEntity(prompts.get(new Random().nextInt(prompts.size())));
    }

    // 2. 검색 관련 메서드들
    public List<PromptDTO> searchPromptsByGenre(String genre) {
        return promptRepository.findByGenreContainingAndIsActiveTrue(genre).stream()
                .map(PromptDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<PromptDTO> searchAllPromptsByGenre(String genre) {
        return promptRepository.findByGenreContaining(genre).stream()
                .map(PromptDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 3. 수정 관련 메서드들
    @Transactional
    public PromptDTO createPrompt(PromptDTO promptDTO) {
        Prompt prompt = new Prompt();
        prompt.setGenre(promptDTO.getGenre());
        prompt.setTitle(promptDTO.getTitle());
        prompt.setContent(promptDTO.getContent());
        prompt.setActive(promptDTO.isActive());  // 활성화 상태 설정

        return PromptDTO.fromEntity(promptRepository.save(prompt));
    }

    @Transactional
    public PromptDTO updatePrompt(Long id, PromptDTO promptDTO) {
        Prompt prompt = promptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No prompt found with id: " + id));

        if (promptDTO.getGenre() != null) prompt.setGenre(promptDTO.getGenre());
        if (promptDTO.getTitle() != null) prompt.setTitle(promptDTO.getTitle());
        if (promptDTO.getContent() != null) prompt.setContent(promptDTO.getContent());
        prompt.setActive(promptDTO.isActive());

        return PromptDTO.fromEntity(promptRepository.save(prompt));
    }

    @Transactional
    public PromptDTO togglePromptStatus(Long id) {
        Prompt prompt = promptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No prompt found with id: " + id));
        prompt.setActive(!prompt.isActive());
        return PromptDTO.fromEntity(promptRepository.save(prompt));
    }
}