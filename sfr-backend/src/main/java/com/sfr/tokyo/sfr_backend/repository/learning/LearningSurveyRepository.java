package com.sfr.tokyo.sfr_backend.repository.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningSurvey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 学習アンケート リポジトリ - LearningSurveyRepository
 */
@Repository
public interface LearningSurveyRepository extends JpaRepository<LearningSurvey, Long> {

    /**
     * ユーザー別アンケート取得
     */
    List<LearningSurvey> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * スペース別アンケート取得
     */
    List<LearningSurvey> findBySpaceIdOrderByCreatedAtDesc(Long spaceId);

    /**
     * コンテンツ別アンケート取得
     */
    List<LearningSurvey> findByContentIdOrderByCreatedAtDesc(Long contentId);

    /**
     * カテゴリ別アンケート取得
     */
    List<LearningSurvey> findBySurveyCategoryOrderByCreatedAtDesc(LearningSurvey.SurveyCategory surveyCategory);

    /**
     * ステータス別アンケート取得
     */
    List<LearningSurvey> findBySurveyStatusOrderByCreatedAtDesc(LearningSurvey.SurveyStatus surveyStatus);

    /**
     * 優先度別アンケート取得
     */
    List<LearningSurvey> findByPriorityLevelOrderByCreatedAtDesc(LearningSurvey.PriorityLevel priorityLevel);

    /**
     * ユーザー・スペース別アンケート取得
     */
    List<LearningSurvey> findByUserIdAndSpaceIdOrderByCreatedAtDesc(UUID userId, Long spaceId);

    /**
     * ユーザー・カテゴリ別アンケート取得
     */
    List<LearningSurvey> findByUserIdAndSurveyCategoryOrderByCreatedAtDesc(UUID userId,
            LearningSurvey.SurveyCategory surveyCategory);

    /**
     * ユーザー・ステータス別アンケート取得
     */
    List<LearningSurvey> findByUserIdAndSurveyStatusOrderByCreatedAtDesc(UUID userId,
            LearningSurvey.SurveyStatus surveyStatus);

    /**
     * 完了済みアンケート取得
     */
    @Query("SELECT ls FROM LearningSurvey ls WHERE ls.surveyStatus = 'COMPLETED' ORDER BY ls.completedAt DESC")
    List<LearningSurvey> findCompletedSurveys();

    /**
     * 未完了アンケート取得
     */
    @Query("SELECT ls FROM LearningSurvey ls WHERE ls.surveyStatus IN ('NOT_STARTED', 'IN_PROGRESS') ORDER BY ls.dueDate ASC NULLS LAST")
    List<LearningSurvey> findIncompleteSurveys();

    /**
     * 期限切れアンケート取得
     */
    @Query("SELECT ls FROM LearningSurvey ls WHERE ls.dueDate < :currentTime AND ls.surveyStatus != 'COMPLETED' ORDER BY ls.dueDate ASC")
    List<LearningSurvey> findExpiredSurveys(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 必須アンケート取得
     */
    List<LearningSurvey> findByIsMandatoryTrueOrderByPriorityLevelDescCreatedAtDesc();

    /**
     * 匿名アンケート取得
     */
    List<LearningSurvey> findByIsAnonymousTrueOrderByCreatedAtDesc();

    /**
     * ユーザーの完了済みアンケート数
     */
    @Query("SELECT COUNT(ls) FROM LearningSurvey ls WHERE ls.userId = :userId AND ls.surveyStatus = 'COMPLETED'")
    int countCompletedSurveysByUser(@Param("userId") UUID userId);

    /**
     * ユーザーの未完了アンケート数
     */
    @Query("SELECT COUNT(ls) FROM LearningSurvey ls WHERE ls.userId = :userId AND ls.surveyStatus IN ('NOT_STARTED', 'IN_PROGRESS')")
    int countIncompleteSurveysByUser(@Param("userId") UUID userId);

    /**
     * カテゴリ別完了数
     */
    @Query("SELECT COUNT(ls) FROM LearningSurvey ls WHERE ls.surveyCategory = :category AND ls.surveyStatus = 'COMPLETED'")
    int countCompletedSurveysByCategory(@Param("category") LearningSurvey.SurveyCategory category);

    /**
     * スペース別完了数
     */
    @Query("SELECT COUNT(ls) FROM LearningSurvey ls WHERE ls.spaceId = :spaceId AND ls.surveyStatus = 'COMPLETED'")
    int countCompletedSurveysBySpace(@Param("spaceId") Long spaceId);

    /**
     * 平均満足度スコア取得
     */
    @Query("SELECT AVG(ls.satisfactionScore) FROM LearningSurvey ls WHERE ls.surveyStatus = 'COMPLETED' AND ls.satisfactionScore IS NOT NULL")
    Optional<BigDecimal> getAverageSatisfactionScore();

    /**
     * カテゴリ別平均満足度スコア
     */
    @Query("SELECT AVG(ls.satisfactionScore) FROM LearningSurvey ls WHERE ls.surveyCategory = :category AND ls.surveyStatus = 'COMPLETED' AND ls.satisfactionScore IS NOT NULL")
    Optional<BigDecimal> getAverageSatisfactionScoreByCategory(
            @Param("category") LearningSurvey.SurveyCategory category);

    /**
     * 平均推奨度スコア取得
     */
    @Query("SELECT AVG(ls.recommendationScore) FROM LearningSurvey ls WHERE ls.surveyStatus = 'COMPLETED' AND ls.recommendationScore IS NOT NULL")
    Optional<BigDecimal> getAverageRecommendationScore();

    /**
     * 平均完了率取得
     */
    @Query("SELECT AVG(ls.completionRate) FROM LearningSurvey ls WHERE ls.completionRate IS NOT NULL")
    Optional<BigDecimal> getAverageCompletionRate();

    /**
     * 平均所要時間取得
     */
    @Query("SELECT AVG(ls.timeSpentMinutes) FROM LearningSurvey ls WHERE ls.surveyStatus = 'COMPLETED' AND ls.timeSpentMinutes IS NOT NULL")
    Optional<BigDecimal> getAverageTimeSpent();

    /**
     * ユーザーの平均満足度スコア
     */
    @Query("SELECT AVG(ls.satisfactionScore) FROM LearningSurvey ls WHERE ls.userId = :userId AND ls.surveyStatus = 'COMPLETED' AND ls.satisfactionScore IS NOT NULL")
    Optional<BigDecimal> getAverageSatisfactionScoreByUser(@Param("userId") UUID userId);

    /**
     * ユーザーの平均完了率
     */
    @Query("SELECT AVG(ls.completionRate) FROM LearningSurvey ls WHERE ls.userId = :userId AND ls.completionRate IS NOT NULL")
    Optional<BigDecimal> getAverageCompletionRateByUser(@Param("userId") UUID userId);

    /**
     * 高評価アンケート取得（満足度4.0以上）
     */
    @Query("SELECT ls FROM LearningSurvey ls WHERE ls.satisfactionScore >= 4.0 AND ls.surveyStatus = 'COMPLETED' ORDER BY ls.satisfactionScore DESC")
    List<LearningSurvey> findHighRatedSurveys();

    /**
     * 低評価アンケート取得（満足度2.5以下）
     */
    @Query("SELECT ls FROM LearningSurvey ls WHERE ls.satisfactionScore <= 2.5 AND ls.surveyStatus = 'COMPLETED' ORDER BY ls.satisfactionScore ASC")
    List<LearningSurvey> findLowRatedSurveys();

    /**
     * 推奨者カテゴリ取得（推奨度9以上）
     */
    @Query("SELECT ls FROM LearningSurvey ls WHERE ls.recommendationScore >= 9.0 AND ls.surveyStatus = 'COMPLETED' ORDER BY ls.recommendationScore DESC")
    List<LearningSurvey> findPromoters();

    /**
     * 批判者カテゴリ取得（推奨度6以下）
     */
    @Query("SELECT ls FROM LearningSurvey ls WHERE ls.recommendationScore <= 6.0 AND ls.surveyStatus = 'COMPLETED' ORDER BY ls.recommendationScore ASC")
    List<LearningSurvey> findDetractors();

    /**
     * フィードバック付きアンケート取得
     */
    @Query("SELECT ls FROM LearningSurvey ls WHERE ls.feedbackText IS NOT NULL AND TRIM(ls.feedbackText) != '' ORDER BY ls.completedAt DESC")
    List<LearningSurvey> findSurveysWithFeedback();

    /**
     * 提案付きアンケート取得
     */
    @Query("SELECT ls FROM LearningSurvey ls WHERE ls.suggestions IS NOT NULL AND TRIM(ls.suggestions) != '' ORDER BY ls.completedAt DESC")
    List<LearningSurvey> findSurveysWithSuggestions();

    /**
     * 期間内完了アンケート取得
     */
    @Query("SELECT ls FROM LearningSurvey ls WHERE ls.completedAt BETWEEN :startDate AND :endDate ORDER BY ls.completedAt DESC")
    List<LearningSurvey> findSurveysCompletedBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 期間内作成アンケート取得
     */
    @Query("SELECT ls FROM LearningSurvey ls WHERE ls.createdAt BETWEEN :startDate AND :endDate ORDER BY ls.createdAt DESC")
    List<LearningSurvey> findSurveysCreatedBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 長時間アンケート取得
     */
    @Query("SELECT ls FROM LearningSurvey ls WHERE ls.timeSpentMinutes > :thresholdMinutes ORDER BY ls.timeSpentMinutes DESC")
    List<LearningSurvey> findLongDurationSurveys(@Param("thresholdMinutes") int thresholdMinutes);

    /**
     * 短時間アンケート取得
     */
    @Query("SELECT ls FROM LearningSurvey ls WHERE ls.timeSpentMinutes < :thresholdMinutes AND ls.surveyStatus = 'COMPLETED' ORDER BY ls.timeSpentMinutes ASC")
    List<LearningSurvey> findShortDurationSurveys(@Param("thresholdMinutes") int thresholdMinutes);

    /**
     * リマインダー送信対象アンケート取得
     */
    @Query("SELECT ls FROM LearningSurvey ls WHERE ls.surveyStatus IN ('NOT_STARTED', 'IN_PROGRESS') AND ls.isMandatory = true AND (ls.lastReminderAt IS NULL OR ls.lastReminderAt < :reminderThreshold) ORDER BY ls.priorityLevel DESC, ls.createdAt ASC")
    List<LearningSurvey> findSurveysForReminder(@Param("reminderThreshold") LocalDateTime reminderThreshold);

    /**
     * 難易度評価統計
     */
    @Query("SELECT AVG(ls.difficultyRating), MIN(ls.difficultyRating), MAX(ls.difficultyRating) FROM LearningSurvey ls WHERE ls.difficultyRating IS NOT NULL AND ls.surveyStatus = 'COMPLETED'")
    Object[] getDifficultyRatingStatistics();

    /**
     * 有用性評価統計
     */
    @Query("SELECT AVG(ls.usefulnessRating), MIN(ls.usefulnessRating), MAX(ls.usefulnessRating) FROM LearningSurvey ls WHERE ls.usefulnessRating IS NOT NULL AND ls.surveyStatus = 'COMPLETED'")
    Object[] getUsefulnessRatingStatistics();

    /**
     * ユーザー別最新アンケート取得
     */
    @Query("SELECT ls FROM LearningSurvey ls WHERE ls.userId = :userId ORDER BY ls.createdAt DESC")
    List<LearningSurvey> findLatestSurveysByUser(@Param("userId") UUID userId);

    /**
     * カテゴリ別最新アンケート取得
     */
    @Query("SELECT ls FROM LearningSurvey ls WHERE ls.surveyCategory = :category ORDER BY ls.createdAt DESC")
    List<LearningSurvey> findLatestSurveysByCategory(@Param("category") LearningSurvey.SurveyCategory category);

    /**
     * タグ検索
     */
    @Query("SELECT ls FROM LearningSurvey ls WHERE ls.tags LIKE %:tag% ORDER BY ls.createdAt DESC")
    List<LearningSurvey> findByTagsContaining(@Param("tag") String tag);

    /**
     * タイトル検索
     */
    @Query("SELECT ls FROM LearningSurvey ls WHERE LOWER(ls.title) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY ls.createdAt DESC")
    List<LearningSurvey> findByTitleContainingIgnoreCase(@Param("keyword") String keyword);

    /**
     * 説明文検索
     */
    @Query("SELECT ls FROM LearningSurvey ls WHERE LOWER(ls.description) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY ls.createdAt DESC")
    List<LearningSurvey> findByDescriptionContainingIgnoreCase(@Param("keyword") String keyword);

    /**
     * フィードバック検索
     */
    @Query("SELECT ls FROM LearningSurvey ls WHERE LOWER(ls.feedbackText) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY ls.completedAt DESC")
    List<LearningSurvey> findByFeedbackContainingIgnoreCase(@Param("keyword") String keyword);

    /**
     * 複合検索（タイトル、説明、フィードバック）
     */
    @Query("SELECT ls FROM LearningSurvey ls WHERE " +
            "LOWER(ls.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(ls.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(ls.feedbackText) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY ls.createdAt DESC")
    List<LearningSurvey> searchByKeyword(@Param("keyword") String keyword);

    /**
     * ユーザー・期間別統計
     */
    @Query("SELECT COUNT(ls), AVG(ls.satisfactionScore), AVG(ls.completionRate) " +
            "FROM LearningSurvey ls " +
            "WHERE ls.userId = :userId AND ls.createdAt BETWEEN :startDate AND :endDate")
    Object[] getUserStatisticsBetween(@Param("userId") UUID userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * カテゴリ・期間別統計
     */
    @Query("SELECT COUNT(ls), AVG(ls.satisfactionScore), AVG(ls.completionRate) " +
            "FROM LearningSurvey ls " +
            "WHERE ls.surveyCategory = :category AND ls.createdAt BETWEEN :startDate AND :endDate")
    Object[] getCategoryStatisticsBetween(@Param("category") LearningSurvey.SurveyCategory category,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 完了率の高い順にアンケート取得
     */
    @Query("SELECT ls FROM LearningSurvey ls WHERE ls.completionRate IS NOT NULL ORDER BY ls.completionRate DESC")
    List<LearningSurvey> findByCompletionRateDesc();

    /**
     * 満足度の高い順にアンケート取得
     */
    @Query("SELECT ls FROM LearningSurvey ls WHERE ls.satisfactionScore IS NOT NULL ORDER BY ls.satisfactionScore DESC")
    List<LearningSurvey> findBySatisfactionScoreDesc();
}
