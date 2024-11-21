package com.nova.narrativa.domain.llm.service;

import com.nova.narrativa.domain.llm.controller.StoryRequest;

public interface StoryService {
    String generateStory(StoryRequest request);
}
