package com.sfr.tokyo.sfr_backend.controller.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningAiLogDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningAiLog;
import com.sfr.tokyo.sfr_backend.service.learning.LearningAiLogService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * AI学習ログコントローラー
 * AIインタラクション履歴の記録・取得・分析APIを提供
 */
@RestController
@RequestMapping("/api/learning/ai-logs")
public class LearningAiLogController {

    private final LearningAiLogService aiLogService;

    public LearningAiLogController(LearningAiLogService aiLogService) {
        this.aiLogService = aiLogService;
    }

    // ========== ログ記録機能 ==========

    /**
     * AI学習ログを作成
     */
    @PostMapping
    public ResponseEntity<LearningAiLogDto> createAiLog(@Valid @RequestBody LearningAiLogDto dto) {
        LearningAiLogDto createdLog = aiLogService.createAiLog(dto);
        return ResponseEntity.ok(createdLog);
    }

    /**
     * AIセッションを開始
     */
    @PostMapping("/sessions/start")
    public ResponseEntity<Map<String, Object>> startAiSession(
            @RequestParam UUID userId,
            @RequestParam LearningAiLog.AiType aiType,
            @RequestParam(required = false) UUID spaceId) {

        UUID sessionId = aiLogService.startAiSession(userId, aiType, spaceId);
        return ResponseEntity.ok(Map.of(
                "sessionId", sessionId,
                "message", "AIセッションが開始されました"));
    }

    /**
     * AIレスポンスを記録
     */
    @PostMapping("/responses")
    public ResponseEntity<LearningAiLogDto> recordAiResponse(
            @RequestParam UUID userId,
            @RequestParam UUID sessionId,
            @RequestParam LearningAiLog.AiType aiType,
            @RequestParam LearningAiLog.InteractionType interactionType,
            @RequestParam String userInput,
            @RequestParam String aiResponse,
            @RequestParam(required = false) BigDecimal confidenceScore,
            @RequestParam(required = false) Long processingTimeMs,
            @RequestParam(required = false) Integer tokensUsed) {

        LearningAiLogDto log = aiLogService.recordAiResponse(
                userId, sessionId, aiType, interactionType,
                userInput, aiResponse, confidenceScore, processingTimeMs, tokensUsed);
        return ResponseEntity.ok(log);
    }

    /**
     * AIエラーを記録
     */
    @PostMapping("/errors")
    public ResponseEntity<LearningAiLogDto> recordAiError(
            @RequestParam UUID userId,
            @RequestParam UUID sessionId,
            @RequestParam LearningAiLog.AiType aiType,
            @RequestParam LearningAiLog.InteractionType interactionType,
            @RequestParam String userInput,
            @RequestParam String errorMessage,
            @RequestParam(required = false) Long processingTimeMs) {

        LearningAiLogDto log = aiLogService.recordAiError(
                userId, sessionId, aiType, interactionType,
                userInput, errorMessage, processingTimeMs);
        return ResponseEntity.ok(log);
    }

    // ========== ログ取得機能 ==========

    /**
     * ユーザーのAI学習ログ一覧を取得
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<Page<LearningAiLogDto>> getUserAiLogs(
            @PathVariable UUID userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<LearningAiLogDto> logs = aiLogService.getUserAiLogs(userId, pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * ユーザーの最近のAI学習ログを取得
     */
    @GetMapping("/users/{userId}/recent")
    public ResponseEntity<List<LearningAiLogDto>> getUserRecentAiLogs(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "10") int limit) {

        List<LearningAiLogDto> logs = aiLogService.getUserRecentAiLogs(userId, limit);
        return ResponseEntity.ok(logs);
    }

    /**
     * セッション別のログを取得
     */
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<List<LearningAiLogDto>> getSessionLogs(@PathVariable UUID sessionId) {
        List<LearningAiLogDto> logs = aiLogService.getSessionLogs(sessionId);
        return ResponseEntity.ok(logs);
    }

    /**
     * セッションの詳細情報を取得
     */
    @GetMapping("/sessions/{sessionId}/details")
    public ResponseEntity<Map<String, Object>> getSessionDetails(@PathVariable UUID sessionId) {
        Map<String, Object> details = aiLogService.getSessionDetails(sessionId);
        return ResponseEntity.ok(details);
    }

    /**
     * ユーザーのAIタイプ別ログを取得
     */
    @GetMapping("/users/{userId}/ai-types/{aiType}")
    public ResponseEntity<Page<LearningAiLogDto>> getUserLogsByAiType(
            @PathVariable UUID userId,
            @PathVariable LearningAiLog.AiType aiType,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<LearningAiLogDto> logs = aiLogService.getUserLogsByAiType(userId, aiType, pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * ユーザーのスペース別ログを取得
     */
    @GetMapping("/users/{userId}/spaces/{spaceId}")
    public ResponseEntity<Page<LearningAiLogDto>> getUserLogsBySpace(
            @PathVariable UUID userId,
            @PathVariable UUID spaceId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<LearningAiLogDto> logs = aiLogService.getUserLogsBySpace(userId, spaceId, pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * ユーザーの期間指定ログを取得
     */
    @GetMapping("/users/{userId}/date-range")
    public ResponseEntity<Page<LearningAiLogDto>> getUserLogsByDateRange(
            @PathVariable UUID userId,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<LearningAiLogDto> logs = aiLogService.getUserLogsByDateRange(userId, startDate, endDate, pageable);
        return ResponseEntity.ok(logs);
    }

    // ========== セッション管理 ==========

    /**
     * ユーザーのセッション一覧を取得
     */
    @GetMapping("/users/{userId}/sessions")
    public ResponseEntity<Page<UUID>> getUserSessions(
            @PathVariable UUID userId,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<UUID> sessions = aiLogService.getUserSessions(userId, pageable);
        return ResponseEntity.ok(sessions);
    }

    // ========== 統計・分析機能 ==========

    /**
     * ユーザーのAI使用統計を取得
     */
    @GetMapping("/users/{userId}/statistics")
    public ResponseEntity<Map<String, Object>> getUserAiStatistics(@PathVariable UUID userId) {
        Map<String, Object> stats = aiLogService.getUserAiStatistics(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * スペースのAI使用統計を取得
     */
    @GetMapping("/spaces/{spaceId}/statistics")
    public ResponseEntity<Map<String, Object>> getSpaceAiStatistics(@PathVariable UUID spaceId) {
        Map<String, Object> stats = aiLogService.getSpaceAiStatistics(spaceId);
        return ResponseEntity.ok(stats);
    }

    /**
     * グローバルAI使用統計を取得
     */
    @GetMapping("/statistics/global")
    public ResponseEntity<Map<String, Object>> getGlobalAiStatistics() {
        Map<String, Object> stats = aiLogService.getGlobalAiStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * 最近のアクティブユーザーを取得
     */
    @GetMapping("/active-users")
    public ResponseEntity<Page<UUID>> getRecentActiveUsers(
            @RequestParam(defaultValue = "24") int hoursAgo,
            @PageableDefault(size = 20) Pageable pageable) {

        LocalDateTime since = LocalDateTime.now().minusHours(hoursAgo);
        Page<UUID> activeUsers = aiLogService.getRecentActiveUsers(since, pageable);
        return ResponseEntity.ok(activeUsers);
    }

    /**
     * 人気のAIタイプを取得
     */
    @GetMapping("/popular-ai-types")
    public ResponseEntity<List<Map<String, Object>>> getPopularAiTypes(
            @RequestParam(defaultValue = "24") int hoursAgo) {

        LocalDateTime since = LocalDateTime.now().minusHours(hoursAgo);
        List<Map<String, Object>> popularTypes = aiLogService.getPopularAiTypes(since);
        return ResponseEntity.ok(popularTypes);
    }

    // ========== 品質・パフォーマンス分析 ==========

    /**
     * 高満足度のログを取得
     */
    @GetMapping("/high-satisfaction")
    public ResponseEntity<Page<LearningAiLogDto>> getHighSatisfactionLogs(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<LearningAiLogDto> logs = aiLogService.getHighSatisfactionLogs(pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * 低満足度のログを取得
     */
    @GetMapping("/low-satisfaction")
    public ResponseEntity<Page<LearningAiLogDto>> getLowSatisfactionLogs(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<LearningAiLogDto> logs = aiLogService.getLowSatisfactionLogs(pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * エラーログを取得
     */
    @GetMapping("/errors")
    public ResponseEntity<Page<LearningAiLogDto>> getErrorLogs(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<LearningAiLogDto> logs = aiLogService.getErrorLogs(pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * 処理時間が長いログを取得
     */
    @GetMapping("/slow-responses")
    public ResponseEntity<Page<LearningAiLogDto>> getSlowResponseLogs(
            @RequestParam(defaultValue = "5000") Long thresholdMs,
            @PageableDefault(size = 20, sort = "processingTimeMs", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<LearningAiLogDto> logs = aiLogService.getSlowResponseLogs(thresholdMs, pageable);
        return ResponseEntity.ok(logs);
    }

    // ========== 更新機能 ==========

    /**
     * ユーザー満足度を更新
     */
    @PutMapping("/{logId}/satisfaction")
    public ResponseEntity<LearningAiLogDto> updateUserSatisfaction(
            @PathVariable Long logId,
            @RequestParam Integer satisfaction) {

        LearningAiLogDto updatedLog = aiLogService.updateUserSatisfaction(logId, satisfaction);
        return ResponseEntity.ok(updatedLog);
    }

    /**
     * メタデータを更新
     */
    @PutMapping("/{logId}/metadata")
    public ResponseEntity<LearningAiLogDto> updateMetadata(
            @PathVariable Long logId,
            @RequestParam String metadata) {

        LearningAiLogDto updatedLog = aiLogService.updateMetadata(logId, metadata);
        return ResponseEntity.ok(updatedLog);
    }

    // ========== 便利機能 ==========

    /**
     * AIタイプ一覧を取得
     */
    @GetMapping("/ai-types")
    public ResponseEntity<Map<String, String>> getAiTypes() {
        Map<String, String> aiTypes = Map.of(
                "LEARNING_ASSISTANT", "学習アシスタント",
                "CONTENT_RECOMMENDER", "コンテンツレコメンダー",
                "QUIZ_GENERATOR", "クイズ生成",
                "PROGRESS_ANALYZER", "進捗分析",
                "WRITING_ASSISTANT", "文章作成支援",
                "CODE_REVIEWER", "コードレビュー",
                "STUDY_PLANNER", "学習計画立案",
                "SKILL_ASSESSOR", "スキル評価");
        return ResponseEntity.ok(aiTypes);
    }

    /**
     * インタラクションタイプ一覧を取得
     */
    @GetMapping("/interaction-types")
    public ResponseEntity<Map<String, String>> getInteractionTypes() {
        Map<String, String> interactionTypes = Map.of(
                "QUESTION", "質問",
                "RECOMMENDATION", "レコメンデーション",
                "ANALYSIS", "分析",
                "GENERATION", "生成",
                "REVIEW", "レビュー",
                "FEEDBACK", "フィードバック",
                "PLANNING", "計画立案",
                "ASSESSMENT", "評価");
        return ResponseEntity.ok(interactionTypes);
    }
}
