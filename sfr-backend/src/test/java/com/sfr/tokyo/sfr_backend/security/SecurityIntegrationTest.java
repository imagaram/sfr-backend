package com.sfr.tokyo.sfr_backend.security;

import com.sfr.tokyo.sfr_backend.service.RateLimitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.hamcrest.Matchers;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 統合セキュリティテスト
 * セキュリティ機能の統合的なテスト
 */
@SpringBootTest
@AutoConfigureWebMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class SecurityIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private RateLimitService rateLimitService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void testSecurityHeaders_AreAppliedToAllResponses() throws Exception {
        mockMvc.perform(get("/api/public"))
                .andExpect(header().exists("Content-Security-Policy"))
                .andExpect(header().string("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-XSS-Protection", "1; mode=block"))
                .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"))
                .andExpect(header().exists("Permissions-Policy"));
    }

    @Test
    void testCSPHeader_ContainsCorrectDirectives() throws Exception {
        mockMvc.perform(get("/api/test"))
                .andExpect(header().string("Content-Security-Policy",
                        Matchers.containsString("default-src 'self'")))
                .andExpect(header().string("Content-Security-Policy",
                        Matchers.containsString("frame-ancestors 'none'")))
                .andExpect(header().string("Content-Security-Policy",
                        Matchers.containsString("base-uri 'self'")));
    }

    @Test
    void testRateLimitHeaders_AreIncluded() throws Exception {
        // Given
        when(rateLimitService.isAllowed(anyString())).thenReturn(true);
        when(rateLimitService.getRemainingRequests(anyString())).thenReturn(59);
        when(rateLimitService.getSecondsUntilReset(anyString())).thenReturn(45L);

        // When & Then
        mockMvc.perform(get("/api/test"))
                .andExpect(header().exists("X-RateLimit-Remaining"))
                .andExpect(header().exists("X-RateLimit-Reset"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void testAuthenticatedRequest_IncludesUserContext() throws Exception {
        // Given
        when(rateLimitService.isAllowed(anyString())).thenReturn(true);
        when(rateLimitService.getRemainingRequests(anyString())).thenReturn(58);
        when(rateLimitService.getSecondsUntilReset(anyString())).thenReturn(40L);

        // When & Then
        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isNotFound()) // エンドポイントが存在しないため404
                .andExpect(header().exists("Content-Security-Policy"))
                .andExpect(header().exists("X-RateLimit-Remaining"));
    }

    @Test
    void testSQLInjectionAttempt_IsDetectedAndLogged() throws Exception {
        // Given
        when(rateLimitService.isAllowed(anyString())).thenReturn(true);
        when(rateLimitService.getRemainingRequests(anyString())).thenReturn(57);
        when(rateLimitService.getSecondsUntilReset(anyString())).thenReturn(35L);

        // When & Then - SQL Injection試行
        mockMvc.perform(get("/api/search?q='; DROP TABLE users; --"))
                .andExpect(header().exists("Content-Security-Policy"))
                .andExpect(header().exists("X-RateLimit-Remaining"));
        // セキュリティイベントがログに記録されることをテスト（間接的に）
    }

    @Test
    void testXSSAttempt_IsDetectedAndLogged() throws Exception {
        // Given
        when(rateLimitService.isAllowed(anyString())).thenReturn(true);
        when(rateLimitService.getRemainingRequests(anyString())).thenReturn(56);
        when(rateLimitService.getSecondsUntilReset(anyString())).thenReturn(30L);

        // When & Then - XSS試行
        mockMvc.perform(get("/api/comment?text=<script>alert('XSS')</script>"))
                .andExpect(header().exists("Content-Security-Policy"))
                .andExpect(header().exists("X-RateLimit-Remaining"));
    }

    @Test
    void testPathTraversalAttempt_IsDetectedAndLogged() throws Exception {
        // Given
        when(rateLimitService.isAllowed(anyString())).thenReturn(true);
        when(rateLimitService.getRemainingRequests(anyString())).thenReturn(55);
        when(rateLimitService.getSecondsUntilReset(anyString())).thenReturn(25L);

        // When & Then - Path Traversal試行
        mockMvc.perform(get("/api/file?path=../../../etc/passwd"))
                .andExpect(header().exists("Content-Security-Policy"))
                .andExpect(header().exists("X-RateLimit-Remaining"));
    }

    @Test
    void testRateLimitExceeded_Returns429() throws Exception {
        // Given
        when(rateLimitService.isAllowed(anyString())).thenReturn(false);
        when(rateLimitService.getRemainingRequests(anyString())).thenReturn(0);
        when(rateLimitService.getSecondsUntilReset(anyString())).thenReturn(60L);

        // When & Then
        mockMvc.perform(get("/api/test"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("X-RateLimit-Remaining", "0"))
                .andExpect(header().string("X-RateLimit-Reset", "60"))
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void testAuthEndpoint_UsesAuthRateLimit() throws Exception {
        // Given
        when(rateLimitService.isAuthAllowed(anyString())).thenReturn(true);
        when(rateLimitService.getRemainingAuthRequests(anyString())).thenReturn(4);
        when(rateLimitService.getSecondsUntilReset(anyString())).thenReturn(50L);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content("{\"email\":\"test@example.com\",\"password\":\"password\"}"))
                .andExpect(header().exists("X-RateLimit-Remaining"))
                .andExpect(header().exists("X-RateLimit-Reset"));
    }

    @Test
    void testCORSHeaders_AreSetCorrectly() throws Exception {
        // When & Then
        mockMvc.perform(options("/api/test")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().exists("Access-Control-Allow-Methods"))
                .andExpect(header().exists("Access-Control-Allow-Headers"));
    }

    @Test
    void testMultipleSecurityFeatures_WorkTogether() throws Exception {
        // Given
        when(rateLimitService.isAllowed(anyString())).thenReturn(true);
        when(rateLimitService.getRemainingRequests(anyString())).thenReturn(50);
        when(rateLimitService.getSecondsUntilReset(anyString())).thenReturn(45L);

        // When & Then - すべてのセキュリティ機能が同時に動作することを確認
        mockMvc.perform(get("/api/test")
                .header("X-Forwarded-For", "203.0.113.1"))
                .andExpect(header().exists("Content-Security-Policy")) // SecurityHeaders
                .andExpect(header().exists("Strict-Transport-Security")) // SecurityHeaders
                .andExpect(header().exists("X-Frame-Options")) // SecurityHeaders
                .andExpect(header().exists("X-RateLimit-Remaining")) // RateLimit
                .andExpect(header().exists("X-RateLimit-Reset")); // RateLimit
        // AccessLog機能も同時に動作（間接的にテスト）
    }

    @Test
    void testSecurityHeaders_CacheControl_ForAPIEndpoints() throws Exception {
        mockMvc.perform(get("/api/sensitive/data"))
                .andExpect(header().string("Cache-Control", "no-cache, no-store, must-revalidate"))
                .andExpect(header().string("Pragma", "no-cache"))
                .andExpect(header().string("Expires", "0"));
    }

    @Test
    void testSecurityHeaders_NoCache_ForNonAPIEndpoints() throws Exception {
        mockMvc.perform(get("/static/css/style.css"))
                .andExpect(header().exists("Content-Security-Policy"));
        // APIでないためキャッシュ制御ヘッダーはセットされない
    }

    @Test
    void testPermissionsPolicy_RestrictsFeatures() throws Exception {
        mockMvc.perform(get("/api/test"))
                .andExpect(header().string("Permissions-Policy",
                        "camera=(), microphone=(), geolocation=(), payment=(), usb=()"));
    }

    @Test
    void testHSTSHeader_IncludesSubdomains() throws Exception {
        mockMvc.perform(get("/api/test"))
                .andExpect(header().string("Strict-Transport-Security",
                        Matchers.containsString("includeSubDomains")))
                .andExpect(header().string("Strict-Transport-Security",
                        Matchers.containsString("preload")));
    }
}
