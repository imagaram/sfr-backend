package com.sfr.tokyo.sfr_backend.controller.crypto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.UUID;

import com.sfr.tokyo.sfr_backend.entity.crypto.UserBalance;
import com.sfr.tokyo.sfr_backend.service.crypto.UserBalanceService;
import com.sfr.tokyo.sfr_backend.service.crypto.BalanceHistoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.core.userdetails.UserDetailsService;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.data.domain.Page;
import com.sfr.tokyo.sfr_backend.service.RateLimitService;
import com.sfr.tokyo.sfr_backend.filter.JwtAuthenticationFilter;
import com.sfr.tokyo.sfr_backend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import static org.mockito.Mockito.*;

@WebMvcTest(controllers = TokenController.class)
@ActiveProfiles("test")
@Import(TokenControllerTest.TestSecurityConfig.class)
@WithMockUser(username = "test-user-123", roles = "USER")
class TokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserBalanceService userBalanceService;

    @MockBean
    private BalanceHistoryService balanceHistoryService;

    // レート制限/セキュリティ依存のモック
    @MockBean
    private RateLimitService rateLimitService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtService jwtService;

    // Auto-config の InMemoryUserDetailsManager を回避
    @MockBean
    private UserDetailsService userDetailsService;

    private void allowRateLimit() {
        when(rateLimitService.isAllowed(anyString())).thenReturn(true);
        when(rateLimitService.isAuthAllowed(anyString())).thenReturn(true);
        when(rateLimitService.getRemainingRequests(anyString())).thenReturn(100);
        when(rateLimitService.getRemainingAuthRequests(anyString())).thenReturn(5);
        when(rateLimitService.getSecondsUntilReset(anyString())).thenReturn(0L);
    }

    @BeforeEach
    void setupSecurity() throws Exception {
        allowRateLimit();
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
    void getBalance_ShouldReturnSuccess() throws Exception {
        // Given
        UserBalance userBalance = new UserBalance();
        userBalance.setCurrentBalance(new BigDecimal("1000.00"));
        when(userBalanceService.getUserBalance(any(String.class), any(Long.class)))
                .thenReturn(java.util.Optional.of(userBalance));

        // When & Then
        mockMvc.perform(
                get("/api/crypto/tokens/balance").with(user("test-user-123")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin-1", roles = "ADMIN")
    void getBalanceByUserId_ShouldReturnSuccess() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        UserBalance userBalance = new UserBalance();
        userBalance.setCurrentBalance(new BigDecimal("500.00"));
        when(userBalanceService.getUserBalance(any(String.class), any(Long.class)))
                .thenReturn(java.util.Optional.of(userBalance));

        // When & Then
        mockMvc.perform(get("/api/crypto/tokens/balance/{userId}", userId).with(user("admin-1").roles("ADMIN"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void transfer_ShouldReturnSuccess() throws Exception {
        // Given
        String transferRequest = "{"
                + "\"recipientId\":\"recipient-123\","
                + "\"amount\":100.00,"
                + "\"message\":\"Test transfer\""
                + "}";

        // Stub balances for sender and recipient
        UserBalance sender = new UserBalance();
        sender.setUserId("test-user-123");
        sender.setCurrentBalance(new BigDecimal("1000.00"));
        when(userBalanceService.getUserBalance(eq("test-user-123"), any(Long.class)))
                .thenReturn(java.util.Optional.of(sender));

        UserBalance recipient = new UserBalance();
        recipient.setUserId("recipient-123");
        recipient.setCurrentBalance(new BigDecimal("0.00"));
        when(userBalanceService.getUserBalance(eq("recipient-123"), any(Long.class)))
                .thenReturn(java.util.Optional.of(recipient));

        // When & Then
        mockMvc.perform(post("/api/crypto/tokens/transfer").with(user("test-user-123")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(transferRequest))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin-1", roles = "ADMIN")
    void issue_ShouldReturnSuccess() throws Exception {
        // Given
        String issueRequest = "{"
                + "\"userId\":\"user-123\","
                + "\"amount\":100.00,"
                + "\"reason\":\"Test issue\""
                + "}";

        // Stub balance for target user
        UserBalance target = new UserBalance();
        target.setUserId("user-123");
        target.setCurrentBalance(new BigDecimal("0.00"));
        when(userBalanceService.getUserBalance(eq("user-123"), any(Long.class)))
                .thenReturn(java.util.Optional.of(target));

        // When & Then
        mockMvc.perform(post("/api/crypto/tokens/issue").with(user("admin-1").roles("ADMIN")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(issueRequest))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin-1", roles = "ADMIN")
    void collect_ShouldReturnSuccess() throws Exception {
        // Given
        String collectRequest = "{"
                + "\"userId\":\"user-123\","
                + "\"amount\":50.00,"
                + "\"reason\":\"Test collect\""
                + "}";

        // Stub balance for target user (enough to collect)
        UserBalance target = new UserBalance();
        target.setUserId("user-123");
        target.setCurrentBalance(new BigDecimal("100.00"));
        when(userBalanceService.getUserBalance(eq("user-123"), any(Long.class)))
                .thenReturn(java.util.Optional.of(target));

        // When & Then
        mockMvc.perform(post("/api/crypto/tokens/collect").with(user("admin-1").roles("ADMIN")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(collectRequest))
                .andExpect(status().isOk());
    }

    @Test
    void getHistory_ShouldReturnSuccess() throws Exception {
        // Stub empty page for history
        when(balanceHistoryService.getUserBalanceHistory(any(String.class), any(Integer.class), any(Integer.class)))
                .thenReturn(Page.empty());
        // When & Then
        mockMvc.perform(
                get("/api/crypto/tokens/history").with(user("test-user-123")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getStatistics_ShouldReturnSuccess() throws Exception {
        // Stub stats and current balance
        com.sfr.tokyo.sfr_backend.service.crypto.BalanceHistoryService.BalanceStatistics stats = com.sfr.tokyo.sfr_backend.service.crypto.BalanceHistoryService.BalanceStatistics
                .builder()
                .totalTransactions(0)
                .totalEarnings(BigDecimal.ZERO)
                .totalSpendings(BigDecimal.ZERO)
                .totalCollections(BigDecimal.ZERO)
                .totalBurns(BigDecimal.ZERO)
                .netChange(BigDecimal.ZERO)
                .averageTransactionAmount(BigDecimal.ZERO)
                .maxIncrease(BigDecimal.ZERO)
                .maxDecrease(BigDecimal.ZERO)
                .startingBalance(BigDecimal.ZERO)
                .endingBalance(BigDecimal.ZERO)
                .build();
        when(balanceHistoryService.getBalanceStatistics(any(String.class), any(), any())).thenReturn(stats);
        when(balanceHistoryService.calculateCurrentBalance(any(String.class))).thenReturn(BigDecimal.ZERO);
        // When & Then
        mockMvc.perform(
                get("/api/crypto/tokens/statistics").with(user("test-user-123")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
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
