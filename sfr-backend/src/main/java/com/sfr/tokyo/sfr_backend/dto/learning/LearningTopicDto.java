package com.sfr.tokyo.sfr_backend.dto.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningTopic;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 学習トピック DTO - LearningTopicDto
 */
public class LearningTopicDto {

    private Long id;

    @NotNull(message = "フォーラムIDは必須です")
    private Long forumId;

    @NotNull(message = "作成者IDは必須です")
    private UUID creatorId;

    @NotBlank(message = "タイトルは必須です")
    @Size(max = 200, message = "タイトルは200文字以内で入力してください")
    private String title;

    @NotBlank(message = "内容は必須です")
    @Size(max = 50000, message = "内容は50000文字以内で入力してください")
    private String content;

    @NotNull(message = "トピックタイプは必須です")
    private LearningTopic.TopicType topicType;

    @NotNull(message = "トピックステータスは必須です")
    private LearningTopic.TopicStatus topicStatus;

    @NotNull(message = "優先度は必須です")
    private LearningTopic.TopicPriority priority;

    @Size(max = 500, message = "タグは500文字以内で入力してください")
    private String tags;

    @Min(value = 0, message = "閲覧数は0以上である必要があります")
    private Integer viewCount = 0;

    @Min(value = 0, message = "いいね数は0以上である必要があります")
    private Integer likeCount = 0;

    @Min(value = 0, message = "コメント数は0以上である必要があります")
    private Integer commentCount = 0;

    @Min(value = 0, message = "ブックマーク数は0以上である必要があります")
    private Integer bookmarkCount = 0;

    @Min(value = 0, message = "シェア数は0以上である必要があります")
    private Integer shareCount = 0;

    private Boolean isPinned = false;
    private Boolean isLocked = false;
    private Boolean isFeatured = false;
    private Boolean isSolved = false;
    private Boolean isAnnouncement = false;
    private Boolean requiresModeration = false;

    private LearningTopic.ModerationStatus moderationStatus;
    private UUID moderatorId;
    private String moderationNotes;
    private LocalDateTime moderatedAt;

    @DecimalMin(value = "0.00", message = "アクティビティスコアは0以上である必要があります")
    @DecimalMax(value = "100.00", message = "アクティビティスコアは100以下である必要があります")
    private BigDecimal activityScore = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "品質スコアは0以上である必要があります")
    @DecimalMax(value = "100.00", message = "品質スコアは100以下である必要があります")
    private BigDecimal qualityScore = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "人気スコアは0以上である必要があります")
    @DecimalMax(value = "100.00", message = "人気スコアは100以下である必要があります")
    private BigDecimal popularityScore = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "総合スコアは0以上である必要があります")
    @DecimalMax(value = "100.00", message = "総合スコアは100以下である必要があります")
    private BigDecimal overallScore = BigDecimal.ZERO;

    private LocalDateTime lastActivityAt;
    private UUID lastActivityUserId;
    private Long lastCommentId;
    private LocalDateTime lastCommentAt;
    private UUID lastCommentUserId;

    private Long bestAnswerId;
    private LocalDateTime bestAnswerSelectedAt;
    private UUID bestAnswerSelectedBy;

    @Min(value = 0, message = "解決ポイントは0以上である必要があります")
    private Integer solutionPoints = 0;

    private LocalDateTime autoCloseAt;
    private LocalDateTime closedAt;
    private UUID closedBy;

    @Size(max = 500, message = "クローズ理由は500文字以内で入力してください")
    private String closeReason;

    private LocalDateTime archivedAt;
    private UUID archivedBy;

    @Size(max = 500, message = "アーカイブ理由は500文字以内で入力してください")
    private String archiveReason;

    private String metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    // 計算フィールド
    private BigDecimal engagementRate;
    private BigDecimal solutionRate;
    private BigDecimal healthScore;
    private Boolean isPopular;
    private Boolean isHighQuality;
    private Boolean isActive;
    private Boolean shouldAutoClose;
    private Boolean needsModeration;
    private List<String> tagList;

    // フォーラム情報（結合用）
    private String forumTitle;
    private String forumCategory;
    private String creatorUsername;
    private String lastActivityUsername;

    // Constructors
    public LearningTopicDto() {
    }

    public LearningTopicDto(Long forumId, UUID creatorId, String title, String content,
            LearningTopic.TopicType topicType, LearningTopic.TopicPriority priority) {
        this.forumId = forumId;
        this.creatorId = creatorId;
        this.title = title;
        this.content = content;
        this.topicType = topicType;
        this.topicStatus = LearningTopic.TopicStatus.ACTIVE;
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
        if (!LearningTopic.TopicType.QUESTION.equals(topicType))
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
        if (LearningTopic.TopicStatus.LOCKED.equals(topicStatus)
                || LearningTopic.TopicStatus.ARCHIVED.equals(topicStatus)) {
            healthScore -= 40.0;
        } else if (LearningTopic.TopicStatus.CLOSED.equals(topicStatus)) {
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
    public Boolean calculateIsPopular() {
        BigDecimal popularityScore = calculatePopularityScore();
        return popularityScore.compareTo(BigDecimal.valueOf(70.0)) >= 0;
    }

    /**
     * 高品質判定
     */
    public Boolean calculateIsHighQuality() {
        BigDecimal qualityScore = calculateQualityScore();
        return qualityScore.compareTo(BigDecimal.valueOf(80.0)) >= 0;
    }

    /**
     * アクティブ判定
     */
    public Boolean calculateIsActive() {
        if (!LearningTopic.TopicStatus.ACTIVE.equals(topicStatus))
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
    public Boolean calculateShouldAutoClose() {
        if (autoCloseAt == null)
            return false;
        if (!LearningTopic.TopicStatus.ACTIVE.equals(topicStatus))
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
    public Boolean calculateNeedsModeration() {
        return Boolean.TRUE.equals(requiresModeration) &&
                (moderationStatus == null || LearningTopic.ModerationStatus.PENDING.equals(moderationStatus));
    }

    /**
     * タグリスト取得
     */
    public List<String> getTagList() {
        if (tagList != null)
            return tagList;

        if (tags == null || tags.trim().isEmpty()) {
            this.tagList = java.util.List.of();
        } else {
            this.tagList = java.util.Arrays.asList(tags.split(","))
                    .stream()
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .collect(java.util.stream.Collectors.toList());
        }
        return this.tagList;
    }

    /**
     * タグリスト設定
     */
    public void setTagList(List<String> tagList) {
        this.tagList = tagList;
        if (tagList == null || tagList.isEmpty()) {
            this.tags = null;
        } else {
            this.tags = String.join(",", tagList);
        }
    }

    /**
     * 計算フィールド更新
     */
    public void updateCalculatedFields() {
        this.activityScore = calculateActivityScore();
        this.qualityScore = calculateQualityScore();
        this.popularityScore = calculatePopularityScore();
        this.overallScore = calculateOverallScore();
        this.engagementRate = calculateEngagementRate();
        this.solutionRate = calculateSolutionRate();
        this.healthScore = calculateHealthScore();
        this.isPopular = calculateIsPopular();
        this.isHighQuality = calculateIsHighQuality();
        this.isActive = calculateIsActive();
        this.shouldAutoClose = calculateShouldAutoClose();
        this.needsModeration = calculateNeedsModeration();
    }

    /**
     * 優先度の説明取得
     */
    public String getPriorityDescription() {
        if (priority == null)
            return "";
        return switch (priority) {
            case LOW -> "低";
            case NORMAL -> "通常";
            case HIGH -> "高";
            case URGENT -> "緊急";
            case CRITICAL -> "クリティカル";
        };
    }

    /**
     * ステータスの説明取得
     */
    public String getStatusDescription() {
        if (topicStatus == null)
            return "";
        return switch (topicStatus) {
            case ACTIVE -> "アクティブ";
            case CLOSED -> "クローズ";
            case LOCKED -> "ロック";
            case ARCHIVED -> "アーカイブ";
            case PENDING -> "承認待ち";
            case REJECTED -> "拒否";
            case DELETED -> "削除";
            case UNDER_REVIEW -> "レビュー中";
            case SUSPENDED -> "停止";
        };
    }

    /**
     * トピックタイプの説明取得
     */
    public String getTypeDescription() {
        if (topicType == null)
            return "";
        return switch (topicType) {
            case DISCUSSION -> "ディスカッション";
            case QUESTION -> "質問";
            case TUTORIAL -> "チュートリアル";
            case SHOWCASE -> "作品紹介";
            case ANNOUNCEMENT -> "お知らせ";
            case POLL -> "投票";
            case EVENT -> "イベント";
            case RESOURCE -> "リソース";
            case FEEDBACK -> "フィードバック";
            case BUG_REPORT -> "バグ報告";
            case FEATURE_REQUEST -> "機能要望";
            case COLLABORATION -> "協力";
            case STUDY_GROUP -> "勉強会";
            case JOB_POSTING -> "求人";
            case OTHER -> "その他";
        };
    }

    /**
     * モデレーションステータスの説明取得
     */
    public String getModerationStatusDescription() {
        if (moderationStatus == null)
            return "";
        return switch (moderationStatus) {
            case PENDING -> "承認待ち";
            case APPROVED -> "承認済み";
            case REJECTED -> "拒否";
            case FLAGGED -> "フラグ付き";
            case UNDER_REVIEW -> "レビュー中";
            case AUTO_APPROVED -> "自動承認";
            case REQUIRES_ATTENTION -> "要注意";
        };
    }

    /**
     * 経過時間の説明取得
     */
    public String getTimeAgoDescription() {
        if (createdAt == null)
            return "";

        long minutes = java.time.Duration.between(createdAt, LocalDateTime.now()).toMinutes();
        if (minutes < 60)
            return minutes + "分前";

        long hours = minutes / 60;
        if (hours < 24)
            return hours + "時間前";

        long days = hours / 24;
        if (days < 30)
            return days + "日前";

        long months = days / 30;
        if (months < 12)
            return months + "ヶ月前";

        long years = months / 12;
        return years + "年前";
    }

    /**
     * 最後のアクティビティからの経過時間説明取得
     */
    public String getLastActivityAgoDescription() {
        if (lastActivityAt == null)
            return "アクティビティなし";

        long minutes = java.time.Duration.between(lastActivityAt, LocalDateTime.now()).toMinutes();
        if (minutes < 60)
            return minutes + "分前";

        long hours = minutes / 60;
        if (hours < 24)
            return hours + "時間前";

        long days = hours / 24;
        if (days < 30)
            return days + "日前";

        long months = days / 30;
        if (months < 12)
            return months + "ヶ月前";

        long years = months / 12;
        return years + "年前";
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

    public LearningTopic.TopicType getTopicType() {
        return topicType;
    }

    public void setTopicType(LearningTopic.TopicType topicType) {
        this.topicType = topicType;
    }

    public LearningTopic.TopicStatus getTopicStatus() {
        return topicStatus;
    }

    public void setTopicStatus(LearningTopic.TopicStatus topicStatus) {
        this.topicStatus = topicStatus;
    }

    public LearningTopic.TopicPriority getPriority() {
        return priority;
    }

    public void setPriority(LearningTopic.TopicPriority priority) {
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

    public LearningTopic.ModerationStatus getModerationStatus() {
        return moderationStatus;
    }

    public void setModerationStatus(LearningTopic.ModerationStatus moderationStatus) {
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

    public BigDecimal getEngagementRate() {
        return engagementRate;
    }

    public void setEngagementRate(BigDecimal engagementRate) {
        this.engagementRate = engagementRate;
    }

    public BigDecimal getSolutionRate() {
        return solutionRate;
    }

    public void setSolutionRate(BigDecimal solutionRate) {
        this.solutionRate = solutionRate;
    }

    public BigDecimal getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(BigDecimal healthScore) {
        this.healthScore = healthScore;
    }

    public Boolean getIsPopular() {
        return isPopular;
    }

    public void setIsPopular(Boolean isPopular) {
        this.isPopular = isPopular;
    }

    public Boolean getIsHighQuality() {
        return isHighQuality;
    }

    public void setIsHighQuality(Boolean isHighQuality) {
        this.isHighQuality = isHighQuality;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getShouldAutoClose() {
        return shouldAutoClose;
    }

    public void setShouldAutoClose(Boolean shouldAutoClose) {
        this.shouldAutoClose = shouldAutoClose;
    }

    public Boolean getNeedsModeration() {
        return needsModeration;
    }

    public void setNeedsModeration(Boolean needsModeration) {
        this.needsModeration = needsModeration;
    }

    public String getForumTitle() {
        return forumTitle;
    }

    public void setForumTitle(String forumTitle) {
        this.forumTitle = forumTitle;
    }

    public String getForumCategory() {
        return forumCategory;
    }

    public void setForumCategory(String forumCategory) {
        this.forumCategory = forumCategory;
    }

    public String getCreatorUsername() {
        return creatorUsername;
    }

    public void setCreatorUsername(String creatorUsername) {
        this.creatorUsername = creatorUsername;
    }

    public String getLastActivityUsername() {
        return lastActivityUsername;
    }

    public void setLastActivityUsername(String lastActivityUsername) {
        this.lastActivityUsername = lastActivityUsername;
    }
}
