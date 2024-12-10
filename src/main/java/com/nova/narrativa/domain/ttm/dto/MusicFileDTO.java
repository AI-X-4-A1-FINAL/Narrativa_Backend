package com.nova.narrativa.domain.ttm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MusicFileDTO {
    private String name;
    private long size;
    private String contentType;
    private Instant lastModified;
    private String presignedUrl;
    private String genre;
}

