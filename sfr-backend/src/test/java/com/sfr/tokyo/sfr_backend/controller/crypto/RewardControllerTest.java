package com.sfr.tokyo.sfr_backend.controller.crypto;

import com.sfr.tokyo.sfr_backend.service.crypto.RewardDistributionService;
import com.sfr.tokyo.sfr_backend.service.crypto.UserBalanceService;
import com.sfr.tokyo.sfr_backend.service.crypto.BalanceHistoryService;
import com.sfr.tokyo.sfr_backend.entity.crypto.RewardDistribution;
import com.sfr.tokyo.sfr_backend.entity.crypto.UserBalance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
// removed unused Bean and TestConfiguration imports
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.core.userdetails.UserDetailsService;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
// no direct security filter imports needed
import com.sfr.tokyo.sfr_backend.service.RateLimitService;
import com.sfr.tokyo.sfr_backend.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
// removed unused servlet imports
import static org.mockito.Mockito.*;
import org.springframework.context.annotation.Import;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("removal")
@WebMvcTest(controllers = RewardController.class)
@ActiveProfiles("test")
// no extra imports
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@Import(RewardControllerTest.SecurityArgResolverConfig.class)
class RewardControllerTest {
    // テスト用: Authentication 引数を SecurityContextHolder から解決するリゾルバを登録
    @org.springframework.boot.test.context.TestConfiguration
    public static class SecurityArgResolverConfig
            implements org.springframework.web.servlet.config.annotation.WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(
                java.util.List<org.springframework.web.method.support.HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new org.springframework.web.method.support.HandlerMethodArgumentResolver() {
                @Override
                public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
                    return org.springframework.security.core.Authentication.class
                            .isAssignableFrom(parameter.getParameterType());
                }

                @Override
                public Object resolveArgument(org.springframework.core.MethodParameter parameter,
                        org.springframework.web.method.support.ModelAndViewContainer mavContainer,
                        org.springframework.web.context.request.NativeWebRequest webRequest,
                        org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {
                    return org.springframework.security.core.context.SecurityContextHolder.getContext()
                            .getAuthentication();
                }
            });
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RewardDistributionService rewardDistributionService;

    @MockBean
    private UserBalanceService userBalanceService;

    @MockBean
    private BalanceHistoryService balanceHistoryService;

    @MockBean
    private RateLimitService rateLimitService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    private void allowRateLimit() {
        when(rateLimitService.isAllowed(anyString())).thenReturn(true);
        when(rateLimitService.isAuthAllowed(anyString())).thenReturn(true);
        when(rateLimitService.getRemainingRequests(anyString())).thenReturn(100);
        when(rateLimitService.getRemainingAuthRequests(anyString())).thenReturn(5);
        when(rateLimitService.getSecondsUntilReset(anyString())).thenReturn(0L);
    }

    private UsernamePasswordAuthenticationToken auth(String username, String... roles) {
        String[] granted = java.util.Arrays.stream(roles)
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .toArray(String[]::new);
        return new UsernamePasswordAuthenticationToken(
                username,
                "password",
                AuthorityUtils.createAuthorityList(granted));
    }

    private void setAuth(String username, String... roles) {
        SecurityContextHolder.getContext().setAuthentication(auth(username, roles));
    }

    @BeforeEach
    void setupSecurity() throws Exception {
        allowRateLimit();
        // 共通デフォルトスタブ: 空ページやデフォルト残高を返す
        when(rewardDistributionService.findByUserId(any(UUID.class), anyInt(), anyInt()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        // getUserBalance は常に存在する体で Optional.of を返す
        when(userBalanceService.getUserBalance(anyString(), anyLong())).thenAnswer(invocation -> {
            String uid = invocation.getArgument(0);
            Long space = invocation.getArgument(1);
            return java.util.Optional.of(
                    com.sfr.tokyo.sfr_backend.entity.crypto.UserBalance.builder()
                            .userId(uid)
                            .spaceId(space)
                            .currentBalance(java.math.BigDecimal.ZERO)
                            .build());
        });

        // createUserBalance も念のため有効なオブジェクトを返す
        when(userBalanceService.createUserBalance(anyString(), anyLong(), any()))
                .thenAnswer(invocation -> com.sfr.tokyo.sfr_backend.entity.crypto.UserBalance.builder()
                        .userId(invocation.getArgument(0))
                        .spaceId(invocation.getArgument(1))
                        .currentBalance(invocation.getArgument(2))
                        .build());
    }

    @Test
    void getHistory_ShouldReturnSuccess() throws Exception {
        String uid = "11111111-1111-1111-1111-111111111111";
        setAuth(uid, "USER");
        mockMvc.perform(get("/api/crypto/rewards/history")
                .with(user(uid).roles("USER"))
                .principal(auth(uid, "USER"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getStatistics_ShouldReturnSuccess() throws Exception {
        String uid = "11111111-1111-1111-1111-111111111111";
        setAuth(uid, "USER");
        mockMvc.perform(get("/api/crypto/rewards/statistics")
                .with(user(uid).roles("USER"))
                .principal(auth(uid, "USER"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getSummary_ShouldReturnSuccess() throws Exception {
        String uid = "11111111-1111-1111-1111-111111111111";
        setAuth(uid, "USER");
        mockMvc.perform(get("/api/crypto/rewards/summary")
                .with(user(uid).roles("USER"))
                .principal(auth(uid, "USER"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", roles = "ADMIN")
    void distributeRewards_ShouldReturnSuccess() throws Exception {
        // Given
        String userId = UUID.randomUUID().toString();
        String distributeRequest = "{" +
                "\"userId\":\"" + userId + "\"," +
                "\"amount\":100.00," +
                "\"category\":\"CONTENT_CREATION\"," +
                "\"triggerType\":\"MANUAL\"," +
                "\"reason\":\"Test distribution\"" +
                "}";

        RewardDistribution saved = RewardDistribution.builder()
                .id(1L)
                .userId(UUID.fromString(userId))
                .amount(new java.math.BigDecimal("100.00"))
                .distributionDate(java.time.LocalDateTime.now())
                .status(RewardDistribution.DistributionStatus.PENDING)
                .build();
        when(rewardDistributionService.createRewardDistribution(any())).thenReturn(saved);

        // user balance stubs
        UserBalance balance = UserBalance.builder().userId(userId).spaceId(1L)
                .currentBalance(new java.math.BigDecimal("0.00")).build();
        when(userBalanceService.getUserBalance(eq(userId), anyLong())).thenReturn(java.util.Optional.of(balance));
        // updateUserBalance は UserBalance を返すため、戻り値を返すスタブに修正
        com.sfr.tokyo.sfr_backend.entity.crypto.UserBalance updatedBalance = com.sfr.tokyo.sfr_backend.entity.crypto.UserBalance
                .builder()
                .userId(userId).spaceId(1L).currentBalance(new java.math.BigDecimal("100.00")).build();
        when(userBalanceService.updateUserBalance(eq(userId), anyLong(), any())).thenReturn(updatedBalance);
        com.sfr.tokyo.sfr_backend.entity.crypto.BalanceHistory bh = com.sfr.tokyo.sfr_backend.entity.crypto.BalanceHistory
                .builder()
                .historyId(java.util.UUID.randomUUID().toString())
                .userId(java.util.UUID.fromString(userId))
                .amount(new java.math.BigDecimal("100.00"))
                .balanceBefore(new java.math.BigDecimal("0.00"))
                .balanceAfter(new java.math.BigDecimal("100.00"))
                .transactionType(com.sfr.tokyo.sfr_backend.entity.crypto.BalanceHistory.TransactionType.EARN)
                .build();
        when(balanceHistoryService.recordEarning(eq(userId), any(), any(), any(), any())).thenReturn(bh);

        // When & Then
        String admin = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
        setAuth(admin, "ADMIN");
        mockMvc.perform(post("/api/crypto/rewards/distribute").with(csrf())
                .with(user(admin).roles("ADMIN"))
                .principal(auth(admin, "ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(distributeRequest))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", roles = "ADMIN")
    void approveDistribution_ShouldReturnSuccess() throws Exception {
        // Given
        Long distributionId = 1L;
        RewardDistribution approved = RewardDistribution.builder()
                .id(distributionId)
                .processedAt(java.time.LocalDateTime.now())
                .status(RewardDistribution.DistributionStatus.APPROVED)
                .build();
        when(rewardDistributionService.approveReward(eq(distributionId), any())).thenReturn(approved);

        // When & Then
        String admin = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
        setAuth(admin, "ADMIN");
        mockMvc.perform(post("/api/crypto/rewards/approve/{distributionId}", distributionId).with(csrf())
                .with(user(admin).roles("ADMIN"))
                .principal(auth(admin, "ADMIN"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", roles = "ADMIN")
    void processDistribution_ShouldReturnSuccess() throws Exception {
        // Given
        Long distributionId = 1L;
        RewardDistribution dist = RewardDistribution.builder()
                .id(distributionId)
                .userId(UUID.randomUUID())
                .amount(new java.math.BigDecimal("10.00"))
                .status(RewardDistribution.DistributionStatus.PENDING)
                .build();
        when(rewardDistributionService.findById(eq(distributionId))).thenReturn(java.util.Optional.of(dist));
        RewardDistribution processed = RewardDistribution.builder()
                .id(distributionId)
                .userId(dist.getUserId())
                .amount(dist.getAmount())
                .status(RewardDistribution.DistributionStatus.COMPLETED)
                .processedAt(java.time.LocalDateTime.now())
                .build();
        when(rewardDistributionService.processReward(eq(distributionId), anyString()))
                .thenReturn(processed);

        // When & Then
        String admin = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
        setAuth(admin, "ADMIN");
        mockMvc.perform(post("/api/crypto/rewards/process/{distributionId}", distributionId).with(csrf())
                .with(user(admin).roles("ADMIN"))
                .principal(auth(admin, "ADMIN"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", roles = "ADMIN")
    void getAdminHistory_ShouldReturnSuccess() throws Exception {
        when(rewardDistributionService.findAll(anyInt(), anyInt(), anyString()))
                .thenReturn(org.springframework.data.domain.Page.empty());
        // When & Then
        String admin = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
        setAuth(admin, "ADMIN");
        mockMvc.perform(get("/api/crypto/rewards/admin/history")
                .with(user(admin).roles("ADMIN"))
                .principal(auth(admin, "ADMIN"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

}
