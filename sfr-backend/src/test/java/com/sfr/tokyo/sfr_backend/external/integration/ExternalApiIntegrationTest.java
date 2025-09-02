package com.sfr.tokyo.sfr_backend.external.integration;

import com.sfr.tokyo.sfr_backend.council.dto.ManifestoI18nDto;
import com.sfr.tokyo.sfr_backend.council.service.ManifestoI18nService;
import com.sfr.tokyo.sfr_backend.external.adapter.SfrManifestoAdapter;
import com.sfr.tokyo.sfr_backend.external.client.SfrExternalApiClient;
import com.sfr.tokyo.sfr_backend.external.contract.ApiResponse;
import com.sfr.tokyo.sfr_backend.external.controller.ManifestoExternalControllerSimple;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 外部API統合テスト
 * 
 * Step 4: 統合テストの自動化
 * 外部連携システムの包括的な統合テストを実装します。
 * 
 * @author SFR.TOKYO Development Team
 * @version 1.0.0
 * @since 2025-09-02
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
public class ExternalApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ManifestoExternalControllerSimple manifestoController;

    @Autowired
    private SfrManifestoAdapter manifestoAdapter;

    @Autowired
    private ManifestoI18nService manifestoI18nService;

    private SfrExternalApiClient apiClient;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        RestTemplate restTemplate = new RestTemplate();
        
        // テスト用のAPIクライアント設定
        apiClient = new SfrExternalApiClient(restTemplate);
        
        // BaseUrlを動的に設定（リフレクションを使用）
        try {
            var field = SfrExternalApiClient.class.getDeclaredField("baseUrl");
            field.setAccessible(true);
            field.set(apiClient, baseUrl);
        } catch (Exception e) {
            fail("Failed to set baseUrl for test client: " + e.getMessage());
        }
    }

    @Test
    @Order(1)
    void testApiAvailabilityCheck() {
        // Given: APIサーバーが起動している状態

        // When: 接続状態を確認
        boolean isAvailable = manifestoAdapter.isConnectionAvailable();

        // Then: 接続可能であること
        assertTrue(isAvailable, "Manifesto adapter should be available");
        
        // アダプター情報の確認
        var adapterInfo = manifestoAdapter.getAdapterInfo();
        assertNotNull(adapterInfo, "Adapter info should not be null");
        assertEquals("SfrManifestoAdapter", adapterInfo.adapterName());
        assertTrue(adapterInfo.isActive(), "Adapter should be active");
    }

    @Test
    @Order(2)
    void testGetAvailableLanguages() {
        // Given: 多言語システムが設定されている状態

        // When: コントローラー経由で言語一覧を取得
        ResponseEntity<ApiResponse<List<String>>> response = 
                manifestoController.getAvailableLanguages();

        // Then: 成功レスポンスを受信
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        
        List<String> languages = response.getBody().getData();
        assertNotNull(languages, "Languages list should not be null");
        
        // デフォルト言語が含まれていることを確認
        assertTrue(languages.contains("ja") || languages.contains("en"), 
                "Should contain at least ja or en language");
    }

    @Test
    @Order(3)
    void testGetAvailableVersions() {
        // Given: バージョン管理システムが設定されている状態

        // When: コントローラー経由でバージョン一覧を取得
        ResponseEntity<ApiResponse<List<String>>> response = 
                manifestoController.getAvailableVersions();

        // Then: 成功レスポンスを受信
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        
        List<String> versions = response.getBody().getData();
        assertNotNull(versions, "Versions list should not be null");
        assertFalse(versions.isEmpty(), "Should have at least one version");
    }

    @Test
    @Order(4)
    void testGetCurrentManifestoJapanese() {
        // Given: 日本語のManifestoが存在する状態

        // When: 最新の日本語Manifestoを取得
        ResponseEntity<ApiResponse<ManifestoI18nDto>> response = 
                manifestoController.getCurrentManifesto("ja");

        // Then: 適切なレスポンスを受信
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        
        if (response.getBody().isSuccess()) {
            ManifestoI18nDto manifesto = response.getBody().getData();
            assertNotNull(manifesto, "Manifesto should not be null when successful");
        } else {
            // データが存在しない場合の確認
            assertEquals("NOT_FOUND", response.getBody().getErrorCode());
        }
    }

    @Test
    @Order(5)
    void testGetCurrentManifestoEnglish() {
        // Given: 英語のManifestoが存在する可能性がある状態

        // When: 最新の英語Manifestoを取得
        ResponseEntity<ApiResponse<ManifestoI18nDto>> response = 
                manifestoController.getCurrentManifesto("en");

        // Then: レスポンスを受信（データの存在は問わない）
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        
        // 成功またはNOT_FOUNDのいずれかであること
        assertTrue(response.getBody().isSuccess() || 
                  "NOT_FOUND".equals(response.getBody().getErrorCode()));
    }

    @Test
    @Order(6)
    void testSearchManifestoWithResults() {
        // Given: 検索可能なManifestoコンテンツが存在する状態

        // When: キーワード検索を実行
        ResponseEntity<ApiResponse<List<ManifestoI18nDto.ContentStructureDto>>> response = 
                manifestoController.searchManifesto("test", "ja", 0, 10);

        // Then: 検索結果を受信
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        
        List<ManifestoI18nDto.ContentStructureDto> results = response.getBody().getData();
        assertNotNull(results, "Search results should not be null");
        
        // メタデータの確認
        assertNotNull(response.getBody().getMetadata());
        assertTrue(response.getBody().getMetadata().containsKey("totalElements"));
        assertTrue(response.getBody().getMetadata().containsKey("currentPage"));
    }

    @Test
    @Order(7)
    void testSearchManifestoInvalidLanguage() {
        // Given: 無効な言語コードを使用

        // When: 無効な言語で検索を実行
        ResponseEntity<ApiResponse<List<ManifestoI18nDto.ContentStructureDto>>> response = 
                manifestoController.searchManifesto("test", "invalid", 0, 10);

        // Then: バリデーションエラーまたは空の結果を受信
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    @Order(8)
    void testAdapterPatternIntegration() {
        // Given: アダプターパターンが実装されている状態

        // When: アダプター経由でManifestoを取得
        Optional<ManifestoI18nDto> manifesto = manifestoAdapter.getCurrentManifesto("ja");
        
        // Then: アダプターが正常に動作
        // データの存在は問わないが、エラーが発生しないことを確認
        assertDoesNotThrow(() -> manifestoAdapter.getAvailableLanguages());
        assertDoesNotThrow(() -> manifestoAdapter.getAvailableVersions());
        
        // アダプター情報が取得できること
        var adapterInfo = manifestoAdapter.getAdapterInfo();
        assertNotNull(adapterInfo);
        assertNotNull(adapterInfo.adapterName());
        assertNotNull(adapterInfo.version());
    }

    @Test
    @Order(9)
    void testApiResponseStructure() {
        // Given: API応答構造の標準化

        // When: 各エンドポイントの応答を確認
        ResponseEntity<ApiResponse<List<String>>> languagesResponse = 
                manifestoController.getAvailableLanguages();
        
        ResponseEntity<ApiResponse<List<String>>> versionsResponse = 
                manifestoController.getAvailableVersions();

        // Then: 応答構造が統一されている
        validateApiResponseStructure(languagesResponse.getBody());
        validateApiResponseStructure(versionsResponse.getBody());
    }

    @Test
    @Order(10)
    void testConcurrentAccess() {
        // Given: 複数の同時アクセス

        // When: 複数スレッドから同時にアクセス
        List<Thread> threads = List.of(
                new Thread(() -> manifestoController.getAvailableLanguages()),
                new Thread(() -> manifestoController.getAvailableVersions()),
                new Thread(() -> manifestoController.getCurrentManifesto("ja")),
                new Thread(() -> manifestoController.searchManifesto("test", "ja", 0, 5))
        );

        // Then: 全てのスレッドが正常に完了
        assertDoesNotThrow(() -> {
            threads.forEach(Thread::start);
            for (Thread thread : threads) {
                thread.join(5000); // 5秒でタイムアウト
            }
        });
    }

    /**
     * API応答構造の検証ヘルパーメソッド
     */
    private void validateApiResponseStructure(ApiResponse<?> response) {
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getMessage(), "Response message should not be null");
        assertNotNull(response.getTimestamp(), "Response timestamp should not be null");
        
        if (response.isSuccess()) {
            assertNull(response.getErrorCode(), "Success response should not have error code");
        } else {
            assertNotNull(response.getErrorCode(), "Error response should have error code");
        }
    }
}
