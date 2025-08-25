package com.sfr.tokyo.sfr_backend.repository.crypto;

import com.sfr.tokyo.sfr_backend.entity.crypto.CollectionHistory;
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
 * CollectionHistoryRepository
 * SFR 収集履歴のデータアクセス層
 * 収集履歴、統計、分析などを提供
 */
@Repository
public interface CollectionHistoryRepository
        extends JpaRepository<CollectionHistory, Long>, JpaSpecificationExecutor<CollectionHistory> {

    // ===== 基本検索メソッド =====

    /**
     * ユーザーIDで収集履歴を検索
     */
    List<CollectionHistory> findByUserId(UUID userId);

    /**
     * ユーザーIDで収集履歴をページネーション付きで検索
     */
    Page<CollectionHistory> findByUserId(UUID userId, Pageable pageable);

    /**
     * スペースIDで収集履歴を検索
     */
    List<CollectionHistory> findBySpaceId(Long spaceId);

    /**
     * スペースIDで収集履歴をページネーション付きで検索
     */
    Page<CollectionHistory> findBySpaceId(Long spaceId, Pageable pageable);

    /**
     * ユーザーIDとスペースIDで収集履歴を検索
     */
    List<CollectionHistory> findByUserIdAndSpaceId(UUID userId, Long spaceId);

    /**
     * 収集理由で検索
     */
    List<CollectionHistory> findByCollectionReason(CollectionHistory.CollectionReason collectionReason);

    /**
     * 複数のユーザーIDで収集履歴を検索
     */
    List<CollectionHistory> findByUserIdIn(List<UUID> userIds);

    // ===== 金額・日時ベース検索 =====

    /**
     * 指定金額以上の収集履歴を検索
     */
    @Query("SELECT ch FROM CollectionHistory ch WHERE ch.collectedAmount >= :minAmount ORDER BY ch.collectedAmount DESC")
    List<CollectionHistory> findByAmountGreaterThanEqual(@Param("minAmount") BigDecimal minAmount, Pageable pageable);

    /**
     * 金額範囲で収集履歴を検索
     */
    @Query("SELECT ch FROM CollectionHistory ch WHERE ch.collectedAmount BETWEEN :minAmount AND :maxAmount ORDER BY ch.collectedAmount DESC")
    Page<CollectionHistory> findByAmountBetween(@Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            Pageable pageable);

    /**
     * 収集日時範囲で検索
     */
    @Query("SELECT ch FROM CollectionHistory ch WHERE ch.collectedAt BETWEEN :startDate AND :endDate ORDER BY ch.collectedAt DESC")
    Page<CollectionHistory> findByCollectedAtBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * 指定日時以降の収集履歴を検索
     */
    @Query("SELECT ch FROM CollectionHistory ch WHERE ch.collectedAt >= :since ORDER BY ch.collectedAt DESC")
    Page<CollectionHistory> findByCollectedAtAfter(@Param("since") LocalDateTime since, Pageable pageable);

    // ===== ユーザー別統計メソッド =====

    /**
     * ユーザーの総収集金額を計算
     */
    @Query("SELECT SUM(ch.collectedAmount) FROM CollectionHistory ch WHERE ch.userId = :userId")
    BigDecimal getTotalCollectionsByUser(@Param("userId") UUID userId);

    /**
     * ユーザーの平均収集金額を計算
     */
    @Query("SELECT AVG(ch.collectedAmount) FROM CollectionHistory ch WHERE ch.userId = :userId")
    BigDecimal getAverageCollectionsByUser(@Param("userId") UUID userId);

    /**
     * ユーザーの収集回数をカウント
     */
    @Query("SELECT COUNT(ch) FROM CollectionHistory ch WHERE ch.userId = :userId")
    Long countCollectionsByUser(@Param("userId") UUID userId);

    /**
     * ユーザーの特定期間の収集合計を計算
     */
    @Query("SELECT SUM(ch.collectedAmount) FROM CollectionHistory ch WHERE ch.userId = :userId " +
            "AND ch.collectedAt BETWEEN :startDate AND :endDate")
    BigDecimal getUserCollectionsByPeriod(@Param("userId") UUID userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * ユーザーの収集タイプ別統計を取得
     */
    @Query("SELECT ch.collectionReason, COUNT(ch), SUM(ch.collectedAmount), AVG(ch.collectedAmount) " +
            "FROM CollectionHistory ch WHERE ch.userId = :userId " +
            "GROUP BY ch.collectionReason ORDER BY SUM(ch.collectedAmount) DESC")
    List<Object[]> getUserCollectionTypeStatistics(@Param("userId") UUID userId);

    // ===== スペース別統計メソッド =====

    /**
     * スペースの総収集金額を計算
     */
    @Query("SELECT SUM(ch.collectedAmount) FROM CollectionHistory ch WHERE ch.spaceId = :spaceId")
    BigDecimal getTotalCollectionsBySpace(@Param("spaceId") Long spaceId);

    /**
     * スペースの平均収集金額を計算
     */
    @Query("SELECT AVG(ch.collectedAmount) FROM CollectionHistory ch WHERE ch.spaceId = :spaceId")
    BigDecimal getAverageCollectionsBySpace(@Param("spaceId") Long spaceId);

    /**
     * スペースの収集回数をカウント
     */
    @Query("SELECT COUNT(ch) FROM CollectionHistory ch WHERE ch.spaceId = :spaceId")
    Long countCollectionsBySpace(@Param("spaceId") Long spaceId);

    /**
     * スペースの特定期間の収集統計を取得
     */
    @Query("SELECT SUM(ch.collectedAmount), AVG(ch.collectedAmount), COUNT(ch) FROM CollectionHistory ch " +
            "WHERE ch.spaceId = :spaceId AND ch.collectedAt BETWEEN :startDate AND :endDate")
    Object[] getSpaceCollectionStatsByPeriod(@Param("spaceId") Long spaceId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // ===== 収集タイプ別統計 =====

    /**
     * 収集タイプ別の統計情報を取得
     */
    @Query("SELECT ch.collectionReason, COUNT(ch), SUM(ch.collectedAmount), AVG(ch.collectedAmount) " +
            "FROM CollectionHistory ch GROUP BY ch.collectionReason ORDER BY SUM(ch.collectedAmount) DESC")
    List<Object[]> getCollectionTypeStatistics();

    /**
     * 特定収集タイプの詳細統計を取得
     */
    @Query("SELECT COUNT(ch) as collectionCount, " +
            "SUM(ch.collectedAmount) as totalAmount, " +
            "AVG(ch.collectedAmount) as averageAmount, " +
            "MAX(ch.collectedAmount) as maxAmount, " +
            "MIN(ch.collectedAmount) as minAmount " +
            "FROM CollectionHistory ch WHERE ch.collectionReason = :collectionType")
    Object[] getDetailedCollectionTypeStatistics(
            @Param("collectionType") CollectionHistory.CollectionReason collectionType);

    // ===== 時間ベース集計 =====

    /**
     * 日別収集統計を取得
     */
    @Query("SELECT DATE(ch.collectedAt) as date, COUNT(ch), SUM(ch.collectedAmount), AVG(ch.collectedAmount) " +
            "FROM CollectionHistory ch WHERE ch.collectedAt >= :since " +
            "GROUP BY DATE(ch.collectedAt) ORDER BY DATE(ch.collectedAt) DESC")
    List<Object[]> getDailyCollectionStatistics(@Param("since") LocalDateTime since);

    /**
     * 月別収集統計を取得
     */
    @Query("SELECT YEAR(ch.collectedAt), MONTH(ch.collectedAt), COUNT(ch), SUM(ch.collectedAmount), AVG(ch.collectedAmount) "
            +
            "FROM CollectionHistory ch WHERE ch.collectedAt >= :since " +
            "GROUP BY YEAR(ch.collectedAt), MONTH(ch.collectedAt) " +
            "ORDER BY YEAR(ch.collectedAt) DESC, MONTH(ch.collectedAt) DESC")
    List<Object[]> getMonthlyCollectionStatistics(@Param("since") LocalDateTime since);

    /**
     * 時間別収集統計を取得
     */
    @Query("SELECT HOUR(ch.collectedAt) as hour, COUNT(ch), SUM(ch.collectedAmount), AVG(ch.collectedAmount) " +
            "FROM CollectionHistory ch WHERE ch.collectedAt >= :since " +
            "GROUP BY HOUR(ch.collectedAt) ORDER BY HOUR(ch.collectedAt)")
    List<Object[]> getHourlyCollectionStatistics(@Param("since") LocalDateTime since);

    // ===== ランキング・トップユーザー =====

    /**
     * 総収集金額でユーザーランキングを取得
     */
    @Query("SELECT ch.userId, SUM(ch.collectedAmount) as totalCollection FROM CollectionHistory ch " +
            "GROUP BY ch.userId ORDER BY totalCollection DESC")
    Page<Object[]> getUserCollectionRanking(Pageable pageable);

    /**
     * 特定期間の収集金額でユーザーランキングを取得
     */
    @Query("SELECT ch.userId, SUM(ch.collectedAmount) as totalCollection FROM CollectionHistory ch " +
            "WHERE ch.collectedAt BETWEEN :startDate AND :endDate " +
            "GROUP BY ch.userId ORDER BY totalCollection DESC")
    Page<Object[]> getUserCollectionRankingByPeriod(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * スペース別収集ランキングを取得
     */
    @Query("SELECT ch.spaceId, SUM(ch.collectedAmount) as totalCollection FROM CollectionHistory ch " +
            "GROUP BY ch.spaceId ORDER BY totalCollection DESC")
    Page<Object[]> getSpaceCollectionRanking(Pageable pageable);

    /**
     * 収集回数でユーザーランキングを取得
     */
    @Query("SELECT ch.userId, COUNT(ch) as collectionCount FROM CollectionHistory ch " +
            "GROUP BY ch.userId ORDER BY collectionCount DESC")
    Page<Object[]> getUserCollectionCountRanking(Pageable pageable);

    // ===== 高度な分析クエリ =====

    /**
     * ユーザーの収集トレンドを取得
     */
    @Query("SELECT DATE(ch.collectedAt) as date, SUM(ch.collectedAmount) as dailyCollection " +
            "FROM CollectionHistory ch WHERE ch.userId = :userId " +
            "AND ch.collectedAt >= :since " +
            "GROUP BY DATE(ch.collectedAt) ORDER BY DATE(ch.collectedAt) ASC")
    List<Object[]> getUserCollectionTrend(@Param("userId") UUID userId, @Param("since") LocalDateTime since);

    /**
     * 収集タイプ別の期間比較統計を取得
     */
    @Query("SELECT ch.collectionReason, " +
            "SUM(CASE WHEN ch.collectedAt BETWEEN :period1Start AND :period1End THEN ch.collectedAmount ELSE 0 END) as period1Total, "
            +
            "SUM(CASE WHEN ch.collectedAt BETWEEN :period2Start AND :period2End THEN ch.collectedAmount ELSE 0 END) as period2Total "
            +
            "FROM CollectionHistory ch " +
            "WHERE ch.collectedAt BETWEEN :period1Start AND :period2End " +
            "GROUP BY ch.collectionReason")
    List<Object[]> getCollectionTypeComparison(@Param("period1Start") LocalDateTime period1Start,
            @Param("period1End") LocalDateTime period1End,
            @Param("period2Start") LocalDateTime period2Start,
            @Param("period2End") LocalDateTime period2End);

    /**
     * ユーザーの収集パフォーマンス分析
     */
    @Query("SELECT ch.userId, " +
            "COUNT(ch) as totalCollections, " +
            "SUM(ch.collectedAmount) as totalAmount, " +
            "AVG(ch.collectedAmount) as avgAmount, " +
            "MAX(ch.collectedAmount) as maxAmount, " +
            "MIN(ch.collectedAmount) as minAmount " +
            "FROM CollectionHistory ch WHERE ch.collectedAt >= :since " +
            "GROUP BY ch.userId " +
            "ORDER BY totalAmount DESC")
    Page<Object[]> getUserCollectionPerformance(@Param("since") LocalDateTime since, Pageable pageable);

    // ===== 複合条件検索 =====

    /**
     * 複合条件で収集履歴を検索（ユーザー + 金額範囲 + 期間）
     */
    @Query("SELECT ch FROM CollectionHistory ch WHERE " +
            "ch.userId = :userId AND " +
            "ch.collectedAmount BETWEEN :minAmount AND :maxAmount AND " +
            "ch.collectedAt BETWEEN :startDate AND :endDate " +
            "ORDER BY ch.collectedAt DESC")
    Page<CollectionHistory> findByComplexConditions(@Param("userId") UUID userId,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * スペースと収集タイプでの複合検索
     */
    @Query("SELECT ch FROM CollectionHistory ch WHERE " +
            "ch.spaceId = :spaceId AND " +
            "ch.collectionReason = :collectionType AND " +
            "ch.collectedAt >= :since " +
            "ORDER BY ch.collectedAmount DESC")
    Page<CollectionHistory> findBySpaceAndTypeAndDate(@Param("spaceId") Long spaceId,
            @Param("collectionType") CollectionHistory.CollectionReason collectionType,
            @Param("since") LocalDateTime since,
            Pageable pageable);

    // ===== カスタムネイティブクエリ =====

    /**
     * 収集の詳細ランキング（パフォーマンス最適化）
     */
    @Query(value = """
            SELECT ch.*,
                    ROW_NUMBER() OVER (ORDER BY ch.collected_amount DESC) as amount_rank,
                    DENSE_RANK() OVER (ORDER BY ch.collected_amount DESC) as amount_dense_rank
            FROM collection_history ch
            WHERE ch.collected_at >= :since
            ORDER BY ch.collected_amount DESC
            """, nativeQuery = true)
    List<Object[]> getDetailedCollectionRanking(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * 収集金額分布のヒストグラムデータを取得
     */
    @Query(value = """
            SELECT
                    CASE
                            WHEN collected_amount = 0 THEN '0'
                            WHEN collected_amount BETWEEN 0.01 AND 10 THEN '0.01-10'
                            WHEN collected_amount BETWEEN 10.01 AND 100 THEN '10-100'
                            WHEN collected_amount BETWEEN 100.01 AND 1000 THEN '100-1K'
                            WHEN collected_amount BETWEEN 1000.01 AND 10000 THEN '1K-10K'
                            ELSE '10K+'
                    END as amount_range,
                    COUNT(*) as collection_count,
                    SUM(collected_amount) as total_amount,
                    AVG(collected_amount) as average_amount
            FROM collection_history
            GROUP BY amount_range
            ORDER BY MIN(collected_amount)
            """, nativeQuery = true)
    List<Object[]> getCollectionAmountDistribution();

    /**
     * 週別収集パターン分析
     */
    @Query(value = """
            SELECT
                    DAYOFWEEK(collected_at) as day_of_week,
                    HOUR(collected_at) as hour_of_day,
                    COUNT(*) as collection_count,
                    SUM(collected_amount) as total_amount,
                    AVG(collected_amount) as avg_amount
            FROM collection_history
            WHERE collected_at >= :since
            GROUP BY DAYOFWEEK(collected_at), HOUR(collected_at)
            ORDER BY day_of_week, hour_of_day
            """, nativeQuery = true)
    List<Object[]> getCollectionTimePattern(@Param("since") LocalDateTime since);

    /**
     * ユーザー収集効率分析
     */
    @Query(value = """
            SELECT
                    user_id,
                    COUNT(*) as total_collections,
                    SUM(collected_amount) as total_amount,
                    AVG(collected_amount) as avg_amount,
                    MIN(collected_amount) as min_amount,
                    MAX(collected_amount) as max_amount,
                    STDDEV(collected_amount) as amount_stddev,
                    COUNT(DISTINCT collection_reason) as collection_types_used
            FROM collection_history
            WHERE collected_at >= :since
            GROUP BY user_id
            HAVING COUNT(*) >= :minCollections
            ORDER BY total_amount DESC
            """, nativeQuery = true)
    List<Object[]> getUserCollectionEfficiencyAnalysis(@Param("since") LocalDateTime since,
            @Param("minCollections") int minCollections,
            Pageable pageable);
}
