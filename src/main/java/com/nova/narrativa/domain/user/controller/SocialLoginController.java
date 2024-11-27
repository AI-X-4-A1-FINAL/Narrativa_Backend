package com.nova.narrativa.domain.user.controller;

import com.nova.narrativa.domain.user.dto.SignUp;
import com.nova.narrativa.domain.user.dto.SocialLoginResult;
import com.nova.narrativa.domain.user.dto.UserExistenceDto;
import com.nova.narrativa.domain.user.entity.User;
import com.nova.narrativa.domain.user.service.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

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
    public void kakaoLogin(@RequestParam String code, HttpServletResponse response) throws IOException {
        log.info("code = {}", code);
        SocialLoginResult socialLoginResult;
        String redirectWithParams = frontUrl + "/home";
        Long dbId;
        try {
            socialLoginResult = kakaoService.login(code);
            UserExistenceDto userExistenceDto = UserExistenceDto.builder()
                    .userId(socialLoginResult.getId())
                    .loginType(User.LoginType.KAKAO)
                    .build();
            log.info("userExistenceDto = {}, {}", userExistenceDto, userExistenceDto.getUserId().getClass());

            // DB 조회 후, 해당 유저 존재시 /home으로 redirect
            if (signUpService.isUserExist(userExistenceDto)) {
                log.info("해당 유저가 존재합니다.");
                Optional<User> userOptional = signUpService.getUserId(userExistenceDto);

                User user = userOptional.orElseThrow(() -> new RuntimeException("해당 유저가 존재하지 않습니다."));
                dbId = user.getId();

                // 로그인 버튼 클릭시, IF 해당 유저 탈퇴(INACTIVE) 상태 -> 정상(ACTIVE) 상태로 변환
                if (user.getStatus() == User.Status.INACTIVE) {
                    user.setStatus(User.Status.ACTIVE);
                    signUpService.saveUser(user);
                    log.info("로그인 유저 상태가 INACTIVE에서 ACTIVE로 변경되었습니다.");
                }

                // DB 조회 후, 해당 유저 존재x -> 자동 회원가입 처리
            } else {
                SignUp signUp = SignUp.builder()
                        .username(socialLoginResult.getNickname())
                        .profile_url(socialLoginResult.getProfile_image_url())
                        .user_id(socialLoginResult.getId())
                        .login_type(User.LoginType.KAKAO.name())
                        .build();

                // 회원가입
                signUpService.register(signUp);

                Optional<User> userOptional = signUpService.getUserId(userExistenceDto);

                User user = userOptional.orElseThrow(() -> new RuntimeException("해당 유저가 존재하지 않습니다."));
                dbId = user.getId();
            }
            log.info("dbId = {}", dbId);

            // Session Cookie 생성 (브라우저 닫으면 쿠키 삭제)
            
            // http, https 환경 동작
//            log.info("http, https 환경 cookie test 중입니다.");
//            String idCookie = String.format("id=%d; SameSite=None; Secure; Path=/", dbId);

            // https 환경 동작
            log.info("https 환경 cookie test 중입니다.");
            String idCookie = String.format("id=%d; SameSite=Strict; Secure; HttpOnly; Path=/", dbId);

            log.info("idCookie: {}", idCookie);
            response.setHeader("Set-Cookie", idCookie);

            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        }
        log.info("redirectWithParams: {}", redirectWithParams);
        response.sendRedirect(redirectWithParams);
    }

    @GetMapping("/google")
    public void googleLogin(@RequestParam String code, HttpServletResponse response) throws IOException {
        log.info("code = {}", code);
        SocialLoginResult socialLoginResult;
        String redirectWithParams = frontUrl + "/home";
        Long dbId;
        try {
            socialLoginResult = googleService.login(code);
            UserExistenceDto userExistenceDto = UserExistenceDto.builder()
                    .userId(socialLoginResult.getId())
                    .loginType(User.LoginType.GOOGLE)
                    .build();
            log.info("userExistenceDto = {}, {}", userExistenceDto, userExistenceDto.getUserId().getClass());

            // DB 조회 후, 해당 유저 존재시 /home으로 redirect
            if (signUpService.isUserExist(userExistenceDto)) {
                Optional<User> userOptional = signUpService.getUserId(userExistenceDto);

                User user = userOptional.orElseThrow(() -> new RuntimeException("해당 유저가 존재하지 않습니다."));
                dbId = user.getId();

                // 로그인 버튼 클릭시, IF 해당 유저 탈퇴(INACTIVE) 상태 -> 정상(ACTIVE) 상태로 변환
                if (user.getStatus() == User.Status.INACTIVE) {
                    user.setStatus(User.Status.ACTIVE);
                    signUpService.saveUser(user);
                    log.info("로그인 유저 상태가 INACTIVE에서 ACTIVE로 변경되었습니다.");
                }

                // DB 조회 후, 해당 유저 존재x -> 자동 회원가입 처리
            } else {
                SignUp signUp = SignUp.builder()
                        .username(socialLoginResult.getNickname())
                        .profile_url(socialLoginResult.getProfile_image_url())
                        .user_id(socialLoginResult.getId())
                        .login_type(User.LoginType.GOOGLE.name())
                        .build();

                // 회원가입
                signUpService.register(signUp);

                Optional<User> userOptional = signUpService.getUserId(userExistenceDto);

                User user = userOptional.orElseThrow(() -> new RuntimeException("해당 유저가 존재하지 않습니다."));
                dbId = user.getId();
            }
            log.info("dbId = {}", dbId);

            // Session Cookie 생성 (브라우저 닫으면 쿠키 삭제)
            String idCookie = String.format("id=%d; SameSite=None; Secure; Path=/", dbId);

            log.info("idCookie: {}", idCookie);
            response.setHeader("Set-Cookie", idCookie);

            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        }
        log.info("redirectWithParams: {}", redirectWithParams);
        response.sendRedirect(redirectWithParams);
    }

    @GetMapping("/github")
    public void githubLogin(@RequestParam String code, HttpServletResponse response) throws IOException {
        log.info("code = {}", code);
        SocialLoginResult socialLoginResult;
        String redirectWithParams = frontUrl + "/home";
        Long dbId;
        try {
            socialLoginResult = githubService.login(code);
            UserExistenceDto userExistenceDto = UserExistenceDto.builder()
                    .userId(socialLoginResult.getId())
                    .loginType(User.LoginType.GITHUB)
                    .build();
            log.info("userExistenceDto = {}, {}", userExistenceDto, userExistenceDto.getUserId().getClass());

            // DB 조회 후, 해당 유저 존재시 /home으로 redirect
            if (signUpService.isUserExist(userExistenceDto)) {
                Optional<User> userOptional = signUpService.getUserId(userExistenceDto);

                User user = userOptional.orElseThrow(() -> new RuntimeException("해당 유저가 존재하지 않습니다."));
                dbId = user.getId();

                // 로그인 버튼 클릭시, IF 해당 유저 탈퇴(INACTIVE) 상태 -> 정상(ACTIVE) 상태로 변환
                if (user.getStatus() == User.Status.INACTIVE) {
                    user.setStatus(User.Status.ACTIVE);
                    signUpService.saveUser(user);
                    log.info("로그인 유저 상태가 INACTIVE에서 ACTIVE로 변경되었습니다.");
                }

                // DB 조회 후, 해당 유저 존재x -> 자동 회원가입 처리
            } else {
                SignUp signUp = SignUp.builder()
                        .username(socialLoginResult.getNickname())
                        .profile_url(socialLoginResult.getProfile_image_url())
                        .user_id(socialLoginResult.getId())
                        .login_type(User.LoginType.GITHUB.name())
                        .build();

                // 회원가입
                signUpService.register(signUp);

                Optional<User> userOptional = signUpService.getUserId(userExistenceDto);

                User user = userOptional.orElseThrow(() -> new RuntimeException("해당 유저가 존재하지 않습니다."));
                dbId = user.getId();
            }
            log.info("dbId = {}", dbId);

            // Session Cookie 생성 (브라우저 닫으면 쿠키 삭제)
            String idCookie = String.format("id=%d; SameSite=None; Secure; Path=/", dbId);

            log.info("idCookie: {}", idCookie);
            response.setHeader("Set-Cookie", idCookie);

            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        }
        log.info("redirectWithParams: {}", redirectWithParams);
        response.sendRedirect(redirectWithParams);
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
