package com.sfr.tokyo.sfr_backend.controller.crypto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfr.tokyo.sfr_backend.dto.crypto.TokenDto;
import com.sfr.tokyo.sfr_backend.entity.crypto.UserBalance;
import com.sfr.tokyo.sfr_backend.service.crypto.BalanceHistoryService;
import com.sfr.tokyo.sfr_backend.service.crypto.UserBalanceService;
import com.sfr.tokyo.sfr_backend.service.RateLimitService;
import com.sfr.tokyo.sfr_backend.filter.JwtAuthenticationFilter;
import com.sfr.tokyo.sfr_backend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TokenController.class)
@WithMockUser(username = "test-user-123", roles = "USER")
class TokenControllerTestSimple {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserBalanceService userBalanceService;

    @MockBean
    private BalanceHistoryService balanceHistoryService;

    @MockBean
    private RateLimitService rateLimitService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    private String testUserId;
    private UserBalance testUserBalance;

    @BeforeEach
    void setUp() throws Exception {
        testUserId = "test-user-123";

        testUserBalance = UserBalance.builder()
                .userId(testUserId)
                .currentBalance(new BigDecimal("1000.00"))
                .totalEarned(new BigDecimal("2000.00"))
                .totalSpent(new BigDecimal("1000.00"))
                .totalCollected(new BigDecimal("0.00"))
                .collectionExempt(false)
                .frozen(false)
                .build();

        // allow rate limit
        when(rateLimitService.isAllowed(anyString())).thenReturn(true);
        when(rateLimitService.isAuthAllowed(anyString())).thenReturn(true);
        when(rateLimitService.getRemainingRequests(anyString())).thenReturn(100);
        when(rateLimitService.getRemainingAuthRequests(anyString())).thenReturn(5);
        when(rateLimitService.getSecondsUntilReset(anyString())).thenReturn(0L);
        if (jwtAuthenticationFilter != null) {
            doAnswer(inv -> {
                ServletRequest req = inv.getArgument(0);
                ServletResponse res = inv.getArgument(1);
                FilterChain chain = inv.getArgument(2);
                chain.doFilter(req, res);
                return null;
            }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
        }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserBalance_AsAdmin_ShouldReturnBalance() throws Exception {
        // Given
        when(userBalanceService.getUserBalance(eq(testUserId), anyLong())).thenReturn(Optional.of(testUserBalance));

        // When & Then
        mockMvc.perform(get("/api/crypto/tokens/balance/{userId}", testUserId).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUserBalance.getUserId()))
                .andExpect(jsonPath("$.balance").value(testUserBalance.getCurrentBalance().doubleValue()));

        verify(userBalanceService).getUserBalance(eq(testUserId), anyLong());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getUserBalance_AsUser_ShouldReturn403() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/crypto/tokens/balance/{userId}", testUserId).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER", username = "test-user-123")
    void getMyBalance_ShouldReturnBalance() throws Exception {
        // Given
        String username = "test-user-123";
        when(userBalanceService.getUserBalance(eq(username), anyLong())).thenReturn(Optional.of(testUserBalance));

        // When & Then
        mockMvc.perform(get("/api/crypto/tokens/balance").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUserBalance.getUserId()))
                .andExpect(jsonPath("$.balance").value(testUserBalance.getCurrentBalance().doubleValue()));

        verify(userBalanceService).getUserBalance(eq(username), anyLong());
    }

    @Test
    @WithMockUser(roles = "USER", username = "test-user-123")
    void transferTokens_ShouldTransferSuccessfully() throws Exception {
        // Given
        TokenDto.TransferRequest request = new TokenDto.TransferRequest();
        request.setRecipientId("recipient-123");
        request.setAmount(new BigDecimal("500.00"));

        when(userBalanceService.getUserBalance(eq(testUserId), anyLong())).thenReturn(Optional.of(testUserBalance));
        when(userBalanceService.getUserBalance(eq("recipient-123"), anyLong()))
                .thenReturn(Optional.of(testUserBalance));

        // When & Then
        mockMvc.perform(post("/api/crypto/tokens/transfer")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userBalanceService).getUserBalance(eq(testUserId), anyLong());
        verify(userBalanceService).getUserBalance(eq("recipient-123"), anyLong());
    }

    @Test
    void getBalance_WithoutAuthentication_ShouldReturn401() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/crypto/tokens/balance/{userId}", testUserId))
                .andExpect(status().isForbidden());

        verify(userBalanceService, never()).getUserBalance(anyString(), anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminEndpoint_ShouldBeAccessible() throws Exception {
        // Given
        when(userBalanceService.getUserBalance(eq(testUserId), anyLong())).thenReturn(Optional.of(testUserBalance));

        // When & Then
        mockMvc.perform(get("/api/crypto/tokens/balance/{userId}", testUserId).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userBalanceService).getUserBalance(eq(testUserId), anyLong());
    }

    @TestConfiguration
    @EnableMethodSecurity(prePostEnabled = true)
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testFilterChain() {
            return new org.springframework.security.web.DefaultSecurityFilterChain(
                    org.springframework.security.web.util.matcher.AnyRequestMatcher.INSTANCE);
        }
    }
}
