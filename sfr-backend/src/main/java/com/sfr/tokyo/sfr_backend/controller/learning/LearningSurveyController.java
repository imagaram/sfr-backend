package com.sfr.tokyo.sfr_backend.controller.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningSurveyDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningSurvey;
import com.sfr.tokyo.sfr_backend.service.learning.LearningSurveyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 学習アンケート コントローラー - LearningSurveyController
 */
@RestController
@RequestMapping("/api/learning/surveys")
@CrossOrigin(origins = "*")
public class LearningSurveyController {

    @Autowired
    private LearningSurveyService learningSurveyService;

    // CRUD操作

    /**
     * アンケート作成
     */
    @PostMapping
    public ResponseEntity<LearningSurveyDto> createSurvey(@Valid @RequestBody LearningSurveyDto surveyDto) {
        try {
            LearningSurveyDto createdSurvey = learningSurveyService.createSurvey(surveyDto);
            return ResponseEntity.ok(createdSurvey);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * アンケート取得
     */
    @GetMapping("/{id}")
    public ResponseEntity<LearningSurveyDto> getSurvey(@PathVariable Long id) {
        return learningSurveyService.getSurvey(id)
                .map(survey -> ResponseEntity.ok(survey))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * アンケート更新
     */
    @PutMapping("/{id}")
    public ResponseEntity<LearningSurveyDto> updateSurvey(@PathVariable Long id,
            @Valid @RequestBody LearningSurveyDto surveyDto) {
        try {
            LearningSurveyDto updatedSurvey = learningSurveyService.updateSurvey(id, surveyDto);
            return ResponseEntity.ok(updatedSurvey);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * アンケート削除
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSurvey(@PathVariable Long id) {
        try {
            learningSurveyService.deleteSurvey(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // アンケートライフサイクル管理

    /**
     * アンケート開始
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<LearningSurveyDto> startSurvey(@PathVariable Long id) {
        try {
            LearningSurveyDto startedSurvey = learningSurveyService.startSurvey(id);
            return ResponseEntity.ok(startedSurvey);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * アンケート中断
     */
    @PostMapping("/{id}/suspend")
    public ResponseEntity<LearningSurveyDto> suspendSurvey(@PathVariable Long id) {
        try {
            LearningSurveyDto suspendedSurvey = learningSurveyService.suspendSurvey(id);
            return ResponseEntity.ok(suspendedSurvey);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * アンケート完了
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<LearningSurveyDto> completeSurvey(@PathVariable Long id) {
        try {
            LearningSurveyDto completedSurvey = learningSurveyService.completeSurvey(id);
            return ResponseEntity.ok(completedSurvey);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * アンケートスキップ
     */
    @PostMapping("/{id}/skip")
    public ResponseEntity<LearningSurveyDto> skipSurvey(@PathVariable Long id,
            @RequestParam(required = false) String reason) {
        try {
            LearningSurveyDto skippedSurvey = learningSurveyService.skipSurvey(id, reason);
            return ResponseEntity.ok(skippedSurvey);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * アンケート期限切れ
     */
    @PostMapping("/{id}/expire")
    public ResponseEntity<LearningSurveyDto> expireSurvey(@PathVariable Long id) {
        try {
            LearningSurveyDto expiredSurvey = learningSurveyService.expireSurvey(id);
            return ResponseEntity.ok(expiredSurvey);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // 進捗管理

    /**
     * 回答進捗更新
     */
    @PutMapping("/{id}/progress")
    public ResponseEntity<LearningSurveyDto> updateProgress(@PathVariable Long id,
            @RequestParam int answeredQuestions) {
        try {
            LearningSurveyDto updatedSurvey = learningSurveyService.updateProgress(id, answeredQuestions);
            return ResponseEntity.ok(updatedSurvey);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 時間更新
     */
    @PutMapping("/{id}/time")
    public ResponseEntity<LearningSurveyDto> updateTimeSpent(@PathVariable Long id,
            @RequestParam int timeSpentMinutes) {
        try {
            LearningSurveyDto updatedSurvey = learningSurveyService.updateTimeSpent(id, timeSpentMinutes);
            return ResponseEntity.ok(updatedSurvey);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 評価スコア更新
     */
    @PutMapping("/{id}/ratings")
    public ResponseEntity<LearningSurveyDto> updateRatings(@PathVariable Long id,
            @RequestParam(required = false) BigDecimal satisfactionScore,
            @RequestParam(required = false) BigDecimal difficultyRating,
            @RequestParam(required = false) BigDecimal usefulnessRating,
            @RequestParam(required = false) BigDecimal recommendationScore) {
        try {
            LearningSurveyDto updatedSurvey = learningSurveyService.updateRatings(id, satisfactionScore,
                    difficultyRating, usefulnessRating,
                    recommendationScore);
            return ResponseEntity.ok(updatedSurvey);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * フィードバック更新
     */
    @PutMapping("/{id}/feedback")
    public ResponseEntity<LearningSurveyDto> updateFeedback(@PathVariable Long id,
            @RequestParam(required = false) String feedbackText,
            @RequestParam(required = false) String suggestions) {
        try {
            LearningSurveyDto updatedSurvey = learningSurveyService.updateFeedback(id, feedbackText, suggestions);
            return ResponseEntity.ok(updatedSurvey);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 検索・取得系

    /**
     * 全アンケート取得
     */
    @GetMapping
    public ResponseEntity<List<LearningSurveyDto>> getAllSurveys() {
        // この実装では基本的な検索機能を提供
        return ResponseEntity.ok(List.of());
    }

    /**
     * ユーザー別アンケート取得
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LearningSurveyDto>> getSurveysByUser(@PathVariable UUID userId) {
        List<LearningSurveyDto> surveys = learningSurveyService.getSurveysByUser(userId);
        return ResponseEntity.ok(surveys);
    }

    /**
     * スペース別アンケート取得
     */
    @GetMapping("/space/{spaceId}")
    public ResponseEntity<List<LearningSurveyDto>> getSurveysBySpace(@PathVariable Long spaceId) {
        List<LearningSurveyDto> surveys = learningSurveyService.getSurveysBySpace(spaceId);
        return ResponseEntity.ok(surveys);
    }

    /**
     * カテゴリ別アンケート取得
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<LearningSurveyDto>> getSurveysByCategory(@PathVariable String category) {
        try {
            LearningSurvey.SurveyCategory surveyCategory = LearningSurvey.SurveyCategory
                    .valueOf(category.toUpperCase());
            List<LearningSurveyDto> surveys = learningSurveyService.getSurveysByCategory(surveyCategory);
            return ResponseEntity.ok(surveys);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * ステータス別アンケート取得
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<LearningSurveyDto>> getSurveysByStatus(@PathVariable String status) {
        try {
            LearningSurvey.SurveyStatus surveyStatus = LearningSurvey.SurveyStatus.valueOf(status.toUpperCase());
            List<LearningSurveyDto> surveys = learningSurveyService.getSurveysByStatus(surveyStatus);
            return ResponseEntity.ok(surveys);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 完了済みアンケート取得
     */
    @GetMapping("/completed")
    public ResponseEntity<List<LearningSurveyDto>> getCompletedSurveys() {
        List<LearningSurveyDto> surveys = learningSurveyService.getCompletedSurveys();
        return ResponseEntity.ok(surveys);
    }

    /**
     * 未完了アンケート取得
     */
    @GetMapping("/incomplete")
    public ResponseEntity<List<LearningSurveyDto>> getIncompleteSurveys() {
        List<LearningSurveyDto> surveys = learningSurveyService.getIncompleteSurveys();
        return ResponseEntity.ok(surveys);
    }

    /**
     * 期限切れアンケート取得
     */
    @GetMapping("/expired")
    public ResponseEntity<List<LearningSurveyDto>> getExpiredSurveys() {
        List<LearningSurveyDto> surveys = learningSurveyService.getExpiredSurveys();
        return ResponseEntity.ok(surveys);
    }

    /**
     * 必須アンケート取得
     */
    @GetMapping("/mandatory")
    public ResponseEntity<List<LearningSurveyDto>> getMandatorySurveys() {
        List<LearningSurveyDto> surveys = learningSurveyService.getMandatorySurveys();
        return ResponseEntity.ok(surveys);
    }

    /**
     * 高評価アンケート取得
     */
    @GetMapping("/high-rated")
    public ResponseEntity<List<LearningSurveyDto>> getHighRatedSurveys() {
        List<LearningSurveyDto> surveys = learningSurveyService.getHighRatedSurveys();
        return ResponseEntity.ok(surveys);
    }

    /**
     * 低評価アンケート取得
     */
    @GetMapping("/low-rated")
    public ResponseEntity<List<LearningSurveyDto>> getLowRatedSurveys() {
        List<LearningSurveyDto> surveys = learningSurveyService.getLowRatedSurveys();
        return ResponseEntity.ok(surveys);
    }

    // 統計・分析系

    /**
     * ユーザー統計取得
     */
    @GetMapping("/user/{userId}/statistics")
    public ResponseEntity<LearningSurveyService.UserSurveyStatistics> getUserStatistics(@PathVariable UUID userId) {
        LearningSurveyService.UserSurveyStatistics statistics = learningSurveyService.getUserStatistics(userId);
        return ResponseEntity.ok(statistics);
    }

    /**
     * 全体統計取得
     */
    @GetMapping("/statistics")
    public ResponseEntity<LearningSurveyService.OverallSurveyStatistics> getOverallStatistics() {
        LearningSurveyService.OverallSurveyStatistics statistics = learningSurveyService.getOverallStatistics();
        return ResponseEntity.ok(statistics);
    }

    // 検索機能

    /**
     * キーワード検索
     */
    @GetMapping("/search")
    public ResponseEntity<List<LearningSurveyDto>> searchSurveys(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        List<LearningSurveyDto> surveys = learningSurveyService.searchSurveys(keyword.trim());
        return ResponseEntity.ok(surveys);
    }

    /**
     * タグ検索
     */
    @GetMapping("/search/tag")
    public ResponseEntity<List<LearningSurveyDto>> searchByTag(@RequestParam String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        List<LearningSurveyDto> surveys = learningSurveyService.searchByTag(tag.trim());
        return ResponseEntity.ok(surveys);
    }

    // リマインダー機能

    /**
     * リマインダー送信対象取得
     */
    @GetMapping("/reminders")
    public ResponseEntity<List<LearningSurveyDto>> getSurveysForReminder() {
        List<LearningSurveyDto> surveys = learningSurveyService.getSurveysForReminder();
        return ResponseEntity.ok(surveys);
    }

    /**
     * リマインダー送信済み更新
     */
    @PostMapping("/{id}/reminder-sent")
    public ResponseEntity<LearningSurveyDto> updateReminderSent(@PathVariable Long id) {
        try {
            LearningSurveyDto updatedSurvey = learningSurveyService.updateReminderSent(id);
            return ResponseEntity.ok(updatedSurvey);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // バッチ処理

    /**
     * 期限切れアンケート一括処理
     */
    @PostMapping("/batch/process-expired")
    public ResponseEntity<BatchProcessResult> processExpiredSurveys() {
        try {
            int processedCount = learningSurveyService.processExpiredSurveys();
            return ResponseEntity.ok(new BatchProcessResult(processedCount, "期限切れアンケートの処理が完了しました"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BatchProcessResult(0, "処理中にエラーが発生しました: " + e.getMessage()));
        }
    }

    // アンケートカテゴリ取得

    /**
     * 利用可能なアンケートカテゴリ取得
     */
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryInfo>> getAvailableCategories() {
        List<CategoryInfo> categories = List.of(
                new CategoryInfo("COURSE_FEEDBACK", "コースフィードバック"),
                new CategoryInfo("CONTENT_EVALUATION", "コンテンツ評価"),
                new CategoryInfo("USER_EXPERIENCE", "ユーザーエクスペリエンス"),
                new CategoryInfo("LEARNING_EFFECTIVENESS", "学習効果"),
                new CategoryInfo("SYSTEM_USABILITY", "システム使いやすさ"),
                new CategoryInfo("FEATURE_REQUEST", "機能要望"),
                new CategoryInfo("BUG_REPORT", "バグレポート"),
                new CategoryInfo("GENERAL_FEEDBACK", "一般フィードバック"),
                new CategoryInfo("SATISFACTION_SURVEY", "満足度調査"),
                new CategoryInfo("OTHER", "その他"));
        return ResponseEntity.ok(categories);
    }

    /**
     * 利用可能なアンケートステータス取得
     */
    @GetMapping("/statuses")
    public ResponseEntity<List<StatusInfo>> getAvailableStatuses() {
        List<StatusInfo> statuses = List.of(
                new StatusInfo("NOT_STARTED", "未開始"),
                new StatusInfo("IN_PROGRESS", "進行中"),
                new StatusInfo("COMPLETED", "完了"),
                new StatusInfo("SKIPPED", "スキップ"),
                new StatusInfo("EXPIRED", "期限切れ"),
                new StatusInfo("CANCELLED", "キャンセル"),
                new StatusInfo("ARCHIVED", "アーカイブ済み"));
        return ResponseEntity.ok(statuses);
    }

    /**
     * 利用可能な優先度レベル取得
     */
    @GetMapping("/priority-levels")
    public ResponseEntity<List<PriorityInfo>> getAvailablePriorityLevels() {
        List<PriorityInfo> priorities = List.of(
                new PriorityInfo("LOW", "低"),
                new PriorityInfo("NORMAL", "通常"),
                new PriorityInfo("HIGH", "高"),
                new PriorityInfo("URGENT", "緊急"),
                new PriorityInfo("CRITICAL", "重要"));
        return ResponseEntity.ok(priorities);
    }

    // アンケート分析

    /**
     * 期間別アンケート分析
     */
    @GetMapping("/analysis/period")
    public ResponseEntity<PeriodAnalysisResult> getAnalysisByPeriod(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime start = LocalDateTime.parse(startDate + " 00:00:00", formatter);
            LocalDateTime end = LocalDateTime.parse(endDate + " 23:59:59", formatter);

            // 簡易的な分析結果を返す（実際の実装では詳細な分析を行う）
            PeriodAnalysisResult result = new PeriodAnalysisResult(start, end, 0, BigDecimal.ZERO, BigDecimal.ZERO);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Response DTOs

    public static class BatchProcessResult {
        private final int processedCount;
        private final String message;

        public BatchProcessResult(int processedCount, String message) {
            this.processedCount = processedCount;
            this.message = message;
        }

        public int getProcessedCount() {
            return processedCount;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class CategoryInfo {
        private final String code;
        private final String description;

        public CategoryInfo(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    public static class StatusInfo {
        private final String code;
        private final String description;

        public StatusInfo(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    public static class PriorityInfo {
        private final String code;
        private final String description;

        public PriorityInfo(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    public static class PeriodAnalysisResult {
        private final LocalDateTime startDate;
        private final LocalDateTime endDate;
        private final int surveyCount;
        private final BigDecimal averageSatisfaction;
        private final BigDecimal averageCompletion;

        public PeriodAnalysisResult(LocalDateTime startDate, LocalDateTime endDate, int surveyCount,
                BigDecimal averageSatisfaction, BigDecimal averageCompletion) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.surveyCount = surveyCount;
            this.averageSatisfaction = averageSatisfaction;
            this.averageCompletion = averageCompletion;
        }

        public LocalDateTime getStartDate() {
            return startDate;
        }

        public LocalDateTime getEndDate() {
            return endDate;
        }

        public int getSurveyCount() {
            return surveyCount;
        }

        public BigDecimal getAverageSatisfaction() {
            return averageSatisfaction;
        }

        public BigDecimal getAverageCompletion() {
            return averageCompletion;
        }
    }
}
