package com.nova.narrativa.domain.prompt.controller;

import com.nova.narrativa.domain.prompt.dto.PromptDTO;
import com.nova.narrativa.domain.prompt.service.PromptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.nova.narrativa.domain.admin.util.AdminAuth;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/admin/prompts")
@RequiredArgsConstructor
public class PromptController {
    private final PromptService promptService;

    @GetMapping("/random")
    public ResponseEntity<PromptDTO> getRandomPrompt(@RequestParam String genre) {
        try {
            PromptDTO prompt = promptService.getRandomPromptByGenre(genre);
            return ResponseEntity.ok(prompt);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 어드민용 메서드

    // 활성화된 프롬프트만 조회
    @AdminAuth
    @GetMapping
    public ResponseEntity<List<PromptDTO>> getAllPrompts() {
        List<PromptDTO> prompts = promptService.getAllPrompts(); // 메서드 이름 변경
        return ResponseEntity.ok(prompts);
    }

    // 모든 프롬프트 조회 (활성/비활성 모두)
    @AdminAuth
    @GetMapping("/all")
    public ResponseEntity<List<PromptDTO>> getPrompts() {
        List<PromptDTO> prompts = promptService.getPrompts();
        return ResponseEntity.ok(prompts);
    }

    // 프롬프트 상태 토글 엔드포인트 추가
    @AdminAuth
    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<PromptDTO> togglePromptStatus(@PathVariable Long id) {
        try {
            PromptDTO updatedPrompt = promptService.togglePromptStatus(id);
            return ResponseEntity.ok(updatedPrompt);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 장르로 검색 - 활성화 상태에 따른 검색
    @AdminAuth
    @GetMapping("/search")
    public ResponseEntity<List<PromptDTO>> searchPromptsByGenre(
            @RequestParam String genre,
            @RequestParam(required = false, defaultValue = "false") boolean includeInactive
    ) {
        List<PromptDTO> prompts = includeInactive
                ? promptService.searchAllPromptsByGenre(genre)
                : promptService.searchPromptsByGenre(genre);
        return ResponseEntity.ok(prompts);
    }

    @AdminAuth
    @PostMapping
    public ResponseEntity<PromptDTO> createPrompt(@RequestBody PromptDTO promptDTO) {
        PromptDTO createdPrompt = promptService.createPrompt(promptDTO);
        return ResponseEntity.ok(createdPrompt);
    }

    @AdminAuth
    @GetMapping("/{id}")
    public ResponseEntity<PromptDTO> getPrompt(@PathVariable Long id) {
        try {
            PromptDTO prompt = promptService.getPrompt(id);
            return ResponseEntity.ok(prompt);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @AdminAuth
    @PutMapping("/{id}")
    public ResponseEntity<PromptDTO> updatePrompt(@PathVariable Long id, @RequestBody PromptDTO promptDTO) {
        try {
            PromptDTO updatedPrompt = promptService.updatePrompt(id, promptDTO);
            return ResponseEntity.ok(updatedPrompt);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}