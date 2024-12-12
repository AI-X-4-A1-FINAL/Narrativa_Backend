package com.nova.narrativa.domain.ttm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MusicUploadResponseDTO {
    private String filename;
    private String message;
    private boolean success;
}