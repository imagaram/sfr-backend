package com.sfr.tokyo.sfr_backend.integration.crypto;

import com.sfr.tokyo.sfr_backend.dto.crypto.TokenDto;
import com.sfr.tokyo.sfr_backend.entity.crypto.UserBalance;
import com.sfr.tokyo.sfr_backend.integration.BaseIntegrationTest;
import com.sfr.tokyo.sfr_backend.repository.crypto.UserBalanceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TokenController 統合テスト
 * 
 * 実際のHTTPリクエストとデータベース操作を含む
 * エンドツーエンドのテストを実行
 */
@DisplayName("Token Controller Integration Tests")
class TokenControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserBalanceRepository userBalanceRepository;

    @Test
    @DisplayName("残高取得 - 正常系")
    void getBalance_ShouldReturnUserBalance_WhenUserExists() throws Exception {
        // Given
        String userId = "integration-test-user-1";
        String token = createTestJwtToken(userId, "USER");
        String url = baseUrl + "/api/crypto/tokens/balance/" + userId;

        // When
        ResponseEntity<UserBalance> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders(token)),
                UserBalance.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        UserBalance userBalance = response.getBody();
        assertThat(userBalance).isNotNull();
        assertThat(userBalance.getUserId()).isEqualTo(userId);
        assertThat(userBalance.getCurrentBalance()).isEqualTo(new BigDecimal("1000.00"));
    }

    @Test
    @DisplayName("残高取得 - ユーザーが存在しない場合")
    void getBalance_ShouldReturn404_WhenUserNotFound() throws Exception {
        // Given
        String userId = "non-existent-user";
        String token = createTestJwtToken(userId, "USER");
        String url = baseUrl + "/api/crypto/tokens/balance/" + userId;

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders(token)),
                String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("自分の残高取得 - 正常系")
    void getMyBalance_ShouldReturnCurrentUserBalance() throws Exception {
        // Given
        String userId = "integration-test-user-2";
        String token = createTestJwtToken(userId, "USER");
        String url = baseUrl + "/api/crypto/tokens/my-balance";

        // When
        ResponseEntity<UserBalance> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders(token)),
                UserBalance.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        UserBalance userBalance = response.getBody();
        assertThat(userBalance).isNotNull();
        assertThat(userBalance.getUserId()).isEqualTo(userId);
        assertThat(userBalance.getCurrentBalance()).isEqualTo(new BigDecimal("500.00"));
    }

    @Test
    @DisplayName("トークン転送 - 正常系")
    void transferTokens_ShouldTransferSuccessfully_WhenValidRequest() throws Exception {
        // Given
        String fromUserId = "integration-test-user-1";
        String toUserId = "integration-test-user-2";
        BigDecimal transferAmount = new BigDecimal("100.00");

        TokenDto.TransferRequest transferRequest = new TokenDto.TransferRequest();
        transferRequest.setRecipientId(toUserId);
        transferRequest.setAmount(transferAmount);
        transferRequest.setMessage("Integration test transfer");

        String token = createTestJwtToken(fromUserId, "USER");
        String url = baseUrl + "/api/crypto/tokens/transfer";

        // 転送前の残高を確認
        List<UserBalance> fromUserBefore = userBalanceRepository.findByUserId(fromUserId);
        List<UserBalance> toUserBefore = userBalanceRepository.findByUserId(toUserId);

        assertThat(fromUserBefore).isNotEmpty();
        assertThat(toUserBefore).isNotEmpty();

        BigDecimal fromBalanceBefore = fromUserBefore.get(0).getCurrentBalance();
        BigDecimal toBalanceBefore = toUserBefore.get(0).getCurrentBalance();

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(transferRequest, createAuthHeaders(token)),
                String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 転送後の残高を確認
        List<UserBalance> fromUserAfter = userBalanceRepository.findByUserId(fromUserId);
        List<UserBalance> toUserAfter = userBalanceRepository.findByUserId(toUserId);

        assertThat(fromUserAfter).isNotEmpty();
        assertThat(toUserAfter).isNotEmpty();

        assertThat(fromUserAfter.get(0).getCurrentBalance())
                .isEqualTo(fromBalanceBefore.subtract(transferAmount));
        assertThat(toUserAfter.get(0).getCurrentBalance())
                .isEqualTo(toBalanceBefore.add(transferAmount));
    }

    @Test
    @DisplayName("トークン転送 - 残高不足の場合")
    void transferTokens_ShouldFail_WhenInsufficientBalance() throws Exception {
        // Given
        String fromUserId = "integration-test-user-2"; // 残高500.00
        String toUserId = "integration-test-user-1";
        BigDecimal transferAmount = new BigDecimal("1000.00"); // 残高を超える金額

        TokenDto.TransferRequest transferRequest = new TokenDto.TransferRequest();
        transferRequest.setRecipientId(toUserId);
        transferRequest.setAmount(transferAmount);
        transferRequest.setMessage("Insufficient balance test");

        String token = createTestJwtToken(fromUserId, "USER");
        String url = baseUrl + "/api/crypto/tokens/transfer";

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(transferRequest, createAuthHeaders(token)),
                String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("認証なしアクセス - 401エラー")
    void accessWithoutAuth_ShouldReturn401() throws Exception {
        // Given
        String url = baseUrl + "/api/crypto/tokens/my-balance";

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(new org.springframework.http.HttpHeaders()),
                String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
