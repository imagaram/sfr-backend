package com.sfr.tokyo.sfr_backend.entity.learning;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 学習分析レポート エンティティ
 * AI生成による学習分析とレポート生成データを管理
 */
@Entity
@Table(name = "learning_analytics_reports", indexes = {
        @Index(name = "idx_report_user_id", columnList = "user_id"),
        @Index(name = "idx_report_space_id", columnList = "space_id"),
        @Index(name = "idx_report_type", columnList = "report_type"),
        @Index(name = "idx_report_period", columnList = "analysis_start_date, analysis_end_date"),
        @Index(name = "idx_report_status", columnList = "status"),
        @Index(name = "idx_report_generated_at", columnList = "generated_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningAnalyticsReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "space_id")
    private UUID spaceId;

    @Column(name = "report_title", nullable = false, length = 500)
    private String reportTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    private ReportType reportType;

    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_scope", nullable = false)
    private AnalysisScope analysisScope;

    @Column(name = "analysis_start_date", nullable = false)
    private LocalDateTime analysisStartDate;

    @Column(name = "analysis_end_date", nullable = false)
    private LocalDateTime analysisEndDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReportStatus status;

    @Column(name = "ai_model", length = 100)
    private String aiModel;

    @Column(name = "ai_confidence", precision = 5, scale = 4)
    private BigDecimal aiConfidence;

    @Column(name = "processing_time_seconds")
    private Integer processingTimeSeconds;

    // レポート内容（JSON形式）
    @Lob
    @Column(name = "report_summary", columnDefinition = "TEXT")
    private String reportSummary;

    @Lob
    @Column(name = "detailed_analysis", columnDefinition = "LONGTEXT")
    private String detailedAnalysis;

    @Lob
    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;

    @Lob
    @Column(name = "insights", columnDefinition = "TEXT")
    private String insights;

    @Lob
    @Column(name = "performance_metrics", columnDefinition = "TEXT")
    private String performanceMetrics;

    @Lob
    @Column(name = "learning_patterns", columnDefinition = "TEXT")
    private String learningPatterns;

    @Lob
    @Column(name = "improvement_areas", columnDefinition = "TEXT")
    private String improvementAreas;

    @Lob
    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths;

    @Lob
    @Column(name = "weaknesses", columnDefinition = "TEXT")
    private String weaknesses;

    @Lob
    @Column(name = "learning_goals", columnDefinition = "TEXT")
    private String learningGoals;

    @Lob
    @Column(name = "achievement_summary", columnDefinition = "TEXT")
    private String achievementSummary;

    // 統計データ
    @Column(name = "total_learning_time_minutes")
    private Integer totalLearningTimeMinutes;

    @Column(name = "content_completed_count")
    private Integer contentCompletedCount;

    @Column(name = "quiz_average_score", precision = 5, scale = 2)
    private BigDecimal quizAverageScore;

    @Column(name = "engagement_score", precision = 5, scale = 2)
    private BigDecimal engagementScore;

    @Column(name = "progress_percentage", precision = 5, scale = 2)
    private BigDecimal progressPercentage;

    @Column(name = "consistency_score", precision = 5, scale = 2)
    private BigDecimal consistencyScore;

    // メタデータ
    @Column(name = "data_points_analyzed")
    private Integer dataPointsAnalyzed;

    @Lob
    @Column(name = "data_sources", columnDefinition = "TEXT")
    private String dataSources;

    @Column(name = "report_language", length = 10, nullable = false)
    private String reportLanguage;

    @Column(name = "report_format", length = 50)
    private String reportFormat;

    @Builder.Default
    @Column(name = "is_shared", nullable = false)
    private Boolean isShared = false;

    @Builder.Default
    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;

    @Builder.Default
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Builder.Default
    @Column(name = "download_count", nullable = false)
    private Integer downloadCount = 0;

    @Column(name = "tags", length = 1000)
    private String tags;

    @Column(name = "keywords", length = 1000)
    private String keywords;

    // 生成・更新情報
    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Column(name = "generated_by")
    private UUID generatedBy;

    @Column(name = "last_viewed_at")
    private LocalDateTime lastViewedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * レポートタイプ列挙型
     */
    public enum ReportType {
        INDIVIDUAL_PROGRESS("個人進捗レポート"),
        LEARNING_ANALYTICS("学習分析レポート"),
        PERFORMANCE_SUMMARY("パフォーマンス要約"),
        SKILL_ASSESSMENT("スキル評価"),
        ENGAGEMENT_ANALYSIS("エンゲージメント分析"),
        RECOMMENDATION_REPORT("推奨レポート"),
        COMPARISON_REPORT("比較レポート"),
        TREND_ANALYSIS("トレンド分析"),
        GOAL_TRACKING("目標追跡"),
        WEAKNESS_ANALYSIS("弱点分析"),
        STRENGTH_ANALYSIS("強み分析"),
        LEARNING_PATH("学習パス提案"),
        COMPREHENSIVE("総合レポート");

        private final String displayName;

        ReportType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 分析範囲列挙型
     */
    public enum AnalysisScope {
        INDIVIDUAL("個人"),
        SPACE("学習空間"),
        COURSE("コース"),
        MODULE("モジュール"),
        SKILL_AREA("スキル領域"),
        TIME_PERIOD("期間"),
        COMPARISON("比較"),
        GLOBAL("全体");

        private final String displayName;

        AnalysisScope(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * レポートステータス列挙型
     */
    public enum ReportStatus {
        PENDING("生成待ち"),
        PROCESSING("生成中"),
        COMPLETED("完了"),
        FAILED("失敗"),
        ARCHIVED("アーカイブ"),
        EXPIRED("期限切れ"),
        CANCELLED("キャンセル");

        private final String displayName;

        ReportStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // ========== ビジネスロジックメソッド ==========

    /**
     * レポートが完了しているかチェック
     */
    public boolean isCompleted() {
        return status == ReportStatus.COMPLETED;
    }

    /**
     * レポートが期限切れかチェック
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * レポートが削除されているかチェック
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * レポートが公開されているかチェック
     */
    public boolean isAccessible() {
        return !isDeleted() && !isExpired() && (isCompleted() || status == ReportStatus.PROCESSING);
    }

    /**
     * 閲覧数を増加
     */
    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
        this.lastViewedAt = LocalDateTime.now();
    }

    /**
     * ダウンロード数を増加
     */
    public void incrementDownloadCount() {
        this.downloadCount = (this.downloadCount == null ? 0 : this.downloadCount) + 1;
    }

    /**
     * レポート生成を開始
     */
    public void startGeneration() {
        this.status = ReportStatus.PROCESSING;
        this.generatedAt = LocalDateTime.now();
    }

    /**
     * レポート生成を完了
     */
    public void completeGeneration(Integer processingTimeSeconds) {
        this.status = ReportStatus.COMPLETED;
        this.processingTimeSeconds = processingTimeSeconds;
    }

    /**
     * レポート生成を失敗
     */
    public void failGeneration() {
        this.status = ReportStatus.FAILED;
    }

    /**
     * レポートをアーカイブ
     */
    public void archive() {
        this.status = ReportStatus.ARCHIVED;
        this.archivedAt = LocalDateTime.now();
    }

    /**
     * レポートを論理削除
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * レポートを復元
     */
    public void restore() {
        this.deletedAt = null;
        if (this.status == ReportStatus.ARCHIVED) {
            this.status = ReportStatus.COMPLETED;
            this.archivedAt = null;
        }
    }

    /**
     * 期限を設定
     */
    public void setExpiration(int daysFromNow) {
        this.expiresAt = LocalDateTime.now().plusDays(daysFromNow);
    }

    /**
     * 品質スコアを計算
     */
    public BigDecimal calculateQualityScore() {
        BigDecimal score = BigDecimal.ZERO;
        int factors = 0;

        // AI信頼度
        if (aiConfidence != null) {
            score = score.add(aiConfidence.multiply(BigDecimal.valueOf(30)));
            factors++;
        }

        // データ量
        if (dataPointsAnalyzed != null && dataPointsAnalyzed > 0) {
            BigDecimal dataScore = BigDecimal.valueOf(Math.min(dataPointsAnalyzed / 100.0, 1.0) * 25);
            score = score.add(dataScore);
            factors++;
        }

        // 処理時間（短いほど良い、但し最低限は必要）
        if (processingTimeSeconds != null && processingTimeSeconds > 0) {
            BigDecimal timeScore = BigDecimal.valueOf(Math.max(0, 20 - (processingTimeSeconds / 60.0)) * 2);
            score = score.add(timeScore);
            factors++;
        }

        // コンテンツの充実度
        int contentRichness = 0;
        if (reportSummary != null && !reportSummary.trim().isEmpty())
            contentRichness++;
        if (detailedAnalysis != null && !detailedAnalysis.trim().isEmpty())
            contentRichness++;
        if (recommendations != null && !recommendations.trim().isEmpty())
            contentRichness++;
        if (insights != null && !insights.trim().isEmpty())
            contentRichness++;

        BigDecimal contentScore = BigDecimal.valueOf(contentRichness * 6.25); // 最大25点
        score = score.add(contentScore);
        factors++;

        // 活用度
        if (viewCount != null && downloadCount != null) {
            BigDecimal usageScore = BigDecimal.valueOf(Math.min((viewCount + downloadCount * 2) / 10.0, 1.0) * 20);
            score = score.add(usageScore);
            factors++;
        }

        return factors > 0 ? score.divide(BigDecimal.valueOf(factors), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    /**
     * レポートの有効性をチェック
     */
    public boolean isValid() {
        return reportTitle != null && !reportTitle.trim().isEmpty() &&
                reportType != null &&
                analysisScope != null &&
                analysisStartDate != null &&
                analysisEndDate != null &&
                analysisStartDate.isBefore(analysisEndDate) &&
                userId != null;
    }
}
