package com.sfr.tokyo.sfr_backend.dto.learning;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningAnalyticsReport;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 学習分析レポート DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LearningAnalyticsReportDto {

    private Long id;

    @NotNull(message = "ユーザーIDは必須です")
    private UUID userId;

    private UUID spaceId;

    @NotBlank(message = "レポートタイトルは必須です")
    @Size(max = 500, message = "レポートタイトルは500文字以内で入力してください")
    private String reportTitle;

    @NotNull(message = "レポートタイプは必須です")
    private LearningAnalyticsReport.ReportType reportType;

    @NotNull(message = "分析範囲は必須です")
    private LearningAnalyticsReport.AnalysisScope analysisScope;

    @NotNull(message = "分析開始日は必須です")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime analysisStartDate;

    @NotNull(message = "分析終了日は必須です")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime analysisEndDate;

    private LearningAnalyticsReport.ReportStatus status;

    @Size(max = 100, message = "AIモデル名は100文字以内で入力してください")
    private String aiModel;

    @DecimalMin(value = "0.0", message = "AI信頼度は0.0以上である必要があります")
    @DecimalMax(value = "1.0", message = "AI信頼度は1.0以下である必要があります")
    private BigDecimal aiConfidence;

    private Integer processingTimeSeconds;

    // レポート内容
    private String reportSummary;
    private String detailedAnalysis;
    private String recommendations;
    private String insights;
    private String performanceMetrics;
    private String learningPatterns;
    private String improvementAreas;
    private String strengths;
    private String weaknesses;
    private String learningGoals;
    private String achievementSummary;

    // 統計データ
    private Integer totalLearningTimeMinutes;
    private Integer contentCompletedCount;
    private BigDecimal quizAverageScore;
    private BigDecimal engagementScore;
    private BigDecimal progressPercentage;
    private BigDecimal consistencyScore;

    // メタデータ
    private Integer dataPointsAnalyzed;
    private String dataSources;

    @NotBlank(message = "レポート言語は必須です")
    @Size(max = 10, message = "レポート言語は10文字以内で入力してください")
    private String reportLanguage;

    private String reportFormat;
    private Boolean isShared;
    private Boolean isPublic;
    private Integer viewCount;
    private Integer downloadCount;

    private List<String> tagList;
    private List<String> keywordList;

    // 生成・更新情報
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime generatedAt;

    private UUID generatedBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastViewedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime archivedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deletedAt;

    // 追加フィールド
    private String reportTypeDisplayName;
    private String analysisScopeDisplayName;
    private String statusDisplayName;
    private BigDecimal qualityScore;
    private Boolean isCompleted;
    private Boolean isExpired;
    private Boolean isDeleted;
    private Boolean isAccessible;
    private Integer analysisRangeDays;

    // ========== 内部クラス ==========

    /**
     * レポート要約情報
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReportSummary {
        private Long id;
        private String reportTitle;
        private LearningAnalyticsReport.ReportType reportType;
        private String reportTypeDisplayName;
        private LearningAnalyticsReport.ReportStatus status;
        private String statusDisplayName;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime generatedAt;
        private BigDecimal qualityScore;
        private Integer viewCount;
        private Boolean isPublic;
    }

    /**
     * レポート生成リクエスト
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GenerationRequest {
        @NotNull(message = "ユーザーIDは必須です")
        private UUID userId;

        private UUID spaceId;

        @NotBlank(message = "レポートタイトルは必須です")
        @Size(max = 500, message = "レポートタイトルは500文字以内で入力してください")
        private String reportTitle;

        @NotNull(message = "レポートタイプは必須です")
        private LearningAnalyticsReport.ReportType reportType;

        @NotNull(message = "分析範囲は必須です")
        private LearningAnalyticsReport.AnalysisScope analysisScope;

        @NotNull(message = "分析開始日は必須です")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime analysisStartDate;

        @NotNull(message = "分析終了日は必須です")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime analysisEndDate;

        @Size(max = 100, message = "AIモデル名は100文字以内で入力してください")
        private String aiModel;

        @Builder.Default
        @NotBlank(message = "レポート言語は必須です")
        @Size(max = 10, message = "レポート言語は10文字以内で入力してください")
        private String reportLanguage = "ja";

        @Builder.Default
        private String reportFormat = "HTML";

        @Builder.Default
        private Boolean isShared = false;
        @Builder.Default
        private Boolean isPublic = false;

        private List<String> focusAreas;
        private List<String> includeMetrics;
        private Map<String, Object> customOptions;

        @Builder.Default
        @Min(value = 1, message = "有効期限は1日以上である必要があります")
        @Max(value = 365, message = "有効期限は365日以下である必要があります")
        private Integer expirationDays = 30;
    }

    /**
     * レポート統計情報
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReportStatistics {
        private Long totalReports;
        private Long completedReports;
        private Long pendingReports;
        private Long failedReports;
        private BigDecimal averageQualityScore;
        private BigDecimal averageProcessingTime;
        private Integer totalViewCount;
        private Integer totalDownloadCount;
        private Map<LearningAnalyticsReport.ReportType, Long> reportsByType;
        private Map<LearningAnalyticsReport.AnalysisScope, Long> reportsByScope;
        private Map<String, Long> reportsByLanguage;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime lastUpdated;
    }

    /**
     * レポート検索フィルター
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SearchFilter {
        private UUID userId;
        private UUID spaceId;
        private LearningAnalyticsReport.ReportType reportType;
        private LearningAnalyticsReport.AnalysisScope analysisScope;
        private LearningAnalyticsReport.ReportStatus status;
        private String keyword;
        private String reportLanguage;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime generatedAfter;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime generatedBefore;

        private BigDecimal minQualityScore;
        private Boolean isPublic;
        private Boolean includeArchived;
        private Boolean includeExpired;

        private List<String> tags;
        private List<String> keywords;
    }

    /**
     * AI分析結果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AiAnalysisResult {
        private String analysis;
        private BigDecimal confidence;
        private List<String> insights;
        private List<String> recommendations;
        private Map<String, Object> metrics;
        private List<String> patterns;
        private List<String> anomalies;
        private Map<String, BigDecimal> scores;
    }

    /**
     * パフォーマンス指標
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PerformanceMetrics {
        private BigDecimal overallScore;
        private BigDecimal learningEfficiency;
        private BigDecimal engagementLevel;
        private BigDecimal progressRate;
        private BigDecimal consistencyScore;
        private BigDecimal improvementRate;
        private Map<String, BigDecimal> skillLevels;
        private Map<String, BigDecimal> subjectScores;
        private List<String> achievements;
        private List<String> milestones;
    }

    /**
     * 学習パターン分析
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LearningPatterns {
        private String preferredLearningTime;
        private String learningStyle;
        private List<String> strongSubjects;
        private List<String> weakSubjects;
        private Map<String, Integer> studyHabits;
        private List<String> motivationFactors;
        private BigDecimal attentionSpan;
        private String learningPace;
        private List<String> challengeAreas;
        private Map<String, Object> behavioralPatterns;
    }

    /**
     * 改善提案
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ImprovementSuggestions {
        private List<String> shortTermGoals;
        private List<String> longTermGoals;
        private List<String> studyMethods;
        private List<String> resourceRecommendations;
        private Map<String, String> skillDevelopment;
        private List<String> practiceAreas;
        private String timeManagement;
        private List<String> motivationStrategies;
        private Map<String, Object> personalizedPlan;
    }

    /**
     * トレンド分析
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TrendAnalysis {
        private String trendDirection;
        private BigDecimal improvementRate;
        private List<String> positiveTrends;
        private List<String> concerningTrends;
        private Map<String, BigDecimal> monthlyProgress;
        private Map<String, BigDecimal> skillTrends;
        private List<String> predictions;
        private Map<String, Object> seasonalPatterns;
    }
}
