package com.sfr.tokyo.sfr_backend.controller.crypto;

import com.sfr.tokyo.sfr_backend.service.crypto.GovernanceProposalService;
import com.sfr.tokyo.sfr_backend.service.crypto.GovernanceVoteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.sfr.tokyo.sfr_backend.service.RateLimitService;
import com.sfr.tokyo.sfr_backend.filter.JwtAuthenticationFilter;
import com.sfr.tokyo.sfr_backend.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import static org.mockito.Mockito.*;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GovernanceController.class)
class GovernanceControllerTestSimple {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GovernanceProposalService governanceProposalService;

    @MockBean
    private GovernanceVoteService governanceVoteService;

    @MockBean
    private RateLimitService rateLimitService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtService jwtService;

    @BeforeEach
    void setupSecurity() throws Exception {
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
    @WithMockUser(roles = "USER")
    void getProposals_ShouldBeAccessible() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/crypto/governance/proposals").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminEndpoint_ShouldBeAccessible() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/crypto/governance/proposals").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getProposals_WithoutAuthentication_ShouldReturn401() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/crypto/governance/proposals").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
