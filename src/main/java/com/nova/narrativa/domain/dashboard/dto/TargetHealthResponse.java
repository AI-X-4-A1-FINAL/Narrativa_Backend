package com.nova.narrativa.domain.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TargetHealthResponse {
    private String targetId;
    private String state;
    private String reason;
    private String description;
    private String targetGroupName;
}