package com.sfr.tokyo.sfr_backend.service.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningForumDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningForum;
import com.sfr.tokyo.sfr_backend.repository.learning.LearningForumRepository;
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
 * 学習フォーラム サービス - LearningForumService
 */
@Service
@Transactional
public class LearningForumService {

    @Autowired
    private LearningForumRepository learningForumRepository;

    // CRUD操作

    /**
     * フォーラム作成
     */
    public LearningForumDto createForum(LearningForumDto forumDto) {
        LearningForum forum = convertToEntity(forumDto);
        forum.setCreatedAt(LocalDateTime.now());
        forum.setUpdatedAt(LocalDateTime.now());

        LearningForum savedForum = learningForumRepository.save(forum);
        return convertToDto(savedForum);
    }

    /**
     * フォーラム更新
     */
    public LearningForumDto updateForum(Long id, LearningForumDto forumDto) {
        Optional<LearningForum> existingForum = learningForumRepository.findById(id);
        if (existingForum.isEmpty()) {
            throw new RuntimeException("フォーラムが見つかりません: " + id);
        }

        LearningForum forum = existingForum.get();
        updateForumFromDto(forum, forumDto);
        forum.setUpdatedAt(LocalDateTime.now());

        LearningForum savedForum = learningForumRepository.save(forum);
        return convertToDto(savedForum);
    }

    /**
     * フォーラム取得
     */
    @Transactional(readOnly = true)
    public Optional<LearningForumDto> getForum(Long id) {
        return learningForumRepository.findById(id)
                .map(this::convertToDto);
    }

    /**
     * フォーラム削除
     */
    public void deleteForum(Long id) {
        if (!learningForumRepository.existsById(id)) {
            throw new RuntimeException("フォーラムが見つかりません: " + id);
        }
        learningForumRepository.deleteById(id);
    }

    // フォーラム管理

    /**
     * フォーラムピン留め
     */
    public LearningForumDto pinForum(Long id) {
        Optional<LearningForum> forumOpt = learningForumRepository.findById(id);
        if (forumOpt.isEmpty()) {
            throw new RuntimeException("フォーラムが見つかりません: " + id);
        }

        LearningForum forum = forumOpt.get();
        forum.setIsPinned(true);
        forum.setUpdatedAt(LocalDateTime.now());

        LearningForum savedForum = learningForumRepository.save(forum);
        return convertToDto(savedForum);
    }

    /**
     * フォーラムピン留め解除
     */
    public LearningForumDto unpinForum(Long id) {
        Optional<LearningForum> forumOpt = learningForumRepository.findById(id);
        if (forumOpt.isEmpty()) {
            throw new RuntimeException("フォーラムが見つかりません: " + id);
        }

        LearningForum forum = forumOpt.get();
        forum.setIsPinned(false);
        forum.setUpdatedAt(LocalDateTime.now());

        LearningForum savedForum = learningForumRepository.save(forum);
        return convertToDto(savedForum);
    }

    /**
     * フォーラムロック
     */
    public LearningForumDto lockForum(Long id) {
        Optional<LearningForum> forumOpt = learningForumRepository.findById(id);
        if (forumOpt.isEmpty()) {
            throw new RuntimeException("フォーラムが見つかりません: " + id);
        }

        LearningForum forum = forumOpt.get();
        forum.setIsLocked(true);
        forum.setUpdatedAt(LocalDateTime.now());

        LearningForum savedForum = learningForumRepository.save(forum);
        return convertToDto(savedForum);
    }

    /**
     * フォーラムロック解除
     */
    public LearningForumDto unlockForum(Long id) {
        Optional<LearningForum> forumOpt = learningForumRepository.findById(id);
        if (forumOpt.isEmpty()) {
            throw new RuntimeException("フォーラムが見つかりません: " + id);
        }

        LearningForum forum = forumOpt.get();
        forum.setIsLocked(false);
        forum.setUpdatedAt(LocalDateTime.now());

        LearningForum savedForum = learningForumRepository.save(forum);
        return convertToDto(savedForum);
    }

    /**
     * フォーラムアーカイブ
     */
    public LearningForumDto archiveForum(Long id) {
        Optional<LearningForum> forumOpt = learningForumRepository.findById(id);
        if (forumOpt.isEmpty()) {
            throw new RuntimeException("フォーラムが見つかりません: " + id);
        }

        LearningForum forum = forumOpt.get();
        forum.setIsArchived(true);
        forum.setForumStatus(LearningForum.ForumStatus.ARCHIVED);
        forum.setUpdatedAt(LocalDateTime.now());

        LearningForum savedForum = learningForumRepository.save(forum);
        return convertToDto(savedForum);
    }

    /**
     * フォーラムアーカイブ解除
     */
    public LearningForumDto unarchiveForum(Long id) {
        Optional<LearningForum> forumOpt = learningForumRepository.findById(id);
        if (forumOpt.isEmpty()) {
            throw new RuntimeException("フォーラムが見つかりません: " + id);
        }

        LearningForum forum = forumOpt.get();
        forum.setIsArchived(false);
        forum.setForumStatus(LearningForum.ForumStatus.ACTIVE);
        forum.setUpdatedAt(LocalDateTime.now());

        LearningForum savedForum = learningForumRepository.save(forum);
        return convertToDto(savedForum);
    }

    /**
     * フォーラムフィーチャー
     */
    public LearningForumDto featureForum(Long id) {
        Optional<LearningForum> forumOpt = learningForumRepository.findById(id);
        if (forumOpt.isEmpty()) {
            throw new RuntimeException("フォーラムが見つかりません: " + id);
        }

        LearningForum forum = forumOpt.get();
        forum.setIsFeatured(true);
        forum.setUpdatedAt(LocalDateTime.now());

        LearningForum savedForum = learningForumRepository.save(forum);
        return convertToDto(savedForum);
    }

    /**
     * フォーラムフィーチャー解除
     */
    public LearningForumDto unfeatureForum(Long id) {
        Optional<LearningForum> forumOpt = learningForumRepository.findById(id);
        if (forumOpt.isEmpty()) {
            throw new RuntimeException("フォーラムが見つかりません: " + id);
        }

        LearningForum forum = forumOpt.get();
        forum.setIsFeatured(false);
        forum.setUpdatedAt(LocalDateTime.now());

        LearningForum savedForum = learningForumRepository.save(forum);
        return convertToDto(savedForum);
    }

    // 統計・スコア更新

    /**
     * フォーラム統計更新
     */
    public LearningForumDto updateForumStatistics(Long id, int topicCount, int commentCount, int viewCount,
            int subscriberCount) {
        Optional<LearningForum> forumOpt = learningForumRepository.findById(id);
        if (forumOpt.isEmpty()) {
            throw new RuntimeException("フォーラムが見つかりません: " + id);
        }

        LearningForum forum = forumOpt.get();
        forum.updateStatistics(topicCount, commentCount, viewCount, subscriberCount);
        forum.setUpdatedAt(LocalDateTime.now());

        LearningForum savedForum = learningForumRepository.save(forum);
        return convertToDto(savedForum);
    }

    /**
     * 最後のアクティビティ更新
     */
    public LearningForumDto updateLastActivity(Long id, Long topicId, String topicTitle, UUID userId) {
        Optional<LearningForum> forumOpt = learningForumRepository.findById(id);
        if (forumOpt.isEmpty()) {
            throw new RuntimeException("フォーラムが見つかりません: " + id);
        }

        LearningForum forum = forumOpt.get();
        forum.updateLastActivity(topicId, topicTitle, userId);
        forum.setUpdatedAt(LocalDateTime.now());

        LearningForum savedForum = learningForumRepository.save(forum);
        return convertToDto(savedForum);
    }

    /**
     * 閲覧数増加
     */
    public LearningForumDto incrementViewCount(Long id) {
        Optional<LearningForum> forumOpt = learningForumRepository.findById(id);
        if (forumOpt.isEmpty()) {
            throw new RuntimeException("フォーラムが見つかりません: " + id);
        }

        LearningForum forum = forumOpt.get();
        forum.setViewCount(forum.getViewCount() + 1);
        forum.setPopularityScore(forum.calculatePopularityScore());
        forum.setUpdatedAt(LocalDateTime.now());

        LearningForum savedForum = learningForumRepository.save(forum);
        return convertToDto(savedForum);
    }

    /**
     * サブスクライバー数増加
     */
    public LearningForumDto incrementSubscriberCount(Long id) {
        Optional<LearningForum> forumOpt = learningForumRepository.findById(id);
        if (forumOpt.isEmpty()) {
            throw new RuntimeException("フォーラムが見つかりません: " + id);
        }

        LearningForum forum = forumOpt.get();
        forum.setSubscriberCount(forum.getSubscriberCount() + 1);
        forum.setPopularityScore(forum.calculatePopularityScore());
        forum.setQualityScore(forum.calculateQualityScore());
        forum.setUpdatedAt(LocalDateTime.now());

        LearningForum savedForum = learningForumRepository.save(forum);
        return convertToDto(savedForum);
    }

    /**
     * サブスクライバー数減少
     */
    public LearningForumDto decrementSubscriberCount(Long id) {
        Optional<LearningForum> forumOpt = learningForumRepository.findById(id);
        if (forumOpt.isEmpty()) {
            throw new RuntimeException("フォーラムが見つかりません: " + id);
        }

        LearningForum forum = forumOpt.get();
        if (forum.getSubscriberCount() > 0) {
            forum.setSubscriberCount(forum.getSubscriberCount() - 1);
            forum.setPopularityScore(forum.calculatePopularityScore());
            forum.setQualityScore(forum.calculateQualityScore());
            forum.setUpdatedAt(LocalDateTime.now());
        }

        LearningForum savedForum = learningForumRepository.save(forum);
        return convertToDto(savedForum);
    }

    // 検索・取得系

    /**
     * スペース別フォーラム取得
     */
    @Transactional(readOnly = true)
    public List<LearningForumDto> getForumsBySpace(Long spaceId) {
        return learningForumRepository.findBySpaceIdOrderByIsPinnedDescLastActivityAtDesc(spaceId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 作成者別フォーラム取得
     */
    @Transactional(readOnly = true)
    public List<LearningForumDto> getForumsByCreator(UUID creatorId) {
        return learningForumRepository.findByCreatorIdOrderByCreatedAtDesc(creatorId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * カテゴリ別フォーラム取得
     */
    @Transactional(readOnly = true)
    public List<LearningForumDto> getForumsByCategory(LearningForum.ForumCategory category) {
        return learningForumRepository.findByForumCategoryOrderByPopularityScoreDesc(category)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * ステータス別フォーラム取得
     */
    @Transactional(readOnly = true)
    public List<LearningForumDto> getForumsByStatus(LearningForum.ForumStatus status) {
        return learningForumRepository.findByForumStatusOrderByLastActivityAtDesc(status)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * アクティブフォーラム取得
     */
    @Transactional(readOnly = true)
    public List<LearningForumDto> getActiveForums() {
        return learningForumRepository.findActiveForums()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 人気フォーラム取得
     */
    @Transactional(readOnly = true)
    public List<LearningForumDto> getPopularForums() {
        return learningForumRepository.findPopularForums()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * フィーチャードフォーラム取得
     */
    @Transactional(readOnly = true)
    public List<LearningForumDto> getFeaturedForums() {
        return learningForumRepository.findByIsFeaturedTrueOrderByPopularityScoreDesc()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * ピン留めフォーラム取得
     */
    @Transactional(readOnly = true)
    public List<LearningForumDto> getPinnedForums() {
        return learningForumRepository.findByIsPinnedTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 最近アクティブなフォーラム取得
     */
    @Transactional(readOnly = true)
    public List<LearningForumDto> getRecentlyActiveForums(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return learningForumRepository.findRecentlyActiveForums(since)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 高品質フォーラム取得
     */
    @Transactional(readOnly = true)
    public List<LearningForumDto> getHighQualityForums(BigDecimal minScore) {
        return learningForumRepository.findHighQualityForums(minScore)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * トレンドフォーラム取得
     */
    @Transactional(readOnly = true)
    public List<LearningForumDto> getTrendingForums() {
        LocalDateTime recentThreshold = LocalDateTime.now().minusDays(7);
        return learningForumRepository.findTrendingForums(recentThreshold)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 検索機能

    /**
     * キーワード検索
     */
    @Transactional(readOnly = true)
    public List<LearningForumDto> searchForums(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        return learningForumRepository.searchByKeyword(keyword.trim())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * タグ検索
     */
    @Transactional(readOnly = true)
    public List<LearningForumDto> searchByTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            return List.of();
        }
        return learningForumRepository.findByTagsContaining(tag.trim())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 統計・分析系

    /**
     * 全体統計取得
     */
    @Transactional(readOnly = true)
    public ForumStatistics getOverallStatistics() {
        long totalForums = learningForumRepository.count();
        long activeForums = learningForumRepository.findActiveForums().size();

        Optional<BigDecimal> avgPopularity = learningForumRepository.getAveragePopularityScore();
        Optional<BigDecimal> avgActivity = learningForumRepository.getAverageActivityScore();
        Optional<BigDecimal> avgQuality = learningForumRepository.getAverageQualityScore();

        return new ForumStatistics(totalForums, activeForums,
                avgPopularity.orElse(BigDecimal.ZERO),
                avgActivity.orElse(BigDecimal.ZERO),
                avgQuality.orElse(BigDecimal.ZERO));
    }

    /**
     * カテゴリ統計取得
     */
    @Transactional(readOnly = true)
    public CategoryStatistics getCategoryStatistics(LearningForum.ForumCategory category) {
        Object[] stats = learningForumRepository.getCategoryStatistics(category);

        long forumCount = stats[0] != null ? ((Number) stats[0]).longValue() : 0;
        double avgTopics = stats[1] != null ? ((Number) stats[1]).doubleValue() : 0.0;
        double avgComments = stats[2] != null ? ((Number) stats[2]).doubleValue() : 0.0;
        double avgPopularity = stats[3] != null ? ((Number) stats[3]).doubleValue() : 0.0;

        return new CategoryStatistics(category, forumCount,
                BigDecimal.valueOf(avgTopics), BigDecimal.valueOf(avgComments),
                BigDecimal.valueOf(avgPopularity));
    }

    /**
     * スペース統計取得
     */
    @Transactional(readOnly = true)
    public SpaceStatistics getSpaceStatistics(Long spaceId) {
        Object[] stats = learningForumRepository.getSpaceStatistics(spaceId);

        long forumCount = stats[0] != null ? ((Number) stats[0]).longValue() : 0;
        double avgTopics = stats[1] != null ? ((Number) stats[1]).doubleValue() : 0.0;
        double avgComments = stats[2] != null ? ((Number) stats[2]).doubleValue() : 0.0;
        double avgViews = stats[3] != null ? ((Number) stats[3]).doubleValue() : 0.0;

        return new SpaceStatistics(spaceId, forumCount,
                BigDecimal.valueOf(avgTopics), BigDecimal.valueOf(avgComments),
                BigDecimal.valueOf(avgViews));
    }

    // バッチ処理

    /**
     * 自動クローズ処理
     */
    public int processAutoCloseForums() {
        List<LearningForum> forums = learningForumRepository.findAll();
        int processedCount = 0;

        for (LearningForum forum : forums) {
            if (forum.shouldAutoClose()) {
                forum.setForumStatus(LearningForum.ForumStatus.ARCHIVED);
                forum.setIsArchived(true);
                forum.setUpdatedAt(LocalDateTime.now());
                learningForumRepository.save(forum);
                processedCount++;
            }
        }

        return processedCount;
    }

    /**
     * 全フォーラムのスコア再計算
     */
    public int recalculateAllScores() {
        List<LearningForum> forums = learningForumRepository.findAll();
        int processedCount = 0;

        for (LearningForum forum : forums) {
            forum.setActivityScore(forum.calculateActivityScore());
            forum.setQualityScore(forum.calculateQualityScore());
            forum.setPopularityScore(forum.calculatePopularityScore());
            forum.setUpdatedAt(LocalDateTime.now());
            learningForumRepository.save(forum);
            processedCount++;
        }

        return processedCount;
    }

    // Utility Methods

    private LearningForum convertToEntity(LearningForumDto dto) {
        LearningForum entity = new LearningForum();
        entity.setSpaceId(dto.getSpaceId());
        entity.setCreatorId(dto.getCreatorId());
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setForumCategory(dto.getForumCategory());
        entity.setForumStatus(dto.getForumStatus());
        entity.setVisibilityLevel(dto.getVisibilityLevel());
        entity.setModerationLevel(dto.getModerationLevel());
        entity.setTopicCount(dto.getTopicCount() != null ? dto.getTopicCount() : 0);
        entity.setCommentCount(dto.getCommentCount() != null ? dto.getCommentCount() : 0);
        entity.setSubscriberCount(dto.getSubscriberCount() != null ? dto.getSubscriberCount() : 0);
        entity.setViewCount(dto.getViewCount() != null ? dto.getViewCount() : 0);
        entity.setActivityScore(dto.getActivityScore());
        entity.setQualityScore(dto.getQualityScore());
        entity.setPopularityScore(dto.getPopularityScore());
        entity.setTags(dto.getTags());
        entity.setRules(dto.getRules());
        entity.setWelcomeMessage(dto.getWelcomeMessage());
        entity.setIconUrl(dto.getIconUrl());
        entity.setBannerUrl(dto.getBannerUrl());
        entity.setColorScheme(dto.getColorScheme());
        entity.setIsPinned(dto.getIsPinned() != null ? dto.getIsPinned() : false);
        entity.setIsLocked(dto.getIsLocked() != null ? dto.getIsLocked() : false);
        entity.setIsArchived(dto.getIsArchived() != null ? dto.getIsArchived() : false);
        entity.setIsFeatured(dto.getIsFeatured() != null ? dto.getIsFeatured() : false);
        entity.setIsPrivate(dto.getIsPrivate() != null ? dto.getIsPrivate() : false);
        entity.setAllowAnonymous(dto.getAllowAnonymous() != null ? dto.getAllowAnonymous() : true);
        entity.setRequireApproval(dto.getRequireApproval() != null ? dto.getRequireApproval() : false);
        entity.setMaxTopicsPerUser(dto.getMaxTopicsPerUser());
        entity.setMaxCommentsPerTopic(dto.getMaxCommentsPerTopic());
        entity.setAutoCloseDays(dto.getAutoCloseDays());
        entity.setNotificationSettings(dto.getNotificationSettings());
        return entity;
    }

    private LearningForumDto convertToDto(LearningForum entity) {
        LearningForumDto dto = new LearningForumDto();
        dto.setId(entity.getId());
        dto.setSpaceId(entity.getSpaceId());
        dto.setCreatorId(entity.getCreatorId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setForumCategory(entity.getForumCategory());
        dto.setForumStatus(entity.getForumStatus());
        dto.setVisibilityLevel(entity.getVisibilityLevel());
        dto.setModerationLevel(entity.getModerationLevel());
        dto.setTopicCount(entity.getTopicCount());
        dto.setCommentCount(entity.getCommentCount());
        dto.setSubscriberCount(entity.getSubscriberCount());
        dto.setViewCount(entity.getViewCount());
        dto.setActivityScore(entity.getActivityScore());
        dto.setQualityScore(entity.getQualityScore());
        dto.setPopularityScore(entity.getPopularityScore());
        dto.setLastTopicId(entity.getLastTopicId());
        dto.setLastTopicTitle(entity.getLastTopicTitle());
        dto.setLastActivityUserId(entity.getLastActivityUserId());
        dto.setLastActivityAt(entity.getLastActivityAt());
        dto.setTags(entity.getTags());
        dto.setRules(entity.getRules());
        dto.setWelcomeMessage(entity.getWelcomeMessage());
        dto.setIconUrl(entity.getIconUrl());
        dto.setBannerUrl(entity.getBannerUrl());
        dto.setColorScheme(entity.getColorScheme());
        dto.setIsPinned(entity.getIsPinned());
        dto.setIsLocked(entity.getIsLocked());
        dto.setIsArchived(entity.getIsArchived());
        dto.setIsFeatured(entity.getIsFeatured());
        dto.setIsPrivate(entity.getIsPrivate());
        dto.setAllowAnonymous(entity.getAllowAnonymous());
        dto.setRequireApproval(entity.getRequireApproval());
        dto.setMaxTopicsPerUser(entity.getMaxTopicsPerUser());
        dto.setMaxCommentsPerTopic(entity.getMaxCommentsPerTopic());
        dto.setAutoCloseDays(entity.getAutoCloseDays());
        dto.setNotificationSettings(entity.getNotificationSettings());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    private void updateForumFromDto(LearningForum entity, LearningForumDto dto) {
        if (dto.getTitle() != null)
            entity.setTitle(dto.getTitle());
        if (dto.getDescription() != null)
            entity.setDescription(dto.getDescription());
        if (dto.getForumCategory() != null)
            entity.setForumCategory(dto.getForumCategory());
        if (dto.getForumStatus() != null)
            entity.setForumStatus(dto.getForumStatus());
        if (dto.getVisibilityLevel() != null)
            entity.setVisibilityLevel(dto.getVisibilityLevel());
        if (dto.getModerationLevel() != null)
            entity.setModerationLevel(dto.getModerationLevel());
        if (dto.getTags() != null)
            entity.setTags(dto.getTags());
        if (dto.getRules() != null)
            entity.setRules(dto.getRules());
        if (dto.getWelcomeMessage() != null)
            entity.setWelcomeMessage(dto.getWelcomeMessage());
        if (dto.getIconUrl() != null)
            entity.setIconUrl(dto.getIconUrl());
        if (dto.getBannerUrl() != null)
            entity.setBannerUrl(dto.getBannerUrl());
        if (dto.getColorScheme() != null)
            entity.setColorScheme(dto.getColorScheme());
        if (dto.getIsPinned() != null)
            entity.setIsPinned(dto.getIsPinned());
        if (dto.getIsLocked() != null)
            entity.setIsLocked(dto.getIsLocked());
        if (dto.getIsArchived() != null)
            entity.setIsArchived(dto.getIsArchived());
        if (dto.getIsFeatured() != null)
            entity.setIsFeatured(dto.getIsFeatured());
        if (dto.getIsPrivate() != null)
            entity.setIsPrivate(dto.getIsPrivate());
        if (dto.getAllowAnonymous() != null)
            entity.setAllowAnonymous(dto.getAllowAnonymous());
        if (dto.getRequireApproval() != null)
            entity.setRequireApproval(dto.getRequireApproval());
        if (dto.getMaxTopicsPerUser() != null)
            entity.setMaxTopicsPerUser(dto.getMaxTopicsPerUser());
        if (dto.getMaxCommentsPerTopic() != null)
            entity.setMaxCommentsPerTopic(dto.getMaxCommentsPerTopic());
        if (dto.getAutoCloseDays() != null)
            entity.setAutoCloseDays(dto.getAutoCloseDays());
        if (dto.getNotificationSettings() != null)
            entity.setNotificationSettings(dto.getNotificationSettings());
    }

    // 統計クラス
    public static class ForumStatistics {
        private final long totalForums;
        private final long activeForums;
        private final BigDecimal averagePopularityScore;
        private final BigDecimal averageActivityScore;
        private final BigDecimal averageQualityScore;

        public ForumStatistics(long totalForums, long activeForums,
                BigDecimal averagePopularityScore, BigDecimal averageActivityScore,
                BigDecimal averageQualityScore) {
            this.totalForums = totalForums;
            this.activeForums = activeForums;
            this.averagePopularityScore = averagePopularityScore;
            this.averageActivityScore = averageActivityScore;
            this.averageQualityScore = averageQualityScore;
        }

        // Getters
        public long getTotalForums() {
            return totalForums;
        }

        public long getActiveForums() {
            return activeForums;
        }

        public BigDecimal getAveragePopularityScore() {
            return averagePopularityScore;
        }

        public BigDecimal getAverageActivityScore() {
            return averageActivityScore;
        }

        public BigDecimal getAverageQualityScore() {
            return averageQualityScore;
        }
    }

    public static class CategoryStatistics {
        private final LearningForum.ForumCategory category;
        private final long forumCount;
        private final BigDecimal averageTopicCount;
        private final BigDecimal averageCommentCount;
        private final BigDecimal averagePopularityScore;

        public CategoryStatistics(LearningForum.ForumCategory category, long forumCount,
                BigDecimal averageTopicCount, BigDecimal averageCommentCount,
                BigDecimal averagePopularityScore) {
            this.category = category;
            this.forumCount = forumCount;
            this.averageTopicCount = averageTopicCount;
            this.averageCommentCount = averageCommentCount;
            this.averagePopularityScore = averagePopularityScore;
        }

        // Getters
        public LearningForum.ForumCategory getCategory() {
            return category;
        }

        public long getForumCount() {
            return forumCount;
        }

        public BigDecimal getAverageTopicCount() {
            return averageTopicCount;
        }

        public BigDecimal getAverageCommentCount() {
            return averageCommentCount;
        }

        public BigDecimal getAveragePopularityScore() {
            return averagePopularityScore;
        }
    }

    public static class SpaceStatistics {
        private final Long spaceId;
        private final long forumCount;
        private final BigDecimal averageTopicCount;
        private final BigDecimal averageCommentCount;
        private final BigDecimal averageViewCount;

        public SpaceStatistics(Long spaceId, long forumCount,
                BigDecimal averageTopicCount, BigDecimal averageCommentCount,
                BigDecimal averageViewCount) {
            this.spaceId = spaceId;
            this.forumCount = forumCount;
            this.averageTopicCount = averageTopicCount;
            this.averageCommentCount = averageCommentCount;
            this.averageViewCount = averageViewCount;
        }

        // Getters
        public Long getSpaceId() {
            return spaceId;
        }

        public long getForumCount() {
            return forumCount;
        }

        public BigDecimal getAverageTopicCount() {
            return averageTopicCount;
        }

        public BigDecimal getAverageCommentCount() {
            return averageCommentCount;
        }

        public BigDecimal getAverageViewCount() {
            return averageViewCount;
        }
    }
}
