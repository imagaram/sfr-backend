package com.sfr.tokyo.sfr_backend.external.contract;

import com.sfr.tokyo.sfr_backend.council.dto.ManifestoI18nDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Manifesto外部API契約インターフェース
 * 
 * 外部システム・SDK・DAOから利用可能なManifesto API の契約定義です。
 * このインターフェースを実装することで、統一されたManifesto API を提供できます。
 * 
 * @author SFR.TOKYO Development Team
 * @version 1.0.0
 * @since 2025-09-02
 */
public interface ManifestoExternalApi {

    /**
     * 最新Manifestoの取得
     * 
     * @param language 言語コード (ISO 639-1準拠)
     * @param sections フィルタ対象セクション（カンマ区切り、オプション）
     * @return 最新Manifestoの情報
     */
    ResponseEntity<ApiResponse<ManifestoI18nDto>> getCurrentManifesto(
            @RequestParam(value = "language", defaultValue = "ja") String language,
            @RequestParam(value = "sections", required = false) String sections
    );

    /**
     * Manifestoの検索
     * 
     * @param keyword 検索キーワード
     * @param language 検索対象言語
     * @param page ページ番号 (0から開始)
     * @param size 1ページあたりの件数
     * @return 検索結果
     */
    ResponseEntity<ApiResponse<List<ManifestoI18nDto.ContentStructureDto>>> searchManifesto(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "language", defaultValue = "ja") String language,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    );

    /**
     * 利用可能言語一覧の取得
     * 
     * @return 利用可能な言語コード一覧
     */
    ResponseEntity<ApiResponse<List<String>>> getAvailableLanguages();

    /**
     * Manifestoバージョン一覧の取得
     * 
     * @return 利用可能なバージョン一覧
     */
    ResponseEntity<ApiResponse<List<String>>> getAvailableVersions();
}
