package com.nova.narrativa.domain.llm.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class AdviceResponse {
    private String npcMessage;
    private String gameId;
}