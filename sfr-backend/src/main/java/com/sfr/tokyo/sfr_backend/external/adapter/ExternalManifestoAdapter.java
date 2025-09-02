package com.sfr.tokyo.sfr_backend.external.adapter;

import com.sfr.tokyo.sfr_backend.council.dto.ManifestoI18nDto;
import com.sfr.tokyo.sfr_backend.external.contract.ApiResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 外部システム統合用アダプターインターフェース
 * 
 * 外部システム・SDK・DAOとの統合を標準化するためのアダプターパターンです。
 * このインターフェースを実装することで、様々な外部システムからSFR.TOKYOの
 * 機能を統一された方法で利用できます。
 * 
 * @author SFR.TOKYO Development Team
 * @version 1.0.0
 * @since 2025-09-02
 */
public interface ExternalManifestoAdapter {

    /**
     * 最新Manifestoの取得
     * 
     * @param languageCode 言語コード
     * @return Manifestoデータ
     */
    Optional<ManifestoI18nDto> getCurrentManifesto(String languageCode);

    /**
     * Manifestoの検索
     * 
     * @param keyword 検索キーワード
     * @param languageCode 言語コード
     * @param pageable ページング情報
     * @return 検索結果
     */
    ApiResponse<List<ManifestoI18nDto.ContentStructureDto>> searchManifesto(
            String keyword, String languageCode, Pageable pageable);

    /**
     * 利用可能言語一覧の取得
     * 
     * @return 言語コード一覧
     */
    List<String> getAvailableLanguages();

    /**
     * 利用可能バージョン一覧の取得
     * 
     * @return バージョン一覧
     */
    List<String> getAvailableVersions();

    /**
     * 接続状態の確認
     * 
     * @return 接続可能かどうか
     */
    boolean isConnectionAvailable();

    /**
     * アダプター情報の取得
     * 
     * @return アダプター情報
     */
    AdapterInfo getAdapterInfo();

    /**
     * アダプター情報を保持するクラス
     */
    record AdapterInfo(
            String adapterName,
            String version,
            String description,
            String targetSystem,
            boolean isActive
    ) {}
}
