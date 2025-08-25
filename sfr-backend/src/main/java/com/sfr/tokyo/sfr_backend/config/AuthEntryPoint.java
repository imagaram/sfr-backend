// AuthEntryPoint.java
package com.sfr.tokyo.sfr_backend.config;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class AuthEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        // HTTPステータスコードを401 Unauthorizedに設定
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        // Content-Typeをapplication/jsonに設定
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // エラーメッセージをJSON形式でレスポンスボディに書き込む
        String jsonError = new ObjectMapper().writeValueAsString(
            new ErrorResponse("Authentication Failed", authException.getMessage())
        );
        response.getWriter().write(jsonError);
    }

    // エラーレスポンスのクラス
    public record ErrorResponse(String error, String message) {}
}