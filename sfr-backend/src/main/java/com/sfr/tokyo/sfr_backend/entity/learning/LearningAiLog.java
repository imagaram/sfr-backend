package com.sfr.tokyo.sfr_backend.entity.learning;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AI学習ログエンティティ
 * AIとのインタラクション、学習支援、進捗分析、レコメンデーション履歴を管理
 * インデックス推奨: user_id, ai_type, created_at（複合）、space_id
 */
@Entity
@Table(name = "learning_ai_logs", indexes = {
        @Index(name = "idx_learning_ai_logs_user", columnList = "user_id"),
        @Index(name = "idx_learning_ai_logs_space", columnList = "space_id"),
        @Index(name = "idx_learning_ai_logs_type", columnList = "ai_type"),
        @Index(name = "idx_learning_ai_logs_user_type_created", columnList = "user_id, ai_type, created_at"),
        @Index(name = "idx_learning_ai_logs_session", columnList = "session_id")
})
public class LearningAiLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ユーザーID（必須）
     */
    @Column(name = "user_id", nullable = false)
    @NotNull(message = "ユーザーIDは必須です")
    private UUID userId;

    /**
     * スペースID（オプショナル - グローバルAI学習の場合はnull）
     */
    @Column(name = "space_id")
    private UUID spaceId;

    /**
     * セッションID（同一セッション内のやり取りをグループ化）
     */
    @Column(name = "session_id", nullable = false)
    @NotNull(message = "セッションIDは必須です")
    private UUID sessionId;

    /**
     * AIタイプ
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "ai_type", nullable = false)
    @NotNull(message = "AIタイプは必須です")
    private AiType aiType;

    /**
     * インタラクションタイプ
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "interaction_type", nullable = false)
    @NotNull(message = "インタラクションタイプは必須です")
    private InteractionType interactionType;

    /**
     * ユーザーの入力内容
     */
    @Column(name = "user_input", columnDefinition = "TEXT")
    @Size(max = 10000, message = "ユーザー入力は10000文字以内で入力してください")
    private String userInput;

    /**
     * AIの応答内容
     */
    @Column(name = "ai_response", columnDefinition = "TEXT")
    @Size(max = 20000, message = "AI応答は20000文字以内で入力してください")
    private String aiResponse;

    /**
     * 学習コンテンツID（関連するコンテンツがある場合）
     */
    @Column(name = "content_id")
    private Long contentId;

    /**
     * クイズID（関連するクイズがある場合）
     */
    @Column(name = "quiz_id")
    private Long quizId;

    /**
     * AI信頼度スコア（0.0-1.0）
     */
    @Column(name = "confidence_score", precision = 5, scale = 4)
    private BigDecimal confidenceScore;

    /**
     * ユーザー満足度（1-5の評価）
     */
    @Column(name = "user_satisfaction")
    private Integer userSatisfaction;

    /**
     * 処理時間（ミリ秒）
     */
    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    /**
     * 使用トークン数
     */
    @Column(name = "tokens_used")
    private Integer tokensUsed;

    /**
     * エラーフラグ
     */
    @Column(name = "is_error", nullable = false)
    private Boolean isError = false;

    /**
     * エラーメッセージ
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * メタデータ（JSON形式の追加情報）
     */
    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;

    /**
     * 作成日時
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新日時
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * AIタイプ列挙型
     */
    public enum AiType {
        LEARNING_ASSISTANT("学習アシスタント"),
        CONTENT_RECOMMENDER("コンテンツレコメンダー"),
        QUIZ_GENERATOR("クイズ生成"),
        PROGRESS_ANALYZER("進捗分析"),
        WRITING_ASSISTANT("文章作成支援"),
        CODE_REVIEWER("コードレビュー"),
        STUDY_PLANNER("学習計画立案"),
        SKILL_ASSESSOR("スキル評価");

        private final String displayName;

        AiType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * インタラクションタイプ列挙型
     */
    public enum InteractionType {
        QUESTION("質問"),
        RECOMMENDATION("レコメンデーション"),
        ANALYSIS("分析"),
        GENERATION("生成"),
        REVIEW("レビュー"),
        FEEDBACK("フィードバック"),
        PLANNING("計画立案"),
        ASSESSMENT("評価");

        private final String displayName;

        InteractionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Constructors
    public LearningAiLog() {
    }

    public LearningAiLog(UUID userId, UUID sessionId, AiType aiType, InteractionType interactionType) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.aiType = aiType;
        this.interactionType = interactionType;
    }

    // Business Logic Methods

    /**
     * エラー状態を設定
     */
    public void setError(String errorMessage) {
        this.isError = true;
        this.errorMessage = errorMessage;
    }

    /**
     * 成功状態を設定
     */
    public void setSuccess() {
        this.isError = false;
        this.errorMessage = null;
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
     * 学習支援に関するログかどうか判定
     */
    public boolean isLearningSupport() {
        return aiType == AiType.LEARNING_ASSISTANT ||
                aiType == AiType.STUDY_PLANNER ||
                aiType == AiType.SKILL_ASSESSOR;
    }

    /**
     * コンテンツ生成に関するログかどうか判定
     */
    public boolean isContentGeneration() {
        return aiType == AiType.QUIZ_GENERATOR ||
                aiType == AiType.WRITING_ASSISTANT ||
                interactionType == InteractionType.GENERATION;
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

    public AiType getAiType() {
        return aiType;
    }

    public void setAiType(AiType aiType) {
        this.aiType = aiType;
    }

    public InteractionType getInteractionType() {
        return interactionType;
    }

    public void setInteractionType(InteractionType interactionType) {
        this.interactionType = interactionType;
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
}
