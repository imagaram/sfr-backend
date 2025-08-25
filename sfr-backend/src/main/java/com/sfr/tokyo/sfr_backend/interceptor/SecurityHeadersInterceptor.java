package com.sfr.tokyo.sfr_backend.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * セキュリティヘッダーインターセプター
 * すべてのHTTPレスポンスにセキュリティヘッダーを追加
 */
@Component
public class SecurityHeadersInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Content Security Policy (CSP)
        response.setHeader("Content-Security-Policy",
                "default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://cdn.jsdelivr.net https://unpkg.com; " +
                        "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://cdn.jsdelivr.net; " +
                        "font-src 'self' https://fonts.gstatic.com; " +
                        "img-src 'self' data: https:; " +
                        "connect-src 'self' https://api.github.com; " +
                        "frame-ancestors 'none'; " +
                        "base-uri 'self'; " +
                        "form-action 'self'");

        // HTTP Strict Transport Security (HSTS)
        response.setHeader("Strict-Transport-Security",
                "max-age=31536000; includeSubDomains; preload");

        // X-Frame-Options (クリックジャッキング防止)
        response.setHeader("X-Frame-Options", "DENY");

        // X-Content-Type-Options (MIMEタイプスニッフィング防止)
        response.setHeader("X-Content-Type-Options", "nosniff");

        // X-XSS-Protection (XSS攻撃防止)
        response.setHeader("X-XSS-Protection", "1; mode=block");

        // Referrer Policy (リファラー情報の制御)
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // Permissions Policy (旧Feature Policy)
        response.setHeader("Permissions-Policy",
                "camera=(), microphone=(), geolocation=(), payment=(), usb=()");

        // Cache Control (機密情報のキャッシュ防止)
        if (request.getRequestURI().contains("/api/")) {
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
        }

        return true;
    }
}
