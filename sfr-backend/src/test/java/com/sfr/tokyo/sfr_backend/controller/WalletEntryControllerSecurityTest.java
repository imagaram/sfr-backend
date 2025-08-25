package com.sfr.tokyo.sfr_backend.controller;

import com.sfr.tokyo.sfr_backend.dto.WalletEntryDTO;
import com.sfr.tokyo.sfr_backend.dto.WalletBalanceDTO;
import com.sfr.tokyo.sfr_backend.service.WalletEntryService;
import com.sfr.tokyo.sfr_backend.service.JwtService;
import com.sfr.tokyo.sfr_backend.service.UserService;
import com.sfr.tokyo.sfr_backend.user.User;
import com.sfr.tokyo.sfr_backend.filter.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SuppressWarnings("removal")
@SpringBootTest
@AutoConfigureMockMvc
public class WalletEntryControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletEntryService walletEntryService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private User buildUser() {
        UUID id = UUID.randomUUID();
        return User.builder().id(id).email("u@example.com").build();
    }

    @BeforeEach
    void before() throws Exception {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        // JwtAuthenticationFilter をスタブして Bearer valid-token の時だけ認証をセット
        org.mockito.Mockito.doAnswer((org.mockito.stubbing.Answer<Void>) inv -> {
            jakarta.servlet.http.HttpServletRequest req = inv.getArgument(0);
            jakarta.servlet.http.HttpServletResponse res = inv.getArgument(1);
            jakarta.servlet.FilterChain chain = inv.getArgument(2);
            String auth = req.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ") && auth.substring(7).trim().equals("valid-token")) {
                User user = buildUser();
                org.springframework.security.authentication.UsernamePasswordAuthenticationToken authToken = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        user, null, java.util.Collections.emptyList());
                org.springframework.security.core.context.SecurityContextHolder.getContext()
                        .setAuthentication(authToken);
            }
            chain.doFilter(req, res);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void addEntry_withValidToken_shouldReturnOk() throws Exception {
        User user = buildUser();
        WalletEntryDTO dto = new WalletEntryDTO();
        dto.setAmount(100.0);
        dto.setTeamId(1L);

        when(walletEntryService.addEntry(any())).thenReturn(dto);
        when(jwtService.extractUsername("valid-token")).thenReturn(user.getEmail());
        when(userService.loadUserByUsername(user.getEmail())).thenReturn(user);
        when(jwtService.isTokenValid("valid-token", user)).thenReturn(true);

        String payload = "{\"amount\":100,\"teamId\":1,\"description\":\"テスト収入\",\"transactionType\":\"INCOME\"}";
        mockMvc.perform(post("/wallet/entry").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(payload)
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk());
    }

    @Test
    void getBalance_withValidToken_shouldReturnOk() throws Exception {
        User user = buildUser();
        when(walletEntryService.getBalance(any(), any(), any())).thenReturn(new WalletBalanceDTO());
        when(jwtService.extractUsername("valid-token")).thenReturn(user.getEmail());
        when(userService.loadUserByUsername(user.getEmail())).thenReturn(user);
        when(jwtService.isTokenValid("valid-token", user)).thenReturn(true);

        mockMvc.perform(get("/wallet/balance?teamId=1&start=2020-01-01T00:00:00&end=2020-12-31T00:00:00")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk());
    }
}
