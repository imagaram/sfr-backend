package com.sfr.tokyo.sfr_backend.entity.learning;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 学習写経 - LearningShakyo Entity
 * プログラミングコードや文章の写経学習を管理するエンティティ
 */
@Entity
@Table(name = "learning_shakyo", indexes = {
        @Index(name = "idx_learning_shakyo_user_id", columnList = "user_id"),
        @Index(name = "idx_learning_shakyo_space_id", columnList = "space_id"),
        @Index(name = "idx_learning_shakyo_type", columnList = "shakyo_type"),
        @Index(name = "idx_learning_shakyo_status", columnList = "shakyo_status"),
        @Index(name = "idx_learning_shakyo_difficulty", columnList = "difficulty_level"),
        @Index(name = "idx_learning_shakyo_language", columnList = "programming_language"),
        @Index(name = "idx_learning_shakyo_created_at", columnList = "created_at"),
        @Index(name = "idx_learning_shakyo_completed_at", columnList = "completed_at"),
        @Index(name = "idx_learning_shakyo_user_type", columnList = "user_id, shakyo_type"),
        @Index(name = "idx_learning_shakyo_space_type", columnList = "space_id, shakyo_type"),
        @Index(name = "idx_learning_shakyo_user_status", columnList = "user_id, shakyo_status"),
        @Index(name = "idx_learning_shakyo_user_language", columnList = "user_id, programming_language")
})
public class LearningShakyo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "space_id")
    private Long spaceId;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "shakyo_type", nullable = false)
    private ShakyoType shakyoType;

    @Enumerated(EnumType.STRING)
    @Column(name = "shakyo_status", nullable = false)
    private ShakyoStatus shakyoStatus = ShakyoStatus.NOT_STARTED;

    @Column(name = "programming_language", length = 50)
    private String programmingLanguage;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false)
    private DifficultyLevel difficultyLevel;

    @Column(name = "original_text", columnDefinition = "TEXT", nullable = false)
    private String originalText;

    @Column(name = "current_text", columnDefinition = "TEXT")
    private String currentText;

    @Column(name = "total_characters", nullable = false)
    private Integer totalCharacters;

    @Column(name = "typed_characters")
    private Integer typedCharacters = 0;

    @Column(name = "correct_characters")
    private Integer correctCharacters = 0;

    @Column(name = "accuracy_rate", precision = 5, scale = 2)
    private BigDecimal accuracyRate = BigDecimal.ZERO;

    @Column(name = "typing_speed_cpm")
    private Integer typingSpeedCpm = 0;

    @Column(name = "total_typing_time_seconds")
    private Integer totalTypingTimeSeconds = 0;

    @Column(name = "error_count")
    private Integer errorCount = 0;

    @Column(name = "current_line")
    private Integer currentLine = 1;

    @Column(name = "current_position")
    private Integer currentPosition = 0;

    @Column(name = "hint_used_count")
    private Integer hintUsedCount = 0;

    @Column(name = "pause_count")
    private Integer pauseCount = 0;

    @Column(name = "score", precision = 10, scale = 2)
    private BigDecimal score = BigDecimal.ZERO;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "last_typed_at")
    private LocalDateTime lastTypedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 写経の種類
     */
    public enum ShakyoType {
        PROGRAMMING_CODE("プログラミングコード"),
        LITERARY_TEXT("文学作品"),
        TECHNICAL_DOCUMENT("技術文書"),
        BUSINESS_DOCUMENT("ビジネス文書"),
        POETRY("詩歌"),
        SCRIPTURE("経典"),
        CALLIGRAPHY("書道"),
        FOREIGN_LANGUAGE("外国語"),
        HISTORICAL_TEXT("歴史文書"),
        PERSONAL_NOTES("個人ノート");

        private final String description;

        ShakyoType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 写経の状態
     */
    public enum ShakyoStatus {
        NOT_STARTED("未開始"),
        IN_PROGRESS("進行中"),
        PAUSED("一時停止"),
        COMPLETED("完了"),
        ABANDONED("中断"),
        REVIEWING("復習中"),
        ARCHIVED("アーカイブ済み");

        private final String description;

        ShakyoStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 難易度レベル
     */
    public enum DifficultyLevel {
        BEGINNER("初級"),
        INTERMEDIATE("中級"),
        ADVANCED("上級"),
        EXPERT("熟練"),
        MASTER("達人");

        private final String description;

        DifficultyLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Constructors
    public LearningShakyo() {
    }

    public LearningShakyo(UUID userId, String title, ShakyoType shakyoType,
            DifficultyLevel difficultyLevel, String originalText) {
        this.userId = userId;
        this.title = title;
        this.shakyoType = shakyoType;
        this.difficultyLevel = difficultyLevel;
        this.originalText = originalText;
        this.totalCharacters = originalText.length();
    }

    // Business Logic Methods

    /**
     * 写経を開始する
     */
    public void startShakyo() {
        if (this.shakyoStatus == ShakyoStatus.NOT_STARTED ||
                this.shakyoStatus == ShakyoStatus.PAUSED) {
            this.shakyoStatus = ShakyoStatus.IN_PROGRESS;
            if (this.startedAt == null) {
                this.startedAt = LocalDateTime.now();
            }
            this.lastTypedAt = LocalDateTime.now();
        }
    }

    /**
     * 写経を一時停止する
     */
    public void pauseShakyo() {
        if (this.shakyoStatus == ShakyoStatus.IN_PROGRESS) {
            this.shakyoStatus = ShakyoStatus.PAUSED;
            this.pauseCount++;
        }
    }

    /**
     * 写経を完了する
     */
    public void completeShakyo() {
        if (this.shakyoStatus == ShakyoStatus.IN_PROGRESS) {
            this.shakyoStatus = ShakyoStatus.COMPLETED;
            this.completedAt = LocalDateTime.now();
            this.currentPosition = this.totalCharacters;
            this.typedCharacters = this.totalCharacters;
            calculateFinalScore();
        }
    }

    /**
     * 文字入力を記録する
     */
    public boolean typeCharacter(char inputChar) {
        if (this.shakyoStatus != ShakyoStatus.IN_PROGRESS) {
            return false;
        }

        if (this.currentPosition >= this.totalCharacters) {
            return false;
        }

        char expectedChar = this.originalText.charAt(this.currentPosition);
        this.typedCharacters++;
        this.lastTypedAt = LocalDateTime.now();

        if (inputChar == expectedChar) {
            this.correctCharacters++;
            this.currentPosition++;

            // 現在行の更新
            if (inputChar == '\n') {
                this.currentLine++;
            }

            // 完了チェック
            if (this.currentPosition >= this.totalCharacters) {
                completeShakyo();
            }

            calculateAccuracyRate();
            return true;
        } else {
            this.errorCount++;
            calculateAccuracyRate();
            return false;
        }
    }

    /**
     * 正確率を計算する
     */
    public void calculateAccuracyRate() {
        if (this.typedCharacters > 0) {
            BigDecimal rate = BigDecimal.valueOf(this.correctCharacters)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(this.typedCharacters), 2, RoundingMode.HALF_UP);
            this.accuracyRate = rate;
        } else {
            this.accuracyRate = BigDecimal.ZERO;
        }
    }

    /**
     * 入力速度を計算する（CPM - Characters Per Minute）
     */
    public void calculateTypingSpeed() {
        if (this.startedAt != null && this.lastTypedAt != null) {
            long secondsElapsed = java.time.Duration.between(this.startedAt, this.lastTypedAt).getSeconds();
            this.totalTypingTimeSeconds = (int) secondsElapsed;

            if (secondsElapsed > 0) {
                this.typingSpeedCpm = (int) (this.correctCharacters * 60.0 / secondsElapsed);
            }
        }
    }

    /**
     * 進捗率を計算する
     */
    public BigDecimal getProgressRate() {
        if (this.totalCharacters == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(this.currentPosition)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(this.totalCharacters), 2, RoundingMode.HALF_UP);
    }

    /**
     * ヒント使用
     */
    public String useHint() {
        if (this.shakyoStatus != ShakyoStatus.IN_PROGRESS) {
            return null;
        }

        if (this.currentPosition >= this.totalCharacters) {
            return null;
        }

        this.hintUsedCount++;

        // 次の5文字をヒントとして返す
        int endPos = Math.min(this.currentPosition + 5, this.totalCharacters);
        return this.originalText.substring(this.currentPosition, endPos);
    }

    /**
     * 最終スコアを計算する
     */
    private void calculateFinalScore() {
        if (this.totalCharacters == 0) {
            this.score = BigDecimal.ZERO;
            return;
        }

        // 基本スコア = 正確率 * 進捗率
        BigDecimal baseScore = this.accuracyRate.multiply(getProgressRate()).divide(BigDecimal.valueOf(100), 2,
                RoundingMode.HALF_UP);

        // 速度ボーナス（CPMが100以上で加点）
        BigDecimal speedBonus = BigDecimal.ZERO;
        if (this.typingSpeedCpm >= 100) {
            speedBonus = BigDecimal.valueOf(Math.min(this.typingSpeedCpm - 100, 200)).multiply(BigDecimal.valueOf(0.1));
        }

        // 難易度ボーナス
        BigDecimal difficultyBonus = BigDecimal.valueOf(this.difficultyLevel.ordinal() * 10);

        // ヒント使用ペナルティ
        BigDecimal hintPenalty = BigDecimal.valueOf(this.hintUsedCount * 2);

        // エラーペナルティ
        BigDecimal errorPenalty = BigDecimal.valueOf(this.errorCount * 0.5);

        this.score = baseScore.add(speedBonus).add(difficultyBonus).subtract(hintPenalty).subtract(errorPenalty)
                .max(BigDecimal.ZERO);
    }

    /**
     * 現在の文字を取得
     */
    public char getCurrentCharacter() {
        if (this.currentPosition < this.totalCharacters) {
            return this.originalText.charAt(this.currentPosition);
        }
        return '\0';
    }

    /**
     * 残り文字数を取得
     */
    public int getRemainingCharacters() {
        return this.totalCharacters - this.currentPosition;
    }

    /**
     * 完了しているかチェック
     */
    public boolean isCompleted() {
        return this.shakyoStatus == ShakyoStatus.COMPLETED;
    }

    /**
     * 進行中かチェック
     */
    public boolean isInProgress() {
        return this.shakyoStatus == ShakyoStatus.IN_PROGRESS;
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

    public ShakyoType getShakyoType() {
        return shakyoType;
    }

    public void setShakyoType(ShakyoType shakyoType) {
        this.shakyoType = shakyoType;
    }

    public ShakyoStatus getShakyoStatus() {
        return shakyoStatus;
    }

    public void setShakyoStatus(ShakyoStatus shakyoStatus) {
        this.shakyoStatus = shakyoStatus;
    }

    public String getProgrammingLanguage() {
        return programmingLanguage;
    }

    public void setProgrammingLanguage(String programmingLanguage) {
        this.programmingLanguage = programmingLanguage;
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
        this.totalCharacters = originalText != null ? originalText.length() : 0;
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
