package com.nova.narrativa.domain.prompt.dto;

import com.nova.narrativa.domain.prompt.entity.Prompt;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PromptDTO {
    private Long id;
    private String title;
    private String genre;
    private String content;
    private boolean isActive;

    public static PromptDTO fromEntity(Prompt prompt) {
        PromptDTO dto = new PromptDTO();
        dto.setId(prompt.getId());
        dto.setGenre(prompt.getGenre());
        dto.setTitle(prompt.getTitle());
        dto.setContent(prompt.getContent());
        dto.setActive(prompt.isActive());
        return dto;
    }
}