package com.sfr.tokyo.sfr_backend.service.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningSurveyDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningSurvey;
import com.sfr.tokyo.sfr_backend.repository.learning.LearningSurveyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 学習アンケート サービス - LearningSurveyService
 */
@Service
@Transactional
public class LearningSurveyService {

    @Autowired
    private LearningSurveyRepository learningSurveyRepository;

    // CRUD操作

    /**
     * アンケート作成
     */
    public LearningSurveyDto createSurvey(LearningSurveyDto surveyDto) {
        LearningSurvey survey = convertToEntity(surveyDto);
        survey.setSurveyStatus(LearningSurvey.SurveyStatus.NOT_STARTED);
        survey.setAnsweredQuestions(0);
        survey.setCompletionRate(BigDecimal.ZERO);
        survey.setTimeSpentMinutes(0);
        survey.setReminderCount(0);
        survey.setCreatedAt(LocalDateTime.now());
        survey.setUpdatedAt(LocalDateTime.now());

        LearningSurvey savedSurvey = learningSurveyRepository.save(survey);
        return convertToDto(savedSurvey);
    }

    /**
     * アンケート更新
     */
    public LearningSurveyDto updateSurvey(Long id, LearningSurveyDto surveyDto) {
        Optional<LearningSurvey> existingSurvey = learningSurveyRepository.findById(id);
        if (existingSurvey.isEmpty()) {
            throw new RuntimeException("アンケートが見つかりません: " + id);
        }

        LearningSurvey survey = existingSurvey.get();
        updateSurveyFromDto(survey, surveyDto);
        survey.setUpdatedAt(LocalDateTime.now());

        LearningSurvey savedSurvey = learningSurveyRepository.save(survey);
        return convertToDto(savedSurvey);
    }

    /**
     * アンケート取得
     */
    @Transactional(readOnly = true)
    public Optional<LearningSurveyDto> getSurvey(Long id) {
        return learningSurveyRepository.findById(id)
                .map(this::convertToDto);
    }

    /**
     * アンケート削除
     */
    public void deleteSurvey(Long id) {
        if (!learningSurveyRepository.existsById(id)) {
            throw new RuntimeException("アンケートが見つかりません: " + id);
        }
        learningSurveyRepository.deleteById(id);
    }

    // アンケートライフサイクル管理

    /**
     * アンケート開始
     */
    public LearningSurveyDto startSurvey(Long id) {
        Optional<LearningSurvey> surveyOpt = learningSurveyRepository.findById(id);
        if (surveyOpt.isEmpty()) {
            throw new RuntimeException("アンケートが見つかりません: " + id);
        }

        LearningSurvey survey = surveyOpt.get();

        if (survey.getSurveyStatus() != LearningSurvey.SurveyStatus.NOT_STARTED) {
            throw new RuntimeException("アンケートは既に開始されているか完了しています");
        }

        survey.setSurveyStatus(LearningSurvey.SurveyStatus.IN_PROGRESS);
        if (survey.getStartedAt() == null) {
            survey.setStartedAt(LocalDateTime.now());
        }
        survey.setUpdatedAt(LocalDateTime.now());

        LearningSurvey savedSurvey = learningSurveyRepository.save(survey);
        return convertToDto(savedSurvey);
    }

    /**
     * アンケート中断
     */
    public LearningSurveyDto suspendSurvey(Long id) {
        Optional<LearningSurvey> surveyOpt = learningSurveyRepository.findById(id);
        if (surveyOpt.isEmpty()) {
            throw new RuntimeException("アンケートが見つかりません: " + id);
        }

        LearningSurvey survey = surveyOpt.get();

        if (survey.getSurveyStatus() != LearningSurvey.SurveyStatus.IN_PROGRESS) {
            throw new RuntimeException("進行中のアンケートのみ中断できます");
        }

        survey.setSurveyStatus(LearningSurvey.SurveyStatus.CANCELLED);
        survey.setUpdatedAt(LocalDateTime.now());

        LearningSurvey savedSurvey = learningSurveyRepository.save(survey);
        return convertToDto(savedSurvey);
    }

    /**
     * アンケート完了
     */
    public LearningSurveyDto completeSurvey(Long id) {
        Optional<LearningSurvey> surveyOpt = learningSurveyRepository.findById(id);
        if (surveyOpt.isEmpty()) {
            throw new RuntimeException("アンケートが見つかりません: " + id);
        }

        LearningSurvey survey = surveyOpt.get();
        survey.setSurveyStatus(LearningSurvey.SurveyStatus.COMPLETED);
        survey.setCompletedAt(LocalDateTime.now());
        survey.setCompletionRate(BigDecimal.valueOf(100));
        survey.setUpdatedAt(LocalDateTime.now());

        LearningSurvey savedSurvey = learningSurveyRepository.save(survey);
        return convertToDto(savedSurvey);
    }

    /**
     * アンケートスキップ
     */
    public LearningSurveyDto skipSurvey(Long id, String reason) {
        Optional<LearningSurvey> surveyOpt = learningSurveyRepository.findById(id);
        if (surveyOpt.isEmpty()) {
            throw new RuntimeException("アンケートが見つかりません: " + id);
        }

        LearningSurvey survey = surveyOpt.get();

        if (survey.getIsMandatory() != null && survey.getIsMandatory()) {
            throw new RuntimeException("必須アンケートはスキップできません");
        }

        survey.setSurveyStatus(LearningSurvey.SurveyStatus.SKIPPED);
        if (reason != null && !reason.trim().isEmpty()) {
            survey.setFeedbackText(reason);
        }
        survey.setUpdatedAt(LocalDateTime.now());

        LearningSurvey savedSurvey = learningSurveyRepository.save(survey);
        return convertToDto(savedSurvey);
    }

    /**
     * アンケート期限切れ
     */
    public LearningSurveyDto expireSurvey(Long id) {
        Optional<LearningSurvey> surveyOpt = learningSurveyRepository.findById(id);
        if (surveyOpt.isEmpty()) {
            throw new RuntimeException("アンケートが見つかりません: " + id);
        }

        LearningSurvey survey = surveyOpt.get();
        survey.setSurveyStatus(LearningSurvey.SurveyStatus.EXPIRED);
        survey.setUpdatedAt(LocalDateTime.now());

        LearningSurvey savedSurvey = learningSurveyRepository.save(survey);
        return convertToDto(savedSurvey);
    }

    // 進捗管理

    /**
     * 回答進捗更新
     */
    public LearningSurveyDto updateProgress(Long id, int answeredQuestions) {
        Optional<LearningSurvey> surveyOpt = learningSurveyRepository.findById(id);
        if (surveyOpt.isEmpty()) {
            throw new RuntimeException("アンケートが見つかりません: " + id);
        }

        LearningSurvey survey = surveyOpt.get();
        survey.setAnsweredQuestions(answeredQuestions);

        // 完了率計算
        if (survey.getTotalQuestions() != null && survey.getTotalQuestions() > 0) {
            BigDecimal rate = BigDecimal.valueOf(answeredQuestions)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(survey.getTotalQuestions()), 2, RoundingMode.HALF_UP);
            survey.setCompletionRate(rate);
        }

        survey.setUpdatedAt(LocalDateTime.now());

        LearningSurvey savedSurvey = learningSurveyRepository.save(survey);
        return convertToDto(savedSurvey);
    }

    /**
     * 時間更新
     */
    public LearningSurveyDto updateTimeSpent(Long id, int timeSpentMinutes) {
        Optional<LearningSurvey> surveyOpt = learningSurveyRepository.findById(id);
        if (surveyOpt.isEmpty()) {
            throw new RuntimeException("アンケートが見つかりません: " + id);
        }

        LearningSurvey survey = surveyOpt.get();
        survey.setTimeSpentMinutes(timeSpentMinutes);
        survey.setUpdatedAt(LocalDateTime.now());

        LearningSurvey savedSurvey = learningSurveyRepository.save(survey);
        return convertToDto(savedSurvey);
    }

    /**
     * 評価スコア更新
     */
    public LearningSurveyDto updateRatings(Long id, BigDecimal satisfactionScore,
            BigDecimal difficultyRating, BigDecimal usefulnessRating,
            BigDecimal recommendationScore) {
        Optional<LearningSurvey> surveyOpt = learningSurveyRepository.findById(id);
        if (surveyOpt.isEmpty()) {
            throw new RuntimeException("アンケートが見つかりません: " + id);
        }

        LearningSurvey survey = surveyOpt.get();

        if (satisfactionScore != null) {
            survey.setSatisfactionScore(satisfactionScore);
        }
        if (difficultyRating != null) {
            survey.setDifficultyRating(difficultyRating);
        }
        if (usefulnessRating != null) {
            survey.setUsefulnessRating(usefulnessRating);
        }
        if (recommendationScore != null) {
            survey.setRecommendationScore(recommendationScore);
        }

        survey.setUpdatedAt(LocalDateTime.now());

        LearningSurvey savedSurvey = learningSurveyRepository.save(survey);
        return convertToDto(savedSurvey);
    }

    /**
     * フィードバック更新
     */
    public LearningSurveyDto updateFeedback(Long id, String feedbackText, String suggestions) {
        Optional<LearningSurvey> surveyOpt = learningSurveyRepository.findById(id);
        if (surveyOpt.isEmpty()) {
            throw new RuntimeException("アンケートが見つかりません: " + id);
        }

        LearningSurvey survey = surveyOpt.get();

        if (feedbackText != null) {
            survey.setFeedbackText(feedbackText);
        }
        if (suggestions != null) {
            survey.setSuggestions(suggestions);
        }

        survey.setUpdatedAt(LocalDateTime.now());

        LearningSurvey savedSurvey = learningSurveyRepository.save(survey);
        return convertToDto(savedSurvey);
    }

    // 検索・取得系

    /**
     * ユーザー別アンケート取得
     */
    @Transactional(readOnly = true)
    public List<LearningSurveyDto> getSurveysByUser(UUID userId) {
        return learningSurveyRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * スペース別アンケート取得
     */
    @Transactional(readOnly = true)
    public List<LearningSurveyDto> getSurveysBySpace(Long spaceId) {
        return learningSurveyRepository.findBySpaceIdOrderByCreatedAtDesc(spaceId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * カテゴリ別アンケート取得
     */
    @Transactional(readOnly = true)
    public List<LearningSurveyDto> getSurveysByCategory(LearningSurvey.SurveyCategory category) {
        return learningSurveyRepository.findBySurveyCategoryOrderByCreatedAtDesc(category)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * ステータス別アンケート取得
     */
    @Transactional(readOnly = true)
    public List<LearningSurveyDto> getSurveysByStatus(LearningSurvey.SurveyStatus status) {
        return learningSurveyRepository.findBySurveyStatusOrderByCreatedAtDesc(status)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 完了済みアンケート取得
     */
    @Transactional(readOnly = true)
    public List<LearningSurveyDto> getCompletedSurveys() {
        return learningSurveyRepository.findCompletedSurveys()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 未完了アンケート取得
     */
    @Transactional(readOnly = true)
    public List<LearningSurveyDto> getIncompleteSurveys() {
        return learningSurveyRepository.findIncompleteSurveys()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 期限切れアンケート取得
     */
    @Transactional(readOnly = true)
    public List<LearningSurveyDto> getExpiredSurveys() {
        return learningSurveyRepository.findExpiredSurveys(LocalDateTime.now())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 必須アンケート取得
     */
    @Transactional(readOnly = true)
    public List<LearningSurveyDto> getMandatorySurveys() {
        return learningSurveyRepository.findByIsMandatoryTrueOrderByPriorityLevelDescCreatedAtDesc()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 統計・分析系

    /**
     * ユーザー統計取得
     */
    @Transactional(readOnly = true)
    public UserSurveyStatistics getUserStatistics(UUID userId) {
        int completedCount = learningSurveyRepository.countCompletedSurveysByUser(userId);
        int incompleteCount = learningSurveyRepository.countIncompleteSurveysByUser(userId);

        Optional<BigDecimal> avgSatisfaction = learningSurveyRepository.getAverageSatisfactionScoreByUser(userId);
        Optional<BigDecimal> avgCompletion = learningSurveyRepository.getAverageCompletionRateByUser(userId);

        return new UserSurveyStatistics(userId, completedCount, incompleteCount,
                avgSatisfaction.orElse(BigDecimal.ZERO),
                avgCompletion.orElse(BigDecimal.ZERO));
    }

    /**
     * 全体統計取得
     */
    @Transactional(readOnly = true)
    public OverallSurveyStatistics getOverallStatistics() {
        Optional<BigDecimal> avgSatisfaction = learningSurveyRepository.getAverageSatisfactionScore();
        Optional<BigDecimal> avgRecommendation = learningSurveyRepository.getAverageRecommendationScore();
        Optional<BigDecimal> avgCompletion = learningSurveyRepository.getAverageCompletionRate();
        Optional<BigDecimal> avgTimeSpent = learningSurveyRepository.getAverageTimeSpent();

        return new OverallSurveyStatistics(
                avgSatisfaction.orElse(BigDecimal.ZERO),
                avgRecommendation.orElse(BigDecimal.ZERO),
                avgCompletion.orElse(BigDecimal.ZERO),
                avgTimeSpent.orElse(BigDecimal.ZERO));
    }

    /**
     * 高評価アンケート取得
     */
    @Transactional(readOnly = true)
    public List<LearningSurveyDto> getHighRatedSurveys() {
        return learningSurveyRepository.findHighRatedSurveys()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 低評価アンケート取得
     */
    @Transactional(readOnly = true)
    public List<LearningSurveyDto> getLowRatedSurveys() {
        return learningSurveyRepository.findLowRatedSurveys()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // リマインダー機能

    /**
     * リマインダー送信対象取得
     */
    @Transactional(readOnly = true)
    public List<LearningSurveyDto> getSurveysForReminder() {
        LocalDateTime reminderThreshold = LocalDateTime.now().minusDays(1); // 1日前
        return learningSurveyRepository.findSurveysForReminder(reminderThreshold)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * リマインダー送信済み更新
     */
    public LearningSurveyDto updateReminderSent(Long id) {
        Optional<LearningSurvey> surveyOpt = learningSurveyRepository.findById(id);
        if (surveyOpt.isEmpty()) {
            throw new RuntimeException("アンケートが見つかりません: " + id);
        }

        LearningSurvey survey = surveyOpt.get();
        survey.setLastReminderAt(LocalDateTime.now());
        survey.setReminderCount(survey.getReminderCount() != null ? survey.getReminderCount() + 1 : 1);
        survey.setUpdatedAt(LocalDateTime.now());

        LearningSurvey savedSurvey = learningSurveyRepository.save(survey);
        return convertToDto(savedSurvey);
    }

    // 検索機能

    /**
     * キーワード検索
     */
    @Transactional(readOnly = true)
    public List<LearningSurveyDto> searchSurveys(String keyword) {
        return learningSurveyRepository.searchByKeyword(keyword)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * タグ検索
     */
    @Transactional(readOnly = true)
    public List<LearningSurveyDto> searchByTag(String tag) {
        return learningSurveyRepository.findByTagsContaining(tag)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // バッチ処理

    /**
     * 期限切れアンケートの一括処理
     */
    public int processExpiredSurveys() {
        List<LearningSurvey> expiredSurveys = learningSurveyRepository.findExpiredSurveys(LocalDateTime.now());
        int processedCount = 0;

        for (LearningSurvey survey : expiredSurveys) {
            if (survey.getSurveyStatus() != LearningSurvey.SurveyStatus.COMPLETED &&
                    survey.getSurveyStatus() != LearningSurvey.SurveyStatus.EXPIRED) {
                survey.setSurveyStatus(LearningSurvey.SurveyStatus.EXPIRED);
                survey.setUpdatedAt(LocalDateTime.now());
                learningSurveyRepository.save(survey);
                processedCount++;
            }
        }

        return processedCount;
    }

    // Utility Methods

    private LearningSurvey convertToEntity(LearningSurveyDto dto) {
        LearningSurvey entity = new LearningSurvey();
        entity.setUserId(dto.getUserId());
        entity.setSpaceId(dto.getSpaceId());
        entity.setContentId(dto.getContentId());
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setSurveyCategory(dto.getSurveyCategory());
        entity.setSurveyStatus(dto.getSurveyStatus());
        entity.setPriorityLevel(dto.getPriorityLevel());
        entity.setTotalQuestions(dto.getTotalQuestions());
        entity.setAnsweredQuestions(dto.getAnsweredQuestions());
        entity.setRequiredQuestions(dto.getRequiredQuestions());
        entity.setCompletionRate(dto.getCompletionRate());
        entity.setSatisfactionScore(dto.getSatisfactionScore());
        entity.setDifficultyRating(dto.getDifficultyRating());
        entity.setUsefulnessRating(dto.getUsefulnessRating());
        entity.setRecommendationScore(dto.getRecommendationScore());
        entity.setTimeSpentMinutes(dto.getTimeSpentMinutes());
        entity.setEstimatedTimeMinutes(dto.getEstimatedTimeMinutes());
        entity.setFeedbackText(dto.getFeedbackText());
        entity.setSuggestions(dto.getSuggestions());
        entity.setTags(dto.getTags());
        entity.setIsAnonymous(dto.getIsAnonymous());
        entity.setIsMandatory(dto.getIsMandatory());
        entity.setReminderCount(dto.getReminderCount());
        entity.setLastReminderAt(dto.getLastReminderAt());
        entity.setStartedAt(dto.getStartedAt());
        entity.setCompletedAt(dto.getCompletedAt());
        entity.setDueDate(dto.getDueDate());
        return entity;
    }

    private LearningSurveyDto convertToDto(LearningSurvey entity) {
        LearningSurveyDto dto = new LearningSurveyDto();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setSpaceId(entity.getSpaceId());
        dto.setContentId(entity.getContentId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setSurveyCategory(entity.getSurveyCategory());
        dto.setSurveyStatus(entity.getSurveyStatus());
        dto.setPriorityLevel(entity.getPriorityLevel());
        dto.setTotalQuestions(entity.getTotalQuestions());
        dto.setAnsweredQuestions(entity.getAnsweredQuestions());
        dto.setRequiredQuestions(entity.getRequiredQuestions());
        dto.setCompletionRate(entity.getCompletionRate());
        dto.setSatisfactionScore(entity.getSatisfactionScore());
        dto.setDifficultyRating(entity.getDifficultyRating());
        dto.setUsefulnessRating(entity.getUsefulnessRating());
        dto.setRecommendationScore(entity.getRecommendationScore());
        dto.setTimeSpentMinutes(entity.getTimeSpentMinutes());
        dto.setEstimatedTimeMinutes(entity.getEstimatedTimeMinutes());
        dto.setFeedbackText(entity.getFeedbackText());
        dto.setSuggestions(entity.getSuggestions());
        dto.setTags(entity.getTags());
        dto.setIsAnonymous(entity.getIsAnonymous());
        dto.setIsMandatory(entity.getIsMandatory());
        dto.setReminderCount(entity.getReminderCount());
        dto.setLastReminderAt(entity.getLastReminderAt());
        dto.setStartedAt(entity.getStartedAt());
        dto.setCompletedAt(entity.getCompletedAt());
        dto.setDueDate(entity.getDueDate());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    private void updateSurveyFromDto(LearningSurvey entity, LearningSurveyDto dto) {
        if (dto.getTitle() != null)
            entity.setTitle(dto.getTitle());
        if (dto.getDescription() != null)
            entity.setDescription(dto.getDescription());
        if (dto.getSurveyCategory() != null)
            entity.setSurveyCategory(dto.getSurveyCategory());
        if (dto.getPriorityLevel() != null)
            entity.setPriorityLevel(dto.getPriorityLevel());
        if (dto.getTotalQuestions() != null)
            entity.setTotalQuestions(dto.getTotalQuestions());
        if (dto.getRequiredQuestions() != null)
            entity.setRequiredQuestions(dto.getRequiredQuestions());
        if (dto.getEstimatedTimeMinutes() != null)
            entity.setEstimatedTimeMinutes(dto.getEstimatedTimeMinutes());
        if (dto.getTags() != null)
            entity.setTags(dto.getTags());
        if (dto.getIsAnonymous() != null)
            entity.setIsAnonymous(dto.getIsAnonymous());
        if (dto.getIsMandatory() != null)
            entity.setIsMandatory(dto.getIsMandatory());
        if (dto.getDueDate() != null)
            entity.setDueDate(dto.getDueDate());
    }

    // 統計クラス
    public static class UserSurveyStatistics {
        private final UUID userId;
        private final int completedCount;
        private final int incompleteCount;
        private final BigDecimal averageSatisfactionScore;
        private final BigDecimal averageCompletionRate;

        public UserSurveyStatistics(UUID userId, int completedCount, int incompleteCount,
                BigDecimal averageSatisfactionScore, BigDecimal averageCompletionRate) {
            this.userId = userId;
            this.completedCount = completedCount;
            this.incompleteCount = incompleteCount;
            this.averageSatisfactionScore = averageSatisfactionScore;
            this.averageCompletionRate = averageCompletionRate;
        }

        // Getters
        public UUID getUserId() {
            return userId;
        }

        public int getCompletedCount() {
            return completedCount;
        }

        public int getIncompleteCount() {
            return incompleteCount;
        }

        public BigDecimal getAverageSatisfactionScore() {
            return averageSatisfactionScore;
        }

        public BigDecimal getAverageCompletionRate() {
            return averageCompletionRate;
        }
    }

    public static class OverallSurveyStatistics {
        private final BigDecimal averageSatisfactionScore;
        private final BigDecimal averageRecommendationScore;
        private final BigDecimal averageCompletionRate;
        private final BigDecimal averageTimeSpent;

        public OverallSurveyStatistics(BigDecimal averageSatisfactionScore, BigDecimal averageRecommendationScore,
                BigDecimal averageCompletionRate, BigDecimal averageTimeSpent) {
            this.averageSatisfactionScore = averageSatisfactionScore;
            this.averageRecommendationScore = averageRecommendationScore;
            this.averageCompletionRate = averageCompletionRate;
            this.averageTimeSpent = averageTimeSpent;
        }

        // Getters
        public BigDecimal getAverageSatisfactionScore() {
            return averageSatisfactionScore;
        }

        public BigDecimal getAverageRecommendationScore() {
            return averageRecommendationScore;
        }

        public BigDecimal getAverageCompletionRate() {
            return averageCompletionRate;
        }

        public BigDecimal getAverageTimeSpent() {
            return averageTimeSpent;
        }
    }
}
