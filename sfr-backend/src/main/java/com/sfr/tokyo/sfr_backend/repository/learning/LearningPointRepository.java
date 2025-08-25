package com.sfr.tokyo.sfr_backend.repository.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningPoint;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningPoint.PointType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ポイントリポジトリ
 */
@Repository
public interface LearningPointRepository extends JpaRepository<LearningPoint, Long> {

    /**
     * ユーザーの指定されたポイントタイプを取得
     */
    Optional<LearningPoint> findByUserIdAndPointType(UUID userId, PointType pointType);

    /**
     * ユーザーのスペース特定ポイントタイプを取得
     */
    Optional<LearningPoint> findByUserIdAndSpaceIdAndPointType(UUID userId, Long spaceId, PointType pointType);

    /**
     * ユーザーの全ポイント取得
     */
    List<LearningPoint> findByUserId(UUID userId);

    /**
     * ユーザーのスペース内ポイント取得
     */
    List<LearningPoint> findByUserIdAndSpaceId(UUID userId, Long spaceId);

    /**
     * ユーザーのグローバルポイント取得（スペース特定なし）
     */
    List<LearningPoint> findByUserIdAndSpaceIdIsNull(UUID userId);

    /**
     * 指定されたスペースの全ユーザーポイント取得
     */
    List<LearningPoint> findBySpaceId(Long spaceId);

    /**
     * ユーザーの総ポイント（TOTALタイプ）を取得
     */
    @Query("SELECT lp FROM LearningPoint lp WHERE lp.userId = :userId AND lp.pointType = 'TOTAL' AND lp.spaceId IS NULL")
    Optional<LearningPoint> findUserTotalPoints(@Param("userId") UUID userId);

    /**
     * ユーザーのスペース総ポイントを取得
     */
    @Query("SELECT lp FROM LearningPoint lp WHERE lp.userId = :userId AND lp.spaceId = :spaceId AND lp.pointType = 'TOTAL'")
    Optional<LearningPoint> findUserSpaceTotalPoints(@Param("userId") UUID userId, @Param("spaceId") Long spaceId);

    /**
     * ポイント数でランキング取得（グローバル総ポイント）
     */
    @Query("SELECT lp FROM LearningPoint lp WHERE lp.pointType = 'TOTAL' AND lp.spaceId IS NULL ORDER BY lp.points DESC")
    List<LearningPoint> findGlobalPointsRanking();

    /**
     * スペースポイントランキング取得
     */
    @Query("SELECT lp FROM LearningPoint lp WHERE lp.spaceId = :spaceId AND lp.pointType = 'TOTAL' ORDER BY lp.points DESC")
    List<LearningPoint> findSpacePointsRanking(@Param("spaceId") Long spaceId);

    /**
     * 上位N名のポイントランキング取得
     */
    List<LearningPoint> findTop10ByPointTypeAndSpaceIdIsNullOrderByPointsDesc(PointType pointType);

    /**
     * ユーザーの最近更新されたポイント取得
     */
    @Query("SELECT lp FROM LearningPoint lp WHERE lp.userId = :userId AND lp.updatedAt >= :since ORDER BY lp.updatedAt DESC")
    List<LearningPoint> findRecentPointUpdates(@Param("userId") UUID userId, @Param("since") LocalDateTime since);

    /**
     * 指定された期間のポイント更新履歴取得
     */
    @Query("SELECT lp FROM LearningPoint lp WHERE lp.userId = :userId AND lp.updatedAt BETWEEN :startDate AND :endDate ORDER BY lp.updatedAt DESC")
    List<LearningPoint> findPointUpdatesByPeriod(@Param("userId") UUID userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 指定値以上のポイントを持つユーザー数をカウント
     */
    @Query("SELECT COUNT(lp) FROM LearningPoint lp WHERE lp.pointType = 'TOTAL' AND lp.points >= :minPoints AND lp.spaceId IS NULL")
    long countUsersWithMinPoints(@Param("minPoints") Integer minPoints);

    /**
     * スペース内で指定値以上のポイントを持つユーザー数をカウント
     */
    @Query("SELECT COUNT(lp) FROM LearningPoint lp WHERE lp.spaceId = :spaceId AND lp.pointType = 'TOTAL' AND lp.points >= :minPoints")
    long countSpaceUsersWithMinPoints(@Param("spaceId") Long spaceId, @Param("minPoints") Integer minPoints);

    /**
     * ユーザーのポイントタイプ別集計取得
     */
    @Query("SELECT lp.pointType, SUM(lp.points) FROM LearningPoint lp WHERE lp.userId = :userId GROUP BY lp.pointType")
    List<Object[]> findUserPointsSummary(@Param("userId") UUID userId);
}
