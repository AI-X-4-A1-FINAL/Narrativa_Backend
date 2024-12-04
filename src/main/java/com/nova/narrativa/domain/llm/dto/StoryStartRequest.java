package com.nova.narrativa.domain.llm.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
public class StoryStartRequest {

    @NotEmpty(message = "장르 선택은 필수입니다.")
    private String genre;

    private List<String> tags;

    @NotNull(message = "유저 ID는 필수입니다.")
    private Long userId;

}
