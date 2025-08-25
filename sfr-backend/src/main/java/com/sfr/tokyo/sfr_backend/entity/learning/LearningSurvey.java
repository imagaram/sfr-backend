package com.sfr.tokyo.sfr_backend.entity.learning;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 学習アンケート - LearningSurvey Entity
 * 学習コンテンツやコースに関するアンケートを管理するエンティティ
 */
@Entity
@Table(name = "learning_survey", indexes = {
        @Index(name = "idx_learning_survey_user_id", columnList = "user_id"),
        @Index(name = "idx_learning_survey_space_id", columnList = "space_id"),
        @Index(name = "idx_learning_survey_content_id", columnList = "content_id"),
        @Index(name = "idx_learning_survey_category", columnList = "survey_category"),
        @Index(name = "idx_learning_survey_status", columnList = "survey_status"),
        @Index(name = "idx_learning_survey_priority", columnList = "priority_level"),
        @Index(name = "idx_learning_survey_created_at", columnList = "created_at"),
        @Index(name = "idx_learning_survey_completed_at", columnList = "completed_at"),
        @Index(name = "idx_learning_survey_user_category", columnList = "user_id, survey_category"),
        @Index(name = "idx_learning_survey_space_category", columnList = "space_id, survey_category"),
        @Index(name = "idx_learning_survey_user_status", columnList = "user_id, survey_status"),
        @Index(name = "idx_learning_survey_content_status", columnList = "content_id, survey_status")
})
public class LearningSurvey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "space_id")
    private Long spaceId;

    @Column(name = "content_id")
    private Long contentId;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "survey_category", nullable = false)
    private SurveyCategory surveyCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "survey_status", nullable = false)
    private SurveyStatus surveyStatus = SurveyStatus.NOT_STARTED;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority_level", nullable = false)
    private PriorityLevel priorityLevel = PriorityLevel.NORMAL;

    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    @Column(name = "answered_questions")
    private Integer answeredQuestions = 0;

    @Column(name = "required_questions")
    private Integer requiredQuestions = 0;

    @Column(name = "completion_rate", precision = 5, scale = 2)
    private BigDecimal completionRate = BigDecimal.ZERO;

    @Column(name = "satisfaction_score", precision = 3, scale = 2)
    private BigDecimal satisfactionScore;

    @Column(name = "difficulty_rating", precision = 3, scale = 2)
    private BigDecimal difficultyRating;

    @Column(name = "usefulness_rating", precision = 3, scale = 2)
    private BigDecimal usefulnessRating;

    @Column(name = "recommendation_score", precision = 3, scale = 2)
    private BigDecimal recommendationScore;

    @Column(name = "time_spent_minutes")
    private Integer timeSpentMinutes = 0;

    @Column(name = "estimated_time_minutes")
    private Integer estimatedTimeMinutes;

    @Column(name = "feedback_text", columnDefinition = "TEXT")
    private String feedbackText;

    @Column(name = "suggestions", columnDefinition = "TEXT")
    private String suggestions;

    @Column(name = "tags", length = 1000)
    private String tags;

    @Column(name = "is_anonymous")
    private Boolean isAnonymous = false;

    @Column(name = "is_mandatory")
    private Boolean isMandatory = false;

    @Column(name = "reminder_count")
    private Integer reminderCount = 0;

    @Column(name = "last_reminder_at")
    private LocalDateTime lastReminderAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * アンケートカテゴリ
     */
    public enum SurveyCategory {
        COURSE_FEEDBACK("コースフィードバック"),
        CONTENT_EVALUATION("コンテンツ評価"),
        USER_EXPERIENCE("ユーザーエクスペリエンス"),
        TECHNICAL_ASSESSMENT("技術評価"),
        SATISFACTION_SURVEY("満足度調査"),
        IMPROVEMENT_SUGGESTION("改善提案"),
        LEARNING_OUTCOME("学習成果"),
        INSTRUCTOR_EVALUATION("講師評価"),
        PLATFORM_FEEDBACK("プラットフォームフィードバック"),
        CUSTOM_SURVEY("カスタムアンケート");

        private final String description;

        SurveyCategory(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * アンケート状態
     */
    public enum SurveyStatus {
        NOT_STARTED("未開始"),
        IN_PROGRESS("進行中"),
        COMPLETED("完了"),
        SKIPPED("スキップ"),
        EXPIRED("期限切れ"),
        CANCELLED("キャンセル"),
        ARCHIVED("アーカイブ済み");

        private final String description;

        SurveyStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 優先度レベル
     */
    public enum PriorityLevel {
        LOW("低"),
        NORMAL("通常"),
        HIGH("高"),
        URGENT("緊急"),
        CRITICAL("重要");

        private final String description;

        PriorityLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Constructors
    public LearningSurvey() {
    }

    public LearningSurvey(UUID userId, String title, SurveyCategory surveyCategory,
            Integer totalQuestions) {
        this.userId = userId;
        this.title = title;
        this.surveyCategory = surveyCategory;
        this.totalQuestions = totalQuestions;
        this.surveyStatus = SurveyStatus.NOT_STARTED;
        this.priorityLevel = PriorityLevel.NORMAL;
    }

    // Business Logic Methods

    /**
     * アンケートを開始する
     */
    public void startSurvey() {
        if (this.surveyStatus == SurveyStatus.NOT_STARTED ||
                this.surveyStatus == SurveyStatus.IN_PROGRESS) {
            this.surveyStatus = SurveyStatus.IN_PROGRESS;
            if (this.startedAt == null) {
                this.startedAt = LocalDateTime.now();
            }
        }
    }

    /**
     * 質問に回答する
     */
    public void answerQuestion() {
        if (this.surveyStatus == SurveyStatus.IN_PROGRESS) {
            this.answeredQuestions++;
            calculateCompletionRate();

            // 全ての質問に回答した場合、完了状態にする
            if (this.answeredQuestions >= this.totalQuestions) {
                completeSurvey();
            }
        }
    }

    /**
     * アンケートを完了する
     */
    public void completeSurvey() {
        if (this.surveyStatus == SurveyStatus.IN_PROGRESS) {
            this.surveyStatus = SurveyStatus.COMPLETED;
            this.completedAt = LocalDateTime.now();
            this.completionRate = BigDecimal.valueOf(100);
            calculateTimeSpent();
        }
    }

    /**
     * アンケートをスキップする
     */
    public void skipSurvey() {
        if (this.surveyStatus != SurveyStatus.COMPLETED) {
            this.surveyStatus = SurveyStatus.SKIPPED;
        }
    }

    /**
     * 完了率を計算する
     */
    public void calculateCompletionRate() {
        if (this.totalQuestions > 0) {
            BigDecimal rate = BigDecimal.valueOf(this.answeredQuestions)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(this.totalQuestions), 2, RoundingMode.HALF_UP);
            this.completionRate = rate;
        } else {
            this.completionRate = BigDecimal.ZERO;
        }
    }

    /**
     * 所要時間を計算する
     */
    private void calculateTimeSpent() {
        if (this.startedAt != null && this.completedAt != null) {
            long minutes = java.time.Duration.between(this.startedAt, this.completedAt).toMinutes();
            this.timeSpentMinutes = (int) minutes;
        }
    }

    /**
     * 期限切れかチェックする
     */
    public boolean isExpired() {
        return this.dueDate != null &&
                LocalDateTime.now().isAfter(this.dueDate) &&
                this.surveyStatus != SurveyStatus.COMPLETED;
    }

    /**
     * 期限切れにする
     */
    public void markAsExpired() {
        if (isExpired() && this.surveyStatus != SurveyStatus.COMPLETED) {
            this.surveyStatus = SurveyStatus.EXPIRED;
        }
    }

    /**
     * リマインダーを送信する
     */
    public void sendReminder() {
        this.reminderCount++;
        this.lastReminderAt = LocalDateTime.now();
    }

    /**
     * 満足度を設定する（1-5スケール）
     */
    public void setSatisfactionRating(double rating) {
        if (rating >= 1.0 && rating <= 5.0) {
            this.satisfactionScore = BigDecimal.valueOf(rating).setScale(2, RoundingMode.HALF_UP);
        }
    }

    /**
     * 難易度を設定する（1-5スケール）
     */
    public void setDifficultyRating(double rating) {
        if (rating >= 1.0 && rating <= 5.0) {
            this.difficultyRating = BigDecimal.valueOf(rating).setScale(2, RoundingMode.HALF_UP);
        }
    }

    /**
     * 有用性を設定する（1-5スケール）
     */
    public void setUsefulnessRating(double rating) {
        if (rating >= 1.0 && rating <= 5.0) {
            this.usefulnessRating = BigDecimal.valueOf(rating).setScale(2, RoundingMode.HALF_UP);
        }
    }

    /**
     * 推奨度を設定する（1-10スケール）
     */
    public void setRecommendationRating(double rating) {
        if (rating >= 1.0 && rating <= 10.0) {
            this.recommendationScore = BigDecimal.valueOf(rating).setScale(2, RoundingMode.HALF_UP);
        }
    }

    /**
     * 総合スコアを計算する
     */
    public BigDecimal calculateOverallScore() {
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal weightedSum = BigDecimal.ZERO;

        if (this.satisfactionScore != null) {
            weightedSum = weightedSum.add(this.satisfactionScore.multiply(BigDecimal.valueOf(0.3)));
            totalWeight = totalWeight.add(BigDecimal.valueOf(0.3));
        }

        if (this.usefulnessRating != null) {
            weightedSum = weightedSum.add(this.usefulnessRating.multiply(BigDecimal.valueOf(0.3)));
            totalWeight = totalWeight.add(BigDecimal.valueOf(0.3));
        }

        if (this.recommendationScore != null) {
            // 10スケールを5スケールに正規化
            BigDecimal normalizedRecommendation = this.recommendationScore.divide(BigDecimal.valueOf(2), 2,
                    RoundingMode.HALF_UP);
            weightedSum = weightedSum.add(normalizedRecommendation.multiply(BigDecimal.valueOf(0.4)));
            totalWeight = totalWeight.add(BigDecimal.valueOf(0.4));
        }

        if (totalWeight.compareTo(BigDecimal.ZERO) > 0) {
            return weightedSum.divide(totalWeight, 2, RoundingMode.HALF_UP);
        }

        return BigDecimal.ZERO;
    }

    /**
     * 必須質問が完了しているかチェック
     */
    public boolean isRequiredQuestionsCompleted() {
        return this.answeredQuestions >= this.requiredQuestions;
    }

    /**
     * 進捗率を取得
     */
    public BigDecimal getProgressRate() {
        return this.completionRate != null ? this.completionRate : BigDecimal.ZERO;
    }

    /**
     * 残り質問数を取得
     */
    public int getRemainingQuestions() {
        return this.totalQuestions - this.answeredQuestions;
    }

    /**
     * 完了しているかチェック
     */
    public boolean isCompleted() {
        return this.surveyStatus == SurveyStatus.COMPLETED;
    }

    /**
     * 進行中かチェック
     */
    public boolean isInProgress() {
        return this.surveyStatus == SurveyStatus.IN_PROGRESS;
    }

    /**
     * タグを追加
     */
    public void addTag(String tag) {
        if (this.tags == null || this.tags.isEmpty()) {
            this.tags = tag;
        } else {
            this.tags += "," + tag;
        }
    }

    /**
     * タグを削除
     */
    public void removeTag(String tag) {
        if (this.tags != null && this.tags.contains(tag)) {
            String[] tagArray = this.tags.split(",");
            StringBuilder newTags = new StringBuilder();
            for (String t : tagArray) {
                if (!t.trim().equals(tag)) {
                    if (newTags.length() > 0) {
                        newTags.append(",");
                    }
                    newTags.append(t.trim());
                }
            }
            this.tags = newTags.toString();
        }
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

    public SurveyCategory getSurveyCategory() {
        return surveyCategory;
    }

    public void setSurveyCategory(SurveyCategory surveyCategory) {
        this.surveyCategory = surveyCategory;
    }

    public SurveyStatus getSurveyStatus() {
        return surveyStatus;
    }

    public void setSurveyStatus(SurveyStatus surveyStatus) {
        this.surveyStatus = surveyStatus;
    }

    public PriorityLevel getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(PriorityLevel priorityLevel) {
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
