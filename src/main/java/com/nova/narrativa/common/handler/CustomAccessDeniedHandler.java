package com.nova.narrativa.common.handler;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {

        // Jackson ObjectMapper 생성
        ObjectMapper objectMapper = new ObjectMapper();

        // JSON 객체 생성
        ObjectNode errorResponse = objectMapper.createObjectNode();
        errorResponse.put("error", "ERROR_ACCESS_DENIED");

        // JSON 문자열로 변환
        String jsonStr = objectMapper.writeValueAsString(errorResponse);

        // HTTP 응답 설정
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        // 응답 출력
        PrintWriter printWriter = response.getWriter();
        printWriter.println(jsonStr);
        printWriter.close();
    }
}