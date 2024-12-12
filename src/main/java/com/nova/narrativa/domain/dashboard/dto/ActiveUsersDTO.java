package com.nova.narrativa.domain.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class ActiveUsersDTO {
    private List<DailyStats> dauStats;
    private List<DailyStats> mauStats;

    @Getter
    @Builder
    public static class DailyStats {
        private LocalDate date;
        private Long count;
    }
}