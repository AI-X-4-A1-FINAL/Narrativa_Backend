package com.nova.narrativa.domain.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PageVisitDto {
    private String pagePath;
    private int visitCount;
}