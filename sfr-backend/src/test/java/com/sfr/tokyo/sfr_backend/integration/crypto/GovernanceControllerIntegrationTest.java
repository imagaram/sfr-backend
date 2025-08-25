package com.sfr.tokyo.sfr_backend.integration.crypto;

import com.sfr.tokyo.sfr_backend.integration.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GovernanceController 統合テスト
 * 
 * ガバナンス機能の統合テスト
 */
@DisplayName("Governance Controller Integration Tests")
class GovernanceControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("提案一覧取得 - 正常系")
    void getProposals_ShouldReturnProposalsList() throws Exception {
        // Given
        String userId = "integration-test-user-1";
        String token = createTestJwtToken(userId, "USER");
        String url = baseUrl + "/api/crypto/governance/proposals";

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
    @DisplayName("提案詳細取得 - 正常系")
    void getProposal_ShouldReturnProposalDetails() throws Exception {
        // Given
        String userId = "integration-test-user-1";
        String token = createTestJwtToken(userId, "USER");
        String proposalId = "test-proposal-1";
        String url = baseUrl + "/api/crypto/governance/proposals/" + proposalId;

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders(token)),
                String.class);

        // Then
        // 提案が存在しない場合は404、存在する場合は200
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("投票 - 正常系")
    void vote_ShouldVoteSuccessfully() throws Exception {
        // Given
        String userId = "integration-test-user-1";
        String token = createTestJwtToken(userId, "USER");
        String proposalId = "test-proposal-1";
        String url = baseUrl + "/api/crypto/governance/proposals/" + proposalId + "/vote";

        // 簡単な投票リクエスト（JSONで送信）
        String voteRequest = "{\"vote\":\"FOR\",\"reason\":\"Integration test vote\"}";

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(voteRequest, createAuthHeaders(token)),
                String.class);

        // Then
        // 提案が存在しない場合は404、存在する場合は200
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("提案作成 - 管理者権限")
    void createProposal_ShouldCreateSuccessfully_WhenAdmin() throws Exception {
        // Given
        String adminUserId = "integration-test-admin";
        String token = createTestJwtToken(adminUserId, "ADMIN");
        String url = baseUrl + "/api/crypto/governance/proposals";

        // 簡単な提案作成リクエスト
        String proposalRequest = "{\"title\":\"Integration Test Proposal\",\"description\":\"Test proposal for integration testing\",\"type\":\"PARAMETER_CHANGE\"}";

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(proposalRequest, createAuthHeaders(token)),
                String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("提案作成 - ユーザー権限で403エラー")
    void createProposal_ShouldReturn403_WhenNotAdmin() throws Exception {
        // Given
        String userId = "integration-test-user-1";
        String token = createTestJwtToken(userId, "USER");
        String url = baseUrl + "/api/crypto/governance/proposals";

        // 簡単な提案作成リクエスト
        String proposalRequest = "{\"title\":\"Unauthorized Test Proposal\",\"description\":\"This should fail\",\"type\":\"PARAMETER_CHANGE\"}";

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(proposalRequest, createAuthHeaders(token)),
                String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("認証なしアクセス - 401エラー")
    void accessWithoutAuth_ShouldReturn401() throws Exception {
        // Given
        String url = baseUrl + "/api/crypto/governance/proposals";

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
