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
    private String previousUserInput; // 이전 대화 내용
    private List<String> ConversationHistory; // 모든 대화 내용

}

