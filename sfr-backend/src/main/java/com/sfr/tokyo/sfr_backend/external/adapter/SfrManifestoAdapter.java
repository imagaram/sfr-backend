package com.sfr.tokyo.sfr_backend.external.adapter;

import com.sfr.tokyo.sfr_backend.council.dto.ManifestoI18nDto;
import com.sfr.tokyo.sfr_backend.council.service.ManifestoI18nService;
import com.sfr.tokyo.sfr_backend.external.contract.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * SFR.TOKYO標準Manifestoアダプター実装
 * 
 * SFR.TOKYOの内部サービスを外部システム向けに適合させる標準アダプターです。
 * このアダプターを使用することで、外部システムから統一されたインターフェースで
 * Manifesto機能にアクセスできます。
 * 
 * @author SFR.TOKYO Development Team
 * @version 1.0.0
 * @since 2025-09-02
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SfrManifestoAdapter implements ExternalManifestoAdapter {

    private final ManifestoI18nService manifestoI18nService;

    @Override
    public Optional<ManifestoI18nDto> getCurrentManifesto(String languageCode) {
        try {
            log.debug("SfrManifestoAdapter: getCurrentManifesto called with language={}", languageCode);
            
            ManifestoI18nDto manifesto = manifestoI18nService.getCurrentManifestoForExternal(languageCode);
            return Optional.ofNullable(manifesto);
            
        } catch (Exception e) {
            log.error("SfrManifestoAdapter: Error retrieving current manifesto", e);
            return Optional.empty();
        }
    }

    @Override
    public ApiResponse<List<ManifestoI18nDto.ContentStructureDto>> searchManifesto(
            String keyword, String languageCode, Pageable pageable) {
        try {
            log.debug("SfrManifestoAdapter: searchManifesto called with keyword={}, language={}", 
                    keyword, languageCode);
            
            Page<ManifestoI18nDto.ContentStructureDto> searchResults = 
                    manifestoI18nService.searchManifestoWithPaging(keyword, languageCode, pageable);
            
            return ApiResponse.success(
                    searchResults.getContent(),
                    String.format("Found %d results for '%s'", searchResults.getTotalElements(), keyword),
                    java.util.Map.of(
                            "totalElements", searchResults.getTotalElements(),
                            "totalPages", searchResults.getTotalPages(),
                            "currentPage", pageable.getPageNumber(),
                            "pageSize", pageable.getPageSize(),
                            "hasNext", searchResults.hasNext(),
                            "hasPrevious", searchResults.hasPrevious()
                    )
            );
            
        } catch (Exception e) {
            log.error("SfrManifestoAdapter: Error searching manifesto", e);
            return ApiResponse.internalError("Search failed: " + e.getMessage());
        }
    }

    @Override
    public List<String> getAvailableLanguages() {
        try {
            log.debug("SfrManifestoAdapter: getAvailableLanguages called");
            return manifestoI18nService.getAvailableLanguages();
            
        } catch (Exception e) {
            log.error("SfrManifestoAdapter: Error retrieving available languages", e);
            return List.of("ja", "en"); // フォールバック
        }
    }

    @Override
    public List<String> getAvailableVersions() {
        try {
            log.debug("SfrManifestoAdapter: getAvailableVersions called");
            return manifestoI18nService.getAvailableVersions();
            
        } catch (Exception e) {
            log.error("SfrManifestoAdapter: Error retrieving available versions", e);
            return List.of("1.0.0"); // フォールバック
        }
    }

    @Override
    public boolean isConnectionAvailable() {
        try {
            // 簡単な接続テスト（言語一覧の取得を試行）
            List<String> languages = manifestoI18nService.getAvailableLanguages();
            boolean isAvailable = languages != null && !languages.isEmpty();
            
            log.debug("SfrManifestoAdapter: Connection check result={}", isAvailable);
            return isAvailable;
            
        } catch (Exception e) {
            log.warn("SfrManifestoAdapter: Connection check failed", e);
            return false;
        }
    }

    @Override
    public AdapterInfo getAdapterInfo() {
        return new AdapterInfo(
                "SfrManifestoAdapter",
                "1.0.0",
                "SFR.TOKYO標準Manifesto外部連携アダプター",
                "SFR.TOKYO Internal Services",
                isConnectionAvailable()
        );
    }
}
