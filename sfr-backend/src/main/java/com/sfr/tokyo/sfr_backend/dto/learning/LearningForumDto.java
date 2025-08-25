package com.sfr.tokyo.sfr_backend.dto.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningForum;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 学習フォーラム DTO - LearningForumDto
 */
public class LearningForumDto {

    private Long id;

    private Long spaceId;

    @NotNull(message = "作成者IDは必須です")
    private UUID creatorId;

    @NotBlank(message = "タイトルは必須です")
    @Size(max = 500, message = "タイトルは500文字以内で入力してください")
    private String title;

    @Size(max = 10000, message = "説明は10000文字以内で入力してください")
    private String description;

    @NotNull(message = "フォーラムカテゴリは必須です")
    private LearningForum.ForumCategory forumCategory;

    @NotNull(message = "フォーラムステータスは必須です")
    private LearningForum.ForumStatus forumStatus;

    @NotNull(message = "可視性レベルは必須です")
    private LearningForum.VisibilityLevel visibilityLevel;

    @NotNull(message = "モデレーションレベルは必須です")
    private LearningForum.ModerationLevel moderationLevel;

    @Min(value = 0, message = "トピック数は0以上である必要があります")
    private Integer topicCount;

    @Min(value = 0, message = "コメント数は0以上である必要があります")
    private Integer commentCount;

    @Min(value = 0, message = "サブスクライバー数は0以上である必要があります")
    private Integer subscriberCount;

    @Min(value = 0, message = "閲覧数は0以上である必要があります")
    private Integer viewCount;

    @DecimalMin(value = "0.00", message = "アクティビティスコアは0.00以上である必要があります")
    private BigDecimal activityScore;

    @DecimalMin(value = "0.00", message = "品質スコアは0.00以上である必要があります")
    private BigDecimal qualityScore;

    @DecimalMin(value = "0.00", message = "人気スコアは0.00以上である必要があります")
    private BigDecimal popularityScore;

    private Long lastTopicId;

    @Size(max = 500, message = "最後のトピックタイトルは500文字以内である必要があります")
    private String lastTopicTitle;

    private UUID lastActivityUserId;

    private LocalDateTime lastActivityAt;

    @Size(max = 2000, message = "タグは2000文字以内で入力してください")
    private String tags;

    @Size(max = 10000, message = "ルールは10000文字以内で入力してください")
    private String rules;

    @Size(max = 5000, message = "ウェルカムメッセージは5000文字以内で入力してください")
    private String welcomeMessage;

    @Size(max = 1000, message = "アイコンURLは1000文字以内で入力してください")
    private String iconUrl;

    @Size(max = 1000, message = "バナーURLは1000文字以内で入力してください")
    private String bannerUrl;

    @Size(max = 50, message = "カラースキームは50文字以内で入力してください")
    private String colorScheme;

    private Boolean isPinned;

    private Boolean isLocked;

    private Boolean isArchived;

    private Boolean isFeatured;

    private Boolean isPrivate;

    private Boolean allowAnonymous;

    private Boolean requireApproval;

    @Min(value = 1, message = "ユーザー当たりの最大トピック数は1以上である必要があります")
    private Integer maxTopicsPerUser;

    @Min(value = 1, message = "トピック当たりの最大コメント数は1以上である必要があります")
    private Integer maxCommentsPerTopic;

    @Min(value = 1, message = "自動クローズ日数は1以上である必要があります")
    private Integer autoCloseDays;

    @Size(max = 5000, message = "通知設定は5000文字以内で入力してください")
    private String notificationSettings;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public LearningForumDto() {
    }

    public LearningForumDto(UUID creatorId, String title, LearningForum.ForumCategory forumCategory) {
        this.creatorId = creatorId;
        this.title = title;
        this.forumCategory = forumCategory;
        this.forumStatus = LearningForum.ForumStatus.ACTIVE;
        this.visibilityLevel = LearningForum.VisibilityLevel.PUBLIC;
        this.moderationLevel = LearningForum.ModerationLevel.STANDARD;
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

    // Business Logic Methods

    /**
     * フォーラムがアクティブかチェック
     */
    public boolean isActive() {
        return forumStatus == LearningForum.ForumStatus.ACTIVE &&
                (isLocked == null || !isLocked) &&
                (isArchived == null || !isArchived);
    }

    /**
     * 投稿可能かチェック
     */
    public boolean canPost() {
        return isActive() && (requireApproval == null || !requireApproval);
    }

    /**
     * 閲覧可能かチェック
     */
    public boolean canView(UUID userId, boolean isModerator) {
        if (isArchived != null && isArchived && !isModerator) {
            return false;
        }

        if (visibilityLevel == null) {
            return true;
        }

        switch (visibilityLevel) {
            case PUBLIC:
                return true;
            case MEMBERS_ONLY:
                return userId != null;
            case MODERATORS_ONLY:
                return isModerator;
            case PRIVATE:
                return isModerator || (creatorId != null && creatorId.equals(userId));
            default:
                return false;
        }
    }

    /**
     * アクティビティスコアを計算
     */
    public BigDecimal calculateActivityScore() {
        if (topicCount == null || topicCount == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal topicScore = BigDecimal.valueOf(topicCount).multiply(BigDecimal.valueOf(2));
        BigDecimal commentScore = BigDecimal.valueOf(commentCount != null ? commentCount : 0)
                .multiply(BigDecimal.valueOf(1));
        BigDecimal viewScore = BigDecimal.valueOf(viewCount != null ? viewCount : 0).multiply(BigDecimal.valueOf(0.1));
        BigDecimal subscriberScore = BigDecimal.valueOf(subscriberCount != null ? subscriberCount : 0)
                .multiply(BigDecimal.valueOf(5));

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
        if (topicCount == null || topicCount == 0) {
            return BigDecimal.ZERO;
        }

        // トピック当たりのコメント数（エンゲージメント指標）
        BigDecimal engagementRatio = BigDecimal.valueOf(commentCount != null ? commentCount : 0)
                .divide(BigDecimal.valueOf(topicCount), 2, java.math.RoundingMode.HALF_UP);

        // サブスクライバー率（人気指標）
        int views = viewCount != null ? viewCount : 0;
        BigDecimal subscriberRatio = views > 0
                ? BigDecimal.valueOf(subscriberCount != null ? subscriberCount : 0).multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(views), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 基本品質スコア
        BigDecimal baseScore = engagementRatio.multiply(BigDecimal.valueOf(0.6))
                .add(subscriberRatio.multiply(BigDecimal.valueOf(0.4)));

        // モデレーションによるボーナス
        BigDecimal moderationBonus = BigDecimal.ONE;
        if (moderationLevel != null) {
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
        }

        return baseScore.multiply(moderationBonus).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 人気スコアを計算
     */
    public BigDecimal calculatePopularityScore() {
        BigDecimal viewWeight = BigDecimal.valueOf(viewCount != null ? viewCount : 0).multiply(BigDecimal.valueOf(0.3));
        BigDecimal subscriberWeight = BigDecimal.valueOf(subscriberCount != null ? subscriberCount : 0)
                .multiply(BigDecimal.valueOf(3));
        BigDecimal topicWeight = BigDecimal.valueOf(topicCount != null ? topicCount : 0)
                .multiply(BigDecimal.valueOf(1));
        BigDecimal commentWeight = BigDecimal.valueOf(commentCount != null ? commentCount : 0)
                .multiply(BigDecimal.valueOf(0.5));

        BigDecimal totalScore = viewWeight.add(subscriberWeight).add(topicWeight).add(commentWeight);

        // フィーチャー済みボーナス
        if (isFeatured != null && isFeatured) {
            totalScore = totalScore.multiply(BigDecimal.valueOf(1.5));
        }

        // ピン留めボーナス
        if (isPinned != null && isPinned) {
            totalScore = totalScore.multiply(BigDecimal.valueOf(1.2));
        }

        return totalScore.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * タグリストを取得
     */
    public List<String> getTagList() {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return Arrays.asList(tags.split(","));
    }

    /**
     * タグリストを設定
     */
    public void setTagList(List<String> tagList) {
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
        if (viewCount == null || viewCount == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(subscriberCount != null ? subscriberCount : 0).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(viewCount), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * トピック当たりのコメント数を計算
     */
    public BigDecimal getCommentsPerTopic() {
        if (topicCount == null || topicCount == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(commentCount != null ? commentCount : 0)
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

    /**
     * フォーラムの活発さをテキストで取得
     */
    public String getActivityLevel() {
        if (activityScore == null) {
            return "不明";
        }

        double score = activityScore.doubleValue();
        if (score >= 100)
            return "非常に活発";
        if (score >= 50)
            return "活発";
        if (score >= 20)
            return "普通";
        if (score >= 10)
            return "やや低調";
        return "低調";
    }

    /**
     * フォーラムの品質をテキストで取得
     */
    public String getQualityLevel() {
        if (qualityScore == null) {
            return "不明";
        }

        double score = qualityScore.doubleValue();
        if (score >= 80)
            return "非常に高品質";
        if (score >= 60)
            return "高品質";
        if (score >= 40)
            return "普通";
        if (score >= 20)
            return "改善の余地あり";
        return "要改善";
    }

    /**
     * フォーラムの人気度をテキストで取得
     */
    public String getPopularityLevel() {
        if (popularityScore == null) {
            return "不明";
        }

        double score = popularityScore.doubleValue();
        if (score >= 200)
            return "非常に人気";
        if (score >= 100)
            return "人気";
        if (score >= 50)
            return "普通";
        if (score >= 20)
            return "やや不人気";
        return "不人気";
    }

    /**
     * 最後のアクティビティからの経過時間をテキストで取得
     */
    public String getLastActivityTimeText() {
        if (lastActivityAt == null) {
            return "アクティビティなし";
        }

        long days = java.time.temporal.ChronoUnit.DAYS.between(lastActivityAt, LocalDateTime.now());
        long hours = java.time.temporal.ChronoUnit.HOURS.between(lastActivityAt, LocalDateTime.now());

        if (days > 30)
            return days + "日前";
        if (days > 0)
            return days + "日前";
        if (hours > 0)
            return hours + "時間前";
        return "1時間以内";
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

    public LearningForum.ForumCategory getForumCategory() {
        return forumCategory;
    }

    public void setForumCategory(LearningForum.ForumCategory forumCategory) {
        this.forumCategory = forumCategory;
    }

    public LearningForum.ForumStatus getForumStatus() {
        return forumStatus;
    }

    public void setForumStatus(LearningForum.ForumStatus forumStatus) {
        this.forumStatus = forumStatus;
    }

    public LearningForum.VisibilityLevel getVisibilityLevel() {
        return visibilityLevel;
    }

    public void setVisibilityLevel(LearningForum.VisibilityLevel visibilityLevel) {
        this.visibilityLevel = visibilityLevel;
    }

    public LearningForum.ModerationLevel getModerationLevel() {
        return moderationLevel;
    }

    public void setModerationLevel(LearningForum.ModerationLevel moderationLevel) {
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
