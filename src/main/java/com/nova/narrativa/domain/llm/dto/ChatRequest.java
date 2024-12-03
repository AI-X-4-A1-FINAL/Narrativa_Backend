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

    private String previousUserInput;

    private List<String> ConversationHistory;

    private String imageUrl;

}
