package com.sfr.tokyo.sfr_backend.repository.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import java.util.UUID;

/**
 * 学習トピック リポジトリ - LearningTopicRepository
 */
@Repository
public interface LearningTopicRepository extends JpaRepository<LearningTopic, Long> {

    // 基本検索

    /**
     * フォーラム別トピック取得
     */
    @Query("SELECT t FROM LearningTopic t WHERE t.forumId = :forumId AND t.deletedAt IS NULL ORDER BY t.isPinned DESC, t.createdAt DESC")
    List<LearningTopic> findByForumId(@Param("forumId") Long forumId);

    /**
     * 作成者別トピック取得
     */
    @Query("SELECT t FROM LearningTopic t WHERE t.creatorId = :creatorId AND t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    List<LearningTopic> findByCreatorId(@Param("creatorId") UUID creatorId);

    /**
     * ステータス別トピック取得
     */
    @Query("SELECT t FROM LearningTopic t WHERE t.topicStatus = :status AND t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    List<LearningTopic> findByTopicStatus(@Param("status") LearningTopic.TopicStatus status);

    /**
     * タイプ別トピック取得
     */
    @Query("SELECT t FROM LearningTopic t WHERE t.topicType = :type AND t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    List<LearningTopic> findByTopicType(@Param("type") LearningTopic.TopicType type);

    /**
     * 優先度別トピック取得
     */
    @Query("SELECT t FROM LearningTopic t WHERE t.priority = :priority AND t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    List<LearningTopic> findByPriority(@Param("priority") LearningTopic.TopicPriority priority);

    /**
     * アクティブトピック取得
     */
    @Query("SELECT t FROM LearningTopic t WHERE t.topicStatus = 'ACTIVE' AND t.deletedAt IS NULL ORDER BY t.isPinned DESC, t.lastActivityAt DESC")
    List<LearningTopic> findActiveTopics();

    /**
     * フォーラム別アクティブトピック取得
     */
    @Query("SELECT t FROM LearningTopic t WHERE t.forumId = :forumId AND t.topicStatus = 'ACTIVE' AND t.deletedAt IS NULL ORDER BY t.isPinned DESC, t.lastActivityAt DESC")
    List<LearningTopic> findActiveTopicsByForum(@Param("forumId") Long forumId);

    // ピン留め・フィーチャー系

    /**
     * ピン留めトピック取得
     */
    @Query("SELECT t FROM LearningTopic t WHERE t.isPinned = true AND t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    List<LearningTopic> findPinnedTopics();

    /**
     * フィーチャードトピック取得
     */
    @Query("SELECT t FROM LearningTopic t WHERE t.isFeatured = true AND t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    List<LearningTopic> findFeaturedTopics();

    /**
     * ロックされたトピック取得
     */
    @Query("SELECT t FROM LearningTopic t WHERE t.isLocked = true AND t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    List<LearningTopic> findLockedTopics();

    /**
     * お知らせトピック取得
     */
    @Query("SELECT t FROM LearningTopic t WHERE t.isAnnouncement = true AND t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    List<LearningTopic> findAnnouncementTopics();

    // スコア・人気度系

    /**
     * 人気トピック取得
     */
    @Query("SELECT t FROM LearningTopic t WHERE t.deletedAt IS NULL ORDER BY t.popularityScore DESC, t.viewCount DESC LIMIT 20")
    List<LearningTopic> findPopularTopics();

    /**
     * 高品質トピック取得
     */
    @Query("SELECT t FROM LearningTopic t WHERE t.qualityScore >= :minScore AND t.deletedAt IS NULL ORDER BY t.qualityScore DESC")
    List<LearningTopic> findHighQualityTopics(@Param("minScore") BigDecimal minScore);

    /**
     * 高アクティビティトピック取得
     */
    @Query("SELECT t FROM LearningTopic t WHERE t.activityScore >= :minScore AND t.deletedAt IS NULL ORDER BY t.activityScore DESC")
    List<LearningTopic> findHighActivityTopics(@Param("minScore") BigDecimal minScore);

    /**
     * トレンドトピック取得（最近のアクティビティが高い）
     */
    @Query("SELECT t FROM LearningTopic t WHERE t.lastActivityAt >= :since AND t.deletedAt IS NULL ORDER BY t.activityScore DESC, t.popularityScore DESC LIMIT 20")
    List<LearningTopic> findTrendingTopics(@Param("since") LocalDateTime since);

    // 時間ベース検索

    /**
     * 最近作成されたトピック取得
     */
    @Query("SELECT t FROM LearningTopic t WHERE t.createdAt >= :since AND t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    List<LearningTopic> findRecentTopics(@Param("since") LocalDateTime since);

    /**
     * 最近アクティブなトピック取得
     */
    @Query("SELECT t FROM LearningTopic t WHERE t.lastActivityAt >= :since AND t.deletedAt IS NULL ORDER BY t.lastActivityAt DESC")
    List<LearningTopic> findRecentlyActiveTopics(@Param("since") LocalDateTime since);

    /**
     * 最近解決されたトピック取得
     */
    @Query("SELECT t FROM LearningTopic t WHERE t.isSolved = true AND t.bestAnswerSelectedAt >= :since AND t.deletedAt IS NULL ORDER BY t.bestAnswerSelectedAt DESC")
    List<LearningTopic> findRecentlySolvedTopics(@Param("since") LocalDateTime since);

    // 質問・解決系

    /**
     * 未解決の質問取得
     */
    @Query("SELECT t FROM LearningTopic t WHERE t.topicType = 'QUESTION' AND t.isSolved = false AND t.topicStatus = 'ACTIVE' AND t.deletedAt IS NULL ORDER BY t.priority DESC, t.createdAt ASC")
    List<LearningTopic> findUnsolvedQuestions();

    /**
     * 解決済みの質問取得
     */
    @Query("SELECT t FROM LearningTopic t WHERE t.topicType = 'QUESTION' AND t.isSolved = true AND t.deletedAt IS NULL ORDER BY t.bestAnswerSelectedAt DESC")
    List<LearningTopic> findSolvedQuestions();

    /**
     * 特定期間の解決率取得
     */
    @Query("SELECT " +
            "(SELECT COUNT(t1) FROM LearningTopic t1 WHERE t1.topicType = 'QUESTION' AND t1.isSolved = true AND t1.createdAt BETWEEN :startDate AND :endDate) * 100.0 / "
            +
            "NULLIF((SELECT COUNT(t2) FROM LearningTopic t2 WHERE t2.topicType = 'QUESTION' AND t2.createdAt BETWEEN :startDate AND :endDate), 0)")
    Double calculateSolutionRate(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // 検索機能

    /**
     * キーワード検索
     */
    @Query("SELECT t FROM LearningTopic t WHERE " +
            "(LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "t.deletedAt IS NULL ORDER BY t.overallScore DESC, t.createdAt DESC")
    List<LearningTopic> searchByKeyword(@Param("keyword") String keyword);

    /**
     * タグ検索
     */
    @Query("SELECT t FROM LearningTopic t WHERE " +
            "LOWER(t.tags) LIKE LOWER(CONCAT('%', :tag, '%')) AND " +
            "t.deletedAt IS NULL ORDER BY t.overallScore DESC, t.createdAt DESC")
    List<LearningTopic> searchByTag(@Param("tag") String tag);

    /**
     * 複合検索（フォーラム、タイプ、ステータス）
     */
    @Query("SELECT t FROM LearningTopic t WHERE " +
            "(:forumId IS NULL OR t.forumId = :forumId) AND " +
            "(:type IS NULL OR t.topicType = :type) AND " +
            "(:status IS NULL OR t.topicStatus = :status) AND " +
            "t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    List<LearningTopic> searchTopics(@Param("forumId") Long forumId,
            @Param("type") LearningTopic.TopicType type,
            @Param("status") LearningTopic.TopicStatus status);

    // モデレーション系

    /**
     * モデレーション待ちトピック取得
     */
    @Query("SELECT t FROM LearningTopic t WHERE t.requiresModeration = true AND t.moderationStatus = 'PENDING' AND t.deletedAt IS NULL ORDER BY t.createdAt ASC")
    List<LearningTopic> findTopicsRequiringModeration();

    /**
     * モデレーター別処理済みトピック取得
     */
    @Query("SELECT t FROM LearningTopic t WHERE t.moderatorId = :moderatorId AND t.moderatedAt >= :since AND t.deletedAt IS NULL ORDER BY t.moderatedAt DESC")
    List<LearningTopic> findModeratedTopics(@Param("moderatorId") UUID moderatorId,
            @Param("since") LocalDateTime since);

    /**
     * フラグ付きトピック取得
     */
    @Query("SELECT t FROM LearningTopic t WHERE t.moderationStatus = 'FLAGGED' AND t.deletedAt IS NULL ORDER BY t.moderatedAt DESC")
    List<LearningTopic> findFlaggedTopics();

    // 自動処理対象

    /**
     * 自動クローズ対象トピック取得
     */
    @Query("SELECT t FROM LearningTopic t WHERE " +
            "t.autoCloseAt IS NOT NULL AND t.autoCloseAt <= :now AND " +
            "t.topicStatus = 'ACTIVE' AND t.isPinned = false AND t.isFeatured = false AND " +
            "t.deletedAt IS NULL")
    List<LearningTopic> findTopicsForAutoClose(@Param("now") LocalDateTime now);

    /**
     * 非アクティブトピック取得（指定日数以上アクティビティなし）
     */
    @Query("SELECT t FROM LearningTopic t WHERE " +
            "t.lastActivityAt < :threshold AND t.topicStatus = 'ACTIVE' AND " +
            "t.isPinned = false AND t.isFeatured = false AND t.deletedAt IS NULL")
    List<LearningTopic> findInactiveTopics(@Param("threshold") LocalDateTime threshold);

    // 統計・カウント系

    /**
     * ステータス別トピック数カウント
     */
    @Query("SELECT COUNT(t) FROM LearningTopic t WHERE t.topicStatus = :status AND t.deletedAt IS NULL")
    Long countByStatus(@Param("status") LearningTopic.TopicStatus status);

    /**
     * タイプ別トピック数カウント
     */
    @Query("SELECT COUNT(t) FROM LearningTopic t WHERE t.topicType = :type AND t.deletedAt IS NULL")
    Long countByType(@Param("type") LearningTopic.TopicType type);

    /**
     * 期間別トピック数カウント
     */
    @Query("SELECT COUNT(t) FROM LearningTopic t WHERE t.createdAt BETWEEN :startDate AND :endDate AND t.deletedAt IS NULL")
    Long countByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // 統計情報取得

    /**
     * 全体統計取得
     */
    @Query("SELECT " +
            "COUNT(t) as totalTopics, " +
            "COUNT(CASE WHEN t.topicStatus = 'ACTIVE' THEN 1 END) as activeTopics, " +
            "COUNT(CASE WHEN t.topicType = 'QUESTION' AND t.isSolved = true THEN 1 END) as solvedQuestions, " +
            "COUNT(CASE WHEN t.topicType = 'QUESTION' AND t.isSolved = false THEN 1 END) as unsolvedQuestions, " +
            "AVG(t.viewCount) as avgViewCount, " +
            "AVG(t.commentCount) as avgCommentCount, " +
            "AVG(t.likeCount) as avgLikeCount " +
            "FROM LearningTopic t WHERE t.deletedAt IS NULL")
    Object[] getOverallStatistics();

    /**
     * フォーラム別統計取得
     */
    @Query("SELECT " +
            "COUNT(t) as totalTopics, " +
            "COUNT(CASE WHEN t.topicStatus = 'ACTIVE' THEN 1 END) as activeTopics, " +
            "AVG(t.viewCount) as avgViewCount, " +
            "AVG(t.commentCount) as avgCommentCount, " +
            "MAX(t.lastActivityAt) as lastActivity " +
            "FROM LearningTopic t WHERE t.forumId = :forumId AND t.deletedAt IS NULL")
    Object[] getForumStatistics(@Param("forumId") Long forumId);

    /**
     * 期間別統計取得
     */
    @Query("SELECT " +
            "COUNT(t) as totalTopics, " +
            "COUNT(CASE WHEN t.topicStatus = 'ACTIVE' THEN 1 END) as activeTopics, " +
            "COUNT(CASE WHEN t.isSolved = true THEN 1 END) as solvedTopics, " +
            "AVG(t.overallScore) as avgScore " +
            "FROM LearningTopic t WHERE t.createdAt BETWEEN :startDate AND :endDate AND t.deletedAt IS NULL")
    Object[] getPeriodStatistics(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // 更新系クエリ

    /**
     * 閲覧数増加
     */
    @Modifying
    @Query("UPDATE LearningTopic t SET t.viewCount = t.viewCount + 1, t.updatedAt = CURRENT_TIMESTAMP WHERE t.id = :id")
    int incrementViewCount(@Param("id") Long id);

    /**
     * コメント数増加
     */
    @Modifying
    @Query("UPDATE LearningTopic t SET t.commentCount = t.commentCount + 1, t.updatedAt = CURRENT_TIMESTAMP WHERE t.id = :id")
    int incrementCommentCount(@Param("id") Long id);

    /**
     * コメント数減少
     */
    @Modifying
    @Query("UPDATE LearningTopic t SET t.commentCount = GREATEST(t.commentCount - 1, 0), t.updatedAt = CURRENT_TIMESTAMP WHERE t.id = :id")
    int decrementCommentCount(@Param("id") Long id);

    /**
     * ブックマーク数増加
     */
    @Modifying
    @Query("UPDATE LearningTopic t SET t.bookmarkCount = t.bookmarkCount + 1, t.updatedAt = CURRENT_TIMESTAMP WHERE t.id = :id")
    int incrementBookmarkCount(@Param("id") Long id);

    /**
     * ブックマーク数減少
     */
    @Modifying
    @Query("UPDATE LearningTopic t SET t.bookmarkCount = GREATEST(t.bookmarkCount - 1, 0), t.updatedAt = CURRENT_TIMESTAMP WHERE t.id = :id")
    int decrementBookmarkCount(@Param("id") Long id);

    /**
     * シェア数増加
     */
    @Modifying
    @Query("UPDATE LearningTopic t SET t.shareCount = t.shareCount + 1, t.updatedAt = CURRENT_TIMESTAMP WHERE t.id = :id")
    int incrementShareCount(@Param("id") Long id);

    /**
     * 最後のアクティビティ更新
     */
    @Modifying
    @Query("UPDATE LearningTopic t SET " +
            "t.lastActivityAt = :activityAt, " +
            "t.lastActivityUserId = :userId, " +
            "t.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE t.id = :id")
    int updateLastActivity(@Param("id") Long id,
            @Param("activityAt") LocalDateTime activityAt,
            @Param("userId") UUID userId);

    /**
     * 最後のコメント更新
     */
    @Modifying
    @Query("UPDATE LearningTopic t SET " +
            "t.lastCommentId = :commentId, " +
            "t.lastCommentAt = :commentAt, " +
            "t.lastCommentUserId = :userId, " +
            "t.lastActivityAt = :commentAt, " +
            "t.lastActivityUserId = :userId, " +
            "t.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE t.id = :id")
    int updateLastComment(@Param("id") Long id,
            @Param("commentId") Long commentId,
            @Param("commentAt") LocalDateTime commentAt,
            @Param("userId") UUID userId);

    /**
     * ベストアンサー設定
     */
    @Modifying
    @Query("UPDATE LearningTopic t SET " +
            "t.bestAnswerId = :answerId, " +
            "t.bestAnswerSelectedAt = :selectedAt, " +
            "t.bestAnswerSelectedBy = :selectedBy, " +
            "t.isSolved = true, " +
            "t.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE t.id = :id")
    int setBestAnswer(@Param("id") Long id,
            @Param("answerId") Long answerId,
            @Param("selectedAt") LocalDateTime selectedAt,
            @Param("selectedBy") UUID selectedBy);

    /**
     * ベストアンサー解除
     */
    @Modifying
    @Query("UPDATE LearningTopic t SET " +
            "t.bestAnswerId = NULL, " +
            "t.bestAnswerSelectedAt = NULL, " +
            "t.bestAnswerSelectedBy = NULL, " +
            "t.isSolved = false, " +
            "t.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE t.id = :id")
    int clearBestAnswer(@Param("id") Long id);

    /**
     * スコア更新
     */
    @Modifying
    @Query("UPDATE LearningTopic t SET " +
            "t.activityScore = :activityScore, " +
            "t.qualityScore = :qualityScore, " +
            "t.popularityScore = :popularityScore, " +
            "t.overallScore = :overallScore, " +
            "t.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE t.id = :id")
    int updateScores(@Param("id") Long id,
            @Param("activityScore") BigDecimal activityScore,
            @Param("qualityScore") BigDecimal qualityScore,
            @Param("popularityScore") BigDecimal popularityScore,
            @Param("overallScore") BigDecimal overallScore);

    /**
     * 一括スコア再計算対象取得
     */
    @Query("SELECT t FROM LearningTopic t WHERE t.deletedAt IS NULL")
    List<LearningTopic> findAllForScoreRecalculation();

    /**
     * ピン留め設定
     */
    @Modifying
    @Query("UPDATE LearningTopic t SET t.isPinned = :pinned, t.updatedAt = CURRENT_TIMESTAMP WHERE t.id = :id")
    int updatePinStatus(@Param("id") Long id, @Param("pinned") boolean pinned);

    /**
     * ロック設定
     */
    @Modifying
    @Query("UPDATE LearningTopic t SET t.isLocked = :locked, t.updatedAt = CURRENT_TIMESTAMP WHERE t.id = :id")
    int updateLockStatus(@Param("id") Long id, @Param("locked") boolean locked);

    /**
     * フィーチャー設定
     */
    @Modifying
    @Query("UPDATE LearningTopic t SET t.isFeatured = :featured, t.updatedAt = CURRENT_TIMESTAMP WHERE t.id = :id")
    int updateFeatureStatus(@Param("id") Long id, @Param("featured") boolean featured);

    /**
     * ステータス更新
     */
    @Modifying
    @Query("UPDATE LearningTopic t SET t.topicStatus = :status, t.updatedAt = CURRENT_TIMESTAMP WHERE t.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") LearningTopic.TopicStatus status);

    /**
     * 削除マーク（論理削除）
     */
    @Modifying
    @Query("UPDATE LearningTopic t SET t.deletedAt = CURRENT_TIMESTAMP WHERE t.id = :id")
    int markAsDeleted(@Param("id") Long id);

    // 特殊検索

    /**
     * 類似トピック検索（タイトルベース）
     */
    @Query("SELECT t FROM LearningTopic t WHERE " +
            "t.id != :excludeId AND " +
            "LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%')) AND " +
            "t.topicType = :type AND t.deletedAt IS NULL " +
            "ORDER BY t.overallScore DESC LIMIT 5")
    List<LearningTopic> findSimilarTopics(@Param("excludeId") Long excludeId,
            @Param("title") String title,
            @Param("type") LearningTopic.TopicType type);

    /**
     * ユーザーの関心トピック取得（過去のアクティビティベース）
     */
    @Query("SELECT t FROM LearningTopic t WHERE " +
            "t.forumId IN (SELECT t2.forumId FROM LearningTopic t2 WHERE t2.creatorId = :userId OR t2.lastActivityUserId = :userId) AND "
            +
            "t.creatorId != :userId AND t.topicStatus = 'ACTIVE' AND t.deletedAt IS NULL " +
            "ORDER BY t.lastActivityAt DESC LIMIT 10")
    List<LearningTopic> findTopicsOfInterest(@Param("userId") UUID userId);

    /**
     * 解決に時間がかかっているトピック取得
     */
    @Query("SELECT t FROM LearningTopic t WHERE " +
            "t.topicType = 'QUESTION' AND t.isSolved = false AND " +
            "t.createdAt < :threshold AND t.topicStatus = 'ACTIVE' AND " +
            "t.deletedAt IS NULL ORDER BY t.createdAt ASC")
    List<LearningTopic> findLongUnsolvedQuestions(@Param("threshold") LocalDateTime threshold);

    // ========== Controller用追加メソッド ==========

    /**
     * フォーラム別トピック一覧（Page）
     */
    @Query("SELECT t FROM LearningTopic t WHERE t.forumId = :forumId AND t.deletedAt IS NULL ORDER BY t.lastActivityAt DESC")
    org.springframework.data.domain.Page<LearningTopic> findByForumIdOrderByLastActivityAtDesc(
            @Param("forumId") Long forumId, org.springframework.data.domain.Pageable pageable);

    /**
     * ユーザー別トピック一覧（Page）
     */
    @Query("SELECT t FROM LearningTopic t WHERE t.creatorId = :userId AND t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    org.springframework.data.domain.Page<LearningTopic> findByCreatorIdOrderByCreatedAtDesc(
            @Param("userId") UUID userId, org.springframework.data.domain.Pageable pageable);

    /**
     * ピン留めトピック取得
     */
    @Query("SELECT t FROM LearningTopic t WHERE t.forumId = :forumId AND t.isPinned = true AND t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    List<LearningTopic> findPinnedByForumId(@Param("forumId") Long forumId);

    /**
     * 人気トピック取得
     */
    @Query("SELECT t FROM LearningTopic t WHERE t.forumId = :forumId AND t.deletedAt IS NULL ORDER BY t.popularityScore DESC, t.likeCount DESC")
    List<LearningTopic> findPopularByForumId(@Param("forumId") Long forumId,
            org.springframework.data.domain.Pageable pageable);

    /**
     * 最新トピック取得
     */
    @Query("SELECT t FROM LearningTopic t WHERE t.forumId = :forumId AND t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    List<LearningTopic> findByForumIdOrderByCreatedAtDesc(@Param("forumId") Long forumId,
            org.springframework.data.domain.Pageable pageable);

    /**
     * 解決済みトピック取得
     */
    @Query("SELECT t FROM LearningTopic t WHERE t.forumId = :forumId AND t.isSolved = true AND t.deletedAt IS NULL ORDER BY t.updatedAt DESC")
    org.springframework.data.domain.Page<LearningTopic> findSolvedByForumId(@Param("forumId") Long forumId,
            org.springframework.data.domain.Pageable pageable);

    /**
     * 未解決トピック取得
     */
    @Query("SELECT t FROM LearningTopic t WHERE t.forumId = :forumId AND t.isSolved = false AND t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    org.springframework.data.domain.Page<LearningTopic> findUnsolvedByForumId(@Param("forumId") Long forumId,
            org.springframework.data.domain.Pageable pageable);

    /**
     * いいね数増加
     */
    @Modifying
    @Query("UPDATE LearningTopic t SET t.likeCount = t.likeCount + 1, t.updatedAt = CURRENT_TIMESTAMP WHERE t.id = :id")
    int incrementLikeCount(@Param("id") Long id);

    /**
     * いいね数減少
     */
    @Modifying
    @Query("UPDATE LearningTopic t SET t.likeCount = t.likeCount - 1, t.updatedAt = CURRENT_TIMESTAMP WHERE t.id = :id AND t.likeCount > 0")
    int decrementLikeCount(@Param("id") Long id);

    /**
     * 解決済み設定
     */
    @Modifying
    @Query("UPDATE LearningTopic t SET t.isSolved = :solved, t.updatedAt = CURRENT_TIMESTAMP WHERE t.id = :id")
    int updateSolvedStatus(@Param("id") Long id, @Param("solved") boolean solved);

    /**
     * 品質スコア更新
     */
    @Modifying
    @Query("UPDATE LearningTopic t SET t.qualityScore = :score, t.updatedAt = CURRENT_TIMESTAMP WHERE t.id = :id")
    int updateQualityScore(@Param("id") Long id, @Param("score") BigDecimal score);

    /**
     * 人気度スコア更新
     */
    @Modifying
    @Query("UPDATE LearningTopic t SET t.popularityScore = :score, t.updatedAt = CURRENT_TIMESTAMP WHERE t.id = :id")
    int updatePopularityScore(@Param("id") Long id, @Param("score") BigDecimal score);

    /**
     * モデレーション待ちトピック取得
     */
    @Query("SELECT t FROM LearningTopic t WHERE t.moderationStatus = 'PENDING' AND t.deletedAt IS NULL ORDER BY t.createdAt ASC")
    org.springframework.data.domain.Page<LearningTopic> findPendingModeration(
            org.springframework.data.domain.Pageable pageable);

    /**
     * モデレーションステータス更新
     */
    @Modifying
    @Query("UPDATE LearningTopic t SET t.moderationStatus = :status, t.moderatorId = :moderatorId, t.moderatedAt = CURRENT_TIMESTAMP, t.updatedAt = CURRENT_TIMESTAMP WHERE t.id = :id")
    int updateModerationStatus(@Param("id") Long id, @Param("status") LearningTopic.ModerationStatus status,
            @Param("moderatorId") UUID moderatorId);

    /**
     * トピックステータス更新
     */
    @Modifying
    @Query("UPDATE LearningTopic t SET t.topicStatus = :status, t.updatedAt = CURRENT_TIMESTAMP WHERE t.id = :id")
    int updateTopicStatus(@Param("id") Long id, @Param("status") LearningTopic.TopicStatus status);

    /**
     * トピック検索（複数条件）
     */
    @Query("SELECT t FROM LearningTopic t WHERE " +
            "(:forumId IS NULL OR t.forumId = :forumId) AND " +
            "(:keyword IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND "
            +
            "(:status IS NULL OR t.topicStatus = :status) AND " +
            "(:priority IS NULL OR t.priority = :priority) AND " +
            "(:creatorId IS NULL OR t.creatorId = :creatorId) AND " +
            "t.deletedAt IS NULL ORDER BY t.lastActivityAt DESC")
    org.springframework.data.domain.Page<LearningTopic> searchTopics(
            @Param("forumId") Long forumId,
            @Param("keyword") String keyword,
            @Param("status") LearningTopic.TopicStatus status,
            @Param("priority") LearningTopic.TopicPriority priority,
            @Param("creatorId") UUID creatorId,
            org.springframework.data.domain.Pageable pageable);

    /**
     * コンテンツ検索
     */
    @Query("SELECT t FROM LearningTopic t WHERE t.forumId = :forumId AND " +
            "(LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND "
            +
            "t.deletedAt IS NULL ORDER BY t.lastActivityAt DESC")
    org.springframework.data.domain.Page<LearningTopic> searchByContent(@Param("forumId") Long forumId,
            @Param("keyword") String keyword, org.springframework.data.domain.Pageable pageable);

    /**
     * フォーラム別トピック数
     */
    @Query("SELECT COUNT(t) FROM LearningTopic t WHERE t.forumId = :forumId AND t.deletedAt IS NULL")
    Long countByForumId(@Param("forumId") Long forumId);

    /**
     * ユーザー別トピック数
     */
    @Query("SELECT COUNT(t) FROM LearningTopic t WHERE t.creatorId = :userId AND t.deletedAt IS NULL")
    Long countByCreatorId(@Param("userId") UUID userId);

    /**
     * 期間別トピック統計
     */
    @Query("SELECT CAST(t.createdAt AS DATE) as date, COUNT(t) as count FROM LearningTopic t WHERE " +
            "t.createdAt BETWEEN :fromDate AND :toDate AND t.deletedAt IS NULL GROUP BY CAST(t.createdAt AS DATE) ORDER BY date")
    List<java.util.Map<String, Object>> getTopicCountByDateRange(@Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);

    /**
     * フォーラム統計（引数なし版）
     */
    @Query("SELECT t.forumId, COUNT(t) as topicCount, " +
            "SUM(CASE WHEN t.topicStatus = 'ACTIVE' THEN 1 ELSE 0 END) as activeCount, " +
            "AVG(t.qualityScore) as avgQuality FROM LearningTopic t WHERE t.deletedAt IS NULL " +
            "GROUP BY t.forumId ORDER BY topicCount DESC")
    List<java.util.Map<String, Object>> getForumStatistics();

    /**
     * 関連トピック取得
     */
    @Query("SELECT t FROM LearningTopic t WHERE t.id != :topicId AND t.forumId = " +
            "(SELECT t2.forumId FROM LearningTopic t2 WHERE t2.id = :topicId) AND " +
            "t.deletedAt IS NULL ORDER BY t.qualityScore DESC, t.likeCount DESC")
    List<LearningTopic> findRelatedTopics(@Param("topicId") Long topicId,
            org.springframework.data.domain.Pageable pageable);

    /**
     * アクティブな討論取得
     */
    @Query("SELECT t FROM LearningTopic t WHERE t.forumId = :forumId AND " +
            "t.lastActivityAt > :sinceDate AND t.topicStatus = 'ACTIVE' AND " +
            "t.deletedAt IS NULL ORDER BY t.lastActivityAt DESC")
    List<LearningTopic> findActiveDiscussions(@Param("forumId") Long forumId,
            @Param("sinceDate") LocalDateTime sinceDate);

    /**
     * 古いトピックのアーカイブ
     */
    @Modifying
    @Query("UPDATE LearningTopic t SET t.topicStatus = 'ARCHIVED', t.updatedAt = CURRENT_TIMESTAMP WHERE " +
            "t.lastActivityAt < :cutoffDate AND t.topicStatus = 'ACTIVE' AND t.deletedAt IS NULL")
    int archiveOldTopics(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * 非アクティブトピックのマーク
     */
    @Modifying
    @Query("UPDATE LearningTopic t SET t.topicStatus = 'INACTIVE', t.updatedAt = CURRENT_TIMESTAMP WHERE " +
            "t.lastActivityAt < :cutoffDate AND t.topicStatus = 'ACTIVE' AND t.deletedAt IS NULL")
    int markInactiveTopics(@Param("cutoffDate") LocalDateTime cutoffDate);
}
