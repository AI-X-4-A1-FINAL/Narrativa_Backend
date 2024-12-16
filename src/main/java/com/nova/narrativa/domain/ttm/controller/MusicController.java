package com.nova.narrativa.domain.ttm.controller;

import java.util.List;
import java.util.Map;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.nova.narrativa.domain.admin.service.AuthService;
import com.nova.narrativa.domain.ttm.dto.MusicFileDTO;
import com.nova.narrativa.domain.ttm.entity.MusicGeneration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nova.narrativa.domain.ttm.service.MusicService;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/music")
public class MusicController {

    private final MusicService musicService;
    private final AuthService authService;

    public MusicController(MusicService musicService, AuthService authService) {
        this.musicService = musicService;
        this.authService = authService;
    }

    @GetMapping("/random")
    public ResponseEntity<Map<String, String>> getRandomMusicByGenre(@RequestParam String genre) {
        try {
            String presignedUrl = musicService.getRandomFileByGenre(genre);

            // JSON 형식으로 URL 반환
            Map<String, String> response = Map.of("url", presignedUrl);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage()); // 에러 로그
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No music found for genre: " + genre));
        }
    }

    @GetMapping("/files")
    public ResponseEntity<?> getAllMusicFiles(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String idToken = extractToken(authorizationHeader);
            FirebaseToken decodedToken = authService.verifyToken(idToken);

            List<MusicFileDTO> files = musicService.getAllMusicFiles();
            return ResponseEntity.ok(files);
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("유효하지 않은 Firebase 토큰입니다.");
        }
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadMusic(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam("file") MultipartFile file,
            @RequestParam("genre") String genre) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("파일이 비어있습니다.");
        }

        if (file.getSize() > 52428800) { // 50MB
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body("파일 크기는 50MB를 초과할 수 없습니다.");
        }

        try {
            String idToken = extractToken(authorizationHeader);
            FirebaseToken decodedToken = authService.verifyToken(idToken);

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("audio/")) {
                return ResponseEntity.badRequest().body("오디오 파일만 업로드 가능합니다.");
            }

            MusicGeneration.Genre genreEnum = MusicGeneration.Genre.valueOf(genre.toUpperCase());
            MusicFileDTO uploadedFile = musicService.uploadMusic(file, genreEnum);

            return ResponseEntity.ok(uploadedFile);

        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("유효하지 않은 Firebase 토큰입니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("잘못된 장르입니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("파일 업로드 중 오류가 발생했습니다.");
        }
    }

    @DeleteMapping("/delete/{filename}")
    public ResponseEntity<?> deleteMusic(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable String filename) {
        try {
            String idToken = extractToken(authorizationHeader);
            FirebaseToken decodedToken = authService.verifyToken(idToken);

            musicService.deleteMusic(filename);
            return ResponseEntity.noContent().build();
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("유효하지 않은 Firebase 토큰입니다.");
        }
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        throw new IllegalArgumentException("Authorization 헤더가 유효하지 않습니다.");
    }
}