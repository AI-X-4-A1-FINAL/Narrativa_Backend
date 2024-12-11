package com.nova.narrativa.common.config;

import com.nova.narrativa.common.filter.JWTCheckFilter;
import com.nova.narrativa.common.handler.APILoginFailHandler;
import com.nova.narrativa.common.handler.APILoginSuccessHandler;
import com.nova.narrativa.common.handler.CustomAccessDeniedHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;


@Configuration
@Slf4j
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)    // method 보안(@PreAuthorize)
@EnableWebSecurity                              // 웹 애플리케이션에서의 HTTP 보안
public class CustomSecurityConfig {

    @Value("${environments.narrativa-front.url}")
    private String frontUrl;

    @Value("${environments.server.url}")
    private String serverUrl;

    @Value("${environments.narrativa-admin.url}")
    private String adminUrl;

    @Value("${environments.narrativa-ml.url}")
    private String mlUrl;

    // 인증/인가 필터 처리
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        log.info("--------------------------- securityFilterChain ---------------------------");

        http.csrf(AbstractHttpConfigurer::disable);

        http.cors(httpSecurityCorsConfigurer -> {
            httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource());
        });

        // 세션 생성 x
        http.sessionManagement(httpSecuritySessionManagementConfigurer -> {
           httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.NEVER);
        });

        http.formLogin(AbstractHttpConfigurer::disable);    // 기본 로그인 페이지 비활성화

//        http.formLogin(config -> {
//            config.loginPage("/api/member/login");
//            config.successHandler(new APILoginSuccessHandler());
//            config.failureHandler(new APILoginFailHandler());
//        });

        http.addFilterBefore(new JWTCheckFilter(), UsernamePasswordAuthenticationFilter.class);

        http.exceptionHandling(config -> {
           config.accessDeniedHandler(new CustomAccessDeniedHandler());
        });

        return http.build();
    }

    // 암호화
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // cors 관련 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(List.of(frontUrl, serverUrl, adminUrl, mlUrl));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}