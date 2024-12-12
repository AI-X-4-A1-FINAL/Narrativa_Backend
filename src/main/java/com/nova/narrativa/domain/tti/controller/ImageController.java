package com.nova.narrativa.domain.tti.controller;

import com.nova.narrativa.domain.tti.dto.ImageRequest;
import com.nova.narrativa.domain.tti.dto.ImageResponse;
import com.nova.narrativa.domain.tti.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Base64;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageService imageService;

    @Autowired
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    // Endpoint to get a random image presigned URL as a JSON response
    @GetMapping("/random")
    public ResponseEntity<?> getRandomImage() {
        String imageUrl = imageService.getRandomImage();
        return ResponseEntity.ok(new ImageResponse(imageUrl));
    }

    // Endpoint to get a generated image from FastAPI
    @PostMapping("/generate-image")
    public ResponseEntity<String> generateImage(@RequestBody ImageRequest request) {
        try {
            // 이미지 생성 요청을 서비스로 전달하고, 생성된 이미지 URL을 반환
            ResponseEntity<byte[]> imageUrl = imageService.generateImage(
                    request.getGameId(),
                    request.getStageNumber(),
                    request.getPrompt(),
                    request.getSize(),
                    request.getN(),
                    request.getGenre()
            );

            // byte[] 데이터를 Base64로 변환
            byte[] imageBytes = imageUrl.getBody();
            if (imageBytes == null) {
                return ResponseEntity.status(500).body("{\"error\":\"Failed to generate image\"}");
            }
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            // JSON으로 Base64 데이터를 반환
            return ResponseEntity.ok("{\"image\":\"data:image/jpg;base64," + base64Image + "\"}");

        } catch (Exception e) {
            // 예외가 발생하면 500 오류와 함께 에러 메시지를 반환
            return ResponseEntity.status(500).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }


}