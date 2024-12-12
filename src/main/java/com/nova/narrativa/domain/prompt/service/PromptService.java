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

    public PromptDTO getRandomPromptByGenre(String genre) {
        List<Prompt> prompts = promptRepository.findByGenreAndIsActiveTrue(genre);
        if (prompts.isEmpty()) {
            throw new RuntimeException("No prompt found for genre: " + genre);
        }

        Random random = new Random();
        Prompt prompt = prompts.get(random.nextInt(prompts.size()));
        return PromptDTO.fromEntity(prompt);
    }

    public PromptDTO getPrompt(Long id) {
        Prompt prompt = promptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No prompt found with id: " + id));
        return PromptDTO.fromEntity(prompt);
    }

    @Transactional
    public PromptDTO createPrompt(PromptDTO promptDTO) {
        Prompt prompt = new Prompt();
        prompt.setGenre(promptDTO.getGenre());
        prompt.setTitle(promptDTO.getTitle());
        prompt.setContent(promptDTO.getContent());
        prompt.setActive(true);

        Prompt savedPrompt = promptRepository.save(prompt);
        return PromptDTO.fromEntity(savedPrompt);
    }

    @Transactional
    public PromptDTO updatePrompt(Long id, PromptDTO promptDTO) {
        Prompt prompt = promptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No prompt found with id: " + id));

        // 수정할 필드들 업데이트
        if (promptDTO.getGenre() != null) {
            prompt.setGenre(promptDTO.getGenre());
        }
        if (promptDTO.getTitle() != null) {
            prompt.setTitle(promptDTO.getTitle());
        }
        if (promptDTO.getContent() != null) {
            prompt.setContent(promptDTO.getContent());
        }

        Prompt updatedPrompt = promptRepository.save(prompt);
        return PromptDTO.fromEntity(updatedPrompt);
    }

    public List<PromptDTO> getAllPrompts() {
        return promptRepository.findByIsActiveTrue().stream()
                .map(PromptDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<PromptDTO> searchPromptsByGenre(String genre) {
        return promptRepository.findByGenreContainingAndIsActiveTrue(genre).stream()
                .map(PromptDTO::fromEntity)
                .collect(Collectors.toList());
    }



}