package com.sfr.tokyo.sfr_backend.external.client;

import com.sfr.tokyo.sfr_backend.council.dto.ManifestoI18nDto;
import com.sfr.tokyo.sfr_backend.external.contract.ApiResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;

/**
 * SFR.TOKYO外部APIクライアント
 * 
 * 外部システムからHTTP経由でSFR.TOKYO APIにアクセスするためのクライアントです。
 * このクライアントを使用することで、外部アプリケーションから
 * SFR.TOKYOの機能を簡単に利用できます。
 * 
 * 使用例:
 * - 外部Webアプリケーション
 * - マイクロサービス間連携
 * - サードパーティ統合
 * 
 * @author SFR.TOKYO Development Team
 * @version 1.0.0
 * @since 2025-09-02
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SfrExternalApiClient {

    private final RestTemplate restTemplate;

    @Value("${sfr.external.api.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${sfr.external.api.timeout:30000}")
    private int timeoutMs;

    private static final String MANIFESTO_API_PATH = "/api/external/v1/manifesto";

    /**
     * 最新Manifestoの取得
     * 
     * @param languageCode 言語コード (ja, en等)
     * @return Manifestoデータ
     */
    public Optional<ManifestoI18nDto> getCurrentManifesto(String languageCode) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(baseUrl + MANIFESTO_API_PATH + "/current")
                    .queryParam("language", languageCode)
                    .toUriString();

            log.debug("SfrExternalApiClient: Calling getCurrentManifesto URL={}", url);

            ResponseEntity<ApiResponse<ManifestoI18nDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<ManifestoI18nDto>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && 
                response.getBody() != null && 
                response.getBody().isSuccess()) {
                
                return Optional.ofNullable(response.getBody().getData());
            }

            log.warn("SfrExternalApiClient: getCurrentManifesto failed with response={}", response);
            return Optional.empty();

        } catch (Exception e) {
            log.error("SfrExternalApiClient: Error calling getCurrentManifesto", e);
            return Optional.empty();
        }
    }

    /**
     * Manifestoの検索
     * 
     * @param keyword 検索キーワード
     * @param languageCode 言語コード
     * @param page ページ番号 (0から開始)
     * @param size ページサイズ
     * @return 検索結果
     */
    public SearchResult<ManifestoI18nDto.ContentStructureDto> searchManifesto(
            String keyword, String languageCode, int page, int size) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(baseUrl + MANIFESTO_API_PATH + "/search")
                    .queryParam("keyword", keyword)
                    .queryParam("language", languageCode)
                    .queryParam("page", page)
                    .queryParam("size", size)
                    .toUriString();

            log.debug("SfrExternalApiClient: Calling searchManifesto URL={}", url);

            ResponseEntity<ApiResponse<List<ManifestoI18nDto.ContentStructureDto>>> response = 
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<ApiResponse<List<ManifestoI18nDto.ContentStructureDto>>>() {}
                    );

            if (response.getStatusCode().is2xxSuccessful() && 
                response.getBody() != null && 
                response.getBody().isSuccess()) {
                
                ApiResponse<List<ManifestoI18nDto.ContentStructureDto>> apiResponse = response.getBody();
                
                return SearchResult.<ManifestoI18nDto.ContentStructureDto>builder()
                        .data(apiResponse.getData())
                        .totalElements(extractMetadataLong(apiResponse, "totalElements"))
                        .totalPages(extractMetadataInt(apiResponse, "totalPages"))
                        .currentPage(page)
                        .pageSize(size)
                        .hasNext(extractMetadataBoolean(apiResponse, "hasNext"))
                        .hasPrevious(extractMetadataBoolean(apiResponse, "hasPrevious"))
                        .message(apiResponse.getMessage())
                        .build();
            }

            log.warn("SfrExternalApiClient: searchManifesto failed with response={}", response);
            return SearchResult.<ManifestoI18nDto.ContentStructureDto>builder()
                    .data(List.of())
                    .totalElements(0L)
                    .totalPages(0)
                    .currentPage(page)
                    .pageSize(size)
                    .hasNext(false)
                    .hasPrevious(false)
                    .message("Search failed")
                    .build();

        } catch (Exception e) {
            log.error("SfrExternalApiClient: Error calling searchManifesto", e);
            return SearchResult.<ManifestoI18nDto.ContentStructureDto>builder()
                    .data(List.of())
                    .totalElements(0L)
                    .totalPages(0)
                    .currentPage(page)
                    .pageSize(size)
                    .hasNext(false)
                    .hasPrevious(false)
                    .message("Error: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 利用可能言語一覧の取得
     * 
     * @return 言語コード一覧
     */
    public List<String> getAvailableLanguages() {
        try {
            String url = baseUrl + MANIFESTO_API_PATH + "/languages";

            log.debug("SfrExternalApiClient: Calling getAvailableLanguages URL={}", url);

            ResponseEntity<ApiResponse<List<String>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<List<String>>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && 
                response.getBody() != null && 
                response.getBody().isSuccess()) {
                
                return response.getBody().getData();
            }

            log.warn("SfrExternalApiClient: getAvailableLanguages failed with response={}", response);
            return List.of("ja", "en"); // フォールバック

        } catch (Exception e) {
            log.error("SfrExternalApiClient: Error calling getAvailableLanguages", e);
            return List.of("ja", "en"); // フォールバック
        }
    }

    /**
     * 利用可能バージョン一覧の取得
     * 
     * @return バージョン一覧
     */
    public List<String> getAvailableVersions() {
        try {
            String url = baseUrl + MANIFESTO_API_PATH + "/versions";

            log.debug("SfrExternalApiClient: Calling getAvailableVersions URL={}", url);

            ResponseEntity<ApiResponse<List<String>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<List<String>>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && 
                response.getBody() != null && 
                response.getBody().isSuccess()) {
                
                return response.getBody().getData();
            }

            log.warn("SfrExternalApiClient: getAvailableVersions failed with response={}", response);
            return List.of("1.0.0"); // フォールバック

        } catch (Exception e) {
            log.error("SfrExternalApiClient: Error calling getAvailableVersions", e);
            return List.of("1.0.0"); // フォールバック
        }
    }

    /**
     * API接続状態の確認
     * 
     * @return 接続可能かどうか
     */
    public boolean isApiAvailable() {
        try {
            List<String> languages = getAvailableLanguages();
            return languages != null && !languages.isEmpty();
        } catch (Exception e) {
            log.warn("SfrExternalApiClient: API availability check failed", e);
            return false;
        }
    }

    // ヘルパーメソッド
    private Long extractMetadataLong(ApiResponse<?> response, String key) {
        if (response.getMetadata() != null && response.getMetadata().containsKey(key)) {
            Object value = response.getMetadata().get(key);
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
        }
        return 0L;
    }

    private Integer extractMetadataInt(ApiResponse<?> response, String key) {
        if (response.getMetadata() != null && response.getMetadata().containsKey(key)) {
            Object value = response.getMetadata().get(key);
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
        }
        return 0;
    }

    private Boolean extractMetadataBoolean(ApiResponse<?> response, String key) {
        if (response.getMetadata() != null && response.getMetadata().containsKey(key)) {
            Object value = response.getMetadata().get(key);
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
        }
        return false;
    }

    /**
     * 検索結果を格納するデータクラス
     */
    @Data
    @lombok.Builder
    public static class SearchResult<T> {
        private List<T> data;
        private long totalElements;
        private int totalPages;
        private int currentPage;
        private int pageSize;
        private boolean hasNext;
        private boolean hasPrevious;
        private String message;
    }
}
