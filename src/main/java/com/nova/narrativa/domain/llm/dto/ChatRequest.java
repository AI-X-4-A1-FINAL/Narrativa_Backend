package com.nova.narrativa.domain.llm.dto;

import jakarta.validation.constraints.Min;
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

    private Long gameId;

    @NotEmpty(message = "장르 선택은 필수입니다.")
    private String genre;

    @NotNull(message = "currentStage는 필수 값입니다.")
    @Min(value = 0, message = "currentStage는 0 이상의 값이어야 합니다.")
    private Integer currentStage;

    @NotEmpty(message = "초기 세계관은 필수입니다.")
    private String initialStory; // 초기 세계관

    @NotEmpty(message = "선택은 필수입니다.")
    private String userSelect;

    private String previousUserInput;

    private List<String> ConversationHistory;

    private String imageUrl;

}
