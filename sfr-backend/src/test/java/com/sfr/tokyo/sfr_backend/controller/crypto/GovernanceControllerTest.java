package com.sfr.tokyo.sfr_backend.controller.crypto;

import com.sfr.tokyo.sfr_backend.service.crypto.GovernanceProposalService;
import com.sfr.tokyo.sfr_backend.service.crypto.GovernanceVoteService;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;
import com.sfr.tokyo.sfr_backend.user.User;
import com.sfr.tokyo.sfr_backend.entity.crypto.GovernanceProposal;
import com.sfr.tokyo.sfr_backend.entity.crypto.GovernanceVote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.authentication.AuthenticationProvider;
import com.sfr.tokyo.sfr_backend.config.AuthEntryPoint;
import org.springframework.security.test.context.support.WithMockUser;
import com.sfr.tokyo.sfr_backend.service.RateLimitService;
import com.sfr.tokyo.sfr_backend.filter.JwtAuthenticationFilter;
import com.sfr.tokyo.sfr_backend.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("removal")
@WebMvcTest(controllers = GovernanceController.class)
@ActiveProfiles("test")
@WithMockUser(username = "test-user-123", roles = { "USER", "ADMIN" })
class GovernanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GovernanceProposalService governanceProposalService;

    @MockBean
    private GovernanceVoteService governanceVoteService;

    // Controller が利用する UserService をモック
    @MockBean
    private com.sfr.tokyo.sfr_backend.service.UserService userService;

    @MockBean
    private RateLimitService rateLimitService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtService jwtService;
    @MockBean
    private AuthenticationProvider authenticationProvider;

    @MockBean
    private AuthEntryPoint authEntryPoint;

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

        // 共通で利用するユーザーモック
        User mockUser = User.builder()
                .id(UUID.randomUUID())
                .email("test-user-123")
                .firstname("Test")
                .lastname("User")
                .password("pw")
                .build();
        when(userService.loadUserByUsername(anyString())).thenReturn(mockUser);

        // createProposal 用の Proposal モック (レスポンス生成に必要な最小フィールドのみ設定)
        GovernanceProposal mockProposal = GovernanceProposal.builder()
                .id(1L)
                .spaceId(1L)
                .proposerId(mockUser.getId())
                .title("Valid Proposal Title")
                .description("This is a sufficiently long description for governance proposal testing purposes.")
                .category(GovernanceProposal.ProposalCategory.GOVERNANCE)
                .proposalType(GovernanceProposal.ProposalType.PARAMETER_CHANGE)
                .minimumQuorum(100)
                .status(GovernanceProposal.ProposalStatus.DRAFT)
                .votingStartDate(LocalDateTime.now().plusHours(1))
                .votingEndDate(LocalDateTime.now().plusHours(169))
                .build();
        when(governanceProposalService.createProposal(anyLong(), any(), anyString(), anyString(), any(), anyInt(),
                anyInt(), any()))
                .thenReturn(mockProposal);

        // getProposal 用
        when(governanceProposalService.getProposal(anyLong())).thenReturn(mockProposal);
        // getProposals 用（空ページ）
        when(governanceProposalService.getProposalsByStatus(any(), anyInt(), anyInt()))
                .thenReturn(Page.empty());

        // vote 用の Vote モック
        GovernanceVote mockVote = GovernanceVote.builder()
                .id(10L)
                .proposalId(1L)
                .voterId(mockUser.getId())
                .voteType(GovernanceVote.VoteType.FOR)
                .votingPower(java.math.BigDecimal.valueOf(100))
                .votedAt(LocalDateTime.now())
                .build();
        when(governanceVoteService.castVote(anyLong(), any(), any(), any(), any(), any(), any()))
                .thenReturn(mockVote);
    }

    @Test
    void createProposal_ShouldReturnSuccess() throws Exception {
        // Given
        String proposalRequest = "{" +
                "\"title\":\"Valid Proposal Title\"," +
                "\"description\":\"This is a sufficiently long description for governance proposal testing purposes.\","
                +
                "\"category\":\"GOVERNANCE\"," +
                "\"proposalType\":\"PARAMETER_CHANGE\"," +
                "\"parameters\":\"{}\"" +
                "}";

        // When & Then
        mockMvc.perform(post("/api/governance/proposals").with(csrf())
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                        .user("test-user-123"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(proposalRequest))
                .andExpect(status().isCreated());
    }

    @Test
    void vote_ShouldReturnSuccess() throws Exception {
        // Given
        String voteRequest = "{" +
                "\"proposalId\":1," +
                "\"voteType\":\"FOR\"," +
                "\"reason\":\"I support this proposal\"" +
                "}";

        // When & Then
        mockMvc.perform(post("/api/governance/votes").with(csrf())
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                        .user("test-user-123"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(voteRequest))
                .andExpect(status().isCreated());
    }

    @Test
    void getProposal_ShouldReturnSuccess() throws Exception {
        // Given
        Long proposalId = 1L;

        // When & Then
        mockMvc.perform(get("/api/governance/proposals/{proposalId}", proposalId)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                        .user("test-user-123"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getProposals_ShouldReturnSuccess() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/governance/proposals")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                        .user("test-user-123"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getVoteHistory_ShouldReturnSuccess() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/governance/votes/history")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                        .user("test-user-123"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void executeProposal_ShouldReturnSuccess() throws Exception {
        // Given
        Long proposalId = 1L;

        // When & Then
        mockMvc.perform(post("/api/governance/proposals/{proposalId}/execute", proposalId)
                .with(csrf())
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                        .user("admin-user").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                // DTO の @NotNull proposalId を満たす JSON を送信
                .content("{\"proposalId\":1}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getStatistics_ShouldReturnSuccess() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/governance/statistics")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                        .user("test-user-123"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

}
