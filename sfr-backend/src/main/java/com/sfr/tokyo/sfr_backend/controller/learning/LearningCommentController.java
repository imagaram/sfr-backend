package com.sfr.tokyo.sfr_backend.controller.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningCommentDto;
import com.sfr.tokyo.sfr_backend.service.learning.LearningCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * LearningComment Controller
 * 学習コメント管理REST API
 */
@RestController
@RequestMapping("/api/learning/comments")
@RequiredArgsConstructor
@Slf4j
public class LearningCommentController {

    private final LearningCommentService commentService;

    // ========== 基本CRUD操作 ==========

    /**
     * コメント作成
     */
    @PostMapping
    public ResponseEntity<LearningCommentDto> createComment(@Valid @RequestBody LearningCommentDto commentDto) {
        log.info("コメント作成API呼び出し: topicId={}, content={}",
                commentDto.getTopicId(),
                commentDto.getContent() != null
                        ? commentDto.getContent().substring(0, Math.min(50, commentDto.getContent().length())) + "..."
                        : "null");

        LearningCommentDto created = commentService.createComment(commentDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * コメント更新
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<LearningCommentDto> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody LearningCommentDto commentDto) {
        log.info("コメント更新API呼び出し: commentId={}", commentId);

        LearningCommentDto updated = commentService.updateComment(commentId, commentDto);
        return ResponseEntity.ok(updated);
    }

    /**
     * コメント削除
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        log.info("コメント削除API呼び出し: commentId={}", commentId);

        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * コメント取得
     */
    @GetMapping("/{commentId}")
    public ResponseEntity<LearningCommentDto> getComment(@PathVariable Long commentId) {
        LearningCommentDto comment = commentService.getCommentById(commentId);
        return ResponseEntity.ok(comment);
    }

    // ========== 検索機能 ==========

    /**
     * トピック別コメント一覧取得
     */
    @GetMapping("/topic/{topicId}")
    public ResponseEntity<Page<LearningCommentDto>> getCommentsByTopicId(
            @PathVariable Long topicId,
            Pageable pageable) {
        Page<LearningCommentDto> comments = commentService.getCommentsByTopicId(topicId, pageable);
        return ResponseEntity.ok(comments);
    }

    /**
     * トピック別コメント一覧取得（順序指定）
     */
    @GetMapping("/topic/{topicId}/ordered")
    public ResponseEntity<List<LearningCommentDto>> getOrderedCommentsByTopicId(
            @PathVariable Long topicId,
            @RequestParam(defaultValue = "createdAt") String sortBy) {
        List<LearningCommentDto> comments = commentService.getOrderedCommentsByTopicId(topicId, sortBy);
        return ResponseEntity.ok(comments);
    }

    /**
     * 親コメントの返信取得
     */
    @GetMapping("/parent/{parentId}/replies")
    public ResponseEntity<List<LearningCommentDto>> getRepliesByParentId(@PathVariable Long parentId) {
        List<LearningCommentDto> replies = commentService.getRepliesByParentId(parentId);
        return ResponseEntity.ok(replies);
    }

    /**
     * ユーザー別コメント一覧取得
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<LearningCommentDto>> getCommentsByUserId(
            @PathVariable UUID userId,
            Pageable pageable) {
        Page<LearningCommentDto> comments = commentService.getCommentsByUserId(userId, pageable);
        return ResponseEntity.ok(comments);
    }

    /**
     * 承認済みコメント取得
     */
    @GetMapping("/topic/{topicId}/approved")
    public ResponseEntity<List<LearningCommentDto>> getApprovedCommentsByTopicId(@PathVariable Long topicId) {
        List<LearningCommentDto> comments = commentService.getApprovedCommentsByTopicId(topicId);
        return ResponseEntity.ok(comments);
    }

    /**
     * 高品質コメント取得
     */
    @GetMapping("/topic/{topicId}/high-quality")
    public ResponseEntity<List<LearningCommentDto>> getHighQualityCommentsByTopicId(
            @PathVariable Long topicId,
            @RequestParam(defaultValue = "3.0") double minScore) {
        List<LearningCommentDto> comments = commentService.getHighQualityCommentsByTopicId(topicId, minScore);
        return ResponseEntity.ok(comments);
    }

    /**
     * 人気コメント取得
     */
    @GetMapping("/topic/{topicId}/popular")
    public ResponseEntity<List<LearningCommentDto>> getPopularCommentsByTopicId(
            @PathVariable Long topicId,
            @RequestParam(defaultValue = "10") int limit) {
        List<LearningCommentDto> comments = commentService.getPopularCommentsByTopicId(topicId, limit);
        return ResponseEntity.ok(comments);
    }

    /**
     * ピン留めコメント取得
     */
    @GetMapping("/topic/{topicId}/pinned")
    public ResponseEntity<List<LearningCommentDto>> getPinnedCommentsByTopicId(@PathVariable Long topicId) {
        List<LearningCommentDto> comments = commentService.getPinnedCommentsByTopicId(topicId);
        return ResponseEntity.ok(comments);
    }

    /**
     * ベストアンサーコメント取得
     */
    @GetMapping("/topic/{topicId}/best-answer")
    public ResponseEntity<LearningCommentDto> getBestAnswerByTopicId(@PathVariable Long topicId) {
        return commentService.getBestAnswerByTopicId(topicId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ========== アクション機能 ==========

    /**
     * コメントに「いいね」
     */
    @PostMapping("/{commentId}/like")
    public ResponseEntity<Void> likeComment(@PathVariable Long commentId) {
        commentService.toggleLike(commentId, true);
        return ResponseEntity.ok().build();
    }

    /**
     * コメントの「いいね」解除
     */
    @DeleteMapping("/{commentId}/like")
    public ResponseEntity<Void> unlikeComment(@PathVariable Long commentId) {
        commentService.toggleLike(commentId, false);
        return ResponseEntity.ok().build();
    }

    /**
     * ベストアンサーに設定
     */
    @PostMapping("/{commentId}/best-answer")
    public ResponseEntity<Void> setBestAnswer(@PathVariable Long commentId) {
        commentService.setBestAnswer(commentId, true);
        return ResponseEntity.ok().build();
    }

    /**
     * ベストアンサー解除
     */
    @DeleteMapping("/{commentId}/best-answer")
    public ResponseEntity<Void> removeBestAnswer(@PathVariable Long commentId) {
        commentService.setBestAnswer(commentId, false);
        return ResponseEntity.ok().build();
    }

    /**
     * コメントをピン留め
     */
    @PostMapping("/{commentId}/pin")
    public ResponseEntity<Void> pinComment(@PathVariable Long commentId) {
        commentService.setPinned(commentId, true);
        return ResponseEntity.ok().build();
    }

    /**
     * コメントのピン留め解除
     */
    @DeleteMapping("/{commentId}/pin")
    public ResponseEntity<Void> unpinComment(@PathVariable Long commentId) {
        commentService.setPinned(commentId, false);
        return ResponseEntity.ok().build();
    }

    /**
     * ソリューションに設定
     */
    @PostMapping("/{commentId}/solution")
    public ResponseEntity<Void> setSolution(@PathVariable Long commentId) {
        commentService.setSolution(commentId, true);
        return ResponseEntity.ok().build();
    }

    /**
     * ソリューション解除
     */
    @DeleteMapping("/{commentId}/solution")
    public ResponseEntity<Void> removeSolution(@PathVariable Long commentId) {
        commentService.setSolution(commentId, false);
        return ResponseEntity.ok().build();
    }

    // ========== モデレーション機能 ==========

    /**
     * モデレーション待ちコメント取得
     */
    @GetMapping("/moderation/pending")
    public ResponseEntity<Page<LearningCommentDto>> getPendingModerationComments(Pageable pageable) {
        Page<LearningCommentDto> comments = commentService.getPendingModerationComments(pageable);
        return ResponseEntity.ok(comments);
    }

    /**
     * 承認
     */
    @PostMapping("/{commentId}/approve")
    public ResponseEntity<Void> approveComment(
            @PathVariable Long commentId,
            @RequestParam UUID moderatorId) {
        commentService.approveComment(commentId, moderatorId);
        return ResponseEntity.ok().build();
    }

    /**
     * 拒否
     */
    @PostMapping("/{commentId}/reject")
    public ResponseEntity<Void> rejectComment(
            @PathVariable Long commentId,
            @RequestParam UUID moderatorId,
            @RequestParam(required = false) String reason) {
        commentService.rejectComment(commentId, moderatorId, reason);
        return ResponseEntity.ok().build();
    }

    /**
     * モデレーションステータス更新
     */
    @PutMapping("/{commentId}/moderation")
    public ResponseEntity<Void> updateModerationStatus(
            @PathVariable Long commentId,
            @RequestParam String status,
            @RequestParam UUID moderatorId,
            @RequestParam(required = false) String reason) {
        commentService.updateModerationStatus(commentId, status, moderatorId, reason);
        return ResponseEntity.ok().build();
    }

    // ========== 検索機能 ==========

    /**
     * コンテンツ検索
     */
    @GetMapping("/search")
    public ResponseEntity<Page<LearningCommentDto>> searchComments(
            @RequestParam(required = false) Long topicId,
            @RequestParam String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID authorId,
            Pageable pageable) {

        Page<LearningCommentDto> searchResults = commentService.searchComments(
                topicId, keyword, status, authorId, pageable);

        return ResponseEntity.ok(searchResults);
    }

    /**
     * トピック内検索
     */
    @GetMapping("/topic/{topicId}/search")
    public ResponseEntity<Page<LearningCommentDto>> searchCommentsInTopic(
            @PathVariable Long topicId,
            @RequestParam String keyword,
            Pageable pageable) {
        Page<LearningCommentDto> searchResults = commentService.searchByContent(topicId, keyword, pageable);
        return ResponseEntity.ok(searchResults);
    }

    // ========== 統計・分析 ==========

    /**
     * トピック別コメント数取得
     */
    @GetMapping("/topic/{topicId}/count")
    public ResponseEntity<Long> getCommentCountByTopicId(@PathVariable Long topicId) {
        Long count = commentService.getCommentCountByTopicId(topicId);
        return ResponseEntity.ok(count);
    }

    /**
     * ユーザー別コメント数取得
     */
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Long> getCommentCountByUserId(@PathVariable UUID userId) {
        Long count = commentService.getCommentCountByUserId(userId);
        return ResponseEntity.ok(count);
    }

    /**
     * 期間別コメント統計
     */
    @GetMapping("/statistics/daily")
    public ResponseEntity<List<Map<String, Object>>> getDailyCommentStatistics(
            @RequestParam LocalDateTime fromDate,
            @RequestParam LocalDateTime toDate) {
        List<Map<String, Object>> statistics = commentService.getDailyCommentStatistics(fromDate, toDate);
        return ResponseEntity.ok(statistics);
    }

    /**
     * ユーザー別統計
     */
    @GetMapping("/statistics/user")
    public ResponseEntity<List<Map<String, Object>>> getUserCommentStatistics() {
        List<Map<String, Object>> statistics = commentService.getUserCommentStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * コメント品質統計
     */
    @GetMapping("/statistics/quality")
    public ResponseEntity<Map<String, Object>> getCommentQualityStatistics() {
        Map<String, Object> statistics = commentService.getCommentQualityStatistics();
        return ResponseEntity.ok(statistics);
    }

    // ========== 高度な機能 ==========

    /**
     * 最近のアクティビティ取得
     */
    @GetMapping("/recent-activity")
    public ResponseEntity<List<LearningCommentDto>> getRecentActivity(
            @RequestParam(defaultValue = "24") int hours,
            @RequestParam(defaultValue = "20") int limit) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        List<LearningCommentDto> recentComments = commentService.getRecentActivity(since, limit);
        return ResponseEntity.ok(recentComments);
    }

    /**
     * ユーザーのアクティビティ取得
     */
    @GetMapping("/user/{userId}/activity")
    public ResponseEntity<List<LearningCommentDto>> getUserActivity(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "20") int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<LearningCommentDto> userActivity = commentService.getUserActivity(userId, since, limit);
        return ResponseEntity.ok(userActivity);
    }

    /**
     * 通知対象コメント取得
     */
    @GetMapping("/notifications")
    public ResponseEntity<List<LearningCommentDto>> getNotificationComments(
            @RequestParam UUID userId,
            @RequestParam(defaultValue = "24") int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        List<LearningCommentDto> notifications = commentService.getNotificationComments(userId, since);
        return ResponseEntity.ok(notifications);
    }

    // ========== バッチ処理 ==========

    /**
     * 古いコメントのアーカイブ
     */
    @PostMapping("/batch/archive-old")
    public ResponseEntity<Void> archiveOldComments(@RequestParam int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        commentService.archiveOldComments(cutoffDate);
        return ResponseEntity.ok().build();
    }

    /**
     * 削除されたトピックのコメント削除
     */
    @PostMapping("/batch/cleanup-orphaned")
    public ResponseEntity<Void> cleanupOrphanedComments() {
        commentService.cleanupOrphanedComments();
        return ResponseEntity.ok().build();
    }

    /**
     * 品質スコアの再計算
     */
    @PostMapping("/batch/recalculate-quality")
    public ResponseEntity<Void> recalculateQualityScores() {
        commentService.recalculateQualityScores();
        return ResponseEntity.ok().build();
    }
}
