package com.nova.narrativa.domain.llm.dto;

public class StoryRequest {
    private String genre;
    private int affection;
    private int cut;
    private String userInput;

    // Getters and Setters
    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getAffection() {
        return affection;
    }

    public void setAffection(int affection) {
        this.affection = affection;
    }

    public int getCut() {
        return cut;
    }

    public void setCut(int cut) {
        this.cut = cut;
    }

    public String getUserInput() {
        return userInput;
    }

    public void setUserInput(String userInput) {
        this.userInput = userInput;
    }

    @Override
    public String toString() {
        return "StoryRequest{" +
                "genre='" + genre + '\'' +
                ", affection=" + affection +
                ", cut=" + cut +
                ", userInput='" + userInput + '\'' +
                '}';
    }
}
