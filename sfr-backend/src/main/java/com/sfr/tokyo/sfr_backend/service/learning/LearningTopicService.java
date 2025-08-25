package com.sfr.tokyo.sfr_backend.service.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningTopicDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningTopic;
import com.sfr.tokyo.sfr_backend.repository.learning.LearningTopicRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 学習トピック サービス - LearningTopicService
 */
@Service
@Transactional
public class LearningTopicService {

    @Autowired
    private LearningTopicRepository topicRepository;

    // CRUD操作

    /**
     * トピック作成
     */
    public LearningTopicDto createTopic(LearningTopicDto topicDto) {
        LearningTopic topic = new LearningTopic();
        BeanUtils.copyProperties(topicDto, topic, "id", "createdAt", "updatedAt");

        // 初期値設定
        topic.setTopicStatus(LearningTopic.TopicStatus.ACTIVE);
        topic.setLastActivityAt(LocalDateTime.now());
        topic.setLastActivityUserId(topic.getCreatorId());

        // モデレーション設定
        if (Boolean.TRUE.equals(topicDto.getRequiresModeration())) {
            topic.setModerationStatus(LearningTopic.ModerationStatus.PENDING);
        } else {
            topic.setModerationStatus(LearningTopic.ModerationStatus.AUTO_APPROVED);
        }

        LearningTopic savedTopic = topicRepository.save(topic);
        return convertToDto(savedTopic);
    }

    /**
     * トピック取得
     */
    @Transactional(readOnly = true)
    public Optional<LearningTopicDto> getTopic(Long id) {
        return topicRepository.findById(id)
                .filter(topic -> topic.getDeletedAt() == null)
                .map(this::convertToDto);
    }

    /**
     * トピック更新
     */
    public LearningTopicDto updateTopic(Long id, LearningTopicDto topicDto) {
        LearningTopic existingTopic = topicRepository.findById(id)
                .filter(topic -> topic.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("トピックが見つかりません: " + id));

        // 更新可能フィールドのコピー
        existingTopic.setTitle(topicDto.getTitle());
        existingTopic.setContent(topicDto.getContent());
        existingTopic.setTopicType(topicDto.getTopicType());
        existingTopic.setPriority(topicDto.getPriority());
        existingTopic.setTags(topicDto.getTags());
        existingTopic.setAutoCloseAt(topicDto.getAutoCloseAt());

        LearningTopic savedTopic = topicRepository.save(existingTopic);
        return convertToDto(savedTopic);
    }

    /**
     * トピック削除（論理削除）
     */
    public void deleteTopic(Long id) {
        LearningTopic topic = topicRepository.findById(id)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("トピックが見つかりません: " + id));

        topic.setDeletedAt(LocalDateTime.now());
        topicRepository.save(topic);
    }

    // トピック管理機能

    /**
     * トピックピン留め
     */
    public LearningTopicDto pinTopic(Long id) {
        topicRepository.updatePinStatus(id, true);
        return getTopic(id).orElseThrow(() -> new RuntimeException("トピックが見つかりません: " + id));
    }

    /**
     * トピックピン留め解除
     */
    public LearningTopicDto unpinTopic(Long id) {
        topicRepository.updatePinStatus(id, false);
        return getTopic(id).orElseThrow(() -> new RuntimeException("トピックが見つかりません: " + id));
    }

    /**
     * トピックロック
     */
    public LearningTopicDto lockTopic(Long id) {
        topicRepository.updateLockStatus(id, true);
        return getTopic(id).orElseThrow(() -> new RuntimeException("トピックが見つかりません: " + id));
    }

    /**
     * トピックロック解除
     */
    public LearningTopicDto unlockTopic(Long id) {
        topicRepository.updateLockStatus(id, false);
        return getTopic(id).orElseThrow(() -> new RuntimeException("トピックが見つかりません: " + id));
    }

    /**
     * トピックフィーチャー
     */
    public LearningTopicDto featureTopic(Long id) {
        topicRepository.updateFeatureStatus(id, true);
        return getTopic(id).orElseThrow(() -> new RuntimeException("トピックが見つかりません: " + id));
    }

    /**
     * トピックフィーチャー解除
     */
    public LearningTopicDto unfeatureTopic(Long id) {
        topicRepository.updateFeatureStatus(id, false);
        return getTopic(id).orElseThrow(() -> new RuntimeException("トピックが見つかりません: " + id));
    }

    /**
     * トピッククローズ
     */
    public LearningTopicDto closeTopic(Long id, UUID closedBy, String reason) {
        LearningTopic topic = topicRepository.findById(id)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("トピックが見つかりません: " + id));

        topic.close(closedBy, reason);
        LearningTopic savedTopic = topicRepository.save(topic);
        return convertToDto(savedTopic);
    }

    /**
     * トピックアーカイブ
     */
    public LearningTopicDto archiveTopic(Long id, UUID archivedBy, String reason) {
        LearningTopic topic = topicRepository.findById(id)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("トピックが見つかりません: " + id));

        topic.archive(archivedBy, reason);
        LearningTopic savedTopic = topicRepository.save(topic);
        return convertToDto(savedTopic);
    }

    // 統計・エンゲージメント操作

    /**
     * 閲覧数増加
     */
    public LearningTopicDto incrementViewCount(Long id) {
        topicRepository.incrementViewCount(id);
        return getTopic(id).orElseThrow(() -> new RuntimeException("トピックが見つかりません: " + id));
    }

    /**
     * いいね追加
     */
    public LearningTopicDto likeTopic(Long id) {
        topicRepository.incrementLikeCount(id);
        return getTopic(id).orElseThrow(() -> new RuntimeException("トピックが見つかりません: " + id));
    }

    /**
     * いいね削除
     */
    public LearningTopicDto unlikeTopic(Long id) {
        topicRepository.decrementLikeCount(id);
        return getTopic(id).orElseThrow(() -> new RuntimeException("トピックが見つかりません: " + id));
    }

    /**
     * ブックマーク追加
     */
    public LearningTopicDto bookmarkTopic(Long id) {
        topicRepository.incrementBookmarkCount(id);
        return getTopic(id).orElseThrow(() -> new RuntimeException("トピックが見つかりません: " + id));
    }

    /**
     * ブックマーク削除
     */
    public LearningTopicDto unbookmarkTopic(Long id) {
        topicRepository.decrementBookmarkCount(id);
        return getTopic(id).orElseThrow(() -> new RuntimeException("トピックが見つかりません: " + id));
    }

    /**
     * シェア数増加
     */
    public LearningTopicDto shareTopic(Long id) {
        topicRepository.incrementShareCount(id);
        return getTopic(id).orElseThrow(() -> new RuntimeException("トピックが見つかりません: " + id));
    }

    /**
     * 最後のアクティビティ更新
     */
    public LearningTopicDto updateLastActivity(Long id, UUID userId) {
        topicRepository.updateLastActivity(id, LocalDateTime.now(), userId);
        return getTopic(id).orElseThrow(() -> new RuntimeException("トピックが見つかりません: " + id));
    }

    /**
     * 最後のコメント更新
     */
    public LearningTopicDto updateLastComment(Long id, Long commentId, UUID userId) {
        topicRepository.updateLastComment(id, commentId, LocalDateTime.now(), userId);
        topicRepository.incrementCommentCount(id);
        return getTopic(id).orElseThrow(() -> new RuntimeException("トピックが見つかりません: " + id));
    }

    /**
     * ベストアンサー設定
     */
    public LearningTopicDto setBestAnswer(Long id, Long answerId, UUID selectedBy) {
        topicRepository.setBestAnswer(id, answerId, LocalDateTime.now(), selectedBy);
        return getTopic(id).orElseThrow(() -> new RuntimeException("トピックが見つかりません: " + id));
    }

    /**
     * ベストアンサー解除
     */
    public LearningTopicDto clearBestAnswer(Long id) {
        topicRepository.clearBestAnswer(id);
        return getTopic(id).orElseThrow(() -> new RuntimeException("トピックが見つかりません: " + id));
    }

    // 検索・取得系

    /**
     * フォーラム別トピック取得
     */
    @Transactional(readOnly = true)
    public List<LearningTopicDto> getTopicsByForum(Long forumId) {
        return topicRepository.findByForumId(forumId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 作成者別トピック取得
     */
    @Transactional(readOnly = true)
    public List<LearningTopicDto> getTopicsByCreator(UUID creatorId) {
        return topicRepository.findByCreatorId(creatorId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * ステータス別トピック取得
     */
    @Transactional(readOnly = true)
    public List<LearningTopicDto> getTopicsByStatus(LearningTopic.TopicStatus status) {
        return topicRepository.findByTopicStatus(status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * タイプ別トピック取得
     */
    @Transactional(readOnly = true)
    public List<LearningTopicDto> getTopicsByType(LearningTopic.TopicType type) {
        return topicRepository.findByTopicType(type).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 優先度別トピック取得
     */
    @Transactional(readOnly = true)
    public List<LearningTopicDto> getTopicsByPriority(LearningTopic.TopicPriority priority) {
        return topicRepository.findByPriority(priority).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * アクティブトピック取得
     */
    @Transactional(readOnly = true)
    public List<LearningTopicDto> getActiveTopics() {
        return topicRepository.findActiveTopics().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * フォーラム別アクティブトピック取得
     */
    @Transactional(readOnly = true)
    public List<LearningTopicDto> getActiveTopicsByForum(Long forumId) {
        return topicRepository.findActiveTopicsByForum(forumId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * ピン留めトピック取得
     */
    @Transactional(readOnly = true)
    public List<LearningTopicDto> getPinnedTopics() {
        return topicRepository.findPinnedTopics().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * フィーチャードトピック取得
     */
    @Transactional(readOnly = true)
    public List<LearningTopicDto> getFeaturedTopics() {
        return topicRepository.findFeaturedTopics().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 人気トピック取得
     */
    @Transactional(readOnly = true)
    public List<LearningTopicDto> getPopularTopics() {
        return topicRepository.findPopularTopics().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 高品質トピック取得
     */
    @Transactional(readOnly = true)
    public List<LearningTopicDto> getHighQualityTopics(BigDecimal minScore) {
        return topicRepository.findHighQualityTopics(minScore).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * トレンドトピック取得
     */
    @Transactional(readOnly = true)
    public List<LearningTopicDto> getTrendingTopics() {
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        return topicRepository.findTrendingTopics(since).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 最近のトピック取得
     */
    @Transactional(readOnly = true)
    public List<LearningTopicDto> getRecentTopics(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return topicRepository.findRecentTopics(since).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 最近アクティブなトピック取得
     */
    @Transactional(readOnly = true)
    public List<LearningTopicDto> getRecentlyActiveTopics(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return topicRepository.findRecentlyActiveTopics(since).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 未解決の質問取得
     */
    @Transactional(readOnly = true)
    public List<LearningTopicDto> getUnsolvedQuestions() {
        return topicRepository.findUnsolvedQuestions().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 解決済みの質問取得
     */
    @Transactional(readOnly = true)
    public List<LearningTopicDto> getSolvedQuestions() {
        return topicRepository.findSolvedQuestions().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 検索機能

    /**
     * キーワード検索
     */
    @Transactional(readOnly = true)
    public List<LearningTopicDto> searchTopics(String keyword) {
        return topicRepository.searchByKeyword(keyword).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * タグ検索
     */
    @Transactional(readOnly = true)
    public List<LearningTopicDto> searchByTag(String tag) {
        return topicRepository.searchByTag(tag).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 複合検索
     */
    @Transactional(readOnly = true)
    public List<LearningTopicDto> searchTopics(Long forumId, LearningTopic.TopicType type,
            LearningTopic.TopicStatus status) {
        return topicRepository.searchTopics(forumId, type, status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 類似トピック検索
     */
    @Transactional(readOnly = true)
    public List<LearningTopicDto> findSimilarTopics(Long topicId, String title, LearningTopic.TopicType type) {
        return topicRepository.findSimilarTopics(topicId, title, type).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * ユーザーの関心トピック取得
     */
    @Transactional(readOnly = true)
    public List<LearningTopicDto> getTopicsOfInterest(UUID userId) {
        return topicRepository.findTopicsOfInterest(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // モデレーション機能

    /**
     * モデレーション待ちトピック取得
     */
    @Transactional(readOnly = true)
    public List<LearningTopicDto> getTopicsRequiringModeration() {
        return topicRepository.findTopicsRequiringModeration().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * モデレーション承認
     */
    public LearningTopicDto approveModeration(Long id, UUID moderatorId, String notes) {
        LearningTopic topic = topicRepository.findById(id)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("トピックが見つかりません: " + id));

        topic.completeModerationApproval(moderatorId, notes);
        LearningTopic savedTopic = topicRepository.save(topic);
        return convertToDto(savedTopic);
    }

    /**
     * モデレーション拒否
     */
    public LearningTopicDto rejectModeration(Long id, UUID moderatorId, String notes) {
        LearningTopic topic = topicRepository.findById(id)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("トピックが見つかりません: " + id));

        topic.rejectModeration(moderatorId, notes);
        LearningTopic savedTopic = topicRepository.save(topic);
        return convertToDto(savedTopic);
    }

    // 統計・分析機能

    /**
     * 全体統計取得
     */
    @Transactional(readOnly = true)
    public TopicStatistics getOverallStatistics() {
        Object[] stats = topicRepository.getOverallStatistics();
        return new TopicStatistics(stats);
    }

    /**
     * フォーラム別統計取得
     */
    @Transactional(readOnly = true)
    public ForumTopicStatistics getForumStatistics(Long forumId) {
        Object[] stats = topicRepository.getForumStatistics(forumId);
        return new ForumTopicStatistics(stats);
    }

    /**
     * 期間別統計取得
     */
    @Transactional(readOnly = true)
    public PeriodTopicStatistics getPeriodStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        Object[] stats = topicRepository.getPeriodStatistics(startDate, endDate);
        return new PeriodTopicStatistics(stats);
    }

    /**
     * 解決率計算
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateSolutionRate(LocalDateTime startDate, LocalDateTime endDate) {
        Double rate = topicRepository.calculateSolutionRate(startDate, endDate);
        return rate != null ? BigDecimal.valueOf(rate).setScale(2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    // バッチ処理機能

    /**
     * 自動クローズ処理
     */
    public int processAutoCloseTopics() {
        List<LearningTopic> topicsToClose = topicRepository.findTopicsForAutoClose(LocalDateTime.now());
        int processedCount = 0;

        for (LearningTopic topic : topicsToClose) {
            try {
                topic.close(null, "自動クローズ - 指定日時に達しました");
                topicRepository.save(topic);
                processedCount++;
            } catch (Exception e) {
                // ログに記録（実装省略）
                System.err.println("自動クローズ処理エラー: トピックID " + topic.getId() + " - " + e.getMessage());
            }
        }

        return processedCount;
    }

    /**
     * 全トピックスコア再計算
     */
    public int recalculateAllScores() {
        List<LearningTopic> allTopics = topicRepository.findAllForScoreRecalculation();
        int processedCount = 0;

        for (LearningTopic topic : allTopics) {
            try {
                BigDecimal activityScore = topic.calculateActivityScore();
                BigDecimal qualityScore = topic.calculateQualityScore();
                BigDecimal popularityScore = topic.calculatePopularityScore();
                BigDecimal overallScore = topic.calculateOverallScore();

                topicRepository.updateScores(topic.getId(), activityScore, qualityScore, popularityScore, overallScore);
                processedCount++;
            } catch (Exception e) {
                // ログに記録（実装省略）
                System.err.println("スコア再計算エラー: トピックID " + topic.getId() + " - " + e.getMessage());
            }
        }

        return processedCount;
    }

    /**
     * 非アクティブトピック検出
     */
    @Transactional(readOnly = true)
    public List<LearningTopicDto> findInactiveTopics(int days) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(days);
        return topicRepository.findInactiveTopics(threshold).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 長期未解決の質問取得
     */
    @Transactional(readOnly = true)
    public List<LearningTopicDto> findLongUnsolvedQuestions(int days) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(days);
        return topicRepository.findLongUnsolvedQuestions(threshold).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // DTO変換

    /**
     * エンティティをDTOに変換
     */
    private LearningTopicDto convertToDto(LearningTopic topic) {
        LearningTopicDto dto = new LearningTopicDto();
        BeanUtils.copyProperties(topic, dto);

        // 計算フィールドの更新
        dto.updateCalculatedFields();

        return dto;
    }

    // 統計クラス

    /**
     * トピック統計
     */
    public static class TopicStatistics {
        private final Long totalTopics;
        private final Long activeTopics;
        private final Long solvedQuestions;
        private final Long unsolvedQuestions;
        private final Double avgViewCount;
        private final Double avgCommentCount;
        private final Double avgLikeCount;

        public TopicStatistics(Object[] stats) {
            this.totalTopics = ((Number) stats[0]).longValue();
            this.activeTopics = ((Number) stats[1]).longValue();
            this.solvedQuestions = ((Number) stats[2]).longValue();
            this.unsolvedQuestions = ((Number) stats[3]).longValue();
            this.avgViewCount = stats[4] != null ? ((Number) stats[4]).doubleValue() : 0.0;
            this.avgCommentCount = stats[5] != null ? ((Number) stats[5]).doubleValue() : 0.0;
            this.avgLikeCount = stats[6] != null ? ((Number) stats[6]).doubleValue() : 0.0;
        }

        // Getters
        public Long getTotalTopics() {
            return totalTopics;
        }

        public Long getActiveTopics() {
            return activeTopics;
        }

        public Long getSolvedQuestions() {
            return solvedQuestions;
        }

        public Long getUnsolvedQuestions() {
            return unsolvedQuestions;
        }

        public Double getAvgViewCount() {
            return avgViewCount;
        }

        public Double getAvgCommentCount() {
            return avgCommentCount;
        }

        public Double getAvgLikeCount() {
            return avgLikeCount;
        }
    }

    /**
     * フォーラムトピック統計
     */
    public static class ForumTopicStatistics {
        private final Long totalTopics;
        private final Long activeTopics;
        private final Double avgViewCount;
        private final Double avgCommentCount;
        private final LocalDateTime lastActivity;

        public ForumTopicStatistics(Object[] stats) {
            this.totalTopics = ((Number) stats[0]).longValue();
            this.activeTopics = ((Number) stats[1]).longValue();
            this.avgViewCount = stats[2] != null ? ((Number) stats[2]).doubleValue() : 0.0;
            this.avgCommentCount = stats[3] != null ? ((Number) stats[3]).doubleValue() : 0.0;
            this.lastActivity = (LocalDateTime) stats[4];
        }

        // Getters
        public Long getTotalTopics() {
            return totalTopics;
        }

        public Long getActiveTopics() {
            return activeTopics;
        }

        public Double getAvgViewCount() {
            return avgViewCount;
        }

        public Double getAvgCommentCount() {
            return avgCommentCount;
        }

        public LocalDateTime getLastActivity() {
            return lastActivity;
        }
    }

    // ========== 追加のController対応メソッド ==========

    /**
     * トピック取得（例外なし版）
     */
    @Transactional(readOnly = true)
    public LearningTopicDto getTopicById(Long id) {
        return getTopic(id).orElseThrow(() -> new RuntimeException("トピックが見つかりません: " + id));
    }

    /**
     * フォーラム別トピック一覧取得（Page版）
     */
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<LearningTopicDto> getTopicsByForumId(Long forumId,
            org.springframework.data.domain.Pageable pageable) {
        return topicRepository.findByForumIdOrderByLastActivityAtDesc(forumId, pageable)
                .map(this::convertToDto);
    }

    /**
     * ユーザー別トピック一覧取得（Page版）
     */
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<LearningTopicDto> getTopicsByUserId(UUID userId,
            org.springframework.data.domain.Pageable pageable) {
        return topicRepository.findByCreatorIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::convertToDto);
    }

    /**
     * ピン留めトピック取得
     */
    @Transactional(readOnly = true)
    public List<LearningTopicDto> getPinnedTopicsByForumId(Long forumId) {
        return topicRepository.findPinnedByForumId(forumId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 人気トピック取得
     */
    @Transactional(readOnly = true)
    public List<LearningTopicDto> getPopularTopicsByForumId(Long forumId, int limit) {
        return topicRepository.findPopularByForumId(forumId, org.springframework.data.domain.PageRequest.of(0, limit))
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 最新トピック取得
     */
    @Transactional(readOnly = true)
    public List<LearningTopicDto> getLatestTopicsByForumId(Long forumId, int limit) {
        return topicRepository
                .findByForumIdOrderByCreatedAtDesc(forumId, org.springframework.data.domain.PageRequest.of(0, limit))
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 解決済みトピック取得
     */
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<LearningTopicDto> getSolvedTopicsByForumId(Long forumId,
            org.springframework.data.domain.Pageable pageable) {
        return topicRepository.findSolvedByForumId(forumId, pageable)
                .map(this::convertToDto);
    }

    /**
     * 未解決トピック取得
     */
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<LearningTopicDto> getUnsolvedTopicsByForumId(Long forumId,
            org.springframework.data.domain.Pageable pageable) {
        return topicRepository.findUnsolvedByForumId(forumId, pageable)
                .map(this::convertToDto);
    }

    /**
     * いいね切り替え
     */
    public void toggleLike(Long topicId, boolean isLiked) {
        if (isLiked) {
            topicRepository.incrementLikeCount(topicId);
        } else {
            topicRepository.decrementLikeCount(topicId);
        }
    }

    /**
     * ピン留め設定
     */
    public void setPinned(Long topicId, boolean pinned) {
        topicRepository.updatePinStatus(topicId, pinned);
    }

    /**
     * ロック設定
     */
    public void setLocked(Long topicId, boolean locked) {
        topicRepository.updateLockStatus(topicId, locked);
    }

    /**
     * 解決済み設定
     */
    public void setSolved(Long topicId, boolean solved) {
        topicRepository.updateSolvedStatus(topicId, solved);
    }

    /**
     * 推奨設定
     */
    public void setFeatured(Long topicId, boolean featured) {
        topicRepository.updateFeatureStatus(topicId, featured);
    }

    /**
     * 品質スコア更新
     */
    public void updateQualityScore(Long topicId, BigDecimal score) {
        topicRepository.updateQualityScore(topicId, score);
    }

    /**
     * 人気度スコア更新
     */
    public void updatePopularityScore(Long topicId, BigDecimal score) {
        topicRepository.updatePopularityScore(topicId, score);
    }

    /**
     * モデレーション待ちトピック取得
     */
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<LearningTopicDto> getPendingModerationTopics(
            org.springframework.data.domain.Pageable pageable) {
        return topicRepository.findPendingModeration(pageable)
                .map(this::convertToDto);
    }

    /**
     * モデレーションステータス更新
     */
    public void updateModerationStatus(Long topicId, String status, UUID moderatorId) {
        topicRepository.updateModerationStatus(topicId, LearningTopic.ModerationStatus.valueOf(status), moderatorId);
    }

    /**
     * ステータス更新
     */
    public void updateStatus(Long topicId, String status) {
        topicRepository.updateTopicStatus(topicId, LearningTopic.TopicStatus.valueOf(status));
    }

    /**
     * 検索
     */
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<LearningTopicDto> searchTopics(
            Long forumId, String keyword, String status, String priority, UUID creatorId,
            org.springframework.data.domain.Pageable pageable) {

        LearningTopic.TopicStatus topicStatus = status != null ? LearningTopic.TopicStatus.valueOf(status) : null;
        LearningTopic.TopicPriority topicPriority = priority != null ? LearningTopic.TopicPriority.valueOf(priority)
                : null;

        return topicRepository.searchTopics(forumId, keyword, topicStatus, topicPriority, creatorId, pageable)
                .map(this::convertToDto);
    }

    /**
     * コンテンツ検索
     */
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<LearningTopicDto> searchByContent(Long forumId, String keyword,
            org.springframework.data.domain.Pageable pageable) {
        return topicRepository.searchByContent(forumId, keyword, pageable)
                .map(this::convertToDto);
    }

    /**
     * フォーラム別トピック数取得
     */
    @Transactional(readOnly = true)
    public Long getTopicCountByForumId(Long forumId) {
        return topicRepository.countByForumId(forumId);
    }

    /**
     * ユーザー別トピック数取得
     */
    @Transactional(readOnly = true)
    public Long getTopicCountByUserId(UUID userId) {
        return topicRepository.countByCreatorId(userId);
    }

    /**
     * 日次統計
     */
    @Transactional(readOnly = true)
    public List<java.util.Map<String, Object>> getDailyTopicStatistics(LocalDateTime fromDate, LocalDateTime toDate) {
        return topicRepository.getTopicCountByDateRange(fromDate, toDate);
    }

    /**
     * フォーラム統計
     */
    @Transactional(readOnly = true)
    public List<java.util.Map<String, Object>> getForumStatistics() {
        return topicRepository.getForumStatistics();
    }

    /**
     * 関連トピック取得
     */
    @Transactional(readOnly = true)
    public List<LearningTopicDto> getRelatedTopics(Long topicId, int limit) {
        return topicRepository.findRelatedTopics(topicId, org.springframework.data.domain.PageRequest.of(0, limit))
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * トレンディングトピック取得（オーバーライド）
     */
    @Transactional(readOnly = true)
    public List<LearningTopicDto> getTrendingTopics(int limit) {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return topicRepository.findTrendingTopics(since)
                .stream()
                .limit(limit)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * アクティブな討論取得
     */
    @Transactional(readOnly = true)
    public List<LearningTopicDto> getActiveDiscussions(Long forumId, LocalDateTime sinceDate) {
        return topicRepository.findActiveDiscussions(forumId, sinceDate)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 古いトピックのアーカイブ
     */
    public void archiveOldTopics(LocalDateTime cutoffDate) {
        topicRepository.archiveOldTopics(cutoffDate);
    }

    /**
     * 非アクティブトピックのマーク
     */
    public void markInactiveTopics(LocalDateTime cutoffDate) {
        topicRepository.markInactiveTopics(cutoffDate);
    }

    /**
     * 期間トピック統計
     */
    public static class PeriodTopicStatistics {
        private final Long totalTopics;
        private final Long activeTopics;
        private final Long solvedTopics;
        private final Double avgScore;

        public PeriodTopicStatistics(Object[] stats) {
            this.totalTopics = ((Number) stats[0]).longValue();
            this.activeTopics = ((Number) stats[1]).longValue();
            this.solvedTopics = ((Number) stats[2]).longValue();
            this.avgScore = stats[3] != null ? ((Number) stats[3]).doubleValue() : 0.0;
        }

        // Getters
        public Long getTotalTopics() {
            return totalTopics;
        }

        public Long getActiveTopics() {
            return activeTopics;
        }

        public Long getSolvedTopics() {
            return solvedTopics;
        }

        public Double getAvgScore() {
            return avgScore;
        }
    }
}
