package com.sfr.tokyo.sfr_backend.entity.crypto;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AiDecisionLog Entity - AI決定ログの管理
 * SFRトークン関連のAI判定と決定プロセスを記録
 */
@Entity
@Table(name = "ai_decision_logs", indexes = {
        @Index(name = "idx_ai_decision_logs_space_id", columnList = "space_id"),
        @Index(name = "idx_ai_decision_logs_decision_type", columnList = "decision_type"),
        @Index(name = "idx_ai_decision_logs_decision_date", columnList = "decision_date"),
        @Index(name = "idx_ai_decision_logs_model_version", columnList = "model_version"),
        @Index(name = "idx_ai_decision_logs_confidence_score", columnList = "confidence_score"),
        @Index(name = "idx_ai_decision_logs_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiDecisionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "space_id", nullable = false)
    @NotNull(message = "スペースIDは必須です")
    private Long spaceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision_type", nullable = false, length = 30)
    @NotNull(message = "決定タイプは必須です")
    private DecisionType decisionType;

    @Column(name = "reference_id", nullable = true, length = 100)
    @Size(max = 100, message = "参照IDは100文字以内で入力してください")
    private String referenceId; // 関連するエンティティのID

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", nullable = true, length = 30)
    private ReferenceType referenceType;

    @Column(name = "model_version", nullable = false, length = 20)
    @NotBlank(message = "モデルバージョンは必須です")
    @Size(max = 20, message = "モデルバージョンは20文字以内で入力してください")
    @Builder.Default
    private String modelVersion = "v1.0";

    @Column(name = "algorithm_name", nullable = false, length = 50)
    @NotBlank(message = "アルゴリズム名は必須です")
    @Size(max = 50, message = "アルゴリズム名は50文字以内で入力してください")
    private String algorithmName;

    @Column(name = "input_parameters", columnDefinition = "TEXT")
    private String inputParameters; // JSON形式での入力パラメータ

    @Column(name = "market_data", columnDefinition = "TEXT")
    private String marketData; // JSON形式での市場データ

    @Column(name = "user_behavior_data", columnDefinition = "TEXT")
    private String userBehaviorData; // JSON形式でのユーザー行動データ

    @Column(name = "economic_indicators", columnDefinition = "TEXT")
    private String economicIndicators; // JSON形式での経済指標

    @Column(name = "decision_factors", columnDefinition = "TEXT")
    private String decisionFactors; // JSON形式での決定要因

    @Column(name = "confidence_score", nullable = false, precision = 5, scale = 2)
    @NotNull(message = "信頼度スコアは必須です")
    @DecimalMin(value = "0.0", message = "信頼度スコアは0以上である必要があります")
    @DecimalMax(value = "100.0", message = "信頼度スコアは100以下である必要があります")
    private BigDecimal confidenceScore;

    @Column(name = "risk_score", nullable = false, precision = 5, scale = 2)
    @NotNull(message = "リスクスコアは必須です")
    @DecimalMin(value = "0.0", message = "リスクスコアは0以上である必要があります")
    @DecimalMax(value = "100.0", message = "リスクスコアは100以下である必要があります")
    private BigDecimal riskScore;

    @Column(name = "impact_score", nullable = false, precision = 5, scale = 2)
    @NotNull(message = "影響スコアは必須です")
    @DecimalMin(value = "0.0", message = "影響スコアは0以上である必要があります")
    @DecimalMax(value = "100.0", message = "影響スコアは100以下である必要があります")
    private BigDecimal impactScore;

    @Column(name = "recommended_action", nullable = false, length = 50)
    @NotBlank(message = "推奨アクションは必須です")
    @Size(max = 50, message = "推奨アクションは50文字以内で入力してください")
    private String recommendedAction;

    @Column(name = "alternative_actions", columnDefinition = "TEXT")
    private String alternativeActions; // JSON形式での代替アクション

    @Column(name = "decision_rationale", columnDefinition = "TEXT")
    private String decisionRationale; // 決定根拠

    @Column(name = "expected_outcomes", columnDefinition = "TEXT")
    private String expectedOutcomes; // 期待される結果

    @Column(name = "monitoring_metrics", columnDefinition = "TEXT")
    private String monitoringMetrics; // JSON形式での監視指標

    @Column(name = "validation_data", columnDefinition = "TEXT")
    private String validationData; // JSON形式での検証データ

    @Column(name = "decision_date", nullable = false)
    @NotNull(message = "決定日は必須です")
    private LocalDateTime decisionDate;

    @Column(name = "execution_deadline", nullable = true)
    private LocalDateTime executionDeadline;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "ステータスは必須です")
    @Builder.Default
    private DecisionStatus status = DecisionStatus.PROPOSED;

    @Column(name = "human_review_required", nullable = false)
    @Builder.Default
    private Boolean humanReviewRequired = false;

    @Column(name = "reviewed_by", nullable = true)
    private UUID reviewedBy;

    @Column(name = "reviewed_at", nullable = true)
    private LocalDateTime reviewedAt;

    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_result", nullable = true, length = 20)
    private ReviewResult reviewResult;

    @Column(name = "executed_at", nullable = true)
    private LocalDateTime executedAt;

    @Column(name = "executed_by", nullable = true)
    private UUID executedBy; // 実行者（システムまたは人間）

    @Column(name = "execution_result", columnDefinition = "TEXT")
    private String executionResult; // JSON形式での実行結果

    @Column(name = "actual_outcomes", columnDefinition = "TEXT")
    private String actualOutcomes; // JSON形式での実際の結果

    @Column(name = "outcome_variance", columnDefinition = "TEXT")
    private String outcomeVariance; // 期待値と実際の結果の差異

    @Column(name = "feedback_score", precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "フィードバックスコアは0以上である必要があります")
    @DecimalMax(value = "100.0", message = "フィードバックスコアは100以下である必要があります")
    private BigDecimal feedbackScore; // 決定の効果に対するフィードバック

    @Column(name = "learning_data", columnDefinition = "TEXT")
    private String learningData; // JSON形式でのモデル学習用データ

    @Column(name = "model_accuracy", precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "モデル精度は0以上である必要があります")
    @DecimalMax(value = "100.0", message = "モデル精度は100以下である必要があります")
    private BigDecimal modelAccuracy;

    @Column(name = "computation_time_ms", nullable = false)
    @Min(value = 0, message = "計算時間は0以上である必要があります")
    @Builder.Default
    private Long computationTimeMs = 0L;

    @Column(name = "data_freshness_score", precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "データ新鮮度スコアは0以上である必要があります")
    @DecimalMax(value = "100.0", message = "データ新鮮度スコアは100以下である必要があります")
    private BigDecimal dataFreshnessScore;

    @Column(name = "bias_score", precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "バイアススコアは0以上である必要があります")
    @DecimalMax(value = "100.0", message = "バイアススコアは100以下である必要があります")
    private BigDecimal biasScore;

    @Column(name = "explainability_score", precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "説明可能性スコアは0以上である必要があります")
    @DecimalMax(value = "100.0", message = "説明可能性スコアは100以下である必要があります")
    private BigDecimal explainabilityScore;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage; // エラーが発生した場合のメッセージ

    @Column(name = "debug_info", columnDefinition = "TEXT")
    private String debugInfo; // デバッグ情報

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 列挙型定義

    /**
     * 決定タイプ
     */
    public enum DecisionType {
        TOKEN_ISSUANCE, // トークン発行
        TOKEN_BURN, // トークンバーン
        REWARD_DISTRIBUTION, // リワード配布
        COLLECTION_TRIGGER, // 回収トリガー
        GOVERNANCE_SUPPORT, // ガバナンスサポート
        MARKET_INTERVENTION, // 市場介入
        RISK_MITIGATION, // リスク軽減
        ANOMALY_DETECTION, // 異常検知
        OPTIMIZATION, // 最適化
        PREDICTION, // 予測
        CLASSIFICATION, // 分類
        RECOMMENDATION // 推奨
    }

    /**
     * 参照タイプ
     */
    public enum ReferenceType {
        BURN_DECISION, // バーン決定
        REWARD_DISTRIBUTION, // リワード配布
        COLLECTION_HISTORY, // 回収履歴
        GOVERNANCE_PROPOSAL, // ガバナンス提案
        TOKEN_POOL, // トークンプール
        USER_BALANCE, // ユーザー残高
        MARKET_DATA, // 市場データ
        USER_BEHAVIOR, // ユーザー行動
        SYSTEM_METRIC // システム指標
    }

    /**
     * 決定ステータス
     */
    public enum DecisionStatus {
        PROPOSED, // 提案済み
        UNDER_REVIEW, // レビュー中
        APPROVED, // 承認済み
        REJECTED, // 拒否
        EXECUTED, // 実行済み
        FAILED, // 失敗
        CANCELLED, // キャンセル
        MONITORING // 監視中
    }

    /**
     * レビュー結果
     */
    public enum ReviewResult {
        APPROVED, // 承認
        REJECTED, // 拒否
        MODIFIED, // 修正
        NEEDS_MORE_DATA, // より多くのデータが必要
        ESCALATED // エスカレート
    }

    // ビジネスロジックメソッド

    /**
     * 人間のレビューが必要かどうかを判定する
     * 
     * @return レビューが必要かどうか
     */
    public boolean requiresHumanReview() {
        // 低い信頼度、高いリスク、高い影響のいずれかの場合はレビューが必要
        return confidenceScore.compareTo(new BigDecimal("80.0")) < 0 ||
                riskScore.compareTo(new BigDecimal("70.0")) > 0 ||
                impactScore.compareTo(new BigDecimal("80.0")) > 0;
    }

    /**
     * 決定の品質スコアを計算する
     * 
     * @return 品質スコア（0-100）
     */
    public BigDecimal calculateQualityScore() {
        BigDecimal confidenceWeight = new BigDecimal("0.3");
        BigDecimal riskWeight = new BigDecimal("0.2");
        BigDecimal explainabilityWeight = new BigDecimal("0.2");
        BigDecimal accuracyWeight = new BigDecimal("0.2");
        BigDecimal freshnessWeight = new BigDecimal("0.1");

        BigDecimal qualityScore = confidenceScore.multiply(confidenceWeight)
                .add(new BigDecimal("100").subtract(riskScore).multiply(riskWeight))
                .add((explainabilityScore != null ? explainabilityScore : BigDecimal.ZERO)
                        .multiply(explainabilityWeight))
                .add((modelAccuracy != null ? modelAccuracy : BigDecimal.ZERO).multiply(accuracyWeight))
                .add((dataFreshnessScore != null ? dataFreshnessScore : BigDecimal.ZERO).multiply(freshnessWeight));

        return qualityScore.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * レビューを開始する
     * 
     * @param reviewer レビュー者
     */
    public void startReview(UUID reviewer) {
        if (status == DecisionStatus.PROPOSED) {
            this.status = DecisionStatus.UNDER_REVIEW;
            this.reviewedBy = reviewer;
            this.reviewedAt = LocalDateTime.now();
            this.humanReviewRequired = true;
        }
    }

    /**
     * レビューを完了する
     * 
     * @param result レビュー結果
     * @param notes  レビューノート
     */
    public void completeReview(ReviewResult result, String notes) {
        if (status == DecisionStatus.UNDER_REVIEW) {
            this.reviewResult = result;
            this.reviewNotes = notes;

            switch (result) {
                case APPROVED:
                    this.status = DecisionStatus.APPROVED;
                    break;
                case REJECTED:
                    this.status = DecisionStatus.REJECTED;
                    break;
                case MODIFIED:
                    this.status = DecisionStatus.PROPOSED; // 修正後再提案
                    break;
                default:
                    this.status = DecisionStatus.UNDER_REVIEW;
                    break;
            }
        }
    }

    /**
     * 決定を実行する
     * 
     * @param executor 実行者
     * @param result   実行結果
     */
    public void execute(UUID executor, String result) {
        if (status == DecisionStatus.APPROVED) {
            this.status = DecisionStatus.EXECUTED;
            this.executedBy = executor;
            this.executedAt = LocalDateTime.now();
            this.executionResult = result;
        }
    }

    /**
     * 実行を失敗にマークする
     * 
     * @param error エラーメッセージ
     */
    public void markAsFailed(String error) {
        if (status == DecisionStatus.APPROVED || status == DecisionStatus.EXECUTED) {
            this.status = DecisionStatus.FAILED;
            this.errorMessage = error;
        }
    }

    /**
     * フィードバックを記録する
     * 
     * @param score    フィードバックスコア
     * @param outcomes 実際の結果
     * @param variance 期待値との差異
     */
    public void recordFeedback(BigDecimal score, String outcomes, String variance) {
        this.feedbackScore = score;
        this.actualOutcomes = outcomes;
        this.outcomeVariance = variance;

        // 学習データとして記録
        this.learningData = generateLearningData();
    }

    /**
     * 学習データを生成する
     * 
     * @return JSON形式の学習データ
     */
    private String generateLearningData() {
        return String.format(
                "{\"inputParams\":%s,\"decision\":\"%s\",\"confidence\":%s,\"actualOutcome\":%s,\"feedback\":%s,\"accuracy\":%s}",
                inputParameters != null ? inputParameters : "{}",
                recommendedAction,
                confidenceScore,
                actualOutcomes != null ? actualOutcomes : "null",
                feedbackScore != null ? feedbackScore : "null",
                modelAccuracy != null ? modelAccuracy : "null");
    }

    /**
     * 決定が実行可能かチェックする
     * 
     * @return 実行可能かどうか
     */
    public boolean isExecutable() {
        return status == DecisionStatus.APPROVED &&
                (executionDeadline == null || LocalDateTime.now().isBefore(executionDeadline));
    }

    /**
     * 決定が期限切れかチェックする
     * 
     * @return 期限切れかどうか
     */
    public boolean isExpired() {
        return executionDeadline != null &&
                LocalDateTime.now().isAfter(executionDeadline) &&
                status == DecisionStatus.APPROVED;
    }

    /**
     * 自動実行決定ファクトリメソッド
     */
    public static AiDecisionLog createAutomaticDecision(
            Long spaceId,
            DecisionType decisionType,
            String algorithmName,
            String inputParams,
            BigDecimal confidence,
            BigDecimal risk,
            BigDecimal impact,
            String recommendation,
            String rationale) {

        AiDecisionLog decision = AiDecisionLog.builder()
                .spaceId(spaceId)
                .decisionType(decisionType)
                .algorithmName(algorithmName)
                .inputParameters(inputParams)
                .confidenceScore(confidence)
                .riskScore(risk)
                .impactScore(impact)
                .recommendedAction(recommendation)
                .decisionRationale(rationale)
                .decisionDate(LocalDateTime.now())
                .computationTimeMs(System.currentTimeMillis())
                .build();

        // レビューの必要性を判定
        decision.humanReviewRequired = decision.requiresHumanReview();

        return decision;
    }

    /**
     * バーン決定ファクトリメソッド
     */
    public static AiDecisionLog createBurnDecision(
            Long spaceId,
            String marketData,
            String economicIndicators,
            BigDecimal confidence,
            BigDecimal burnAmount,
            String rationale) {

        return AiDecisionLog.builder()
                .spaceId(spaceId)
                .decisionType(DecisionType.TOKEN_BURN)
                .algorithmName("DeflatioNeuralNet")
                .marketData(marketData)
                .economicIndicators(economicIndicators)
                .confidenceScore(confidence)
                .riskScore(new BigDecimal("30.0")) // バーンは比較的低リスク
                .impactScore(new BigDecimal("85.0")) // 高い影響
                .recommendedAction("BURN_" + burnAmount + "_TOKENS")
                .decisionRationale(rationale)
                .decisionDate(LocalDateTime.now())
                .computationTimeMs(System.currentTimeMillis())
                .humanReviewRequired(true) // バーンは常にレビューが必要
                .build();
    }

    /**
     * リワード配布決定ファクトリメソッド
     */
    public static AiDecisionLog createRewardDecision(
            Long spaceId,
            String userBehaviorData,
            BigDecimal confidence,
            String rewardAction,
            String rationale) {

        return AiDecisionLog.builder()
                .spaceId(spaceId)
                .decisionType(DecisionType.REWARD_DISTRIBUTION)
                .algorithmName("RewardOptimizer")
                .userBehaviorData(userBehaviorData)
                .confidenceScore(confidence)
                .riskScore(new BigDecimal("15.0")) // リワードは低リスク
                .impactScore(new BigDecimal("60.0")) // 中程度の影響
                .recommendedAction(rewardAction)
                .decisionRationale(rationale)
                .decisionDate(LocalDateTime.now())
                .computationTimeMs(System.currentTimeMillis())
                .humanReviewRequired(false) // 自動実行可能
                .build();
    }
}
