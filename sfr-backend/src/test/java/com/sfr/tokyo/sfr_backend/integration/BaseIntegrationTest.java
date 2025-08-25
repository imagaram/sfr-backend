package com.sfr.tokyo.sfr_backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfr.tokyo.sfr_backend.SfrBackendApplication;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

/**
 * 統合テストの基底クラス
 * 
 * 全体的なアプリケーションコンテキストを起動して
 * 実際のHTTPリクエスト/レスポンスをテストします
 */
@SpringBootTest(classes = SfrBackendApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@TestPropertySource(locations = "classpath:application-integration-test.properties")
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:test-data.sql")
@Transactional
public abstract class BaseIntegrationTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String baseUrl;

    @BeforeEach
    void setUpBaseIntegrationTest() {
        baseUrl = "http://localhost:" + port;
    }

    /**
     * テスト用のJWTトークンを生成
     */
    protected String createTestJwtToken(String userId, String role) {
        // TODO: JWTサービスを使用してテスト用トークンを生成
        return "test-jwt-token-" + userId;
    }

    /**
     * 認証ヘッダーを含むHTTPヘッダーを作成
     */
    protected org.springframework.http.HttpHeaders createAuthHeaders(String token) {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        return headers;
    }
}
