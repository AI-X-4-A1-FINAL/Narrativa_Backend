package com.nova.narrativa.domain.user.controller;

import com.nova.narrativa.domain.user.dto.SignUp;
import com.nova.narrativa.domain.user.dto.SocialLoginResult;
import com.nova.narrativa.domain.user.entity.User;
import com.nova.narrativa.domain.user.service.SignUpService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Optional;

@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
@RestController
public class SignController {

    private final SignUpService signUpService;

    // 회원가입
    @PostMapping("/users/sign-up")
    public ModelAndView signUp(@RequestBody SignUp signUp) {
        log.info("Sign up: {}", signUp);

        try {
            // 회원가입 처리
            signUpService.register(signUp);
            return ResponseEntity.status(201).body("회원 가입 성공하셨습니다.");
        } catch (ResponseStatusException e) {
            // 409 에러 시 /home으로 리다이렉트 프론트에 요청
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                return new ResponseEntity<String>("/home", HttpStatus.OK);  // 리다이렉트 경로 설정
            }
            // 그 외의 오류는 그대로 반환
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    // 회원탈퇴
    @PutMapping("/users/{userId}/deactivate")
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

    // 회원정보 업데이트
    @PutMapping("/users/{userId}")
    public ResponseEntity<Object> updateUser(@PathVariable Long userId, @RequestBody User UpdateUser) {
        log.info("Update user: {}", UpdateUser);

        return signUpService.updateUser(userId, UpdateUser);
    }

    // 소셜 로그인 여부 확인
    @GetMapping("/get-social-login-result")
    public ResponseEntity<SocialLoginResult> getSocialLoginResult(HttpSession session) {
        SocialLoginResult result = (SocialLoginResult) session.getAttribute("SocialLoginResult");

        if (result != null) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
