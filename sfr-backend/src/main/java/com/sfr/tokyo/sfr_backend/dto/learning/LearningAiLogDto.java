package com.sfr.tokyo.sfr_backend.dto.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningAiLog;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AI学習ログデータ転送オブジェクト
 * AIインタラクション履歴の表示・作成・更新用
 */
public class LearningAiLogDto {

    private Long id;

    @NotNull(message = "ユーザーIDは必須です")
    private UUID userId;

    private UUID spaceId;

    @NotNull(message = "セッションIDは必須です")
    private UUID sessionId;

    @NotNull(message = "AIタイプは必須です")
    private LearningAiLog.AiType aiType;

    @NotNull(message = "インタラクションタイプは必須です")
    private LearningAiLog.InteractionType interactionType;

    @Size(max = 10000, message = "ユーザー入力は10000文字以内で入力してください")
    private String userInput;

    @Size(max = 20000, message = "AI応答は20000文字以内で入力してください")
    private String aiResponse;

    private Long contentId;

    private Long quizId;

    @DecimalMin(value = "0.0", message = "信頼度スコアは0.0以上である必要があります")
    @DecimalMax(value = "1.0", message = "信頼度スコアは1.0以下である必要があります")
    private BigDecimal confidenceScore;

    @Min(value = 1, message = "ユーザー満足度は1以上である必要があります")
    @Max(value = 5, message = "ユーザー満足度は5以下である必要があります")
    private Integer userSatisfaction;

    @Min(value = 0, message = "処理時間は0以上である必要があります")
    private Long processingTimeMs;

    @Min(value = 0, message = "使用トークン数は0以上である必要があります")
    private Integer tokensUsed;

    private Boolean isError;

    private String errorMessage;

    private String metadata;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // 追加表示用フィールド
    private String aiTypeDisplayName;
    private String interactionTypeDisplayName;
    private String userName;
    private String contentTitle;
    private String quizTitle;
    private String sessionSummary;

    // Constructors
    public LearningAiLogDto() {
    }

    public LearningAiLogDto(UUID userId, UUID sessionId, LearningAiLog.AiType aiType,
            LearningAiLog.InteractionType interactionType) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.aiType = aiType;
        this.interactionType = interactionType;
        this.aiTypeDisplayName = aiType != null ? aiType.getDisplayName() : null;
        this.interactionTypeDisplayName = interactionType != null ? interactionType.getDisplayName() : null;
    }

    // Business Logic Methods

    /**
     * 表示用のAIタイプ名を取得
     */
    public String getAiTypeDisplayName() {
        if (aiTypeDisplayName == null && aiType != null) {
            aiTypeDisplayName = aiType.getDisplayName();
        }
        return aiTypeDisplayName;
    }

    /**
     * 表示用のインタラクションタイプ名を取得
     */
    public String getInteractionTypeDisplayName() {
        if (interactionTypeDisplayName == null && interactionType != null) {
            interactionTypeDisplayName = interactionType.getDisplayName();
        }
        return interactionTypeDisplayName;
    }

    /**
     * 高信頼度の応答かどうか判定
     */
    public boolean isHighConfidence() {
        return confidenceScore != null && confidenceScore.compareTo(new BigDecimal("0.8")) >= 0;
    }

    /**
     * 高満足度の応答かどうか判定
     */
    public boolean isHighSatisfaction() {
        return userSatisfaction != null && userSatisfaction >= 4;
    }

    /**
     * レスポンス時間が遅いかどうか判定
     */
    public boolean isSlowResponse() {
        return processingTimeMs != null && processingTimeMs > 5000;
    }

    /**
     * トークン使用量が多いかどうか判定
     */
    public boolean isHighTokenUsage() {
        return tokensUsed != null && tokensUsed > 1000;
    }

    /**
     * エラー状態の表示メッセージを取得
     */
    public String getStatusMessage() {
        if (Boolean.TRUE.equals(isError)) {
            return "エラー: " + (errorMessage != null ? errorMessage : "不明なエラー");
        }
        return "正常";
    }

    /**
     * 信頼度スコアの表示用文字列を取得
     */
    public String getConfidenceScoreDisplay() {
        if (confidenceScore == null) {
            return "未設定";
        }

        BigDecimal percentage = confidenceScore.multiply(new BigDecimal("100"));
        return percentage.setScale(1, RoundingMode.HALF_UP) + "%";
    }

    /**
     * 満足度の表示用文字列を取得
     */
    public String getSatisfactionDisplay() {
        if (userSatisfaction == null) {
            return "未評価";
        }

        String[] ratings = { "", "★", "★★", "★★★", "★★★★", "★★★★★" };
        return ratings[userSatisfaction] + " (" + userSatisfaction + "/5)";
    }

    /**
     * 処理時間の表示用文字列を取得
     */
    public String getProcessingTimeDisplay() {
        if (processingTimeMs == null) {
            return "未記録";
        }

        if (processingTimeMs < 1000) {
            return processingTimeMs + "ms";
        } else {
            return String.format("%.1fs", processingTimeMs / 1000.0);
        }
    }

    /**
     * セッションサマリーを生成
     */
    public String generateSessionSummary() {
        if (sessionSummary != null) {
            return sessionSummary;
        }

        StringBuilder summary = new StringBuilder();
        summary.append(getAiTypeDisplayName()).append(" - ");
        summary.append(getInteractionTypeDisplayName());

        if (userInput != null && userInput.length() > 50) {
            summary.append(": ").append(userInput.substring(0, 50)).append("...");
        } else if (userInput != null) {
            summary.append(": ").append(userInput);
        }

        sessionSummary = summary.toString();
        return sessionSummary;
    }

    /**
     * 学習支援に関するログかどうか判定
     */
    public boolean isLearningSupport() {
        return aiType == LearningAiLog.AiType.LEARNING_ASSISTANT ||
                aiType == LearningAiLog.AiType.STUDY_PLANNER ||
                aiType == LearningAiLog.AiType.SKILL_ASSESSOR;
    }

    /**
     * コンテンツ生成に関するログかどうか判定
     */
    public boolean isContentGeneration() {
        return aiType == LearningAiLog.AiType.QUIZ_GENERATOR ||
                aiType == LearningAiLog.AiType.WRITING_ASSISTANT ||
                interactionType == LearningAiLog.InteractionType.GENERATION;
    }

    /**
     * 応答品質スコアを計算（0-100）
     */
    public int calculateQualityScore() {
        int score = 0;

        // 信頼度スコア（40%）
        if (confidenceScore != null) {
            score += confidenceScore.multiply(new BigDecimal("40")).intValue();
        }

        // 満足度（30%）
        if (userSatisfaction != null) {
            score += (userSatisfaction * 6); // 5点満点を30点満点に変換
        }

        // エラーがない場合（20%）
        if (!Boolean.TRUE.equals(isError)) {
            score += 20;
        }

        // レスポンス時間（10%）
        if (processingTimeMs != null) {
            if (processingTimeMs <= 1000) {
                score += 10;
            } else if (processingTimeMs <= 3000) {
                score += 5;
            }
        }

        return Math.min(score, 100);
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

    public UUID getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(UUID spaceId) {
        this.spaceId = spaceId;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public LearningAiLog.AiType getAiType() {
        return aiType;
    }

    public void setAiType(LearningAiLog.AiType aiType) {
        this.aiType = aiType;
        this.aiTypeDisplayName = aiType != null ? aiType.getDisplayName() : null;
    }

    public LearningAiLog.InteractionType getInteractionType() {
        return interactionType;
    }

    public void setInteractionType(LearningAiLog.InteractionType interactionType) {
        this.interactionType = interactionType;
        this.interactionTypeDisplayName = interactionType != null ? interactionType.getDisplayName() : null;
    }

    public String getUserInput() {
        return userInput;
    }

    public void setUserInput(String userInput) {
        this.userInput = userInput;
    }

    public String getAiResponse() {
        return aiResponse;
    }

    public void setAiResponse(String aiResponse) {
        this.aiResponse = aiResponse;
    }

    public Long getContentId() {
        return contentId;
    }

    public void setContentId(Long contentId) {
        this.contentId = contentId;
    }

    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    public BigDecimal getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(BigDecimal confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public Integer getUserSatisfaction() {
        return userSatisfaction;
    }

    public void setUserSatisfaction(Integer userSatisfaction) {
        this.userSatisfaction = userSatisfaction;
    }

    public Long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(Long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public Integer getTokensUsed() {
        return tokensUsed;
    }

    public void setTokensUsed(Integer tokensUsed) {
        this.tokensUsed = tokensUsed;
    }

    public Boolean getIsError() {
        return isError;
    }

    public void setIsError(Boolean isError) {
        this.isError = isError;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
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

    public void setAiTypeDisplayName(String aiTypeDisplayName) {
        this.aiTypeDisplayName = aiTypeDisplayName;
    }

    public void setInteractionTypeDisplayName(String interactionTypeDisplayName) {
        this.interactionTypeDisplayName = interactionTypeDisplayName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public void setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
    }

    public String getQuizTitle() {
        return quizTitle;
    }

    public void setQuizTitle(String quizTitle) {
        this.quizTitle = quizTitle;
    }

    public String getSessionSummary() {
        return sessionSummary;
    }

    public void setSessionSummary(String sessionSummary) {
        this.sessionSummary = sessionSummary;
    }
}
