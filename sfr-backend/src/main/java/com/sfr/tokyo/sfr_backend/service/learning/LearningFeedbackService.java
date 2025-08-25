package com.sfr.tokyo.sfr_backend.service.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningFeedback;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningFeedback.*;
import com.sfr.tokyo.sfr_backend.dto.learning.LearningFeedbackDto;
import com.sfr.tokyo.sfr_backend.repository.learning.LearningFeedbackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * LearningFeedback Service
 * フィードバック管理サービス
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningFeedbackService {

    private final LearningFeedbackRepository feedbackRepository;

    // ========== CRUD操作 ==========

    /**
     * フィードバック作成
     */
    @Transactional
    public LearningFeedbackDto createFeedback(LearningFeedbackDto dto) {
        try {
            LearningFeedback feedback = convertToEntity(dto);
            feedback.setCreatedAt(LocalDateTime.now());
            feedback.setUpdatedAt(LocalDateTime.now());

            // スコア計算
            calculateAndSetScores(feedback);

            LearningFeedback savedFeedback = feedbackRepository.save(feedback);
            log.info("フィードバックが作成されました: ID={}, Type={}, Category={}",
                    savedFeedback.getId(), savedFeedback.getFeedbackType(), savedFeedback.getFeedbackCategory());

            return convertToDto(savedFeedback);
        } catch (Exception e) {
            log.error("フィードバック作成中にエラーが発生しました: {}", e.getMessage(), e);
            throw new RuntimeException("フィードバックの作成に失敗しました", e);
        }
    }

    /**
     * フィードバック取得
     */
    public LearningFeedbackDto getFeedback(Long feedbackId) {
        LearningFeedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("フィードバックが見つかりません: " + feedbackId));
        return convertToDto(feedback);
    }

    /**
     * フィードバック更新
     */
    @Transactional
    public LearningFeedbackDto updateFeedback(Long feedbackId, LearningFeedbackDto dto) {
        try {
            LearningFeedback feedback = feedbackRepository.findById(feedbackId)
                    .orElseThrow(() -> new RuntimeException("フィードバックが見つかりません: " + feedbackId));

            // 更新可能フィールドの設定
            updateFeedbackFields(feedback, dto);
            feedback.setUpdatedAt(LocalDateTime.now());

            // スコア再計算
            calculateAndSetScores(feedback);

            LearningFeedback updatedFeedback = feedbackRepository.save(feedback);
            log.info("フィードバックが更新されました: ID={}", feedbackId);

            return convertToDto(updatedFeedback);
        } catch (Exception e) {
            log.error("フィードバック更新中にエラーが発生しました: ID={}, Error={}", feedbackId, e.getMessage(), e);
            throw new RuntimeException("フィードバックの更新に失敗しました", e);
        }
    }

    /**
     * フィードバック削除（論理削除）
     */
    @Transactional
    public void deleteFeedback(Long feedbackId) {
        try {
            feedbackRepository.softDeleteFeedback(feedbackId);
            log.info("フィードバックが削除されました: ID={}", feedbackId);
        } catch (Exception e) {
            log.error("フィードバック削除中にエラーが発生しました: ID={}, Error={}", feedbackId, e.getMessage(), e);
            throw new RuntimeException("フィードバックの削除に失敗しました", e);
        }
    }

    // ========== 検索操作 ==========

    /**
     * 対象別フィードバック取得
     */
    public List<LearningFeedbackDto> getFeedbacksByTarget(TargetType targetType, Long targetId) {
        List<LearningFeedback> feedbacks = feedbackRepository.findByTargetTypeAndTargetId(targetType, targetId);
        return feedbacks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * ユーザー別フィードバック取得（送信者）
     */
    public Page<LearningFeedbackDto> getFeedbacksByGiver(UUID userId, int page, int size, String sortBy) {
        Pageable pageable = createPageable(page, size, sortBy);
        Page<LearningFeedback> feedbacks = feedbackRepository.findByFeedbackGiverId(userId, pageable);
        return feedbacks.map(this::convertToDto);
    }

    /**
     * ユーザー別フィードバック取得（受信者）
     */
    public Page<LearningFeedbackDto> getFeedbacksByReceiver(UUID userId, int page, int size, String sortBy) {
        Pageable pageable = createPageable(page, size, sortBy);
        Page<LearningFeedback> feedbacks = feedbackRepository.findByFeedbackReceiverId(userId, pageable);
        return feedbacks.map(this::convertToDto);
    }

    /**
     * 複合条件検索
     */
    public Page<LearningFeedbackDto> searchFeedbacks(
            TargetType targetType, FeedbackType feedbackType, FeedbackCategory feedbackCategory,
            FeedbackStatus feedbackStatus, Integer minRating, Integer maxRating,
            int page, int size, String sortBy) {

        Pageable pageable = createPageable(page, size, sortBy);
        Page<LearningFeedback> feedbacks = feedbackRepository.searchFeedbacks(
                targetType, feedbackType, feedbackCategory, feedbackStatus,
                minRating, maxRating, pageable);
        return feedbacks.map(this::convertToDto);
    }

    /**
     * キーワード検索
     */
    public Page<LearningFeedbackDto> searchByKeyword(String keyword, int page, int size, String sortBy) {
        Pageable pageable = createPageable(page, size, sortBy);
        Page<LearningFeedback> feedbacks = feedbackRepository.searchByKeyword(keyword, pageable);
        return feedbacks.map(this::convertToDto);
    }

    /**
     * 期間別検索
     */
    public Page<LearningFeedbackDto> getFeedbacksByDateRange(
            LocalDateTime startDate, LocalDateTime endDate, int page, int size, String sortBy) {
        Pageable pageable = createPageable(page, size, sortBy);
        Page<LearningFeedback> feedbacks = feedbackRepository.findByCreatedAtBetween(startDate, endDate, pageable);
        return feedbacks.map(this::convertToDto);
    }

    // ========== フィードバック管理操作 ==========

    /**
     * フィードバック状態更新
     */
    @Transactional
    public void updateFeedbackStatus(Long feedbackId, FeedbackStatus status) {
        try {
            feedbackRepository.updateFeedbackStatus(feedbackId, status);
            log.info("フィードバック状態が更新されました: ID={}, Status={}", feedbackId, status);
        } catch (Exception e) {
            log.error("フィードバック状態更新中にエラーが発生しました: ID={}, Error={}", feedbackId, e.getMessage(), e);
            throw new RuntimeException("フィードバック状態の更新に失敗しました", e);
        }
    }

    /**
     * フィードバック承認
     */
    @Transactional
    public void approveFeedback(Long feedbackId, UUID moderatorId, String moderationNotes) {
        try {
            feedbackRepository.updateModerationStatus(feedbackId, ModerationStatus.APPROVED, moderatorId,
                    moderationNotes);
            feedbackRepository.updateFeedbackStatus(feedbackId, FeedbackStatus.ACKNOWLEDGED);
            log.info("フィードバックが承認されました: ID={}, Moderator={}", feedbackId, moderatorId);
        } catch (Exception e) {
            log.error("フィードバック承認中にエラーが発生しました: ID={}, Error={}", feedbackId, e.getMessage(), e);
            throw new RuntimeException("フィードバックの承認に失敗しました", e);
        }
    }

    /**
     * フィードバック拒否
     */
    @Transactional
    public void rejectFeedback(Long feedbackId, UUID moderatorId, String moderationNotes) {
        try {
            feedbackRepository.updateModerationStatus(feedbackId, ModerationStatus.REJECTED, moderatorId,
                    moderationNotes);
            feedbackRepository.updateFeedbackStatus(feedbackId, FeedbackStatus.REJECTED);
            log.info("フィードバックが拒否されました: ID={}, Moderator={}", feedbackId, moderatorId);
        } catch (Exception e) {
            log.error("フィードバック拒否中にエラーが発生しました: ID={}, Error={}", feedbackId, e.getMessage(), e);
            throw new RuntimeException("フィードバックの拒否に失敗しました", e);
        }
    }

    /**
     * 有用フラグ追加
     */
    @Transactional
    public void markAsHelpful(Long feedbackId) {
        try {
            feedbackRepository.incrementHelpfulCount(feedbackId);
            log.info("フィードバックに有用フラグが追加されました: ID={}", feedbackId);
        } catch (Exception e) {
            log.error("有用フラグ追加中にエラーが発生しました: ID={}, Error={}", feedbackId, e.getMessage(), e);
            throw new RuntimeException("有用フラグの追加に失敗しました", e);
        }
    }

    /**
     * 有用でないフラグ追加
     */
    @Transactional
    public void markAsNotHelpful(Long feedbackId) {
        try {
            feedbackRepository.incrementNotHelpfulCount(feedbackId);
            log.info("フィードバックに有用でないフラグが追加されました: ID={}", feedbackId);
        } catch (Exception e) {
            log.error("有用でないフラグ追加中にエラーが発生しました: ID={}, Error={}", feedbackId, e.getMessage(), e);
            throw new RuntimeException("有用でないフラグの追加に失敗しました", e);
        }
    }

    /**
     * 報告追加
     */
    @Transactional
    public void reportFeedback(Long feedbackId) {
        try {
            feedbackRepository.incrementReportCount(feedbackId);

            // 報告数が閾値を超えた場合はモデレーション必要フラグを設定
            LearningFeedback feedback = feedbackRepository.findById(feedbackId)
                    .orElseThrow(() -> new RuntimeException("フィードバックが見つかりません: " + feedbackId));

            if (feedback.getReportCount() >= 5) {
                feedback.setRequiresModeration(true);
                feedbackRepository.save(feedback);
            }

            log.info("フィードバックが報告されました: ID={}", feedbackId);
        } catch (Exception e) {
            log.error("フィードバック報告中にエラーが発生しました: ID={}, Error={}", feedbackId, e.getMessage(), e);
            throw new RuntimeException("フィードバックの報告に失敗しました", e);
        }
    }

    // ========== 統計・分析操作 ==========

    /**
     * 対象別フィードバック統計
     */
    public Long getFeedbackCount(TargetType targetType, Long targetId) {
        return feedbackRepository.countByTargetTypeAndTargetId(targetType, targetId);
    }

    /**
     * 平均評価計算
     */
    public BigDecimal calculateAverageRating(TargetType targetType, Long targetId) {
        BigDecimal average = feedbackRepository.calculateAverageRating(targetType, targetId);
        return average != null ? average : BigDecimal.ZERO;
    }

    /**
     * 評価分布取得
     */
    public List<Object[]> getRatingDistribution(TargetType targetType, Long targetId) {
        return feedbackRepository.getRatingDistribution(targetType, targetId);
    }

    /**
     * フィードバックタイプ別統計
     */
    public List<Object[]> getFeedbackTypeStatistics() {
        return feedbackRepository.countByFeedbackType();
    }

    /**
     * フィードバックカテゴリ別統計
     */
    public List<Object[]> getFeedbackCategoryStatistics() {
        return feedbackRepository.countByFeedbackCategory();
    }

    /**
     * 月別統計
     */
    public List<Object[]> getMonthlyStatistics(LocalDateTime fromDate) {
        return feedbackRepository.countByMonth(fromDate);
    }

    /**
     * 高品質フィードバック取得
     */
    public List<LearningFeedbackDto> getHighQualityFeedbacks(BigDecimal minScore) {
        List<LearningFeedback> feedbacks = feedbackRepository.findHighQualityFeedbacks(minScore);
        return feedbacks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 上位評価フィードバック取得
     */
    public List<LearningFeedbackDto> getTopRatedFeedbacks(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<LearningFeedback> feedbacks = feedbackRepository.findTopRatedFeedbacks(pageable);
        return feedbacks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ========== モデレーション操作 ==========

    /**
     * モデレーション必要フィードバック取得
     */
    public List<LearningFeedbackDto> getFeedbacksRequiringModeration() {
        List<LearningFeedback> feedbacks = feedbackRepository.findFeedbacksRequiringModeration();
        return feedbacks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 報告されたフィードバック取得
     */
    public List<LearningFeedbackDto> getReportedFeedbacks(int threshold) {
        List<LearningFeedback> feedbacks = feedbackRepository.findReportedFeedbacks(threshold);
        return feedbacks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ========== フォローアップ操作 ==========

    /**
     * フォローアップ完了
     */
    @Transactional
    public void completeFollowup(Long feedbackId, String followupNotes) {
        try {
            feedbackRepository.markFollowupCompleted(feedbackId, followupNotes);
            log.info("フォローアップが完了されました: ID={}", feedbackId);
        } catch (Exception e) {
            log.error("フォローアップ完了中にエラーが発生しました: ID={}, Error={}", feedbackId, e.getMessage(), e);
            throw new RuntimeException("フォローアップの完了に失敗しました", e);
        }
    }

    /**
     * フォローアップ必要フィードバック取得
     */
    public List<LearningFeedbackDto> getFeedbacksRequiringFollowup() {
        List<LearningFeedback> feedbacks = feedbackRepository.findFeedbacksRequiringFollowup();
        return feedbacks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 未回答フィードバック取得
     */
    public List<LearningFeedbackDto> getUnansweredFeedbacks() {
        List<LearningFeedback> feedbacks = feedbackRepository.findUnansweredFeedbacks();
        return feedbacks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ========== バッチ処理操作 ==========

    /**
     * スコア一括再計算
     */
    @Transactional
    public void recalculateScores() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(1);
            List<LearningFeedback> feedbacks = feedbackRepository.findFeedbacksForScoreRecalculation(cutoffDate);

            int updatedCount = 0;
            for (LearningFeedback feedback : feedbacks) {
                calculateAndSetScores(feedback);
                feedbackRepository.save(feedback);
                updatedCount++;
            }

            log.info("フィードバックスコアが一括再計算されました: 更新数={}", updatedCount);
        } catch (Exception e) {
            log.error("スコア一括再計算中にエラーが発生しました: {}", e.getMessage(), e);
            throw new RuntimeException("スコアの一括再計算に失敗しました", e);
        }
    }

    /**
     * 自動クローズ処理
     */
    @Transactional
    public void autoCloseFeedbacks() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
            List<LearningFeedback> feedbacks = feedbackRepository.findFeedbacksForAutoClose(cutoffDate);

            int closedCount = 0;
            for (LearningFeedback feedback : feedbacks) {
                feedback.setFeedbackStatus(FeedbackStatus.CLOSED);
                feedback.setUpdatedAt(LocalDateTime.now());
                feedbackRepository.save(feedback);
                closedCount++;
            }

            log.info("フィードバックが自動クローズされました: クローズ数={}", closedCount);
        } catch (Exception e) {
            log.error("フィードバック自動クローズ中にエラーが発生しました: {}", e.getMessage(), e);
            throw new RuntimeException("フィードバックの自動クローズに失敗しました", e);
        }
    }

    // ========== ヘルパーメソッド ==========

    /**
     * DTOからEntityへの変換
     */
    private LearningFeedback convertToEntity(LearningFeedbackDto dto) {
        LearningFeedback feedback = new LearningFeedback();

        // 基本情報
        feedback.setTargetType(dto.getTargetType());
        feedback.setTargetId(Long.valueOf(dto.getTargetId()));
        feedback.setFeedbackGiverId(UUID.fromString(dto.getFeedbackUserId()));
        feedback.setFeedbackType(dto.getFeedbackType());
        feedback.setFeedbackCategory(dto.getFeedbackCategory());
        feedback.setTitle(dto.getContent() != null && dto.getContent().length() > 200
                ? dto.getContent().substring(0, 200)
                : dto.getContent());
        feedback.setContent(dto.getContent());
        feedback.setRating(dto.getRating());
        feedback.setFeedbackStatus(dto.getStatus() != null ? dto.getStatus() : FeedbackStatus.SUBMITTED);

        // フラグ設定
        feedback.setIsAnonymous(dto.getIsAnonymous() != null ? dto.getIsAnonymous() : false);
        feedback.setIsPublic(true);
        feedback.setIsHelpful(false);
        feedback.setIsConstructive(dto.getFeedbackType() == FeedbackType.CONSTRUCTIVE);
        feedback.setIsActionable(dto.getFeedbackType() == FeedbackType.SUGGESTION ||
                dto.getFeedbackType() == FeedbackType.IMPROVEMENT);

        // カウント初期化
        feedback.setHelpfulCount(0);
        feedback.setNotHelpfulCount(0);
        feedback.setReportCount(0);
        feedback.setRequiresModeration(false);

        // スコア初期化
        feedback.setQualityScore(BigDecimal.ZERO);
        feedback.setHelpfulnessScore(BigDecimal.ZERO);
        feedback.setConstructivenessScore(BigDecimal.ZERO);
        feedback.setOverallScore(BigDecimal.ZERO);

        // その他
        feedback.setFollowupRequired(dto.getWantsContact() != null ? dto.getWantsContact() : false);
        feedback.setFollowupCompleted(false);
        feedback.setTags(dto.getSuggestion() != null ? "suggestion" : "feedback");

        return feedback;
    }

    /**
     * EntityからDTOへの変換
     */
    private LearningFeedbackDto convertToDto(LearningFeedback feedback) {
        LearningFeedbackDto dto = new LearningFeedbackDto();

        // 基本情報
        dto.setFeedbackId(feedback.getId().toString());
        dto.setTargetType(feedback.getTargetType());
        dto.setTargetId(feedback.getTargetId().toString());
        dto.setFeedbackUserId(feedback.getFeedbackGiverId().toString());
        dto.setFeedbackType(feedback.getFeedbackType());
        dto.setFeedbackCategory(feedback.getFeedbackCategory());
        dto.setContent(feedback.getContent());
        dto.setRating(feedback.getRating());
        dto.setStatus(feedback.getFeedbackStatus());

        // フラグ
        dto.setIsAnonymous(feedback.getIsAnonymous());

        // スコア
        dto.setQualityScore(feedback.getQualityScore());
        dto.setUsefulnessScore(feedback.getHelpfulnessScore());
        dto.setConstructivenessScore(feedback.getConstructivenessScore());
        dto.setOverallScore(feedback.getOverallScore());

        // カウント
        dto.setHelpfulCount(feedback.getHelpfulCount());
        dto.setReportCount(feedback.getReportCount());

        // 日時
        dto.setCreatedAt(feedback.getCreatedAt());
        dto.setUpdatedAt(feedback.getUpdatedAt());
        dto.setResolvedAt(feedback.getResponseGivenAt());

        // DTO初期化
        dto.initialize();

        return dto;
    }

    /**
     * フィードバックフィールド更新
     */
    private void updateFeedbackFields(LearningFeedback feedback, LearningFeedbackDto dto) {
        if (dto.getContent() != null) {
            feedback.setContent(dto.getContent());
            feedback.setTitle(dto.getContent().length() > 200
                    ? dto.getContent().substring(0, 200)
                    : dto.getContent());
        }
        if (dto.getRating() != null) {
            feedback.setRating(dto.getRating());
        }
        if (dto.getStatus() != null) {
            feedback.setFeedbackStatus(dto.getStatus());
        }
        if (dto.getIsAnonymous() != null) {
            feedback.setIsAnonymous(dto.getIsAnonymous());
        }
    }

    /**
     * スコア計算・設定
     */
    private void calculateAndSetScores(LearningFeedback feedback) {
        feedback.setQualityScore(feedback.calculateQualityScore());
        feedback.setHelpfulnessScore(feedback.calculateHelpfulnessScore());
        feedback.setConstructivenessScore(feedback.calculateConstructivenessScore());
        feedback.setOverallScore(feedback.calculateOverallScore());
    }

    /**
     * Pageable作成
     */
    private Pageable createPageable(int page, int size, String sortBy) {
        Sort sort = Sort.by(Sort.Direction.DESC, sortBy != null ? sortBy : "createdAt");
        return PageRequest.of(page, size, sort);
    }
}
