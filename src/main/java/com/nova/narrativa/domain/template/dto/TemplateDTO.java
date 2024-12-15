package com.nova.narrativa.domain.template.dto;

import com.nova.narrativa.domain.template.entity.Template;
import com.nova.narrativa.domain.template.enums.Genre; // 올바른 import
import com.nova.narrativa.domain.template.enums.TemplateType; // 올바른 import
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TemplateDTO {
    private Long id;
    private String genre;
    private String type;
    private String content;

    public static TemplateDTO fromEntity(Template template) {
        TemplateDTO dto = new TemplateDTO();
        dto.setId(template.getId());
        dto.setGenre(template.getGenre().name());
        dto.setType(template.getType().name());
        dto.setContent(template.getContent());
        return dto;
    }

    public Template toEntity() {
        Template template = new Template();
        template.setId(this.id);
        template.setGenre(Genre.valueOf(this.genre.toUpperCase())); // 대소문자 무시
        template.setType(TemplateType.valueOf(this.type.toUpperCase())); // 대소문자 무시
        template.setContent(this.content);
        return template;
    }

}
