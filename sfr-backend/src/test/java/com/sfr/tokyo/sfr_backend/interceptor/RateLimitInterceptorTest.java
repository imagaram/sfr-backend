package com.sfr.tokyo.sfr_backend.interceptor;

import com.sfr.tokyo.sfr_backend.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * RateLimitInterceptorのテストクラス
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RateLimitInterceptorTest {

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Object handler;

    @Mock
    private PrintWriter writer;

    private RateLimitInterceptor interceptor;

    @BeforeEach
    void setUp() throws Exception {
        interceptor = new RateLimitInterceptor(rateLimitService);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void testPreHandle_AllowedRequest_ReturnsTrue() throws Exception {
        // Given
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(rateLimitService.isAllowed("192.168.1.1")).thenReturn(true);
        when(rateLimitService.getRemainingRequests("192.168.1.1")).thenReturn(59);
        when(rateLimitService.getSecondsUntilReset("192.168.1.1")).thenReturn(45L);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        verify(response).setHeader("X-RateLimit-Remaining", "59");
        verify(response).setHeader("X-RateLimit-Reset", "45");
    }

    @Test
    void testPreHandle_BlockedRequest_ReturnsFalse() throws Exception {
        // Given
        when(request.getRemoteAddr()).thenReturn("192.168.1.2");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(rateLimitService.isAllowed("192.168.1.2")).thenReturn(false);
        when(rateLimitService.getRemainingRequests("192.168.1.2")).thenReturn(0);
        when(rateLimitService.getSecondsUntilReset("192.168.1.2")).thenReturn(30L);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertFalse(result);
        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        verify(response).setHeader("X-RateLimit-Remaining", "0");
        verify(response).setHeader("X-RateLimit-Reset", "30");
        verify(response).setContentType("application/json");
        verify(writer).write(anyString());
    }

    @Test
    void testPreHandle_AuthEndpoint_AllowedRequest() throws Exception {
        // Given
        when(request.getRemoteAddr()).thenReturn("192.168.1.3");
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(rateLimitService.isAuthAllowed("192.168.1.3")).thenReturn(true);
        when(rateLimitService.getRemainingAuthRequests("192.168.1.3")).thenReturn(4);
        when(rateLimitService.getSecondsUntilReset("192.168.1.3")).thenReturn(50L);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        verify(response).setHeader("X-RateLimit-Remaining", "4");
        verify(response).setHeader("X-RateLimit-Reset", "50");
    }

    @Test
    void testPreHandle_AuthEndpoint_BlockedRequest() throws Exception {
        // Given
        when(request.getRemoteAddr()).thenReturn("192.168.1.4");
        when(request.getRequestURI()).thenReturn("/api/auth/register");
        when(rateLimitService.isAuthAllowed("192.168.1.4")).thenReturn(false);
        when(rateLimitService.getRemainingAuthRequests("192.168.1.4")).thenReturn(0);
        when(rateLimitService.getSecondsUntilReset("192.168.1.4")).thenReturn(25L);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertFalse(result);
        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        verify(response).setHeader("X-RateLimit-Remaining", "0");
        verify(response).setHeader("X-RateLimit-Reset", "25");
    }

    @Test
    void testPreHandle_WithXForwardedForHeader() throws Exception {
        // Given
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.1, 192.168.1.1");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(rateLimitService.isAllowed("203.0.113.1")).thenReturn(true);
        when(rateLimitService.getRemainingRequests("203.0.113.1")).thenReturn(50);
        when(rateLimitService.getSecondsUntilReset("203.0.113.1")).thenReturn(40L);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        verify(rateLimitService).isAllowed("203.0.113.1");
        verify(rateLimitService, never()).isAllowed("192.168.1.1");
    }

    @Test
    void testPreHandle_WithXRealIpHeader() throws Exception {
        // Given
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn("203.0.113.2");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(rateLimitService.isAllowed("203.0.113.2")).thenReturn(true);
        when(rateLimitService.getRemainingRequests("203.0.113.2")).thenReturn(35);
        when(rateLimitService.getSecondsUntilReset("203.0.113.2")).thenReturn(20L);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        verify(rateLimitService).isAllowed("203.0.113.2");
        verify(rateLimitService, never()).isAllowed("192.168.1.1");
    }

    @Test
    void testPreHandle_AuthEndpoint_LoginPage() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getRemoteAddr()).thenReturn("192.168.1.5");
        when(rateLimitService.isAuthAllowed("192.168.1.5")).thenReturn(true);
        when(rateLimitService.getRemainingAuthRequests("192.168.1.5")).thenReturn(5);
        when(rateLimitService.getSecondsUntilReset("192.168.1.5")).thenReturn(60L);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        verify(rateLimitService).isAuthAllowed("192.168.1.5");
        verify(rateLimitService, never()).isAllowed(anyString());
    }

    @Test
    void testPreHandle_AuthEndpoint_RegisterPage() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/auth/register");
        when(request.getRemoteAddr()).thenReturn("192.168.1.6");
        when(rateLimitService.isAuthAllowed("192.168.1.6")).thenReturn(true);
        when(rateLimitService.getRemainingAuthRequests("192.168.1.6")).thenReturn(4);
        when(rateLimitService.getSecondsUntilReset("192.168.1.6")).thenReturn(45L);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        verify(rateLimitService).isAuthAllowed("192.168.1.6");
        verify(rateLimitService, never()).isAllowed(anyString());
    }

    @Test
    void testPreHandle_RateLimitServiceException_ShouldThrowException() {
        // Given
        when(request.getRemoteAddr()).thenReturn("192.168.1.7");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(rateLimitService.isAllowed("192.168.1.7"))
                .thenThrow(new RuntimeException("Service unavailable"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            interceptor.preHandle(request, response, handler);
        });
    }

    @Test
    void testPreHandle_EmptyIP_ShouldHandleGracefully() throws Exception {
        // Given
        when(request.getRemoteAddr()).thenReturn("");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(rateLimitService.isAllowed("")).thenReturn(true);
        when(rateLimitService.getRemainingRequests("")).thenReturn(60);
        when(rateLimitService.getSecondsUntilReset("")).thenReturn(60L);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
    }

    @Test
    void testPreHandle_NullIP_ShouldHandleGracefully() throws Exception {
        // Given
        when(request.getRemoteAddr()).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(rateLimitService.isAllowed(null)).thenReturn(true);
        when(rateLimitService.getRemainingRequests(null)).thenReturn(60);
        when(rateLimitService.getSecondsUntilReset(null)).thenReturn(60L);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
    }

    @Test
    void testAuthEndpointDetection() throws Exception {
        // Test various auth endpoints
        testAuthEndpoint("/api/auth/login", true);
        testAuthEndpoint("/api/auth/register", true);
        testAuthEndpoint("/api/auth/refresh", true);
        testAuthEndpoint("/api/users", false);
        testAuthEndpoint("/api/posts", false);
        testAuthEndpoint("/static/css/style.css", false);
    }

    private void testAuthEndpoint(String uri, boolean isAuth) throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn(uri);
        when(request.getRemoteAddr()).thenReturn("192.168.1.100");

        if (isAuth) {
            when(rateLimitService.isAuthAllowed("192.168.1.100")).thenReturn(true);
            when(rateLimitService.getRemainingAuthRequests("192.168.1.100")).thenReturn(5);
        } else {
            when(rateLimitService.isAllowed("192.168.1.100")).thenReturn(true);
            when(rateLimitService.getRemainingRequests("192.168.1.100")).thenReturn(60);
        }
        when(rateLimitService.getSecondsUntilReset("192.168.1.100")).thenReturn(60L);

        // When
        boolean result = interceptor.preHandle(request, response, handler);

        // Then
        assertTrue(result);
        if (isAuth) {
            verify(rateLimitService, atLeastOnce()).isAuthAllowed("192.168.1.100");
        } else {
            verify(rateLimitService, atLeastOnce()).isAllowed("192.168.1.100");
        }

        // Reset mocks for next iteration
        reset(rateLimitService);
    }
}
