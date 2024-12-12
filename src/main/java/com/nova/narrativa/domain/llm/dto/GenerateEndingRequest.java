package com.nova.narrativa.domain.llm.dto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class GenerateEndingRequest {
    @NotNull
    private Long gameId;

    @NotNull
    private String genre;

    @NotNull
    private String userChoice;
}
