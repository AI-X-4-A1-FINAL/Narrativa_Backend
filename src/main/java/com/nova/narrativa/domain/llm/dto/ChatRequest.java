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
    private String initialStory;
    private String userInput;
    private String previousUserInput; // 이전 대화 내용 추가
    private double survivalProbability; // 생존 확률 추가


}
