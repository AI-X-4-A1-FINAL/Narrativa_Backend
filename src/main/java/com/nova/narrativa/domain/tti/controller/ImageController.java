package com.nova.narrativa.domain.tti.controller;

import com.nova.narrativa.domain.tti.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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


    // Endpoint to get a generated image from FastAPI
    @PostMapping("/generate-image")
    public ResponseEntity<byte[]> generateImage(@RequestParam String prompt,
                                                @RequestParam(required = false, defaultValue = "1024x1024") String size,
                                                @RequestParam(required = false, defaultValue = "1") int n) {
        // FastAPI로부터 이미지를 받아옴
        byte[] imageBytes = imageService.generateImage(prompt, size, n);

        // 이미지를 응답으로 반환 (이미지의 MIME 타입 설정)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.IMAGE_JPEG);

        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
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
