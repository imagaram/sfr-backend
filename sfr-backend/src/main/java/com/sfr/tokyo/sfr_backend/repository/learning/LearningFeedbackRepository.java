package com.sfr.tokyo.sfr_backend.repository.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningFeedback;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningFeedback.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * LearningFeedback Repository
 * フィードバック管理リポジトリ
 */
@Repository
public interface LearningFeedbackRepository extends JpaRepository<LearningFeedback, Long> {

    // ========== 基本検索系 ==========

    /**
     * 対象別フィードバック検索
     */
    @Query("SELECT f FROM LearningFeedback f WHERE f.targetType = :targetType AND f.targetId = :targetId AND f.deletedAt IS NULL ORDER BY f.createdAt DESC")
    List<LearningFeedback> findByTargetTypeAndTargetId(@Param("targetType") TargetType targetType,
            @Param("targetId") Long targetId);

    /**
     * フィードバック送信者別検索
     */
    @Query("SELECT f FROM LearningFeedback f WHERE f.feedbackGiverId = :feedbackGiverId AND f.deletedAt IS NULL ORDER BY f.createdAt DESC")
    Page<LearningFeedback> findByFeedbackGiverId(@Param("feedbackGiverId") UUID feedbackGiverId, Pageable pageable);

    /**
     * フィードバック受信者別検索
     */
    @Query("SELECT f FROM LearningFeedback f WHERE f.feedbackReceiverId = :feedbackReceiverId AND f.deletedAt IS NULL ORDER BY f.createdAt DESC")
    Page<LearningFeedback> findByFeedbackReceiverId(@Param("feedbackReceiverId") UUID feedbackReceiverId,
            Pageable pageable);

    /**
     * フィードバックタイプ別検索
     */
    @Query("SELECT f FROM LearningFeedback f WHERE f.feedbackType = :feedbackType AND f.deletedAt IS NULL ORDER BY f.createdAt DESC")
    Page<LearningFeedback> findByFeedbackType(@Param("feedbackType") FeedbackType feedbackType, Pageable pageable);

    /**
     * フィードバックカテゴリ別検索
     */
    @Query("SELECT f FROM LearningFeedback f WHERE f.feedbackCategory = :feedbackCategory AND f.deletedAt IS NULL ORDER BY f.createdAt DESC")
    Page<LearningFeedback> findByFeedbackCategory(@Param("feedbackCategory") FeedbackCategory feedbackCategory,
            Pageable pageable);

    /**
     * フィードバックステータス別検索
     */
    @Query("SELECT f FROM LearningFeedback f WHERE f.feedbackStatus = :feedbackStatus AND f.deletedAt IS NULL ORDER BY f.createdAt DESC")
    Page<LearningFeedback> findByFeedbackStatus(@Param("feedbackStatus") FeedbackStatus feedbackStatus,
            Pageable pageable);

    /**
     * 評価値での検索
     */
    @Query("SELECT f FROM LearningFeedback f WHERE f.rating = :rating AND f.deletedAt IS NULL ORDER BY f.createdAt DESC")
    Page<LearningFeedback> findByRating(@Param("rating") Integer rating, Pageable pageable);

    /**
     * 評価値範囲での検索
     */
    @Query("SELECT f FROM LearningFeedback f WHERE f.rating BETWEEN :minRating AND :maxRating AND f.deletedAt IS NULL ORDER BY f.createdAt DESC")
    Page<LearningFeedback> findByRatingBetween(@Param("minRating") Integer minRating,
            @Param("maxRating") Integer maxRating, Pageable pageable);

    // ========== 複合検索系 ==========

    /**
     * 複合条件検索
     */
    @Query("""
            SELECT f FROM LearningFeedback f
            WHERE (:targetType IS NULL OR f.targetType = :targetType)
            AND (:feedbackType IS NULL OR f.feedbackType = :feedbackType)
            AND (:feedbackCategory IS NULL OR f.feedbackCategory = :feedbackCategory)
            AND (:feedbackStatus IS NULL OR f.feedbackStatus = :feedbackStatus)
            AND (:minRating IS NULL OR f.rating >= :minRating)
            AND (:maxRating IS NULL OR f.rating <= :maxRating)
            AND f.deletedAt IS NULL
            ORDER BY f.createdAt DESC
            """)
    Page<LearningFeedback> searchFeedbacks(
            @Param("targetType") TargetType targetType,
            @Param("feedbackType") FeedbackType feedbackType,
            @Param("feedbackCategory") FeedbackCategory feedbackCategory,
            @Param("feedbackStatus") FeedbackStatus feedbackStatus,
            @Param("minRating") Integer minRating,
            @Param("maxRating") Integer maxRating,
            Pageable pageable);

    /**
     * 期間別フィードバック検索
     */
    @Query("SELECT f FROM LearningFeedback f WHERE f.createdAt BETWEEN :startDate AND :endDate AND f.deletedAt IS NULL ORDER BY f.createdAt DESC")
    Page<LearningFeedback> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate, Pageable pageable);

    /**
     * キーワード検索
     */
    @Query("""
            SELECT f FROM LearningFeedback f
            WHERE (LOWER(f.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(f.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(f.tags) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND f.deletedAt IS NULL
            ORDER BY f.createdAt DESC
            """)
    Page<LearningFeedback> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // ========== 統計・集計系 ==========

    /**
     * 対象別フィードバック数カウント
     */
    @Query("SELECT COUNT(f) FROM LearningFeedback f WHERE f.targetType = :targetType AND f.targetId = :targetId AND f.deletedAt IS NULL")
    Long countByTargetTypeAndTargetId(@Param("targetType") TargetType targetType, @Param("targetId") Long targetId);

    /**
     * フィードバックタイプ別統計
     */
    @Query("""
            SELECT f.feedbackType, COUNT(f)
            FROM LearningFeedback f
            WHERE f.deletedAt IS NULL
            GROUP BY f.feedbackType
            """)
    List<Object[]> countByFeedbackType();

    /**
     * フィードバックカテゴリ別統計
     */
    @Query("""
            SELECT f.feedbackCategory, COUNT(f)
            FROM LearningFeedback f
            WHERE f.deletedAt IS NULL
            GROUP BY f.feedbackCategory
            """)
    List<Object[]> countByFeedbackCategory();

    /**
     * 月別フィードバック統計
     */
    @Query("""
            SELECT DATE_FORMAT(f.createdAt, '%Y-%m'), COUNT(f)
            FROM LearningFeedback f
            WHERE f.createdAt >= :fromDate AND f.deletedAt IS NULL
            GROUP BY DATE_FORMAT(f.createdAt, '%Y-%m')
            ORDER BY DATE_FORMAT(f.createdAt, '%Y-%m')
            """)
    List<Object[]> countByMonth(@Param("fromDate") LocalDateTime fromDate);

    /**
     * 平均評価計算
     */
    @Query("SELECT AVG(f.rating) FROM LearningFeedback f WHERE f.targetType = :targetType AND f.targetId = :targetId AND f.deletedAt IS NULL")
    BigDecimal calculateAverageRating(@Param("targetType") TargetType targetType, @Param("targetId") Long targetId);

    /**
     * 評価分布統計
     */
    @Query("""
            SELECT f.rating, COUNT(f)
            FROM LearningFeedback f
            WHERE f.targetType = :targetType AND f.targetId = :targetId AND f.deletedAt IS NULL
            GROUP BY f.rating
            ORDER BY f.rating
            """)
    List<Object[]> getRatingDistribution(@Param("targetType") TargetType targetType, @Param("targetId") Long targetId);

    // ========== スコア・品質系 ==========

    /**
     * 高品質フィードバック検索
     */
    @Query("SELECT f FROM LearningFeedback f WHERE f.qualityScore >= :minScore AND f.deletedAt IS NULL ORDER BY f.qualityScore DESC")
    List<LearningFeedback> findHighQualityFeedbacks(@Param("minScore") BigDecimal minScore);

    /**
     * 有用フィードバック検索
     */
    @Query("SELECT f FROM LearningFeedback f WHERE f.helpfulnessScore >= :minScore AND f.deletedAt IS NULL ORDER BY f.helpfulnessScore DESC")
    List<LearningFeedback> findHelpfulFeedbacks(@Param("minScore") BigDecimal minScore);

    /**
     * 建設的フィードバック検索
     */
    @Query("SELECT f FROM LearningFeedback f WHERE f.constructivenessScore >= :minScore AND f.deletedAt IS NULL ORDER BY f.constructivenessScore DESC")
    List<LearningFeedback> findConstructiveFeedbacks(@Param("minScore") BigDecimal minScore);

    /**
     * 総合スコア上位フィードバック
     */
    @Query("SELECT f FROM LearningFeedback f WHERE f.deletedAt IS NULL ORDER BY f.overallScore DESC")
    List<LearningFeedback> findTopRatedFeedbacks(Pageable pageable);

    // ========== モデレーション系 ==========

    /**
     * モデレーション必要フィードバック
     */
    @Query("SELECT f FROM LearningFeedback f WHERE f.requiresModeration = true AND f.deletedAt IS NULL ORDER BY f.createdAt ASC")
    List<LearningFeedback> findFeedbacksRequiringModeration();

    /**
     * モデレーションステータス別検索
     */
    @Query("SELECT f FROM LearningFeedback f WHERE f.moderationStatus = :moderationStatus AND f.deletedAt IS NULL ORDER BY f.createdAt DESC")
    Page<LearningFeedback> findByModerationStatus(@Param("moderationStatus") ModerationStatus moderationStatus,
            Pageable pageable);

    /**
     * 報告されたフィードバック
     */
    @Query("SELECT f FROM LearningFeedback f WHERE f.reportCount > :threshold AND f.deletedAt IS NULL ORDER BY f.reportCount DESC")
    List<LearningFeedback> findReportedFeedbacks(@Param("threshold") Integer threshold);

    // ========== フォローアップ系 ==========

    /**
     * フォローアップ必要フィードバック
     */
    @Query("SELECT f FROM LearningFeedback f WHERE f.followupRequired = true AND f.followupCompleted = false AND f.deletedAt IS NULL ORDER BY f.createdAt ASC")
    List<LearningFeedback> findFeedbacksRequiringFollowup();

    /**
     * 未回答フィードバック
     */
    @Query("SELECT f FROM LearningFeedback f WHERE f.responseContent IS NULL AND f.feedbackStatus IN ('SUBMITTED', 'ACKNOWLEDGED') AND f.deletedAt IS NULL ORDER BY f.createdAt ASC")
    List<LearningFeedback> findUnansweredFeedbacks();

    /**
     * 期限切れフィードバック
     */
    @Query("SELECT f FROM LearningFeedback f WHERE f.createdAt < :cutoffDate AND f.feedbackStatus IN ('SUBMITTED', 'ACKNOWLEDGED', 'UNDER_REVIEW') AND f.deletedAt IS NULL")
    List<LearningFeedback> findOverdueFeedbacks(@Param("cutoffDate") LocalDateTime cutoffDate);

    // ========== 関連データ系 ==========

    /**
     * 類似フィードバック検索
     */
    @Query("""
            SELECT f FROM LearningFeedback f
            WHERE f.targetType = :targetType
            AND f.feedbackCategory = :feedbackCategory
            AND f.id != :excludeId
            AND f.deletedAt IS NULL
            ORDER BY f.overallScore DESC
            """)
    List<LearningFeedback> findSimilarFeedbacks(
            @Param("targetType") TargetType targetType,
            @Param("feedbackCategory") FeedbackCategory feedbackCategory,
            @Param("excludeId") Long excludeId,
            Pageable pageable);

    /**
     * ユーザーの最近のフィードバック
     */
    @Query("SELECT f FROM LearningFeedback f WHERE f.feedbackGiverId = :userId AND f.deletedAt IS NULL ORDER BY f.createdAt DESC")
    List<LearningFeedback> findRecentFeedbacksByUser(@Param("userId") UUID userId, Pageable pageable);

    // ========== 更新・削除系 ==========

    /**
     * フィードバックスコア更新
     */
    @Modifying
    @Transactional
    @Query("""
            UPDATE LearningFeedback f
            SET f.qualityScore = :qualityScore,
                f.helpfulnessScore = :helpfulnessScore,
                f.constructivenessScore = :constructivenessScore,
                f.overallScore = :overallScore,
                f.updatedAt = CURRENT_TIMESTAMP
            WHERE f.id = :feedbackId
            """)
    void updateFeedbackScores(
            @Param("feedbackId") Long feedbackId,
            @Param("qualityScore") BigDecimal qualityScore,
            @Param("helpfulnessScore") BigDecimal helpfulnessScore,
            @Param("constructivenessScore") BigDecimal constructivenessScore,
            @Param("overallScore") BigDecimal overallScore);

    /**
     * 有用カウント更新
     */
    @Modifying
    @Transactional
    @Query("UPDATE LearningFeedback f SET f.helpfulCount = f.helpfulCount + 1, f.updatedAt = CURRENT_TIMESTAMP WHERE f.id = :feedbackId")
    void incrementHelpfulCount(@Param("feedbackId") Long feedbackId);

    /**
     * 有用でないカウント更新
     */
    @Modifying
    @Transactional
    @Query("UPDATE LearningFeedback f SET f.notHelpfulCount = f.notHelpfulCount + 1, f.updatedAt = CURRENT_TIMESTAMP WHERE f.id = :feedbackId")
    void incrementNotHelpfulCount(@Param("feedbackId") Long feedbackId);

    /**
     * 報告カウント更新
     */
    @Modifying
    @Transactional
    @Query("UPDATE LearningFeedback f SET f.reportCount = f.reportCount + 1, f.updatedAt = CURRENT_TIMESTAMP WHERE f.id = :feedbackId")
    void incrementReportCount(@Param("feedbackId") Long feedbackId);

    /**
     * フィードバック状態更新
     */
    @Modifying
    @Transactional
    @Query("UPDATE LearningFeedback f SET f.feedbackStatus = :status, f.updatedAt = CURRENT_TIMESTAMP WHERE f.id = :feedbackId")
    void updateFeedbackStatus(@Param("feedbackId") Long feedbackId, @Param("status") FeedbackStatus status);

    /**
     * モデレーション状態更新
     */
    @Modifying
    @Transactional
    @Query("""
            UPDATE LearningFeedback f
            SET f.moderationStatus = :moderationStatus,
                f.moderatorId = :moderatorId,
                f.moderationNotes = :moderationNotes,
                f.moderatedAt = CURRENT_TIMESTAMP,
                f.updatedAt = CURRENT_TIMESTAMP
            WHERE f.id = :feedbackId
            """)
    void updateModerationStatus(
            @Param("feedbackId") Long feedbackId,
            @Param("moderationStatus") ModerationStatus moderationStatus,
            @Param("moderatorId") UUID moderatorId,
            @Param("moderationNotes") String moderationNotes);

    /**
     * フォローアップ完了更新
     */
    @Modifying
    @Transactional
    @Query("""
            UPDATE LearningFeedback f
            SET f.followupCompleted = true,
                f.followupNotes = :followupNotes,
                f.updatedAt = CURRENT_TIMESTAMP
            WHERE f.id = :feedbackId
            """)
    void markFollowupCompleted(@Param("feedbackId") Long feedbackId, @Param("followupNotes") String followupNotes);

    /**
     * 論理削除
     */
    @Modifying
    @Transactional
    @Query("UPDATE LearningFeedback f SET f.deletedAt = CURRENT_TIMESTAMP WHERE f.id = :feedbackId")
    void softDeleteFeedback(@Param("feedbackId") Long feedbackId);

    // ========== バッチ処理系 ==========

    /**
     * スコア再計算対象フィードバック
     */
    @Query("SELECT f FROM LearningFeedback f WHERE f.overallScore IS NULL OR f.updatedAt < :cutoffDate AND f.deletedAt IS NULL")
    List<LearningFeedback> findFeedbacksForScoreRecalculation(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * 自動クローズ対象フィードバック
     */
    @Query("""
            SELECT f FROM LearningFeedback f
            WHERE f.feedbackStatus IN ('RESOLVED', 'IMPLEMENTED')
            AND f.acknowledgedAt IS NULL
            AND f.responseGivenAt < :cutoffDate
            AND f.deletedAt IS NULL
            """)
    List<LearningFeedback> findFeedbacksForAutoClose(@Param("cutoffDate") LocalDateTime cutoffDate);

    // ========== カスタム統計クエリ ==========

    /**
     * ユーザー別フィードバック統計
     */
    @Query("""
            SELECT f.feedbackGiverId, COUNT(f), AVG(f.rating), AVG(f.overallScore)
            FROM LearningFeedback f
            WHERE f.deletedAt IS NULL
            GROUP BY f.feedbackGiverId
            """)
    List<Object[]> getUserFeedbackStatistics();

    /**
     * 対象別フィードバック統計
     */
    @Query("""
            SELECT f.targetType, f.targetId, COUNT(f), AVG(f.rating), AVG(f.overallScore)
            FROM LearningFeedback f
            WHERE f.deletedAt IS NULL
            GROUP BY f.targetType, f.targetId
            """)
    List<Object[]> getTargetFeedbackStatistics();

    /**
     * 期間別詳細統計
     */
    @Query("""
            SELECT DATE(f.createdAt),
                   COUNT(f),
                   AVG(f.rating),
                   AVG(f.overallScore),
                   SUM(CASE WHEN f.feedbackType = 'POSITIVE' THEN 1 ELSE 0 END),
                   SUM(CASE WHEN f.feedbackType = 'NEGATIVE' THEN 1 ELSE 0 END),
                   SUM(CASE WHEN f.feedbackType = 'CONSTRUCTIVE' THEN 1 ELSE 0 END)
            FROM LearningFeedback f
            WHERE f.createdAt BETWEEN :startDate AND :endDate
            AND f.deletedAt IS NULL
            GROUP BY DATE(f.createdAt)
            ORDER BY DATE(f.createdAt)
            """)
    List<Object[]> getDetailedStatistics(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
