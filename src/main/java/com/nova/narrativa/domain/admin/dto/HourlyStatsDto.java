package com.nova.narrativa.domain.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HourlyStatsDto {
    private String hour;
    private int activeUsers;
}