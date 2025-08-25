package com.sfr.tokyo.sfr_backend.repository.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningRanking;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningRanking.RankingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 学習ランキングリポジトリ
 */
@Repository
public interface LearningRankingRepository extends JpaRepository<LearningRanking, Long> {

    /**
     * 指定されたランキングタイプのランキング一覧を取得（順位順）
     */
    List<LearningRanking> findByRankingTypeOrderByRankPosition(RankingType rankingType);

    /**
     * スペース特定のランキング一覧を取得
     */
    List<LearningRanking> findBySpaceIdAndRankingTypeOrderByRankPosition(Long spaceId, RankingType rankingType);

    /**
     * グローバルランキング一覧を取得（スペース特定なし）
     */
    List<LearningRanking> findBySpaceIdIsNullAndRankingTypeOrderByRankPosition(RankingType rankingType);

    /**
     * ユーザーの特定ランキングタイプでの順位を取得
     */
    Optional<LearningRanking> findByUserIdAndRankingType(UUID userId, RankingType rankingType);

    /**
     * ユーザーのスペース特定ランキングでの順位を取得
     */
    Optional<LearningRanking> findByUserIdAndSpaceIdAndRankingType(UUID userId, Long spaceId, RankingType rankingType);

    /**
     * ユーザーの全ランキング情報を取得
     */
    List<LearningRanking> findByUserId(UUID userId);

    /**
     * ユーザーのスペース内全ランキング情報を取得
     */
    List<LearningRanking> findByUserIdAndSpaceId(UUID userId, Long spaceId);

    /**
     * 上位N位のランキングを取得
     */
    List<LearningRanking> findTop10ByRankingTypeOrderByRankPosition(RankingType rankingType);

    /**
     * スペース特定の上位N位のランキングを取得
     */
    List<LearningRanking> findTop10BySpaceIdAndRankingTypeOrderByRankPosition(Long spaceId, RankingType rankingType);

    /**
     * ランク範囲でのランキングを取得
     */
    @Query("SELECT lr FROM LearningRanking lr WHERE lr.rankingType = :rankingType AND lr.rankPosition BETWEEN :startRank AND :endRank ORDER BY lr.rankPosition")
    List<LearningRanking> findRankingsByRange(@Param("rankingType") RankingType rankingType,
            @Param("startRank") Integer startRank,
            @Param("endRank") Integer endRank);

    /**
     * スペース特定のランク範囲でのランキングを取得
     */
    @Query("SELECT lr FROM LearningRanking lr WHERE lr.spaceId = :spaceId AND lr.rankingType = :rankingType AND lr.rankPosition BETWEEN :startRank AND :endRank ORDER BY lr.rankPosition")
    List<LearningRanking> findSpaceRankingsByRange(@Param("spaceId") Long spaceId,
            @Param("rankingType") RankingType rankingType,
            @Param("startRank") Integer startRank,
            @Param("endRank") Integer endRank);

    /**
     * 最近更新されたランキングを取得
     */
    @Query("SELECT lr FROM LearningRanking lr WHERE lr.updatedAt >= :since ORDER BY lr.updatedAt DESC")
    List<LearningRanking> findRecentlyUpdatedRankings(@Param("since") LocalDateTime since);

    /**
     * 指定された期間のランキング変動履歴を取得
     */
    @Query("SELECT lr FROM LearningRanking lr WHERE lr.userId = :userId AND lr.updatedAt BETWEEN :startDate AND :endDate ORDER BY lr.updatedAt DESC")
    List<LearningRanking> findUserRankingHistory(@Param("userId") UUID userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * スコア範囲でのランキングを取得
     */
    @Query("SELECT lr FROM LearningRanking lr WHERE lr.rankingType = :rankingType AND lr.score >= :minScore ORDER BY lr.score DESC")
    List<LearningRanking> findRankingsByMinScore(@Param("rankingType") RankingType rankingType,
            @Param("minScore") java.math.BigDecimal minScore);

    /**
     * 活発ユーザーのランキングを取得（最近活動しているユーザー）
     */
    @Query("SELECT lr FROM LearningRanking lr WHERE lr.rankingType = :rankingType AND lr.lastActivity >= :since ORDER BY lr.rankPosition")
    List<LearningRanking> findActiveUserRankings(@Param("rankingType") RankingType rankingType,
            @Param("since") LocalDateTime since);

    /**
     * ランキングタイプ別の総ユーザー数をカウント
     */
    long countByRankingType(RankingType rankingType);

    /**
     * スペース特定のランキングタイプ別総ユーザー数をカウント
     */
    long countBySpaceIdAndRankingType(Long spaceId, RankingType rankingType);

    /**
     * ユーザーより上位のランクユーザー数をカウント
     */
    @Query("SELECT COUNT(lr) FROM LearningRanking lr WHERE lr.rankingType = :rankingType AND lr.rankPosition < :userRank")
    long countUsersAboveRank(@Param("rankingType") RankingType rankingType, @Param("userRank") Integer userRank);

    /**
     * ユーザーより下位のランクユーザー数をカウント
     */
    @Query("SELECT COUNT(lr) FROM LearningRanking lr WHERE lr.rankingType = :rankingType AND lr.rankPosition > :userRank")
    long countUsersBelowRank(@Param("rankingType") RankingType rankingType, @Param("userRank") Integer userRank);

    /**
     * 指定されたスコア以上のユーザー数をカウント
     */
    @Query("SELECT COUNT(lr) FROM LearningRanking lr WHERE lr.rankingType = :rankingType AND lr.score >= :minScore")
    long countUsersWithMinScore(@Param("rankingType") RankingType rankingType,
            @Param("minScore") java.math.BigDecimal minScore);

    /**
     * ランキングデータの一括削除（古いデータ削除用）
     */
    @Query("DELETE FROM LearningRanking lr WHERE lr.updatedAt < :cutoffDate")
    void deleteOldRankings(@Param("cutoffDate") LocalDateTime cutoffDate);
}
