package com.nova.narrativa.domain.user.controller;

import com.nova.narrativa.domain.user.dto.SignUp;
import com.nova.narrativa.domain.user.service.SignUpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RequestMapping("/api/users")
@RequiredArgsConstructor
@RestController
public class SignController {

    private final SignUpService signUpService;

    @PostMapping("/sign-up")
    public ResponseEntity<String> signUp(@RequestBody SignUp signUp) {
        System.out.println("signUp = " + signUp);

        try {
            // 회원가입 처리
            signUpService.register(signUp);
            return ResponseEntity.status(201).body("User registered successfully");
        } catch (ResponseStatusException e) {
            // 이메일 중복 시 400 에러 처리
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }
}
