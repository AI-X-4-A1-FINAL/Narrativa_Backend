package com.nova.narrativa.domain.tti.dto;

public class ImageResponse {

    private String imageUrl;

    // 기본 생성자
    public ImageResponse() {}

    // 생성자
    public ImageResponse(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // Getter 및 Setter
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
