package com.sfr.tokyo.sfr_backend.repository.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LearningProgressRepository extends JpaRepository<LearningProgress, Long> {

    /**
     * ユーザーとコンテンツによる進捗検索
     */
    Optional<LearningProgress> findByUserIdAndLearningContentId(UUID userId, Long contentId);

    /**
     * ユーザーの全進捗取得
     */
    @Query("SELECT lp FROM LearningProgress lp " +
            "JOIN FETCH lp.learningContent lc " +
            "WHERE lp.user.id = :userId " +
            "ORDER BY lp.updatedAt DESC")
    List<LearningProgress> findByUserIdWithContent(@Param("userId") UUID userId);

    /**
     * ユーザーの学習空間内進捗取得
     */
    @Query("SELECT lp FROM LearningProgress lp " +
            "JOIN FETCH lp.learningContent lc " +
            "WHERE lp.user.id = :userId " +
            "AND lc.space.id = :spaceId " +
            "ORDER BY lp.updatedAt DESC")
    List<LearningProgress> findByUserIdAndSpaceId(@Param("userId") UUID userId, @Param("spaceId") Long spaceId);

    /**
     * コンテンツの進捗統計取得
     */
    @Query("SELECT COUNT(lp) FROM LearningProgress lp " +
            "WHERE lp.learningContent.id = :contentId")
    Long countByContentId(@Param("contentId") Long contentId);

    /**
     * コンテンツの完了者数取得
     */
    @Query("SELECT COUNT(lp) FROM LearningProgress lp " +
            "WHERE lp.learningContent.id = :contentId " +
            "AND lp.progressPercent = 100.0")
    Long countCompletedByContentId(@Param("contentId") Long contentId);

    /**
     * ユーザーの完了コンテンツ数取得
     */
    @Query("SELECT COUNT(lp) FROM LearningProgress lp " +
            "WHERE lp.user.id = :userId " +
            "AND lp.progressPercent = 100.0")
    Long countCompletedByUserId(@Param("userId") UUID userId);

    /**
     * ユーザーの平均進捗取得
     */
    @Query("SELECT AVG(lp.progressPercent) FROM LearningProgress lp " +
            "WHERE lp.user.id = :userId")
    Optional<BigDecimal> findAverageProgressByUserId(@Param("userId") UUID userId);

    /**
     * 学習空間内の進捗ランキング取得（完了コンテンツ数ベース）
     */
    @Query("SELECT lp.user.id, COUNT(lp) as completedCount " +
            "FROM LearningProgress lp " +
            "WHERE lp.learningContent.space.id = :spaceId " +
            "AND lp.progressPercent = 100.0 " +
            "GROUP BY lp.user.id " +
            "ORDER BY completedCount DESC")
    List<Object[]> findProgressRankingBySpaceId(@Param("spaceId") Long spaceId);

    /**
     * 指定期間内の進捗取得
     */
    @Query("SELECT lp FROM LearningProgress lp " +
            "WHERE lp.user.id = :userId " +
            "AND lp.updatedAt >= :startDate " +
            "AND lp.updatedAt <= :endDate " +
            "ORDER BY lp.updatedAt DESC")
    List<LearningProgress> findByUserIdAndDateRange(@Param("userId") UUID userId,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * 未完了の進捗取得
     */
    @Query("SELECT lp FROM LearningProgress lp " +
            "JOIN FETCH lp.learningContent lc " +
            "WHERE lp.user.id = :userId " +
            "AND lp.progressPercent < 100.0 " +
            "ORDER BY lp.updatedAt DESC")
    List<LearningProgress> findIncompleteByUserId(@Param("userId") UUID userId);
}
