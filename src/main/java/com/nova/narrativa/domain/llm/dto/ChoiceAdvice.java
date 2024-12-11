package com.nova.narrativa.domain.llm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ChoiceAdvice {
    private String advice;

    @JsonProperty("survival_rate")
    private int survivalRate;
}