package com.sfr.tokyo.sfr_backend.external.controller;

import com.sfr.tokyo.sfr_backend.council.dto.ManifestoI18nDto;
import com.sfr.tokyo.sfr_backend.council.service.ManifestoI18nService;
import com.sfr.tokyo.sfr_backend.external.contract.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Manifesto外部API連携コントローラー（簡略版）
 * 
 * 外部システム・SDK・DAOからのManifesto情報アクセス用のRESTful APIを提供します。
 * このAPIは外部プロジェクトでの再利用を想定して設計されています。
 * 
 * @author SFR.TOKYO Development Team
 * @version 1.0.0
 * @since 2025-09-02
 */
@RestController
@RequestMapping("/api/external/v1/manifesto")
@Tag(name = "Manifesto External API", description = "外部連携用Manifesto多言語API")
@RequiredArgsConstructor
@Slf4j
@Validated
@CrossOrigin(origins = {"${sfr.cors.allowed-origins:http://localhost:3000,http://localhost:8080}"})
public class ManifestoExternalControllerSimple {

    private final ManifestoI18nService manifestoI18nService;

    /**
     * 最新Manifestoの取得
     */
    @GetMapping("/current")
    @Operation(
        summary = "最新Manifestoの取得",
        description = "現在アクティブな最新バージョンのManifestoを指定言語で取得します。外部システムでの表示用途に最適化されています。"
    )
    public ResponseEntity<ApiResponse<ManifestoI18nDto>> getCurrentManifesto(
            @Parameter(description = "言語コード (ISO 639-1準拠)", example = "ja")
            @RequestParam(value = "language", defaultValue = "ja")
            @Pattern(regexp = "^[a-z]{2}$", message = "言語コードはISO 639-1形式で入力してください")
            String language) {

        try {
            log.info("External API: getCurrentManifesto called with language={}", language);
            
            ManifestoI18nDto manifestoDto = manifestoI18nService.getCurrentManifestoForExternal(language);
            
            if (manifestoDto == null) {
                return ResponseEntity.ok(ApiResponse.notFound("Manifesto not found"));
            }
            
            return ResponseEntity.ok(ApiResponse.success(manifestoDto, "Manifesto retrieved successfully"));
            
        } catch (Exception e) {
            log.error("External API: Error retrieving current manifesto", e);
            return ResponseEntity.badRequest().body(ApiResponse.internalError("Failed to retrieve manifesto: " + e.getMessage()));
        }
    }

    /**
     * Manifestoの検索
     */
    @GetMapping("/search")
    @Operation(
        summary = "Manifestoの検索",
        description = "キーワードと言語を指定してManifestoの内容を検索します。外部システムでの検索機能統合に使用できます。"
    )
    public ResponseEntity<ApiResponse<List<ManifestoI18nDto.ContentStructureDto>>> searchManifesto(
            @Parameter(description = "検索キーワード", example = "ガバナンス")
            @RequestParam("keyword")
            @Size(min = 1, max = 100, message = "検索キーワードは1-100文字で入力してください")
            String keyword,

            @Parameter(description = "検索対象言語", example = "ja")
            @RequestParam(value = "language", defaultValue = "ja")
            @Pattern(regexp = "^[a-z]{2}$", message = "言語コードはISO 639-1形式で入力してください")
            String language,

            @Parameter(description = "ページ番号 (0から開始)", example = "0")
            @RequestParam(value = "page", defaultValue = "0")
            @Min(value = 0, message = "ページ番号は0以上である必要があります")
            int page,

            @Parameter(description = "1ページあたりの件数", example = "10")
            @RequestParam(value = "size", defaultValue = "10")
            @Min(value = 1, message = "ページサイズは1以上である必要があります")
            @Max(value = 100, message = "ページサイズは100以下である必要があります")
            int size) {

        try {
            log.info("External API: searchManifesto called with keyword={}, language={}, page={}, size={}", 
                    keyword, language, page, size);
            
            Pageable pageable = PageRequest.of(page, size);
            Page<ManifestoI18nDto.ContentStructureDto> searchResults = 
                manifestoI18nService.searchManifestoWithPaging(keyword, language, pageable);
            
            return ResponseEntity.ok(
                    ApiResponse.success(
                            searchResults.getContent(),
                            String.format("Found %d results for '%s'", searchResults.getTotalElements(), keyword),
                            java.util.Map.of(
                                    "totalElements", searchResults.getTotalElements(),
                                    "totalPages", searchResults.getTotalPages(),
                                    "currentPage", page,
                                    "pageSize", size,
                                    "hasNext", searchResults.hasNext(),
                                    "hasPrevious", searchResults.hasPrevious()
                            )
                    )
            );
            
        } catch (Exception e) {
            log.error("External API: Error searching manifesto", e);
            return ResponseEntity.badRequest().body(ApiResponse.internalError("Search failed: " + e.getMessage()));
        }
    }

    /**
     * 利用可能言語一覧の取得
     */
    @GetMapping("/languages")
    @Operation(
        summary = "利用可能言語一覧の取得",
        description = "Manifestoで利用可能な言語コード一覧を取得します。外部システムでの多言語対応UI構築に使用できます。"
    )
    public ResponseEntity<ApiResponse<List<String>>> getAvailableLanguages() {
        try {
            log.info("External API: getAvailableLanguages called");
            
            List<String> languages = manifestoI18nService.getAvailableLanguages();
            
            return ResponseEntity.ok(ApiResponse.success(languages, "Available languages retrieved successfully"));
            
        } catch (Exception e) {
            log.error("External API: Error retrieving available languages", e);
            return ResponseEntity.badRequest().body(ApiResponse.internalError("Failed to retrieve languages: " + e.getMessage()));
        }
    }

    /**
     * Manifestoバージョン一覧の取得
     */
    @GetMapping("/versions")
    @Operation(
        summary = "Manifestoバージョン一覧の取得",
        description = "利用可能なManifestoバージョン一覧を取得します。外部システムでのバージョン管理・履歴表示に使用できます。"
    )
    public ResponseEntity<ApiResponse<List<String>>> getAvailableVersions() {
        try {
            log.info("External API: getAvailableVersions called");
            
            List<String> versions = manifestoI18nService.getAvailableVersions();
            
            return ResponseEntity.ok(ApiResponse.success(versions, "Available versions retrieved successfully"));
            
        } catch (Exception e) {
            log.error("External API: Error retrieving available versions", e);
            return ResponseEntity.badRequest().body(ApiResponse.internalError("Failed to retrieve versions: " + e.getMessage()));
        }
    }
}
