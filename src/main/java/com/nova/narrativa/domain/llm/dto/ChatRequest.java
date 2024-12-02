package com.nova.narrativa.domain.llm.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChatRequest {

    @NotNull
    private long gameId;

    @NotEmpty(message = "장르 선택은 필수입니다.")
    private String genre;

    @NotEmpty(message = "현재 스테이지 값은 필수입니다.")
    private int currentStage;

    @NotEmpty(message = "초기 세계관은 필수입니다.")
    private String initialStory; // 초기 세계관

    @NotEmpty(message = "선택은 필수입니다.")
    private String userSelect;

    @NotEmpty(message = "이전 대화 내용은 필수입니다.")
    private String previousUserInput; // 이전 대화 내용

    @NotEmpty(message = "전체 대화 내용은 필수입니다.")
    private List<String> ConversationHistory; // 모든 대화 내용

}
