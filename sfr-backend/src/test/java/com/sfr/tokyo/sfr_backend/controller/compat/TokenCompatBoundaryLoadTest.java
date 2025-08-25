package com.sfr.tokyo.sfr_backend.controller.compat;

import com.sfr.tokyo.sfr_backend.service.RateLimitService;
import com.sfr.tokyo.sfr_backend.service.crypto.BalanceHistoryService;
import com.sfr.tokyo.sfr_backend.service.crypto.UserBalanceService;
import com.sfr.tokyo.sfr_backend.entity.crypto.UserBalance;
import com.sfr.tokyo.sfr_backend.entity.crypto.BalanceHistory;
import com.sfr.tokyo.sfr_backend.filter.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.sfr.tokyo.sfr_backend.config.SecurityConfiguration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * 互換レイヤー TokenCompatController の境界/簡易負荷テスト。
 * - 最大limit
 * - 大きな残高/最大小数スケール
 * - 0/極小/端数の送金
 * - 多件履歴取得（20件上限）
 */
@WebMvcTest(controllers = TokenCompatController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfiguration.class))
@AutoConfigureMockMvc(addFilters = false)
public class TokenCompatBoundaryLoadTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserBalanceService userBalanceService;

    @MockBean
    BalanceHistoryService balanceHistoryService;

    @MockBean
    RateLimitService rateLimitService;

    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setup() {
        Mockito.when(rateLimitService.isAllowed(Mockito.anyString())).thenReturn(true);
    }

    @Test
    @DisplayName("履歴: page=1 & page_size=100 (最大) 要求で100件返却")
    @WithMockUser(username = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
    void history_max_limit() throws Exception {
        String userId = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
        List<BalanceHistory> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(BalanceHistory.builder()
                    .historyId("h" + i)
                    .userId(UUID.fromString(userId))
                    .transactionType(BalanceHistory.TransactionType.EARN)
                    .amount(BigDecimal.ONE)
                    .balanceBefore(BigDecimal.valueOf(i))
                    .balanceAfter(BigDecimal.valueOf(i + 1))
                    .reason("earn")
                    .referenceId("ref" + i)
                    .createdAt(LocalDateTime.now())
                    .build());
        }
        Page<BalanceHistory> page = new PageImpl<>(list, PageRequest.of(0, 100), 100);
        Mockito.when(balanceHistoryService.getUserBalanceHistory(Mockito.eq(userId), Mockito.eq(0), Mockito.eq(100)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/sfr/balance/{userId}/history?page=1&page_size=100", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(100))
                .andExpect(jsonPath("$.pagination.limit").value(100))
                .andExpect(jsonPath("$.pagination.total_count").value(100));
    }

    @Test
    @DisplayName("履歴: page_size > 100 は400")
    @WithMockUser(username = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
    void history_limit_too_large() throws Exception {
        mockMvc.perform(
                get("/api/v1/sfr/balance/{userId}/history?page=1&page_size=101",
                        "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("page_size must be between 1 and 100"));
    }

    @Test
    @DisplayName("履歴: page_size=0 は400")
    @WithMockUser(username = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
    void history_limit_zero() throws Exception {
        mockMvc.perform(
                get("/api/v1/sfr/balance/{userId}/history?page=1&page_size=0",
                        "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("page_size must be between 1 and 100"));
    }

    @Test
    @DisplayName("履歴: page=1 正常取得 (page_index=0 互換)")
    @WithMockUser(username = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
    void history_page_zero() throws Exception {
        String userId = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
        // 空ページを返すモック（page_index=0 有効確認）
        Page<BalanceHistory> empty = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        Mockito.when(balanceHistoryService.getUserBalanceHistory(Mockito.eq(userId), Mockito.eq(0), Mockito.eq(20)))
                .thenReturn(empty);

        // 新正式: page=1
        mockMvc.perform(get("/api/v1/sfr/balance/{userId}/history?page=1&page_size=20", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.limit").value(20))
                .andExpect(jsonPath("$.pagination.total_count").value(0));
        // 互換: page_index=0
        mockMvc.perform(get("/api/v1/sfr/balance/{userId}/history?page_index=0&page_size=20", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagination.page").value(1));
    }

    @Test
    @DisplayName("履歴: page=0 は400 (1始まりルール)")
    @WithMockUser(username = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
    void history_page_negative() throws Exception {
        mockMvc.perform(get("/api/v1/sfr/balance/{userId}/history?page=0&page_size=20",
                "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("page must be >= 1"));
    }

    @Test
    @DisplayName("残高: 非常に大きな数値と最大小数桁の切り捨て確認")
    @WithMockUser(username = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")
    void balance_large_number_truncation() throws Exception {
        UserBalance ub = new UserBalance();
        ub.setUserId("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        ub.setSpaceId(1L);
        ub.setCurrentBalance(new BigDecimal("999999999999.123456789")); // 9桁小数 -> 切り捨て
        ub.setTotalEarned(BigDecimal.ZERO);
        ub.setTotalSpent(BigDecimal.ZERO);
        ub.setTotalCollected(BigDecimal.ZERO);
        Mockito.when(userBalanceService.getUserBalance(Mockito.eq(ub.getUserId()), Mockito.eq(1L)))
                .thenReturn(Optional.of(ub));

        mockMvc.perform(get("/api/v1/sfr/balance/{uid}", ub.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.current_balance").value("999999999999.12345678"));
    }

    @Test
    @DisplayName("送金: 0送金は Amount must be greater than 0 で400")
    @WithMockUser(username = "cccccccc-cccc-cccc-cccc-cccccccccccc")
    void transfer_zero_amount_bad_request_after_validation() throws Exception {
        // 送金者残高用意
        UserBalance sender = new UserBalance();
        sender.setUserId("cccccccc-cccc-cccc-cccc-cccccccccccc");
        sender.setSpaceId(1L);
        sender.setCurrentBalance(new BigDecimal("10"));
        Mockito.when(userBalanceService.getUserBalance(Mockito.eq(sender.getUserId()), Mockito.eq(1L)))
                .thenReturn(Optional.of(sender));
        // 受取人
        UserBalance rcpt = new UserBalance();
        rcpt.setUserId("dddddddd-dddd-dddd-dddd-dddddddddddd");
        rcpt.setSpaceId(1L);
        rcpt.setCurrentBalance(BigDecimal.ZERO);
        Mockito.when(userBalanceService.getUserBalance(Mockito.eq(rcpt.getUserId()), Mockito.eq(1L)))
                .thenReturn(Optional.of(rcpt));

        String body = "{" +
                "\"from_user_id\":\"" + sender.getUserId() + "\"," +
                "\"to_user_id\":\"" + rcpt.getUserId() + "\"," +
                "\"amount\":\"0.00000000\"," +
                "\"reason\":\"zero\"" +
                "}";

        mockMvc.perform(post("/api/v1/sfr/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Amount must be greater than 0"));
    }

    @Test
    @DisplayName("履歴: ページング多ページ (total=250, page_size=100, page=3 最終ページ has_next=false)")
    @WithMockUser(username = "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee")
    void history_multi_page_last_page() throws Exception {
        String userId = "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee";
        List<BalanceHistory> list = new ArrayList<>();
        for (int i = 200; i < 250; i++) {
            list.add(BalanceHistory.builder()
                    .historyId("h" + i)
                    .userId(UUID.fromString(userId))
                    .transactionType(BalanceHistory.TransactionType.TRANSFER)
                    .amount(BigDecimal.ONE)
                    .balanceBefore(BigDecimal.valueOf(i))
                    .balanceAfter(BigDecimal.valueOf(i + 1))
                    .reason("transfer")
                    .referenceId("ref" + i)
                    .createdAt(LocalDateTime.now())
                    .build());
        }
        Page<BalanceHistory> page = new PageImpl<>(list, PageRequest.of(2, 100), 250);
        Mockito.when(balanceHistoryService.getUserBalanceHistory(Mockito.eq(userId), Mockito.eq(2), Mockito.eq(100)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/sfr/balance/{userId}/history?page=3&page_size=100", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagination.page").value(3))
                .andExpect(jsonPath("$.pagination.total_pages").value(3))
                .andExpect(jsonPath("$.pagination.has_next").value(false))
                .andExpect(jsonPath("$.pagination.has_previous").value(true));
    }
}
