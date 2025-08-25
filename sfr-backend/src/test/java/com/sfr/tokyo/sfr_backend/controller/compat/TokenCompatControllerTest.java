package com.sfr.tokyo.sfr_backend.controller.compat;

import com.sfr.tokyo.sfr_backend.dto.crypto.api.ApiTransferDtos.TransferRequestDto;
import com.sfr.tokyo.sfr_backend.service.RateLimitService;
import com.sfr.tokyo.sfr_backend.service.crypto.BalanceHistoryService;
import com.sfr.tokyo.sfr_backend.service.crypto.UserBalanceService;
import com.sfr.tokyo.sfr_backend.entity.crypto.UserBalance;
import com.sfr.tokyo.sfr_backend.filter.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import com.sfr.tokyo.sfr_backend.config.SecurityConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.sfr.tokyo.sfr_backend.entity.crypto.BalanceHistory;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

@WebMvcTest(controllers = TokenCompatController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfiguration.class))
@AutoConfigureMockMvc(addFilters = false)
public class TokenCompatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserBalanceService userBalanceService;

    @MockBean
    private BalanceHistoryService balanceHistoryService;

    // RateLimit等の周辺依存はモック
    @MockBean
    private RateLimitService rateLimitService;

    // セキュリティフィルターはsliceテスト対象外なのでモック
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setup() {
        Mockito.when(rateLimitService.isAllowed(Mockito.anyString()))
                .thenReturn(true);
    }

    @Test
    @WithMockUser(username = "11111111-1111-1111-1111-111111111111")
    void getBalance_200_whenExists() throws Exception {
        UserBalance ub = new UserBalance();
        ub.setUserId("11111111-1111-1111-1111-111111111111");
        ub.setSpaceId(1L);
        ub.setCurrentBalance(new BigDecimal("12.3456789"));
        ub.setTotalEarned(new BigDecimal("100"));
        ub.setTotalSpent(new BigDecimal("10"));
        ub.setTotalCollected(new BigDecimal("2"));
        Mockito.when(userBalanceService.getUserBalance(Mockito.anyString(), Mockito.eq(1L)))
                .thenReturn(Optional.of(ub));

        mockMvc.perform(get("/api/v1/sfr/balance/{userId}", ub.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_id").value(ub.getUserId()))
                .andExpect(jsonPath("$.current_balance").value("12.34567890"));
    }

    @Test
    @WithMockUser(username = "11111111-1111-1111-1111-111111111111")
    void transfer_200_happyPath() throws Exception {
        String sender = "11111111-1111-1111-1111-111111111111";
        String recipient = "22222222-2222-2222-2222-222222222222";
        UserBalance senderUb = new UserBalance();
        senderUb.setUserId(sender);
        senderUb.setSpaceId(1L);
        senderUb.setCurrentBalance(new BigDecimal("100"));
        UserBalance recipientUb = new UserBalance();
        recipientUb.setUserId(recipient);
        recipientUb.setSpaceId(1L);
        recipientUb.setCurrentBalance(new BigDecimal("5"));

        Mockito.when(userBalanceService.getUserBalance(Mockito.eq(sender), Mockito.eq(1L)))
                .thenReturn(Optional.of(senderUb));
        Mockito.when(userBalanceService.getUserBalance(Mockito.eq(recipient), Mockito.eq(1L)))
                .thenReturn(Optional.of(recipientUb));

        String body = "{" +
                "\"from_user_id\":\"" + sender + "\"," +
                "\"to_user_id\":\"" + recipient + "\"," +
                "\"amount\":\"10.00000000\"," +
                "\"reason\":\"test\"" +
                "}";

        mockMvc.perform(post("/api/v1/sfr/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from_user_id").value(sender))
                .andExpect(jsonPath("$.to_user_id").value(recipient))
                .andExpect(jsonPath("$.amount").value("10.00000000"));
    }

    @Test
    @WithMockUser(username = "11111111-1111-1111-1111-111111111111")
    void transfer_400_whenInsufficientBalance() throws Exception {
        String sender = "11111111-1111-1111-1111-111111111111";
        String recipient = "22222222-2222-2222-2222-222222222222";
        UserBalance senderUb = new UserBalance();
        senderUb.setUserId(sender);
        senderUb.setSpaceId(1L);
        senderUb.setCurrentBalance(new BigDecimal("5")); // 少ない残高
        UserBalance recipientUb = new UserBalance();
        recipientUb.setUserId(recipient);
        recipientUb.setSpaceId(1L);
        recipientUb.setCurrentBalance(new BigDecimal("5"));

        Mockito.when(userBalanceService.getUserBalance(Mockito.eq(sender), Mockito.eq(1L)))
                .thenReturn(Optional.of(senderUb));
        Mockito.when(userBalanceService.getUserBalance(Mockito.eq(recipient), Mockito.eq(1L)))
                .thenReturn(Optional.of(recipientUb));

        String body = "{" +
                "\"from_user_id\":\"" + sender + "\"," +
                "\"to_user_id\":\"" + recipient + "\"," +
                "\"amount\":\"10.00000000\"," + // 残高超過
                "\"reason\":\"test\"" +
                "}";

        mockMvc.perform(post("/api/v1/sfr/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Insufficient balance"));
    }

    @Test
    @WithMockUser(username = "44444444-4444-4444-4444-444444444444")
    void transfer_400_whenZeroAmount() throws Exception {
        String sender = "44444444-4444-4444-4444-444444444444";
        String recipient = "55555555-5555-5555-5555-555555555555";
        UserBalance senderUb = new UserBalance();
        senderUb.setUserId(sender);
        senderUb.setSpaceId(1L);
        senderUb.setCurrentBalance(new BigDecimal("100"));
        UserBalance recipientUb = new UserBalance();
        recipientUb.setUserId(recipient);
        recipientUb.setSpaceId(1L);
        recipientUb.setCurrentBalance(new BigDecimal("0"));
        Mockito.when(userBalanceService.getUserBalance(Mockito.eq(sender), Mockito.eq(1L)))
                .thenReturn(Optional.of(senderUb));
        Mockito.when(userBalanceService.getUserBalance(Mockito.eq(recipient), Mockito.eq(1L)))
                .thenReturn(Optional.of(recipientUb));

        String body = "{" +
                "\"from_user_id\":\"" + sender + "\"," +
                "\"to_user_id\":\"" + recipient + "\"," +
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
    @WithMockUser(username = "66666666-6666-6666-6666-666666666666")
    void transfer_400_whenNegativeAmount() throws Exception {
        String sender = "66666666-6666-6666-6666-666666666666";
        String recipient = "77777777-7777-7777-7777-777777777777";
        UserBalance senderUb = new UserBalance();
        senderUb.setUserId(sender);
        senderUb.setSpaceId(1L);
        senderUb.setCurrentBalance(new BigDecimal("100"));
        UserBalance recipientUb = new UserBalance();
        recipientUb.setUserId(recipient);
        recipientUb.setSpaceId(1L);
        recipientUb.setCurrentBalance(new BigDecimal("0"));
        Mockito.when(userBalanceService.getUserBalance(Mockito.eq(sender), Mockito.eq(1L)))
                .thenReturn(Optional.of(senderUb));
        Mockito.when(userBalanceService.getUserBalance(Mockito.eq(recipient), Mockito.eq(1L)))
                .thenReturn(Optional.of(recipientUb));

        String body = "{" +
                "\"from_user_id\":\"" + sender + "\"," +
                "\"to_user_id\":\"" + recipient + "\"," +
                "\"amount\":\"-1.00000000\"," +
                "\"reason\":\"neg\"" +
                "}";

        mockMvc.perform(post("/api/v1/sfr/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @WithMockUser(username = "88888888-8888-8888-8888-888888888888")
    void transfer_400_whenInvalidFormatAmount() throws Exception {
        String sender = "88888888-8888-8888-8888-888888888888";
        String recipient = "99999999-9999-9999-9999-999999999999";
        UserBalance senderUb = new UserBalance();
        senderUb.setUserId(sender);
        senderUb.setSpaceId(1L);
        senderUb.setCurrentBalance(new BigDecimal("100"));
        UserBalance recipientUb = new UserBalance();
        recipientUb.setUserId(recipient);
        recipientUb.setSpaceId(1L);
        recipientUb.setCurrentBalance(new BigDecimal("0"));
        Mockito.when(userBalanceService.getUserBalance(Mockito.eq(sender), Mockito.eq(1L)))
                .thenReturn(Optional.of(senderUb));
        Mockito.when(userBalanceService.getUserBalance(Mockito.eq(recipient), Mockito.eq(1L)))
                .thenReturn(Optional.of(recipientUb));

        // 9桁小数 -> 正規表現違反
        String body = "{" +
                "\"from_user_id\":\"" + sender + "\"," +
                "\"to_user_id\":\"" + recipient + "\"," +
                "\"amount\":\"1.123456789\"," +
                "\"reason\":\"format\"" +
                "}";

        mockMvc.perform(post("/api/v1/sfr/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @WithMockUser(username = "11111111-1111-1111-1111-111111111111")
    void getHistory_200_withItems() throws Exception {
        String userId = "11111111-1111-1111-1111-111111111111";
        BalanceHistory h1 = BalanceHistory.builder()
                .historyId("h1")
                .userId(UUID.fromString(userId))
                .transactionType(BalanceHistory.TransactionType.EARN)
                .amount(new BigDecimal("5"))
                .balanceBefore(new BigDecimal("10"))
                .balanceAfter(new BigDecimal("15"))
                .reason("earn")
                .referenceId("ref1")
                .createdAt(LocalDateTime.now())
                .build();
        BalanceHistory h2 = BalanceHistory.builder()
                .historyId("h2")
                .userId(UUID.fromString(userId))
                .transactionType(BalanceHistory.TransactionType.SPEND)
                .amount(new BigDecimal("-2"))
                .balanceBefore(new BigDecimal("15"))
                .balanceAfter(new BigDecimal("13"))
                .reason("spend")
                .referenceId("ref2")
                .createdAt(LocalDateTime.now())
                .build();
        Page<BalanceHistory> page = new PageImpl<>(List.of(h1, h2), PageRequest.of(0, 20), 2);
        Mockito.when(balanceHistoryService.getUserBalanceHistory(Mockito.eq(userId), Mockito.eq(0), Mockito.eq(20)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/sfr/balance/{userId}/history?page=1&page_size=20", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].history_id").value("h1"))
                .andExpect(jsonPath("$.data[0].amount").value("5.00000000"))
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.total_pages").value(1))
                .andExpect(jsonPath("$.pagination.total_count").value(2));
        // 互換: page_index=0 同結果
        mockMvc.perform(get("/api/v1/sfr/balance/{userId}/history?page_index=0&page_size=20", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagination.page").value(1));
    }

    @Test
    @WithMockUser(username = "11111111-1111-1111-1111-111111111111")
    void getHistory_200_empty() throws Exception {
        String userId = "11111111-1111-1111-1111-111111111111";
        Page<BalanceHistory> empty = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        Mockito.when(balanceHistoryService.getUserBalanceHistory(Mockito.eq(userId), Mockito.eq(0), Mockito.eq(20)))
                .thenReturn(empty);

        mockMvc.perform(get("/api/v1/sfr/balance/{userId}/history?page=1&page_size=20", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andExpect(jsonPath("$.pagination.total_count").value(0));
    }

    // D: フォーマッティング検証（内部fmt8の結果をエンドポイント経由で確認）
    @ParameterizedTest
    @CsvSource({
            "1,1.00000000",
            "0.1,0.10000000",
            "123.456789,123.45678900",
            "5.999999999,5.99999999",
            "10.000000001,10.00000000"
    })
    @WithMockUser(username = "33333333-3333-3333-3333-333333333333")
    void balance_formatting_variants(String raw, String expected) throws Exception {
        UserBalance ub = new UserBalance();
        ub.setUserId("33333333-3333-3333-3333-333333333333");
        ub.setSpaceId(1L);
        ub.setCurrentBalance(new BigDecimal(raw));
        ub.setTotalEarned(BigDecimal.ZERO);
        ub.setTotalSpent(BigDecimal.ZERO);
        ub.setTotalCollected(BigDecimal.ZERO);
        Mockito.when(userBalanceService.getUserBalance(Mockito.eq(ub.getUserId()), Mockito.eq(1L)))
                .thenReturn(Optional.of(ub));

        mockMvc.perform(get("/api/v1/sfr/balance/{userId}", ub.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.current_balance").value(expected));
    }
}
