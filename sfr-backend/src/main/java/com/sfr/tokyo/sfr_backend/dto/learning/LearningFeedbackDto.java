package com.sfr.tokyo.sfr_backend.dto.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningFeedback.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * LearningFeedback DTO
 * フィードバック情報の転送用オブジェクト
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LearningFeedbackDto {

    /**
     * フィードバックID
     */
    private String feedbackId;

    /**
     * フィードバック対象タイプ
     */
    @NotNull(message = "フィードバック対象タイプは必須です")
    private TargetType targetType;

    /**
     * フィードバック対象ID
     */
    @NotBlank(message = "フィードバック対象IDは必須です")
    @Size(max = 255, message = "フィードバック対象IDは255文字以内で入力してください")
    private String targetId;

    /**
     * フィードバック送信者ID
     */
    @NotBlank(message = "フィードバック送信者IDは必須です")
    @Size(max = 255, message = "フィードバック送信者IDは255文字以内で入力してください")
    private String feedbackUserId;

    /**
     * フィードバックタイプ
     */
    @NotNull(message = "フィードバックタイプは必須です")
    private FeedbackType feedbackType;

    /**
     * フィードバックカテゴリ
     */
    @NotNull(message = "フィードバックカテゴリは必須です")
    private FeedbackCategory feedbackCategory;

    /**
     * フィードバック内容
     */
    @NotBlank(message = "フィードバック内容は必須です")
    @Size(min = 10, max = 5000, message = "フィードバック内容は10文字以上5000文字以内で入力してください")
    private String content;

    /**
     * 改善提案
     */
    @Size(max = 2000, message = "改善提案は2000文字以内で入力してください")
    private String suggestion;

    /**
     * 評価（1-5）
     */
    @Min(value = 1, message = "評価は1以上である必要があります")
    @Max(value = 5, message = "評価は5以下である必要があります")
    private Integer rating;

    /**
     * フィードバック状態
     */
    private FeedbackStatus status;

    /**
     * 匿名フラグ
     */
    private Boolean isAnonymous;

    /**
     * 連絡希望フラグ
     */
    private Boolean wantsContact;

    /**
     * 連絡先情報
     */
    @Size(max = 500, message = "連絡先情報は500文字以内で入力してください")
    private String contactInfo;

    /**
     * 参考URL
     */
    @Size(max = 1000, message = "参考URLは1000文字以内で入力してください")
    private String referenceUrl;

    /**
     * 管理者メモ
     */
    @Size(max = 2000, message = "管理者メモは2000文字以内で入力してください")
    private String adminNotes;

    /**
     * 解決コメント
     */
    @Size(max = 2000, message = "解決コメントは2000文字以内で入力してください")
    private String resolutionComment;

    /**
     * 処理担当者ID
     */
    @Size(max = 255, message = "処理担当者IDは255文字以内で入力してください")
    private String handlerId;

    /**
     * 品質スコア
     */
    private BigDecimal qualityScore;

    /**
     * 有用性スコア
     */
    private BigDecimal usefulnessScore;

    /**
     * 建設性スコア
     */
    private BigDecimal constructivenessScore;

    /**
     * 総合スコア
     */
    private BigDecimal overallScore;

    /**
     * いいね数
     */
    private Integer likeCount;

    /**
     * 参考になった数
     */
    private Integer helpfulCount;

    /**
     * 閲覧数
     */
    private Integer viewCount;

    /**
     * 共有数
     */
    private Integer shareCount;

    /**
     * ブックマーク数
     */
    private Integer bookmarkCount;

    /**
     * 報告数
     */
    private Integer reportCount;

    /**
     * 作成日時
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新日時
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 解決日時
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime resolvedAt;

    /**
     * 最終確認日時
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastCheckedAt;

    /**
     * 関連フィードバックリスト
     */
    private List<LearningFeedbackDto> relatedFeedbacks;

    /**
     * フィードバック対象表示名
     */
    private String targetDisplayName;

    /**
     * フィードバック送信者名
     */
    private String feedbackUserName;

    /**
     * 処理担当者名
     */
    private String handlerName;

    // ========== ビジネスロジックメソッド ==========

    /**
     * フィードバック詳細の表示用説明
     */
    public String getDetailDescription() {
        StringBuilder description = new StringBuilder();
        description.append(feedbackType.name()).append("フィードバック");

        if (feedbackCategory != null) {
            description.append("（").append(feedbackCategory.name()).append("）");
        }

        if (targetType != null) {
            description.append(" - ").append(targetType.name()).append("への回答");
        }

        if (rating != null) {
            description.append(" | 評価: ").append("★".repeat(rating));
        }

        return description.toString();
    }

    /**
     * フィードバック概要の表示用説明
     */
    public String getSummaryDescription() {
        String truncatedContent = content != null && content.length() > 100
                ? content.substring(0, 100) + "..."
                : (content != null ? content : "");

        return String.format("%s - %s",
                feedbackType.name(),
                truncatedContent);
    }

    /**
     * 緊急度判定
     */
    public String getUrgencyLevel() {
        if (feedbackCategory == FeedbackCategory.TECHNICAL_ISSUE ||
                feedbackCategory == FeedbackCategory.ACCESSIBILITY ||
                reportCount != null && reportCount >= 10) {
            return "高";
        } else if (feedbackCategory == FeedbackCategory.USER_EXPERIENCE ||
                rating != null && rating <= 2) {
            return "中";
        }
        return "低";
    }

    /**
     * 処理優先度判定
     */
    public String getPriorityLevel() {
        if (feedbackType == FeedbackType.BUG_REPORT ||
                feedbackType == FeedbackType.REPORT) {
            return "高";
        } else if (feedbackType == FeedbackType.CONSTRUCTIVE ||
                feedbackType == FeedbackType.IMPROVEMENT) {
            return "中";
        }
        return "低";
    }

    /**
     * フィードバック品質評価
     */
    public String getQualityRating() {
        if (qualityScore == null)
            return "未評価";

        double score = qualityScore.doubleValue();
        if (score >= 80)
            return "優秀";
        else if (score >= 60)
            return "良好";
        else if (score >= 40)
            return "普通";
        else if (score >= 20)
            return "改善必要";
        else
            return "不良";
    }

    /**
     * エンゲージメント評価
     */
    public String getEngagementLevel() {
        int totalEngagement = (likeCount != null ? likeCount : 0) +
                (helpfulCount != null ? helpfulCount : 0) +
                (shareCount != null ? shareCount : 0) +
                (bookmarkCount != null ? bookmarkCount : 0);

        if (totalEngagement >= 50)
            return "高";
        else if (totalEngagement >= 20)
            return "中";
        else if (totalEngagement >= 5)
            return "低";
        else
            return "なし";
    }

    /**
     * 処理状況説明
     */
    public String getStatusDescription() {
        if (status == null)
            return "未処理";

        String statusDesc = status.name();

        if (resolvedAt != null) {
            statusDesc += "（解決済み）";
        } else if (handlerId != null) {
            statusDesc += "（処理中）";
        }

        return statusDesc;
    }

    /**
     * 連絡可能性判定
     */
    public boolean isContactable() {
        return !Boolean.TRUE.equals(isAnonymous) &&
                Boolean.TRUE.equals(wantsContact) &&
                contactInfo != null && !contactInfo.trim().isEmpty();
    }

    /**
     * フォローアップ必要性判定
     */
    public boolean needsFollowUp() {
        return status == FeedbackStatus.SUBMITTED ||
                status == FeedbackStatus.IN_PROGRESS ||
                (feedbackType == FeedbackType.BUG_REPORT && status != FeedbackStatus.RESOLVED) ||
                (Boolean.TRUE.equals(wantsContact) && resolvedAt == null);
    }

    /**
     * 改善提案有無判定
     */
    public boolean hasSuggestion() {
        return suggestion != null && !suggestion.trim().isEmpty();
    }

    /**
     * 参考資料有無判定
     */
    public boolean hasReference() {
        return referenceUrl != null && !referenceUrl.trim().isEmpty();
    }

    /**
     * 関連フィードバック数取得
     */
    public int getRelatedFeedbackCount() {
        return relatedFeedbacks != null ? relatedFeedbacks.size() : 0;
    }

    /**
     * 類似フィードバック存在判定
     */
    public boolean hasSimilarFeedbacks() {
        return getRelatedFeedbackCount() > 0;
    }

    /**
     * フィードバック完了度計算
     */
    public BigDecimal getCompletionRate() {
        if (status == FeedbackStatus.RESOLVED) {
            return new BigDecimal("100.00");
        } else if (status == FeedbackStatus.IN_PROGRESS) {
            return new BigDecimal("50.00");
        } else if (status == FeedbackStatus.SUBMITTED) {
            return new BigDecimal("25.00");
        }
        return BigDecimal.ZERO;
    }

    /**
     * 総合評価スコア計算
     */
    public BigDecimal calculateOverallRating() {
        if (qualityScore == null || usefulnessScore == null || constructivenessScore == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = qualityScore.add(usefulnessScore).add(constructivenessScore);
        return total.divide(new BigDecimal("3"), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * エンゲージメント総計
     */
    public int getTotalEngagement() {
        return (likeCount != null ? likeCount : 0) +
                (helpfulCount != null ? helpfulCount : 0) +
                (shareCount != null ? shareCount : 0) +
                (bookmarkCount != null ? bookmarkCount : 0);
    }

    /**
     * 処理時間計算（日数）
     */
    public Long getProcessingDays() {
        if (createdAt == null)
            return null;

        LocalDateTime endTime = resolvedAt != null ? resolvedAt : LocalDateTime.now();
        return java.time.Duration.between(createdAt, endTime).toDays();
    }

    /**
     * フィードバックタイプの表示用アイコン
     */
    public String getTypeIcon() {
        if (feedbackType == null)
            return "📝";

        return switch (feedbackType) {
            case POSITIVE -> "👍";
            case NEGATIVE -> "👎";
            case CONSTRUCTIVE -> "🔧";
            case SUGGESTION -> "💡";
            case IMPROVEMENT -> "📈";
            case COMPLAINT -> "😤";
            case TESTIMONIAL -> "🏆";
            case REQUEST -> "🙏";
            case FEATURE_REQUEST -> "⭐";
            case BUG_REPORT -> "🐛";
            case QUESTION -> "❓";
            case REVIEW -> "📋";
            case REPORT -> "⚠️";
            case NEUTRAL -> "😐";
            case COMPLIMENT -> "🌟";
        };
    }

    /**
     * フィードバックカテゴリの表示用色
     */
    public String getCategoryColor() {
        if (feedbackCategory == null)
            return "#6c757d";

        return switch (feedbackCategory) {
            case CONTENT_QUALITY -> "#007bff";
            case USER_EXPERIENCE -> "#28a745";
            case FUNCTIONALITY -> "#ffc107";
            case TECHNICAL_ISSUE -> "#dc3545";
            case PERFORMANCE -> "#17a2b8";
            case DESIGN -> "#e83e8c";
            case ACCESSIBILITY -> "#6f42c1";
            case NAVIGATION -> "#fd7e14";
            case COMMUNICATION -> "#20c997";
            case LEARNING_EFFECTIVENESS -> "#007bff";
            case ENGAGEMENT -> "#28a745";
            case SUPPORT -> "#ffc107";
            case POLICY -> "#dc3545";
            case COMMUNITY -> "#17a2b8";
            case OTHER -> "#6c757d";
        };
    }

    /**
     * 初期化メソッド
     */
    public void initialize() {
        if (relatedFeedbacks == null) {
            relatedFeedbacks = new ArrayList<>();
        }
        if (isAnonymous == null) {
            isAnonymous = false;
        }
        if (wantsContact == null) {
            wantsContact = false;
        }
        if (status == null) {
            status = FeedbackStatus.SUBMITTED;
        }
        if (likeCount == null) {
            likeCount = 0;
        }
        if (helpfulCount == null) {
            helpfulCount = 0;
        }
        if (viewCount == null) {
            viewCount = 0;
        }
        if (shareCount == null) {
            shareCount = 0;
        }
        if (bookmarkCount == null) {
            bookmarkCount = 0;
        }
        if (reportCount == null) {
            reportCount = 0;
        }
    }
}
