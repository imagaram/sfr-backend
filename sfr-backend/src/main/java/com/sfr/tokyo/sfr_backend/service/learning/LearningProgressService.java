package com.sfr.tokyo.sfr_backend.service.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningProgressDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningContent;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningProgress;
import com.sfr.tokyo.sfr_backend.mapper.learning.LearningProgressMapper;
import com.sfr.tokyo.sfr_backend.repository.learning.LearningContentRepository;
import com.sfr.tokyo.sfr_backend.repository.learning.LearningProgressRepository;
import com.sfr.tokyo.sfr_backend.repository.UserRepository;
import com.sfr.tokyo.sfr_backend.user.User;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class LearningProgressService {

    private final LearningProgressRepository progressRepository;
    private final LearningContentRepository contentRepository;
    private final UserRepository userRepository;
    private final LearningProgressMapper progressMapper;

    public LearningProgressService(LearningProgressRepository progressRepository,
            LearningContentRepository contentRepository,
            UserRepository userRepository,
            LearningProgressMapper progressMapper) {
        this.progressRepository = progressRepository;
        this.contentRepository = contentRepository;
        this.userRepository = userRepository;
        this.progressMapper = progressMapper;
    }

    /**
     * 学習進捗登録・更新
     */
    public LearningProgressDto saveProgress(UUID userId, LearningProgressDto progressDto) {
        // ユーザー存在確認
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("ユーザーが見つかりません: " + userId));

        // コンテンツ存在確認
        LearningContent content = contentRepository.findById(progressDto.getContentId())
                .orElseThrow(() -> new EntityNotFoundException("学習コンテンツが見つかりません: " + progressDto.getContentId()));

        // 既存進捗確認
        Optional<LearningProgress> existingProgress = progressRepository.findByUserIdAndLearningContentId(userId,
                progressDto.getContentId());

        LearningProgress progress;
        if (existingProgress.isPresent()) {
            // 既存進捗の更新
            progress = existingProgress.get();
            progress.updateProgress(progressDto.getProgressPercent());
        } else {
            // 新規進捗作成
            progress = new LearningProgress(user, content, progressDto.getProgressPercent());
        }

        LearningProgress savedProgress = progressRepository.save(progress);
        return progressMapper.toDto(savedProgress);
    }

    /**
     * ユーザーの全進捗取得
     */
    @Transactional(readOnly = true)
    public List<LearningProgressDto> getUserProgress(UUID userId) {
        // ユーザー存在確認
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("ユーザーが見つかりません: " + userId);
        }

        List<LearningProgress> progressList = progressRepository.findByUserIdWithContent(userId);
        return progressMapper.toDtoList(progressList);
    }

    /**
     * ユーザーの学習空間内進捗取得
     */
    @Transactional(readOnly = true)
    public List<LearningProgressDto> getUserProgressBySpace(UUID userId, Long spaceId) {
        // ユーザー存在確認
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("ユーザーが見つかりません: " + userId);
        }

        List<LearningProgress> progressList = progressRepository.findByUserIdAndSpaceId(userId, spaceId);
        return progressMapper.toDtoList(progressList);
    }

    /**
     * 特定コンテンツの進捗取得
     */
    @Transactional(readOnly = true)
    public Optional<LearningProgressDto> getProgressByUserAndContent(UUID userId, Long contentId) {
        Optional<LearningProgress> progress = progressRepository.findByUserIdAndLearningContentId(userId, contentId);

        return progress.map(progressMapper::toDto);
    }

    /**
     * ユーザーの学習統計取得
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserLearningStats(UUID userId) {
        // ユーザー存在確認
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("ユーザーが見つかりません: " + userId);
        }

        Long completedCount = progressRepository.countCompletedByUserId(userId);
        Optional<BigDecimal> averageProgress = progressRepository.findAverageProgressByUserId(userId);
        List<LearningProgress> allProgress = progressRepository.findByUserIdWithContent(userId);
        List<LearningProgress> incompleteProgress = progressRepository.findIncompleteByUserId(userId);

        return Map.of(
                "totalContentCount", allProgress.size(),
                "completedCount", completedCount,
                "incompleteCount", incompleteProgress.size(),
                "averageProgress", averageProgress.orElse(BigDecimal.ZERO),
                "completionRate",
                allProgress.isEmpty() ? 0 : (completedCount.doubleValue() / allProgress.size()) * 100);
    }

    /**
     * コンテンツの学習統計取得
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getContentLearningStats(Long contentId) {
        // コンテンツ存在確認
        if (!contentRepository.existsById(contentId)) {
            throw new EntityNotFoundException("学習コンテンツが見つかりません: " + contentId);
        }

        Long totalLearners = progressRepository.countByContentId(contentId);
        Long completedLearners = progressRepository.countCompletedByContentId(contentId);

        return Map.of(
                "totalLearners", totalLearners,
                "completedLearners", completedLearners,
                "completionRate", totalLearners == 0 ? 0 : (completedLearners.doubleValue() / totalLearners) * 100);
    }

    /**
     * 学習空間内進捗ランキング取得
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getProgressRanking(Long spaceId) {
        List<Object[]> rankingData = progressRepository.findProgressRankingBySpaceId(spaceId);

        return rankingData.stream()
                .map(data -> Map.of(
                        "userId", data[0],
                        "completedCount", data[1]))
                .collect(Collectors.toList());
    }

    /**
     * 指定期間の学習進捗取得
     */
    @Transactional(readOnly = true)
    public List<LearningProgressDto> getProgressByDateRange(UUID userId,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        // ユーザー存在確認
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("ユーザーが見つかりません: " + userId);
        }

        List<LearningProgress> progressList = progressRepository.findByUserIdAndDateRange(userId, startDate, endDate);

        return progressMapper.toDtoList(progressList);
    }

    /**
     * 未完了の学習進捗取得
     */
    @Transactional(readOnly = true)
    public List<LearningProgressDto> getIncompleteProgress(UUID userId) {
        // ユーザー存在確認
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("ユーザーが見つかりません: " + userId);
        }

        List<LearningProgress> progressList = progressRepository.findIncompleteByUserId(userId);
        return progressMapper.toDtoList(progressList);
    }

    /**
     * 進捗削除
     */
    public void deleteProgress(UUID userId, Long contentId) {
        LearningProgress progress = progressRepository.findByUserIdAndLearningContentId(userId, contentId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "指定されたユーザーとコンテンツの進捗が見つかりません: userId=" + userId + ", contentId=" + contentId));

        progressRepository.delete(progress);
    }
}
