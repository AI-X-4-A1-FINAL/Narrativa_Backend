package com.nova.narrativa.domain.llm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Data
@Getter
@Setter
public class NPCChatResponse {
    private Map<String, ChoiceAdvice> response;

    @JsonProperty("game_id")
    private String gameId;

    @JsonProperty("additional_comment")
    private String additionalComment;


    @Override
    public String toString() {
        return "NPCChatResponse{" +
                "response=" + response +
                ", gameId='" + gameId + '\'' +
                ", additionalComment='" + additionalComment + '\'' +
                '}';
    }
}