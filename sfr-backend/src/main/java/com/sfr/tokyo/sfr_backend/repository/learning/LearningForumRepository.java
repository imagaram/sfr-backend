package com.sfr.tokyo.sfr_backend.repository.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningForum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 学習フォーラム リポジトリ - LearningForumRepository
 */
@Repository
public interface LearningForumRepository extends JpaRepository<LearningForum, Long> {

    /**
     * スペース別フォーラム取得
     */
    List<LearningForum> findBySpaceIdOrderByIsPinnedDescLastActivityAtDesc(Long spaceId);

    /**
     * 作成者別フォーラム取得
     */
    List<LearningForum> findByCreatorIdOrderByCreatedAtDesc(UUID creatorId);

    /**
     * カテゴリ別フォーラム取得
     */
    List<LearningForum> findByForumCategoryOrderByPopularityScoreDesc(LearningForum.ForumCategory forumCategory);

    /**
     * ステータス別フォーラム取得
     */
    List<LearningForum> findByForumStatusOrderByLastActivityAtDesc(LearningForum.ForumStatus forumStatus);

    /**
     * 可視性レベル別フォーラム取得
     */
    List<LearningForum> findByVisibilityLevelOrderByCreatedAtDesc(LearningForum.VisibilityLevel visibilityLevel);

    /**
     * アクティブフォーラム取得
     */
    @Query("SELECT lf FROM LearningForum lf WHERE lf.forumStatus = 'ACTIVE' AND lf.isLocked = false AND lf.isArchived = false ORDER BY lf.lastActivityAt DESC")
    List<LearningForum> findActiveForums();

    /**
     * ピン留めフォーラム取得
     */
    List<LearningForum> findByIsPinnedTrueOrderByCreatedAtDesc();

    /**
     * フィーチャードフォーラム取得
     */
    List<LearningForum> findByIsFeaturedTrueOrderByPopularityScoreDesc();

    /**
     * 人気フォーラム取得
     */
    @Query("SELECT lf FROM LearningForum lf WHERE lf.forumStatus = 'ACTIVE' ORDER BY lf.popularityScore DESC")
    List<LearningForum> findPopularForums();

    /**
     * 最近のアクティビティフォーラム取得
     */
    @Query("SELECT lf FROM LearningForum lf WHERE lf.lastActivityAt >= :since ORDER BY lf.lastActivityAt DESC")
    List<LearningForum> findRecentlyActiveForums(@Param("since") LocalDateTime since);

    /**
     * 高品質フォーラム取得
     */
    @Query("SELECT lf FROM LearningForum lf WHERE lf.qualityScore >= :minScore ORDER BY lf.qualityScore DESC")
    List<LearningForum> findHighQualityForums(@Param("minScore") BigDecimal minScore);

    /**
     * アクティビティスコア順フォーラム取得
     */
    @Query("SELECT lf FROM LearningForum lf WHERE lf.activityScore IS NOT NULL ORDER BY lf.activityScore DESC")
    List<LearningForum> findByActivityScoreDesc();

    /**
     * スペース・カテゴリ別フォーラム取得
     */
    List<LearningForum> findBySpaceIdAndForumCategoryOrderByLastActivityAtDesc(Long spaceId,
            LearningForum.ForumCategory forumCategory);

    /**
     * 作成者・ステータス別フォーラム取得
     */
    List<LearningForum> findByCreatorIdAndForumStatusOrderByCreatedAtDesc(UUID creatorId,
            LearningForum.ForumStatus forumStatus);

    /**
     * プライベートフォーラム取得
     */
    List<LearningForum> findByIsPrivateTrueOrderByCreatedAtDesc();

    /**
     * 承認が必要なフォーラム取得
     */
    List<LearningForum> findByRequireApprovalTrueOrderByCreatedAtDesc();

    /**
     * アーカイブされたフォーラム取得
     */
    List<LearningForum> findByIsArchivedTrueOrderByCreatedAtDesc();

    /**
     * ロックされたフォーラム取得
     */
    List<LearningForum> findByIsLockedTrueOrderByCreatedAtDesc();

    /**
     * 作成者・スペース別フォーラム数
     */
    @Query("SELECT COUNT(lf) FROM LearningForum lf WHERE lf.creatorId = :creatorId AND lf.spaceId = :spaceId")
    int countByCreatorIdAndSpaceId(@Param("creatorId") UUID creatorId, @Param("spaceId") Long spaceId);

    /**
     * スペース別アクティブフォーラム数
     */
    @Query("SELECT COUNT(lf) FROM LearningForum lf WHERE lf.spaceId = :spaceId AND lf.forumStatus = 'ACTIVE' AND lf.isArchived = false")
    int countActiveForumsBySpace(@Param("spaceId") Long spaceId);

    /**
     * カテゴリ別フォーラム数
     */
    @Query("SELECT COUNT(lf) FROM LearningForum lf WHERE lf.forumCategory = :category")
    int countForumsByCategory(@Param("category") LearningForum.ForumCategory category);

    /**
     * 作成者別フォーラム数
     */
    @Query("SELECT COUNT(lf) FROM LearningForum lf WHERE lf.creatorId = :creatorId")
    int countForumsByCreator(@Param("creatorId") UUID creatorId);

    /**
     * 期間内作成フォーラム数
     */
    @Query("SELECT COUNT(lf) FROM LearningForum lf WHERE lf.createdAt BETWEEN :startDate AND :endDate")
    int countForumsCreatedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * 平均人気スコア取得
     */
    @Query("SELECT AVG(lf.popularityScore) FROM LearningForum lf WHERE lf.popularityScore IS NOT NULL")
    Optional<BigDecimal> getAveragePopularityScore();

    /**
     * 平均アクティビティスコア取得
     */
    @Query("SELECT AVG(lf.activityScore) FROM LearningForum lf WHERE lf.activityScore IS NOT NULL")
    Optional<BigDecimal> getAverageActivityScore();

    /**
     * 平均品質スコア取得
     */
    @Query("SELECT AVG(lf.qualityScore) FROM LearningForum lf WHERE lf.qualityScore IS NOT NULL")
    Optional<BigDecimal> getAverageQualityScore();

    /**
     * カテゴリ別統計
     */
    @Query("SELECT COUNT(lf), AVG(lf.topicCount), AVG(lf.commentCount), AVG(lf.popularityScore) " +
            "FROM LearningForum lf WHERE lf.forumCategory = :category")
    Object[] getCategoryStatistics(@Param("category") LearningForum.ForumCategory category);

    /**
     * スペース別統計
     */
    @Query("SELECT COUNT(lf), AVG(lf.topicCount), AVG(lf.commentCount), AVG(lf.viewCount) " +
            "FROM LearningForum lf WHERE lf.spaceId = :spaceId")
    Object[] getSpaceStatistics(@Param("spaceId") Long spaceId);

    /**
     * 自動クローズ対象フォーラム取得
     */
    @Query("SELECT lf FROM LearningForum lf WHERE lf.autoCloseDays IS NOT NULL AND lf.autoCloseDays > 0 " +
            "AND lf.lastActivityAt < :thresholdDate AND lf.forumStatus = 'ACTIVE'")
    List<LearningForum> findForumsForAutoClose(@Param("thresholdDate") LocalDateTime thresholdDate);

    /**
     * 非アクティブフォーラム取得
     */
    @Query("SELECT lf FROM LearningForum lf WHERE lf.lastActivityAt < :thresholdDate OR lf.lastActivityAt IS NULL ORDER BY lf.lastActivityAt ASC NULLS FIRST")
    List<LearningForum> findInactiveForums(@Param("thresholdDate") LocalDateTime thresholdDate);

    /**
     * トピック数上位フォーラム取得
     */
    @Query("SELECT lf FROM LearningForum lf ORDER BY lf.topicCount DESC")
    List<LearningForum> findByTopicCountDesc();

    /**
     * コメント数上位フォーラム取得
     */
    @Query("SELECT lf FROM LearningForum lf ORDER BY lf.commentCount DESC")
    List<LearningForum> findByCommentCountDesc();

    /**
     * 閲覧数上位フォーラム取得
     */
    @Query("SELECT lf FROM LearningForum lf ORDER BY lf.viewCount DESC")
    List<LearningForum> findByViewCountDesc();

    /**
     * サブスクライバー数上位フォーラム取得
     */
    @Query("SELECT lf FROM LearningForum lf ORDER BY lf.subscriberCount DESC")
    List<LearningForum> findBySubscriberCountDesc();

    /**
     * タグ検索
     */
    @Query("SELECT lf FROM LearningForum lf WHERE lf.tags LIKE %:tag% ORDER BY lf.popularityScore DESC")
    List<LearningForum> findByTagsContaining(@Param("tag") String tag);

    /**
     * タイトル検索
     */
    @Query("SELECT lf FROM LearningForum lf WHERE LOWER(lf.title) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY lf.popularityScore DESC")
    List<LearningForum> findByTitleContainingIgnoreCase(@Param("keyword") String keyword);

    /**
     * 説明文検索
     */
    @Query("SELECT lf FROM LearningForum lf WHERE LOWER(lf.description) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY lf.popularityScore DESC")
    List<LearningForum> findByDescriptionContainingIgnoreCase(@Param("keyword") String keyword);

    /**
     * 複合検索（タイトル、説明、タグ）
     */
    @Query("SELECT lf FROM LearningForum lf WHERE " +
            "LOWER(lf.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(lf.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(lf.tags) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY lf.popularityScore DESC")
    List<LearningForum> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 期間内のアクティブフォーラム取得
     */
    @Query("SELECT lf FROM LearningForum lf WHERE lf.lastActivityAt BETWEEN :startDate AND :endDate ORDER BY lf.lastActivityAt DESC")
    List<LearningForum> findActiveForumsBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 期間内作成フォーラム取得
     */
    @Query("SELECT lf FROM LearningForum lf WHERE lf.createdAt BETWEEN :startDate AND :endDate ORDER BY lf.createdAt DESC")
    List<LearningForum> findForumsCreatedBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * モデレーションレベル別フォーラム取得
     */
    List<LearningForum> findByModerationLevelOrderByCreatedAtDesc(LearningForum.ModerationLevel moderationLevel);

    /**
     * 健康度の高いフォーラム取得
     */
    @Query("SELECT lf FROM LearningForum lf WHERE " +
            "(lf.activityScore * 0.4 + lf.qualityScore * 0.4 + lf.popularityScore * 0.2) >= :minHealthScore " +
            "ORDER BY (lf.activityScore * 0.4 + lf.qualityScore * 0.4 + lf.popularityScore * 0.2) DESC")
    List<LearningForum> findHealthyForums(@Param("minHealthScore") BigDecimal minHealthScore);

    /**
     * 作成者別期間統計
     */
    @Query("SELECT COUNT(lf), AVG(lf.topicCount), AVG(lf.commentCount) " +
            "FROM LearningForum lf " +
            "WHERE lf.creatorId = :creatorId AND lf.createdAt BETWEEN :startDate AND :endDate")
    Object[] getCreatorStatisticsBetween(@Param("creatorId") UUID creatorId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 期間別アクティビティ統計
     */
    @Query("SELECT DATE(lf.lastActivityAt), COUNT(lf) " +
            "FROM LearningForum lf " +
            "WHERE lf.lastActivityAt BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(lf.lastActivityAt) " +
            "ORDER BY DATE(lf.lastActivityAt)")
    List<Object[]> getActivityStatisticsByDate(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * エンゲージメント率の高いフォーラム取得
     */
    @Query("SELECT lf FROM LearningForum lf WHERE lf.viewCount > 0 " +
            "ORDER BY (lf.subscriberCount * 100.0 / lf.viewCount) DESC")
    List<LearningForum> findByEngagementRateDesc();

    /**
     * トピック当たりコメント数の多いフォーラム取得
     */
    @Query("SELECT lf FROM LearningForum lf WHERE lf.topicCount > 0 " +
            "ORDER BY (lf.commentCount * 1.0 / lf.topicCount) DESC")
    List<LearningForum> findByCommentsPerTopicDesc();

    /**
     * 最近作成されたフォーラム取得
     */
    @Query("SELECT lf FROM LearningForum lf WHERE lf.createdAt >= :since ORDER BY lf.createdAt DESC")
    List<LearningForum> findRecentlyCreatedForums(@Param("since") LocalDateTime since);

    /**
     * 最近更新されたフォーラム取得
     */
    @Query("SELECT lf FROM LearningForum lf WHERE lf.updatedAt >= :since ORDER BY lf.updatedAt DESC")
    List<LearningForum> findRecentlyUpdatedForums(@Param("since") LocalDateTime since);

    /**
     * 人気上昇中のフォーラム取得
     */
    @Query("SELECT lf FROM LearningForum lf WHERE lf.lastActivityAt >= :recentThreshold " +
            "ORDER BY lf.popularityScore DESC, lf.lastActivityAt DESC")
    List<LearningForum> findTrendingForums(@Param("recentThreshold") LocalDateTime recentThreshold);

    /**
     * カラースキーム別フォーラム取得
     */
    List<LearningForum> findByColorSchemeOrderByCreatedAtDesc(String colorScheme);

    /**
     * 匿名投稿可能フォーラム取得
     */
    List<LearningForum> findByAllowAnonymousTrueOrderByPopularityScoreDesc();

    /**
     * 特定の統計範囲内のフォーラム取得
     */
    @Query("SELECT lf FROM LearningForum lf WHERE " +
            "lf.topicCount BETWEEN :minTopics AND :maxTopics AND " +
            "lf.commentCount BETWEEN :minComments AND :maxComments " +
            "ORDER BY lf.popularityScore DESC")
    List<LearningForum> findForumsInStatisticsRange(@Param("minTopics") int minTopics,
            @Param("maxTopics") int maxTopics,
            @Param("minComments") int minComments,
            @Param("maxComments") int maxComments);
}
