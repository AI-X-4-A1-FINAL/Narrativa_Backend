package com.nova.narrativa.domain.llm.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChatRequest {

    private int currentStage;
    private String genre;
    private String initialStory;
    private String previousUserInput; // 이전 대화 내용
    private String userInput;
//    private double survivalProbability; // 생존 확률 추가

}

