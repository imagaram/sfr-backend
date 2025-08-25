package com.sfr.tokyo.sfr_backend.repository.crypto;

import com.sfr.tokyo.sfr_backend.entity.crypto.RewardDistribution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * RewardDistributionRepository
 * SFR 報酬配布のデータアクセス層
 * 報酬配布履歴、統計、配布管理などを提供
 */
@Repository
public interface RewardDistributionRepository
        extends JpaRepository<RewardDistribution, Long>, JpaSpecificationExecutor<RewardDistribution> {

    // ===== 基本検索メソッド =====

    /**
     * ユーザーIDで報酬配布履歴を検索
     */
    List<RewardDistribution> findByUserId(UUID userId);

    /**
     * ユーザーIDで報酬配布履歴をページネーション付きで検索
     */
    Page<RewardDistribution> findByUserId(UUID userId, Pageable pageable);

    /**
     * スペースIDで報酬配布履歴を検索
     */
    List<RewardDistribution> findBySpaceId(Long spaceId);

    /**
     * スペースIDで報酬配布履歴をページネーション付きで検索
     */
    Page<RewardDistribution> findBySpaceId(Long spaceId, Pageable pageable);

    /**
     * ユーザーIDとスペースIDで報酬配布履歴を検索
     */
    List<RewardDistribution> findByUserIdAndSpaceId(UUID userId, Long spaceId);

    /**
     * 報酬タイプで検索
     */
    List<RewardDistribution> findByCategory(RewardDistribution.RewardCategory category);

    /**
     * 複数のユーザーIDで報酬配布履歴を検索
     */
    List<RewardDistribution> findByUserIdIn(List<UUID> userIds);

    // ===== 金額・日時ベース検索 =====

    /**
     * 指定金額以上の報酬配布を検索
     */
    @Query("SELECT rd FROM RewardDistribution rd WHERE rd.amount >= :minAmount ORDER BY rd.amount DESC")
    List<RewardDistribution> findByAmountGreaterThanEqual(@Param("minAmount") BigDecimal minAmount, Pageable pageable);

    /**
     * 金額範囲で報酬配布を検索
     */
    @Query("SELECT rd FROM RewardDistribution rd WHERE rd.amount BETWEEN :minAmount AND :maxAmount ORDER BY rd.amount DESC")
    Page<RewardDistribution> findByAmountBetween(@Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            Pageable pageable);

    /**
     * 配布日時範囲で検索
     */
    @Query("SELECT rd FROM RewardDistribution rd WHERE rd.distributionDate BETWEEN :startDate AND :endDate ORDER BY rd.distributionDate DESC")
    Page<RewardDistribution> findByDistributedAtBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * 指定日時以降の報酬配布を検索
     */
    @Query("SELECT rd FROM RewardDistribution rd WHERE rd.distributionDate >= :since ORDER BY rd.distributionDate DESC")
    Page<RewardDistribution> findByDistributedAtAfter(@Param("since") LocalDateTime since, Pageable pageable);

    // ===== ユーザー別統計メソッド =====

    /**
     * ユーザーの総報酬金額を計算
     */
    @Query("SELECT SUM(rd.amount) FROM RewardDistribution rd WHERE rd.userId = :userId")
    BigDecimal getTotalRewardsByUser(@Param("userId") UUID userId);

    /**
     * ユーザーの平均報酬金額を計算
     */
    @Query("SELECT AVG(rd.amount) FROM RewardDistribution rd WHERE rd.userId = :userId")
    BigDecimal getAverageRewardsByUser(@Param("userId") UUID userId);

    /**
     * ユーザーの報酬配布回数をカウント
     */
    @Query("SELECT COUNT(rd) FROM RewardDistribution rd WHERE rd.userId = :userId")
    Long countRewardsByUser(@Param("userId") UUID userId);

    /**
     * ユーザーの特定期間の報酬合計を計算
     */
    @Query("SELECT SUM(rd.amount) FROM RewardDistribution rd WHERE rd.userId = :userId " +
            "AND rd.distributionDate BETWEEN :startDate AND :endDate")
    BigDecimal getUserRewardsByPeriod(@Param("userId") UUID userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * ユーザーの報酬タイプ別統計を取得
     */
    @Query("SELECT rd.category, COUNT(rd), SUM(rd.amount), AVG(rd.amount) " +
            "FROM RewardDistribution rd WHERE rd.userId = :userId " +
            "GROUP BY rd.category ORDER BY SUM(rd.amount) DESC")
    List<Object[]> getUserRewardTypeStatistics(@Param("userId") UUID userId);

    // ===== スペース別統計メソッド =====

    /**
     * スペースの総報酬配布金額を計算
     */
    @Query("SELECT SUM(rd.amount) FROM RewardDistribution rd WHERE rd.spaceId = :spaceId")
    BigDecimal getTotalRewardsBySpace(@Param("spaceId") Long spaceId);

    /**
     * スペースの平均報酬配布金額を計算
     */
    @Query("SELECT AVG(rd.amount) FROM RewardDistribution rd WHERE rd.spaceId = :spaceId")
    BigDecimal getAverageRewardsBySpace(@Param("spaceId") Long spaceId);

    /**
     * スペースの報酬配布回数をカウント
     */
    @Query("SELECT COUNT(rd) FROM RewardDistribution rd WHERE rd.spaceId = :spaceId")
    Long countRewardsBySpace(@Param("spaceId") Long spaceId);

    /**
     * スペースの特定期間の報酬配布統計を取得
     */
    @Query("SELECT SUM(rd.amount), AVG(rd.amount), COUNT(rd) FROM RewardDistribution rd " +
            "WHERE rd.spaceId = :spaceId AND rd.distributionDate BETWEEN :startDate AND :endDate")
    Object[] getSpaceRewardStatsByPeriod(@Param("spaceId") Long spaceId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // ===== 報酬タイプ別統計 =====

    /**
     * 報酬タイプ別の統計情報を取得
     */
    @Query("SELECT rd.category, COUNT(rd), SUM(rd.amount), AVG(rd.amount) " +
            "FROM RewardDistribution rd GROUP BY rd.category ORDER BY SUM(rd.amount) DESC")
    List<Object[]> getRewardTypeStatistics();

    /**
     * 特定報酬タイプの詳細統計を取得
     */
    @Query("SELECT COUNT(rd) as rewardCount, " +
            "SUM(rd.amount) as totalAmount, " +
            "AVG(rd.amount) as averageAmount, " +
            "MAX(rd.amount) as maxAmount, " +
            "MIN(rd.amount) as minAmount " +
            "FROM RewardDistribution rd WHERE rd.category = :category")
    Object[] getDetailedRewardTypeStatistics(@Param("category") RewardDistribution.RewardCategory category);

    // ===== 時間ベース集計 =====

    /**
     * 日別報酬配布統計を取得
     */
    @Query("SELECT DATE(rd.distributionDate) as date, COUNT(rd), SUM(rd.amount), AVG(rd.amount) " +
            "FROM RewardDistribution rd WHERE rd.distributionDate >= :since " +
            "GROUP BY DATE(rd.distributionDate) ORDER BY DATE(rd.distributionDate) DESC")
    List<Object[]> getDailyRewardStatistics(@Param("since") LocalDateTime since);

    /**
     * 月別報酬配布統計を取得
     */
    @Query("SELECT YEAR(rd.distributionDate), MONTH(rd.distributionDate), COUNT(rd), SUM(rd.amount), AVG(rd.amount) " +
            "FROM RewardDistribution rd WHERE rd.distributionDate >= :since " +
            "GROUP BY YEAR(rd.distributionDate), MONTH(rd.distributionDate) " +
            "ORDER BY YEAR(rd.distributionDate) DESC, MONTH(rd.distributionDate) DESC")
    List<Object[]> getMonthlyRewardStatistics(@Param("since") LocalDateTime since);

    // ===== ランキング・トップユーザー =====

    /**
     * 総報酬金額でユーザーランキングを取得
     */
    @Query("SELECT rd.userId, SUM(rd.amount) as totalReward FROM RewardDistribution rd " +
            "GROUP BY rd.userId ORDER BY totalReward DESC")
    Page<Object[]> getUserRewardRanking(Pageable pageable);

    /**
     * 特定期間の報酬金額でユーザーランキングを取得
     */
    @Query("SELECT rd.userId, SUM(rd.amount) as totalReward FROM RewardDistribution rd " +
            "WHERE rd.distributionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY rd.userId ORDER BY totalReward DESC")
    Page<Object[]> getUserRewardRankingByPeriod(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * スペース別報酬配布ランキングを取得
     */
    @Query("SELECT rd.spaceId, SUM(rd.amount) as totalReward FROM RewardDistribution rd " +
            "GROUP BY rd.spaceId ORDER BY totalReward DESC")
    Page<Object[]> getSpaceRewardRanking(Pageable pageable);

    // ===== 高度な分析クエリ =====

    /**
     * ユーザーの報酬配布トレンドを取得
     */
    @Query("SELECT DATE(rd.distributionDate) as date, SUM(rd.amount) as dailyReward " +
            "FROM RewardDistribution rd WHERE rd.userId = :userId " +
            "AND rd.distributionDate >= :since " +
            "GROUP BY DATE(rd.distributionDate) ORDER BY DATE(rd.distributionDate) ASC")
    List<Object[]> getUserRewardTrend(@Param("userId") UUID userId, @Param("since") LocalDateTime since);

    /**
     * 報酬タイプ別の期間比較統計を取得
     */
    @Query("SELECT rd.category, " +
            "SUM(CASE WHEN rd.distributionDate BETWEEN :period1Start AND :period1End THEN rd.amount ELSE 0 END) as period1Total, "
            +
            "SUM(CASE WHEN rd.distributionDate BETWEEN :period2Start AND :period2End THEN rd.amount ELSE 0 END) as period2Total "
            +
            "FROM RewardDistribution rd " +
            "WHERE rd.distributionDate BETWEEN :period1Start AND :period2End " +
            "GROUP BY rd.category")
    List<Object[]> getRewardTypeComparison(@Param("period1Start") LocalDateTime period1Start,
            @Param("period1End") LocalDateTime period1End,
            @Param("period2Start") LocalDateTime period2Start,
            @Param("period2End") LocalDateTime period2End);

    // ===== 複合条件検索 =====

    /**
     * 複合条件で報酬配布を検索（ユーザー + 金額範囲 + 期間）
     */
    @Query("SELECT rd FROM RewardDistribution rd WHERE " +
            "rd.userId = :userId AND " +
            "rd.amount BETWEEN :minAmount AND :maxAmount AND " +
            "rd.distributionDate BETWEEN :startDate AND :endDate " +
            "ORDER BY rd.distributionDate DESC")
    Page<RewardDistribution> findByComplexConditions(@Param("userId") UUID userId,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * スペースと報酬タイプでの複合検索
     */
    @Query("SELECT rd FROM RewardDistribution rd WHERE " +
            "rd.spaceId = :spaceId AND " +
            "rd.category = :category AND " +
            "rd.distributionDate >= :since " +
            "ORDER BY rd.amount DESC")
    Page<RewardDistribution> findBySpaceAndTypeAndDate(@Param("spaceId") Long spaceId,
            @Param("category") RewardDistribution.RewardCategory category,
            @Param("since") LocalDateTime since,
            Pageable pageable);

    // ===== カスタムネイティブクエリ =====

    /**
     * 報酬配布の詳細ランキング（パフォーマンス最適化）
     */
    @Query(value = """
            SELECT rd.*,
                   ROW_NUMBER() OVER (ORDER BY rd.amount DESC) as amount_rank,
                   DENSE_RANK() OVER (ORDER BY rd.amount DESC) as amount_dense_rank
            FROM reward_distributions rd
            WHERE rd.distributed_at >= :since
            ORDER BY rd.amount DESC
            """, nativeQuery = true)
    List<Object[]> getDetailedRewardRanking(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * 報酬金額分布のヒストグラムデータを取得
     */
    @Query(value = """
            SELECT
                CASE
                    WHEN amount = 0 THEN '0'
                    WHEN amount BETWEEN 0.01 AND 10 THEN '0.01-10'
                    WHEN amount BETWEEN 10.01 AND 100 THEN '10-100'
                    WHEN amount BETWEEN 100.01 AND 1000 THEN '100-1K'
                    WHEN amount BETWEEN 1000.01 AND 10000 THEN '1K-10K'
                    ELSE '10K+'
                END as amount_range,
                COUNT(*) as distribution_count,
                SUM(amount) as total_amount,
                AVG(amount) as average_amount
            FROM reward_distributions
            GROUP BY amount_range
            ORDER BY MIN(amount)
            """, nativeQuery = true)
    List<Object[]> getRewardAmountDistribution();

    /**
     * ユーザー報酬配布パフォーマンス分析
     */
    @Query(value = """
            SELECT
                user_id,
                COUNT(*) as total_distributions,
                SUM(amount) as total_amount,
                AVG(amount) as avg_amount,
                MIN(amount) as min_amount,
                MAX(amount) as max_amount,
                STDDEV(amount) as amount_stddev
            FROM reward_distributions
            WHERE distributed_at >= :since
            GROUP BY user_id
            HAVING COUNT(*) >= :minDistributions
            ORDER BY total_amount DESC
            """, nativeQuery = true)
    List<Object[]> getUserRewardPerformanceAnalysis(@Param("since") LocalDateTime since,
            @Param("minDistributions") int minDistributions,
            Pageable pageable);
}
