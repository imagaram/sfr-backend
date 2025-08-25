package com.sfr.tokyo.sfr_backend.dto.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningSurvey;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 学習アンケート DTO - LearningSurveyDto
 */
public class LearningSurveyDto {

    private Long id;

    @NotNull(message = "ユーザーIDは必須です")
    private UUID userId;

    private Long spaceId;

    private Long contentId;

    @NotBlank(message = "タイトルは必須です")
    @Size(max = 500, message = "タイトルは500文字以内で入力してください")
    private String title;

    @Size(max = 5000, message = "説明は5000文字以内で入力してください")
    private String description;

    @NotNull(message = "アンケートカテゴリは必須です")
    private LearningSurvey.SurveyCategory surveyCategory;

    @NotNull(message = "アンケートステータスは必須です")
    private LearningSurvey.SurveyStatus surveyStatus;

    @NotNull(message = "優先度レベルは必須です")
    private LearningSurvey.PriorityLevel priorityLevel;

    @Min(value = 1, message = "総質問数は1以上である必要があります")
    @NotNull(message = "総質問数は必須です")
    private Integer totalQuestions;

    @Min(value = 0, message = "回答済み質問数は0以上である必要があります")
    private Integer answeredQuestions;

    @Min(value = 0, message = "必須質問数は0以上である必要があります")
    private Integer requiredQuestions;

    @DecimalMin(value = "0.00", message = "完了率は0.00以上である必要があります")
    @DecimalMax(value = "100.00", message = "完了率は100.00以下である必要があります")
    private BigDecimal completionRate;

    @DecimalMin(value = "1.00", message = "満足度スコアは1.00以上である必要があります")
    @DecimalMax(value = "5.00", message = "満足度スコアは5.00以下である必要があります")
    private BigDecimal satisfactionScore;

    @DecimalMin(value = "1.00", message = "難易度評価は1.00以上である必要があります")
    @DecimalMax(value = "5.00", message = "難易度評価は5.00以下である必要があります")
    private BigDecimal difficultyRating;

    @DecimalMin(value = "1.00", message = "有用性評価は1.00以上である必要があります")
    @DecimalMax(value = "5.00", message = "有用性評価は5.00以下である必要があります")
    private BigDecimal usefulnessRating;

    @DecimalMin(value = "1.00", message = "推奨度スコアは1.00以上である必要があります")
    @DecimalMax(value = "10.00", message = "推奨度スコアは10.00以下である必要があります")
    private BigDecimal recommendationScore;

    @Min(value = 0, message = "所要時間は0以上である必要があります")
    private Integer timeSpentMinutes;

    @Min(value = 0, message = "推定時間は0以上である必要があります")
    private Integer estimatedTimeMinutes;

    @Size(max = 5000, message = "フィードバックテキストは5000文字以内で入力してください")
    private String feedbackText;

    @Size(max = 3000, message = "提案は3000文字以内で入力してください")
    private String suggestions;

    @Size(max = 1000, message = "タグは1000文字以内で入力してください")
    private String tags;

    private Boolean isAnonymous;

    private Boolean isMandatory;

    @Min(value = 0, message = "リマインダー回数は0以上である必要があります")
    private Integer reminderCount;

    private LocalDateTime lastReminderAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public LearningSurveyDto() {
    }

    public LearningSurveyDto(UUID userId, String title, LearningSurvey.SurveyCategory surveyCategory,
            Integer totalQuestions) {
        this.userId = userId;
        this.title = title;
        this.surveyCategory = surveyCategory;
        this.totalQuestions = totalQuestions;
        this.surveyStatus = LearningSurvey.SurveyStatus.NOT_STARTED;
        this.priorityLevel = LearningSurvey.PriorityLevel.NORMAL;
        this.answeredQuestions = 0;
        this.requiredQuestions = 0;
        this.completionRate = BigDecimal.ZERO;
        this.timeSpentMinutes = 0;
        this.isAnonymous = false;
        this.isMandatory = false;
        this.reminderCount = 0;
    }

    // Business Logic Methods

    /**
     * 進捗率を計算する
     */
    public BigDecimal getProgressRate() {
        if (totalQuestions == null || totalQuestions == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(answeredQuestions != null ? answeredQuestions : 0)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalQuestions), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 残り質問数を取得
     */
    public int getRemainingQuestions() {
        if (totalQuestions == null || answeredQuestions == null) {
            return 0;
        }
        return totalQuestions - answeredQuestions;
    }

    /**
     * 完了しているかチェック
     */
    public boolean isCompleted() {
        return surveyStatus == LearningSurvey.SurveyStatus.COMPLETED;
    }

    /**
     * 進行中かチェック
     */
    public boolean isInProgress() {
        return surveyStatus == LearningSurvey.SurveyStatus.IN_PROGRESS;
    }

    /**
     * 期限切れかチェック
     */
    public boolean isExpired() {
        return dueDate != null &&
                LocalDateTime.now().isAfter(dueDate) &&
                !isCompleted();
    }

    /**
     * 必須質問が完了しているかチェック
     */
    public boolean isRequiredQuestionsCompleted() {
        if (requiredQuestions == null || answeredQuestions == null) {
            return false;
        }
        return answeredQuestions >= requiredQuestions;
    }

    /**
     * 総合スコアを計算する
     */
    public BigDecimal calculateOverallScore() {
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal weightedSum = BigDecimal.ZERO;

        if (satisfactionScore != null) {
            weightedSum = weightedSum.add(satisfactionScore.multiply(BigDecimal.valueOf(0.3)));
            totalWeight = totalWeight.add(BigDecimal.valueOf(0.3));
        }

        if (usefulnessRating != null) {
            weightedSum = weightedSum.add(usefulnessRating.multiply(BigDecimal.valueOf(0.3)));
            totalWeight = totalWeight.add(BigDecimal.valueOf(0.3));
        }

        if (recommendationScore != null) {
            // 10スケールを5スケールに正規化
            BigDecimal normalizedRecommendation = recommendationScore.divide(BigDecimal.valueOf(2), 2,
                    java.math.RoundingMode.HALF_UP);
            weightedSum = weightedSum.add(normalizedRecommendation.multiply(BigDecimal.valueOf(0.4)));
            totalWeight = totalWeight.add(BigDecimal.valueOf(0.4));
        }

        if (totalWeight.compareTo(BigDecimal.ZERO) > 0) {
            return weightedSum.divide(totalWeight, 2, java.math.RoundingMode.HALF_UP);
        }

        return BigDecimal.ZERO;
    }

    /**
     * 効率性スコアを計算（完了率/所要時間）
     */
    public BigDecimal getEfficiencyScore() {
        if (timeSpentMinutes == null || timeSpentMinutes == 0 || completionRate == null) {
            return BigDecimal.ZERO;
        }
        return completionRate.divide(BigDecimal.valueOf(timeSpentMinutes), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 推定時間との比較率を計算
     */
    public BigDecimal getTimeComparisonRate() {
        if (estimatedTimeMinutes == null || estimatedTimeMinutes == 0 || timeSpentMinutes == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(timeSpentMinutes)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(estimatedTimeMinutes), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * カテゴリ説明を取得
     */
    public String getCategoryDescription() {
        return surveyCategory != null ? surveyCategory.getDescription() : "";
    }

    /**
     * ステータス説明を取得
     */
    public String getStatusDescription() {
        return surveyStatus != null ? surveyStatus.getDescription() : "";
    }

    /**
     * 優先度説明を取得
     */
    public String getPriorityDescription() {
        return priorityLevel != null ? priorityLevel.getDescription() : "";
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
     * 満足度をテキストで取得
     */
    public String getSatisfactionText() {
        if (satisfactionScore == null) {
            return "未回答";
        }
        double score = satisfactionScore.doubleValue();
        if (score >= 4.5)
            return "非常に満足";
        if (score >= 3.5)
            return "満足";
        if (score >= 2.5)
            return "普通";
        if (score >= 1.5)
            return "不満";
        return "非常に不満";
    }

    /**
     * 難易度をテキストで取得
     */
    public String getDifficultyText() {
        if (difficultyRating == null) {
            return "未回答";
        }
        double rating = difficultyRating.doubleValue();
        if (rating >= 4.5)
            return "非常に難しい";
        if (rating >= 3.5)
            return "難しい";
        if (rating >= 2.5)
            return "普通";
        if (rating >= 1.5)
            return "簡単";
        return "非常に簡単";
    }

    /**
     * 有用性をテキストで取得
     */
    public String getUsefulnessText() {
        if (usefulnessRating == null) {
            return "未回答";
        }
        double rating = usefulnessRating.doubleValue();
        if (rating >= 4.5)
            return "非常に有用";
        if (rating >= 3.5)
            return "有用";
        if (rating >= 2.5)
            return "普通";
        if (rating >= 1.5)
            return "あまり有用でない";
        return "全く有用でない";
    }

    /**
     * 推奨度をカテゴリで取得（NPS風）
     */
    public String getRecommendationCategory() {
        if (recommendationScore == null) {
            return "未回答";
        }
        double score = recommendationScore.doubleValue();
        if (score >= 9.0)
            return "推奨者";
        if (score >= 7.0)
            return "中立者";
        return "批判者";
    }

    /**
     * アンケートの品質スコアを計算
     */
    public BigDecimal getQualityScore() {
        BigDecimal score = BigDecimal.ZERO;
        int factors = 0;

        // 完了率
        if (completionRate != null) {
            score = score.add(completionRate.divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP));
            factors++;
        }

        // フィードバックの質（文字数ベース）
        if (feedbackText != null && !feedbackText.trim().isEmpty()) {
            int length = feedbackText.length();
            BigDecimal feedbackQuality;
            if (length >= 200)
                feedbackQuality = BigDecimal.ONE;
            else if (length >= 100)
                feedbackQuality = BigDecimal.valueOf(0.8);
            else if (length >= 50)
                feedbackQuality = BigDecimal.valueOf(0.6);
            else
                feedbackQuality = BigDecimal.valueOf(0.4);

            score = score.add(feedbackQuality);
            factors++;
        }

        // 提案の有無
        if (suggestions != null && !suggestions.trim().isEmpty()) {
            score = score.add(BigDecimal.valueOf(0.2));
        }

        if (factors > 0) {
            return score.divide(BigDecimal.valueOf(factors), 2, java.math.RoundingMode.HALF_UP);
        }

        return BigDecimal.ZERO;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Long getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(Long spaceId) {
        this.spaceId = spaceId;
    }

    public Long getContentId() {
        return contentId;
    }

    public void setContentId(Long contentId) {
        this.contentId = contentId;
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

    public LearningSurvey.SurveyCategory getSurveyCategory() {
        return surveyCategory;
    }

    public void setSurveyCategory(LearningSurvey.SurveyCategory surveyCategory) {
        this.surveyCategory = surveyCategory;
    }

    public LearningSurvey.SurveyStatus getSurveyStatus() {
        return surveyStatus;
    }

    public void setSurveyStatus(LearningSurvey.SurveyStatus surveyStatus) {
        this.surveyStatus = surveyStatus;
    }

    public LearningSurvey.PriorityLevel getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(LearningSurvey.PriorityLevel priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public Integer getAnsweredQuestions() {
        return answeredQuestions;
    }

    public void setAnsweredQuestions(Integer answeredQuestions) {
        this.answeredQuestions = answeredQuestions;
    }

    public Integer getRequiredQuestions() {
        return requiredQuestions;
    }

    public void setRequiredQuestions(Integer requiredQuestions) {
        this.requiredQuestions = requiredQuestions;
    }

    public BigDecimal getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(BigDecimal completionRate) {
        this.completionRate = completionRate;
    }

    public BigDecimal getSatisfactionScore() {
        return satisfactionScore;
    }

    public void setSatisfactionScore(BigDecimal satisfactionScore) {
        this.satisfactionScore = satisfactionScore;
    }

    public BigDecimal getDifficultyRating() {
        return difficultyRating;
    }

    public void setDifficultyRating(BigDecimal difficultyRating) {
        this.difficultyRating = difficultyRating;
    }

    public BigDecimal getUsefulnessRating() {
        return usefulnessRating;
    }

    public void setUsefulnessRating(BigDecimal usefulnessRating) {
        this.usefulnessRating = usefulnessRating;
    }

    public BigDecimal getRecommendationScore() {
        return recommendationScore;
    }

    public void setRecommendationScore(BigDecimal recommendationScore) {
        this.recommendationScore = recommendationScore;
    }

    public Integer getTimeSpentMinutes() {
        return timeSpentMinutes;
    }

    public void setTimeSpentMinutes(Integer timeSpentMinutes) {
        this.timeSpentMinutes = timeSpentMinutes;
    }

    public Integer getEstimatedTimeMinutes() {
        return estimatedTimeMinutes;
    }

    public void setEstimatedTimeMinutes(Integer estimatedTimeMinutes) {
        this.estimatedTimeMinutes = estimatedTimeMinutes;
    }

    public String getFeedbackText() {
        return feedbackText;
    }

    public void setFeedbackText(String feedbackText) {
        this.feedbackText = feedbackText;
    }

    public String getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(String suggestions) {
        this.suggestions = suggestions;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Boolean getIsAnonymous() {
        return isAnonymous;
    }

    public void setIsAnonymous(Boolean isAnonymous) {
        this.isAnonymous = isAnonymous;
    }

    public Boolean getIsMandatory() {
        return isMandatory;
    }

    public void setIsMandatory(Boolean isMandatory) {
        this.isMandatory = isMandatory;
    }

    public Integer getReminderCount() {
        return reminderCount;
    }

    public void setReminderCount(Integer reminderCount) {
        this.reminderCount = reminderCount;
    }

    public LocalDateTime getLastReminderAt() {
        return lastReminderAt;
    }

    public void setLastReminderAt(LocalDateTime lastReminderAt) {
        this.lastReminderAt = lastReminderAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
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
