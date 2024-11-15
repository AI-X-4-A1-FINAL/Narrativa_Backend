package com.nova.narrativa.domain.user.controller;

import com.nova.narrativa.domain.user.service.GithubService;
import com.nova.narrativa.domain.user.service.GoogleService;
import com.nova.narrativa.domain.user.service.KakaoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.Enumeration;

@RequestMapping("/login")
@Slf4j
@RequiredArgsConstructor
@RestController
public class SocialLoginController {

    private final KakaoService kakaoService;
    private final GoogleService googleService;
    private final GithubService githubService;

    @GetMapping("/kakao")
    public ModelAndView kakaoLogin(@RequestParam String code) {
        log.info("code = {}", code);
        try {
            kakaoService.kakaoLogin(code);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new ModelAndView("redirect:http://localhost:3000/home");
    }

    @GetMapping("/google")
    public ModelAndView googleLogin(@RequestParam String code) {
        log.info("code = {}", code);
        try {
            googleService.googleLogin(code);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new ModelAndView("redirect:http://localhost:3000/home");
    }

    @GetMapping("/github")
    public ModelAndView githubLogin(@RequestParam String code) {
        log.info("code = {}", code);
        try {
            githubService.githubLogin(code);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new ModelAndView("redirect:http://localhost:3000/home");
    }

    // 로그아웃시 로그인 페이지로 이동
    @GetMapping("/logout")
    public String kakaoLogout(HttpServletRequest request) {
        HttpSession session = request.getSession();

        // 세션에 저장된 모든 속성의 이름을 가져옵니다.
        Enumeration<String> attributeNames = session.getAttributeNames();

        // 각 속성 이름을 출력합니다.
        while (attributeNames.hasMoreElements()) {
            String key = attributeNames.nextElement();
            System.out.println("Session key: " + key);
        }
        return "logout";
    }
}
