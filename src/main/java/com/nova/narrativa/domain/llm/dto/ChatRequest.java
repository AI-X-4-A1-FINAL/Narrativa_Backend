package com.nova.narrativa.domain.llm.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ChatRequest {

    @NotNull(message = "게임 ID는 필수입니다.")
    private Long gameId;

    @NotEmpty(message = "장르는 필수입니다.")
    private String genre;

    @NotEmpty(message = "사용자 선택은 필수입니다.")
    private String userChoice;
}