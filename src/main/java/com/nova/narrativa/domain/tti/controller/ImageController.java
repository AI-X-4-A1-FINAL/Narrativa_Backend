package com.nova.narrativa.domain.tti.controller;

import com.nova.narrativa.domain.tti.dto.ImageRequest;
import com.nova.narrativa.domain.tti.dto.ImageResponse;
import com.nova.narrativa.domain.tti.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<byte[]> generateImage(@RequestBody ImageRequest request) {
        // 받은 요청을 확인
        System.out.println("Received Request: " + request);  // 디버깅용 출력 (보안에 주의해야 함)

        try {
            // 이미지 생성 요청을 서비스로 전달하고, 생성된 이미지 URL을 반환
            ResponseEntity<String> imageUrl = imageService.generateImage(
                    request.getGameId(),
                    request.getStageNumber(),
                    request.getPrompt(),
                    request.getSize(),
                    request.getN(),
                    request.getGenre()
            );

            // 생성된 이미지 URL을 응답으로 반환
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)  // JSON 형태로 반환
                    .body(imageUrl.getBody().getBytes());

        } catch (Exception e) {
            // 예외가 발생하면 500 오류와 함께 에러 메시지를 반환
            return ResponseEntity.status(500).body(("Error: " + e.getMessage()).getBytes());
        }
    }


}