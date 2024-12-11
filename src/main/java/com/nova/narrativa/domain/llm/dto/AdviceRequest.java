package com.nova.narrativa.domain.llm.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class AdviceRequest {
    @NotNull(message = "게임 ID는 필수입니다.")
    private Long gameId;
}
