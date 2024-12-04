package com.nova.narrativa.domain.tti.dto;

public class ImageRequest {

    private String prompt;
    private String size;
    private int n;
    private String genre;  // genre 추가

    // Getters and Setters
    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    @Override
    public String toString() {
        return "ImageRequest{" +
                "prompt='" + prompt + '\'' +
                ", size='" + size + '\'' +
                ", n=" + n +
                ", genre='" + genre + '\'' +
                '}';
    }
}
