package com.nova.narrativa.domain.llm.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChatRequest {

    private String genre;
    private int currentStage;
    private String userInput;
    private String initialStory;

}
