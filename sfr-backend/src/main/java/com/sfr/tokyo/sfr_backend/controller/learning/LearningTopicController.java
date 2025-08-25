package com.sfr.tokyo.sfr_backend.controller.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningTopicDto;
import com.sfr.tokyo.sfr_backend.service.learning.LearningTopicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * LearningTopic Controller
 * 学習トピック管理REST API
 */
@RestController
@RequestMapping("/api/learning/topics")
@RequiredArgsConstructor
@Slf4j
public class LearningTopicController {

    private final LearningTopicService topicService;

    // ========== 基本CRUD操作 ==========

    /**
     * トピック作成
     */
    @PostMapping
    public ResponseEntity<LearningTopicDto> createTopic(@Valid @RequestBody LearningTopicDto topicDto) {
        log.info("トピック作成API呼び出し: forumId={}, title={}", topicDto.getForumId(), topicDto.getTitle());

        LearningTopicDto created = topicService.createTopic(topicDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * トピック更新
     */
    @PutMapping("/{topicId}")
    public ResponseEntity<LearningTopicDto> updateTopic(
            @PathVariable Long topicId,
            @Valid @RequestBody LearningTopicDto topicDto) {
        log.info("トピック更新API呼び出し: topicId={}", topicId);

        LearningTopicDto updated = topicService.updateTopic(topicId, topicDto);
        return ResponseEntity.ok(updated);
    }

    /**
     * トピック削除
     */
    @DeleteMapping("/{topicId}")
    public ResponseEntity<Void> deleteTopic(@PathVariable Long topicId) {
        log.info("トピック削除API呼び出し: topicId={}", topicId);

        topicService.deleteTopic(topicId);
        return ResponseEntity.noContent().build();
    }

    /**
     * トピック取得
     */
    @GetMapping("/{topicId}")
    public ResponseEntity<LearningTopicDto> getTopic(@PathVariable Long topicId) {
        LearningTopicDto topic = topicService.getTopicById(topicId);
        return ResponseEntity.ok(topic);
    }

    // ========== 検索機能 ==========

    /**
     * フォーラム別トピック一覧取得
     */
    @GetMapping("/forum/{forumId}")
    public ResponseEntity<Page<LearningTopicDto>> getTopicsByForumId(
            @PathVariable Long forumId,
            Pageable pageable) {
        Page<LearningTopicDto> topics = topicService.getTopicsByForumId(forumId, pageable);
        return ResponseEntity.ok(topics);
    }

    /**
     * ユーザー別トピック一覧取得
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<LearningTopicDto>> getTopicsByUserId(
            @PathVariable UUID userId,
            Pageable pageable) {
        Page<LearningTopicDto> topics = topicService.getTopicsByUserId(userId, pageable);
        return ResponseEntity.ok(topics);
    }

    /**
     * ピン留めトピック取得
     */
    @GetMapping("/forum/{forumId}/pinned")
    public ResponseEntity<List<LearningTopicDto>> getPinnedTopics(@PathVariable Long forumId) {
        List<LearningTopicDto> pinnedTopics = topicService.getPinnedTopicsByForumId(forumId);
        return ResponseEntity.ok(pinnedTopics);
    }

    /**
     * 人気トピック取得
     */
    @GetMapping("/forum/{forumId}/popular")
    public ResponseEntity<List<LearningTopicDto>> getPopularTopics(
            @PathVariable Long forumId,
            @RequestParam(defaultValue = "10") int limit) {
        List<LearningTopicDto> popularTopics = topicService.getPopularTopicsByForumId(forumId, limit);
        return ResponseEntity.ok(popularTopics);
    }

    /**
     * 最新トピック取得
     */
    @GetMapping("/forum/{forumId}/latest")
    public ResponseEntity<List<LearningTopicDto>> getLatestTopics(
            @PathVariable Long forumId,
            @RequestParam(defaultValue = "10") int limit) {
        List<LearningTopicDto> latestTopics = topicService.getLatestTopicsByForumId(forumId, limit);
        return ResponseEntity.ok(latestTopics);
    }

    /**
     * 解決済みトピック取得
     */
    @GetMapping("/forum/{forumId}/solved")
    public ResponseEntity<Page<LearningTopicDto>> getSolvedTopics(
            @PathVariable Long forumId,
            Pageable pageable) {
        Page<LearningTopicDto> solvedTopics = topicService.getSolvedTopicsByForumId(forumId, pageable);
        return ResponseEntity.ok(solvedTopics);
    }

    /**
     * 未解決トピック取得
     */
    @GetMapping("/forum/{forumId}/unsolved")
    public ResponseEntity<Page<LearningTopicDto>> getUnsolvedTopics(
            @PathVariable Long forumId,
            Pageable pageable) {
        Page<LearningTopicDto> unsolvedTopics = topicService.getUnsolvedTopicsByForumId(forumId, pageable);
        return ResponseEntity.ok(unsolvedTopics);
    }

    // ========== アクション機能 ==========

    /**
     * トピックに「いいね」
     */
    @PostMapping("/{topicId}/like")
    public ResponseEntity<Void> likeTopic(@PathVariable Long topicId) {
        topicService.toggleLike(topicId, true);
        return ResponseEntity.ok().build();
    }

    /**
     * トピックの「いいね」解除
     */
    @DeleteMapping("/{topicId}/like")
    public ResponseEntity<Void> unlikeTopic(@PathVariable Long topicId) {
        topicService.toggleLike(topicId, false);
        return ResponseEntity.ok().build();
    }

    /**
     * トピックをピン留め
     */
    @PostMapping("/{topicId}/pin")
    public ResponseEntity<Void> pinTopic(@PathVariable Long topicId) {
        topicService.setPinned(topicId, true);
        return ResponseEntity.ok().build();
    }

    /**
     * トピックのピン留め解除
     */
    @DeleteMapping("/{topicId}/pin")
    public ResponseEntity<Void> unpinTopic(@PathVariable Long topicId) {
        topicService.setPinned(topicId, false);
        return ResponseEntity.ok().build();
    }

    /**
     * トピックをロック
     */
    @PostMapping("/{topicId}/lock")
    public ResponseEntity<Void> lockTopic(@PathVariable Long topicId) {
        topicService.setLocked(topicId, true);
        return ResponseEntity.ok().build();
    }

    /**
     * トピックのロック解除
     */
    @DeleteMapping("/{topicId}/lock")
    public ResponseEntity<Void> unlockTopic(@PathVariable Long topicId) {
        topicService.setLocked(topicId, false);
        return ResponseEntity.ok().build();
    }

    /**
     * トピックを解決済みに設定
     */
    @PostMapping("/{topicId}/solve")
    public ResponseEntity<Void> solveTopic(@PathVariable Long topicId) {
        topicService.setSolved(topicId, true);
        return ResponseEntity.ok().build();
    }

    /**
     * トピックの解決済み状態を解除
     */
    @DeleteMapping("/{topicId}/solve")
    public ResponseEntity<Void> unsolveTopic(@PathVariable Long topicId) {
        topicService.setSolved(topicId, false);
        return ResponseEntity.ok().build();
    }

    /**
     * トピックを推奨設定
     */
    @PostMapping("/{topicId}/feature")
    public ResponseEntity<Void> featureTopic(@PathVariable Long topicId) {
        topicService.setFeatured(topicId, true);
        return ResponseEntity.ok().build();
    }

    /**
     * トピックの推奨解除
     */
    @DeleteMapping("/{topicId}/feature")
    public ResponseEntity<Void> unfeatureTopic(@PathVariable Long topicId) {
        topicService.setFeatured(topicId, false);
        return ResponseEntity.ok().build();
    }

    // ========== 品質・スコア管理 ==========

    /**
     * 品質スコア更新
     */
    @PutMapping("/{topicId}/quality-score")
    public ResponseEntity<Void> updateQualityScore(
            @PathVariable Long topicId,
            @RequestParam BigDecimal score) {
        topicService.updateQualityScore(topicId, score);
        return ResponseEntity.ok().build();
    }

    /**
     * 人気度スコア更新
     */
    @PutMapping("/{topicId}/popularity-score")
    public ResponseEntity<Void> updatePopularityScore(
            @PathVariable Long topicId,
            @RequestParam BigDecimal score) {
        topicService.updatePopularityScore(topicId, score);
        return ResponseEntity.ok().build();
    }

    // ========== モデレーション機能 ==========

    /**
     * モデレーション待ちトピック取得
     */
    @GetMapping("/moderation/pending")
    public ResponseEntity<Page<LearningTopicDto>> getPendingModerationTopics(Pageable pageable) {
        Page<LearningTopicDto> pendingTopics = topicService.getPendingModerationTopics(pageable);
        return ResponseEntity.ok(pendingTopics);
    }

    /**
     * モデレーションステータス更新
     */
    @PutMapping("/{topicId}/moderation")
    public ResponseEntity<Void> updateModerationStatus(
            @PathVariable Long topicId,
            @RequestParam String status,
            @RequestParam UUID moderatorId) {
        topicService.updateModerationStatus(topicId, status, moderatorId);
        return ResponseEntity.ok().build();
    }

    /**
     * ステータス更新
     */
    @PutMapping("/{topicId}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long topicId,
            @RequestParam String status) {
        topicService.updateStatus(topicId, status);
        return ResponseEntity.ok().build();
    }

    // ========== 検索機能 ==========

    /**
     * キーワード検索
     */
    @GetMapping("/search")
    public ResponseEntity<Page<LearningTopicDto>> searchTopics(
            @RequestParam(required = false) Long forumId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) UUID creatorId,
            Pageable pageable) {

        Page<LearningTopicDto> searchResults = topicService.searchTopics(
                forumId, keyword, status, priority, creatorId, pageable);

        return ResponseEntity.ok(searchResults);
    }

    /**
     * タイトル・コンテンツ検索
     */
    @GetMapping("/forum/{forumId}/search")
    public ResponseEntity<Page<LearningTopicDto>> searchTopicsByContent(
            @PathVariable Long forumId,
            @RequestParam String keyword,
            Pageable pageable) {
        Page<LearningTopicDto> searchResults = topicService.searchByContent(forumId, keyword, pageable);
        return ResponseEntity.ok(searchResults);
    }

    // ========== 統計・分析 ==========

    /**
     * フォーラム別トピック数取得
     */
    @GetMapping("/forum/{forumId}/count")
    public ResponseEntity<Long> getTopicCountByForumId(@PathVariable Long forumId) {
        Long count = topicService.getTopicCountByForumId(forumId);
        return ResponseEntity.ok(count);
    }

    /**
     * ユーザー別トピック数取得
     */
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Long> getTopicCountByUserId(@PathVariable UUID userId) {
        Long count = topicService.getTopicCountByUserId(userId);
        return ResponseEntity.ok(count);
    }

    /**
     * 期間別トピック統計
     */
    @GetMapping("/statistics/daily")
    public ResponseEntity<List<Map<String, Object>>> getDailyTopicStatistics(
            @RequestParam LocalDateTime fromDate,
            @RequestParam LocalDateTime toDate) {
        List<Map<String, Object>> statistics = topicService.getDailyTopicStatistics(fromDate, toDate);
        return ResponseEntity.ok(statistics);
    }

    /**
     * フォーラム別統計
     */
    @GetMapping("/statistics/forum")
    public ResponseEntity<List<Map<String, Object>>> getForumStatistics() {
        List<Map<String, Object>> statistics = topicService.getForumStatistics();
        return ResponseEntity.ok(statistics);
    }

    // ========== 高度な機能 ==========

    /**
     * 関連トピック取得
     */
    @GetMapping("/{topicId}/related")
    public ResponseEntity<List<LearningTopicDto>> getRelatedTopics(
            @PathVariable Long topicId,
            @RequestParam(defaultValue = "5") int limit) {
        List<LearningTopicDto> relatedTopics = topicService.getRelatedTopics(topicId, limit);
        return ResponseEntity.ok(relatedTopics);
    }

    /**
     * トレンディングトピック取得
     */
    @GetMapping("/trending")
    public ResponseEntity<List<LearningTopicDto>> getTrendingTopics(
            @RequestParam(defaultValue = "10") int limit) {
        List<LearningTopicDto> trendingTopics = topicService.getTrendingTopics(limit);
        return ResponseEntity.ok(trendingTopics);
    }

    /**
     * アクティブな討論取得
     */
    @GetMapping("/forum/{forumId}/active")
    public ResponseEntity<List<LearningTopicDto>> getActiveDiscussions(
            @PathVariable Long forumId,
            @RequestParam(defaultValue = "24") int hours) {
        LocalDateTime sinceDate = LocalDateTime.now().minusHours(hours);
        List<LearningTopicDto> activeTopics = topicService.getActiveDiscussions(forumId, sinceDate);
        return ResponseEntity.ok(activeTopics);
    }

    // ========== バッチ処理 ==========

    /**
     * 古いトピックのアーカイブ
     */
    @PostMapping("/batch/archive-old")
    public ResponseEntity<Void> archiveOldTopics(@RequestParam int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        topicService.archiveOldTopics(cutoffDate);
        return ResponseEntity.ok().build();
    }

    /**
     * 非アクティブトピックの更新
     */
    @PostMapping("/batch/update-inactive")
    public ResponseEntity<Void> updateInactiveTopics(@RequestParam int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        topicService.markInactiveTopics(cutoffDate);
        return ResponseEntity.ok().build();
    }
}
