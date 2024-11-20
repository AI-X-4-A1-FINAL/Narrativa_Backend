package com.nova.narrativa.domain.user.controller;

import com.nova.narrativa.domain.user.dto.SocialLoginResult;
import com.nova.narrativa.domain.user.dto.UserExistenceDto;
import com.nova.narrativa.domain.user.entity.User;
import com.nova.narrativa.domain.user.service.GithubService;
import com.nova.narrativa.domain.user.service.GoogleService;
import com.nova.narrativa.domain.user.service.KakaoService;
import com.nova.narrativa.domain.user.service.SignUpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.util.Enumeration;

@RequestMapping("/login")
@Slf4j
//@RequiredArgsConstructor
@RestController
public class SocialLoginController {

    private final KakaoService kakaoService;
    private final GoogleService googleService;
    private final GithubService githubService;
    private final SignUpService signUpService;
    private final String frontUrl;
    private final String frontSignupPart;
    private final String redirectUrl;

    public SocialLoginController(KakaoService kakaoService,
                                 GoogleService googleService,
                                 GithubService githubService,
                                 SignUpService signUpService,
                                 @Value("${front.url}") String frontUrl,
                                 @Value("${front.signup-part}") String frontSignupPart) {

        this.kakaoService = kakaoService;
        this.googleService = googleService;
        this.githubService = githubService;
        this.signUpService = signUpService;
        this.frontUrl = frontUrl;
        this.frontSignupPart = frontSignupPart;
        this.redirectUrl = frontUrl + frontSignupPart;
    }

    @GetMapping("/kakao")
    public ModelAndView kakaoLogin(@RequestParam String code) {
        log.info("code = {}", code);
        SocialLoginResult socialLoginResult;
        String redirectWithParams = "";

        try {
            socialLoginResult = kakaoService.kakaoLogin(code);
            UserExistenceDto userExistenceDto = UserExistenceDto.builder()
                    .userId(socialLoginResult.getId())
                    .loginType(User.LoginType.KAKAO)
                    .build();
            log.info("userExistenceDto = {}", userExistenceDto);

            // DB 조회 후, 해당 유저 존재시 /home으로 redirect
            if (signUpService.isUserExist(userExistenceDto)) {
                redirectWithParams = frontUrl + "/home";
            } else {
                redirectWithParams = redirectUrl + "?username=" + URLEncoder.encode(socialLoginResult.getNickname(), "UTF-8")
                        + "&profile_url=" + socialLoginResult.getProfile_image_url()
                        + "&id=" + socialLoginResult.getId()
                        + "&type=" + User.LoginType.KAKAO;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.info("redirectWithParams = {}", redirectWithParams);
        return new ModelAndView("redirect:" + redirectWithParams);

    }

    @GetMapping("/google")
    public ModelAndView googleLogin(@RequestParam String code) {
        log.info("code = {}", code);
        SocialLoginResult socialLoginResult;
        String redirectWithParams = "";
        try {
            socialLoginResult = googleService.googleLogin(code);
            log.info("socialLoginResult = {}", socialLoginResult);
            UserExistenceDto userExistenceDto = UserExistenceDto.builder()
                    .userId(socialLoginResult.getId())
                    .loginType(User.LoginType.GOOGLE)
                    .build();
            log.info("userExistenceDto = {}", userExistenceDto);

            // DB 조회 후, 해당 유저 존재시 /home으로 redirect
            if (signUpService.isUserExist(userExistenceDto)) {
                redirectWithParams = frontUrl + "/home";
            } else {
                redirectWithParams = redirectUrl + "?username=" + URLEncoder.encode(socialLoginResult.getNickname(), "UTF-8")
                        + "&profile_url=" + socialLoginResult.getProfile_image_url()
                        + "&id=" + socialLoginResult.getId()
                        + "&type=" + User.LoginType.GOOGLE;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        log.info("redirectWithParams = {}", redirectWithParams);
        return new ModelAndView("redirect:" + redirectWithParams);
    }

    @GetMapping("/github")
    public ModelAndView githubLogin(@RequestParam String code) {
        log.info("code = {}", code);
        SocialLoginResult socialLoginResult;
        String redirectWithParams = "";

        try {
            socialLoginResult = githubService.githubLogin(code);
            UserExistenceDto userExistenceDto = UserExistenceDto.builder()
                    .userId(socialLoginResult.getId())
                    .loginType(User.LoginType.GITHUB)
                    .build();
            log.info("userExistenceDto = {}, {}", userExistenceDto, userExistenceDto.getUserId().getClass());

            // DB 조회 후, 해당 유저 존재시 /home으로 redirect
            if (signUpService.isUserExist(userExistenceDto)) {
                redirectWithParams = frontUrl + "/home";
            } else {
                redirectWithParams = redirectUrl + "?username=" + URLEncoder.encode(socialLoginResult.getNickname(), "UTF-8")
                        + "&profile_url=" + socialLoginResult.getProfile_image_url()
                        + "&id=" + socialLoginResult.getId()
                        + "&type=" + User.LoginType.GITHUB;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        log.info("redirectWithParams = {}", redirectWithParams);
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
