package com.sfr.tokyo.sfr_backend.service.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningComment;
import com.sfr.tokyo.sfr_backend.dto.learning.LearningCommentDto;
import com.sfr.tokyo.sfr_backend.repository.learning.LearningCommentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * LearningComment Service
 * 学習コメントビジネスロジック層
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LearningCommentService {

    private final LearningCommentRepository commentRepository;

    // ========== CRUD操作 ==========

    /**
     * 新規コメント作成
     */
    @Transactional
    public LearningCommentDto createComment(LearningCommentDto dto) {
        log.info("コメント作成開始: topicId={}, authorId={}", dto.getTopicId(), dto.getAuthorId());

        LearningComment entity = convertToEntity(dto);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        LearningComment saved = commentRepository.save(entity);
        log.info("コメント作成完了: id={}, topicId={}", saved.getId(), saved.getTopicId());

        return convertToDto(saved);
    }

    /**
     * コメント更新
     */
    @Transactional
    public LearningCommentDto updateComment(Long commentId, LearningCommentDto dto) {
        log.info("コメント更新開始: commentId={}", commentId);

        LearningComment entity = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("コメントが見つかりません: " + commentId));

        updateEntityFromDto(entity, dto);
        entity.setUpdatedAt(LocalDateTime.now());

        LearningComment saved = commentRepository.save(entity);
        log.info("コメント更新完了: id={}", saved.getId());

        return convertToDto(saved);
    }

    /**
     * コメント削除
     */
    @Transactional
    public void deleteComment(Long commentId) {
        log.info("コメント削除開始: commentId={}", commentId);

        // 子コメント（返信）も一緒に削除
        commentRepository.softDeleteRepliesByParentId(commentId, LocalDateTime.now());

        int updated = commentRepository.softDelete(commentId, LocalDateTime.now());
        if (updated == 0) {
            throw new EntityNotFoundException("コメントが見つかりません: " + commentId);
        }

        log.info("コメント削除完了: commentId={}", commentId);
    }

    /**
     * コメント復旧
     */
    @Transactional
    public void restoreComment(Long commentId) {
        log.info("コメント復旧開始: commentId={}", commentId);

        int updated = commentRepository.restore(commentId, LocalDateTime.now());
        if (updated == 0) {
            throw new EntityNotFoundException("コメントが見つかりません: " + commentId);
        }

        log.info("コメント復旧完了: commentId={}", commentId);
    }

    /**
     * ID別コメント取得
     */
    public LearningCommentDto getCommentById(Long commentId) {
        LearningComment entity = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("コメントが見つかりません: " + commentId));

        return convertToDto(entity);
    }

    // ========== 検索機能 ==========

    /**
     * トピック別コメント一覧取得
     */
    public List<LearningCommentDto> getCommentsByTopicId(Long topicId) {
        return commentRepository.findByTopicIdOrderByCreatedAt(topicId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * トピック別コメント一覧取得（ページング）
     */
    public Page<LearningCommentDto> getCommentsByTopicId(Long topicId, Pageable pageable) {
        return commentRepository.findByTopicId(topicId, pageable)
                .map(this::convertToDto);
    }

    /**
     * 親コメント別返信一覧取得
     */
    public List<LearningCommentDto> getRepliesByParentId(Long parentId) {
        return commentRepository.findRepliesByParentId(parentId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * ユーザー別コメント一覧取得
     */
    public Page<LearningCommentDto> getCommentsByAuthorId(UUID authorId, Pageable pageable) {
        return commentRepository.findByAuthorId(authorId, pageable)
                .map(this::convertToDto);
    }

    /**
     * ベストアンサーコメント取得
     */
    public Optional<LearningCommentDto> getBestAnswerByTopicId(Long topicId) {
        return commentRepository.findBestAnswerByTopicId(topicId)
                .map(this::convertToDto);
    }

    /**
     * ソリューションコメント一覧取得
     */
    public List<LearningCommentDto> getSolutionsByTopicId(Long topicId) {
        return commentRepository.findSolutionsByTopicId(topicId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * ピン留めコメント一覧取得
     */
    public List<LearningCommentDto> getPinnedByTopicId(Long topicId) {
        return commentRepository.findPinnedByTopicId(topicId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * トップレベルコメント取得
     */
    public List<LearningCommentDto> getTopLevelCommentsByTopicId(Long topicId) {
        return commentRepository.findTopLevelByTopicId(topicId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 高品質コメント取得
     */
    public List<LearningCommentDto> getHighQualityComments(Long topicId, BigDecimal minScore) {
        return commentRepository.findHighQualityByTopicId(topicId, minScore).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 人気コメント取得
     */
    public List<LearningCommentDto> getPopularComments(Long topicId, Integer minLikes) {
        return commentRepository.findPopularByTopicId(topicId, minLikes).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ========== ステータス管理 ==========

    /**
     * ステータス別コメント検索
     */
    public Page<LearningCommentDto> getCommentsByStatus(String status, Pageable pageable) {
        return commentRepository.findByStatus(status, pageable)
                .map(this::convertToDto);
    }

    /**
     * モデレーション待ちコメント取得
     */
    public Page<LearningCommentDto> getPendingModerationComments(Pageable pageable) {
        return commentRepository.findPendingModeration(pageable)
                .map(this::convertToDto);
    }

    /**
     * 承認済みコメント取得
     */
    public List<LearningCommentDto> getApprovedCommentsByTopicId(Long topicId) {
        return commentRepository.findApprovedByTopicId(topicId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ========== アクション機能 ==========

    /**
     * いいね追加/削除
     */
    @Transactional
    public void toggleLike(Long commentId, boolean isLike) {
        log.info("いいね切り替え開始: commentId={}, isLike={}", commentId, isLike);

        int increment = isLike ? 1 : -1;
        int updated = commentRepository.updateLikeCount(commentId, increment, LocalDateTime.now());
        if (updated == 0) {
            throw new EntityNotFoundException("コメントが見つかりません: " + commentId);
        }

        log.info("いいね切り替え完了: commentId={}", commentId);
    }

    /**
     * ベストアンサー設定
     */
    @Transactional
    public void setBestAnswer(Long commentId, boolean isBestAnswer) {
        log.info("ベストアンサー設定開始: commentId={}, isBestAnswer={}", commentId, isBestAnswer);

        int updated = commentRepository.updateBestAnswer(commentId, isBestAnswer, LocalDateTime.now());
        if (updated == 0) {
            throw new EntityNotFoundException("コメントが見つかりません: " + commentId);
        }

        log.info("ベストアンサー設定完了: commentId={}", commentId);
    }

    /**
     * ピン留め設定
     */
    @Transactional
    public void setPinned(Long commentId, boolean isPinned) {
        log.info("ピン留め設定開始: commentId={}, isPinned={}", commentId, isPinned);

        int updated = commentRepository.updatePinned(commentId, isPinned, LocalDateTime.now());
        if (updated == 0) {
            throw new EntityNotFoundException("コメントが見つかりません: " + commentId);
        }

        log.info("ピン留め設定完了: commentId={}", commentId);
    }

    /**
     * ソリューション設定
     */
    @Transactional
    public void setSolution(Long commentId, boolean isSolution) {
        log.info("ソリューション設定開始: commentId={}, isSolution={}", commentId, isSolution);

        int updated = commentRepository.updateSolution(commentId, isSolution, LocalDateTime.now());
        if (updated == 0) {
            throw new EntityNotFoundException("コメントが見つかりません: " + commentId);
        }

        log.info("ソリューション設定完了: commentId={}", commentId);
    }

    /**
     * 品質スコア更新
     */
    @Transactional
    public void updateQualityScore(Long commentId, BigDecimal score) {
        log.info("品質スコア更新開始: commentId={}, score={}", commentId, score);

        int updated = commentRepository.updateQualityScore(commentId, score, LocalDateTime.now());
        if (updated == 0) {
            throw new EntityNotFoundException("コメントが見つかりません: " + commentId);
        }

        log.info("品質スコア更新完了: commentId={}", commentId);
    }

    // ========== モデレーション機能 ==========

    /**
     * モデレーションステータス更新
     */
    @Transactional
    public void updateModerationStatus(Long commentId, String status, UUID moderatorId) {
        log.info("モデレーションステータス更新開始: commentId={}, status={}, moderatorId={}", commentId, status, moderatorId);

        int updated = commentRepository.updateModerationStatus(commentId, status, moderatorId, LocalDateTime.now());
        if (updated == 0) {
            throw new EntityNotFoundException("コメントが見つかりません: " + commentId);
        }

        log.info("モデレーションステータス更新完了: commentId={}", commentId);
    }

    /**
     * ステータス更新
     */
    @Transactional
    public void updateStatus(Long commentId, String status) {
        log.info("ステータス更新開始: commentId={}, status={}", commentId, status);

        int updated = commentRepository.updateStatus(commentId, status, LocalDateTime.now());
        if (updated == 0) {
            throw new EntityNotFoundException("コメントが見つかりません: " + commentId);
        }

        log.info("ステータス更新完了: commentId={}", commentId);
    }

    // ========== 検索機能 ==========

    /**
     * コンテンツ検索
     */
    public Page<LearningCommentDto> searchByContent(Long topicId, String keyword, Pageable pageable) {
        return commentRepository.searchByContent(topicId, keyword, pageable)
                .map(this::convertToDto);
    }

    /**
     * 複合検索
     */
    public Page<LearningCommentDto> searchComments(Long topicId, UUID authorId, String status, String keyword,
            Pageable pageable) {
        return commentRepository.searchComments(topicId, authorId, status, keyword, pageable)
                .map(this::convertToDto);
    }

    // ========== 統計・分析 ==========

    /**
     * トピック別コメント数取得
     */
    public Long getCommentCountByTopicId(Long topicId) {
        return commentRepository.countByTopicId(topicId);
    }

    /**
     * ユーザー別コメント数取得
     */
    public Long getCommentCountByAuthorId(UUID authorId) {
        return commentRepository.countByAuthorId(authorId);
    }

    /**
     * 期間別コメント数統計
     */
    public List<Map<String, Object>> getCommentCountByDateRange(LocalDateTime fromDate, LocalDateTime toDate) {
        List<Object[]> results = commentRepository.getCommentCountByDateRange(fromDate, toDate);

        return results.stream()
                .map(result -> {
                    Map<String, Object> stats = new HashMap<>();
                    stats.put("date", result[0]);
                    stats.put("count", result[1]);
                    return stats;
                })
                .collect(Collectors.toList());
    }

    /**
     * トピック別返信数統計
     */
    public Map<Long, Long> getReplyCountsByTopicId(Long topicId) {
        List<Object[]> results = commentRepository.getReplyCountsByTopicId(topicId);

        return results.stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0],
                        result -> (Long) result[1]));
    }

    // ========== 高度な検索 ==========

    /**
     * 関連コメント検索
     */
    public List<LearningCommentDto> getRelatedCommentsByAuthor(UUID authorId, Long excludeTopicId, Pageable pageable) {
        return commentRepository.findRelatedCommentsByAuthor(authorId, excludeTopicId, pageable).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 人気コメント（複合スコア順）
     */
    public List<LearningCommentDto> getPopularCommentsByScore(Long topicId, Pageable pageable) {
        return commentRepository.findPopularCommentsByScore(topicId, pageable).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * アクティブな討論
     */
    public List<LearningCommentDto> getActiveDiscussions(Long topicId, LocalDateTime sinceDate) {
        return commentRepository.findActiveDiscussions(topicId, sinceDate).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 解決済みコメント
     */
    public List<LearningCommentDto> getSolvedComments(Long topicId) {
        return commentRepository.findSolvedComments(topicId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 未解決コメント
     */
    public List<LearningCommentDto> getUnresolvedComments(Long topicId, Integer maxReplies) {
        return commentRepository.findUnresolvedComments(topicId, maxReplies).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 最近のアクティビティ
     */
    public List<LearningCommentDto> getRecentActivity(Long topicId, LocalDateTime sinceDate) {
        return commentRepository.findRecentActivity(topicId, sinceDate).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ========== バッチ処理 ==========

    /**
     * トピック削除に伴うコメント一括削除
     */
    @Transactional
    public void deleteCommentsByTopicId(Long topicId) {
        log.info("トピック別コメント一括削除開始: topicId={}", topicId);

        int updated = commentRepository.softDeleteByTopicId(topicId, LocalDateTime.now());

        log.info("トピック別コメント一括削除完了: topicId={}, deleted={}", topicId, updated);
    }

    /**
     * 古いコメントのアーカイブ
     */
    @Transactional
    public void archiveOldComments(LocalDateTime cutoffDate) {
        log.info("古いコメントのアーカイブ開始: cutoffDate={}", cutoffDate);

        int updated = commentRepository.archiveOldComments(cutoffDate, LocalDateTime.now());

        log.info("古いコメントのアーカイブ完了: archived={}", updated);
    }

    // ========== ヘルパーメソッド ==========

    /**
     * DTOからEntityへの変換
     */
    private LearningComment convertToEntity(LearningCommentDto dto) {
        LearningComment entity = new LearningComment();
        updateEntityFromDto(entity, dto);
        return entity;
    }

    /**
     * DTOからEntityへのデータ移行
     */
    private void updateEntityFromDto(LearningComment entity, LearningCommentDto dto) {
        entity.setTopicId(dto.getTopicId());
        entity.setAuthorId(dto.getAuthorId());
        entity.setParentCommentId(dto.getParentCommentId());
        // 他のフィールドも必要に応じて設定
        // 実際のDTOの構造に合わせて調整が必要
    }

    /**
     * EntityからDTOへの変換
     */
    private LearningCommentDto convertToDto(LearningComment entity) {
        LearningCommentDto dto = new LearningCommentDto();
        dto.setTopicId(entity.getTopicId());
        dto.setAuthorId(entity.getAuthorId());
        dto.setParentCommentId(entity.getParentCommentId());
        // 他のフィールドも必要に応じて設定
        // 実際のDTOの構造に合わせて調整が必要
        return dto;
    }

    // ========== Controller対応の追加メソッド ==========

    /**
     * トピック別コメント一覧（順序指定）
     */
    @Transactional(readOnly = true)
    public List<LearningCommentDto> getOrderedCommentsByTopicId(Long topicId, String sortBy) {
        List<LearningComment> comments;
        switch (sortBy.toLowerCase()) {
            case "createdat":
                comments = commentRepository.findByTopicIdOrderByCreatedAt(topicId);
                break;
            case "likes":
                comments = commentRepository.findPopularByTopicIdWithPageable(topicId,
                        org.springframework.data.domain.PageRequest.of(0, 100));
                break;
            case "quality":
                comments = commentRepository.findHighQualityByTopicIdDouble(topicId, 3.0);
                break;
            default:
                comments = commentRepository.findByTopicIdOrderByCreatedAt(topicId);
        }
        return comments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * ユーザー別コメント一覧（Page版）
     */
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<LearningCommentDto> getCommentsByUserId(UUID userId,
            org.springframework.data.domain.Pageable pageable) {
        return commentRepository.findByAuthorIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::convertToDto);
    }

    /**
     * 高品質コメント取得
     */
    @Transactional(readOnly = true)
    public List<LearningCommentDto> getHighQualityCommentsByTopicId(Long topicId, double minScore) {
        return commentRepository.findHighQualityByTopicIdDouble(topicId, minScore).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 人気コメント取得
     */
    @Transactional(readOnly = true)
    public List<LearningCommentDto> getPopularCommentsByTopicId(Long topicId, int limit) {
        return commentRepository
                .findPopularByTopicIdWithPageable(topicId, org.springframework.data.domain.PageRequest.of(0, limit))
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * ピン留めコメント取得
     */
    @Transactional(readOnly = true)
    public List<LearningCommentDto> getPinnedCommentsByTopicId(Long topicId) {
        return commentRepository.findPinnedByTopicId(topicId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 承認処理
     */
    @Transactional
    public void approveComment(Long commentId, UUID moderatorId) {
        updateModerationStatus(commentId, "APPROVED", moderatorId);
    }

    /**
     * 拒否処理
     */
    @Transactional
    public void rejectComment(Long commentId, UUID moderatorId, String reason) {
        updateModerationStatus(commentId, "REJECTED", moderatorId);
        if (reason != null) {
            commentRepository.updateModerationReason(commentId, reason);
        }
    }

    /**
     * モデレーションステータス更新（理由付き）
     */
    @Transactional
    public void updateModerationStatus(Long commentId, String status, UUID moderatorId, String reason) {
        updateModerationStatus(commentId, status, moderatorId);
        if (reason != null) {
            commentRepository.updateModerationReason(commentId, reason);
        }
    }

    /**
     * 検索（引数順序調整）
     */
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<LearningCommentDto> searchComments(
            Long topicId, String keyword, String status, UUID authorId,
            org.springframework.data.domain.Pageable pageable) {
        return searchComments(topicId, authorId, keyword, status, pageable);
    }

    /**
     * ユーザー別コメント数取得
     */
    @Transactional(readOnly = true)
    public Long getCommentCountByUserId(UUID userId) {
        return commentRepository.countByAuthorId(userId);
    }

    /**
     * 日次統計
     */
    @Transactional(readOnly = true)
    public List<java.util.Map<String, Object>> getDailyCommentStatistics(LocalDateTime fromDate, LocalDateTime toDate) {
        return getCommentCountByDateRange(fromDate, toDate);
    }

    /**
     * ユーザー別統計
     */
    @Transactional(readOnly = true)
    public List<java.util.Map<String, Object>> getUserCommentStatistics() {
        List<Object[]> results = commentRepository.getUserCommentStatistics();
        return results.stream()
                .map(result -> {
                    java.util.Map<String, Object> stats = new java.util.HashMap<>();
                    stats.put("authorId", result[0]);
                    stats.put("commentCount", result[1]);
                    stats.put("avgQuality", result[2]);
                    stats.put("totalLikes", result[3]);
                    return stats;
                })
                .collect(Collectors.toList());
    }

    /**
     * 品質統計
     */
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getCommentQualityStatistics() {
        Object[] result = commentRepository.getCommentQualityStatistics();
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("avgQuality", result[0]);
        stats.put("highQuality", result[1]);
        stats.put("lowQuality", result[2]);
        stats.put("totalComments", result[3]);
        return stats;
    }

    /**
     * 最近のアクティビティ取得（引数順序調整）
     */
    @Transactional(readOnly = true)
    public List<LearningCommentDto> getRecentActivity(LocalDateTime since, int limit) {
        return getRecentActivity(Long.valueOf(limit), since);
    }

    /**
     * ユーザーアクティビティ取得
     */
    @Transactional(readOnly = true)
    public List<LearningCommentDto> getUserActivity(UUID userId, LocalDateTime since, int limit) {
        return commentRepository
                .findUserActivity(userId, since, org.springframework.data.domain.PageRequest.of(0, limit))
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 通知対象コメント取得
     */
    @Transactional(readOnly = true)
    public List<LearningCommentDto> getNotificationComments(UUID userId, LocalDateTime since) {
        return commentRepository.findNotificationComments(userId, since)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 孤立コメントのクリーンアップ
     */
    @Transactional
    public void cleanupOrphanedComments() {
        commentRepository.cleanupOrphanedComments();
        log.info("孤立コメントをクリーンアップしました");
    }

    /**
     * 品質スコア再計算
     */
    @Transactional
    public void recalculateQualityScores() {
        commentRepository.recalculateQualityScores();
        log.info("品質スコアを再計算しました");
    }
}
