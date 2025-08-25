package com.sfr.tokyo.sfr_backend.controller.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningForumDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningForum;
import com.sfr.tokyo.sfr_backend.service.learning.LearningForumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * 学習フォーラム コントローラー - LearningForumController
 */
@RestController
@RequestMapping("/api/learning/forums")
@CrossOrigin(origins = "*")
public class LearningForumController {

    @Autowired
    private LearningForumService learningForumService;

    // CRUD操作

    /**
     * フォーラム作成
     */
    @PostMapping
    public ResponseEntity<LearningForumDto> createForum(@Valid @RequestBody LearningForumDto forumDto) {
        try {
            LearningForumDto createdForum = learningForumService.createForum(forumDto);
            return ResponseEntity.ok(createdForum);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * フォーラム取得
     */
    @GetMapping("/{id}")
    public ResponseEntity<LearningForumDto> getForum(@PathVariable Long id) {
        return learningForumService.getForum(id)
                .map(forum -> ResponseEntity.ok(forum))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * フォーラム更新
     */
    @PutMapping("/{id}")
    public ResponseEntity<LearningForumDto> updateForum(@PathVariable Long id,
            @Valid @RequestBody LearningForumDto forumDto) {
        try {
            LearningForumDto updatedForum = learningForumService.updateForum(id, forumDto);
            return ResponseEntity.ok(updatedForum);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * フォーラム削除
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteForum(@PathVariable Long id) {
        try {
            learningForumService.deleteForum(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // フォーラム管理

    /**
     * フォーラムピン留め
     */
    @PostMapping("/{id}/pin")
    public ResponseEntity<LearningForumDto> pinForum(@PathVariable Long id) {
        try {
            LearningForumDto pinnedForum = learningForumService.pinForum(id);
            return ResponseEntity.ok(pinnedForum);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * フォーラムピン留め解除
     */
    @PostMapping("/{id}/unpin")
    public ResponseEntity<LearningForumDto> unpinForum(@PathVariable Long id) {
        try {
            LearningForumDto unpinnedForum = learningForumService.unpinForum(id);
            return ResponseEntity.ok(unpinnedForum);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * フォーラムロック
     */
    @PostMapping("/{id}/lock")
    public ResponseEntity<LearningForumDto> lockForum(@PathVariable Long id) {
        try {
            LearningForumDto lockedForum = learningForumService.lockForum(id);
            return ResponseEntity.ok(lockedForum);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * フォーラムロック解除
     */
    @PostMapping("/{id}/unlock")
    public ResponseEntity<LearningForumDto> unlockForum(@PathVariable Long id) {
        try {
            LearningForumDto unlockedForum = learningForumService.unlockForum(id);
            return ResponseEntity.ok(unlockedForum);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * フォーラムアーカイブ
     */
    @PostMapping("/{id}/archive")
    public ResponseEntity<LearningForumDto> archiveForum(@PathVariable Long id) {
        try {
            LearningForumDto archivedForum = learningForumService.archiveForum(id);
            return ResponseEntity.ok(archivedForum);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * フォーラムアーカイブ解除
     */
    @PostMapping("/{id}/unarchive")
    public ResponseEntity<LearningForumDto> unarchiveForum(@PathVariable Long id) {
        try {
            LearningForumDto unarchivedForum = learningForumService.unarchiveForum(id);
            return ResponseEntity.ok(unarchivedForum);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * フォーラムフィーチャー
     */
    @PostMapping("/{id}/feature")
    public ResponseEntity<LearningForumDto> featureForum(@PathVariable Long id) {
        try {
            LearningForumDto featuredForum = learningForumService.featureForum(id);
            return ResponseEntity.ok(featuredForum);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * フォーラムフィーチャー解除
     */
    @PostMapping("/{id}/unfeature")
    public ResponseEntity<LearningForumDto> unfeatureForum(@PathVariable Long id) {
        try {
            LearningForumDto unfeaturedForum = learningForumService.unfeatureForum(id);
            return ResponseEntity.ok(unfeaturedForum);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 統計・スコア更新

    /**
     * フォーラム統計更新
     */
    @PutMapping("/{id}/statistics")
    public ResponseEntity<LearningForumDto> updateStatistics(@PathVariable Long id,
            @RequestParam int topicCount,
            @RequestParam int commentCount,
            @RequestParam int viewCount,
            @RequestParam int subscriberCount) {
        try {
            LearningForumDto updatedForum = learningForumService.updateForumStatistics(id, topicCount, commentCount,
                    viewCount, subscriberCount);
            return ResponseEntity.ok(updatedForum);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 最後のアクティビティ更新
     */
    @PutMapping("/{id}/last-activity")
    public ResponseEntity<LearningForumDto> updateLastActivity(@PathVariable Long id,
            @RequestParam Long topicId,
            @RequestParam String topicTitle,
            @RequestParam UUID userId) {
        try {
            LearningForumDto updatedForum = learningForumService.updateLastActivity(id, topicId, topicTitle, userId);
            return ResponseEntity.ok(updatedForum);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 閲覧数増加
     */
    @PostMapping("/{id}/increment-view")
    public ResponseEntity<LearningForumDto> incrementViewCount(@PathVariable Long id) {
        try {
            LearningForumDto updatedForum = learningForumService.incrementViewCount(id);
            return ResponseEntity.ok(updatedForum);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * サブスクライバー数増加
     */
    @PostMapping("/{id}/subscribe")
    public ResponseEntity<LearningForumDto> subscribeForum(@PathVariable Long id) {
        try {
            LearningForumDto updatedForum = learningForumService.incrementSubscriberCount(id);
            return ResponseEntity.ok(updatedForum);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * サブスクライバー数減少
     */
    @PostMapping("/{id}/unsubscribe")
    public ResponseEntity<LearningForumDto> unsubscribeForum(@PathVariable Long id) {
        try {
            LearningForumDto updatedForum = learningForumService.decrementSubscriberCount(id);
            return ResponseEntity.ok(updatedForum);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 検索・取得系

    /**
     * 全フォーラム取得
     */
    @GetMapping
    public ResponseEntity<List<LearningForumDto>> getAllForums() {
        // 基本的にはアクティブフォーラムを返す
        List<LearningForumDto> forums = learningForumService.getActiveForums();
        return ResponseEntity.ok(forums);
    }

    /**
     * スペース別フォーラム取得
     */
    @GetMapping("/space/{spaceId}")
    public ResponseEntity<List<LearningForumDto>> getForumsBySpace(@PathVariable Long spaceId) {
        List<LearningForumDto> forums = learningForumService.getForumsBySpace(spaceId);
        return ResponseEntity.ok(forums);
    }

    /**
     * 作成者別フォーラム取得
     */
    @GetMapping("/creator/{creatorId}")
    public ResponseEntity<List<LearningForumDto>> getForumsByCreator(@PathVariable UUID creatorId) {
        List<LearningForumDto> forums = learningForumService.getForumsByCreator(creatorId);
        return ResponseEntity.ok(forums);
    }

    /**
     * カテゴリ別フォーラム取得
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<LearningForumDto>> getForumsByCategory(@PathVariable String category) {
        try {
            LearningForum.ForumCategory forumCategory = LearningForum.ForumCategory.valueOf(category.toUpperCase());
            List<LearningForumDto> forums = learningForumService.getForumsByCategory(forumCategory);
            return ResponseEntity.ok(forums);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * ステータス別フォーラム取得
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<LearningForumDto>> getForumsByStatus(@PathVariable String status) {
        try {
            LearningForum.ForumStatus forumStatus = LearningForum.ForumStatus.valueOf(status.toUpperCase());
            List<LearningForumDto> forums = learningForumService.getForumsByStatus(forumStatus);
            return ResponseEntity.ok(forums);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * アクティブフォーラム取得
     */
    @GetMapping("/active")
    public ResponseEntity<List<LearningForumDto>> getActiveForums() {
        List<LearningForumDto> forums = learningForumService.getActiveForums();
        return ResponseEntity.ok(forums);
    }

    /**
     * 人気フォーラム取得
     */
    @GetMapping("/popular")
    public ResponseEntity<List<LearningForumDto>> getPopularForums() {
        List<LearningForumDto> forums = learningForumService.getPopularForums();
        return ResponseEntity.ok(forums);
    }

    /**
     * フィーチャードフォーラム取得
     */
    @GetMapping("/featured")
    public ResponseEntity<List<LearningForumDto>> getFeaturedForums() {
        List<LearningForumDto> forums = learningForumService.getFeaturedForums();
        return ResponseEntity.ok(forums);
    }

    /**
     * ピン留めフォーラム取得
     */
    @GetMapping("/pinned")
    public ResponseEntity<List<LearningForumDto>> getPinnedForums() {
        List<LearningForumDto> forums = learningForumService.getPinnedForums();
        return ResponseEntity.ok(forums);
    }

    /**
     * 最近アクティブなフォーラム取得
     */
    @GetMapping("/recent")
    public ResponseEntity<List<LearningForumDto>> getRecentlyActiveForums(@RequestParam(defaultValue = "7") int days) {
        List<LearningForumDto> forums = learningForumService.getRecentlyActiveForums(days);
        return ResponseEntity.ok(forums);
    }

    /**
     * 高品質フォーラム取得
     */
    @GetMapping("/high-quality")
    public ResponseEntity<List<LearningForumDto>> getHighQualityForums(
            @RequestParam(defaultValue = "50.0") BigDecimal minScore) {
        List<LearningForumDto> forums = learningForumService.getHighQualityForums(minScore);
        return ResponseEntity.ok(forums);
    }

    /**
     * トレンドフォーラム取得
     */
    @GetMapping("/trending")
    public ResponseEntity<List<LearningForumDto>> getTrendingForums() {
        List<LearningForumDto> forums = learningForumService.getTrendingForums();
        return ResponseEntity.ok(forums);
    }

    // 検索機能

    /**
     * キーワード検索
     */
    @GetMapping("/search")
    public ResponseEntity<List<LearningForumDto>> searchForums(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        List<LearningForumDto> forums = learningForumService.searchForums(keyword.trim());
        return ResponseEntity.ok(forums);
    }

    /**
     * タグ検索
     */
    @GetMapping("/search/tag")
    public ResponseEntity<List<LearningForumDto>> searchByTag(@RequestParam String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        List<LearningForumDto> forums = learningForumService.searchByTag(tag.trim());
        return ResponseEntity.ok(forums);
    }

    // 統計・分析系

    /**
     * 全体統計取得
     */
    @GetMapping("/statistics")
    public ResponseEntity<LearningForumService.ForumStatistics> getOverallStatistics() {
        LearningForumService.ForumStatistics statistics = learningForumService.getOverallStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * カテゴリ統計取得
     */
    @GetMapping("/statistics/category/{category}")
    public ResponseEntity<LearningForumService.CategoryStatistics> getCategoryStatistics(
            @PathVariable String category) {
        try {
            LearningForum.ForumCategory forumCategory = LearningForum.ForumCategory.valueOf(category.toUpperCase());
            LearningForumService.CategoryStatistics statistics = learningForumService
                    .getCategoryStatistics(forumCategory);
            return ResponseEntity.ok(statistics);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * スペース統計取得
     */
    @GetMapping("/statistics/space/{spaceId}")
    public ResponseEntity<LearningForumService.SpaceStatistics> getSpaceStatistics(@PathVariable Long spaceId) {
        LearningForumService.SpaceStatistics statistics = learningForumService.getSpaceStatistics(spaceId);
        return ResponseEntity.ok(statistics);
    }

    // バッチ処理

    /**
     * 自動クローズ処理実行
     */
    @PostMapping("/batch/auto-close")
    public ResponseEntity<BatchProcessResult> processAutoClose() {
        try {
            int processedCount = learningForumService.processAutoCloseForums();
            return ResponseEntity.ok(new BatchProcessResult(processedCount, "自動クローズ処理が完了しました"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BatchProcessResult(0, "処理中にエラーが発生しました: " + e.getMessage()));
        }
    }

    /**
     * 全フォーラムスコア再計算
     */
    @PostMapping("/batch/recalculate-scores")
    public ResponseEntity<BatchProcessResult> recalculateScores() {
        try {
            int processedCount = learningForumService.recalculateAllScores();
            return ResponseEntity.ok(new BatchProcessResult(processedCount, "スコア再計算が完了しました"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BatchProcessResult(0, "処理中にエラーが発生しました: " + e.getMessage()));
        }
    }

    // フォーラムカテゴリ取得

    /**
     * 利用可能なフォーラムカテゴリ取得
     */
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryInfo>> getAvailableCategories() {
        List<CategoryInfo> categories = List.of(
                new CategoryInfo("GENERAL_DISCUSSION", "一般ディスカッション"),
                new CategoryInfo("COURSE_SUPPORT", "コースサポート"),
                new CategoryInfo("TECHNICAL_HELP", "技術的質問"),
                new CategoryInfo("STUDY_GROUP", "勉強会"),
                new CategoryInfo("PROJECT_COLLABORATION", "プロジェクト協力"),
                new CategoryInfo("CAREER_ADVICE", "キャリア相談"),
                new CategoryInfo("RESOURCE_SHARING", "リソース共有"),
                new CategoryInfo("FEEDBACK_SUGGESTIONS", "フィードバック・提案"),
                new CategoryInfo("ANNOUNCEMENTS", "お知らせ"),
                new CategoryInfo("SHOWCASE", "作品紹介"),
                new CategoryInfo("EVENTS", "イベント"),
                new CategoryInfo("OFF_TOPIC", "雑談"),
                new CategoryInfo("MENTORSHIP", "メンタリング"),
                new CategoryInfo("JOB_BOARD", "求人情報"),
                new CategoryInfo("OTHER", "その他"));
        return ResponseEntity.ok(categories);
    }

    /**
     * 利用可能なフォーラムステータス取得
     */
    @GetMapping("/statuses")
    public ResponseEntity<List<StatusInfo>> getAvailableStatuses() {
        List<StatusInfo> statuses = List.of(
                new StatusInfo("ACTIVE", "アクティブ"),
                new StatusInfo("INACTIVE", "非アクティブ"),
                new StatusInfo("LOCKED", "ロック済み"),
                new StatusInfo("ARCHIVED", "アーカイブ済み"),
                new StatusInfo("UNDER_REVIEW", "レビュー中"),
                new StatusInfo("SUSPENDED", "停止中"),
                new StatusInfo("DELETED", "削除済み"));
        return ResponseEntity.ok(statuses);
    }

    /**
     * 利用可能な可視性レベル取得
     */
    @GetMapping("/visibility-levels")
    public ResponseEntity<List<VisibilityInfo>> getAvailableVisibilityLevels() {
        List<VisibilityInfo> visibilityLevels = List.of(
                new VisibilityInfo("PUBLIC", "公開"),
                new VisibilityInfo("MEMBERS_ONLY", "メンバーのみ"),
                new VisibilityInfo("SPACE_ONLY", "スペース限定"),
                new VisibilityInfo("MODERATORS_ONLY", "モデレーターのみ"),
                new VisibilityInfo("PRIVATE", "プライベート"));
        return ResponseEntity.ok(visibilityLevels);
    }

    /**
     * 利用可能なモデレーションレベル取得
     */
    @GetMapping("/moderation-levels")
    public ResponseEntity<List<ModerationInfo>> getAvailableModerationLevels() {
        List<ModerationInfo> moderationLevels = List.of(
                new ModerationInfo("NONE", "なし"),
                new ModerationInfo("LIGHT", "軽度"),
                new ModerationInfo("STANDARD", "標準"),
                new ModerationInfo("STRICT", "厳格"),
                new ModerationInfo("MANUAL_APPROVAL", "手動承認"));
        return ResponseEntity.ok(moderationLevels);
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

    public static class VisibilityInfo {
        private final String code;
        private final String description;

        public VisibilityInfo(String code, String description) {
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

    public static class ModerationInfo {
        private final String code;
        private final String description;

        public ModerationInfo(String code, String description) {
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
}
