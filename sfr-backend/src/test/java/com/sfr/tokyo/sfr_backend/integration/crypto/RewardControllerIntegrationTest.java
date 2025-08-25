package com.sfr.tokyo.sfr_backend.integration.crypto;

import com.sfr.tokyo.sfr_backend.dto.crypto.RewardDto;
import com.sfr.tokyo.sfr_backend.integration.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RewardController 統合テスト
 * 
 * リワード配布機能の統合テスト
 */
@DisplayName("Reward Controller Integration Tests")
class RewardControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("リワード取得 - 正常系")
    void getRewards_ShouldReturnRewardsList() throws Exception {
        // Given
        String userId = "integration-test-user-1";
        String token = createTestJwtToken(userId, "USER");
        String url = baseUrl + "/api/crypto/rewards";

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders(token)),
                String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("リワード配布 - 正常系")
    void distributeReward_ShouldDistributeSuccessfully() throws Exception {
        // Given
        String adminUserId = "integration-test-admin";
        String token = createTestJwtToken(adminUserId, "ADMIN");
        String url = baseUrl + "/api/crypto/rewards/distribute";

        RewardDto.DistributeRequest distributeRequest = new RewardDto.DistributeRequest();
        distributeRequest.setUserId("integration-test-user-1");
        distributeRequest.setAmount(new BigDecimal("50.00"));
        distributeRequest.setCategory("POST_CREATION");
        distributeRequest.setTriggerType("MANUAL");
        distributeRequest.setReason("Integration test reward");

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(distributeRequest, createAuthHeaders(token)),
                String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("リワード配布 - ユーザー権限で403エラー")
    void distributeReward_ShouldReturn403_WhenNotAdmin() throws Exception {
        // Given
        String userId = "integration-test-user-1";
        String token = createTestJwtToken(userId, "USER");
        String url = baseUrl + "/api/crypto/rewards/distribute";

        RewardDto.DistributeRequest distributeRequest = new RewardDto.DistributeRequest();
        distributeRequest.setUserId("integration-test-user-2");
        distributeRequest.setAmount(new BigDecimal("50.00"));
        distributeRequest.setCategory("POST_CREATION");
        distributeRequest.setTriggerType("MANUAL");
        distributeRequest.setReason("Unauthorized test");

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(distributeRequest, createAuthHeaders(token)),
                String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("認証なしアクセス - 401エラー")
    void accessWithoutAuth_ShouldReturn401() throws Exception {
        // Given
        String url = baseUrl + "/api/crypto/rewards";

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
