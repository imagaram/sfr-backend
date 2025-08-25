package com.sfr.tokyo.sfr_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfr.tokyo.sfr_backend.dto.WalletEntryDTO;
import com.sfr.tokyo.sfr_backend.entity.TransactionType;
import com.sfr.tokyo.sfr_backend.service.RateLimitService;
import com.sfr.tokyo.sfr_backend.filter.JwtAuthenticationFilter;
import com.sfr.tokyo.sfr_backend.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.sfr.tokyo.sfr_backend.service.WalletEntryService;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.BeforeEach;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@WebMvcTest(WalletEntryController.class)
public class WalletEntryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletEntryService walletEntryService;

    // WebMvcスライスでの依存解決用（RateLimitConfig -> RateLimitService）
    @MockBean
    private RateLimitService rateLimitService;

    // セキュリティ関連の依存をモックしてコンテキスト起動を安定化
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtService jwtService;

    private void allowRateLimit() {
        when(rateLimitService.isAllowed(anyString())).thenReturn(true);
        when(rateLimitService.isAuthAllowed(anyString())).thenReturn(true);
        when(rateLimitService.getRemainingRequests(anyString())).thenReturn(100);
        when(rateLimitService.getRemainingAuthRequests(anyString())).thenReturn(5);
        when(rateLimitService.getSecondsUntilReset(anyString())).thenReturn(0L);
    }

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() throws Exception {
        // レート制限は通す
        allowRateLimit();
        // セキュリティフィルタがチェーンを止めないようにする
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

    @Autowired(required = false)
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Test
    void printMappings() {
        if (requestMappingHandlerMapping != null) {
            requestMappingHandlerMapping.getHandlerMethods().forEach((info, handler) -> {
                System.out.println("MAPPING: " + info + " -> " + handler);
            });
        } else {
            System.out.println("RequestMappingHandlerMapping is null");
        }
    }

    @Test
    void testAddEntry() throws Exception {
        allowRateLimit();
        WalletEntryDTO dto = new WalletEntryDTO();
        dto.setTeamId(1L);
        dto.setAmount(100.0);
        dto.setDescription("テスト収入");
        dto.setTransactionType(TransactionType.INCOME);
        dto.setTimestamp(LocalDateTime.now());
        // lenient に固定レスポンスを返す
        doReturn(dto).when(walletEntryService).addEntry(any());

        mockMvc.perform(post("/wallet/entry")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamId").value(1L))
                .andExpect(jsonPath("$.amount").value(100.0))
                .andExpect(jsonPath("$.transactionType").value("INCOME"));
    }

    @Test
    void testGetEntries() throws Exception {
        allowRateLimit();
        WalletEntryDTO dto = new WalletEntryDTO();
        dto.setTeamId(1L);
        dto.setAmount(100.0);
        dto.setDescription("テスト収入");
        dto.setTransactionType(TransactionType.INCOME);
        dto.setTimestamp(LocalDateTime.now());
        doReturn(Collections.singletonList(dto)).when(walletEntryService).getEntriesByTeam(1L);

        mockMvc.perform(get("/wallet/entries?teamId=1").accept(MediaType.APPLICATION_JSON))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].teamId").value(1L));
    }
}
