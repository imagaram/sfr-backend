package com.sfr.tokyo.sfr_backend.dto.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningComment;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 学習コメント DTO - LearningCommentDto
 */
public class LearningCommentDto {

    private Long id;

    @NotNull(message = "トピックIDは必須です")
    private Long topicId;

    @NotNull(message = "作成者IDは必須です")
    private UUID authorId;

    private Long parentCommentId;

    @NotBlank(message = "内容は必須です")
    @Size(max = 10000, message = "内容は10000文字以内で入力してください")
    private String content;

    @NotNull(message = "コメントタイプは必須です")
    private LearningComment.CommentType commentType;

    @NotNull(message = "コメントステータスは必須です")
    private LearningComment.CommentStatus commentStatus;

    @Min(value = 0, message = "いいね数は0以上である必要があります")
    private Integer likeCount = 0;

    @Min(value = 0, message = "よくないね数は0以上である必要があります")
    private Integer dislikeCount = 0;

    @Min(value = 0, message = "返信数は0以上である必要があります")
    private Integer replyCount = 0;

    @Min(value = 0, message = "報告数は0以上である必要があります")
    private Integer reportCount = 0;

    private Boolean isBestAnswer = false;
    private Boolean isSolution = false;
    private Boolean isPinned = false;
    private Boolean isFeatured = false;
    private Boolean isHighlighted = false;
    private Boolean requiresModeration = false;

    private LearningComment.ModerationStatus moderationStatus;
    private UUID moderatorId;
    private String moderationNotes;
    private LocalDateTime moderatedAt;

    @DecimalMin(value = "0.00", message = "品質スコアは0以上である必要があります")
    @DecimalMax(value = "100.00", message = "品質スコアは100以下である必要があります")
    private BigDecimal qualityScore = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "有用性スコアは0以上である必要があります")
    @DecimalMax(value = "100.00", message = "有用性スコアは100以下である必要があります")
    private BigDecimal helpfulnessScore = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "関連性スコアは0以上である必要があります")
    @DecimalMax(value = "100.00", message = "関連性スコアは100以下である必要があります")
    private BigDecimal relevanceScore = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "総合スコアは0以上である必要があります")
    @DecimalMax(value = "100.00", message = "総合スコアは100以下である必要があります")
    private BigDecimal overallScore = BigDecimal.ZERO;

    private LocalDateTime selectedAsBestAt;
    private UUID selectedAsBestBy;

    @Min(value = 0, message = "ベストアンサーポイントは0以上である必要があります")
    private Integer bestAnswerPoints = 0;

    @Min(value = 0, message = "深度レベルは0以上である必要があります")
    @Max(value = 10, message = "深度レベルは10以下である必要があります")
    private Integer depthLevel = 0;

    @Min(value = 1, message = "スレッド位置は1以上である必要があります")
    private Integer threadPosition = 1;

    private LocalDateTime lastEditedAt;
    private UUID lastEditedBy;

    @Size(max = 500, message = "編集理由は500文字以内で入力してください")
    private String editReason;

    @Min(value = 0, message = "編集回数は0以上である必要があります")
    private Integer editCount = 0;

    private String attachments;
    private String mentions;
    private String metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    // 計算フィールド
    private Boolean isHighQuality;
    private Boolean isHelpful;
    private Boolean isPopular;
    private Boolean isEdited;
    private Boolean isReported;
    private Boolean needsModeration;
    private Boolean isVisible;
    private Boolean canReply;

    // 関連情報（結合用）
    private String topicTitle;
    private String authorUsername;
    private String moderatorUsername;
    private String parentAuthorUsername;
    private List<LearningCommentDto> replies;
    private Integer totalReplies;

    // Constructors
    public LearningCommentDto() {
    }

    public LearningCommentDto(Long topicId, UUID authorId, String content, LearningComment.CommentType commentType) {
        this.topicId = topicId;
        this.authorId = authorId;
        this.content = content;
        this.commentType = commentType;
        this.commentStatus = LearningComment.CommentStatus.ACTIVE;
    }

    public LearningCommentDto(Long topicId, UUID authorId, Long parentCommentId, String content,
            LearningComment.CommentType commentType) {
        this(topicId, authorId, content, commentType);
        this.parentCommentId = parentCommentId;
        this.depthLevel = 1;
    }

    // Business Logic Methods

    /**
     * 品質スコア計算
     */
    public BigDecimal calculateQualityScore() {
        double baseScore = 50.0; // ベーススコア

        // 内容の長さによるスコア調整
        if (content != null) {
            int contentLength = content.length();
            if (contentLength >= 500)
                baseScore += 20.0;
            else if (contentLength >= 200)
                baseScore += 15.0;
            else if (contentLength >= 100)
                baseScore += 10.0;
            else if (contentLength >= 50)
                baseScore += 5.0;
        }

        // いいね率によるスコア調整
        if (likeCount != null && dislikeCount != null) {
            int totalVotes = likeCount + dislikeCount;
            if (totalVotes > 0) {
                double likeRatio = (double) likeCount / totalVotes;
                baseScore += likeRatio * 20.0;
            }
        }

        // ベストアンサーボーナス
        if (Boolean.TRUE.equals(isBestAnswer)) {
            baseScore += 15.0;
        }

        // ソリューションボーナス
        if (Boolean.TRUE.equals(isSolution)) {
            baseScore += 10.0;
        }

        // 返信数によるエンゲージメントスコア
        if (replyCount != null && replyCount > 0) {
            baseScore += Math.min(replyCount * 2.0, 10.0);
        }

        // 報告数による減点
        if (reportCount != null && reportCount > 0) {
            baseScore -= Math.min(reportCount * 5.0, 25.0);
        }

        return BigDecimal.valueOf(Math.max(0.0, Math.min(baseScore, 100.0)))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 有用性スコア計算
     */
    public BigDecimal calculateHelpfulnessScore() {
        double score = 0.0;

        // いいね数スコア
        if (likeCount != null) {
            score += Math.min(likeCount * 5.0, 40.0);
        }

        // ベストアンサーボーナス
        if (Boolean.TRUE.equals(isBestAnswer)) {
            score += 30.0;
        }

        // ソリューションボーナス
        if (Boolean.TRUE.equals(isSolution)) {
            score += 20.0;
        }

        // 返信がある場合のエンゲージメントボーナス
        if (replyCount != null && replyCount > 0) {
            score += Math.min(replyCount * 3.0, 15.0);
        }

        // よくないね数による減点
        if (dislikeCount != null && dislikeCount > 0) {
            score -= Math.min(dislikeCount * 3.0, 20.0);
        }

        return BigDecimal.valueOf(Math.max(0.0, Math.min(score, 100.0)))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 関連性スコア計算
     */
    public BigDecimal calculateRelevanceScore() {
        double score = 70.0; // デフォルト関連性スコア

        // コメントタイプによるスコア調整
        if (commentType != null) {
            switch (commentType) {
                case ANSWER -> score += 20.0;
                case CLARIFICATION -> score += 15.0;
                case EXAMPLE -> score += 10.0;
                case REFERENCE -> score += 5.0;
                case OPINION -> score -= 5.0;
                case EXPERIENCE -> score += 0.0;
                default -> score += 0.0;
            }
        }

        // 深度レベルによる調整（深すぎると関連性が下がる）
        if (depthLevel != null && depthLevel > 3) {
            score -= (depthLevel - 3) * 5.0;
        }

        // ピン留めされている場合は関連性が高い
        if (Boolean.TRUE.equals(isPinned)) {
            score += 10.0;
        }

        return BigDecimal.valueOf(Math.max(0.0, Math.min(score, 100.0)))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 総合スコア計算
     */
    public BigDecimal calculateOverallScore() {
        BigDecimal quality = calculateQualityScore();
        BigDecimal helpfulness = calculateHelpfulnessScore();
        BigDecimal relevance = calculateRelevanceScore();

        // 重み付き平均: 品質40%, 有用性40%, 関連性20%
        return quality.multiply(BigDecimal.valueOf(0.4))
                .add(helpfulness.multiply(BigDecimal.valueOf(0.4)))
                .add(relevance.multiply(BigDecimal.valueOf(0.2)))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 高品質判定
     */
    public Boolean calculateIsHighQuality() {
        BigDecimal qualityScore = calculateQualityScore();
        return qualityScore.compareTo(BigDecimal.valueOf(80.0)) >= 0;
    }

    /**
     * 有用判定
     */
    public Boolean calculateIsHelpful() {
        BigDecimal helpfulnessScore = calculateHelpfulnessScore();
        return helpfulnessScore.compareTo(BigDecimal.valueOf(70.0)) >= 0;
    }

    /**
     * 人気判定
     */
    public Boolean calculateIsPopular() {
        return likeCount != null && likeCount >= 5;
    }

    /**
     * 編集済み判定
     */
    public Boolean calculateIsEdited() {
        return editCount != null && editCount > 0;
    }

    /**
     * 報告対象判定
     */
    public Boolean calculateIsReported() {
        return reportCount != null && reportCount > 0;
    }

    /**
     * モデレーション必要判定
     */
    public Boolean calculateNeedsModeration() {
        return Boolean.TRUE.equals(requiresModeration) &&
                (moderationStatus == null ||
                        LearningComment.ModerationStatus.PENDING.equals(moderationStatus) ||
                        LearningComment.ModerationStatus.NEEDS_REVIEW.equals(moderationStatus));
    }

    /**
     * 表示可能判定
     */
    public Boolean calculateIsVisible() {
        return LearningComment.CommentStatus.ACTIVE.equals(commentStatus) && deletedAt == null;
    }

    /**
     * 返信可能判定
     */
    public Boolean calculateCanReply() {
        Boolean visible = calculateIsVisible();
        return Boolean.TRUE.equals(visible) &&
                !Boolean.TRUE.equals(isPinned) &&
                depthLevel != null && depthLevel < 10;
    }

    /**
     * 計算フィールド更新
     */
    public void updateCalculatedFields() {
        this.qualityScore = calculateQualityScore();
        this.helpfulnessScore = calculateHelpfulnessScore();
        this.relevanceScore = calculateRelevanceScore();
        this.overallScore = calculateOverallScore();
        this.isHighQuality = calculateIsHighQuality();
        this.isHelpful = calculateIsHelpful();
        this.isPopular = calculateIsPopular();
        this.isEdited = calculateIsEdited();
        this.isReported = calculateIsReported();
        this.needsModeration = calculateNeedsModeration();
        this.isVisible = calculateIsVisible();
        this.canReply = calculateCanReply();
    }

    /**
     * コメントタイプの説明取得
     */
    public String getTypeDescription() {
        if (commentType == null)
            return "";
        return switch (commentType) {
            case COMMENT -> "コメント";
            case ANSWER -> "回答";
            case QUESTION -> "質問";
            case CLARIFICATION -> "質問明確化";
            case SUGGESTION -> "提案";
            case FEEDBACK -> "フィードバック";
            case CORRECTION -> "訂正";
            case ADDITION -> "追加情報";
            case EXAMPLE -> "例示";
            case REFERENCE -> "参考資料";
            case SUMMARY -> "まとめ";
            case OPINION -> "意見";
            case EXPERIENCE -> "体験談";
            case WARNING -> "注意点";
            case TIP -> "ヒント";
        };
    }

    /**
     * ステータスの説明取得
     */
    public String getStatusDescription() {
        if (commentStatus == null)
            return "";
        return switch (commentStatus) {
            case ACTIVE -> "アクティブ";
            case HIDDEN -> "非表示";
            case DELETED -> "削除";
            case PENDING -> "承認待ち";
            case REJECTED -> "拒否";
            case FLAGGED -> "フラグ付き";
            case ARCHIVED -> "アーカイブ";
            case UNDER_REVIEW -> "レビュー中";
            case SPAM -> "スパム";
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
            case NEEDS_REVIEW -> "要レビュー";
            case ESCALATED -> "エスカレート";
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
     * 最終編集からの経過時間説明取得
     */
    public String getLastEditAgoDescription() {
        if (lastEditedAt == null)
            return "";

        long minutes = java.time.Duration.between(lastEditedAt, LocalDateTime.now()).toMinutes();
        if (minutes < 60)
            return minutes + "分前に編集";

        long hours = minutes / 60;
        if (hours < 24)
            return hours + "時間前に編集";

        long days = hours / 24;
        if (days < 30)
            return days + "日前に編集";

        long months = days / 30;
        if (months < 12)
            return months + "ヶ月前に編集";

        long years = months / 12;
        return years + "年前に編集";
    }

    /**
     * いいね率計算
     */
    public BigDecimal calculateLikeRatio() {
        if (likeCount == null || dislikeCount == null)
            return BigDecimal.ZERO;

        int totalVotes = likeCount + dislikeCount;
        if (totalVotes == 0)
            return BigDecimal.ZERO;

        return BigDecimal.valueOf((double) likeCount / totalVotes * 100)
                .setScale(1, java.math.RoundingMode.HALF_UP);
    }

    /**
     * エンゲージメント率計算
     */
    public BigDecimal calculateEngagementRate() {
        if (likeCount == null || dislikeCount == null || replyCount == null)
            return BigDecimal.ZERO;

        // エンゲージメント = いいね + よくないね + 返信数
        int totalEngagement = likeCount + dislikeCount + replyCount;

        // 仮想的な閲覧数（実際の閲覧数がない場合の推定値）
        int estimatedViews = Math.max(totalEngagement * 10, 1);

        return BigDecimal.valueOf((double) totalEngagement / estimatedViews * 100)
                .setScale(1, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 深度レベルのインデント取得
     */
    public String getDepthIndent() {
        if (depthLevel == null || depthLevel == 0)
            return "";

        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < depthLevel; i++) {
            indent.append("  "); // 2スペース × 深度レベル
        }
        return indent.toString();
    }

    /**
     * 返信可能な最大深度チェック
     */
    public Boolean canAddReply() {
        return calculateCanReply() && (depthLevel == null || depthLevel < 9); // 最大深度10まで
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public void setAuthorId(UUID authorId) {
        this.authorId = authorId;
    }

    public Long getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(Long parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LearningComment.CommentType getCommentType() {
        return commentType;
    }

    public void setCommentType(LearningComment.CommentType commentType) {
        this.commentType = commentType;
    }

    public LearningComment.CommentStatus getCommentStatus() {
        return commentStatus;
    }

    public void setCommentStatus(LearningComment.CommentStatus commentStatus) {
        this.commentStatus = commentStatus;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getDislikeCount() {
        return dislikeCount;
    }

    public void setDislikeCount(Integer dislikeCount) {
        this.dislikeCount = dislikeCount;
    }

    public Integer getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(Integer replyCount) {
        this.replyCount = replyCount;
    }

    public Integer getReportCount() {
        return reportCount;
    }

    public void setReportCount(Integer reportCount) {
        this.reportCount = reportCount;
    }

    public Boolean getIsBestAnswer() {
        return isBestAnswer;
    }

    public void setIsBestAnswer(Boolean isBestAnswer) {
        this.isBestAnswer = isBestAnswer;
    }

    public Boolean getIsSolution() {
        return isSolution;
    }

    public void setIsSolution(Boolean isSolution) {
        this.isSolution = isSolution;
    }

    public Boolean getIsPinned() {
        return isPinned;
    }

    public void setIsPinned(Boolean isPinned) {
        this.isPinned = isPinned;
    }

    public Boolean getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(Boolean isFeatured) {
        this.isFeatured = isFeatured;
    }

    public Boolean getIsHighlighted() {
        return isHighlighted;
    }

    public void setIsHighlighted(Boolean isHighlighted) {
        this.isHighlighted = isHighlighted;
    }

    public Boolean getRequiresModeration() {
        return requiresModeration;
    }

    public void setRequiresModeration(Boolean requiresModeration) {
        this.requiresModeration = requiresModeration;
    }

    public LearningComment.ModerationStatus getModerationStatus() {
        return moderationStatus;
    }

    public void setModerationStatus(LearningComment.ModerationStatus moderationStatus) {
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

    public BigDecimal getQualityScore() {
        return qualityScore;
    }

    public void setQualityScore(BigDecimal qualityScore) {
        this.qualityScore = qualityScore;
    }

    public BigDecimal getHelpfulnessScore() {
        return helpfulnessScore;
    }

    public void setHelpfulnessScore(BigDecimal helpfulnessScore) {
        this.helpfulnessScore = helpfulnessScore;
    }

    public BigDecimal getRelevanceScore() {
        return relevanceScore;
    }

    public void setRelevanceScore(BigDecimal relevanceScore) {
        this.relevanceScore = relevanceScore;
    }

    public BigDecimal getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(BigDecimal overallScore) {
        this.overallScore = overallScore;
    }

    public LocalDateTime getSelectedAsBestAt() {
        return selectedAsBestAt;
    }

    public void setSelectedAsBestAt(LocalDateTime selectedAsBestAt) {
        this.selectedAsBestAt = selectedAsBestAt;
    }

    public UUID getSelectedAsBestBy() {
        return selectedAsBestBy;
    }

    public void setSelectedAsBestBy(UUID selectedAsBestBy) {
        this.selectedAsBestBy = selectedAsBestBy;
    }

    public Integer getBestAnswerPoints() {
        return bestAnswerPoints;
    }

    public void setBestAnswerPoints(Integer bestAnswerPoints) {
        this.bestAnswerPoints = bestAnswerPoints;
    }

    public Integer getDepthLevel() {
        return depthLevel;
    }

    public void setDepthLevel(Integer depthLevel) {
        this.depthLevel = depthLevel;
    }

    public Integer getThreadPosition() {
        return threadPosition;
    }

    public void setThreadPosition(Integer threadPosition) {
        this.threadPosition = threadPosition;
    }

    public LocalDateTime getLastEditedAt() {
        return lastEditedAt;
    }

    public void setLastEditedAt(LocalDateTime lastEditedAt) {
        this.lastEditedAt = lastEditedAt;
    }

    public UUID getLastEditedBy() {
        return lastEditedBy;
    }

    public void setLastEditedBy(UUID lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    public String getEditReason() {
        return editReason;
    }

    public void setEditReason(String editReason) {
        this.editReason = editReason;
    }

    public Integer getEditCount() {
        return editCount;
    }

    public void setEditCount(Integer editCount) {
        this.editCount = editCount;
    }

    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }

    public String getMentions() {
        return mentions;
    }

    public void setMentions(String mentions) {
        this.mentions = mentions;
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

    public Boolean getIsHighQuality() {
        return isHighQuality;
    }

    public void setIsHighQuality(Boolean isHighQuality) {
        this.isHighQuality = isHighQuality;
    }

    public Boolean getIsHelpful() {
        return isHelpful;
    }

    public void setIsHelpful(Boolean isHelpful) {
        this.isHelpful = isHelpful;
    }

    public Boolean getIsPopular() {
        return isPopular;
    }

    public void setIsPopular(Boolean isPopular) {
        this.isPopular = isPopular;
    }

    public Boolean getIsEdited() {
        return isEdited;
    }

    public void setIsEdited(Boolean isEdited) {
        this.isEdited = isEdited;
    }

    public Boolean getIsReported() {
        return isReported;
    }

    public void setIsReported(Boolean isReported) {
        this.isReported = isReported;
    }

    public Boolean getNeedsModeration() {
        return needsModeration;
    }

    public void setNeedsModeration(Boolean needsModeration) {
        this.needsModeration = needsModeration;
    }

    public Boolean getIsVisible() {
        return isVisible;
    }

    public void setIsVisible(Boolean isVisible) {
        this.isVisible = isVisible;
    }

    public Boolean getCanReply() {
        return canReply;
    }

    public void setCanReply(Boolean canReply) {
        this.canReply = canReply;
    }

    public String getTopicTitle() {
        return topicTitle;
    }

    public void setTopicTitle(String topicTitle) {
        this.topicTitle = topicTitle;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public String getModeratorUsername() {
        return moderatorUsername;
    }

    public void setModeratorUsername(String moderatorUsername) {
        this.moderatorUsername = moderatorUsername;
    }

    public String getParentAuthorUsername() {
        return parentAuthorUsername;
    }

    public void setParentAuthorUsername(String parentAuthorUsername) {
        this.parentAuthorUsername = parentAuthorUsername;
    }

    public List<LearningCommentDto> getReplies() {
        return replies;
    }

    public void setReplies(List<LearningCommentDto> replies) {
        this.replies = replies;
    }

    public Integer getTotalReplies() {
        return totalReplies;
    }

    public void setTotalReplies(Integer totalReplies) {
        this.totalReplies = totalReplies;
    }
}
