package com.nova.narrativa.domain.template.repository;

import com.nova.narrativa.domain.template.entity.Template;
import com.nova.narrativa.domain.template.enums.Genre;
import com.nova.narrativa.domain.template.enums.TemplateType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TemplateRepository extends JpaRepository<Template, Long> {
    Optional<Template> findByGenreAndType(Genre genre, TemplateType type);
}
