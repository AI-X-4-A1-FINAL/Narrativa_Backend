package com.nova.narrativa.domain.template.controller;

import com.nova.narrativa.domain.admin.util.AdminAuth;
import com.nova.narrativa.domain.template.dto.TemplateDTO;
import com.nova.narrativa.domain.template.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    // ML 서비스용 엔드포인트
    @GetMapping("/api/templates/{genre}/{type}")
    public ResponseEntity<TemplateDTO> getTemplateForML(
            @PathVariable String genre,
            @PathVariable String type) {
        TemplateDTO template = templateService.getTemplate(genre, type);
        return ResponseEntity.ok(template);
    }

    // 관리자용 엔드포인트들
    @AdminAuth
    @GetMapping("/api/admin/templates")
    public ResponseEntity<List<TemplateDTO>> getAllTemplates() {
        List<TemplateDTO> templates = templateService.getAllTemplates();
        return ResponseEntity.ok(templates);
    }

    @AdminAuth
    @PostMapping("/api/admin/templates")
    public ResponseEntity<TemplateDTO> createTemplate(@RequestBody TemplateDTO templateDTO) {
        TemplateDTO createdTemplate = templateService.createTemplate(templateDTO);
        return ResponseEntity.ok(createdTemplate);
    }

    @AdminAuth
    @PutMapping("/api/admin/templates/{id}")
    public ResponseEntity<TemplateDTO> updateTemplate(
            @PathVariable Long id,
            @RequestBody TemplateDTO templateDTO) {
        TemplateDTO updatedTemplate = templateService.updateTemplate(id, templateDTO);
        return ResponseEntity.ok(updatedTemplate);
    }

    @AdminAuth
    @DeleteMapping("/api/admin/templates/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }
}
