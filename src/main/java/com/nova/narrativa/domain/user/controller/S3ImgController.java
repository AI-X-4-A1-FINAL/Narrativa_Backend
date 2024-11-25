package com.nova.narrativa.domain.user.controller;

import com.nova.narrativa.domain.user.service.S3ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/api/s3")
@Slf4j
@RequiredArgsConstructor
@RestController
public class S3ImgController {

    private final S3ImageService s3ImageService;

    // 이미지 업로드
    @PostMapping("/images/upload")
    public ResponseEntity<?> s3Upload(@RequestPart(value = "image", required = false) MultipartFile image) {
        if (image == null || image.isEmpty()) {
            log.error("이미지 파일이 비어 있습니다.");
            return ResponseEntity.badRequest().body("이미지 파일이 비어 있습니다.");
        }

        try {
            String uploadedImageUrl = s3ImageService.upload(image);
            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", uploadedImageUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("이미지 업로드 중 오류 발생", e);
            return ResponseEntity.status(500).body("이미지 업로드 실패");
        }
    }

    // S3 버킷에 있는 이미지 삭제
    @DeleteMapping("/images")
    public ResponseEntity<?> s3Delete(@RequestParam(value = "imageUrl") String imageUrl) {
        try {
            s3ImageService.deleteImageFromS3(imageUrl);
            return ResponseEntity.ok("이미지 삭제 완료");
        } catch (Exception e) {
            log.error("이미지 삭제 중 오류 발생", e);
            return ResponseEntity.status(500).body("이미지 삭제 실패");
        }
    }
}
