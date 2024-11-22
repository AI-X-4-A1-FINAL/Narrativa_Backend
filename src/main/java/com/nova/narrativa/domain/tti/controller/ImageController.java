package com.nova.narrativa.domain.tti.controller;

import com.nova.narrativa.domain.tti.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    // Endpoint to get a random image presigned URL as a JSON response
    @GetMapping("/random")
    public ResponseEntity<?> getRandomImage() {
        String imageUrl = imageService.getRandomImage();
        return ResponseEntity.ok(new ImageResponse(imageUrl));
    }

    // ImageResponse class to wrap the image URL in JSON format
    public static class ImageResponse {
        private final String imageUrl;

        public ImageResponse(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getImageUrl() {
            return imageUrl;
        }
    }
}
