package com.sfr.tokyo.sfr_backend.entity.learning;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 学習シミュレーション機能を管理するエンティティ
 * 
 * このエンティティは、各種学習シミュレーション（ビジネスケース、科学実験、
 * 言語練習など）の進行状況と結果を管理します。
 */
@Entity
@Table(name = "learning_simulation", indexes = {
        @Index(name = "idx_learning_simulation_user_id", columnList = "user_id"),
        @Index(name = "idx_learning_simulation_type", columnList = "simulation_type"),
        @Index(name = "idx_learning_simulation_status", columnList = "status"),
        @Index(name = "idx_learning_simulation_created_at", columnList = "created_at"),
        @Index(name = "idx_learning_simulation_user_type", columnList = "user_id,simulation_type"),
        @Index(name = "idx_learning_simulation_user_status", columnList = "user_id,status")
})
public class LearningSimulation {

    /**
     * シミュレーションの種類を定義する列挙型
     */
    public enum SimulationType {
        BUSINESS_CASE("ビジネスケース"),
        SCIENTIFIC_EXPERIMENT("科学実験"),
        LANGUAGE_PRACTICE("言語練習"),
        PROGRAMMING_CHALLENGE("プログラミングチャレンジ"),
        HISTORICAL_SCENARIO("歴史シナリオ"),
        MATHEMATICAL_PROBLEM("数学問題"),
        DEBATE_SIMULATION("ディベートシミュレーション"),
        ROLE_PLAYING("ロールプレイング"),
        DESIGN_THINKING("デザイン思考"),
        VIRTUAL_LAB("バーチャル実験室");

        private final String displayName;

        SimulationType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * シミュレーションの状態を定義する列挙型
     */
    public enum SimulationStatus {
        NOT_STARTED("未開始"),
        IN_PROGRESS("実行中"),
        PAUSED("一時停止"),
        COMPLETED("完了"),
        FAILED("失敗"),
        CANCELLED("キャンセル"),
        TIMEOUT("タイムアウト");

        private final String displayName;

        SimulationStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "ユーザーIDは必須です")
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotNull(message = "シミュレーション種類は必須です")
    @Enumerated(EnumType.STRING)
    @Column(name = "simulation_type", nullable = false, length = 50)
    private SimulationType simulationType;

    @NotBlank(message = "タイトルは必須です")
    @Size(max = 255, message = "タイトルは255文字以内で入力してください")
    @Column(name = "title", nullable = false)
    private String title;

    @Size(max = 1000, message = "説明は1000文字以内で入力してください")
    @Column(name = "description", length = 1000)
    private String description;

    @NotNull(message = "ステータスは必須です")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SimulationStatus status;

    @Column(name = "difficulty_level")
    private Integer difficultyLevel;

    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    @Column(name = "actual_duration_minutes")
    private Integer actualDurationMinutes;

    @Column(name = "progress_percentage", precision = 5, scale = 2)
    private BigDecimal progressPercentage;

    @Column(name = "current_step")
    private Integer currentStep;

    @Column(name = "total_steps")
    private Integer totalSteps;

    @Column(name = "score", precision = 8, scale = 2)
    private BigDecimal score;

    @Column(name = "max_score", precision = 8, scale = 2)
    private BigDecimal maxScore;

    @Column(name = "attempts_count")
    private Integer attemptsCount;

    @Column(name = "max_attempts")
    private Integer maxAttempts;

    @Column(name = "configuration_data", columnDefinition = "JSON")
    private String configurationData;

    @Column(name = "state_data", columnDefinition = "JSON")
    private String stateData;

    @Column(name = "result_data", columnDefinition = "JSON")
    private String resultData;

    @Column(name = "feedback_data", columnDefinition = "JSON")
    private String feedbackData;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // デフォルトコンストラクタ
    public LearningSimulation() {
        this.status = SimulationStatus.NOT_STARTED;
        this.progressPercentage = BigDecimal.ZERO;
        this.attemptsCount = 0;
    }

    // 基本コンストラクタ
    public LearningSimulation(UUID userId, SimulationType simulationType, String title) {
        this();
        this.userId = userId;
        this.simulationType = simulationType;
        this.title = title;
    }

    // ビジネスロジックメソッド

    /**
     * シミュレーションを開始する
     */
    public void startSimulation() {
        if (this.status == SimulationStatus.NOT_STARTED || this.status == SimulationStatus.PAUSED) {
            this.status = SimulationStatus.IN_PROGRESS;
            if (this.startedAt == null) {
                this.startedAt = LocalDateTime.now();
            }
            this.lastAccessedAt = LocalDateTime.now();
            incrementAttempts();
        } else {
            throw new IllegalStateException("シミュレーションは開始できません。現在のステータス: " + this.status.getDisplayName());
        }
    }

    /**
     * シミュレーションを一時停止する
     */
    public void pauseSimulation() {
        if (this.status == SimulationStatus.IN_PROGRESS) {
            this.status = SimulationStatus.PAUSED;
            this.lastAccessedAt = LocalDateTime.now();
        } else {
            throw new IllegalStateException("実行中のシミュレーションのみ一時停止できます");
        }
    }

    /**
     * シミュレーションを完了する
     */
    public void completeSimulation(BigDecimal finalScore) {
        this.status = SimulationStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.progressPercentage = new BigDecimal("100.00");
        this.score = finalScore;
        this.lastAccessedAt = LocalDateTime.now();

        if (this.startedAt != null) {
            long duration = java.time.Duration.between(this.startedAt, this.completedAt).toMinutes();
            this.actualDurationMinutes = (int) duration;
        }
    }

    /**
     * シミュレーションを失敗として終了する
     */
    public void failSimulation(String reason) {
        this.status = SimulationStatus.FAILED;
        this.completedAt = LocalDateTime.now();
        this.lastAccessedAt = LocalDateTime.now();

        // 失敗理由をresultDataに記録
        if (this.resultData == null) {
            this.resultData = "{\"failure_reason\":\"" + reason + "\"}";
        }
    }

    /**
     * シミュレーションをキャンセルする
     */
    public void cancelSimulation() {
        this.status = SimulationStatus.CANCELLED;
        this.completedAt = LocalDateTime.now();
        this.lastAccessedAt = LocalDateTime.now();
    }

    /**
     * 進行状況を更新する
     */
    public void updateProgress(BigDecimal progressPercentage) {
        this.progressPercentage = progressPercentage;
        if (progressPercentage.compareTo(new BigDecimal("100.00")) >= 0) {
            this.progressPercentage = new BigDecimal("100.00");
        }
        this.lastAccessedAt = LocalDateTime.now();
    }

    /**
     * ステップ進行状況を更新する
     */
    public void updateStepProgress(Integer currentStep) {
        this.currentStep = currentStep;

        if (this.totalSteps != null && this.totalSteps > 0) {
            BigDecimal stepProgress = new BigDecimal(currentStep)
                    .divide(new BigDecimal(this.totalSteps), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            updateProgress(stepProgress);
        }

        this.lastAccessedAt = LocalDateTime.now();
    }

    /**
     * 試行回数をインクリメントする
     */
    public void incrementAttempts() {
        this.attemptsCount = (this.attemptsCount == null) ? 1 : this.attemptsCount + 1;
    }

    /**
     * 最大試行回数に達しているかチェックする
     */
    public boolean hasReachedMaxAttempts() {
        return this.maxAttempts != null && this.attemptsCount != null &&
                this.attemptsCount >= this.maxAttempts;
    }

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
     * スコアをパーセンテージで計算する
     */
    public BigDecimal calculatePercentageScore() {
        if (score == null || maxScore == null || maxScore.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return score.divide(maxScore, 2, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
    }

    /**
     * スコアのパーセンテージを取得する（calculatePercentageScoreのエイリアス）
     */
    public BigDecimal getScorePercentage() {
        if (score == null || maxScore == null || maxScore.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return score.divide(maxScore, 2, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
    }

    /**
     * 実行時間が予定時間を超過しているかチェックする
     */
    public boolean isOverEstimatedTime() {
        return this.estimatedDurationMinutes != null &&
                this.actualDurationMinutes != null &&
                this.actualDurationMinutes > this.estimatedDurationMinutes;
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