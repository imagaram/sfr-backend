package com.serendipity.tokyo.sfrbackend.repository.sfrt;

import com.serendipity.tokyo.sfrbackend.entity.sfrt.SfrtBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * SFRT残高リポジトリ
 */
@Repository
public interface SfrtBalanceRepository extends JpaRepository<SfrtBalance, Long> {

    /**
     * ユーザーとスペースでSFRT残高を取得
     */
    Optional<SfrtBalance> findByUserIdAndSpaceId(String userId, Long spaceId);

    /**
     * ユーザーのSFRT残高リストを取得
     */
    List<SfrtBalance> findByUserIdOrderByUpdatedAtDesc(String userId);

    /**
     * スペース内のSFRT残高リストを取得
     */
    List<SfrtBalance> findBySpaceIdOrderByCurrentBalanceDesc(Long spaceId);

    /**
     * アクティブなSFRT残高リストを取得
     */
    List<SfrtBalance> findByStatusOrderByCurrentBalanceDesc(SfrtBalance.SfrtStatus status);

    /**
     * 指定金額以上の残高を持つユーザーを取得
     */
    @Query("SELECT sb FROM SfrtBalance sb WHERE sb.currentBalance >= :minBalance AND sb.status = :status")
    List<SfrtBalance> findByCurrentBalanceGreaterThanEqualAndStatus(
        @Param("minBalance") BigDecimal minBalance,
        @Param("status") SfrtBalance.SfrtStatus status
    );

    /**
     * スペース内のSFRT総供給量を取得
     */
    @Query("SELECT COALESCE(SUM(sb.currentBalance), 0) FROM SfrtBalance sb WHERE sb.spaceId = :spaceId")
    BigDecimal getTotalSupplyBySpaceId(@Param("spaceId") Long spaceId);

    /**
     * スペース内のアクティブSFRT総供給量を取得
     */
    @Query("SELECT COALESCE(SUM(sb.currentBalance), 0) FROM SfrtBalance sb " +
           "WHERE sb.spaceId = :spaceId AND sb.status = 'ACTIVE'")
    BigDecimal getActiveTotalSupplyBySpaceId(@Param("spaceId") Long spaceId);

    /**
     * 総獲得報酬を取得
     */
    @Query("SELECT COALESCE(SUM(sb.totalEarnedPurchase + sb.totalEarnedSales), 0) " +
           "FROM SfrtBalance sb WHERE sb.spaceId = :spaceId")
    BigDecimal getTotalRewardsBySpaceId(@Param("spaceId") Long spaceId);

    /**
     * 総換金済み額を取得
     */
    @Query("SELECT COALESCE(SUM(sb.totalRedeemed), 0) FROM SfrtBalance sb WHERE sb.spaceId = :spaceId")
    BigDecimal getTotalRedeemedBySpaceId(@Param("spaceId") Long spaceId);

    /**
     * ユーザー数を取得（SFRT保有者）
     */
    @Query("SELECT COUNT(sb) FROM SfrtBalance sb " +
           "WHERE sb.spaceId = :spaceId AND sb.currentBalance > 0")
    Long getActiveHolderCountBySpaceId(@Param("spaceId") Long spaceId);

    /**
     * SFRT残高を一括更新（残高増加）
     */
    @Modifying
    @Query("UPDATE SfrtBalance sb SET sb.currentBalance = sb.currentBalance + :amount, " +
           "sb.updatedAt = CURRENT_TIMESTAMP WHERE sb.id = :id")
    int increaseBalance(@Param("id") Long id, @Param("amount") BigDecimal amount);

    /**
     * SFRT残高を一括更新（残高減少）
     */
    @Modifying
    @Query("UPDATE SfrtBalance sb SET sb.currentBalance = sb.currentBalance - :amount, " +
           "sb.updatedAt = CURRENT_TIMESTAMP WHERE sb.id = :id AND sb.currentBalance >= :amount")
    int decreaseBalance(@Param("id") Long id, @Param("amount") BigDecimal amount);

    /**
     * 外部取引所連携状態を更新
     */
    @Modifying
    @Query("UPDATE SfrtBalance sb SET sb.externalExchangeEnabled = :enabled, " +
           "sb.updatedAt = CURRENT_TIMESTAMP WHERE sb.userId = :userId AND sb.spaceId = :spaceId")
    int updateExternalExchangeEnabled(
        @Param("userId") String userId,
        @Param("spaceId") Long spaceId,
        @Param("enabled") Boolean enabled
    );

    /**
     * SFRT状態を更新
     */
    @Modifying
    @Query("UPDATE SfrtBalance sb SET sb.status = :status, " +
           "sb.updatedAt = CURRENT_TIMESTAMP WHERE sb.userId = :userId AND sb.spaceId = :spaceId")
    int updateStatus(
        @Param("userId") String userId,
        @Param("spaceId") Long spaceId,
        @Param("status") SfrtBalance.SfrtStatus status
    );

    /**
     * 分布統計を取得
     */
    @Query("SELECT " +
           "COUNT(sb) as holderCount, " +
           "MIN(sb.currentBalance) as minBalance, " +
           "MAX(sb.currentBalance) as maxBalance, " +
           "AVG(sb.currentBalance) as avgBalance, " +
           "SUM(sb.currentBalance) as totalBalance " +
           "FROM SfrtBalance sb WHERE sb.spaceId = :spaceId AND sb.currentBalance > 0")
    Object[] getBalanceDistributionStats(@Param("spaceId") Long spaceId);

    // ===== 統計情報用追加メソッド =====
    
    /**
     * 総SFRT供給量
     */
    @Query("SELECT COALESCE(SUM(b.currentBalance), 0) FROM SfrtBalance b")
    BigDecimal getTotalSupply();

    /**
     * アクティブアカウントの総SFRT供給量
     */
    @Query("SELECT COALESCE(SUM(b.currentBalance), 0) FROM SfrtBalance b WHERE b.status = 'ACTIVE'")
    BigDecimal getActiveTotalSupply();

    /**
     * 凍結アカウントの総SFRT供給量
     */
    @Query("SELECT COALESCE(SUM(b.currentBalance), 0) FROM SfrtBalance b WHERE b.status = 'FROZEN'")
    BigDecimal getFrozenTotalSupply();

    /**
     * 総アカウント数
     */
    @Query("SELECT COUNT(b) FROM SfrtBalance b")
    Long getTotalAccountsCount();

    /**
     * アクティブアカウント数
     */
    @Query("SELECT COUNT(b) FROM SfrtBalance b WHERE b.status = 'ACTIVE'")
    Long getActiveAccountsCount();

    /**
     * 平均残高
     */
    @Query("SELECT COALESCE(AVG(b.currentBalance), 0) FROM SfrtBalance b WHERE b.currentBalance > 0")
    BigDecimal getAverageBalance();

    /**
     * 中央値残高（簡易計算）
     */
    @Query("SELECT COALESCE(AVG(b.currentBalance), 0) FROM SfrtBalance b WHERE b.currentBalance > 0")
    BigDecimal getMedianBalance();

    /**
     * Space別総SFRT供給量
     */
    @Query("SELECT COALESCE(SUM(b.currentBalance), 0) FROM SfrtBalance b WHERE b.spaceId = :spaceId")
    BigDecimal getSpaceTotalSupply(@Param("spaceId") Long spaceId);

    /**
     * Space別アクティブユーザー数
     */
    @Query("SELECT COUNT(b) FROM SfrtBalance b WHERE b.spaceId = :spaceId AND b.status = 'ACTIVE' AND b.currentBalance > 0")
    Long getActiveUsersCountBySpace(@Param("spaceId") Long spaceId);
}
