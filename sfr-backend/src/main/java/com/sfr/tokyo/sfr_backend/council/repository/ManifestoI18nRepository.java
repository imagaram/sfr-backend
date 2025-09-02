package com.sfr.tokyo.sfr_backend.council.repository;

import com.sfr.tokyo.sfr_backend.entity.council.ManifestoDocument;
import com.sfr.tokyo.sfr_backend.entity.council.ManifestoSection;
import com.sfr.tokyo.sfr_backend.entity.council.ManifestoContent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * SFR.TOKYO Manifesto i18n対応リポジトリ群
 * 
 * 多言語対応による制度の国際化とUXの包摂性を実現
 * 公開フェーズ3: 国内テスト後の展開予定
 */

/**
 * ManifestoDocumentリポジトリ
 */
@Repository
public interface ManifestoDocumentRepository extends JpaRepository<ManifestoDocument, Long> {

    /**
     * 現在アクティブなManifesto文書を取得
     */
    Optional<ManifestoDocument> findByIsActiveTrue();

    /**
     * バージョン別Manifesto文書を取得
     */
    Optional<ManifestoDocument> findByVersion(String version);

    /**
     * 公開フェーズ別Manifesto文書一覧を取得
     */
    List<ManifestoDocument> findByPhaseOrderByUpdatedAtDesc(String phase);

    /**
     * 指定期間内に更新されたManifesto文書一覧を取得
     */
    @Query("SELECT md FROM ManifestoDocument md WHERE md.updatedAt BETWEEN :startDate AND :endDate ORDER BY md.updatedAt DESC")
    List<ManifestoDocument> findByUpdatedAtBetween(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    /**
     * バージョン番号でソートしたManifesto文書一覧を取得
     */
    @Query("SELECT md FROM ManifestoDocument md ORDER BY " +
           "CAST(SUBSTRING_INDEX(md.version, '.', 1) AS UNSIGNED) DESC, " +
           "CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(md.version, '.', 2), '.', -1) AS UNSIGNED) DESC, " +
           "CAST(SUBSTRING_INDEX(md.version, '.', -1) AS UNSIGNED) DESC")
    List<ManifestoDocument> findAllOrderByVersionDesc();

    /**
     * 最新バージョンのManifesto文書を取得
     */
    @Query("SELECT md FROM ManifestoDocument md WHERE md.version = (" +
           "SELECT MAX(md2.version) FROM ManifestoDocument md2)")
    Optional<ManifestoDocument> findLatestVersion();

    /**
     * 作成者別Manifesto文書数を取得
     */
    @Query("SELECT COUNT(md) FROM ManifestoDocument md WHERE md.authors LIKE %:author%")
    Long countByAuthorsContaining(@Param("author") String author);
}

/**
 * ManifestoSectionリポジトリ
 */
@Repository
public interface ManifestoSectionRepository extends JpaRepository<ManifestoSection, Long> {

    /**
     * Manifestoドキュメント別セクション一覧を表示順で取得
     */
    List<ManifestoSection> findByManifestoDocumentOrderByDisplayOrderAsc(ManifestoDocument manifestoDocument);

    /**
     * Manifestoドキュメント別セクション一覧をページング形式で取得
     */
    Page<ManifestoSection> findByManifestoDocumentOrderByDisplayOrderAsc(ManifestoDocument manifestoDocument, Pageable pageable);

    /**
     * セクションID別検索
     */
    List<ManifestoSection> findBySectionIdOrderByManifestoDocument_VersionDesc(String sectionId);

    /**
     * カテゴリ別セクション一覧を取得
     */
    List<ManifestoSection> findByCategoryOrderByDisplayOrderAsc(String category);

    /**
     * 特定Manifestoドキュメントの特定セクションを取得
     */
    Optional<ManifestoSection> findByManifestoDocumentAndSectionId(ManifestoDocument manifestoDocument, String sectionId);

    /**
     * タグを含むセクション検索
     */
    @Query("SELECT ms FROM ManifestoSection ms WHERE ms.tags LIKE %:tag% ORDER BY ms.displayOrder ASC")
    List<ManifestoSection> findByTagsContaining(@Param("tag") String tag);

    /**
     * 指定期間内に更新されたセクション一覧を取得
     */
    @Query("SELECT ms FROM ManifestoSection ms WHERE ms.lastModified BETWEEN :startDate AND :endDate ORDER BY ms.lastModified DESC")
    List<ManifestoSection> findByLastModifiedBetween(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    /**
     * Manifestoドキュメント別セクション数を取得
     */
    Long countByManifestoDocument(ManifestoDocument manifestoDocument);

    /**
     * カテゴリ別セクション数を取得
     */
    Long countByCategory(String category);
}

/**
 * ManifestoContentリポジトリ
 */
@Repository
public interface ManifestoContentRepository extends JpaRepository<ManifestoContent, Long> {

    /**
     * セクション別・言語別コンテンツを取得
     */
    Optional<ManifestoContent> findByManifestoSectionAndLanguageCode(ManifestoSection manifestoSection, String languageCode);

    /**
     * セクション別全言語コンテンツ一覧を取得
     */
    List<ManifestoContent> findByManifestoSectionOrderByLanguageCodeAsc(ManifestoSection manifestoSection);

    /**
     * 言語別コンテンツ一覧を取得
     */
    List<ManifestoContent> findByLanguageCodeOrderByManifestoSection_DisplayOrderAsc(String languageCode);

    /**
     * 特定Manifestoドキュメントの特定言語のコンテンツ一覧を取得
     */
    @Query("SELECT mc FROM ManifestoContent mc " +
           "JOIN mc.manifestoSection ms " +
           "WHERE ms.manifestoDocument = :manifestoDocument " +
           "AND mc.languageCode = :languageCode " +
           "ORDER BY ms.displayOrder ASC")
    List<ManifestoContent> findByManifestoDocumentAndLanguageCode(
            @Param("manifestoDocument") ManifestoDocument manifestoDocument,
            @Param("languageCode") String languageCode);

    /**
     * タイトル・サマリーでのキーワード検索
     */
    @Query("SELECT mc FROM ManifestoContent mc WHERE " +
           "(mc.title LIKE %:keyword% OR mc.summary LIKE %:keyword%) " +
           "AND mc.languageCode = :languageCode " +
           "ORDER BY mc.manifestoSection.displayOrder ASC")
    List<ManifestoContent> findByKeywordAndLanguageCode(@Param("keyword") String keyword, @Param("languageCode") String languageCode);

    /**
     * 翻訳品質スコア別コンテンツ検索
     */
    @Query("SELECT mc FROM ManifestoContent mc WHERE " +
           "mc.translationQualityScore >= :minScore " +
           "AND mc.languageCode = :languageCode " +
           "ORDER BY mc.translationQualityScore DESC")
    List<ManifestoContent> findByTranslationQualityScoreGreaterThanEqualAndLanguageCode(
            @Param("minScore") Double minScore, @Param("languageCode") String languageCode);

    /**
     * 翻訳ステータス別コンテンツ検索
     */
    List<ManifestoContent> findByTranslationStatusOrderByUpdatedAtDesc(String translationStatus);

    /**
     * 言語別コンテンツ数を取得
     */
    Long countByLanguageCode(String languageCode);

    /**
     * セクション別コンテンツ数を取得
     */
    Long countByManifestoSection(ManifestoSection manifestoSection);

    /**
     * 翻訳未完了コンテンツ一覧を取得
     */
    @Query("SELECT mc FROM ManifestoContent mc WHERE " +
           "mc.translationStatus IS NULL OR mc.translationStatus IN ('translated', 'reviewed') " +
           "ORDER BY mc.updatedAt ASC")
    List<ManifestoContent> findIncompleteTranslations();

    /**
     * 指定期間内に更新されたコンテンツ一覧を取得
     */
    List<ManifestoContent> findByUpdatedAtBetweenOrderByUpdatedAtDesc(Instant startDate, Instant endDate);

    /**
     * 古いコンテンツ（指定日時より前に更新）を取得
     */
    @Query("SELECT mc FROM ManifestoContent mc WHERE mc.updatedAt < :beforeDate ORDER BY mc.updatedAt ASC")
    List<ManifestoContent> findOutdatedContent(@Param("beforeDate") Instant beforeDate);
}
