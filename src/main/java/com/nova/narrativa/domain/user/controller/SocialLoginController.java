package com.nova.narrativa.domain.user.controller;

import com.nova.narrativa.domain.dashboard.service.UserLoginHistoryService;
import com.nova.narrativa.domain.user.dto.JWTTokenDTO;
import com.nova.narrativa.domain.user.dto.SignUp;
import com.nova.narrativa.domain.user.util.JWTUtil;
import com.nova.narrativa.domain.user.dto.SocialLoginResult;
import com.nova.narrativa.domain.user.dto.UserExistenceDto;
import com.nova.narrativa.domain.user.entity.User;
import com.nova.narrativa.domain.user.service.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@RequestMapping("/login")
@Slf4j
@RestController
public class SocialLoginController {

    private final KakaoService kakaoService;
    private final GoogleService googleService;
    private final GithubService githubService;
    private final SignUpService signUpService;
    private final String frontUrl;
    private final String frontSignupPart;
    private final String redirectUrl;
    private final String serverDomainUrl;
    private final int accessTokenTime = 60 * 24;     // 1일
    private final int refreshTokenTime = 60 * 24;    // 1일

    private final UserLoginHistoryService loginHistoryService; // 유저 활동기록용

    public SocialLoginController(KakaoService kakaoService,
                                 GoogleService googleService,
                                 GithubService githubService,
                                 SignUpService signUpService,
                                 @Value("${environments.narrativa-front.url}") String frontUrl,
                                 @Value("${environments.narrativa-front.signup-part}") String frontSignupPart,
                                 @Value("${environments.server.url}") String serverUrl, UserLoginHistoryService loginHistoryService) {

        this.kakaoService = kakaoService;
        this.googleService = googleService;
        this.githubService = githubService;
        this.signUpService = signUpService;
        this.frontUrl = frontUrl;
        this.frontSignupPart = frontSignupPart;
        this.loginHistoryService = loginHistoryService;
        this.redirectUrl = frontUrl + frontSignupPart;
        this.serverDomainUrl = getDomainFromUrl(serverUrl);
//        log.info("serverDomainUrl: {}", serverDomainUrl);
    }

    @GetMapping("/kakao")
    public void kakaoLogin(@RequestParam String code, HttpServletResponse response) throws IOException {
//        log.info("code = {}", code);
        SocialLoginResult socialLoginResult;
        String redirectWithParams = frontUrl + "/home";
        User.LoginType kakaoLoginType = User.LoginType.KAKAO;

        // JWTTokenDTO 정의, 로그인 타입 세팅
        JWTTokenDTO tokenDTO = JWTTokenDTO.builder()
                .loginType(kakaoLoginType)
                .build();

        try {
            socialLoginResult = kakaoService.login(code);
            String dbId = socialLoginResult.getId();
            UserExistenceDto userExistenceDto = UserExistenceDto.builder()
                    .userId(socialLoginResult.getId())
                    .loginType(kakaoLoginType)
                    .build();
//            log.info("userExistenceDto = {}, {}", userExistenceDto, userExistenceDto.getUserId().getClass());

            tokenDTO.setId(dbId);   // JWTTokenDTO id값 세팅

            // DB 조회 후, 해당 유저 존재시 /home으로 redirect
            if (signUpService.isUserExist(userExistenceDto)) {
//                log.info("해당 유저가 존재합니다.");
                Optional<User> userOptional = signUpService.getUserId(userExistenceDto);

                User user = userOptional.orElseThrow(() -> new RuntimeException("해당 유저가 존재하지 않습니다."));
                dbId = String.valueOf(user.getId());

                // 로그인 히스토리 기록 추가
                loginHistoryService.recordLogin(user.getUserId());

                // 로그인 버튼 클릭시, IF 해당 유저 탈퇴(INACTIVE) 상태 -> 정상(ACTIVE) 상태로 변환
                if (user.getStatus() == User.Status.INACTIVE) {
                    user.setStatus(User.Status.ACTIVE);
                    signUpService.saveUser(user);
                    log.info("로그인 유저 상태가 INACTIVE에서 ACTIVE로 변경되었습니다.");
                }

                tokenDTO.setUserId(user.getUserId());
                tokenDTO.setUsername(user.getUsername());
                tokenDTO.setProfile_url(user.getProfile_url());
                tokenDTO.setRole(User.Role.ROLE_USER);
                tokenDTO.setStatus(User.Status.ACTIVE);

                // DB 조회 후, 해당 유저 존재x -> 자동 회원가입 처리
            } else {
                tokenDTO.setUsername(socialLoginResult.getNickname());
                tokenDTO.setProfile_url(socialLoginResult.getProfile_image_url());
                tokenDTO.setUserId(socialLoginResult.getId());

                // 회원가입
                signUpService.register(SignUp.builder()
                        .user_id(socialLoginResult.getId())
                        .username(socialLoginResult.getNickname())
                        .profile_url(socialLoginResult.getProfile_image_url())
                        .login_type(String.valueOf(kakaoLoginType))
                        .build());

                Optional<User> userOptional = signUpService.getUserId(userExistenceDto);

                User user = userOptional.orElseThrow(() -> new RuntimeException("해당 유저가 존재하지 않습니다."));
                dbId = String.valueOf(user.getId());

                // 로그인 히스토리 기록 추가
                loginHistoryService.recordLogin(user.getUserId());

                tokenDTO.setUserId(dbId);
                tokenDTO.setStatus(user.getStatus());
            }
            log.info("dbId = {}", dbId);

            Map<String, Object> claims = tokenDTO.getClaims();
            claims.put("id", dbId);
            log.info("claims (token 넣기 전): {}", claims);

            String accessToken = JWTUtil.generateToken(claims, accessTokenTime);
            String refreshToken = JWTUtil.generateToken(claims, refreshTokenTime);
            log.info("user accessToken: {}, refreshToken: {}", accessToken, refreshToken);

            claims.put("access_token", accessToken);
            claims.put("refresh_token", refreshToken);

            log.info("claims (token 넣은 후): {}", claims);

            String encodedClaims = URLEncoder.encode(claims.toString(), StandardCharsets.UTF_8);

            // Session Cookie 생성 (브라우저 닫으면 쿠키 삭제)
            String idCookie = String.format("token=%s; domain=%s; SameSite=None; Secure; Path=/", encodedClaims, serverDomainUrl);

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
        User.LoginType googleLoginType = User.LoginType.GOOGLE;

        // JWTTokenDTO 정의, 로그인 타입 세팅
        JWTTokenDTO tokenDTO = JWTTokenDTO.builder()
                .loginType(googleLoginType)
                .build();
        try {
            socialLoginResult = googleService.login(code);
            String dbId = socialLoginResult.getId();
            UserExistenceDto userExistenceDto = UserExistenceDto.builder()
                    .userId(dbId)
                    .loginType(googleLoginType)
                    .build();
            log.info("userExistenceDto = {}, {}", userExistenceDto, userExistenceDto.getUserId().getClass());

            tokenDTO.setId(dbId);   // JWTTokenDTO id값 세팅

            // DB 조회 후, 해당 유저 존재시 /home으로 redirect
            if (signUpService.isUserExist(userExistenceDto)) {
                Optional<User> userOptional = signUpService.getUserId(userExistenceDto);

                User user = userOptional.orElseThrow(() -> new RuntimeException("해당 유저가 존재하지 않습니다."));
                dbId = String.valueOf(user.getId());

                // 로그인 히스토리 기록 추가
                loginHistoryService.recordLogin(user.getUserId());

                // 로그인 버튼 클릭시, IF 해당 유저 탈퇴(INACTIVE) 상태 -> 정상(ACTIVE) 상태로 변환
                if (user.getStatus() == User.Status.INACTIVE) {
                    user.setStatus(User.Status.ACTIVE);
                    signUpService.saveUser(user);
                    log.info("로그인 유저 상태가 INACTIVE에서 ACTIVE로 변경되었습니다.");
                }

                tokenDTO.setUserId(user.getUserId());
                tokenDTO.setUsername(user.getUsername());
                tokenDTO.setProfile_url(user.getProfile_url());
                tokenDTO.setRole(User.Role.ROLE_USER);
                tokenDTO.setStatus(User.Status.ACTIVE);

                // DB 조회 후, 해당 유저 존재x -> 자동 회원가입 처리
            } else {
                tokenDTO.setUsername(socialLoginResult.getNickname());
                tokenDTO.setProfile_url(socialLoginResult.getProfile_image_url());
                tokenDTO.setUserId(socialLoginResult.getId());

                // 회원가입
                signUpService.register(SignUp.builder()
                        .user_id(socialLoginResult.getId())
                        .username(socialLoginResult.getNickname())
                        .profile_url(socialLoginResult.getProfile_image_url())
                        .login_type(String.valueOf(googleLoginType))
                        .build());

                Optional<User> userOptional = signUpService.getUserId(userExistenceDto);

                User user = userOptional.orElseThrow(() -> new RuntimeException("해당 유저가 존재하지 않습니다."));
                dbId = String.valueOf(user.getId());

                // 로그인 히스토리 기록 추가
                loginHistoryService.recordLogin(user.getUserId());

                tokenDTO.setUserId(String.valueOf(dbId));
                tokenDTO.setStatus(user.getStatus());
            }
            log.info("dbId = {}", dbId);

            Map<String, Object> claims = tokenDTO.getClaims();
            claims.put("id", dbId);
            log.info("claims (token 넣기 전): {}", claims);

            String accessToken = JWTUtil.generateToken(claims, accessTokenTime);
            String refreshToken = JWTUtil.generateToken(claims, refreshTokenTime);
            log.info("user accessToken: {}, refreshToken: {}", accessToken, refreshToken);

            claims.put("access_token", accessToken);
            claims.put("refresh_token", refreshToken);

            log.info("claims (token 넣은 후): {}", claims);

            String encodedClaims = URLEncoder.encode(claims.toString(), StandardCharsets.UTF_8);

            // Session Cookie 생성 (브라우저 닫으면 쿠키 삭제)
            String idCookie = String.format("token=%s; domain=%s; SameSite=None; Secure; Path=/", encodedClaims, serverDomainUrl);

            log.info("idCookie: {}", idCookie);
            response.setHeader("Set-Cookie", idCookie);

            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (Exception e) {
            log.error("google login error: {}", e.getMessage());
            for (StackTraceElement element : e.getStackTrace()) {
                // 파일명, 클래스명, 메서드명, 줄 번호 로깅
                log.error("파일명: {}, 클래스명: {}, 메서드명: {}, 줄 번호: {}",
                        element.getFileName(),
                        element.getClassName(),
                        element.getMethodName(),
                        element.getLineNumber());
            }
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        }
        log.info("redirectWithParams: {}", redirectWithParams);
        response.sendRedirect(redirectWithParams);
    }

    @GetMapping("/github")
    public void githubLogin(@RequestParam String code, HttpServletResponse response) throws IOException {
        log.info("githubLogin code = {}", code);
        SocialLoginResult socialLoginResult;
        String redirectWithParams = frontUrl + "/home";
        User.LoginType githubLoginType = User.LoginType.GITHUB;

        // JWTTokenDTO 정의, 로그인 타입 세팅
        JWTTokenDTO tokenDTO = JWTTokenDTO.builder()
                .loginType(githubLoginType)
                .build();

        try {
            socialLoginResult = githubService.login(code);
            String dbId = socialLoginResult.getId();
            UserExistenceDto userExistenceDto = UserExistenceDto.builder()
                    .userId(socialLoginResult.getId())
                    .loginType(githubLoginType)
                    .build();
            log.info("userExistenceDto = {}, {}", userExistenceDto, userExistenceDto.getUserId().getClass());

            tokenDTO.setId(dbId);   // JWTTokenDTO id값 세팅

            // DB 조회 후, 해당 유저 존재시 /home으로 redirect
            if (signUpService.isUserExist(userExistenceDto)) {
                Optional<User> userOptional = signUpService.getUserId(userExistenceDto);

                User user = userOptional.orElseThrow(() -> new RuntimeException("해당 유저가 존재하지 않습니다."));
                dbId = String.valueOf(user.getId());

                // 로그인 히스토리 기록 추가
                loginHistoryService.recordLogin(user.getUserId());

                // 로그인 버튼 클릭시, IF 해당 유저 탈퇴(INACTIVE) 상태 -> 정상(ACTIVE) 상태로 변환
                if (user.getStatus() == User.Status.INACTIVE) {
                    user.setStatus(User.Status.ACTIVE);
                    signUpService.saveUser(user);
                    log.info("로그인 유저 상태가 INACTIVE에서 ACTIVE로 변경되었습니다.");
                }

                tokenDTO.setUserId(user.getUserId());
                tokenDTO.setUsername(user.getUsername());
                tokenDTO.setProfile_url(user.getProfile_url());
                tokenDTO.setRole(User.Role.ROLE_USER);
                tokenDTO.setStatus(User.Status.ACTIVE);

                // DB 조회 후, 해당 유저 존재x -> 자동 회원가입 처리
            } else {
                tokenDTO.setUsername(socialLoginResult.getNickname());
                tokenDTO.setProfile_url(socialLoginResult.getProfile_image_url());
                tokenDTO.setUserId(socialLoginResult.getId());

                // 회원가입
                signUpService.register(SignUp.builder()
                        .user_id(socialLoginResult.getId())
                        .username(socialLoginResult.getNickname())
                        .profile_url(socialLoginResult.getProfile_image_url())
                        .login_type(String.valueOf(githubLoginType))
                        .build());

                Optional<User> userOptional = signUpService.getUserId(userExistenceDto);

                User user = userOptional.orElseThrow(() -> new RuntimeException("해당 유저가 존재하지 않습니다."));
                dbId = String.valueOf(user.getId());

                // 로그인 히스토리 기록 추가
                loginHistoryService.recordLogin(user.getUserId());

                tokenDTO.setUserId(String.valueOf(dbId));
                tokenDTO.setStatus(user.getStatus());
            }
            log.info("dbId = {}", dbId);

            Map<String, Object> claims = tokenDTO.getClaims();
            claims.put("id", dbId);
            log.info("claims (token 넣기 전): {}", claims);

            String accessToken = JWTUtil.generateToken(claims, accessTokenTime);
            String refreshToken = JWTUtil.generateToken(claims, refreshTokenTime);
            log.info("user accessToken: {}, refreshToken: {}", accessToken, refreshToken);

            claims.put("access_token", accessToken);
            claims.put("refresh_token", refreshToken);

            log.info("claims (token 넣은 후): {}", claims);

            String encodedClaims = URLEncoder.encode(claims.toString(), StandardCharsets.UTF_8);

            // Session Cookie 생성 (브라우저 닫으면 쿠키 삭제)
            String idCookie = String.format("token=%s; domain=%s; SameSite=None; Secure; Path=/", encodedClaims, serverDomainUrl);

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

    private String getDomainFromUrl(String urlString) {
        try {
            // URI 객체를 생성하고, toURL()을 사용하여 URL 객체를 생성
            URI uri = new URI(urlString);
            URL url = uri.toURL();

            // 호스트명 추출
            String host = url.getHost();

            // 도메인 부분만 추출 (상위 도메인 + 최상위 도메인)
            String[] domainParts = host.split("\\.");

            // 최상위 도메인 + 두 번째 레벨 도메인만 반환 (예: test.kr)
            return domainParts.length >= 2 ? domainParts[domainParts.length - 2] + "." + domainParts[domainParts.length - 1] : host;
        } catch (Exception e) {
            e.printStackTrace();
            return null; // 예외 처리
        }
    }
}