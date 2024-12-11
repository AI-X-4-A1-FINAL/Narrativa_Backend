package com.nova.narrativa.domain.user.controller;

import com.nova.narrativa.domain.user.dto.SignUp;
import com.nova.narrativa.domain.user.dto.UserProfileInfo;
import com.nova.narrativa.domain.user.entity.User;
import com.nova.narrativa.domain.user.service.SignUpService;
import com.nova.narrativa.domain.user.util.RequestParseUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;

@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
@RestController
public class SignController {

    private final SignUpService signUpService;

    // 회원가입
    @PostMapping("/users/sign-up")
    public ResponseEntity<String> signUp(@RequestBody SignUp signUp) {
        log.info("Sign up: {}", signUp);

        try {
            // 회원가입 처리
            signUpService.register(signUp);
            return ResponseEntity.status(201).body("회원 가입 성공하셨습니다.");
        } catch (ResponseStatusException e) {
            // 409 에러 시 /home으로 리다이렉트 프론트에 요청
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                return ResponseEntity.status(409).body("회원 가입이 이미되어있습니다.");
            }
            // 그 외의 오류는 그대로 반환
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    // 회원탈퇴
    @PutMapping("/users/deactivate")
    public ResponseEntity<String> deactivate(@RequestHeader("Authorization") String authorizationHeader,
                                             HttpServletRequest request) {
        Long seq = RequestParseUtil.getSeq(request);

        log.info("Deactivate user: {}", seq);

        // TODO: 회원의 경우 본인 userId인 경우만 탈퇴 가능, 어드민의 경우 상관없이 가능

        String loggedInUserResult = signUpService.isLoggedInUser(seq);
        if (loggedInUserResult == null) {
            return ResponseEntity.status(403).body("Unauthorized: 로그인한 유저의 ID가 아닙니다.");
        } else {
            return ResponseEntity.ok(String.valueOf(loggedInUserResult) + " 님 회원탈퇴 성공하였습니다.");
        }
    }

    //    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_VIP')")
//    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @GetMapping("/users")   // 회원 정보 전체 조회
    public ResponseEntity<UserProfileInfo> getUser(@RequestHeader("Authorization") String authorizationHeader,
                                                   HttpServletRequest request) {
        log.info("Get user: {}", authorizationHeader);

        Long seq = RequestParseUtil.getSeq(request);

        Optional<User> userInfo = signUpService.getUserProfileInfo(seq);
        // 회원 정보가 존재 (회원 타입 상태 상관없이 조회처리)
        if (userInfo.isPresent()) {
            UserProfileInfo userProfileInfo = UserProfileInfo.builder()
                    .nickname(userInfo.get().getUsername())
                    .profile_url(userInfo.get().getProfile_url())
                    .status(String.valueOf(userInfo.get().getStatus()))
                    .build();
            return new ResponseEntity<>(userProfileInfo, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // 회원 정보 상태 조회
    @GetMapping("/users/status")
    public ResponseEntity<UserProfileInfo> getUserStatus(@RequestHeader("Authorization") String authorizationHeader,
                                                         HttpServletRequest request) {
        log.info("getUserStatus authorizationHeader: {}", authorizationHeader);

        Long seq = RequestParseUtil.getSeq(request);

        Optional<User> userInfo = signUpService.getUserProfileInfo(seq);
        // 회원 정보가 존재 (회원 타입 상태 상관없이 조회처리)
        if (userInfo.isPresent()) {
            UserProfileInfo userProfileInfo = UserProfileInfo.builder()
                    .status(String.valueOf(userInfo.get().getStatus()))
                    .build();
            return new ResponseEntity<>(userProfileInfo, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // 회원정보 업데이트
    @PutMapping("/users")
    public ResponseEntity<Object> updateUser(@RequestHeader("Authorization") String authorizationHeader,
                                             HttpServletRequest request,
                                             @RequestBody UserProfileInfo userProfileInfo) {
        log.info("userProfileInfo: {}", userProfileInfo);

        Long seq = RequestParseUtil.getSeq(request);

        return signUpService.updateUser(seq, userProfileInfo);
    }

    // 유저ID로 조회한 유저가 ACTIVE 상태인지 조회
    @GetMapping("/checkUserStatus")
    public ResponseEntity<String> checkUserStatus(@RequestHeader("Authorization") String authorizationHeader,
                                                  HttpServletRequest request) {
        Long seq = RequestParseUtil.getSeq(request);
        try {
            boolean isActive = signUpService.isUserActive(seq);
            return new ResponseEntity<>(isActive ? HttpStatus.OK : HttpStatus.FORBIDDEN);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}