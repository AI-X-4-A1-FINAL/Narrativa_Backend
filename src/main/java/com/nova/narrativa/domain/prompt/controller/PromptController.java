package com.nova.narrativa.domain.prompt.controller;

import com.nova.narrativa.domain.prompt.dto.PromptDTO;
import com.nova.narrativa.domain.prompt.service.PromptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prompts")
@CrossOrigin(origins = "*")
public class PromptController {

    private final PromptService promptService;

    @Autowired
    public PromptController(PromptService promptService) {
        this.promptService = promptService;
    }

    @GetMapping
    public ResponseEntity<List<PromptDTO>> getAllPrompts() {
        List<PromptDTO> prompts = promptService.getAllPrompts();
        return ResponseEntity.ok(prompts);
    }

    @PostMapping
    public ResponseEntity<PromptDTO> createPrompt(@RequestBody PromptDTO promptDTO) {
        PromptDTO createdPrompt = promptService.createPrompt(promptDTO);
        return ResponseEntity.ok(createdPrompt);
    }

    @GetMapping("/search")
    public ResponseEntity<List<PromptDTO>> searchPromptsByGenre(@RequestParam String genre) {
        List<PromptDTO> prompts = promptService.searchPromptsByGenre(genre);
        return ResponseEntity.ok(prompts);
    }

    @GetMapping("/random")
    public ResponseEntity<PromptDTO> getRandomPrompt(@RequestParam String genre) {
        try {
            PromptDTO prompt = promptService.getRandomPromptByGenre(genre);
            return ResponseEntity.ok(prompt);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromptDTO> getPrompt(@PathVariable Long id) {
        try {
            PromptDTO prompt = promptService.getPrompt(id);
            return ResponseEntity.ok(prompt);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromptDTO> updatePrompt(@PathVariable Long id, @RequestBody PromptDTO promptDTO) {
        try {
            PromptDTO updatedPrompt = promptService.updatePrompt(id, promptDTO);
            return ResponseEntity.ok(updatedPrompt);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}