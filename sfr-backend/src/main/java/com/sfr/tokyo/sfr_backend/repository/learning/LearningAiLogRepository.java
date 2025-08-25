package com.sfr.tokyo.sfr_backend.repository.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningAiLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * AI学習ログリポジトリ
 * AIとのインタラクション履歴の検索・集計機能を提供
 * インデックス最適化のターゲット：user_id, ai_type, created_at
 */
@Repository
public interface LearningAiLogRepository extends JpaRepository<LearningAiLog, Long> {

    // ========== 基本検索 ==========

    /**
     * ユーザー別のログ一覧を取得（ページネーション対応）
     */
    Page<LearningAiLog> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * ユーザー別のログ一覧を取得（指定件数）
     */
    List<LearningAiLog> findTop50ByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * スペース別のログ一覧を取得
     */
    Page<LearningAiLog> findBySpaceIdOrderByCreatedAtDesc(UUID spaceId, Pageable pageable);

    /**
     * セッション別のログ一覧を取得
     */
    List<LearningAiLog> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);

    /**
     * AIタイプ別のログ一覧を取得
     */
    Page<LearningAiLog> findByAiTypeOrderByCreatedAtDesc(LearningAiLog.AiType aiType, Pageable pageable);

    /**
     * インタラクションタイプ別のログ一覧を取得
     */
    Page<LearningAiLog> findByInteractionTypeOrderByCreatedAtDesc(LearningAiLog.InteractionType interactionType,
            Pageable pageable);

    // ========== ユーザー特定検索 ==========

    /**
     * ユーザーとAIタイプでログを検索
     */
    Page<LearningAiLog> findByUserIdAndAiTypeOrderByCreatedAtDesc(UUID userId, LearningAiLog.AiType aiType,
            Pageable pageable);

    /**
     * ユーザーとスペースでログを検索
     */
    Page<LearningAiLog> findByUserIdAndSpaceIdOrderByCreatedAtDesc(UUID userId, UUID spaceId, Pageable pageable);

    /**
     * ユーザーとコンテンツでログを検索
     */
    List<LearningAiLog> findByUserIdAndContentIdOrderByCreatedAtDesc(UUID userId, Long contentId);

    /**
     * ユーザーとクイズでログを検索
     */
    List<LearningAiLog> findByUserIdAndQuizIdOrderByCreatedAtDesc(UUID userId, Long quizId);

    // ========== 期間指定検索 ==========

    /**
     * ユーザーの指定期間内のログを取得
     */
    Page<LearningAiLog> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            UUID userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * スペースの指定期間内のログを取得
     */
    Page<LearningAiLog> findBySpaceIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            UUID spaceId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * AIタイプの指定期間内のログを取得
     */
    Page<LearningAiLog> findByAiTypeAndCreatedAtBetweenOrderByCreatedAtDesc(
            LearningAiLog.AiType aiType, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // ========== エラー・品質関連 ==========

    /**
     * エラーログを取得
     */
    Page<LearningAiLog> findByIsErrorTrueOrderByCreatedAtDesc(Pageable pageable);

    /**
     * ユーザーのエラーログを取得
     */
    List<LearningAiLog> findByUserIdAndIsErrorTrueOrderByCreatedAtDesc(UUID userId);

    /**
     * 高満足度のログを取得
     */
    @Query("SELECT l FROM LearningAiLog l WHERE l.userSatisfaction >= 4 ORDER BY l.createdAt DESC")
    Page<LearningAiLog> findHighSatisfactionLogs(Pageable pageable);

    /**
     * 低満足度のログを取得
     */
    @Query("SELECT l FROM LearningAiLog l WHERE l.userSatisfaction <= 2 ORDER BY l.createdAt DESC")
    Page<LearningAiLog> findLowSatisfactionLogs(Pageable pageable);

    /**
     * 高信頼度のログを取得
     */
    @Query("SELECT l FROM LearningAiLog l WHERE l.confidenceScore >= 0.8 ORDER BY l.createdAt DESC")
    Page<LearningAiLog> findHighConfidenceLogs(Pageable pageable);

    // ========== 統計・集計クエリ ==========

    /**
     * ユーザーのAIタイプ別ログ数を集計
     */
    @Query("SELECT l.aiType, COUNT(l) FROM LearningAiLog l WHERE l.userId = :userId GROUP BY l.aiType")
    List<Object[]> countByUserIdAndAiType(@Param("userId") UUID userId);

    /**
     * ユーザーの月別ログ数を集計
     */
    @Query("SELECT FUNCTION('YEAR', l.createdAt), FUNCTION('MONTH', l.createdAt), COUNT(l) " +
            "FROM LearningAiLog l WHERE l.userId = :userId " +
            "GROUP BY FUNCTION('YEAR', l.createdAt), FUNCTION('MONTH', l.createdAt) " +
            "ORDER BY FUNCTION('YEAR', l.createdAt) DESC, FUNCTION('MONTH', l.createdAt) DESC")
    List<Object[]> countByUserIdGroupByMonth(@Param("userId") UUID userId);

    /**
     * スペースのAIタイプ別ログ数を集計
     */
    @Query("SELECT l.aiType, COUNT(l) FROM LearningAiLog l WHERE l.spaceId = :spaceId GROUP BY l.aiType")
    List<Object[]> countBySpaceIdAndAiType(@Param("spaceId") UUID spaceId);

    /**
     * 全体のAIタイプ別ログ数を集計
     */
    @Query("SELECT l.aiType, COUNT(l) FROM LearningAiLog l GROUP BY l.aiType ORDER BY COUNT(l) DESC")
    List<Object[]> countByAiTypeGlobal();

    /**
     * ユーザーの平均満足度を計算
     */
    @Query("SELECT AVG(l.userSatisfaction) FROM LearningAiLog l WHERE l.userId = :userId AND l.userSatisfaction IS NOT NULL")
    Double getAverageUserSatisfaction(@Param("userId") UUID userId);

    /**
     * ユーザーの平均信頼度を計算
     */
    @Query("SELECT AVG(l.confidenceScore) FROM LearningAiLog l WHERE l.userId = :userId AND l.confidenceScore IS NOT NULL")
    Double getAverageConfidenceScore(@Param("userId") UUID userId);

    /**
     * ユーザーの平均処理時間を計算
     */
    @Query("SELECT AVG(l.processingTimeMs) FROM LearningAiLog l WHERE l.userId = :userId AND l.processingTimeMs IS NOT NULL")
    Double getAverageProcessingTime(@Param("userId") UUID userId);

    /**
     * ユーザーの総トークン使用量を計算
     */
    @Query("SELECT SUM(l.tokensUsed) FROM LearningAiLog l WHERE l.userId = :userId AND l.tokensUsed IS NOT NULL")
    Long getTotalTokensUsed(@Param("userId") UUID userId);

    /**
     * ユーザーのエラー率を計算
     */
    @Query("SELECT " +
            "CAST(SUM(CASE WHEN l.isError = true THEN 1 ELSE 0 END) AS double) / COUNT(l) * 100 " +
            "FROM LearningAiLog l WHERE l.userId = :userId")
    Double getErrorRate(@Param("userId") UUID userId);

    // ========== セッション関連 ==========

    /**
     * ユーザーの最新セッションIDを取得
     */
    @Query("SELECT l.sessionId FROM LearningAiLog l WHERE l.userId = :userId ORDER BY l.createdAt DESC LIMIT 1")
    UUID getLatestSessionId(@Param("userId") UUID userId);

    /**
     * ユーザーのセッション一覧を取得（重複なし）
     */
    @Query("SELECT DISTINCT l.sessionId FROM LearningAiLog l WHERE l.userId = :userId ORDER BY MAX(l.createdAt) DESC")
    Page<UUID> findDistinctSessionIdsByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * セッションの継続時間を計算
     */
    @Query("SELECT MIN(l.createdAt), MAX(l.createdAt) FROM LearningAiLog l WHERE l.sessionId = :sessionId")
    List<Object[]> getSessionDuration(@Param("sessionId") UUID sessionId);

    // ========== アクティビティ関連 ==========

    /**
     * 最近のアクティブユーザーを取得
     */
    @Query("SELECT DISTINCT l.userId FROM LearningAiLog l WHERE l.createdAt >= :since ORDER BY MAX(l.createdAt) DESC")
    Page<UUID> findActiveUsersSince(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * 人気のAIタイプを取得（指定期間）
     */
    @Query("SELECT l.aiType, COUNT(l) FROM LearningAiLog l " +
            "WHERE l.createdAt >= :since " +
            "GROUP BY l.aiType ORDER BY COUNT(l) DESC")
    List<Object[]> findPopularAiTypesSince(@Param("since") LocalDateTime since);

    /**
     * コンテンツ関連のログを取得
     */
    List<LearningAiLog> findByContentIdIsNotNullOrderByCreatedAtDesc();

    /**
     * クイズ関連のログを取得
     */
    List<LearningAiLog> findByQuizIdIsNotNullOrderByCreatedAtDesc();

    // ========== パフォーマンス関連 ==========

    /**
     * 処理時間が長いログを取得
     */
    @Query("SELECT l FROM LearningAiLog l WHERE l.processingTimeMs > :threshold ORDER BY l.processingTimeMs DESC")
    Page<LearningAiLog> findSlowResponseLogs(@Param("threshold") Long threshold, Pageable pageable);

    /**
     * トークン使用量が多いログを取得
     */
    @Query("SELECT l FROM LearningAiLog l WHERE l.tokensUsed > :threshold ORDER BY l.tokensUsed DESC")
    Page<LearningAiLog> findHighTokenUsageLogs(@Param("threshold") Integer threshold, Pageable pageable);
}
