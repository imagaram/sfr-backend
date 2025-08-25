package com.sfr.tokyo.sfr_backend.repository.crypto;

import com.sfr.tokyo.sfr_backend.entity.crypto.UserBalance;
import com.sfr.tokyo.sfr_backend.entity.crypto.UserBalanceId;
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
 * UserBalance Repository - SFRトークンユーザー残高のデータアクセス層
 */
@Repository
public interface UserBalanceRepository
        extends JpaRepository<UserBalance, UserBalanceId>, JpaSpecificationExecutor<UserBalance> {

    // ===== 基本検索メソッド =====

    /**
     * スペースIDとユーザーIDで残高を検索
     */
    Optional<UserBalance> findBySpaceIdAndUserId(Long spaceId, String userId);

    /**
     * ユーザーIDで全スペースの残高を検索
     */
    List<UserBalance> findByUserId(String userId);

    /**
     * スペースIDで全ユーザーの残高を検索（ページネーション対応）
     */
    Page<UserBalance> findBySpaceId(Long spaceId, Pageable pageable);

    /**
     * アクティブな残高のみを検索 - 実際のフィールド名に合わせて削除
     */
    // Page<UserBalance> findBySpaceIdAndIsActiveTrue(Long spaceId, Pageable
    // pageable);

    /**
     * 凍結されていない残高のみを検索
     */
    Page<UserBalance> findBySpaceIdAndFrozenFalse(Long spaceId, Pageable pageable);

    // ===== 残高範囲検索 =====

    /**
     * 指定金額以上の残高を持つユーザーを検索
     */
    @Query("SELECT ub FROM UserBalance ub WHERE ub.spaceId = :spaceId AND ub.currentBalance >= :minBalance")
    Page<UserBalance> findBySpaceIdAndBalanceGreaterThanEqual(@Param("spaceId") Long spaceId,
            @Param("minBalance") BigDecimal minBalance,
            Pageable pageable);

    /**
     * 指定金額範囲の残高を持つユーザーを検索
     */
    @Query("SELECT ub FROM UserBalance ub WHERE ub.spaceId = :spaceId AND ub.currentBalance BETWEEN :minBalance AND :maxBalance")
    Page<UserBalance> findBySpaceIdAndBalanceBetween(@Param("spaceId") Long spaceId,
            @Param("minBalance") BigDecimal minBalance,
            @Param("maxBalance") BigDecimal maxBalance,
            Pageable pageable);

    /**
     * 回収対象となる高額残高ユーザーを検索
     */
    @Query("SELECT ub FROM UserBalance ub WHERE ub.spaceId = :spaceId AND ub.currentBalance > :threshold AND ub.frozen = false AND ub.collectionExempt = false ORDER BY ub.currentBalance DESC")
    List<UserBalance> findCollectionTargets(@Param("spaceId") Long spaceId, @Param("threshold") BigDecimal threshold);

    // ===== 統計・集計クエリ =====

    /**
     * スペースの総残高を計算
     */
    @Query("SELECT COALESCE(SUM(ub.currentBalance), 0) FROM UserBalance ub WHERE ub.spaceId = :spaceId")
    BigDecimal calculateTotalBalance(@Param("spaceId") Long spaceId);

    /**
     * スペースの平均残高を計算
     */
    @Query("SELECT COALESCE(AVG(ub.currentBalance), 0) FROM UserBalance ub WHERE ub.spaceId = :spaceId AND ub.currentBalance > 0")
    BigDecimal calculateAverageBalance(@Param("spaceId") Long spaceId);

    /**
     * スペースのアクティブユーザー数を取得
     */
    @Query("SELECT COUNT(ub) FROM UserBalance ub WHERE ub.spaceId = :spaceId AND ub.currentBalance > 0")
    Long countActiveUsersWithBalance(@Param("spaceId") Long spaceId);

    /**
     * 閾値を超える残高のユーザー数を取得
     */
    @Query("SELECT COUNT(ub) FROM UserBalance ub WHERE ub.spaceId = :spaceId AND ub.currentBalance > :threshold")
    Long countUsersAboveThreshold(@Param("spaceId") Long spaceId, @Param("threshold") BigDecimal threshold);

    /**
     * 残高分布統計を取得
     */
    @Query("""
            SELECT
                COUNT(ub) as userCount,
                MIN(ub.currentBalance) as minBalance,
                MAX(ub.currentBalance) as maxBalance,
                AVG(ub.currentBalance) as avgBalance,
                SUM(ub.currentBalance) as totalBalance
            FROM UserBalance ub
            WHERE ub.spaceId = :spaceId AND ub.currentBalance > 0
            """)
    Object[] getBalanceStatistics(@Param("spaceId") Long spaceId);

    // ===== 時間ベース検索 =====

    /**
     * 指定期間内に更新された残高を検索
     */
    @Query("SELECT ub FROM UserBalance ub WHERE ub.spaceId = :spaceId AND ub.updatedAt BETWEEN :startDate AND :endDate")
    Page<UserBalance> findBySpaceIdAndLastUpdatedBetween(@Param("spaceId") Long spaceId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * 長期間更新されていない残高を検索
     */
    @Query("SELECT ub FROM UserBalance ub WHERE ub.spaceId = :spaceId AND ub.updatedAt < :cutoffDate")
    Page<UserBalance> findStaleBalances(@Param("spaceId") Long spaceId,
            @Param("cutoffDate") LocalDateTime cutoffDate,
            Pageable pageable);

    /**
     * 最近活動があったユーザーの残高を検索
     */
    @Query("SELECT ub FROM UserBalance ub WHERE ub.spaceId = :spaceId AND ub.updatedAt > :sinceDate")
    Page<UserBalance> findRecentlyActiveUsers(@Param("spaceId") Long spaceId,
            @Param("sinceDate") LocalDateTime sinceDate,
            Pageable pageable);

    // ===== 更新・操作クエリ =====

    /**
     * 残高を更新
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserBalance ub SET ub.currentBalance = :newBalance, ub.updatedAt = :timestamp WHERE ub.spaceId = :spaceId AND ub.userId = :userId")
    int updateBalance(@Param("spaceId") Long spaceId,
            @Param("userId") String userId,
            @Param("newBalance") BigDecimal newBalance,
            @Param("timestamp") LocalDateTime timestamp);

    /**
     * 残高を増加
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserBalance ub SET ub.currentBalance = ub.currentBalance + :amount, ub.totalEarned = ub.totalEarned + :amount, ub.updatedAt = :timestamp WHERE ub.spaceId = :spaceId AND ub.userId = :userId")
    int increaseBalance(@Param("spaceId") Long spaceId,
            @Param("userId") String userId,
            @Param("amount") BigDecimal amount,
            @Param("timestamp") LocalDateTime timestamp);

    /**
     * 残高を減少
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserBalance ub SET ub.currentBalance = ub.currentBalance - :amount, ub.totalSpent = ub.totalSpent + :amount, ub.updatedAt = :timestamp WHERE ub.spaceId = :spaceId AND ub.userId = :userId AND ub.currentBalance >= :amount")
    int decreaseBalance(@Param("spaceId") Long spaceId,
            @Param("userId") String userId,
            @Param("amount") BigDecimal amount,
            @Param("timestamp") LocalDateTime timestamp);

    /**
     * アカウントを凍結
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserBalance ub SET ub.frozen = true, ub.updatedAt = :timestamp WHERE ub.spaceId = :spaceId AND ub.userId = :userId")
    int freezeAccount(@Param("spaceId") Long spaceId,
            @Param("userId") String userId,
            @Param("timestamp") LocalDateTime timestamp);

    /**
     * アカウントの凍結を解除
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserBalance ub SET ub.frozen = false WHERE ub.spaceId = :spaceId AND ub.userId = :userId")
    int unfreezeAccount(@Param("spaceId") Long spaceId, @Param("userId") String userId);

    // ===== 複合検索クエリ =====

    /**
     * アクティブで凍結されていない高額残高ユーザーを検索
     */
    @Query("""
            SELECT ub FROM UserBalance ub
            WHERE ub.spaceId = :spaceId
            AND ub.currentBalance >= :minBalance
            AND ub.frozen = false
            AND ub.updatedAt > :activityThreshold
            ORDER BY ub.currentBalance DESC
            """)
    Page<UserBalance> findActiveHighBalanceUsers(@Param("spaceId") Long spaceId,
            @Param("minBalance") BigDecimal minBalance,
            @Param("activityThreshold") LocalDateTime activityThreshold,
            Pageable pageable);

    /**
     * 凍結されたアカウントを検索
     */
    @Query("SELECT ub FROM UserBalance ub WHERE ub.spaceId = :spaceId AND ub.frozen = true")
    Page<UserBalance> findFrozenAccounts(@Param("spaceId") Long spaceId, Pageable pageable);

    /**
     * 残高がゼロまたは負のアカウントを検索
     */
    @Query("SELECT ub FROM UserBalance ub WHERE ub.spaceId = :spaceId AND ub.currentBalance <= 0")
    Page<UserBalance> findZeroOrNegativeBalances(@Param("spaceId") Long spaceId, Pageable pageable);

    // ===== 管理・監査クエリ =====

    /**
     * 残高整合性チェック用クエリ
     */
    @Query("""
            SELECT ub FROM UserBalance ub
            WHERE ub.spaceId = :spaceId
            AND (ub.currentBalance < 0 OR
                 ub.totalEarned < 0 OR
                 ub.totalSpent < 0 OR
                 ub.totalCollected < 0 OR
                 (ub.totalEarned - ub.totalSpent - ub.totalCollected) != ub.currentBalance)
            """)
    List<UserBalance> findInconsistentBalances(@Param("spaceId") Long spaceId);

    /**
     * 特定の条件でユーザー残高の存在をチェック
     */
    boolean existsBySpaceIdAndUserId(Long spaceId, String userId);

    /**
     * スペース内でアクティブな残高が存在するかチェック
     */
    boolean existsBySpaceIdAndCurrentBalanceGreaterThan(Long spaceId, BigDecimal balance);

    // ===== カスタムネイティブクエリ =====

    /**
     * パフォーマンス最適化された残高ランキング取得
     */
    @Query(value = """
            SELECT ub.*,
                   ROW_NUMBER() OVER (ORDER BY ub.current_balance DESC) as rank
            FROM user_balances ub
            WHERE ub.space_id = :spaceId
            AND ub.frozen = false
            ORDER BY ub.current_balance DESC
            """, nativeQuery = true)
    List<Object[]> getBalanceRanking(@Param("spaceId") Long spaceId, Pageable pageable);

    /**
     * 残高分布のヒストグラムデータを取得
     */
    @Query(value = """
            SELECT
                CASE
                    WHEN current_balance = 0 THEN '0'
                    WHEN current_balance BETWEEN 0.01 AND 10 THEN '0.01-10'
                    WHEN current_balance BETWEEN 10.01 AND 100 THEN '10-100'
                    WHEN current_balance BETWEEN 100.01 AND 1000 THEN '100-1K'
                    WHEN current_balance BETWEEN 1000.01 AND 10000 THEN '1K-10K'
                    ELSE '10K+'
                END as balance_range,
                COUNT(*) as user_count,
                SUM(current_balance) as total_balance
            FROM user_balances
            WHERE space_id = :spaceId
            GROUP BY balance_range
            ORDER BY MIN(current_balance)
            """, nativeQuery = true)
    List<Object[]> getBalanceDistribution(@Param("spaceId") Long spaceId);
}
