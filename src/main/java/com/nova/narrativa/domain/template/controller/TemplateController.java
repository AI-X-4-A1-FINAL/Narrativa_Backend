package com.nova.narrativa.domain.template.controller;

import com.nova.narrativa.domain.template.dto.TemplateDTO;
import com.nova.narrativa.domain.template.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {

    private final TemplateService templateService;

    @Autowired
    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    public ResponseEntity<List<TemplateDTO>> getAllTemplates() {
        List<TemplateDTO> templates = templateService.getAllTemplates();
        return ResponseEntity.ok(templates);
    }


    @GetMapping("/{genre}/{type}")
    public ResponseEntity<TemplateDTO> getTemplate(
            @PathVariable String genre,
            @PathVariable String type) {
        TemplateDTO template = templateService.getTemplate(genre, type); // 수정된 로직 호출
        return ResponseEntity.ok(template);
    }


    // 등록 (Create)
    @PostMapping
    public ResponseEntity<TemplateDTO> createTemplate(@RequestBody TemplateDTO templateDTO) {
        TemplateDTO createdTemplate = templateService.createTemplate(templateDTO);
        return ResponseEntity.ok(createdTemplate);
    }

    // 수정 (Update)
    @PutMapping("/{id}")
    public ResponseEntity<TemplateDTO> updateTemplate(
            @PathVariable Long id,
            @RequestBody TemplateDTO templateDTO) {
        TemplateDTO updatedTemplate = templateService.updateTemplate(id, templateDTO);
        return ResponseEntity.ok(updatedTemplate);
    }

    // 삭제 (Delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }
}
