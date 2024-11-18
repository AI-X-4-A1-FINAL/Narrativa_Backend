package com.nova.narrativa.domain.user.controller;

import com.nova.narrativa.domain.user.dto.SocialLoginResult;
import com.nova.narrativa.domain.user.entity.User;
import com.nova.narrativa.domain.user.service.GithubService;
import com.nova.narrativa.domain.user.service.GoogleService;
import com.nova.narrativa.domain.user.service.KakaoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.util.Enumeration;

@RequestMapping("/login")
@Slf4j
@RequiredArgsConstructor
@RestController
public class SocialLoginController {

    private final KakaoService kakaoService;
    private final GoogleService googleService;
    private final GithubService githubService;

    private static String redirectUrl = "http://localhost:3000/sign-up";

    @GetMapping("/kakao")
    public ModelAndView kakaoLogin(@RequestParam String code) {
        log.info("code = {}", code);
        SocialLoginResult socialLoginResult;
        String redirectWithParams = "";
        try {
            socialLoginResult = kakaoService.kakaoLogin(code);
            redirectWithParams = redirectUrl + "?username=" + URLEncoder.encode(socialLoginResult.getNickname(), "UTF-8")
                    + "&profile_url=" + socialLoginResult.getProfile_image_url();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new ModelAndView("redirect:" + redirectWithParams);
    }

    @GetMapping("/google")
    public ModelAndView googleLogin(@RequestParam String code) {
        log.info("code = {}", code);
        SocialLoginResult socialLoginResult;
        String redirectWithParams = "";
        try {
            socialLoginResult = googleService.googleLogin(code);
            redirectWithParams = redirectUrl + "?username=" + URLEncoder.encode(socialLoginResult.getNickname(), "UTF-8")
                    + "&profile_url=" + socialLoginResult.getProfile_image_url();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new ModelAndView("redirect:" + redirectWithParams);
    }

    @GetMapping("/github")
    public ModelAndView githubLogin(@RequestParam String code) {
        log.info("code = {}", code);
        SocialLoginResult socialLoginResult;
        String redirectWithParams = "";
        try {
            socialLoginResult = githubService.githubLogin(code);
            redirectWithParams = redirectUrl + "?username=" + URLEncoder.encode(socialLoginResult.getNickname(), "UTF-8")
                    + "&profile_url=" + socialLoginResult.getProfile_image_url();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new ModelAndView("redirect:" + redirectWithParams);
    }

    // 로그인 상태 확인
    @GetMapping("/check-login")
    public ResponseEntity<?> checkLoginStatus(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(401).body("Not logged in");
        }
    }


    // 로그아웃 처리
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        log.info("session: {}", session.getAttribute("socialLoginResult"));
        session.invalidate();  // 세션 무효화
        return ResponseEntity.ok().body("로그아웃 성공");
    }
}
