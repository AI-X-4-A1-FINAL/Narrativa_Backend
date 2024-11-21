package com.nova.narrativa.domain.llm.controller;

public class StoryResponse {
    private String story;

    public StoryResponse(String story) {
        this.story = story;
    }

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }
}
