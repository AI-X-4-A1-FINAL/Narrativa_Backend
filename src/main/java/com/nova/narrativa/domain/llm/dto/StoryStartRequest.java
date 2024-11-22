package com.nova.narrativa.domain.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
public class StoryStartRequest {
    private String genre;
    private List<String> tags;

}
