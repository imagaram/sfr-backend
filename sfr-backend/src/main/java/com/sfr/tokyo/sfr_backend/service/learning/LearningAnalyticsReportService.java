package com.sfr.tokyo.sfr_backend.service.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningAnalyticsReportDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningAnalyticsReport;
import com.sfr.tokyo.sfr_backend.repository.learning.LearningAnalyticsReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学習分析レポート AI統合サービス
 * AI活用による学習データ分析とレポート自動生成機能を提供
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LearningAnalyticsReportService {

    private final LearningAnalyticsReportRepository reportRepository;

    // 将来的に他のlearningサービスを注入してデータを収集
    // private final LearningProgressService progressService;
    // private final LearningQuizService quizService;
    // private final LearningContentService contentService;

    // ========== レポート生成機能 ==========

    /**
     * AI分析レポート生成
     */
    @Transactional
    public LearningAnalyticsReportDto generateAnalyticsReport(LearningAnalyticsReportDto.GenerationRequest request) {
        log.info("AI分析レポート生成開始: userId={}, reportType={}", request.getUserId(), request.getReportType());

        try {
            // 1. リクエスト検証
            validateGenerationRequest(request);

            // 2. レポートエンティティ作成
            LearningAnalyticsReport report = createReportEntity(request);

            // 3. レポート生成開始
            report.startGeneration();
            report = reportRepository.save(report);

            // 4. 学習データ収集
            LearningDataCollection dataCollection = collectLearningData(request);

            // 5. AI分析実行
            long startTime = System.currentTimeMillis();
            AiAnalysisResult analysisResult = performAiAnalysis(dataCollection, request);
            long processingTime = (System.currentTimeMillis() - startTime) / 1000;

            // 6. レポート内容生成
            populateReportContent(report, analysisResult, dataCollection);

            // 7. レポート完了
            report.completeGeneration((int) processingTime);
            if (request.getExpirationDays() != null) {
                report.setExpiration(request.getExpirationDays());
            }

            report = reportRepository.save(report);
            log.info("AI分析レポート生成完了: reportId={}, processingTime={}s", report.getId(), processingTime);

            return convertToDto(report);

        } catch (Exception e) {
            log.error("AI分析レポート生成エラー: userId={}, error={}", request.getUserId(), e.getMessage(), e);
            throw new RuntimeException("レポート生成に失敗しました: " + e.getMessage());
        }
    }

    /**
     * バッチレポート生成（複数ユーザー向け）
     */
    @Transactional
    public List<LearningAnalyticsReportDto> generateBatchReports(
            List<LearningAnalyticsReportDto.GenerationRequest> requests) {
        log.info("バッチレポート生成開始: requestCount={}", requests.size());

        List<LearningAnalyticsReportDto> results = new ArrayList<>();

        for (LearningAnalyticsReportDto.GenerationRequest request : requests) {
            try {
                LearningAnalyticsReportDto report = generateAnalyticsReport(request);
                results.add(report);
            } catch (Exception e) {
                log.error("バッチレポート生成エラー: userId={}, error={}", request.getUserId(), e.getMessage());
                // エラーレポートを作成
                LearningAnalyticsReportDto errorReport = createErrorReport(request, e.getMessage());
                results.add(errorReport);
            }
        }

        log.info("バッチレポート生成完了: successCount={}, totalCount={}",
                results.stream().filter(r -> r.getStatus() == LearningAnalyticsReport.ReportStatus.COMPLETED).count(),
                results.size());

        return results;
    }

    /**
     * 定期レポート自動生成
     */
    @Transactional
    public void generateScheduledReports() {
        log.info("定期レポート自動生成開始");

        // TODO: ユーザー設定から定期レポート対象を取得
        // 現在はサンプル実装
        List<UUID> activeUsers = getActiveUsersForScheduledReports();

        for (UUID userId : activeUsers) {
            try {
                // 週次進捗レポート生成
                generateWeeklyProgressReport(userId);

                // 月次総合レポート生成
                if (shouldGenerateMonthlyReport(userId)) {
                    generateMonthlyComprehensiveReport(userId);
                }

            } catch (Exception e) {
                log.error("定期レポート生成エラー: userId={}, error={}", userId, e.getMessage());
            }
        }

        log.info("定期レポート自動生成完了");
    }

    // ========== レポート取得・管理機能 ==========

    /**
     * レポート取得
     */
    @Transactional(readOnly = true)
    public LearningAnalyticsReportDto getReport(Long reportId) {
        LearningAnalyticsReport report = reportRepository.findByIdAndNotDeleted(reportId)
                .orElseThrow(() -> new RuntimeException("レポートが見つかりません: " + reportId));

        // 閲覧数増加
        reportRepository.incrementViewCount(reportId);

        return convertToDto(report);
    }

    /**
     * ユーザーレポート一覧取得
     */
    @Transactional(readOnly = true)
    public Page<LearningAnalyticsReportDto> getUserReports(UUID userId, Pageable pageable) {
        return reportRepository.findByUserId(userId, pageable)
                .map(this::convertToDto);
    }

    /**
     * スペースレポート一覧取得
     */
    @Transactional(readOnly = true)
    public Page<LearningAnalyticsReportDto> getSpaceReports(UUID spaceId, Pageable pageable) {
        return reportRepository.findBySpaceId(spaceId, pageable)
                .map(this::convertToDto);
    }

    /**
     * 公開レポート一覧取得
     */
    @Transactional(readOnly = true)
    public Page<LearningAnalyticsReportDto> getPublicReports(Pageable pageable) {
        return reportRepository.findPublicReports(pageable)
                .map(this::convertToDto);
    }

    /**
     * 複合検索
     */
    @Transactional(readOnly = true)
    public Page<LearningAnalyticsReportDto> searchReports(LearningAnalyticsReportDto.SearchFilter filter,
            Pageable pageable) {
        return reportRepository.findByComplexCriteria(
                filter.getUserId(),
                filter.getSpaceId(),
                filter.getReportType(),
                filter.getStatus(),
                filter.getAnalysisScope(),
                filter.getReportLanguage(),
                filter.getIsPublic(),
                pageable).map(this::convertToDto);
    }

    /**
     * キーワード検索
     */
    @Transactional(readOnly = true)
    public Page<LearningAnalyticsReportDto> searchByKeyword(String keyword, Pageable pageable) {
        return reportRepository.searchByKeyword(keyword, pageable)
                .map(this::convertToDto);
    }

    // ========== AI分析関連機能 ==========

    /**
     * 学習パフォーマンス分析
     */
    @Transactional(readOnly = true)
    public LearningAnalyticsReportDto.PerformanceMetrics analyzePerformance(UUID userId, UUID spaceId,
            LocalDateTime startDate, LocalDateTime endDate) {
        log.info("学習パフォーマンス分析: userId={}, spaceId={}", userId, spaceId);

        // TODO: 実際の学習データからパフォーマンス分析
        // 現在はモック実装
        return LearningAnalyticsReportDto.PerformanceMetrics.builder()
                .overallScore(BigDecimal.valueOf(85.5))
                .learningEfficiency(BigDecimal.valueOf(78.2))
                .engagementLevel(BigDecimal.valueOf(92.1))
                .progressRate(BigDecimal.valueOf(76.8))
                .consistencyScore(BigDecimal.valueOf(88.3))
                .improvementRate(BigDecimal.valueOf(12.5))
                .skillLevels(Map.of(
                        "プログラミング", BigDecimal.valueOf(80.0),
                        "数学", BigDecimal.valueOf(75.5),
                        "英語", BigDecimal.valueOf(90.2)))
                .achievements(List.of("月間目標達成", "7日連続学習", "quiz満点"))
                .build();
    }

    /**
     * 学習パターン分析
     */
    @Transactional(readOnly = true)
    public LearningAnalyticsReportDto.LearningPatterns analyzeLearningPatterns(UUID userId,
            LocalDateTime startDate, LocalDateTime endDate) {
        log.info("学習パターン分析: userId={}", userId);

        // TODO: 実際の学習ログからパターン分析
        return LearningAnalyticsReportDto.LearningPatterns.builder()
                .preferredLearningTime("19:00-21:00")
                .learningStyle("視覚的学習者")
                .strongSubjects(List.of("プログラミング", "論理思考"))
                .weakSubjects(List.of("記憶系学習", "暗算"))
                .studyHabits(Map.of(
                        "平均学習時間/日", 90,
                        "連続学習日数", 5,
                        "休憩頻度", 3))
                .motivationFactors(List.of("成果の可視化", "目標設定", "コミュニティ交流"))
                .attentionSpan(BigDecimal.valueOf(25.5))
                .learningPace("標準")
                .challengeAreas(List.of("集中力維持", "復習習慣"))
                .build();
    }

    /**
     * 改善提案生成
     */
    @Transactional(readOnly = true)
    public LearningAnalyticsReportDto.ImprovementSuggestions generateImprovementSuggestions(
            UUID userId, LearningAnalyticsReportDto.PerformanceMetrics performance,
            LearningAnalyticsReportDto.LearningPatterns patterns) {

        log.info("改善提案生成: userId={}", userId);

        // TODO: AI分析による個別化された改善提案
        return LearningAnalyticsReportDto.ImprovementSuggestions.builder()
                .shortTermGoals(List.of(
                        "毎日30分の復習時間確保",
                        "弱点分野の集中学習",
                        "学習記録の習慣化"))
                .longTermGoals(List.of(
                        "プログラミングスキルの上級レベル到達",
                        "英語資格取得",
                        "自己学習能力の向上"))
                .studyMethods(List.of(
                        "ポモドーロテクニック活用",
                        "アクティブリコール練習",
                        "スペース反復学習"))
                .resourceRecommendations(List.of(
                        "インタラクティブ教材の活用",
                        "オンライン演習問題",
                        "学習コミュニティ参加"))
                .timeManagement("朝の時間帯も活用し、学習時間を分散")
                .motivationStrategies(List.of(
                        "小さな目標設定と達成感",
                        "学習仲間との進捗共有",
                        "定期的な成果振り返り"))
                .build();
    }

    /**
     * トレンド分析
     */
    @Transactional(readOnly = true)
    public LearningAnalyticsReportDto.TrendAnalysis analyzeTrends(UUID userId,
            LocalDateTime startDate, LocalDateTime endDate) {
        log.info("トレンド分析: userId={}", userId);

        // TODO: 時系列データ分析によるトレンド検出
        return LearningAnalyticsReportDto.TrendAnalysis.builder()
                .trendDirection("上昇傾向")
                .improvementRate(BigDecimal.valueOf(15.3))
                .positiveTrends(List.of(
                        "学習時間の増加",
                        "quiz正答率向上",
                        "継続率改善"))
                .concerningTrends(List.of(
                        "復習頻度の低下",
                        "難易度の高い内容への取り組み不足"))
                .monthlyProgress(Map.of(
                        "1月", BigDecimal.valueOf(75.0),
                        "2月", BigDecimal.valueOf(82.5),
                        "3月", BigDecimal.valueOf(88.2)))
                .predictions(List.of(
                        "継続的な成長が期待される",
                        "次月は90点台到達の可能性",
                        "弱点克服に注力すれば大幅改善見込み"))
                .build();
    }

    // ========== 統計・集計機能 ==========

    /**
     * レポート統計取得
     */
    @Transactional(readOnly = true)
    public LearningAnalyticsReportDto.ReportStatistics getReportStatistics() {
        Map<String, Object> stats = reportRepository.getOverallStatistics();

        List<Object[]> typeStats = reportRepository.getReportTypeStatistics();
        Map<LearningAnalyticsReport.ReportType, Long> reportsByType = typeStats.stream()
                .collect(Collectors.toMap(
                        row -> (LearningAnalyticsReport.ReportType) row[0],
                        row -> ((Number) row[1]).longValue()));

        List<Object[]> languageStats = reportRepository.getLanguageStatistics();
        Map<String, Long> reportsByLanguage = languageStats.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> ((Number) row[1]).longValue()));

        return LearningAnalyticsReportDto.ReportStatistics.builder()
                .totalReports(((Number) stats.get("totalReports")).longValue())
                .completedReports(((Number) stats.get("completedReports")).longValue())
                .pendingReports(((Number) stats.get("pendingReports")).longValue())
                .failedReports(((Number) stats.get("failedReports")).longValue())
                .averageQualityScore((BigDecimal) stats.get("avgConfidence"))
                .averageProcessingTime(BigDecimal.valueOf(((Number) stats.get("avgProcessingTime")).doubleValue()))
                .totalViewCount(((Number) stats.get("totalViews")).intValue())
                .totalDownloadCount(((Number) stats.get("totalDownloads")).intValue())
                .reportsByType(reportsByType)
                .reportsByLanguage(reportsByLanguage)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    /**
     * 人気レポート取得
     */
    @Transactional(readOnly = true)
    public List<LearningAnalyticsReportDto> getPopularReports(int limit) {
        return reportRepository.findPopularReports(PageRequest.of(0, limit))
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 最新レポート取得
     */
    @Transactional(readOnly = true)
    public List<LearningAnalyticsReportDto> getRecentReports(int limit) {
        return reportRepository.findRecentReports(PageRequest.of(0, limit))
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ========== レポート操作機能 ==========

    /**
     * レポート削除
     */
    @Transactional
    public void deleteReport(Long reportId) {
        LearningAnalyticsReport report = reportRepository.findByIdAndNotDeleted(reportId)
                .orElseThrow(() -> new RuntimeException("レポートが見つかりません: " + reportId));

        report.softDelete();
        reportRepository.save(report);

        log.info("レポート削除完了: reportId={}", reportId);
    }

    /**
     * レポート復元
     */
    @Transactional
    public void restoreReport(Long reportId) {
        reportRepository.restore(reportId);
        log.info("レポート復元完了: reportId={}", reportId);
    }

    /**
     * レポートアーカイブ
     */
    @Transactional
    public void archiveReport(Long reportId) {
        reportRepository.archive(reportId);
        log.info("レポートアーカイブ完了: reportId={}", reportId);
    }

    /**
     * レポートダウンロード
     */
    @Transactional
    public void downloadReport(Long reportId) {
        reportRepository.incrementDownloadCount(reportId);
        log.info("レポートダウンロード記録: reportId={}", reportId);
    }

    // ========== バッチ処理機能 ==========

    /**
     * 期限切れレポート処理
     */
    @Transactional
    public void processExpiredReports() {
        log.info("期限切れレポート処理開始");

        int expiredCount = reportRepository.markExpiredReports();
        log.info("期限切れレポート処理完了: expiredCount={}", expiredCount);
    }

    /**
     * 古いレポートアーカイブ
     */
    @Transactional
    public void archiveOldReports(int daysOld, int minViewCount) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        int archivedCount = reportRepository.archiveOldReports(cutoffDate, minViewCount);

        log.info("古いレポートアーカイブ完了: archivedCount={}, cutoffDate={}, minViewCount={}",
                archivedCount, cutoffDate, minViewCount);
    }

    /**
     * 失敗レポートクリーンアップ
     */
    @Transactional
    public void cleanupFailedReports(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        int cleanedCount = reportRepository.cleanupFailedReports(cutoffDate);

        log.info("失敗レポートクリーンアップ完了: cleanedCount={}, cutoffDate={}", cleanedCount, cutoffDate);
    }

    // ========== プライベートメソッド ==========

    /**
     * レポート生成リクエスト検証
     */
    private void validateGenerationRequest(LearningAnalyticsReportDto.GenerationRequest request) {
        if (request.getAnalysisStartDate().isAfter(request.getAnalysisEndDate())) {
            throw new IllegalArgumentException("分析開始日は終了日より前である必要があります");
        }

        if (request.getAnalysisEndDate().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("分析終了日は現在日時より前である必要があります");
        }

        long daysBetween = ChronoUnit.DAYS.between(request.getAnalysisStartDate(), request.getAnalysisEndDate());
        if (daysBetween > 365) {
            throw new IllegalArgumentException("分析期間は365日以内である必要があります");
        }
    }

    /**
     * レポートエンティティ作成
     */
    private LearningAnalyticsReport createReportEntity(LearningAnalyticsReportDto.GenerationRequest request) {
        return LearningAnalyticsReport.builder()
                .userId(request.getUserId())
                .spaceId(request.getSpaceId())
                .reportTitle(request.getReportTitle())
                .reportType(request.getReportType())
                .analysisScope(request.getAnalysisScope())
                .analysisStartDate(request.getAnalysisStartDate())
                .analysisEndDate(request.getAnalysisEndDate())
                .status(LearningAnalyticsReport.ReportStatus.PENDING)
                .aiModel(request.getAiModel() != null ? request.getAiModel() : "gpt-4")
                .reportLanguage(request.getReportLanguage())
                .reportFormat(request.getReportFormat())
                .isShared(request.getIsShared())
                .isPublic(request.getIsPublic())
                .generatedBy(request.getUserId())
                .build();
    }

    /**
     * 学習データ収集
     */
    private LearningDataCollection collectLearningData(LearningAnalyticsReportDto.GenerationRequest request) {
        // TODO: 実際のlearningサービスからデータ収集
        // 現在はモック実装
        return LearningDataCollection.builder()
                .userId(request.getUserId())
                .spaceId(request.getSpaceId())
                .dataPointCount(150)
                .learningTimeMinutes(1200)
                .contentCompletedCount(25)
                .quizAverageScore(BigDecimal.valueOf(82.5))
                .engagementScore(BigDecimal.valueOf(88.2))
                .build();
    }

    /**
     * AI分析実行
     */
    private AiAnalysisResult performAiAnalysis(LearningDataCollection dataCollection,
            LearningAnalyticsReportDto.GenerationRequest request) {
        // TODO: 実際のAI API連携
        // 現在はモック実装

        // AI信頼度計算（データ量と品質に基づく）
        BigDecimal confidence = calculateAiConfidence(dataCollection);

        return AiAnalysisResult.builder()
                .summary("学習者は継続的な成長を示しており、特にプログラミング分野で優秀な成果を上げています。")
                .insights(List.of(
                        "学習時間が増加傾向にあり、習慣化が成功している",
                        "quiz正答率が安定して高く、理解度が深い",
                        "難易度の高い内容にも積極的に取り組んでいる"))
                .recommendations(List.of(
                        "復習頻度を増やすことで定着率向上が期待できる",
                        "弱点分野への重点的な取り組みを推奨",
                        "学習ペースの調整で更なる効率化が可能"))
                .patterns(List.of(
                        "夜間学習が中心",
                        "視覚的教材を好む傾向",
                        "短時間集中型の学習スタイル"))
                .metrics(Map.of(
                        "総合スコア", 85.2,
                        "改善率", 12.5,
                        "継続率", 94.1))
                .confidence(confidence)
                .build();
    }

    /**
     * AI信頼度計算
     */
    private BigDecimal calculateAiConfidence(LearningDataCollection dataCollection) {
        BigDecimal confidence = BigDecimal.valueOf(0.5); // ベースライン

        // データ量による調整
        if (dataCollection.getDataPointCount() > 100) {
            confidence = confidence.add(BigDecimal.valueOf(0.2));
        }
        if (dataCollection.getDataPointCount() > 50) {
            confidence = confidence.add(BigDecimal.valueOf(0.1));
        }

        // 学習時間による調整
        if (dataCollection.getLearningTimeMinutes() > 600) {
            confidence = confidence.add(BigDecimal.valueOf(0.1));
        }

        // エンゲージメントスコアによる調整
        if (dataCollection.getEngagementScore() != null &&
                dataCollection.getEngagementScore().compareTo(BigDecimal.valueOf(80)) > 0) {
            confidence = confidence.add(BigDecimal.valueOf(0.1));
        }

        return confidence.min(BigDecimal.valueOf(1.0));
    }

    /**
     * レポート内容生成
     */
    private void populateReportContent(LearningAnalyticsReport report, AiAnalysisResult analysisResult,
            LearningDataCollection dataCollection) {
        report.setReportSummary(analysisResult.getSummary());
        report.setDetailedAnalysis(generateDetailedAnalysis(analysisResult, dataCollection));
        report.setRecommendations(String.join("\n", analysisResult.getRecommendations()));
        report.setInsights(String.join("\n", analysisResult.getInsights()));
        report.setLearningPatterns(String.join("\n", analysisResult.getPatterns()));
        report.setAiConfidence(analysisResult.getConfidence());

        // 統計データ設定
        report.setTotalLearningTimeMinutes(dataCollection.getLearningTimeMinutes());
        report.setContentCompletedCount(dataCollection.getContentCompletedCount());
        report.setQuizAverageScore(dataCollection.getQuizAverageScore());
        report.setEngagementScore(dataCollection.getEngagementScore());
        report.setDataPointsAnalyzed(dataCollection.getDataPointCount());

        // 進捗率計算
        BigDecimal progressPercentage = calculateProgressPercentage(dataCollection);
        report.setProgressPercentage(progressPercentage);

        // 一貫性スコア計算
        BigDecimal consistencyScore = calculateConsistencyScore(dataCollection);
        report.setConsistencyScore(consistencyScore);
    }

    /**
     * 詳細分析生成
     */
    private String generateDetailedAnalysis(AiAnalysisResult analysisResult, LearningDataCollection dataCollection) {
        StringBuilder analysis = new StringBuilder();

        analysis.append("## 学習データ分析結果\n\n");
        analysis.append("### 基本統計\n");
        analysis.append("- 分析データポイント数: ").append(dataCollection.getDataPointCount()).append("\n");
        analysis.append("- 総学習時間: ").append(dataCollection.getLearningTimeMinutes()).append("分\n");
        analysis.append("- 完了コンテンツ数: ").append(dataCollection.getContentCompletedCount()).append("\n");
        analysis.append("- クイズ平均スコア: ").append(dataCollection.getQuizAverageScore()).append("%\n\n");

        analysis.append("### AI分析結果\n");
        analysis.append(analysisResult.getSummary()).append("\n\n");

        analysis.append("### 主要な洞察\n");
        for (String insight : analysisResult.getInsights()) {
            analysis.append("- ").append(insight).append("\n");
        }

        return analysis.toString();
    }

    /**
     * 進捗率計算
     */
    private BigDecimal calculateProgressPercentage(LearningDataCollection dataCollection) {
        // TODO: 実際の進捗データに基づく計算
        // 現在はサンプル計算
        if (dataCollection.getContentCompletedCount() == null) {
            return BigDecimal.ZERO;
        }

        // 仮の総コンテンツ数を100として計算
        int totalContent = 100;
        BigDecimal progress = BigDecimal.valueOf(dataCollection.getContentCompletedCount())
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalContent), 2, RoundingMode.HALF_UP);

        return progress.min(BigDecimal.valueOf(100));
    }

    /**
     * 一貫性スコア計算
     */
    private BigDecimal calculateConsistencyScore(LearningDataCollection dataCollection) {
        // TODO: 学習ログの分析による一貫性計算
        // 現在はサンプル計算
        return BigDecimal.valueOf(85.0);
    }

    /**
     * エラーレポート作成
     */
    private LearningAnalyticsReportDto createErrorReport(LearningAnalyticsReportDto.GenerationRequest request,
            String errorMessage) {
        return LearningAnalyticsReportDto.builder()
                .userId(request.getUserId())
                .spaceId(request.getSpaceId())
                .reportTitle(request.getReportTitle() + " (生成失敗)")
                .reportType(request.getReportType())
                .status(LearningAnalyticsReport.ReportStatus.FAILED)
                .reportSummary("レポート生成中にエラーが発生しました: " + errorMessage)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * アクティブユーザー取得（定期レポート用）
     */
    private List<UUID> getActiveUsersForScheduledReports() {
        // TODO: 実際のユーザーアクティビティから判定
        return List.of(); // 現在は空リスト
    }

    /**
     * 週次進捗レポート生成
     */
    private void generateWeeklyProgressReport(UUID userId) {
        LearningAnalyticsReportDto.GenerationRequest request = LearningAnalyticsReportDto.GenerationRequest.builder()
                .userId(userId)
                .reportTitle("週次進捗レポート - " + LocalDateTime.now().toString())
                .reportType(LearningAnalyticsReport.ReportType.INDIVIDUAL_PROGRESS)
                .analysisScope(LearningAnalyticsReport.AnalysisScope.TIME_PERIOD)
                .analysisStartDate(LocalDateTime.now().minusWeeks(1))
                .analysisEndDate(LocalDateTime.now())
                .reportLanguage("ja")
                .expirationDays(30)
                .build();

        generateAnalyticsReport(request);
    }

    /**
     * 月次レポート生成判定
     */
    private boolean shouldGenerateMonthlyReport(UUID userId) {
        // 月初めかどうか判定
        LocalDateTime now = LocalDateTime.now();
        return now.getDayOfMonth() == 1;
    }

    /**
     * 月次総合レポート生成
     */
    private void generateMonthlyComprehensiveReport(UUID userId) {
        LearningAnalyticsReportDto.GenerationRequest request = LearningAnalyticsReportDto.GenerationRequest.builder()
                .userId(userId)
                .reportTitle("月次総合レポート - " + LocalDateTime.now().toString())
                .reportType(LearningAnalyticsReport.ReportType.COMPREHENSIVE)
                .analysisScope(LearningAnalyticsReport.AnalysisScope.TIME_PERIOD)
                .analysisStartDate(LocalDateTime.now().minusMonths(1))
                .analysisEndDate(LocalDateTime.now())
                .reportLanguage("ja")
                .expirationDays(90)
                .build();

        generateAnalyticsReport(request);
    }

    /**
     * EntityからDTOへの変換
     */
    private LearningAnalyticsReportDto convertToDto(LearningAnalyticsReport entity) {
        LearningAnalyticsReportDto dto = new LearningAnalyticsReportDto();
        BeanUtils.copyProperties(entity, dto);

        // 追加フィールドの設定
        dto.setReportTypeDisplayName(entity.getReportType().getDisplayName());
        dto.setAnalysisScopeDisplayName(entity.getAnalysisScope().getDisplayName());
        dto.setStatusDisplayName(entity.getStatus().getDisplayName());
        dto.setQualityScore(entity.calculateQualityScore());
        dto.setIsCompleted(entity.isCompleted());
        dto.setIsExpired(entity.isExpired());
        dto.setIsDeleted(entity.isDeleted());
        dto.setIsAccessible(entity.isAccessible());

        // 分析期間日数計算
        if (entity.getAnalysisStartDate() != null && entity.getAnalysisEndDate() != null) {
            long days = ChronoUnit.DAYS.between(entity.getAnalysisStartDate(), entity.getAnalysisEndDate());
            dto.setAnalysisRangeDays((int) days);
        }

        // タグ・キーワード配列変換
        if (entity.getTags() != null && !entity.getTags().trim().isEmpty()) {
            dto.setTagList(Arrays.asList(entity.getTags().split(",")));
        }
        if (entity.getKeywords() != null && !entity.getKeywords().trim().isEmpty()) {
            dto.setKeywordList(Arrays.asList(entity.getKeywords().split(",")));
        }

        return dto;
    }

    // ========== 内部データクラス ==========

    /**
     * 学習データ収集結果
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class LearningDataCollection {
        private UUID userId;
        private UUID spaceId;
        private Integer dataPointCount;
        private Integer learningTimeMinutes;
        private Integer contentCompletedCount;
        private BigDecimal quizAverageScore;
        private BigDecimal engagementScore;
    }

    /**
     * AI分析結果
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class AiAnalysisResult {
        private String summary;
        private List<String> insights;
        private List<String> recommendations;
        private List<String> patterns;
        private Map<String, Object> metrics;
        private BigDecimal confidence;
    }
}
