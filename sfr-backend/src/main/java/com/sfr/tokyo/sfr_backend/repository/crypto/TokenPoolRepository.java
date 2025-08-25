package com.sfr.tokyo.sfr_backend.repository.crypto;

import com.sfr.tokyo.sfr_backend.entity.crypto.TokenPool;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * TokenPoolRepository
 * SFR トークンプールのデータアクセス層
 * トークンプールの管理、残高確認、プール統計などを提供
 */
@Repository
public interface TokenPoolRepository extends JpaRepository<TokenPool, Long>, JpaSpecificationExecutor<TokenPool> {

    // ===== 基本検索メソッド =====

    /**
     * スペースIDでトークンプールを検索
     */
    Optional<TokenPool> findBySpaceId(Long spaceId);

    /**
     * 複数のスペースIDでトークンプールを検索
     */
    List<TokenPool> findBySpaceIdIn(List<Long> spaceIds);

    /**
     * スペースIDでトークンプールを検索（基本検索）
     */
    @Query("SELECT tp FROM TokenPool tp WHERE tp.spaceId = :spaceId")
    Optional<TokenPool> findBySpaceIdAndPoolType(@Param("spaceId") Long spaceId, @Param("poolType") String poolType);

    // ===== 残高・プール状態検索 =====

    /**
     * 指定した最小残高以上のトークンプールを検索
     */
    @Query("SELECT tp FROM TokenPool tp WHERE tp.totalSupply >= :minBalance ORDER BY tp.totalSupply DESC")
    List<TokenPool> findByTotalSupplyGreaterThanEqual(@Param("minBalance") BigDecimal minBalance, Pageable pageable);

    /**
     * 残高範囲でトークンプールを検索
     */
    @Query("SELECT tp FROM TokenPool tp WHERE tp.totalSupply BETWEEN :minBalance AND :maxBalance ORDER BY tp.totalSupply DESC")
    Page<TokenPool> findByTotalSupplyBetween(@Param("minBalance") BigDecimal minBalance,
            @Param("maxBalance") BigDecimal maxBalance,
            Pageable pageable);

    /**
     * 流通量が指定値以上のプールを検索
     */
    @Query("SELECT tp FROM TokenPool tp WHERE tp.circulatingSupply >= :minCirculation ORDER BY tp.circulatingSupply DESC")
    List<TokenPool> findByCirculatingSupplyGreaterThanEqual(@Param("minCirculation") BigDecimal minCirculation);

    // ===== 活動状態・時間ベース検索 =====

    /**
     * 最後の活動時間が指定期間内のプールを検索
     */
    @Query("SELECT tp FROM TokenPool tp WHERE tp.updatedAt >= :since ORDER BY tp.updatedAt DESC")
    Page<TokenPool> findByLastActivityAfter(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * 指定期間内に作成されたプールを検索
     */
    @Query("SELECT tp FROM TokenPool tp WHERE tp.createdAt BETWEEN :startDate AND :endDate ORDER BY tp.createdAt DESC")
    List<TokenPool> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 指定期間内に更新されたプールを検索
     */
    @Query("SELECT tp FROM TokenPool tp WHERE tp.updatedAt >= :since ORDER BY tp.updatedAt DESC")
    Page<TokenPool> findByUpdatedAtAfter(@Param("since") LocalDateTime since, Pageable pageable);

    // ===== プール統計・集計メソッド =====

    /**
     * 全プールの総供給量を計算
     */
    @Query("SELECT SUM(tp.totalSupply) FROM TokenPool tp")
    BigDecimal getTotalSupplySum();

    /**
     * 全プールの平均供給量を計算
     */
    @Query("SELECT AVG(tp.totalSupply) FROM TokenPool tp")
    BigDecimal getAverageTotalSupply();

    /**
     * 指定スペースリストの総流通量を計算
     */
    @Query("SELECT SUM(tp.circulatingSupply) FROM TokenPool tp WHERE tp.spaceId IN :spaceIds")
    BigDecimal getTotalCirculatingSupply(@Param("spaceIds") List<Long> spaceIds);

    /**
     * プール統計情報を取得（ステータス別）
     */
    @Query("SELECT tp.status, COUNT(tp), SUM(tp.totalSupply), AVG(tp.totalSupply) " +
            "FROM TokenPool tp GROUP BY tp.status ORDER BY SUM(tp.totalSupply) DESC")
    List<Object[]> getPoolStatusStatistics();

    // ===== プール管理メソッド =====

    /**
     * プールの総供給量を更新
     */
    @Modifying
    @Transactional
    @Query("UPDATE TokenPool tp SET tp.totalSupply = :totalSupply, tp.updatedAt = :updateTime " +
            "WHERE tp.spaceId = :spaceId")
    int updateTotalSupply(@Param("spaceId") Long spaceId,
            @Param("totalSupply") BigDecimal totalSupply,
            @Param("updateTime") LocalDateTime updateTime);

    /**
     * プールの流通量を更新
     */
    @Modifying
    @Transactional
    @Query("UPDATE TokenPool tp SET tp.circulatingSupply = :circulatingSupply, tp.updatedAt = :updateTime " +
            "WHERE tp.spaceId = :spaceId")
    int updateCirculatingSupply(@Param("spaceId") Long spaceId,
            @Param("circulatingSupply") BigDecimal circulatingSupply,
            @Param("updateTime") LocalDateTime updateTime);

    /**
     * プールの最終活動時間を更新
     */
    @Modifying
    @Transactional
    @Query("UPDATE TokenPool tp SET tp.updatedAt = :updateTime " +
            "WHERE tp.spaceId = :spaceId")
    int updateLastActivity(@Param("spaceId") Long spaceId,
            @Param("lastActivity") LocalDateTime lastActivity,
            @Param("updateTime") LocalDateTime updateTime);

    /**
     * 複数プールの総供給量を一括更新
     */
    @Modifying
    @Transactional
    @Query("UPDATE TokenPool tp SET tp.totalSupply = tp.totalSupply + :amount, tp.updatedAt = :updateTime " +
            "WHERE tp.spaceId IN :spaceIds")
    int batchUpdateTotalSupply(@Param("spaceIds") List<Long> spaceIds,
            @Param("amount") BigDecimal amount,
            @Param("updateTime") LocalDateTime updateTime);

    // ===== 高度なクエリ・分析メソッド =====

    /**
     * トップパフォーマンスプールを取得（供給量順）
     */
    @Query("SELECT tp FROM TokenPool tp ORDER BY tp.totalSupply DESC")
    Page<TokenPool> findTopPerformingPools(Pageable pageable);

    /**
     * 最近活動したプールを取得
     */
    @Query("SELECT tp FROM TokenPool tp WHERE tp.updatedAt IS NOT NULL " +
            "ORDER BY tp.updatedAt DESC")
    Page<TokenPool> findRecentlyActivePools(Pageable pageable);

    /**
     * 供給量上位のプールを検索
     */
    @Query("SELECT tp FROM TokenPool tp ORDER BY tp.totalSupply DESC")
    List<TokenPool> findTopSupplyPools(Pageable pageable);

    // ===== 複合条件検索 =====

    /**
     * 複合条件でプールを検索（供給量範囲 + 活動期間）
     */
    @Query("SELECT tp FROM TokenPool tp WHERE " +
            "tp.totalSupply BETWEEN :minSupply AND :maxSupply AND " +
            "tp.updatedAt >= :since " +
            "ORDER BY tp.totalSupply DESC, tp.updatedAt DESC")
    Page<TokenPool> findBySupplyRangeAndActivitySince(@Param("minSupply") BigDecimal minSupply,
            @Param("maxSupply") BigDecimal maxSupply,
            @Param("since") LocalDateTime since,
            Pageable pageable);

    /**
     * プール詳細統計を取得（ステータス別）
     */
    @Query("SELECT tp.status, " +
            "COUNT(tp) as poolCount, " +
            "SUM(tp.totalSupply) as totalSum, " +
            "AVG(tp.totalSupply) as averageSupply, " +
            "MAX(tp.totalSupply) as maxSupply, " +
            "MIN(tp.totalSupply) as minSupply " +
            "FROM TokenPool tp WHERE tp.status = :status " +
            "GROUP BY tp.status")
    Object[] getDetailedPoolStatusStatistics(@Param("status") String status);

    // ===== カスタムネイティブクエリ =====

    /**
     * パフォーマンス最適化されたプールランキング取得
     */
    @Query(value = """
            SELECT tp.*,
                   ROW_NUMBER() OVER (ORDER BY tp.total_supply DESC) as rank,
                   RANK() OVER (ORDER BY tp.total_supply DESC) as dense_rank
            FROM token_pools tp
            ORDER BY tp.total_supply DESC
            """, nativeQuery = true)
    List<Object[]> getPoolRanking(Pageable pageable);

    /**
     * 供給量分布のヒストグラムデータを取得
     */
    @Query(value = """
            SELECT
                CASE
                    WHEN total_supply = 0 THEN '0'
                    WHEN total_supply BETWEEN 0.01 AND 1000 THEN '0-1K'
                    WHEN total_supply BETWEEN 1000.01 AND 10000 THEN '1K-10K'
                    WHEN total_supply BETWEEN 10000.01 AND 100000 THEN '10K-100K'
                    WHEN total_supply BETWEEN 100000.01 AND 1000000 THEN '100K-1M'
                    ELSE '1M+'
                END as supply_range,
                COUNT(*) as pool_count,
                SUM(total_supply) as total_supply_sum,
                AVG(total_supply) as average_supply
            FROM token_pools
            GROUP BY supply_range
            ORDER BY MIN(total_supply)
            """, nativeQuery = true)
    List<Object[]> getSupplyDistribution();

    /**
     * 月次プール成長統計を取得
     */
    @Query(value = """
            SELECT
                DATE_TRUNC('month', created_at) as month,
                COUNT(*) as new_pools,
                SUM(total_supply) as month_total_supply,
                AVG(total_supply) as month_avg_supply
            FROM token_pools
            WHERE created_at >= :since
            GROUP BY DATE_TRUNC('month', created_at)
            ORDER BY month DESC
            """, nativeQuery = true)
    List<Object[]> getMonthlyPoolGrowthStats(@Param("since") LocalDateTime since);
}
