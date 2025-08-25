package com.sfr.tokyo.sfr_backend.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * アクセスログインターセプター
 * 詳細なアクセスログとセキュリティイベントを記録
 */
@Component
public class AccessLogInterceptor implements HandlerInterceptor {

    private static final Logger accessLogger = LoggerFactory.getLogger("ACCESS_LOG");
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY_LOG");

    private static final String START_TIME_ATTRIBUTE = "startTime";
    private static final String REQUEST_ID_ATTRIBUTE = "requestId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();

        request.setAttribute(START_TIME_ATTRIBUTE, startTime);
        request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);

        // MDCにリクエストIDを設定（ログの相関分析用）
        MDC.put("requestId", requestId);
        MDC.put("clientIp", getClientIpAddress(request));

        // セキュリティ関連エンドポイントのアクセス記録
        if (isSecuritySensitiveEndpoint(request.getRequestURI())) {
            logSecurityEvent(request, "SECURITY_ENDPOINT_ACCESS");
        }

        // 疑わしいリクエストの検出
        if (isSuspiciousRequest(request)) {
            logSecurityEvent(request, "SUSPICIOUS_REQUEST");
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) {
        // 必要に応じて後処理を実装
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) {
        long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        String requestId = (String) request.getAttribute(REQUEST_ID_ATTRIBUTE);
        String clientIp = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String referer = request.getHeader("Referer");
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        int statusCode = response.getStatus();

        // 認証情報の取得
        String username = "anonymous";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            username = auth.getName();
        }

        // アクセスログの記録
        String logMessage = String.format(
                "ACCESS [%s] %s %s %s%s - Status:%d Duration:%dms User:%s UserAgent:%s Referer:%s",
                requestId,
                method,
                uri,
                queryString != null ? "?" + queryString : "",
                "",
                statusCode,
                duration,
                username,
                userAgent != null ? userAgent : "N/A",
                referer != null ? referer : "N/A");

        accessLogger.info(logMessage);

        // エラーレスポンスの場合の詳細ログ
        if (statusCode >= 400) {
            String errorLogMessage = String.format(
                    "ERROR_RESPONSE [%s] %s %s - Status:%d User:%s IP:%s",
                    requestId,
                    method,
                    uri,
                    statusCode,
                    username,
                    clientIp);

            if (statusCode >= 500) {
                securityLogger.error(errorLogMessage);
            } else if (statusCode == 401 || statusCode == 403) {
                securityLogger.warn("AUTHENTICATION_FAILURE " + errorLogMessage);
            } else {
                securityLogger.info(errorLogMessage);
            }
        }

        // 認証成功のログ
        if (uri.contains("/auth/login") && statusCode == 200) {
            securityLogger.info("LOGIN_SUCCESS [{}] User:{} IP:{}", requestId, username, clientIp);
        }

        // 認証失敗のログ
        if (uri.contains("/auth/login") && statusCode != 200) {
            securityLogger.warn("LOGIN_FAILURE [{}] IP:{} Status:{}", requestId, clientIp, statusCode);
        }

        // パフォーマンス監視
        if (duration > 5000) { // 5秒以上の場合
            securityLogger.warn("SLOW_REQUEST [{}] {} {} Duration:{}ms", requestId, method, uri, duration);
        }

        // MDCのクリア
        MDC.clear();
    }

    /**
     * セキュリティ関連エンドポイントかどうかを判定
     */
    private boolean isSecuritySensitiveEndpoint(String uri) {
        return uri.contains("/auth/") ||
                uri.contains("/admin/") ||
                uri.contains("/users/") ||
                uri.contains("/api/v1/auth/");
    }

    /**
     * 疑わしいリクエストかどうかを判定
     */
    private boolean isSuspiciousRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String userAgent = request.getHeader("User-Agent");

        // SQLインジェクション試行の検出
        if (uri.toLowerCase().contains("union") ||
                uri.toLowerCase().contains("select") ||
                uri.toLowerCase().contains("drop") ||
                uri.toLowerCase().contains("insert")) {
            return true;
        }

        // XSS試行の検出
        if (uri.contains("<script") ||
                uri.contains("javascript:") ||
                uri.contains("onload=")) {
            return true;
        }

        // パストラバーサル試行の検出
        if (uri.contains("../") || uri.contains("..\\")) {
            return true;
        }

        // 疑わしいUser-Agentの検出
        if (userAgent != null && (userAgent.toLowerCase().contains("bot") ||
                userAgent.toLowerCase().contains("crawler") ||
                userAgent.toLowerCase().contains("spider") ||
                userAgent.toLowerCase().contains("scan"))) {
            return true;
        }

        return false;
    }

    /**
     * セキュリティイベントのログ記録
     */
    private void logSecurityEvent(HttpServletRequest request, String eventType) {
        String clientIp = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String uri = request.getRequestURI();
        String method = request.getMethod();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        String logMessage = String.format(
                "SECURITY_EVENT [%s] %s %s %s IP:%s UserAgent:%s Timestamp:%s",
                eventType,
                method,
                uri,
                "",
                clientIp,
                userAgent != null ? userAgent : "N/A",
                timestamp);

        securityLogger.warn(logMessage);
    }

    /**
     * クライアントのIPアドレスを取得
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
