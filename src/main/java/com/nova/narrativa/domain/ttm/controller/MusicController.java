package com.nova.narrativa.domain.ttm.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nova.narrativa.domain.ttm.service.MusicService;

@RestController
@RequestMapping("/api/music")
public class MusicController {

    private final MusicService musicService;

    public MusicController(MusicService musicService) {
        this.musicService = musicService;
    }

    @GetMapping("/random")
    public ResponseEntity<Map<String, String>> getRandomMusicByGenre(@RequestParam String genre) {
        try {
            System.out.println("Requested genre: " + genre); // 디버깅용 로그
            String presignedUrl = musicService.getRandomFileByGenre(genre);
            System.out.println("Generated URL: " + presignedUrl); // 디버깅용 로그

            // JSON 형식으로 URL 반환
            Map<String, String> response = Map.of("url", presignedUrl);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage()); // 에러 로그
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No music found for genre: " + genre));
        }
    }
}


