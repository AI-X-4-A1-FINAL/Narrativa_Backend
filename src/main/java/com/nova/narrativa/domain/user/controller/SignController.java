package com.nova.narrativa.domain.user.controller;

import com.nova.narrativa.domain.user.dto.SignUp;

import com.nova.narrativa.domain.user.entity.User;
import com.nova.narrativa.domain.user.service.SignUpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RequestMapping("/api/users")
@Slf4j
@RequiredArgsConstructor
@RestController
public class SignController {

    private final SignUpService signUpService;

    @PostMapping("/sign-up")
    public ResponseEntity<String> signUp(@RequestBody SignUp signUp) {
        log.info("Sign up: {}", signUp);
        try {
            // 회원가입 처리
            signUpService.register(signUp);
            return ResponseEntity.status(201).body("User registered successfully");
        } catch (ResponseStatusException e) {
            // 이메일 중복 시 400 에러 처리
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    @PutMapping("/{userId}/deactivate")
    public ResponseEntity<String> deactivate(@PathVariable Long userId) {
        log.info("Deactivate user: {}", userId);

        // TODO: 회원의 경우 본인 userId인 경우만 탈퇴 가능, 어드민의 경우 상관없이 가능

        String loggedInUserResult = signUpService.isLoggedInUser(userId);
        if (loggedInUserResult == null) {
            return ResponseEntity.status(403).body("Unauthorized: 로그인한 유저의 ID가 아닙니다.");
        } else {
            return ResponseEntity.ok(String.valueOf(loggedInUserResult) + " 님 회원탈퇴 성공하였습니다.");
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<Object> updateUser(@PathVariable Long userId, @RequestBody User UpdateUser) {
        log.info("Update user: {}", UpdateUser);

        return signUpService.updateUser(userId, UpdateUser);
    }
}
