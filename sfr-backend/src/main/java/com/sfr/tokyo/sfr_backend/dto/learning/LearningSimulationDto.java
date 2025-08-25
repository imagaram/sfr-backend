package com.sfr.tokyo.sfr_backend.dto.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningSimulation.SimulationType;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningSimulation.SimulationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 学習シミュレーション機能のData Transfer Object
 * 
 * フロントエンドとの通信やAPIレスポンスで使用するDTOクラス
 */
public class LearningSimulationDto {

    // 基本情報
    private Long id;

    @NotNull(message = "ユーザーIDは必須です")
    private UUID userId;

    @NotNull(message = "シミュレーション種類は必須です")
    private SimulationType simulationType;

    @NotBlank(message = "タイトルは必須です")
    @Size(max = 255, message = "タイトルは255文字以内で入力してください")
    private String title;

    @Size(max = 1000, message = "説明は1000文字以内で入力してください")
    private String description;

    @NotNull(message = "ステータスは必須です")
    private SimulationStatus status;

    // 設定情報
    @Min(value = 1, message = "難易度レベルは1以上である必要があります")
    @Max(value = 10, message = "難易度レベルは10以下である必要があります")
    private Integer difficultyLevel;

    @Min(value = 1, message = "予定実行時間は1分以上である必要があります")
    private Integer estimatedDurationMinutes;

    private Integer actualDurationMinutes;

    // 進行状況
    private BigDecimal progressPercentage;

    @Min(value = 0, message = "現在のステップは0以上である必要があります")
    private Integer currentStep;

    @Min(value = 1, message = "総ステップ数は1以上である必要があります")
    private Integer totalSteps;

    // スコア情報
    private BigDecimal score;
    private BigDecimal maxScore;
    private BigDecimal scorePercentage;

    // 試行回数
    private Integer attemptsCount;

    @Min(value = 1, message = "最大試行回数は1以上である必要があります")
    private Integer maxAttempts;

    // JSONデータ
    private String configurationData;
    private String stateData;
    private String resultData;
    private String feedbackData;

    // 時刻情報
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime lastAccessedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // デフォルトコンストラクタ
    public LearningSimulationDto() {
    }

    // 基本コンストラクタ
    public LearningSimulationDto(UUID userId, SimulationType simulationType, String title) {
        this.userId = userId;
        this.simulationType = simulationType;
        this.title = title;
        this.status = SimulationStatus.NOT_STARTED;
        this.progressPercentage = BigDecimal.ZERO;
        this.attemptsCount = 0;
    }

    // ビジネスロジック用メソッド

    /**
     * シミュレーションが完了しているかチェックする
     */
    public boolean isCompleted() {
        return this.status == SimulationStatus.COMPLETED;
    }

    /**
     * シミュレーションが実行中かチェックする
     */
    public boolean isInProgress() {
        return this.status == SimulationStatus.IN_PROGRESS;
    }

    /**
     * 最大試行回数に達しているかチェックする
     */
    public boolean hasReachedMaxAttempts() {
        return this.maxAttempts != null && this.attemptsCount != null &&
                this.attemptsCount >= this.maxAttempts;
    }

    /**
     * 実行時間が予定時間を超過しているかチェックする
     */
    public boolean isOverEstimatedTime() {
        return this.estimatedDurationMinutes != null &&
                this.actualDurationMinutes != null &&
                this.actualDurationMinutes > this.estimatedDurationMinutes;
    }

    /**
     * 進行状況の割合を取得する（0-100の範囲）
     */
    public double getProgressPercentageAsDouble() {
        return progressPercentage != null ? progressPercentage.doubleValue() : 0.0;
    }

    /**
     * スコアの割合を取得する（0-100の範囲）
     */
    public double getScorePercentageAsDouble() {
        return scorePercentage != null ? scorePercentage.doubleValue() : 0.0;
    }

    // Getter & Setter methods

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

    public SimulationType getSimulationType() {
        return simulationType;
    }

    public void setSimulationType(SimulationType simulationType) {
        this.simulationType = simulationType;
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

    public SimulationStatus getStatus() {
        return status;
    }

    public void setStatus(SimulationStatus status) {
        this.status = status;
    }

    public Integer getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(Integer difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public Integer getEstimatedDurationMinutes() {
        return estimatedDurationMinutes;
    }

    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) {
        this.estimatedDurationMinutes = estimatedDurationMinutes;
    }

    public Integer getActualDurationMinutes() {
        return actualDurationMinutes;
    }

    public void setActualDurationMinutes(Integer actualDurationMinutes) {
        this.actualDurationMinutes = actualDurationMinutes;
    }

    public BigDecimal getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(BigDecimal progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public Integer getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(Integer currentStep) {
        this.currentStep = currentStep;
    }

    public Integer getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(Integer totalSteps) {
        this.totalSteps = totalSteps;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public BigDecimal getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(BigDecimal maxScore) {
        this.maxScore = maxScore;
    }

    public BigDecimal getScorePercentage() {
        return scorePercentage;
    }

    public void setScorePercentage(BigDecimal scorePercentage) {
        this.scorePercentage = scorePercentage;
    }

    public Integer getAttemptsCount() {
        return attemptsCount;
    }

    public void setAttemptsCount(Integer attemptsCount) {
        this.attemptsCount = attemptsCount;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public String getConfigurationData() {
        return configurationData;
    }

    public void setConfigurationData(String configurationData) {
        this.configurationData = configurationData;
    }

    public String getStateData() {
        return stateData;
    }

    public void setStateData(String stateData) {
        this.stateData = stateData;
    }

    public String getResultData() {
        return resultData;
    }

    public void setResultData(String resultData) {
        this.resultData = resultData;
    }

    public String getFeedbackData() {
        return feedbackData;
    }

    public void setFeedbackData(String feedbackData) {
        this.feedbackData = feedbackData;
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

    public LocalDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }

    public void setLastAccessedAt(LocalDateTime lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
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
