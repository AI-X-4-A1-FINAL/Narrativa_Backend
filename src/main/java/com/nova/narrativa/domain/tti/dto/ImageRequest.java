package com.nova.narrativa.domain.tti.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ImageRequest {

    private Long gameId;         // 게임 ID
    private int stageNumber;     // 스테이지 번호
    private String prompt;       // 프롬프트
    private String size;         // 이미지 크기
    private int n;               // 생성할 이미지 개수
    private String genre;        // 게임 장르

    @Override
    public String toString() {
        return "ImageRequest{" +
                "gameId=" + gameId +
                ", stageNumber=" + stageNumber +
                ", prompt='" + prompt + '\'' +
                ", size='" + size + '\'' +
                ", n=" + n +
                ", genre='" + genre + '\'' +
                '}';
    }
}
