package com.sfr.tokyo.sfr_backend.entity.learning;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 学習フォーラム エンティティ - LearningForum
 */
@Entity
@Table(name = "learning_forum", indexes = {
        @Index(name = "idx_learning_forum_space_id", columnList = "space_id"),
        @Index(name = "idx_learning_forum_creator_id", columnList = "creator_id"),
        @Index(name = "idx_learning_forum_category", columnList = "forum_category"),
        @Index(name = "idx_learning_forum_status", columnList = "forum_status"),
        @Index(name = "idx_learning_forum_visibility", columnList = "visibility_level"),
        @Index(name = "idx_learning_forum_created_at", columnList = "created_at"),
        @Index(name = "idx_learning_forum_last_activity", columnList = "last_activity_at"),
        @Index(name = "idx_learning_forum_pinned", columnList = "is_pinned"),
        @Index(name = "idx_learning_forum_locked", columnList = "is_locked"),
        @Index(name = "idx_learning_forum_archived", columnList = "is_archived"),
        @Index(name = "idx_learning_forum_topic_count", columnList = "topic_count"),
        @Index(name = "idx_learning_forum_composite", columnList = "space_id, forum_category, forum_status"),
        @Index(name = "idx_learning_forum_search", columnList = "title, description"),
        @Index(name = "idx_learning_forum_activity", columnList = "last_activity_at, is_archived")
})
public class LearningForum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "space_id")
    private Long spaceId;

    @Column(name = "creator_id", nullable = false)
    private UUID creatorId;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "forum_category", nullable = false)
    private ForumCategory forumCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "forum_status", nullable = false)
    private ForumStatus forumStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility_level", nullable = false)
    private VisibilityLevel visibilityLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_level", nullable = false)
    private ModerationLevel moderationLevel;

    @Column(name = "topic_count", nullable = false)
    private Integer topicCount;

    @Column(name = "comment_count", nullable = false)
    private Integer commentCount;

    @Column(name = "subscriber_count", nullable = false)
    private Integer subscriberCount;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    @Column(name = "activity_score", precision = 10, scale = 2)
    private BigDecimal activityScore;

    @Column(name = "quality_score", precision = 10, scale = 2)
    private BigDecimal qualityScore;

    @Column(name = "popularity_score", precision = 10, scale = 2)
    private BigDecimal popularityScore;

    @Column(name = "last_topic_id")
    private Long lastTopicId;

    @Column(name = "last_topic_title", length = 500)
    private String lastTopicTitle;

    @Column(name = "last_activity_user_id")
    private UUID lastActivityUserId;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Column(name = "tags", length = 2000)
    private String tags;

    @Column(name = "rules", columnDefinition = "TEXT")
    private String rules;

    @Column(name = "welcome_message", columnDefinition = "TEXT")
    private String welcomeMessage;

    @Column(name = "icon_url", length = 1000)
    private String iconUrl;

    @Column(name = "banner_url", length = 1000)
    private String bannerUrl;

    @Column(name = "color_scheme", length = 50)
    private String colorScheme;

    @Column(name = "is_pinned", nullable = false)
    private Boolean isPinned;

    @Column(name = "is_locked", nullable = false)
    private Boolean isLocked;

    @Column(name = "is_archived", nullable = false)
    private Boolean isArchived;

    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured;

    @Column(name = "is_private", nullable = false)
    private Boolean isPrivate;

    @Column(name = "allow_anonymous", nullable = false)
    private Boolean allowAnonymous;

    @Column(name = "require_approval", nullable = false)
    private Boolean requireApproval;

    @Column(name = "max_topics_per_user")
    private Integer maxTopicsPerUser;

    @Column(name = "max_comments_per_topic")
    private Integer maxCommentsPerTopic;

    @Column(name = "auto_close_days")
    private Integer autoCloseDays;

    @Column(name = "notification_settings", columnDefinition = "TEXT")
    private String notificationSettings;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public LearningForum() {
    }

    public LearningForum(UUID creatorId, String title, ForumCategory forumCategory) {
        this.creatorId = creatorId;
        this.title = title;
        this.forumCategory = forumCategory;
        this.forumStatus = ForumStatus.ACTIVE;
        this.visibilityLevel = VisibilityLevel.PUBLIC;
        this.moderationLevel = ModerationLevel.STANDARD;
        this.topicCount = 0;
        this.commentCount = 0;
        this.subscriberCount = 0;
        this.viewCount = 0;
        this.activityScore = BigDecimal.ZERO;
        this.qualityScore = BigDecimal.ZERO;
        this.popularityScore = BigDecimal.ZERO;
        this.isPinned = false;
        this.isLocked = false;
        this.isArchived = false;
        this.isFeatured = false;
        this.isPrivate = false;
        this.allowAnonymous = true;
        this.requireApproval = false;
    }

    /**
     * フォーラムカテゴリ
     */
    public enum ForumCategory {
        GENERAL_DISCUSSION("一般ディスカッション"),
        COURSE_SUPPORT("コースサポート"),
        TECHNICAL_HELP("技術的質問"),
        STUDY_GROUP("勉強会"),
        PROJECT_COLLABORATION("プロジェクト協力"),
        CAREER_ADVICE("キャリア相談"),
        RESOURCE_SHARING("リソース共有"),
        FEEDBACK_SUGGESTIONS("フィードバック・提案"),
        ANNOUNCEMENTS("お知らせ"),
        SHOWCASE("作品紹介"),
        EVENTS("イベント"),
        OFF_TOPIC("雑談"),
        MENTORSHIP("メンタリング"),
        JOB_BOARD("求人情報"),
        OTHER("その他");

        private final String description;

        ForumCategory(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * フォーラム状態
     */
    public enum ForumStatus {
        ACTIVE("アクティブ"),
        INACTIVE("非アクティブ"),
        LOCKED("ロック済み"),
        ARCHIVED("アーカイブ済み"),
        UNDER_REVIEW("レビュー中"),
        SUSPENDED("停止中"),
        DELETED("削除済み");

        private final String description;

        ForumStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 可視性レベル
     */
    public enum VisibilityLevel {
        PUBLIC("公開"),
        MEMBERS_ONLY("メンバーのみ"),
        SPACE_ONLY("スペース限定"),
        MODERATORS_ONLY("モデレーターのみ"),
        PRIVATE("プライベート");

        private final String description;

        VisibilityLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * モデレーションレベル
     */
    public enum ModerationLevel {
        NONE("なし"),
        LIGHT("軽度"),
        STANDARD("標準"),
        STRICT("厳格"),
        MANUAL_APPROVAL("手動承認");

        private final String description;

        ModerationLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Business Logic Methods

    /**
     * フォーラムがアクティブかチェック
     */
    public boolean isActive() {
        return forumStatus == ForumStatus.ACTIVE && !isLocked && !isArchived;
    }

    /**
     * 投稿可能かチェック
     */
    public boolean canPost() {
        return isActive() && !requireApproval;
    }

    /**
     * 閲覧可能かチェック
     */
    public boolean canView(UUID userId, boolean isModerator) {
        if (isArchived && !isModerator) {
            return false;
        }

        switch (visibilityLevel) {
            case PUBLIC:
                return true;
            case MEMBERS_ONLY:
                return userId != null;
            case MODERATORS_ONLY:
                return isModerator;
            case PRIVATE:
                return isModerator || creatorId.equals(userId);
            default:
                return false;
        }
    }

    /**
     * アクティビティスコアを計算
     */
    public BigDecimal calculateActivityScore() {
        if (topicCount == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal topicScore = BigDecimal.valueOf(topicCount).multiply(BigDecimal.valueOf(2));
        BigDecimal commentScore = BigDecimal.valueOf(commentCount).multiply(BigDecimal.valueOf(1));
        BigDecimal viewScore = BigDecimal.valueOf(viewCount).multiply(BigDecimal.valueOf(0.1));
        BigDecimal subscriberScore = BigDecimal.valueOf(subscriberCount).multiply(BigDecimal.valueOf(5));

        // 最近のアクティビティに基づく重み付け
        BigDecimal recencyWeight = BigDecimal.ONE;
        if (lastActivityAt != null) {
            long daysSinceLastActivity = java.time.temporal.ChronoUnit.DAYS.between(lastActivityAt,
                    LocalDateTime.now());
            if (daysSinceLastActivity > 30) {
                recencyWeight = BigDecimal.valueOf(0.5);
            } else if (daysSinceLastActivity > 7) {
                recencyWeight = BigDecimal.valueOf(0.8);
            }
        }

        BigDecimal totalScore = topicScore.add(commentScore).add(viewScore).add(subscriberScore);
        return totalScore.multiply(recencyWeight).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 品質スコアを計算
     */
    public BigDecimal calculateQualityScore() {
        if (topicCount == 0) {
            return BigDecimal.ZERO;
        }

        // トピック当たりのコメント数（エンゲージメント指標）
        BigDecimal engagementRatio = BigDecimal.valueOf(commentCount)
                .divide(BigDecimal.valueOf(topicCount), 2, java.math.RoundingMode.HALF_UP);

        // サブスクライバー率（人気指標）
        BigDecimal subscriberRatio = viewCount > 0
                ? BigDecimal.valueOf(subscriberCount).multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(viewCount), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 基本品質スコア
        BigDecimal baseScore = engagementRatio.multiply(BigDecimal.valueOf(0.6))
                .add(subscriberRatio.multiply(BigDecimal.valueOf(0.4)));

        // モデレーションによるボーナス
        BigDecimal moderationBonus = BigDecimal.ZERO;
        switch (moderationLevel) {
            case STRICT:
                moderationBonus = BigDecimal.valueOf(1.2);
                break;
            case STANDARD:
                moderationBonus = BigDecimal.valueOf(1.1);
                break;
            case LIGHT:
                moderationBonus = BigDecimal.valueOf(1.05);
                break;
            default:
                moderationBonus = BigDecimal.ONE;
        }

        return baseScore.multiply(moderationBonus).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 人気スコアを計算
     */
    public BigDecimal calculatePopularityScore() {
        BigDecimal viewWeight = BigDecimal.valueOf(viewCount).multiply(BigDecimal.valueOf(0.3));
        BigDecimal subscriberWeight = BigDecimal.valueOf(subscriberCount).multiply(BigDecimal.valueOf(3));
        BigDecimal topicWeight = BigDecimal.valueOf(topicCount).multiply(BigDecimal.valueOf(1));
        BigDecimal commentWeight = BigDecimal.valueOf(commentCount).multiply(BigDecimal.valueOf(0.5));

        BigDecimal totalScore = viewWeight.add(subscriberWeight).add(topicWeight).add(commentWeight);

        // フィーチャー済みボーナス
        if (isFeatured) {
            totalScore = totalScore.multiply(BigDecimal.valueOf(1.5));
        }

        // ピン留めボーナス
        if (isPinned) {
            totalScore = totalScore.multiply(BigDecimal.valueOf(1.2));
        }

        return totalScore.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * フォーラム統計を更新
     */
    public void updateStatistics(int newTopicCount, int newCommentCount, int newViewCount, int newSubscriberCount) {
        this.topicCount = newTopicCount;
        this.commentCount = newCommentCount;
        this.viewCount = newViewCount;
        this.subscriberCount = newSubscriberCount;
        this.activityScore = calculateActivityScore();
        this.qualityScore = calculateQualityScore();
        this.popularityScore = calculatePopularityScore();
    }

    /**
     * 最後のアクティビティを更新
     */
    public void updateLastActivity(Long topicId, String topicTitle, UUID userId) {
        this.lastTopicId = topicId;
        this.lastTopicTitle = topicTitle;
        this.lastActivityUserId = userId;
        this.lastActivityAt = LocalDateTime.now();
    }

    /**
     * タグリストを取得
     */
    public java.util.List<String> getTagList() {
        if (tags == null || tags.isEmpty()) {
            return java.util.List.of();
        }
        return java.util.Arrays.asList(tags.split(","));
    }

    /**
     * タグリストを設定
     */
    public void setTagList(java.util.List<String> tagList) {
        if (tagList == null || tagList.isEmpty()) {
            this.tags = null;
        } else {
            this.tags = String.join(",", tagList);
        }
    }

    /**
     * 自動クローズ対象かチェック
     */
    public boolean shouldAutoClose() {
        if (autoCloseDays == null || autoCloseDays <= 0 || lastActivityAt == null) {
            return false;
        }

        LocalDateTime closeDate = lastActivityAt.plusDays(autoCloseDays);
        return LocalDateTime.now().isAfter(closeDate) && isActive();
    }

    /**
     * エンゲージメント率を計算
     */
    public BigDecimal getEngagementRate() {
        if (viewCount == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(subscriberCount).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(viewCount), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * トピック当たりのコメント数を計算
     */
    public BigDecimal getCommentsPerTopic() {
        if (topicCount == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(commentCount)
                .divide(BigDecimal.valueOf(topicCount), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * フォーラムの健康度を計算
     */
    public BigDecimal getHealthScore() {
        BigDecimal activityWeight = activityScore != null ? activityScore.multiply(BigDecimal.valueOf(0.4))
                : BigDecimal.ZERO;
        BigDecimal qualityWeight = qualityScore != null ? qualityScore.multiply(BigDecimal.valueOf(0.4))
                : BigDecimal.ZERO;
        BigDecimal popularityWeight = popularityScore != null ? popularityScore.multiply(BigDecimal.valueOf(0.2))
                : BigDecimal.ZERO;

        return activityWeight.add(qualityWeight).add(popularityWeight).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * カテゴリ説明を取得
     */
    public String getCategoryDescription() {
        return forumCategory != null ? forumCategory.getDescription() : "";
    }

    /**
     * ステータス説明を取得
     */
    public String getStatusDescription() {
        return forumStatus != null ? forumStatus.getDescription() : "";
    }

    /**
     * 可視性説明を取得
     */
    public String getVisibilityDescription() {
        return visibilityLevel != null ? visibilityLevel.getDescription() : "";
    }

    /**
     * モデレーション説明を取得
     */
    public String getModerationDescription() {
        return moderationLevel != null ? moderationLevel.getDescription() : "";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(Long spaceId) {
        this.spaceId = spaceId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ForumCategory getForumCategory() {
        return forumCategory;
    }

    public void setForumCategory(ForumCategory forumCategory) {
        this.forumCategory = forumCategory;
    }

    public ForumStatus getForumStatus() {
        return forumStatus;
    }

    public void setForumStatus(ForumStatus forumStatus) {
        this.forumStatus = forumStatus;
    }

    public VisibilityLevel getVisibilityLevel() {
        return visibilityLevel;
    }

    public void setVisibilityLevel(VisibilityLevel visibilityLevel) {
        this.visibilityLevel = visibilityLevel;
    }

    public ModerationLevel getModerationLevel() {
        return moderationLevel;
    }

    public void setModerationLevel(ModerationLevel moderationLevel) {
        this.moderationLevel = moderationLevel;
    }

    public Integer getTopicCount() {
        return topicCount;
    }

    public void setTopicCount(Integer topicCount) {
        this.topicCount = topicCount;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Integer getSubscriberCount() {
        return subscriberCount;
    }

    public void setSubscriberCount(Integer subscriberCount) {
        this.subscriberCount = subscriberCount;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
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

    public Long getLastTopicId() {
        return lastTopicId;
    }

    public void setLastTopicId(Long lastTopicId) {
        this.lastTopicId = lastTopicId;
    }

    public String getLastTopicTitle() {
        return lastTopicTitle;
    }

    public void setLastTopicTitle(String lastTopicTitle) {
        this.lastTopicTitle = lastTopicTitle;
    }

    public UUID getLastActivityUserId() {
        return lastActivityUserId;
    }

    public void setLastActivityUserId(UUID lastActivityUserId) {
        this.lastActivityUserId = lastActivityUserId;
    }

    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public String getColorScheme() {
        return colorScheme;
    }

    public void setColorScheme(String colorScheme) {
        this.colorScheme = colorScheme;
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

    public Boolean getIsArchived() {
        return isArchived;
    }

    public void setIsArchived(Boolean isArchived) {
        this.isArchived = isArchived;
    }

    public Boolean getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(Boolean isFeatured) {
        this.isFeatured = isFeatured;
    }

    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public Boolean getAllowAnonymous() {
        return allowAnonymous;
    }

    public void setAllowAnonymous(Boolean allowAnonymous) {
        this.allowAnonymous = allowAnonymous;
    }

    public Boolean getRequireApproval() {
        return requireApproval;
    }

    public void setRequireApproval(Boolean requireApproval) {
        this.requireApproval = requireApproval;
    }

    public Integer getMaxTopicsPerUser() {
        return maxTopicsPerUser;
    }

    public void setMaxTopicsPerUser(Integer maxTopicsPerUser) {
        this.maxTopicsPerUser = maxTopicsPerUser;
    }

    public Integer getMaxCommentsPerTopic() {
        return maxCommentsPerTopic;
    }

    public void setMaxCommentsPerTopic(Integer maxCommentsPerTopic) {
        this.maxCommentsPerTopic = maxCommentsPerTopic;
    }

    public Integer getAutoCloseDays() {
        return autoCloseDays;
    }

    public void setAutoCloseDays(Integer autoCloseDays) {
        this.autoCloseDays = autoCloseDays;
    }

    public String getNotificationSettings() {
        return notificationSettings;
    }

    public void setNotificationSettings(String notificationSettings) {
        this.notificationSettings = notificationSettings;
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
}
