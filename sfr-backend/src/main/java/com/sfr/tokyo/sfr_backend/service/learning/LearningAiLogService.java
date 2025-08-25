package com.sfr.tokyo.sfr_backend.service.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningAiLogDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningAiLog;
import com.sfr.tokyo.sfr_backend.repository.learning.LearningAiLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI学習ログサービス
 * AIインタラクション履歴の記録・分析・統計機能を提供
 */
@Service
@Transactional
public class LearningAiLogService {

    private final LearningAiLogRepository aiLogRepository;

    public LearningAiLogService(LearningAiLogRepository aiLogRepository) {
        this.aiLogRepository = aiLogRepository;
    }

    // ========== ログ記録機能 ==========

    /**
     * AI学習ログを作成
     */
    public LearningAiLogDto createAiLog(LearningAiLogDto dto) {
        LearningAiLog entity = convertToEntity(dto);
        entity = aiLogRepository.save(entity);
        return convertToDto(entity);
    }

    /**
     * AIインタラクションを開始（セッション開始）
     */
    public UUID startAiSession(UUID userId, LearningAiLog.AiType aiType, UUID spaceId) {
        UUID sessionId = UUID.randomUUID();

        LearningAiLog log = new LearningAiLog();
        log.setUserId(userId);
        log.setSpaceId(spaceId);
        log.setSessionId(sessionId);
        log.setAiType(aiType);
        log.setInteractionType(LearningAiLog.InteractionType.QUESTION);
        log.setUserInput("セッション開始");
        log.setAiResponse("AIセッションが開始されました");

        aiLogRepository.save(log);
        return sessionId;
    }

    /**
     * AIレスポンスを記録
     */
    public LearningAiLogDto recordAiResponse(UUID userId, UUID sessionId,
            LearningAiLog.AiType aiType,
            LearningAiLog.InteractionType interactionType,
            String userInput, String aiResponse,
            BigDecimal confidenceScore, Long processingTimeMs,
            Integer tokensUsed) {
        LearningAiLog log = new LearningAiLog();
        log.setUserId(userId);
        log.setSessionId(sessionId);
        log.setAiType(aiType);
        log.setInteractionType(interactionType);
        log.setUserInput(userInput);
        log.setAiResponse(aiResponse);
        log.setConfidenceScore(confidenceScore);
        log.setProcessingTimeMs(processingTimeMs);
        log.setTokensUsed(tokensUsed);
        log.setSuccess();

        log = aiLogRepository.save(log);
        return convertToDto(log);
    }

    /**
     * AIエラーを記録
     */
    public LearningAiLogDto recordAiError(UUID userId, UUID sessionId,
            LearningAiLog.AiType aiType,
            LearningAiLog.InteractionType interactionType,
            String userInput, String errorMessage,
            Long processingTimeMs) {
        LearningAiLog log = new LearningAiLog();
        log.setUserId(userId);
        log.setSessionId(sessionId);
        log.setAiType(aiType);
        log.setInteractionType(interactionType);
        log.setUserInput(userInput);
        log.setProcessingTimeMs(processingTimeMs);
        log.setError(errorMessage);

        log = aiLogRepository.save(log);
        return convertToDto(log);
    }

    // ========== ログ取得機能 ==========

    /**
     * ユーザーのAI学習ログ一覧を取得
     */
    @Transactional(readOnly = true)
    public Page<LearningAiLogDto> getUserAiLogs(UUID userId, Pageable pageable) {
        Page<LearningAiLog> logs = aiLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return logs.map(this::convertToDto);
    }

    /**
     * ユーザーの最近のAI学習ログを取得
     */
    @Transactional(readOnly = true)
    public List<LearningAiLogDto> getUserRecentAiLogs(UUID userId, int limit) {
        if (limit <= 50) {
            List<LearningAiLog> logs = aiLogRepository.findTop50ByUserIdOrderByCreatedAtDesc(userId);
            return logs.stream().limit(limit).map(this::convertToDto).collect(Collectors.toList());
        }
        return getUserAiLogs(userId, Pageable.ofSize(limit)).getContent();
    }

    /**
     * セッション別のログを取得
     */
    @Transactional(readOnly = true)
    public List<LearningAiLogDto> getSessionLogs(UUID sessionId) {
        List<LearningAiLog> logs = aiLogRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        return logs.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    /**
     * ユーザーのAIタイプ別ログを取得
     */
    @Transactional(readOnly = true)
    public Page<LearningAiLogDto> getUserLogsByAiType(UUID userId, LearningAiLog.AiType aiType, Pageable pageable) {
        Page<LearningAiLog> logs = aiLogRepository.findByUserIdAndAiTypeOrderByCreatedAtDesc(userId, aiType, pageable);
        return logs.map(this::convertToDto);
    }

    /**
     * ユーザーのスペース別ログを取得
     */
    @Transactional(readOnly = true)
    public Page<LearningAiLogDto> getUserLogsBySpace(UUID userId, UUID spaceId, Pageable pageable) {
        Page<LearningAiLog> logs = aiLogRepository.findByUserIdAndSpaceIdOrderByCreatedAtDesc(userId, spaceId,
                pageable);
        return logs.map(this::convertToDto);
    }

    /**
     * ユーザーの期間指定ログを取得
     */
    @Transactional(readOnly = true)
    public Page<LearningAiLogDto> getUserLogsByDateRange(UUID userId, LocalDateTime startDate,
            LocalDateTime endDate, Pageable pageable) {
        Page<LearningAiLog> logs = aiLogRepository.findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                userId, startDate, endDate, pageable);
        return logs.map(this::convertToDto);
    }

    // ========== 統計・分析機能 ==========

    /**
     * ユーザーのAI使用統計を取得
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserAiStatistics(UUID userId) {
        Map<String, Object> stats = new HashMap<>();

        // AIタイプ別ログ数
        List<Object[]> aiTypeStats = aiLogRepository.countByUserIdAndAiType(userId);
        Map<String, Long> aiTypeMap = new HashMap<>();
        for (Object[] stat : aiTypeStats) {
            LearningAiLog.AiType aiType = (LearningAiLog.AiType) stat[0];
            Long count = (Long) stat[1];
            aiTypeMap.put(aiType.getDisplayName(), count);
        }
        stats.put("aiTypeUsage", aiTypeMap);

        // 月別ログ数
        List<Object[]> monthlyStats = aiLogRepository.countByUserIdGroupByMonth(userId);
        List<Map<String, Object>> monthlyData = new ArrayList<>();
        for (Object[] stat : monthlyStats) {
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("year", stat[0]);
            monthData.put("month", stat[1]);
            monthData.put("count", stat[2]);
            monthlyData.add(monthData);
        }
        stats.put("monthlyUsage", monthlyData);

        // 平均値
        stats.put("averageSatisfaction", aiLogRepository.getAverageUserSatisfaction(userId));
        stats.put("averageConfidence", aiLogRepository.getAverageConfidenceScore(userId));
        stats.put("averageProcessingTime", aiLogRepository.getAverageProcessingTime(userId));
        stats.put("totalTokensUsed", aiLogRepository.getTotalTokensUsed(userId));
        stats.put("errorRate", aiLogRepository.getErrorRate(userId));

        return stats;
    }

    /**
     * スペースのAI使用統計を取得
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getSpaceAiStatistics(UUID spaceId) {
        Map<String, Object> stats = new HashMap<>();

        List<Object[]> aiTypeStats = aiLogRepository.countBySpaceIdAndAiType(spaceId);
        Map<String, Long> aiTypeMap = new HashMap<>();
        for (Object[] stat : aiTypeStats) {
            LearningAiLog.AiType aiType = (LearningAiLog.AiType) stat[0];
            Long count = (Long) stat[1];
            aiTypeMap.put(aiType.getDisplayName(), count);
        }
        stats.put("aiTypeUsage", aiTypeMap);

        return stats;
    }

    /**
     * グローバルAI使用統計を取得
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getGlobalAiStatistics() {
        Map<String, Object> stats = new HashMap<>();

        List<Object[]> globalStats = aiLogRepository.countByAiTypeGlobal();
        Map<String, Long> aiTypeMap = new HashMap<>();
        for (Object[] stat : globalStats) {
            LearningAiLog.AiType aiType = (LearningAiLog.AiType) stat[0];
            Long count = (Long) stat[1];
            aiTypeMap.put(aiType.getDisplayName(), count);
        }
        stats.put("globalAiTypeUsage", aiTypeMap);

        return stats;
    }

    /**
     * 最近のアクティブユーザーを取得
     */
    @Transactional(readOnly = true)
    public Page<UUID> getRecentActiveUsers(LocalDateTime since, Pageable pageable) {
        return aiLogRepository.findActiveUsersSince(since, pageable);
    }

    /**
     * 人気のAIタイプを取得
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPopularAiTypes(LocalDateTime since) {
        List<Object[]> popularTypes = aiLogRepository.findPopularAiTypesSince(since);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] type : popularTypes) {
            Map<String, Object> data = new HashMap<>();
            LearningAiLog.AiType aiType = (LearningAiLog.AiType) type[0];
            Long count = (Long) type[1];
            data.put("aiType", aiType.name());
            data.put("displayName", aiType.getDisplayName());
            data.put("usageCount", count);
            result.add(data);
        }

        return result;
    }

    // ========== セッション管理 ==========

    /**
     * ユーザーのセッション一覧を取得
     */
    @Transactional(readOnly = true)
    public Page<UUID> getUserSessions(UUID userId, Pageable pageable) {
        return aiLogRepository.findDistinctSessionIdsByUserId(userId, pageable);
    }

    /**
     * セッションの詳細情報を取得
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getSessionDetails(UUID sessionId) {
        List<LearningAiLog> logs = aiLogRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);

        if (logs.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Object> details = new HashMap<>();
        details.put("sessionId", sessionId);
        details.put("userId", logs.get(0).getUserId());
        details.put("aiType", logs.get(0).getAiType());
        details.put("startTime", logs.get(0).getCreatedAt());
        details.put("endTime", logs.get(logs.size() - 1).getCreatedAt());
        details.put("interactionCount", logs.size());

        // セッション統計
        long errorCount = logs.stream().mapToLong(log -> Boolean.TRUE.equals(log.getIsError()) ? 1 : 0).sum();
        OptionalDouble avgSatisfaction = logs.stream()
                .filter(log -> log.getUserSatisfaction() != null)
                .mapToInt(LearningAiLog::getUserSatisfaction)
                .average();

        details.put("errorCount", errorCount);
        details.put("errorRate", logs.size() > 0 ? (double) errorCount / logs.size() * 100 : 0);
        details.put("averageSatisfaction", avgSatisfaction.isPresent() ? avgSatisfaction.getAsDouble() : null);

        return details;
    }

    // ========== 更新機能 ==========

    /**
     * ユーザー満足度を更新
     */
    public LearningAiLogDto updateUserSatisfaction(Long logId, Integer satisfaction) {
        Optional<LearningAiLog> optionalLog = aiLogRepository.findById(logId);
        if (optionalLog.isPresent()) {
            LearningAiLog log = optionalLog.get();
            log.setUserSatisfaction(satisfaction);
            log = aiLogRepository.save(log);
            return convertToDto(log);
        }
        throw new RuntimeException("AI学習ログが見つかりません: " + logId);
    }

    /**
     * メタデータを更新
     */
    public LearningAiLogDto updateMetadata(Long logId, String metadata) {
        Optional<LearningAiLog> optionalLog = aiLogRepository.findById(logId);
        if (optionalLog.isPresent()) {
            LearningAiLog log = optionalLog.get();
            log.setMetadata(metadata);
            log = aiLogRepository.save(log);
            return convertToDto(log);
        }
        throw new RuntimeException("AI学習ログが見つかりません: " + logId);
    }

    // ========== 品質・パフォーマンス分析 ==========

    /**
     * 高満足度のログを取得
     */
    @Transactional(readOnly = true)
    public Page<LearningAiLogDto> getHighSatisfactionLogs(Pageable pageable) {
        Page<LearningAiLog> logs = aiLogRepository.findHighSatisfactionLogs(pageable);
        return logs.map(this::convertToDto);
    }

    /**
     * 低満足度のログを取得
     */
    @Transactional(readOnly = true)
    public Page<LearningAiLogDto> getLowSatisfactionLogs(Pageable pageable) {
        Page<LearningAiLog> logs = aiLogRepository.findLowSatisfactionLogs(pageable);
        return logs.map(this::convertToDto);
    }

    /**
     * エラーログを取得
     */
    @Transactional(readOnly = true)
    public Page<LearningAiLogDto> getErrorLogs(Pageable pageable) {
        Page<LearningAiLog> logs = aiLogRepository.findByIsErrorTrueOrderByCreatedAtDesc(pageable);
        return logs.map(this::convertToDto);
    }

    /**
     * 処理時間が長いログを取得
     */
    @Transactional(readOnly = true)
    public Page<LearningAiLogDto> getSlowResponseLogs(Long thresholdMs, Pageable pageable) {
        Page<LearningAiLog> logs = aiLogRepository.findSlowResponseLogs(thresholdMs, pageable);
        return logs.map(this::convertToDto);
    }

    // ========== 変換メソッド ==========

    /**
     * EntityからDTOに変換
     */
    private LearningAiLogDto convertToDto(LearningAiLog entity) {
        LearningAiLogDto dto = new LearningAiLogDto();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setSpaceId(entity.getSpaceId());
        dto.setSessionId(entity.getSessionId());
        dto.setAiType(entity.getAiType());
        dto.setInteractionType(entity.getInteractionType());
        dto.setUserInput(entity.getUserInput());
        dto.setAiResponse(entity.getAiResponse());
        dto.setContentId(entity.getContentId());
        dto.setQuizId(entity.getQuizId());
        dto.setConfidenceScore(entity.getConfidenceScore());
        dto.setUserSatisfaction(entity.getUserSatisfaction());
        dto.setProcessingTimeMs(entity.getProcessingTimeMs());
        dto.setTokensUsed(entity.getTokensUsed());
        dto.setIsError(entity.getIsError());
        dto.setErrorMessage(entity.getErrorMessage());
        dto.setMetadata(entity.getMetadata());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    /**
     * DTOからEntityに変換
     */
    private LearningAiLog convertToEntity(LearningAiLogDto dto) {
        LearningAiLog entity = new LearningAiLog();
        if (dto.getId() != null) {
            entity.setId(dto.getId());
        }
        entity.setUserId(dto.getUserId());
        entity.setSpaceId(dto.getSpaceId());
        entity.setSessionId(dto.getSessionId());
        entity.setAiType(dto.getAiType());
        entity.setInteractionType(dto.getInteractionType());
        entity.setUserInput(dto.getUserInput());
        entity.setAiResponse(dto.getAiResponse());
        entity.setContentId(dto.getContentId());
        entity.setQuizId(dto.getQuizId());
        entity.setConfidenceScore(dto.getConfidenceScore());
        entity.setUserSatisfaction(dto.getUserSatisfaction());
        entity.setProcessingTimeMs(dto.getProcessingTimeMs());
        entity.setTokensUsed(dto.getTokensUsed());
        entity.setIsError(dto.getIsError());
        entity.setErrorMessage(dto.getErrorMessage());
        entity.setMetadata(dto.getMetadata());
        return entity;
    }
}
