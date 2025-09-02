package com.sfr.tokyo.sfr_backend.council.repository;

import com.sfr.tokyo.sfr_backend.entity.council.ManifestoDocument;
import com.sfr.tokyo.sfr_backend.entity.council.ManifestoSection;
import com.sfr.tokyo.sfr_backend.entity.council.ManifestoContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * ManifestoContentリポジトリ
 * 
 * 多言語対応による制度の国際化とUXの包摂性を実現
 * 公開フェーズ3: 国内テスト後の展開予定
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

    /**
     * 利用可能言語一覧を取得（外部API用）
     */
    @Query("SELECT DISTINCT mc.languageCode FROM ManifestoContent mc ORDER BY mc.languageCode ASC")
    List<String> findDistinctLanguages();

    /**
     * キーワード検索（外部API用）
     */
    @Query("SELECT mc FROM ManifestoContent mc " +
           "WHERE mc.languageCode = :languageCode " +
           "AND (mc.title LIKE %:keyword% OR mc.summary LIKE %:keyword%) " +
           "ORDER BY mc.manifestoSection.displayOrder ASC")
    List<ManifestoContent> findByLanguageCodeAndTitleContainingOrSummaryContaining(
            @Param("languageCode") String languageCode, 
            @Param("keyword") String keyword1, 
            @Param("keyword") String keyword2);
}
