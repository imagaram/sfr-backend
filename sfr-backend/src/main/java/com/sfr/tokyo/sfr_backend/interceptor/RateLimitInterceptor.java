package com.sfr.tokyo.sfr_backend.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.sfr.tokyo.sfr_backend.service.RateLimitService;

/**
 * レート制限インターセプター
 * IPアドレス毎のリクエスト数を制限
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitInterceptor.class);

    private final RateLimitService rateLimitService;

    public RateLimitInterceptor(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String clientIp = getClientIpAddress(request);
        String requestUri = request.getRequestURI();

        boolean isAllowed;
        int remainingRequests;

        // 認証系エンドポイントかどうかチェック
        if (isAuthEndpoint(requestUri)) {
            isAllowed = rateLimitService.isAuthAllowed(clientIp);
            remainingRequests = rateLimitService.getRemainingAuthRequests(clientIp);
        } else {
            isAllowed = rateLimitService.isAllowed(clientIp);
            remainingRequests = rateLimitService.getRemainingRequests(clientIp);
        }

        // レスポンスヘッダーにレート制限情報を追加
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remainingRequests));
        response.setHeader("X-RateLimit-Reset", String.valueOf(rateLimitService.getSecondsUntilReset(clientIp)));

        if (!isAllowed) {
            logger.warn("Rate limit exceeded for IP: {} on endpoint: {}", clientIp, requestUri);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Please try again later.\"}");

            return false;
        }

        return true;
    }

    /**
     * 認証系エンドポイントかどうかを判定
     */
    private boolean isAuthEndpoint(String requestUri) {
        return requestUri.contains("/auth/login") ||
                requestUri.contains("/auth/register") ||
                requestUri.contains("/auth/");
    }

    /**
     * クライアントのIPアドレスを取得
     * プロキシ経由の場合も考慮
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-Forヘッダーがある場合（プロキシ経由）
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        String xForwarded = request.getHeader("X-Forwarded");
        if (xForwarded != null && !xForwarded.isEmpty()) {
            return xForwarded;
        }

        String forwarded = request.getHeader("Forwarded");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded;
        }

        // 通常のIPアドレス
        return request.getRemoteAddr();
    }
}
