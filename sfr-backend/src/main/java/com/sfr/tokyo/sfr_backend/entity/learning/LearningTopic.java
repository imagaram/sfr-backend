package com.sfr.tokyo.sfr_backend.entity.learning;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 学習トピック エンティティ - LearningTopic
 */
@Entity
@Table(name = "learning_topic", indexes = {
        @Index(name = "idx_topic_forum_id", columnList = "forum_id"),
        @Index(name = "idx_topic_creator_id", columnList = "creator_id"),
        @Index(name = "idx_topic_status", columnList = "topic_status"),
        @Index(name = "idx_topic_priority", columnList = "priority"),
        @Index(name = "idx_topic_created_at", columnList = "created_at"),
        @Index(name = "idx_topic_last_activity", columnList = "last_activity_at"),
        @Index(name = "idx_topic_activity_score", columnList = "activity_score"),
        @Index(name = "idx_topic_quality_score", columnList = "quality_score"),
        @Index(name = "idx_topic_popularity_score", columnList = "popularity_score"),
        @Index(name = "idx_topic_is_pinned", columnList = "is_pinned"),
        @Index(name = "idx_topic_is_locked", columnList = "is_locked"),
        @Index(name = "idx_topic_is_solved", columnList = "is_solved"),
        @Index(name = "idx_topic_is_featured", columnList = "is_featured"),
        @Index(name = "idx_topic_view_count", columnList = "view_count"),
        @Index(name = "idx_topic_like_count", columnList = "like_count"),
        @Index(name = "idx_topic_comment_count", columnList = "comment_count"),
        @Index(name = "idx_topic_composite", columnList = "forum_id, topic_status, created_at"),
        @Index(name = "idx_topic_search", columnList = "title, content"),
        @Index(name = "idx_topic_engagement", columnList = "view_count, like_count, comment_count"),
        @Index(name = "idx_topic_moderation", columnList = "requires_moderation, moderation_status")
})
public class LearningTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "forum_id", nullable = false)
    @NotNull(message = "フォーラムIDは必須です")
    private Long forumId;

    @Column(name = "creator_id", nullable = false)
    @NotNull(message = "作成者IDは必須です")
    private UUID creatorId;

    @Column(name = "title", nullable = false, length = 200)
    @NotBlank(message = "タイトルは必須です")
    @Size(max = 200, message = "タイトルは200文字以内で入力してください")
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "内容は必須です")
    @Size(max = 50000, message = "内容は50000文字以内で入力してください")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "topic_type", nullable = false, length = 50)
    @NotNull(message = "トピックタイプは必須です")
    private TopicType topicType;

    @Enumerated(EnumType.STRING)
    @Column(name = "topic_status", nullable = false, length = 50)
    @NotNull(message = "トピックステータスは必須です")
    private TopicStatus topicStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 50)
    @NotNull(message = "優先度は必須です")
    private TopicPriority priority;

    @Column(name = "tags", length = 500)
    @Size(max = 500, message = "タグは500文字以内で入力してください")
    private String tags;

    @Column(name = "view_count", nullable = false)
    @Min(value = 0, message = "閲覧数は0以上である必要があります")
    private Integer viewCount = 0;

    @Column(name = "like_count", nullable = false)
    @Min(value = 0, message = "いいね数は0以上である必要があります")
    private Integer likeCount = 0;

    @Column(name = "comment_count", nullable = false)
    @Min(value = 0, message = "コメント数は0以上である必要があります")
    private Integer commentCount = 0;

    @Column(name = "bookmark_count", nullable = false)
    @Min(value = 0, message = "ブックマーク数は0以上である必要があります")
    private Integer bookmarkCount = 0;

    @Column(name = "share_count", nullable = false)
    @Min(value = 0, message = "シェア数は0以上である必要があります")
    private Integer shareCount = 0;

    @Column(name = "is_pinned", nullable = false)
    private Boolean isPinned = false;

    @Column(name = "is_locked", nullable = false)
    private Boolean isLocked = false;

    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;

    @Column(name = "is_solved", nullable = false)
    private Boolean isSolved = false;

    @Column(name = "is_announcement", nullable = false)
    private Boolean isAnnouncement = false;

    @Column(name = "requires_moderation", nullable = false)
    private Boolean requiresModeration = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status", length = 50)
    private ModerationStatus moderationStatus;

    @Column(name = "moderator_id")
    private UUID moderatorId;

    @Column(name = "moderation_notes", columnDefinition = "TEXT")
    private String moderationNotes;

    @Column(name = "moderated_at")
    private LocalDateTime moderatedAt;

    @Column(name = "activity_score", precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "アクティビティスコアは0以上である必要があります")
    @DecimalMax(value = "100.00", message = "アクティビティスコアは100以下である必要があります")
    private BigDecimal activityScore = BigDecimal.ZERO;

    @Column(name = "quality_score", precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "品質スコアは0以上である必要があります")
    @DecimalMax(value = "100.00", message = "品質スコアは100以下である必要があります")
    private BigDecimal qualityScore = BigDecimal.ZERO;

    @Column(name = "popularity_score", precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "人気スコアは0以上である必要があります")
    @DecimalMax(value = "100.00", message = "人気スコアは100以下である必要があります")
    private BigDecimal popularityScore = BigDecimal.ZERO;

    @Column(name = "overall_score", precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "総合スコアは0以上である必要があります")
    @DecimalMax(value = "100.00", message = "総合スコアは100以下である必要があります")
    private BigDecimal overallScore = BigDecimal.ZERO;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Column(name = "last_activity_user_id")
    private UUID lastActivityUserId;

    @Column(name = "last_comment_id")
    private Long lastCommentId;

    @Column(name = "last_comment_at")
    private LocalDateTime lastCommentAt;

    @Column(name = "last_comment_user_id")
    private UUID lastCommentUserId;

    @Column(name = "best_answer_id")
    private Long bestAnswerId;

    @Column(name = "best_answer_selected_at")
    private LocalDateTime bestAnswerSelectedAt;

    @Column(name = "best_answer_selected_by")
    private UUID bestAnswerSelectedBy;

    @Column(name = "solution_points", nullable = false)
    @Min(value = 0, message = "解決ポイントは0以上である必要があります")
    private Integer solutionPoints = 0;

    @Column(name = "auto_close_at")
    private LocalDateTime autoCloseAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "closed_by")
    private UUID closedBy;

    @Column(name = "close_reason", length = 500)
    @Size(max = 500, message = "クローズ理由は500文字以内で入力してください")
    private String closeReason;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @Column(name = "archived_by")
    private UUID archivedBy;

    @Column(name = "archive_reason", length = 500)
    @Size(max = 500, message = "アーカイブ理由は500文字以内で入力してください")
    private String archiveReason;

    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // トピックタイプ
    public enum TopicType {
        DISCUSSION, // ディスカッション
        QUESTION, // 質問
        TUTORIAL, // チュートリアル
        SHOWCASE, // 作品紹介
        ANNOUNCEMENT, // お知らせ
        POLL, // 投票
        EVENT, // イベント
        RESOURCE, // リソース
        FEEDBACK, // フィードバック
        BUG_REPORT, // バグ報告
        FEATURE_REQUEST, // 機能要望
        COLLABORATION, // 協力
        STUDY_GROUP, // 勉強会
        JOB_POSTING, // 求人
        OTHER // その他
    }

    // トピックステータス
    public enum TopicStatus {
        ACTIVE, // アクティブ
        CLOSED, // クローズ
        LOCKED, // ロック
        ARCHIVED, // アーカイブ
        PENDING, // 承認待ち
        REJECTED, // 拒否
        DELETED, // 削除
        UNDER_REVIEW, // レビュー中
        SUSPENDED // 停止
    }

    // トピック優先度
    public enum TopicPriority {
        LOW, // 低
        NORMAL, // 通常
        HIGH, // 高
        URGENT, // 緊急
        CRITICAL // クリティカル
    }

    // モデレーションステータス
    public enum ModerationStatus {
        PENDING, // 承認待ち
        APPROVED, // 承認済み
        REJECTED, // 拒否
        FLAGGED, // フラグ付き
        UNDER_REVIEW, // レビュー中
        AUTO_APPROVED, // 自動承認
        REQUIRES_ATTENTION // 要注意
    }

    // Constructors
    public LearningTopic() {
    }

    public LearningTopic(Long forumId, UUID creatorId, String title, String content,
            TopicType topicType, TopicPriority priority) {
        this.forumId = forumId;
        this.creatorId = creatorId;
        this.title = title;
        this.content = content;
        this.topicType = topicType;
        this.topicStatus = TopicStatus.ACTIVE;
        this.priority = priority;
        this.lastActivityAt = LocalDateTime.now();
        this.lastActivityUserId = creatorId;
    }

    // Business Logic Methods

    /**
     * アクティビティスコア計算
     */
    public BigDecimal calculateActivityScore() {
        if (viewCount == null || commentCount == null)
            return BigDecimal.ZERO;

        double viewScore = Math.min(viewCount * 0.1, 30.0);
        double commentScore = Math.min(commentCount * 2.0, 40.0);
        double likeScore = Math.min(likeCount * 1.5, 20.0);

        // 最新性の考慮
        if (lastActivityAt != null) {
            long hoursAgo = java.time.Duration.between(lastActivityAt, LocalDateTime.now()).toHours();
            double recencyMultiplier = Math.max(0.1, 1.0 - (hoursAgo / (24.0 * 7))); // 1週間で減衰
            return BigDecimal.valueOf((viewScore + commentScore + likeScore) * recencyMultiplier)
                    .setScale(2, java.math.RoundingMode.HALF_UP);
        }

        return BigDecimal.valueOf(viewScore + commentScore + likeScore)
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 品質スコア計算
     */
    public BigDecimal calculateQualityScore() {
        double baseScore = 50.0; // ベーススコア

        // 内容の長さによるスコア調整
        if (content != null) {
            int contentLength = content.length();
            if (contentLength >= 500)
                baseScore += 15.0;
            else if (contentLength >= 200)
                baseScore += 10.0;
            else if (contentLength >= 100)
                baseScore += 5.0;
        }

        // タイトルの品質
        if (title != null && title.length() >= 10)
            baseScore += 5.0;

        // タグの存在
        if (tags != null && !tags.trim().isEmpty())
            baseScore += 5.0;

        // 解決済みの場合
        if (Boolean.TRUE.equals(isSolved))
            baseScore += 10.0;

        // ベストアンサーがある場合
        if (bestAnswerId != null)
            baseScore += 5.0;

        // いいね率
        if (viewCount != null && viewCount > 0 && likeCount != null) {
            double likeRate = (double) likeCount / viewCount;
            baseScore += Math.min(likeRate * 50, 15.0);
        }

        return BigDecimal.valueOf(Math.min(baseScore, 100.0))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 人気スコア計算
     */
    public BigDecimal calculatePopularityScore() {
        double score = 0.0;

        // 閲覧数スコア
        if (viewCount != null) {
            score += Math.min(viewCount * 0.05, 25.0);
        }

        // いいね数スコア
        if (likeCount != null) {
            score += Math.min(likeCount * 2.0, 30.0);
        }

        // コメント数スコア
        if (commentCount != null) {
            score += Math.min(commentCount * 1.5, 25.0);
        }

        // ブックマーク数スコア
        if (bookmarkCount != null) {
            score += Math.min(bookmarkCount * 3.0, 15.0);
        }

        // シェア数スコア
        if (shareCount != null) {
            score += Math.min(shareCount * 5.0, 10.0);
        }

        return BigDecimal.valueOf(Math.min(score, 100.0))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 総合スコア計算
     */
    public BigDecimal calculateOverallScore() {
        BigDecimal activity = calculateActivityScore();
        BigDecimal quality = calculateQualityScore();
        BigDecimal popularity = calculatePopularityScore();

        // 重み付き平均: アクティビティ30%, 品質40%, 人気30%
        return activity.multiply(BigDecimal.valueOf(0.3))
                .add(quality.multiply(BigDecimal.valueOf(0.4)))
                .add(popularity.multiply(BigDecimal.valueOf(0.3)))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * エンゲージメント率計算
     */
    public BigDecimal calculateEngagementRate() {
        if (viewCount == null || viewCount == 0)
            return BigDecimal.ZERO;

        int totalEngagements = (likeCount != null ? likeCount : 0) +
                (commentCount != null ? commentCount : 0) +
                (bookmarkCount != null ? bookmarkCount : 0) +
                (shareCount != null ? shareCount : 0);

        return BigDecimal.valueOf((double) totalEngagements / viewCount * 100)
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 解決率計算
     */
    public BigDecimal calculateSolutionRate() {
        if (!TopicType.QUESTION.equals(topicType))
            return BigDecimal.ZERO;
        return Boolean.TRUE.equals(isSolved) ? BigDecimal.valueOf(100.0) : BigDecimal.ZERO;
    }

    /**
     * 健康度スコア計算
     */
    public BigDecimal calculateHealthScore() {
        double healthScore = 100.0;

        // 最後のアクティビティからの経過時間
        if (lastActivityAt != null) {
            long daysAgo = java.time.Duration.between(lastActivityAt, LocalDateTime.now()).toDays();
            if (daysAgo > 30)
                healthScore -= 30.0;
            else if (daysAgo > 14)
                healthScore -= 15.0;
            else if (daysAgo > 7)
                healthScore -= 5.0;
        }

        // ステータスによる調整
        if (TopicStatus.LOCKED.equals(topicStatus) || TopicStatus.ARCHIVED.equals(topicStatus)) {
            healthScore -= 40.0;
        } else if (TopicStatus.CLOSED.equals(topicStatus)) {
            healthScore -= 20.0;
        }

        // モデレーション状況
        if (Boolean.TRUE.equals(requiresModeration)) {
            healthScore -= 20.0;
        }

        return BigDecimal.valueOf(Math.max(healthScore, 0.0))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 人気度判定
     */
    public boolean isPopular() {
        return calculatePopularityScore().compareTo(BigDecimal.valueOf(70.0)) >= 0;
    }

    /**
     * 高品質判定
     */
    public boolean isHighQuality() {
        return calculateQualityScore().compareTo(BigDecimal.valueOf(80.0)) >= 0;
    }

    /**
     * アクティブ判定
     */
    public boolean isActive() {
        if (!TopicStatus.ACTIVE.equals(topicStatus))
            return false;
        if (Boolean.TRUE.equals(isLocked))
            return false;
        if (lastActivityAt == null)
            return true;

        long daysAgo = java.time.Duration.between(lastActivityAt, LocalDateTime.now()).toDays();
        return daysAgo <= 30; // 30日以内にアクティビティがあれば活動中
    }

    /**
     * 自動クローズ対象判定
     */
    public boolean shouldAutoClose() {
        if (autoCloseAt == null)
            return false;
        if (!TopicStatus.ACTIVE.equals(topicStatus))
            return false;
        if (Boolean.TRUE.equals(isPinned))
            return false;
        if (Boolean.TRUE.equals(isFeatured))
            return false;
        return LocalDateTime.now().isAfter(autoCloseAt);
    }

    /**
     * モデレーション必要判定
     */
    public boolean needsModeration() {
        return Boolean.TRUE.equals(requiresModeration) &&
                (moderationStatus == null || ModerationStatus.PENDING.equals(moderationStatus));
    }

    /**
     * タグリスト取得
     */
    public java.util.List<String> getTagList() {
        if (tags == null || tags.trim().isEmpty()) {
            return java.util.List.of();
        }
        return java.util.Arrays.asList(tags.split(","))
                .stream()
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * タグ設定
     */
    public void setTagList(java.util.List<String> tagList) {
        if (tagList == null || tagList.isEmpty()) {
            this.tags = null;
        } else {
            this.tags = String.join(",", tagList);
        }
    }

    /**
     * 最後のアクティビティ更新
     */
    public void updateLastActivity(UUID userId) {
        this.lastActivityAt = LocalDateTime.now();
        this.lastActivityUserId = userId;
    }

    /**
     * 最後のコメント更新
     */
    public void updateLastComment(Long commentId, UUID userId) {
        this.lastCommentId = commentId;
        this.lastCommentAt = LocalDateTime.now();
        this.lastCommentUserId = userId;
        updateLastActivity(userId);
    }

    /**
     * ベストアンサー設定
     */
    public void setBestAnswer(Long answerId, UUID selectedBy) {
        this.bestAnswerId = answerId;
        this.bestAnswerSelectedAt = LocalDateTime.now();
        this.bestAnswerSelectedBy = selectedBy;
        this.isSolved = true;
        updateLastActivity(selectedBy);
    }

    /**
     * ベストアンサー解除
     */
    public void clearBestAnswer() {
        this.bestAnswerId = null;
        this.bestAnswerSelectedAt = null;
        this.bestAnswerSelectedBy = null;
        this.isSolved = false;
    }

    /**
     * クローズ処理
     */
    public void close(UUID closedBy, String reason) {
        this.topicStatus = TopicStatus.CLOSED;
        this.closedAt = LocalDateTime.now();
        this.closedBy = closedBy;
        this.closeReason = reason;
        updateLastActivity(closedBy);
    }

    /**
     * アーカイブ処理
     */
    public void archive(UUID archivedBy, String reason) {
        this.topicStatus = TopicStatus.ARCHIVED;
        this.archivedAt = LocalDateTime.now();
        this.archivedBy = archivedBy;
        this.archiveReason = reason;
        updateLastActivity(archivedBy);
    }

    /**
     * モデレーション完了
     */
    public void completeModerationApproval(UUID moderatorId, String notes) {
        this.moderationStatus = ModerationStatus.APPROVED;
        this.moderatorId = moderatorId;
        this.moderationNotes = notes;
        this.moderatedAt = LocalDateTime.now();
        this.requiresModeration = false;
    }

    /**
     * モデレーション拒否
     */
    public void rejectModeration(UUID moderatorId, String notes) {
        this.moderationStatus = ModerationStatus.REJECTED;
        this.moderatorId = moderatorId;
        this.moderationNotes = notes;
        this.moderatedAt = LocalDateTime.now();
        this.topicStatus = TopicStatus.REJECTED;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getForumId() {
        return forumId;
    }

    public void setForumId(Long forumId) {
        this.forumId = forumId;
    }

    public UUID getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(UUID creatorId) {
        this.creatorId = creatorId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public TopicType getTopicType() {
        return topicType;
    }

    public void setTopicType(TopicType topicType) {
        this.topicType = topicType;
    }

    public TopicStatus getTopicStatus() {
        return topicStatus;
    }

    public void setTopicStatus(TopicStatus topicStatus) {
        this.topicStatus = topicStatus;
    }

    public TopicPriority getPriority() {
        return priority;
    }

    public void setPriority(TopicPriority priority) {
        this.priority = priority;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Integer getBookmarkCount() {
        return bookmarkCount;
    }

    public void setBookmarkCount(Integer bookmarkCount) {
        this.bookmarkCount = bookmarkCount;
    }

    public Integer getShareCount() {
        return shareCount;
    }

    public void setShareCount(Integer shareCount) {
        this.shareCount = shareCount;
    }

    public Boolean getIsPinned() {
        return isPinned;
    }

    public void setIsPinned(Boolean isPinned) {
        this.isPinned = isPinned;
    }

    public Boolean getIsLocked() {
        return isLocked;
    }

    public void setIsLocked(Boolean isLocked) {
        this.isLocked = isLocked;
    }

    public Boolean getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(Boolean isFeatured) {
        this.isFeatured = isFeatured;
    }

    public Boolean getIsSolved() {
        return isSolved;
    }

    public void setIsSolved(Boolean isSolved) {
        this.isSolved = isSolved;
    }

    public Boolean getIsAnnouncement() {
        return isAnnouncement;
    }

    public void setIsAnnouncement(Boolean isAnnouncement) {
        this.isAnnouncement = isAnnouncement;
    }

    public Boolean getRequiresModeration() {
        return requiresModeration;
    }

    public void setRequiresModeration(Boolean requiresModeration) {
        this.requiresModeration = requiresModeration;
    }

    public ModerationStatus getModerationStatus() {
        return moderationStatus;
    }

    public void setModerationStatus(ModerationStatus moderationStatus) {
        this.moderationStatus = moderationStatus;
    }

    public UUID getModeratorId() {
        return moderatorId;
    }

    public void setModeratorId(UUID moderatorId) {
        this.moderatorId = moderatorId;
    }

    public String getModerationNotes() {
        return moderationNotes;
    }

    public void setModerationNotes(String moderationNotes) {
        this.moderationNotes = moderationNotes;
    }

    public LocalDateTime getModeratedAt() {
        return moderatedAt;
    }

    public void setModeratedAt(LocalDateTime moderatedAt) {
        this.moderatedAt = moderatedAt;
    }

    public BigDecimal getActivityScore() {
        return activityScore;
    }

    public void setActivityScore(BigDecimal activityScore) {
        this.activityScore = activityScore;
    }

    public BigDecimal getQualityScore() {
        return qualityScore;
    }

    public void setQualityScore(BigDecimal qualityScore) {
        this.qualityScore = qualityScore;
    }

    public BigDecimal getPopularityScore() {
        return popularityScore;
    }

    public void setPopularityScore(BigDecimal popularityScore) {
        this.popularityScore = popularityScore;
    }

    public BigDecimal getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(BigDecimal overallScore) {
        this.overallScore = overallScore;
    }

    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public UUID getLastActivityUserId() {
        return lastActivityUserId;
    }

    public void setLastActivityUserId(UUID lastActivityUserId) {
        this.lastActivityUserId = lastActivityUserId;
    }

    public Long getLastCommentId() {
        return lastCommentId;
    }

    public void setLastCommentId(Long lastCommentId) {
        this.lastCommentId = lastCommentId;
    }

    public LocalDateTime getLastCommentAt() {
        return lastCommentAt;
    }

    public void setLastCommentAt(LocalDateTime lastCommentAt) {
        this.lastCommentAt = lastCommentAt;
    }

    public UUID getLastCommentUserId() {
        return lastCommentUserId;
    }

    public void setLastCommentUserId(UUID lastCommentUserId) {
        this.lastCommentUserId = lastCommentUserId;
    }

    public Long getBestAnswerId() {
        return bestAnswerId;
    }

    public void setBestAnswerId(Long bestAnswerId) {
        this.bestAnswerId = bestAnswerId;
    }

    public LocalDateTime getBestAnswerSelectedAt() {
        return bestAnswerSelectedAt;
    }

    public void setBestAnswerSelectedAt(LocalDateTime bestAnswerSelectedAt) {
        this.bestAnswerSelectedAt = bestAnswerSelectedAt;
    }

    public UUID getBestAnswerSelectedBy() {
        return bestAnswerSelectedBy;
    }

    public void setBestAnswerSelectedBy(UUID bestAnswerSelectedBy) {
        this.bestAnswerSelectedBy = bestAnswerSelectedBy;
    }

    public Integer getSolutionPoints() {
        return solutionPoints;
    }

    public void setSolutionPoints(Integer solutionPoints) {
        this.solutionPoints = solutionPoints;
    }

    public LocalDateTime getAutoCloseAt() {
        return autoCloseAt;
    }

    public void setAutoCloseAt(LocalDateTime autoCloseAt) {
        this.autoCloseAt = autoCloseAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public UUID getClosedBy() {
        return closedBy;
    }

    public void setClosedBy(UUID closedBy) {
        this.closedBy = closedBy;
    }

    public String getCloseReason() {
        return closeReason;
    }

    public void setCloseReason(String closeReason) {
        this.closeReason = closeReason;
    }

    public LocalDateTime getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(LocalDateTime archivedAt) {
        this.archivedAt = archivedAt;
    }

    public UUID getArchivedBy() {
        return archivedBy;
    }

    public void setArchivedBy(UUID archivedBy) {
        this.archivedBy = archivedBy;
    }

    public String getArchiveReason() {
        return archiveReason;
    }

    public void setArchiveReason(String archiveReason) {
        this.archiveReason = archiveReason;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
