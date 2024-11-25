package com.nova.narrativa.domain.user.controller;

import com.nova.narrativa.domain.user.service.S3ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequestMapping("/api/s3")
@Slf4j
@RequiredArgsConstructor
@RestController
public class S3ImgController {

    private final S3ImageService s3ImageService;

    // 이미지 업로드
    @PostMapping("/images/upload")
    public ResponseEntity<?> s3Upload(@RequestPart(value = "image", required = false) MultipartFile image){
        String profileImage = s3ImageService.upload(image);
        return ResponseEntity.ok(profileImage);
    }

    // S3 버킷에 있는 이미지 삭제
    @GetMapping("/images/{addr}")
    public ResponseEntity<?> s3delete(@PathVariable String addr){
        s3ImageService.deleteImageFromS3(addr);
        return ResponseEntity.ok(null);
    }

//    @PostMapping("/upload-test")
//    public String upload(MultipartFile image) throws IOException {
//        String imageUrl = s3ImageService.testUpload(image);
//        return imageUrl;
//    }
}
