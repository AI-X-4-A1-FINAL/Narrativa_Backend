package com.nova.narrativa.common.handler;

import com.nova.narrativa.domain.user.util.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
public class APILoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        log.info("------------");
        log.info("onAuthenticationSuccess authentication: {}", authentication);
        log.info("------------");
        log.info("authentication.getPrincipal(): {}", authentication.getPrincipal());


//        MemberDTO memberDTO = (MemberDTO) authentication.getPrincipal();
//
//        Map<String, Object> claims = MemberDTO.getClaims();
//
//        String accessToken = JWTUtil.generateToken(claims, 10);
//        String refreshToken = JWTUtil.generateToken(claims, 60 * 24);
//
//        claims.put("accessToken", "");
//        claims.put("refreshToken", "");
//
//        Gson gson = new Gson();
//
//        String jsonStr = gson.toJson(claims);
        String jsonStr = "APILoginSuccessHandler Login successful";
        response.setContentType("application/json; charset=utf-8");

        PrintWriter printWriter = response.getWriter();
        printWriter.println(jsonStr);
        printWriter.close();
    }
}
