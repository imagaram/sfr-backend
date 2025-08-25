package com.sfr.tokyo.sfr_backend.repository.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningAnalyticsReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 学習分析レポート リポジトリ
 */
@Repository
public interface LearningAnalyticsReportRepository extends JpaRepository<LearningAnalyticsReport, Long> {

        // ========== 基本検索メソッド ==========

        /**
         * 削除されていないレポートを取得
         */
        @Query("SELECT r FROM LearningAnalyticsReport r WHERE r.deletedAt IS NULL AND r.id = :id")
        Optional<LearningAnalyticsReport> findByIdAndNotDeleted(@Param("id") Long id);

        /**
         * ユーザー別レポート取得
         */
        @Query("SELECT r FROM LearningAnalyticsReport r WHERE r.userId = :userId AND r.deletedAt IS NULL ORDER BY r.generatedAt DESC")
        Page<LearningAnalyticsReport> findByUserId(@Param("userId") UUID userId, Pageable pageable);

        /**
         * スペース別レポート取得
         */
        @Query("SELECT r FROM LearningAnalyticsReport r WHERE r.spaceId = :spaceId AND r.deletedAt IS NULL ORDER BY r.generatedAt DESC")
        Page<LearningAnalyticsReport> findBySpaceId(@Param("spaceId") UUID spaceId, Pageable pageable);

        /**
         * レポートタイプ別取得
         */
        @Query("SELECT r FROM LearningAnalyticsReport r WHERE r.reportType = :reportType AND r.deletedAt IS NULL ORDER BY r.generatedAt DESC")
        Page<LearningAnalyticsReport> findByReportType(
                        @Param("reportType") LearningAnalyticsReport.ReportType reportType,
                        Pageable pageable);

        /**
         * ステータス別取得
         */
        @Query("SELECT r FROM LearningAnalyticsReport r WHERE r.status = :status AND r.deletedAt IS NULL ORDER BY r.generatedAt DESC")
        Page<LearningAnalyticsReport> findByStatus(@Param("status") LearningAnalyticsReport.ReportStatus status,
                        Pageable pageable);

        /**
         * 分析範囲別取得
         */
        @Query("SELECT r FROM LearningAnalyticsReport r WHERE r.analysisScope = :scope AND r.deletedAt IS NULL ORDER BY r.generatedAt DESC")
        Page<LearningAnalyticsReport> findByAnalysisScope(@Param("scope") LearningAnalyticsReport.AnalysisScope scope,
                        Pageable pageable);

        // ========== 高度な検索メソッド ==========

        /**
         * 複合条件検索
         */
        @Query("SELECT r FROM LearningAnalyticsReport r " +
                        "WHERE (:userId IS NULL OR r.userId = :userId) " +
                        "AND (:spaceId IS NULL OR r.spaceId = :spaceId) " +
                        "AND (:reportType IS NULL OR r.reportType = :reportType) " +
                        "AND (:status IS NULL OR r.status = :status) " +
                        "AND (:scope IS NULL OR r.analysisScope = :scope) " +
                        "AND (:language IS NULL OR r.reportLanguage = :language) " +
                        "AND (:isPublic IS NULL OR r.isPublic = :isPublic) " +
                        "AND r.deletedAt IS NULL " +
                        "ORDER BY r.generatedAt DESC")
        Page<LearningAnalyticsReport> findByComplexCriteria(
                        @Param("userId") UUID userId,
                        @Param("spaceId") UUID spaceId,
                        @Param("reportType") LearningAnalyticsReport.ReportType reportType,
                        @Param("status") LearningAnalyticsReport.ReportStatus status,
                        @Param("scope") LearningAnalyticsReport.AnalysisScope scope,
                        @Param("language") String language,
                        @Param("isPublic") Boolean isPublic,
                        Pageable pageable);

        /**
         * キーワード検索
         */
        @Query("SELECT r FROM LearningAnalyticsReport r " +
                        "WHERE (r.reportTitle LIKE %:keyword% " +
                        "OR r.reportSummary LIKE %:keyword% " +
                        "OR r.tags LIKE %:keyword% " +
                        "OR r.keywords LIKE %:keyword%) " +
                        "AND r.deletedAt IS NULL " +
                        "ORDER BY r.generatedAt DESC")
        Page<LearningAnalyticsReport> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

        /**
         * 期間別検索
         */
        @Query("SELECT r FROM LearningAnalyticsReport r " +
                        "WHERE r.generatedAt BETWEEN :startDate AND :endDate " +
                        "AND r.deletedAt IS NULL " +
                        "ORDER BY r.generatedAt DESC")
        Page<LearningAnalyticsReport> findByGeneratedDateRange(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);

        /**
         * 分析対象期間での検索
         */
        @Query("SELECT r FROM LearningAnalyticsReport r " +
                        "WHERE r.analysisStartDate <= :endDate " +
                        "AND r.analysisEndDate >= :startDate " +
                        "AND r.deletedAt IS NULL " +
                        "ORDER BY r.generatedAt DESC")
        Page<LearningAnalyticsReport> findByAnalysisDateOverlap(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);

        /**
         * 公開レポート取得
         */
        @Query("SELECT r FROM LearningAnalyticsReport r " +
                        "WHERE r.isPublic = true " +
                        "AND r.status = 'COMPLETED' " +
                        "AND r.deletedAt IS NULL " +
                        "AND (r.expiresAt IS NULL OR r.expiresAt > CURRENT_TIMESTAMP) " +
                        "ORDER BY r.generatedAt DESC")
        Page<LearningAnalyticsReport> findPublicReports(Pageable pageable);

        /**
         * 共有レポート取得
         */
        @Query("SELECT r FROM LearningAnalyticsReport r " +
                        "WHERE r.isShared = true " +
                        "AND r.status = 'COMPLETED' " +
                        "AND r.deletedAt IS NULL " +
                        "AND (r.expiresAt IS NULL OR r.expiresAt > CURRENT_TIMESTAMP) " +
                        "ORDER BY r.generatedAt DESC")
        Page<LearningAnalyticsReport> findSharedReports(Pageable pageable);

        // ========== AI・品質関連メソッド ==========

        /**
         * 高品質レポート取得
         */
        @Query("SELECT r FROM LearningAnalyticsReport r " +
                        "WHERE r.status = 'COMPLETED' " +
                        "AND r.aiConfidence >= :minConfidence " +
                        "AND r.deletedAt IS NULL " +
                        "ORDER BY r.aiConfidence DESC, r.generatedAt DESC")
        List<LearningAnalyticsReport> findHighQualityReports(@Param("minConfidence") BigDecimal minConfidence,
                        Pageable pageable);

        /**
         * AIモデル別レポート取得
         */
        @Query("SELECT r FROM LearningAnalyticsReport r " +
                        "WHERE r.aiModel = :aiModel " +
                        "AND r.deletedAt IS NULL " +
                        "ORDER BY r.generatedAt DESC")
        Page<LearningAnalyticsReport> findByAiModel(@Param("aiModel") String aiModel, Pageable pageable);

        /**
         * 処理時間による検索
         */
        @Query("SELECT r FROM LearningAnalyticsReport r " +
                        "WHERE r.processingTimeSeconds BETWEEN :minSeconds AND :maxSeconds " +
                        "AND r.status = 'COMPLETED' " +
                        "AND r.deletedAt IS NULL " +
                        "ORDER BY r.processingTimeSeconds ASC")
        List<LearningAnalyticsReport> findByProcessingTimeRange(
                        @Param("minSeconds") Integer minSeconds,
                        @Param("maxSeconds") Integer maxSeconds,
                        Pageable pageable);

        // ========== 人気・活用度関連メソッド ==========

        /**
         * 人気レポート取得（閲覧数順）
         */
        @Query("SELECT r FROM LearningAnalyticsReport r " +
                        "WHERE r.status = 'COMPLETED' " +
                        "AND r.deletedAt IS NULL " +
                        "AND (r.expiresAt IS NULL OR r.expiresAt > CURRENT_TIMESTAMP) " +
                        "ORDER BY r.viewCount DESC, r.downloadCount DESC")
        List<LearningAnalyticsReport> findPopularReports(Pageable pageable);

        /**
         * 最新レポート取得
         */
        @Query("SELECT r FROM LearningAnalyticsReport r " +
                        "WHERE r.status = 'COMPLETED' " +
                        "AND r.deletedAt IS NULL " +
                        "AND (r.expiresAt IS NULL OR r.expiresAt > CURRENT_TIMESTAMP) " +
                        "ORDER BY r.generatedAt DESC")
        List<LearningAnalyticsReport> findRecentReports(Pageable pageable);

        /**
         * よくダウンロードされるレポート取得
         */
        @Query("SELECT r FROM LearningAnalyticsReport r " +
                        "WHERE r.downloadCount > 0 " +
                        "AND r.status = 'COMPLETED' " +
                        "AND r.deletedAt IS NULL " +
                        "ORDER BY r.downloadCount DESC, r.viewCount DESC")
        List<LearningAnalyticsReport> findMostDownloadedReports(Pageable pageable);

        // ========== 統計・分析メソッド ==========

        /**
         * ユーザー別レポート数
         */
        @Query("SELECT COUNT(r) FROM LearningAnalyticsReport r WHERE r.userId = :userId AND r.deletedAt IS NULL")
        Long countByUserId(@Param("userId") UUID userId);

        /**
         * スペース別レポート数
         */
        @Query("SELECT COUNT(r) FROM LearningAnalyticsReport r WHERE r.spaceId = :spaceId AND r.deletedAt IS NULL")
        Long countBySpaceId(@Param("spaceId") UUID spaceId);

        /**
         * レポートタイプ別統計
         */
        @Query("SELECT r.reportType, COUNT(r) FROM LearningAnalyticsReport r " +
                        "WHERE r.deletedAt IS NULL " +
                        "GROUP BY r.reportType " +
                        "ORDER BY COUNT(r) DESC")
        List<Object[]> getReportTypeStatistics();

        /**
         * ステータス別統計
         */
        @Query("SELECT r.status, COUNT(r) FROM LearningAnalyticsReport r " +
                        "WHERE r.deletedAt IS NULL " +
                        "GROUP BY r.status " +
                        "ORDER BY COUNT(r) DESC")
        List<Object[]> getStatusStatistics();

        /**
         * 言語別統計
         */
        @Query("SELECT r.reportLanguage, COUNT(r) FROM LearningAnalyticsReport r " +
                        "WHERE r.deletedAt IS NULL " +
                        "GROUP BY r.reportLanguage " +
                        "ORDER BY COUNT(r) DESC")
        List<Object[]> getLanguageStatistics();

        /**
         * 期間別レポート数
         */
        @Query("SELECT DATE(r.generatedAt), COUNT(r) FROM LearningAnalyticsReport r " +
                        "WHERE r.generatedAt BETWEEN :fromDate AND :toDate " +
                        "AND r.deletedAt IS NULL " +
                        "GROUP BY DATE(r.generatedAt) " +
                        "ORDER BY DATE(r.generatedAt)")
        List<Object[]> getReportCountByDateRange(@Param("fromDate") LocalDateTime fromDate,
                        @Param("toDate") LocalDateTime toDate);

        /**
         * 月別統計
         */
        @Query("SELECT YEAR(r.generatedAt), MONTH(r.generatedAt), COUNT(r), AVG(r.aiConfidence) " +
                        "FROM LearningAnalyticsReport r " +
                        "WHERE r.deletedAt IS NULL " +
                        "GROUP BY YEAR(r.generatedAt), MONTH(r.generatedAt) " +
                        "ORDER BY YEAR(r.generatedAt), MONTH(r.generatedAt)")
        List<Object[]> getMonthlyStatistics();

        /**
         * 全体統計取得
         */
        @Query("SELECT " +
                        "COUNT(r) as totalReports, " +
                        "SUM(CASE WHEN r.status = 'COMPLETED' THEN 1 ELSE 0 END) as completedReports, " +
                        "SUM(CASE WHEN r.status = 'PENDING' THEN 1 ELSE 0 END) as pendingReports, " +
                        "SUM(CASE WHEN r.status = 'FAILED' THEN 1 ELSE 0 END) as failedReports, " +
                        "AVG(r.aiConfidence) as avgConfidence, " +
                        "AVG(r.processingTimeSeconds) as avgProcessingTime, " +
                        "SUM(r.viewCount) as totalViews, " +
                        "SUM(r.downloadCount) as totalDownloads " +
                        "FROM LearningAnalyticsReport r " +
                        "WHERE r.deletedAt IS NULL")
        Map<String, Object> getOverallStatistics();

        // ========== 更新・削除メソッド ==========

        /**
         * 閲覧数増加
         */
        @Modifying
        @Query("UPDATE LearningAnalyticsReport r SET r.viewCount = r.viewCount + 1, r.lastViewedAt = CURRENT_TIMESTAMP WHERE r.id = :id")
        int incrementViewCount(@Param("id") Long id);

        /**
         * ダウンロード数増加
         */
        @Modifying
        @Query("UPDATE LearningAnalyticsReport r SET r.downloadCount = r.downloadCount + 1 WHERE r.id = :id")
        int incrementDownloadCount(@Param("id") Long id);

        /**
         * ステータス更新
         */
        @Modifying
        @Query("UPDATE LearningAnalyticsReport r SET r.status = :status WHERE r.id = :id")
        int updateStatus(@Param("id") Long id, @Param("status") LearningAnalyticsReport.ReportStatus status);

        /**
         * AI信頼度更新
         */
        @Modifying
        @Query("UPDATE LearningAnalyticsReport r SET r.aiConfidence = :confidence WHERE r.id = :id")
        int updateAiConfidence(@Param("id") Long id, @Param("confidence") BigDecimal confidence);

        /**
         * 論理削除
         */
        @Modifying
        @Query("UPDATE LearningAnalyticsReport r SET r.deletedAt = CURRENT_TIMESTAMP WHERE r.id = :id")
        int softDelete(@Param("id") Long id);

        /**
         * 復元
         */
        @Modifying
        @Query("UPDATE LearningAnalyticsReport r SET r.deletedAt = NULL WHERE r.id = :id")
        int restore(@Param("id") Long id);

        /**
         * アーカイブ
         */
        @Modifying
        @Query("UPDATE LearningAnalyticsReport r SET r.status = 'ARCHIVED', r.archivedAt = CURRENT_TIMESTAMP WHERE r.id = :id")
        int archive(@Param("id") Long id);

        // ========== バッチ処理メソッド ==========

        /**
         * 期限切れレポートの検索
         */
        @Query("SELECT r FROM LearningAnalyticsReport r " +
                        "WHERE r.expiresAt < CURRENT_TIMESTAMP " +
                        "AND r.status != 'EXPIRED' " +
                        "AND r.deletedAt IS NULL")
        List<LearningAnalyticsReport> findExpiredReports();

        /**
         * 期限切れレポートのステータス更新
         */
        @Modifying
        @Query("UPDATE LearningAnalyticsReport r SET r.status = 'EXPIRED' " +
                        "WHERE r.expiresAt < CURRENT_TIMESTAMP " +
                        "AND r.status != 'EXPIRED' " +
                        "AND r.deletedAt IS NULL")
        int markExpiredReports();

        /**
         * 古いレポートのアーカイブ
         */
        @Modifying
        @Query("UPDATE LearningAnalyticsReport r SET r.status = 'ARCHIVED', r.archivedAt = CURRENT_TIMESTAMP " +
                        "WHERE r.generatedAt < :cutoffDate " +
                        "AND r.viewCount < :minViewCount " +
                        "AND r.status = 'COMPLETED' " +
                        "AND r.deletedAt IS NULL")
        int archiveOldReports(@Param("cutoffDate") LocalDateTime cutoffDate,
                        @Param("minViewCount") Integer minViewCount);

        /**
         * 失敗レポートのクリーンアップ
         */
        @Modifying
        @Query("UPDATE LearningAnalyticsReport r SET r.deletedAt = CURRENT_TIMESTAMP " +
                        "WHERE r.status = 'FAILED' " +
                        "AND r.generatedAt < :cutoffDate " +
                        "AND r.deletedAt IS NULL")
        int cleanupFailedReports(@Param("cutoffDate") LocalDateTime cutoffDate);

        // ========== 関連レポート検索 ==========

        /**
         * 関連レポート検索（同一ユーザー・類似期間）
         */
        @Query("SELECT r FROM LearningAnalyticsReport r " +
                        "WHERE r.userId = :userId " +
                        "AND r.reportType = :reportType " +
                        "AND r.id != :excludeId " +
                        "AND r.status = 'COMPLETED' " +
                        "AND r.deletedAt IS NULL " +
                        "AND ABS(timestampdiff(DAY, r.analysisStartDate, :analysisDate)) <= 30 " +
                        "ORDER BY ABS(timestampdiff(DAY, r.analysisStartDate, :analysisDate)) ASC")
        List<LearningAnalyticsReport> findRelatedReports(
                        @Param("userId") UUID userId,
                        @Param("reportType") LearningAnalyticsReport.ReportType reportType,
                        @Param("analysisDate") LocalDateTime analysisDate,
                        @Param("excludeId") Long excludeId,
                        Pageable pageable);

        /**
         * 類似レポート検索（同一スペース・タイプ）
         */
        @Query("SELECT r FROM LearningAnalyticsReport r " +
                        "WHERE r.spaceId = :spaceId " +
                        "AND r.reportType = :reportType " +
                        "AND r.analysisScope = :scope " +
                        "AND r.id != :excludeId " +
                        "AND r.status = 'COMPLETED' " +
                        "AND r.deletedAt IS NULL " +
                        "ORDER BY r.generatedAt DESC")
        List<LearningAnalyticsReport> findSimilarReports(
                        @Param("spaceId") UUID spaceId,
                        @Param("reportType") LearningAnalyticsReport.ReportType reportType,
                        @Param("scope") LearningAnalyticsReport.AnalysisScope scope,
                        @Param("excludeId") Long excludeId,
                        Pageable pageable);

        /**
         * ユーザーの最新レポート取得
         */
        @Query("SELECT r FROM LearningAnalyticsReport r " +
                        "WHERE r.userId = :userId " +
                        "AND r.reportType = :reportType " +
                        "AND r.status = 'COMPLETED' " +
                        "AND r.deletedAt IS NULL " +
                        "ORDER BY r.generatedAt DESC")
        Optional<LearningAnalyticsReport> findLatestReportByUserAndType(
                        @Param("userId") UUID userId,
                        @Param("reportType") LearningAnalyticsReport.ReportType reportType);

        /**
         * スペースの最新レポート取得
         */
        @Query("SELECT r FROM LearningAnalyticsReport r " +
                        "WHERE r.spaceId = :spaceId " +
                        "AND r.reportType = :reportType " +
                        "AND r.status = 'COMPLETED' " +
                        "AND r.deletedAt IS NULL " +
                        "ORDER BY r.generatedAt DESC")
        Optional<LearningAnalyticsReport> findLatestReportBySpaceAndType(
                        @Param("spaceId") UUID spaceId,
                        @Param("reportType") LearningAnalyticsReport.ReportType reportType);
}
