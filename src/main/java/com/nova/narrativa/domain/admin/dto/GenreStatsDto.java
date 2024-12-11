package com.nova.narrativa.domain.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GenreStatsDto {
    private String genre;
    private int playCount;
    private double averagePlayTime;
}