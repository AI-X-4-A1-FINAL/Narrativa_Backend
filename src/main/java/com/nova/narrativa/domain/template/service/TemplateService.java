package com.nova.narrativa.domain.template.service;

import com.nova.narrativa.domain.template.dto.TemplateDTO;
import com.nova.narrativa.domain.template.entity.Template;
import com.nova.narrativa.domain.template.repository.TemplateRepository;
import com.nova.narrativa.domain.template.enums.Genre; // 올바른 import
import com.nova.narrativa.domain.template.enums.TemplateType; // 올바른 import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TemplateService {

    private final TemplateRepository templateRepository;

    @Autowired
    public TemplateService(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    public TemplateDTO getTemplate(String genre, String type) {
        Genre normalizedGenre = Genre.valueOf(genre.toUpperCase()); // 대소문자 무시
        TemplateType normalizedType = TemplateType.valueOf(type.toUpperCase()); // 대소문자 무시

        return templateRepository.findByGenreAndType(normalizedGenre, normalizedType)
                .map(TemplateDTO::fromEntity)
                .orElseThrow(() -> new RuntimeException("Template not found for the given genre and type."));
    }



    public TemplateDTO getTemplateById(Long id) {
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No template found with id: " + id));
        return TemplateDTO.fromEntity(template);
    }

    public List<TemplateDTO> getAllTemplates() {
        return templateRepository.findAll().stream()
                .map(TemplateDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public TemplateDTO createTemplate(TemplateDTO templateDTO) {
        Template template = templateDTO.toEntity();
        Template savedTemplate = templateRepository.save(template);
        return TemplateDTO.fromEntity(savedTemplate);
    }

    @Transactional
    public TemplateDTO updateTemplate(Long id, TemplateDTO templateDTO) {
        Optional<Template> optionalTemplate = templateRepository.findById(id);
        if (optionalTemplate.isEmpty()) {
            throw new RuntimeException("Template not found with ID: " + id);
        }
        Template template = optionalTemplate.get();
        template.setGenre(Genre.valueOf(templateDTO.getGenre().toUpperCase())); // 대소문자 무시
        template.setType(TemplateType.valueOf(templateDTO.getType().toUpperCase())); // 대소문자 무시
        template.setContent(templateDTO.getContent());

        Template updatedTemplate = templateRepository.save(template);
        return TemplateDTO.fromEntity(updatedTemplate);
    }

    @Transactional
    public void deleteTemplate(Long id) {
        if (!templateRepository.existsById(id)) {
            throw new RuntimeException("Template not found with ID: " + id);
        }
        templateRepository.deleteById(id);
    }
}
