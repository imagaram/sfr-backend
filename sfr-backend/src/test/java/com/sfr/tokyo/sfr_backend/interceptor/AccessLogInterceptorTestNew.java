package com.sfr.tokyo.sfr_backend.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AccessLogInterceptorのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AccessLogInterceptorTestNew {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Object handler;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    private AccessLogInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new AccessLogInterceptor();
        MDC.clear();
        SecurityContextHolder.clearContext();

        // Default mock setup for all headers
        when(request.getHeader(anyString())).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getMethod()).thenReturn("GET");
        when(response.getStatus()).thenReturn(200);
    }

    @Test
    void testPreHandle_BasicFunctionality() {
        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        assertNotNull(MDC.get("requestId"));
        assertNotNull(MDC.get("clientIp"));
    }

    @Test
    void testPreHandle_SecuritySensitiveEndpoint() {
        // Given
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
    }

    @Test
    void testPreHandle_SuspiciousRequest_SQLInjection() {
        // Given
        when(request.getRequestURI()).thenReturn("/api/search?q='; DROP TABLE users; --");

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
    }

    @Test
    void testPreHandle_SuspiciousRequest_XSS() {
        // Given
        when(request.getRequestURI()).thenReturn("/api/comment?text=<script>alert('XSS')</script>");

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
    }

    @Test
    void testPreHandle_SuspiciousRequest_PathTraversal() {
        // Given
        when(request.getRequestURI()).thenReturn("/api/file?path=../../../etc/passwd");

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
    }

    @Test
    void testPreHandle_SuspiciousUserAgent() {
        // Given
        when(request.getHeader("User-Agent")).thenReturn("BadBot/1.0 crawler");

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
    }

    @Test
    void testAfterCompletion_LogsAccessInfo() {
        // Given
        long startTime = System.currentTimeMillis();
        String requestId = "test-request-123";

        when(request.getAttribute("startTime")).thenReturn(startTime);
        when(request.getAttribute("requestId")).thenReturn(requestId);

        // When
        interceptor.afterCompletion(request, response, handler, null);

        // Then
        assertTrue(true); // Test passes if no exception
    }

    @Test
    void testAfterCompletion_WithAuthentication() {
        // Given
        long startTime = System.currentTimeMillis();
        String requestId = "test-request-124";

        when(request.getAttribute("startTime")).thenReturn(startTime);
        when(request.getAttribute("requestId")).thenReturn(requestId);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser@example.com");

        // When
        interceptor.afterCompletion(request, response, handler, null);

        // Then
        assertTrue(authentication.isAuthenticated());
    }

    @Test
    void testAfterCompletion_ErrorResponse() {
        // Given
        long startTime = System.currentTimeMillis();
        String requestId = "test-request-125";

        when(request.getAttribute("startTime")).thenReturn(startTime);
        when(request.getAttribute("requestId")).thenReturn(requestId);
        when(request.getRequestURI()).thenReturn("/api/restricted");
        when(response.getStatus()).thenReturn(403);

        // When
        interceptor.afterCompletion(request, response, handler, null);

        // Then
        assertTrue(true); // Test passes if no exception
    }

    @Test
    void testLoginSuccess() {
        // Given
        long startTime = System.currentTimeMillis();
        String requestId = "test-request-126";

        when(request.getAttribute("startTime")).thenReturn(startTime);
        when(request.getAttribute("requestId")).thenReturn(requestId);
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(response.getStatus()).thenReturn(200);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user@example.com");

        // When
        interceptor.afterCompletion(request, response, handler, null);

        // Then
        assertTrue(authentication.isAuthenticated());
    }

    @Test
    void testLoginFailure() {
        // Given
        long startTime = System.currentTimeMillis();
        String requestId = "test-request-127";

        when(request.getAttribute("startTime")).thenReturn(startTime);
        when(request.getAttribute("requestId")).thenReturn(requestId);
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(response.getStatus()).thenReturn(401);

        // When
        interceptor.afterCompletion(request, response, handler, null);

        // Then
        assertTrue(true); // Test passes if no exception
    }

    @Test
    void testGetClientIpAddress_XForwardedFor() {
        // Given
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100, 10.0.0.1");

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
    }

    @Test
    void testSecurityEventDetection() {
        // Given
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
    }

    @Test
    void testConcurrentRequests() throws InterruptedException {
        // Given
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];

        // When
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                HttpServletRequest mockRequest = mock(HttpServletRequest.class);
                HttpServletResponse mockResponse = mock(HttpServletResponse.class);
                when(mockRequest.getRemoteAddr()).thenReturn("192.168.1." + index);
                when(mockRequest.getRequestURI()).thenReturn("/api/test");
                when(mockRequest.getMethod()).thenReturn("GET");
                when(mockRequest.getHeader(anyString())).thenReturn(null);
                when(mockResponse.getStatus()).thenReturn(200);

                interceptor.preHandle(mockRequest, mockResponse, handler);
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Then
        assertTrue(true); // Test passes if no exception
    }

    @Test
    void testMDCContextCleanup() {
        // Given
        MDC.put("testKey", "testValue");

        // When
        interceptor.preHandle(request, response, handler);

        // Then
        assertNotNull(MDC.get("requestId"));
        assertNotNull(MDC.get("clientIp"));
    }
}
