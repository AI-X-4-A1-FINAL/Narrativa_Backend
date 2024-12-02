package com.nova.narrativa.domain.llm.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChatRequest {
    private String genre;
    private int currentStage;
    private String initialStory;
    private String userInput;
    private String previousUserInput;
    private List<String> ConversationHistory;
    private String imageUrl;
}

