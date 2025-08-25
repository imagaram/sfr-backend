package com.sfr.tokyo.sfr_backend.entity.learning;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 学習コメント エンティティ - LearningComment
 */
@Entity
@Table(name = "learning_comment", indexes = {
        @Index(name = "idx_comment_topic_id", columnList = "topic_id"),
        @Index(name = "idx_comment_author_id", columnList = "author_id"),
        @Index(name = "idx_comment_parent_id", columnList = "parent_comment_id"),
        @Index(name = "idx_comment_status", columnList = "comment_status"),
        @Index(name = "idx_comment_type", columnList = "comment_type"),
        @Index(name = "idx_comment_created_at", columnList = "created_at"),
        @Index(name = "idx_comment_like_count", columnList = "like_count"),
        @Index(name = "idx_comment_quality_score", columnList = "quality_score"),
        @Index(name = "idx_comment_is_best_answer", columnList = "is_best_answer"),
        @Index(name = "idx_comment_is_pinned", columnList = "is_pinned"),
        @Index(name = "idx_comment_is_solution", columnList = "is_solution"),
        @Index(name = "idx_comment_thread", columnList = "topic_id, parent_comment_id, created_at"),
        @Index(name = "idx_comment_moderation", columnList = "requires_moderation, moderation_status"),
        @Index(name = "idx_comment_active", columnList = "comment_status, created_at"),
        @Index(name = "idx_comment_popular", columnList = "like_count, quality_score")
})
public class LearningComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "topic_id", nullable = false)
    @NotNull(message = "トピックIDは必須です")
    private Long topicId;

    @Column(name = "author_id", nullable = false)
    @NotNull(message = "作成者IDは必須です")
    private UUID authorId;

    @Column(name = "parent_comment_id")
    private Long parentCommentId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "内容は必須です")
    @Size(max = 10000, message = "内容は10000文字以内で入力してください")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "comment_type", nullable = false, length = 50)
    @NotNull(message = "コメントタイプは必須です")
    private CommentType commentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "comment_status", nullable = false, length = 50)
    @NotNull(message = "コメントステータスは必須です")
    private CommentStatus commentStatus;

    @Column(name = "like_count", nullable = false)
    @Min(value = 0, message = "いいね数は0以上である必要があります")
    private Integer likeCount = 0;

    @Column(name = "dislike_count", nullable = false)
    @Min(value = 0, message = "よくないね数は0以上である必要があります")
    private Integer dislikeCount = 0;

    @Column(name = "reply_count", nullable = false)
    @Min(value = 0, message = "返信数は0以上である必要があります")
    private Integer replyCount = 0;

    @Column(name = "report_count", nullable = false)
    @Min(value = 0, message = "報告数は0以上である必要があります")
    private Integer reportCount = 0;

    @Column(name = "is_best_answer", nullable = false)
    private Boolean isBestAnswer = false;

    @Column(name = "is_solution", nullable = false)
    private Boolean isSolution = false;

    @Column(name = "is_pinned", nullable = false)
    private Boolean isPinned = false;

    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;

    @Column(name = "is_highlighted", nullable = false)
    private Boolean isHighlighted = false;

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

    @Column(name = "quality_score", precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "品質スコアは0以上である必要があります")
    @DecimalMax(value = "100.00", message = "品質スコアは100以下である必要があります")
    private BigDecimal qualityScore = BigDecimal.ZERO;

    @Column(name = "helpfulness_score", precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "有用性スコアは0以上である必要があります")
    @DecimalMax(value = "100.00", message = "有用性スコアは100以下である必要があります")
    private BigDecimal helpfulnessScore = BigDecimal.ZERO;

    @Column(name = "relevance_score", precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "関連性スコアは0以上である必要があります")
    @DecimalMax(value = "100.00", message = "関連性スコアは100以下である必要があります")
    private BigDecimal relevanceScore = BigDecimal.ZERO;

    @Column(name = "overall_score", precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "総合スコアは0以上である必要があります")
    @DecimalMax(value = "100.00", message = "総合スコアは100以下である必要があります")
    private BigDecimal overallScore = BigDecimal.ZERO;

    @Column(name = "selected_as_best_at")
    private LocalDateTime selectedAsBestAt;

    @Column(name = "selected_as_best_by")
    private UUID selectedAsBestBy;

    @Column(name = "best_answer_points", nullable = false)
    @Min(value = 0, message = "ベストアンサーポイントは0以上である必要があります")
    private Integer bestAnswerPoints = 0;

    @Column(name = "depth_level", nullable = false)
    @Min(value = 0, message = "深度レベルは0以上である必要があります")
    @Max(value = 10, message = "深度レベルは10以下である必要があります")
    private Integer depthLevel = 0;

    @Column(name = "thread_position", nullable = false)
    @Min(value = 1, message = "スレッド位置は1以上である必要があります")
    private Integer threadPosition = 1;

    @Column(name = "last_edited_at")
    private LocalDateTime lastEditedAt;

    @Column(name = "last_edited_by")
    private UUID lastEditedBy;

    @Column(name = "edit_reason", length = 500)
    @Size(max = 500, message = "編集理由は500文字以内で入力してください")
    private String editReason;

    @Column(name = "edit_count", nullable = false)
    @Min(value = 0, message = "編集回数は0以上である必要があります")
    private Integer editCount = 0;

    @Column(name = "attachments", columnDefinition = "JSON")
    private String attachments;

    @Column(name = "mentions", columnDefinition = "JSON")
    private String mentions;

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

    // コメントタイプ
    public enum CommentType {
        COMMENT, // 通常コメント
        ANSWER, // 回答
        QUESTION, // 質問
        CLARIFICATION, // 質問明確化
        SUGGESTION, // 提案
        FEEDBACK, // フィードバック
        CORRECTION, // 訂正
        ADDITION, // 追加情報
        EXAMPLE, // 例示
        REFERENCE, // 参考資料
        SUMMARY, // まとめ
        OPINION, // 意見
        EXPERIENCE, // 体験談
        WARNING, // 注意点
        TIP // ヒント
    }

    // コメントステータス
    public enum CommentStatus {
        ACTIVE, // アクティブ
        HIDDEN, // 非表示
        DELETED, // 削除
        PENDING, // 承認待ち
        REJECTED, // 拒否
        FLAGGED, // フラグ付き
        ARCHIVED, // アーカイブ
        UNDER_REVIEW, // レビュー中
        SPAM // スパム
    }

    // モデレーションステータス
    public enum ModerationStatus {
        PENDING, // 承認待ち
        APPROVED, // 承認済み
        REJECTED, // 拒否
        FLAGGED, // フラグ付き
        UNDER_REVIEW, // レビュー中
        AUTO_APPROVED, // 自動承認
        NEEDS_REVIEW, // 要レビュー
        ESCALATED // エスカレート
    }

    // Constructors
    public LearningComment() {
    }

    public LearningComment(Long topicId, UUID authorId, String content, CommentType commentType) {
        this.topicId = topicId;
        this.authorId = authorId;
        this.content = content;
        this.commentType = commentType;
        this.commentStatus = CommentStatus.ACTIVE;
    }

    public LearningComment(Long topicId, UUID authorId, Long parentCommentId, String content, CommentType commentType) {
        this(topicId, authorId, content, commentType);
        this.parentCommentId = parentCommentId;
        this.depthLevel = 1; // 親コメントがある場合は深度1から開始
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
     * ベストアンサー設定
     */
    public void setBestAnswer(UUID selectedBy) {
        this.isBestAnswer = true;
        this.selectedAsBestAt = LocalDateTime.now();
        this.selectedAsBestBy = selectedBy;
        this.commentType = CommentType.ANSWER;
    }

    /**
     * ベストアンサー解除
     */
    public void clearBestAnswer() {
        this.isBestAnswer = false;
        this.selectedAsBestAt = null;
        this.selectedAsBestBy = null;
        this.bestAnswerPoints = 0;
    }

    /**
     * ソリューションマーク設定
     */
    public void markAsSolution() {
        this.isSolution = true;
        this.commentType = CommentType.ANSWER;
    }

    /**
     * ソリューションマーク解除
     */
    public void unmarkAsSolution() {
        this.isSolution = false;
    }

    /**
     * 編集処理
     */
    public void edit(String newContent, UUID editedBy, String reason) {
        this.content = newContent;
        this.lastEditedAt = LocalDateTime.now();
        this.lastEditedBy = editedBy;
        this.editReason = reason;
        this.editCount = (this.editCount != null ? this.editCount : 0) + 1;
    }

    /**
     * 返信数増加
     */
    public void incrementReplyCount() {
        this.replyCount = (this.replyCount != null ? this.replyCount : 0) + 1;
    }

    /**
     * 返信数減少
     */
    public void decrementReplyCount() {
        this.replyCount = Math.max(0, (this.replyCount != null ? this.replyCount : 0) - 1);
    }

    /**
     * 報告数増加
     */
    public void incrementReportCount() {
        this.reportCount = (this.reportCount != null ? this.reportCount : 0) + 1;

        // 報告数が閾値を超えた場合、自動的にレビュー対象とする
        if (this.reportCount >= 3) {
            this.requiresModeration = true;
            this.moderationStatus = ModerationStatus.NEEDS_REVIEW;
        }
    }

    /**
     * モデレーション承認
     */
    public void approveModeration(UUID moderatorId, String notes) {
        this.moderationStatus = ModerationStatus.APPROVED;
        this.moderatorId = moderatorId;
        this.moderationNotes = notes;
        this.moderatedAt = LocalDateTime.now();
        this.requiresModeration = false;
        this.commentStatus = CommentStatus.ACTIVE;
    }

    /**
     * モデレーション拒否
     */
    public void rejectModeration(UUID moderatorId, String notes) {
        this.moderationStatus = ModerationStatus.REJECTED;
        this.moderatorId = moderatorId;
        this.moderationNotes = notes;
        this.moderatedAt = LocalDateTime.now();
        this.commentStatus = CommentStatus.REJECTED;
    }

    /**
     * スパムマーク
     */
    public void markAsSpam(UUID moderatorId) {
        this.commentStatus = CommentStatus.SPAM;
        this.moderationStatus = ModerationStatus.REJECTED;
        this.moderatorId = moderatorId;
        this.moderatedAt = LocalDateTime.now();
    }

    /**
     * 削除処理
     */
    public void delete() {
        this.commentStatus = CommentStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 非表示処理
     */
    public void hide() {
        this.commentStatus = CommentStatus.HIDDEN;
    }

    /**
     * 表示復帰
     */
    public void show() {
        this.commentStatus = CommentStatus.ACTIVE;
    }

    /**
     * アーカイブ処理
     */
    public void archive() {
        this.commentStatus = CommentStatus.ARCHIVED;
    }

    /**
     * 深度レベル計算・設定
     */
    public void calculateAndSetDepthLevel(LearningComment parentComment) {
        if (parentComment != null) {
            this.depthLevel = (parentComment.getDepthLevel() != null ? parentComment.getDepthLevel() : 0) + 1;
        } else {
            this.depthLevel = 0;
        }
    }

    /**
     * スレッド位置計算・設定
     */
    public void calculateAndSetThreadPosition(int position) {
        this.threadPosition = position;
    }

    /**
     * 高品質判定
     */
    public boolean isHighQuality() {
        return calculateQualityScore().compareTo(BigDecimal.valueOf(80.0)) >= 0;
    }

    /**
     * 有用判定
     */
    public boolean isHelpful() {
        return calculateHelpfulnessScore().compareTo(BigDecimal.valueOf(70.0)) >= 0;
    }

    /**
     * 人気判定
     */
    public boolean isPopular() {
        return likeCount != null && likeCount >= 5;
    }

    /**
     * 編集済み判定
     */
    public boolean isEdited() {
        return editCount != null && editCount > 0;
    }

    /**
     * 報告対象判定
     */
    public boolean isReported() {
        return reportCount != null && reportCount > 0;
    }

    /**
     * モデレーション必要判定
     */
    public boolean needsModeration() {
        return Boolean.TRUE.equals(requiresModeration) &&
                (moderationStatus == null ||
                        ModerationStatus.PENDING.equals(moderationStatus) ||
                        ModerationStatus.NEEDS_REVIEW.equals(moderationStatus));
    }

    /**
     * 表示可能判定
     */
    public boolean isVisible() {
        return CommentStatus.ACTIVE.equals(commentStatus) && deletedAt == null;
    }

    /**
     * 返信可能判定
     */
    public boolean canReply() {
        return isVisible() && !Boolean.TRUE.equals(isPinned) && depthLevel != null && depthLevel < 10;
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

    public CommentType getCommentType() {
        return commentType;
    }

    public void setCommentType(CommentType commentType) {
        this.commentType = commentType;
    }

    public CommentStatus getCommentStatus() {
        return commentStatus;
    }

    public void setCommentStatus(CommentStatus commentStatus) {
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
}
