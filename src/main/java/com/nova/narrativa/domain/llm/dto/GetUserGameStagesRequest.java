package com.nova.narrativa.domain.llm.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class GetUserGameStagesRequest {

    @NotNull
    private Long userId;

}

