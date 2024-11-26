package com.nova.narrativa.domain.tti.dto;

public class ImageRequest {

    private String prompt;
    private String size;
    private int n;

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

    // toString()을 오버라이드하면 요청 데이터 확인에 유용할 수 있음
    @Override
    public String toString() {
        return "ImageRequest{" +
                "prompt='" + prompt + '\'' +
                ", size='" + size + '\'' +
                ", n=" + n +
                '}';
    }
}
