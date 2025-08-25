package com.sfr.tokyo.sfr_backend.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

/**
 * SecurityHeadersInterceptorのテストクラス
 */
@ExtendWith(MockitoExtension.class)
public class SecurityHeadersInterceptorTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Object handler;

    private SecurityHeadersInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new SecurityHeadersInterceptor();
    }

    @Test
    void testPreHandle_SetsAllSecurityHeaders() {
        // Given
        when(request.getRequestURI()).thenReturn("/api/test");

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);

        // セキュリティヘッダーの設定を検証
        verify(response).setHeader(eq("Content-Security-Policy"), anyString());
        verify(response).setHeader(eq("Strict-Transport-Security"), eq("max-age=31536000; includeSubDomains; preload"));
        verify(response).setHeader(eq("X-Frame-Options"), eq("DENY"));
        verify(response).setHeader(eq("X-Content-Type-Options"), eq("nosniff"));
        verify(response).setHeader(eq("X-XSS-Protection"), eq("1; mode=block"));
        verify(response).setHeader(eq("Referrer-Policy"), eq("strict-origin-when-cross-origin"));
        verify(response).setHeader(eq("Permissions-Policy"), anyString());
    }

    @Test
    void testPreHandle_SetsCSPHeader() {
        // Given
        when(request.getRequestURI()).thenReturn("/api/test");

        // When
        interceptor.preHandle(request, response, handler);

        // Then
        verify(response).setHeader("Content-Security-Policy",
                "default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://cdn.jsdelivr.net https://unpkg.com; " +
                        "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://cdn.jsdelivr.net; " +
                        "font-src 'self' https://fonts.gstatic.com; " +
                        "img-src 'self' data: https:; " +
                        "connect-src 'self' https://api.github.com; " +
                        "frame-ancestors 'none'; " +
                        "base-uri 'self'; " +
                        "form-action 'self'");
    }

    @Test
    void testPreHandle_SetsApiCacheHeaders() {
        // Given
        when(request.getRequestURI()).thenReturn("/api/user/profile");

        // When
        interceptor.preHandle(request, response, handler);

        // Then
        verify(response).setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        verify(response).setHeader("Pragma", "no-cache");
        verify(response).setHeader("Expires", "0");
    }

    @Test
    void testPreHandle_NonApiRequest_DoesNotSetCacheHeaders() {
        // Given
        when(request.getRequestURI()).thenReturn("/static/css/style.css");

        // When
        interceptor.preHandle(request, response, handler);

        // Then
        verify(response, times(0)).setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        verify(response, times(0)).setHeader("Pragma", "no-cache");
        verify(response, times(0)).setHeader("Expires", "0");
    }

    @Test
    void testPreHandle_SetsHSTSHeader() {
        // Given
        when(request.getRequestURI()).thenReturn("/test");

        // When
        interceptor.preHandle(request, response, handler);

        // Then
        verify(response).setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
    }

    @Test
    void testPreHandle_SetsXFrameOptionsHeader() {
        // Given
        when(request.getRequestURI()).thenReturn("/test");

        // When
        interceptor.preHandle(request, response, handler);

        // Then
        verify(response).setHeader("X-Frame-Options", "DENY");
    }

    @Test
    void testPreHandle_SetsXContentTypeOptionsHeader() {
        // Given
        when(request.getRequestURI()).thenReturn("/test");

        // When
        interceptor.preHandle(request, response, handler);

        // Then
        verify(response).setHeader("X-Content-Type-Options", "nosniff");
    }

    @Test
    void testPreHandle_SetsXXSSProtectionHeader() {
        // Given
        when(request.getRequestURI()).thenReturn("/test");

        // When
        interceptor.preHandle(request, response, handler);

        // Then
        verify(response).setHeader("X-XSS-Protection", "1; mode=block");
    }

    @Test
    void testPreHandle_SetsReferrerPolicyHeader() {
        // Given
        when(request.getRequestURI()).thenReturn("/test");

        // When
        interceptor.preHandle(request, response, handler);

        // Then
        verify(response).setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
    }

    @Test
    void testPreHandle_SetsPermissionsPolicyHeader() {
        // Given
        when(request.getRequestURI()).thenReturn("/test");

        // When
        interceptor.preHandle(request, response, handler);

        // Then
        verify(response).setHeader("Permissions-Policy",
                "camera=(), microphone=(), geolocation=(), payment=(), usb=()");
    }

    @Test
    void testPreHandle_AlwaysReturnsTrue() {
        // Given
        when(request.getRequestURI()).thenReturn("/any/path");

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
    }
}
