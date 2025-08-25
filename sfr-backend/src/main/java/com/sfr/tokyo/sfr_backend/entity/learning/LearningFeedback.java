package com.sfr.tokyo.sfr_backend.entity.learning;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 学習フィードバック エンティティ - LearningFeedback
 */
@Entity
@Table(name = "learning_feedback", indexes = {
        @Index(name = "idx_feedback_target_id", columnList = "target_id"),
        @Index(name = "idx_feedback_target_type", columnList = "target_type"),
        @Index(name = "idx_feedback_giver_id", columnList = "feedback_giver_id"),
        @Index(name = "idx_feedback_receiver_id", columnList = "feedback_receiver_id"),
        @Index(name = "idx_feedback_type", columnList = "feedback_type"),
        @Index(name = "idx_feedback_category", columnList = "feedback_category"),
        @Index(name = "idx_feedback_status", columnList = "feedback_status"),
        @Index(name = "idx_feedback_rating", columnList = "rating"),
        @Index(name = "idx_feedback_created_at", columnList = "created_at"),
        @Index(name = "idx_feedback_is_anonymous", columnList = "is_anonymous"),
        @Index(name = "idx_feedback_is_helpful", columnList = "is_helpful"),
        @Index(name = "idx_feedback_helpfulness_score", columnList = "helpfulness_score"),
        @Index(name = "idx_feedback_composite", columnList = "target_type, target_id, feedback_type"),
        @Index(name = "idx_feedback_moderation", columnList = "requires_moderation, moderation_status"),
        @Index(name = "idx_feedback_quality", columnList = "quality_score, overall_score")
})
public class LearningFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "target_id", nullable = false)
    @NotNull(message = "対象IDは必須です")
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 50)
    @NotNull(message = "対象タイプは必須です")
    private TargetType targetType;

    @Column(name = "feedback_giver_id", nullable = false)
    @NotNull(message = "フィードバック提供者IDは必須です")
    private UUID feedbackGiverId;

    @Column(name = "feedback_receiver_id")
    private UUID feedbackReceiverId;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_type", nullable = false, length = 50)
    @NotNull(message = "フィードバックタイプは必須です")
    private FeedbackType feedbackType;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_category", nullable = false, length = 50)
    @NotNull(message = "フィードバックカテゴリは必須です")
    private FeedbackCategory feedbackCategory;

    @Column(name = "title", nullable = false, length = 200)
    @NotBlank(message = "タイトルは必須です")
    @Size(max = 200, message = "タイトルは200文字以内で入力してください")
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    @Size(max = 5000, message = "内容は5000文字以内で入力してください")
    private String content;

    @Column(name = "rating", nullable = false)
    @NotNull(message = "評価は必須です")
    @Min(value = 1, message = "評価は1以上である必要があります")
    @Max(value = 5, message = "評価は5以下である必要があります")
    private Integer rating;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_status", nullable = false, length = 50)
    @NotNull(message = "フィードバックステータスは必須です")
    private FeedbackStatus feedbackStatus;

    @Column(name = "is_anonymous", nullable = false)
    private Boolean isAnonymous = false;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;

    @Column(name = "is_helpful", nullable = false)
    private Boolean isHelpful = false;

    @Column(name = "is_constructive", nullable = false)
    private Boolean isConstructive = true;

    @Column(name = "is_actionable", nullable = false)
    private Boolean isActionable = false;

    @Column(name = "helpful_count", nullable = false)
    @Min(value = 0, message = "有用カウントは0以上である必要があります")
    private Integer helpfulCount = 0;

    @Column(name = "not_helpful_count", nullable = false)
    @Min(value = 0, message = "有用でないカウントは0以上である必要があります")
    private Integer notHelpfulCount = 0;

    @Column(name = "report_count", nullable = false)
    @Min(value = 0, message = "報告数は0以上である必要があります")
    private Integer reportCount = 0;

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

    @Column(name = "constructiveness_score", precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "建設性スコアは0以上である必要があります")
    @DecimalMax(value = "100.00", message = "建設性スコアは100以下である必要があります")
    private BigDecimal constructivenessScore = BigDecimal.ZERO;

    @Column(name = "overall_score", precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "総合スコアは0以上である必要があります")
    @DecimalMax(value = "100.00", message = "総合スコアは100以下である必要があります")
    private BigDecimal overallScore = BigDecimal.ZERO;

    @Column(name = "improvement_areas", columnDefinition = "JSON")
    private String improvementAreas;

    @Column(name = "suggested_actions", columnDefinition = "JSON")
    private String suggestedActions;

    @Column(name = "followup_required", nullable = false)
    private Boolean followupRequired = false;

    @Column(name = "followup_completed", nullable = false)
    private Boolean followupCompleted = false;

    @Column(name = "followup_notes", columnDefinition = "TEXT")
    private String followupNotes;

    @Column(name = "response_content", columnDefinition = "TEXT")
    private String responseContent;

    @Column(name = "response_given_at")
    private LocalDateTime responseGivenAt;

    @Column(name = "response_given_by")
    private UUID responseGivenBy;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "acknowledged_by")
    private UUID acknowledgedBy;

    @Column(name = "implemented_at")
    private LocalDateTime implementedAt;

    @Column(name = "implementation_notes", columnDefinition = "TEXT")
    private String implementationNotes;

    @Column(name = "tags", length = 500)
    @Size(max = 500, message = "タグは500文字以内で入力してください")
    private String tags;

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

    // 対象タイプ
    public enum TargetType {
        FORUM, // フォーラム
        TOPIC, // トピック
        COMMENT, // コメント
        USER, // ユーザー
        CONTENT, // コンテンツ
        COURSE, // コース
        LESSON, // レッスン
        ASSIGNMENT, // 課題
        PROJECT, // プロジェクト
        INTERACTION, // インタラクション
        SYSTEM, // システム
        FEATURE, // 機能
        EXPERIENCE, // 体験
        SERVICE, // サービス
        OTHER // その他
    }

    // フィードバックタイプ
    public enum FeedbackType {
        POSITIVE, // 肯定的
        NEGATIVE, // 否定的
        CONSTRUCTIVE, // 建設的
        NEUTRAL, // 中立
        SUGGESTION, // 提案
        COMPLAINT, // 苦情
        COMPLIMENT, // 称賛
        QUESTION, // 質問
        REQUEST, // 要求
        REPORT, // 報告
        REVIEW, // レビュー
        TESTIMONIAL, // 推薦
        BUG_REPORT, // バグ報告
        FEATURE_REQUEST, // 機能要望
        IMPROVEMENT // 改善案
    }

    // フィードバックカテゴリ
    public enum FeedbackCategory {
        CONTENT_QUALITY, // コンテンツ品質
        USER_EXPERIENCE, // ユーザー体験
        FUNCTIONALITY, // 機能性
        PERFORMANCE, // パフォーマンス
        ACCESSIBILITY, // アクセシビリティ
        DESIGN, // デザイン
        NAVIGATION, // ナビゲーション
        COMMUNICATION, // コミュニケーション
        LEARNING_EFFECTIVENESS, // 学習効果
        ENGAGEMENT, // エンゲージメント
        SUPPORT, // サポート
        TECHNICAL_ISSUE, // 技術的問題
        POLICY, // ポリシー
        COMMUNITY, // コミュニティ
        OTHER // その他
    }

    // フィードバックステータス
    public enum FeedbackStatus {
        SUBMITTED, // 提出済み
        ACKNOWLEDGED, // 確認済み
        UNDER_REVIEW, // レビュー中
        IN_PROGRESS, // 対応中
        RESOLVED, // 解決済み
        IMPLEMENTED, // 実装済み
        REJECTED, // 拒否
        CLOSED, // クローズ
        DEFERRED, // 延期
        DUPLICATE, // 重複
        SPAM, // スパム
        INVALID // 無効
    }

    // モデレーションステータス
    public enum ModerationStatus {
        PENDING, // 承認待ち
        APPROVED, // 承認済み
        REJECTED, // 拒否
        FLAGGED, // フラグ付き
        UNDER_REVIEW, // レビュー中
        AUTO_APPROVED, // 自動承認
        ESCALATED, // エスカレート
        NEEDS_ATTENTION // 要注意
    }

    // Constructors
    public LearningFeedback() {
    }

    public LearningFeedback(Long targetId, TargetType targetType, UUID feedbackGiverId,
            FeedbackType feedbackType, FeedbackCategory feedbackCategory,
            String title, Integer rating) {
        this.targetId = targetId;
        this.targetType = targetType;
        this.feedbackGiverId = feedbackGiverId;
        this.feedbackType = feedbackType;
        this.feedbackCategory = feedbackCategory;
        this.title = title;
        this.rating = rating;
        this.feedbackStatus = FeedbackStatus.SUBMITTED;
    }

    // Business Logic Methods

    /**
     * 品質スコア計算
     */
    public BigDecimal calculateQualityScore() {
        double baseScore = 50.0; // ベーススコア

        // 内容の長さと詳細さ
        if (content != null && !content.trim().isEmpty()) {
            int contentLength = content.length();
            if (contentLength >= 500)
                baseScore += 20.0;
            else if (contentLength >= 200)
                baseScore += 15.0;
            else if (contentLength >= 100)
                baseScore += 10.0;
            else if (contentLength >= 50)
                baseScore += 5.0;
        } else {
            baseScore -= 10.0; // 内容がない場合は減点
        }

        // タイトルの品質
        if (title != null && title.length() >= 10) {
            baseScore += 5.0;
        }

        // 建設的フィードバックボーナス
        if (Boolean.TRUE.equals(isConstructive)) {
            baseScore += 15.0;
        }

        // 実行可能性ボーナス
        if (Boolean.TRUE.equals(isActionable)) {
            baseScore += 10.0;
        }

        // 有用性による調整
        if (helpfulCount != null && notHelpfulCount != null) {
            int totalVotes = helpfulCount + notHelpfulCount;
            if (totalVotes > 0) {
                double helpfulRatio = (double) helpfulCount / totalVotes;
                baseScore += helpfulRatio * 15.0;
            }
        }

        // 報告数による減点
        if (reportCount != null && reportCount > 0) {
            baseScore -= Math.min(reportCount * 10.0, 30.0);
        }

        // フィードバックタイプによる調整
        if (feedbackType != null) {
            switch (feedbackType) {
                case CONSTRUCTIVE, SUGGESTION, IMPROVEMENT -> baseScore += 5.0;
                case POSITIVE, COMPLIMENT, TESTIMONIAL -> baseScore += 3.0;
                case REQUEST, FEATURE_REQUEST, BUG_REPORT -> baseScore += 2.0;
                case QUESTION, REVIEW, NEUTRAL -> baseScore += 1.0;
                case COMPLAINT -> baseScore -= 5.0;
                case NEGATIVE, REPORT -> baseScore -= 10.0;
                default -> baseScore += 0.0;
            }
        }

        return BigDecimal.valueOf(Math.max(0.0, Math.min(baseScore, 100.0)))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 有用性スコア計算
     */
    public BigDecimal calculateHelpfulnessScore() {
        double score = 0.0;

        // 有用投票による基本スコア
        if (helpfulCount != null) {
            score += Math.min(helpfulCount * 10.0, 50.0);
        }

        // 有用でない投票による減点
        if (notHelpfulCount != null) {
            score -= Math.min(notHelpfulCount * 5.0, 25.0);
        }

        // 実装済みボーナス（実際に改善に役立った場合）
        if (FeedbackStatus.IMPLEMENTED.equals(feedbackStatus)) {
            score += 30.0;
        } else if (FeedbackStatus.RESOLVED.equals(feedbackStatus)) {
            score += 20.0;
        }

        // 確認済みボーナス
        if (acknowledgedAt != null) {
            score += 10.0;
        }

        // 回答があった場合のボーナス
        if (responseContent != null && !responseContent.trim().isEmpty()) {
            score += 5.0;
        }

        return BigDecimal.valueOf(Math.max(0.0, Math.min(score, 100.0)))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 建設性スコア計算
     */
    public BigDecimal calculateConstructivenessScore() {
        double score = 50.0; // ベーススコア

        // 建設的フラグ
        if (Boolean.TRUE.equals(isConstructive)) {
            score += 20.0;
        } else {
            score -= 20.0;
        }

        // 実行可能性
        if (Boolean.TRUE.equals(isActionable)) {
            score += 15.0;
        }

        // 改善案の提供
        if (suggestedActions != null && !suggestedActions.trim().isEmpty()) {
            score += 10.0;
        }

        // フィードバックタイプによる調整
        if (feedbackType != null) {
            switch (feedbackType) {
                case CONSTRUCTIVE -> score += 15.0;
                case SUGGESTION, IMPROVEMENT -> score += 10.0;
                case POSITIVE, COMPLIMENT, TESTIMONIAL -> score += 5.0;
                case REQUEST, FEATURE_REQUEST, BUG_REPORT -> score += 3.0;
                case QUESTION, REVIEW, NEUTRAL -> score += 1.0;
                case COMPLAINT -> score -= 10.0;
                case NEGATIVE, REPORT -> score -= 15.0;
                default -> score += 0.0;
            }
        }

        // 評価による調整（高評価は建設的である可能性が高い）
        if (rating != null) {
            if (rating >= 4)
                score += 5.0;
            else if (rating <= 2)
                score -= 5.0;
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
        BigDecimal constructiveness = calculateConstructivenessScore();

        // 重み付き平均: 品質40%, 有用性35%, 建設性25%
        return quality.multiply(BigDecimal.valueOf(0.4))
                .add(helpfulness.multiply(BigDecimal.valueOf(0.35)))
                .add(constructiveness.multiply(BigDecimal.valueOf(0.25)))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * フィードバック確認
     */
    public void acknowledge(UUID acknowledgedBy) {
        this.acknowledgedAt = LocalDateTime.now();
        this.acknowledgedBy = acknowledgedBy;
        if (FeedbackStatus.SUBMITTED.equals(this.feedbackStatus)) {
            this.feedbackStatus = FeedbackStatus.ACKNOWLEDGED;
        }
    }

    /**
     * 回答追加
     */
    public void addResponse(String responseContent, UUID responseBy) {
        this.responseContent = responseContent;
        this.responseGivenAt = LocalDateTime.now();
        this.responseGivenBy = responseBy;
    }

    /**
     * 実装完了マーク
     */
    public void markAsImplemented(String implementationNotes) {
        this.feedbackStatus = FeedbackStatus.IMPLEMENTED;
        this.implementedAt = LocalDateTime.now();
        this.implementationNotes = implementationNotes;
    }

    /**
     * 解決済みマーク
     */
    public void markAsResolved() {
        this.feedbackStatus = FeedbackStatus.RESOLVED;
    }

    /**
     * 拒否処理
     */
    public void reject(String reason) {
        this.feedbackStatus = FeedbackStatus.REJECTED;
        this.moderationNotes = reason;
        this.moderatedAt = LocalDateTime.now();
    }

    /**
     * スパムマーク
     */
    public void markAsSpam(UUID moderatorId) {
        this.feedbackStatus = FeedbackStatus.SPAM;
        this.moderationStatus = ModerationStatus.REJECTED;
        this.moderatorId = moderatorId;
        this.moderatedAt = LocalDateTime.now();
    }

    /**
     * 有用投票追加
     */
    public void addHelpfulVote() {
        this.helpfulCount = (this.helpfulCount != null ? this.helpfulCount : 0) + 1;
        this.isHelpful = this.helpfulCount > (this.notHelpfulCount != null ? this.notHelpfulCount : 0);
    }

    /**
     * 有用でない投票追加
     */
    public void addNotHelpfulVote() {
        this.notHelpfulCount = (this.notHelpfulCount != null ? this.notHelpfulCount : 0) + 1;
        this.isHelpful = (this.helpfulCount != null ? this.helpfulCount : 0) > this.notHelpfulCount;
    }

    /**
     * 報告数増加
     */
    public void addReport() {
        this.reportCount = (this.reportCount != null ? this.reportCount : 0) + 1;

        // 報告数が閾値を超えた場合、モデレーション要求
        if (this.reportCount >= 3) {
            this.requiresModeration = true;
            this.moderationStatus = ModerationStatus.NEEDS_ATTENTION;
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
    }

    /**
     * モデレーション拒否
     */
    public void rejectModeration(UUID moderatorId, String notes) {
        this.moderationStatus = ModerationStatus.REJECTED;
        this.moderatorId = moderatorId;
        this.moderationNotes = notes;
        this.moderatedAt = LocalDateTime.now();
        this.feedbackStatus = FeedbackStatus.REJECTED;
    }

    /**
     * フォローアップ必要性設定
     */
    public void requireFollowup(String notes) {
        this.followupRequired = true;
        this.followupNotes = notes;
    }

    /**
     * フォローアップ完了
     */
    public void completeFollowup() {
        this.followupCompleted = true;
    }

    /**
     * 削除処理
     */
    public void delete() {
        this.deletedAt = LocalDateTime.now();
        this.feedbackStatus = FeedbackStatus.CLOSED;
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
    public boolean isHelpfulFeedback() {
        return calculateHelpfulnessScore().compareTo(BigDecimal.valueOf(70.0)) >= 0;
    }

    /**
     * 建設的判定
     */
    public boolean isConstructiveFeedback() {
        return calculateConstructivenessScore().compareTo(BigDecimal.valueOf(70.0)) >= 0;
    }

    /**
     * 対応済み判定
     */
    public boolean isAddressed() {
        return FeedbackStatus.RESOLVED.equals(feedbackStatus) ||
                FeedbackStatus.IMPLEMENTED.equals(feedbackStatus);
    }

    /**
     * 公開可能判定
     */
    public boolean isPubliclyVisible() {
        return Boolean.TRUE.equals(isPublic) &&
                !FeedbackStatus.SPAM.equals(feedbackStatus) &&
                !FeedbackStatus.REJECTED.equals(feedbackStatus) &&
                deletedAt == null;
    }

    /**
     * モデレーション必要判定
     */
    public boolean needsModeration() {
        return Boolean.TRUE.equals(requiresModeration) &&
                (moderationStatus == null ||
                        ModerationStatus.PENDING.equals(moderationStatus) ||
                        ModerationStatus.NEEDS_ATTENTION.equals(moderationStatus));
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

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public TargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(TargetType targetType) {
        this.targetType = targetType;
    }

    public UUID getFeedbackGiverId() {
        return feedbackGiverId;
    }

    public void setFeedbackGiverId(UUID feedbackGiverId) {
        this.feedbackGiverId = feedbackGiverId;
    }

    public UUID getFeedbackReceiverId() {
        return feedbackReceiverId;
    }

    public void setFeedbackReceiverId(UUID feedbackReceiverId) {
        this.feedbackReceiverId = feedbackReceiverId;
    }

    public FeedbackType getFeedbackType() {
        return feedbackType;
    }

    public void setFeedbackType(FeedbackType feedbackType) {
        this.feedbackType = feedbackType;
    }

    public FeedbackCategory getFeedbackCategory() {
        return feedbackCategory;
    }

    public void setFeedbackCategory(FeedbackCategory feedbackCategory) {
        this.feedbackCategory = feedbackCategory;
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

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public FeedbackStatus getFeedbackStatus() {
        return feedbackStatus;
    }

    public void setFeedbackStatus(FeedbackStatus feedbackStatus) {
        this.feedbackStatus = feedbackStatus;
    }

    public Boolean getIsAnonymous() {
        return isAnonymous;
    }

    public void setIsAnonymous(Boolean isAnonymous) {
        this.isAnonymous = isAnonymous;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Boolean getIsHelpful() {
        return isHelpful;
    }

    public void setIsHelpful(Boolean isHelpful) {
        this.isHelpful = isHelpful;
    }

    public Boolean getIsConstructive() {
        return isConstructive;
    }

    public void setIsConstructive(Boolean isConstructive) {
        this.isConstructive = isConstructive;
    }

    public Boolean getIsActionable() {
        return isActionable;
    }

    public void setIsActionable(Boolean isActionable) {
        this.isActionable = isActionable;
    }

    public Integer getHelpfulCount() {
        return helpfulCount;
    }

    public void setHelpfulCount(Integer helpfulCount) {
        this.helpfulCount = helpfulCount;
    }

    public Integer getNotHelpfulCount() {
        return notHelpfulCount;
    }

    public void setNotHelpfulCount(Integer notHelpfulCount) {
        this.notHelpfulCount = notHelpfulCount;
    }

    public Integer getReportCount() {
        return reportCount;
    }

    public void setReportCount(Integer reportCount) {
        this.reportCount = reportCount;
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

    public BigDecimal getConstructivenessScore() {
        return constructivenessScore;
    }

    public void setConstructivenessScore(BigDecimal constructivenessScore) {
        this.constructivenessScore = constructivenessScore;
    }

    public BigDecimal getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(BigDecimal overallScore) {
        this.overallScore = overallScore;
    }

    public String getImprovementAreas() {
        return improvementAreas;
    }

    public void setImprovementAreas(String improvementAreas) {
        this.improvementAreas = improvementAreas;
    }

    public String getSuggestedActions() {
        return suggestedActions;
    }

    public void setSuggestedActions(String suggestedActions) {
        this.suggestedActions = suggestedActions;
    }

    public Boolean getFollowupRequired() {
        return followupRequired;
    }

    public void setFollowupRequired(Boolean followupRequired) {
        this.followupRequired = followupRequired;
    }

    public Boolean getFollowupCompleted() {
        return followupCompleted;
    }

    public void setFollowupCompleted(Boolean followupCompleted) {
        this.followupCompleted = followupCompleted;
    }

    public String getFollowupNotes() {
        return followupNotes;
    }

    public void setFollowupNotes(String followupNotes) {
        this.followupNotes = followupNotes;
    }

    public String getResponseContent() {
        return responseContent;
    }

    public void setResponseContent(String responseContent) {
        this.responseContent = responseContent;
    }

    public LocalDateTime getResponseGivenAt() {
        return responseGivenAt;
    }

    public void setResponseGivenAt(LocalDateTime responseGivenAt) {
        this.responseGivenAt = responseGivenAt;
    }

    public UUID getResponseGivenBy() {
        return responseGivenBy;
    }

    public void setResponseGivenBy(UUID responseGivenBy) {
        this.responseGivenBy = responseGivenBy;
    }

    public LocalDateTime getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public void setAcknowledgedAt(LocalDateTime acknowledgedAt) {
        this.acknowledgedAt = acknowledgedAt;
    }

    public UUID getAcknowledgedBy() {
        return acknowledgedBy;
    }

    public void setAcknowledgedBy(UUID acknowledgedBy) {
        this.acknowledgedBy = acknowledgedBy;
    }

    public LocalDateTime getImplementedAt() {
        return implementedAt;
    }

    public void setImplementedAt(LocalDateTime implementedAt) {
        this.implementedAt = implementedAt;
    }

    public String getImplementationNotes() {
        return implementationNotes;
    }

    public void setImplementationNotes(String implementationNotes) {
        this.implementationNotes = implementationNotes;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
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
