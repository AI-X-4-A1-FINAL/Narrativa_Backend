package com.nova.narrativa.domain.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AnalyticsSummaryDto {
    private int totalActiveUsers;
    private int totalPageViews;
    private List<GenreStatsDto> genreStats;
    private List<PageVisitDto> pageVisits;
    private List<HourlyStatsDto> hourlyStats;
    private LocalDateTime timestamp;
}