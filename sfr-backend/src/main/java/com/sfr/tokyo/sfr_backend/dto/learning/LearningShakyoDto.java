package com.sfr.tokyo.sfr_backend.dto.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningShakyo;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 学習写経 DTO - LearningShakyoDto
 */
public class LearningShakyoDto {

    private Long id;

    @NotNull(message = "ユーザーIDは必須です")
    private UUID userId;

    private Long spaceId;

    @NotBlank(message = "タイトルは必須です")
    @Size(max = 500, message = "タイトルは500文字以内で入力してください")
    private String title;

    @NotNull(message = "写経タイプは必須です")
    private LearningShakyo.ShakyoType shakyoType;

    @NotNull(message = "写経ステータスは必須です")
    private LearningShakyo.ShakyoStatus shakyoStatus;

    @Size(max = 50, message = "プログラミング言語は50文字以内で入力してください")
    private String programmingLanguage;

    @NotNull(message = "難易度レベルは必須です")
    private LearningShakyo.DifficultyLevel difficultyLevel;

    @NotBlank(message = "原文テキストは必須です")
    private String originalText;

    private String currentText;

    @Min(value = 0, message = "総文字数は0以上である必要があります")
    private Integer totalCharacters;

    @Min(value = 0, message = "入力文字数は0以上である必要があります")
    private Integer typedCharacters;

    @Min(value = 0, message = "正解文字数は0以上である必要があります")
    private Integer correctCharacters;

    @DecimalMin(value = "0.00", message = "正確率は0.00以上である必要があります")
    @DecimalMax(value = "100.00", message = "正確率は100.00以下である必要があります")
    private BigDecimal accuracyRate;

    @Min(value = 0, message = "入力速度は0以上である必要があります")
    private Integer typingSpeedCpm;

    @Min(value = 0, message = "総入力時間は0以上である必要があります")
    private Integer totalTypingTimeSeconds;

    @Min(value = 0, message = "エラー回数は0以上である必要があります")
    private Integer errorCount;

    @Min(value = 1, message = "現在行は1以上である必要があります")
    private Integer currentLine;

    @Min(value = 0, message = "現在位置は0以上である必要があります")
    private Integer currentPosition;

    @Min(value = 0, message = "ヒント使用回数は0以上である必要があります")
    private Integer hintUsedCount;

    @Min(value = 0, message = "一時停止回数は0以上である必要があります")
    private Integer pauseCount;

    @DecimalMin(value = "0.00", message = "スコアは0.00以上である必要があります")
    private BigDecimal score;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime lastTypedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public LearningShakyoDto() {}

    public LearningShakyoDto(UUID userId, String title, LearningShakyo.ShakyoType shakyoType, 
                            LearningShakyo.DifficultyLevel difficultyLevel, String originalText) {
        this.userId = userId;
        this.title = title;
        this.shakyoType = shakyoType;
        this.difficultyLevel = difficultyLevel;
        this.originalText = originalText;
        this.totalCharacters = originalText != null ? originalText.length() : 0;
        this.shakyoStatus = LearningShakyo.ShakyoStatus.NOT_STARTED;
        this.typedCharacters = 0;
        this.correctCharacters = 0;
        this.accuracyRate = BigDecimal.ZERO;
        this.typingSpeedCpm = 0;
        this.totalTypingTimeSeconds = 0;
        this.errorCount = 0;
        this.currentLine = 1;
        this.currentPosition = 0;
        this.hintUsedCount = 0;
        this.pauseCount = 0;
        this.score = BigDecimal.ZERO;
    }

    // Business Logic Methods

    /**
     * 進捗率を計算する
     */
    public BigDecimal getProgressRate() {
        if (totalCharacters == null || totalCharacters == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(currentPosition != null ? currentPosition : 0)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(totalCharacters), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 残り文字数を取得
     */
    public int getRemainingCharacters() {
        if (totalCharacters == null || currentPosition == null) {
            return 0;
        }
        return totalCharacters - currentPosition;
    }

    /**
     * 完了しているかチェック
     */
    public boolean isCompleted() {
        return shakyoStatus == LearningShakyo.ShakyoStatus.COMPLETED;
    }

    /**
     * 進行中かチェック
     */
    public boolean isInProgress() {
        return shakyoStatus == LearningShakyo.ShakyoStatus.IN_PROGRESS;
    }

    /**
     * 現在の文字を取得
     */
    public Character getCurrentCharacter() {
        if (originalText == null || currentPosition == null || 
            currentPosition >= originalText.length()) {
            return null;
        }
        return originalText.charAt(currentPosition);
    }

    /**
     * 次の数文字を取得（プレビュー用）
     */
    public String getNextCharacters(int count) {
        if (originalText == null || currentPosition == null) {
            return "";
        }
        int startPos = currentPosition;
        int endPos = Math.min(startPos + count, originalText.length());
        return originalText.substring(startPos, endPos);
    }

    /**
     * 平均入力速度を計算（Words Per Minute）
     */
    public BigDecimal getAverageWpm() {
        if (totalTypingTimeSeconds == null || totalTypingTimeSeconds == 0 || correctCharacters == null) {
            return BigDecimal.ZERO;
        }
        // 一般的に1単語は5文字として計算
        BigDecimal words = BigDecimal.valueOf(correctCharacters).divide(BigDecimal.valueOf(5), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal minutes = BigDecimal.valueOf(totalTypingTimeSeconds).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
        return words.divide(minutes, 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * エラー率を計算
     */
    public BigDecimal getErrorRate() {
        if (typedCharacters == null || typedCharacters == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(errorCount != null ? errorCount : 0)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(typedCharacters), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 学習効率を計算（スコア/時間）
     */
    public BigDecimal getLearningEfficiency() {
        if (totalTypingTimeSeconds == null || totalTypingTimeSeconds == 0 || score == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal hours = BigDecimal.valueOf(totalTypingTimeSeconds).divide(BigDecimal.valueOf(3600), 2, java.math.RoundingMode.HALF_UP);
        return score.divide(hours, 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 難易度説明を取得
     */
    public String getDifficultyDescription() {
        return difficultyLevel != null ? difficultyLevel.getDescription() : "";
    }

    /**
     * 写経タイプ説明を取得
     */
    public String getShakyoTypeDescription() {
        return shakyoType != null ? shakyoType.getDescription() : "";
    }

    /**
     * 写経ステータス説明を取得
     */
    public String getShakyoStatusDescription() {
        return shakyoStatus != null ? shakyoStatus.getDescription() : "";
    }

    /**
     * 集中度を計算（一時停止回数の逆数）
     */
    public BigDecimal getConcentrationLevel() {
        if (pauseCount == null || pauseCount == 0) {
            return BigDecimal.valueOf(100);
        }
        // 一時停止回数が多いほど集中度が下がる
        return BigDecimal.valueOf(100).divide(BigDecimal.valueOf(pauseCount + 1), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 自律性スコア（ヒント使用回数の逆数）
     */
    public BigDecimal getAutonomyScore() {
        if (hintUsedCount == null || hintUsedCount == 0) {
            return BigDecimal.valueOf(100);
        }
        // ヒント使用回数が多いほど自律性が下がる
        return BigDecimal.valueOf(100).divide(BigDecimal.valueOf(hintUsedCount + 1), 2, java.math.RoundingMode.HALF_UP);
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LearningShakyo.ShakyoType getShakyoType() {
        return shakyoType;
    }

    public void setShakyoType(LearningShakyo.ShakyoType shakyoType) {
        this.shakyoType = shakyoType;
    }

    public LearningShakyo.ShakyoStatus getShakyoStatus() {
        return shakyoStatus;
    }

    public void setShakyoStatus(LearningShakyo.ShakyoStatus shakyoStatus) {
        this.shakyoStatus = shakyoStatus;
    }

    public String getProgrammingLanguage() {
        return programmingLanguage;
    }

    public void setProgrammingLanguage(String programmingLanguage) {
        this.programmingLanguage = programmingLanguage;
    }

    public LearningShakyo.DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(LearningShakyo.DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getCurrentText() {
        return currentText;
    }

    public void setCurrentText(String currentText) {
        this.currentText = currentText;
    }

    public Integer getTotalCharacters() {
        return totalCharacters;
    }

    public void setTotalCharacters(Integer totalCharacters) {
        this.totalCharacters = totalCharacters;
    }

    public Integer getTypedCharacters() {
        return typedCharacters;
    }

    public void setTypedCharacters(Integer typedCharacters) {
        this.typedCharacters = typedCharacters;
    }

    public Integer getCorrectCharacters() {
        return correctCharacters;
    }

    public void setCorrectCharacters(Integer correctCharacters) {
        this.correctCharacters = correctCharacters;
    }

    public BigDecimal getAccuracyRate() {
        return accuracyRate;
    }

    public void setAccuracyRate(BigDecimal accuracyRate) {
        this.accuracyRate = accuracyRate;
    }

    public Integer getTypingSpeedCpm() {
        return typingSpeedCpm;
    }

    public void setTypingSpeedCpm(Integer typingSpeedCpm) {
        this.typingSpeedCpm = typingSpeedCpm;
    }

    public Integer getTotalTypingTimeSeconds() {
        return totalTypingTimeSeconds;
    }

    public void setTotalTypingTimeSeconds(Integer totalTypingTimeSeconds) {
        this.totalTypingTimeSeconds = totalTypingTimeSeconds;
    }

    public Integer getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Integer errorCount) {
        this.errorCount = errorCount;
    }

    public Integer getCurrentLine() {
        return currentLine;
    }

    public void setCurrentLine(Integer currentLine) {
        this.currentLine = currentLine;
    }

    public Integer getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(Integer currentPosition) {
        this.currentPosition = currentPosition;
    }

    public Integer getHintUsedCount() {
        return hintUsedCount;
    }

    public void setHintUsedCount(Integer hintUsedCount) {
        this.hintUsedCount = hintUsedCount;
    }

    public Integer getPauseCount() {
        return pauseCount;
    }

    public void setPauseCount(Integer pauseCount) {
        this.pauseCount = pauseCount;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
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

    public LocalDateTime getLastTypedAt() {
        return lastTypedAt;
    }

    public void setLastTypedAt(LocalDateTime lastTypedAt) {
        this.lastTypedAt = lastTypedAt;
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
