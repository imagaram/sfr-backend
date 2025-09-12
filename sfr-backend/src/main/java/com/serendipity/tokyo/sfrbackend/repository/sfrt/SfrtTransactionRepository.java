package com.serendipity.tokyo.sfrbackend.repository.sfrt;

import com.serendipity.tokyo.sfrbackend.entity.sfrt.SfrtTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * SFRT取引履歴リポジトリ
 */
@Repository
public interface SfrtTransactionRepository extends JpaRepository<SfrtTransaction, Long> {

    /**
     * ユーザーの取引履歴を取得（ページネーション）
     */
    Page<SfrtTransaction> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    /**
     * ユーザーとスペースの取引履歴を取得
     */
    Page<SfrtTransaction> findByUserIdAndSpaceIdOrderByCreatedAtDesc(
        String userId, Long spaceId, Pageable pageable);

    /**
     * 取引タイプ別履歴を取得
     */
    Page<SfrtTransaction> findByUserIdAndTransactionTypeOrderByCreatedAtDesc(
        String userId, SfrtTransaction.SfrtTransactionType transactionType, Pageable pageable);

    /**
     * 期間指定で取引履歴を取得
     */
    Page<SfrtTransaction> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        String userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * 関連SFR取引の履歴を取得
     */
    List<SfrtTransaction> findByRelatedSfrTransactionIdOrderByCreatedAtDesc(Long relatedSfrTransactionId);

    /**
     * ステータス別取引リストを取得
     */
    List<SfrtTransaction> findByStatusOrderByCreatedAtDesc(SfrtTransaction.SfrtTransactionStatus status);

    /**
     * 処理待ち取引を取得
     */
    @Query("SELECT st FROM SfrtTransaction st WHERE st.status IN ('PENDING', 'PROCESSING') " +
           "ORDER BY st.createdAt ASC")
    List<SfrtTransaction> findPendingTransactions();

    /**
     * ユーザーの報酬履歴を取得
     */
    @Query("SELECT st FROM SfrtTransaction st WHERE st.userId = :userId " +
           "AND st.transactionType IN ('PURCHASE_REWARD', 'SALES_REWARD', 'STAKING_REWARD', " +
           "'GOVERNANCE_REWARD', 'LIQUIDITY_REWARD') ORDER BY st.createdAt DESC")
    Page<SfrtTransaction> findRewardTransactions(@Param("userId") String userId, Pageable pageable);

    /**
     * ユーザーの取引統計を取得
     */
    @Query("SELECT " +
           "COALESCE(SUM(CASE WHEN st.transactionType = 'PURCHASE_REWARD' THEN st.amount ELSE 0 END), 0) as purchaseRewards, " +
           "COALESCE(SUM(CASE WHEN st.transactionType = 'SALES_REWARD' THEN st.amount ELSE 0 END), 0) as salesRewards, " +
           "COALESCE(SUM(CASE WHEN st.transactionType = 'EXCHANGE_TO_JPY' THEN st.amount ELSE 0 END), 0) as totalExchanged, " +
           "COUNT(CASE WHEN st.transactionType = 'EXCHANGE_TO_JPY' THEN 1 END) as exchangeCount " +
           "FROM SfrtTransaction st WHERE st.userId = :userId AND st.status = 'COMPLETED'")
    Object[] getUserTransactionStats(@Param("userId") String userId);

    /**
     * 日別取引量を取得
     */
    @Query("SELECT DATE(st.createdAt) as date, " +
           "SUM(CASE WHEN st.transactionType IN ('PURCHASE_REWARD', 'SALES_REWARD') THEN st.amount ELSE 0 END) as rewardVolume, " +
           "SUM(CASE WHEN st.transactionType = 'EXCHANGE_TO_JPY' THEN st.amount ELSE 0 END) as exchangeVolume " +
           "FROM SfrtTransaction st WHERE st.spaceId = :spaceId " +
           "AND st.createdAt >= :fromDate AND st.status = 'COMPLETED' " +
           "GROUP BY DATE(st.createdAt) ORDER BY DATE(st.createdAt) DESC")
    List<Object[]> getDailyTransactionVolume(
        @Param("spaceId") Long spaceId, 
        @Param("fromDate") LocalDateTime fromDate);

    /**
     * 月別統計を取得
     */
    @Query("SELECT YEAR(st.createdAt) as year, MONTH(st.createdAt) as month, " +
           "COUNT(st) as transactionCount, " +
           "SUM(st.amount) as totalAmount, " +
           "AVG(st.amount) as avgAmount " +
           "FROM SfrtTransaction st WHERE st.userId = :userId AND st.status = 'COMPLETED' " +
           "GROUP BY YEAR(st.createdAt), MONTH(st.createdAt) " +
           "ORDER BY YEAR(st.createdAt) DESC, MONTH(st.createdAt) DESC")
    List<Object[]> getMonthlyStats(@Param("userId") String userId);

    /**
     * 総発行SFRT量を取得
     */
    @Query("SELECT COALESCE(SUM(st.amount), 0) FROM SfrtTransaction st " +
           "WHERE st.spaceId = :spaceId AND st.transactionType IN " +
           "('PURCHASE_REWARD', 'SALES_REWARD', 'PLATFORM_RESERVE') AND st.status = 'COMPLETED'")
    BigDecimal getTotalIssuedAmount(@Param("spaceId") Long spaceId);

    /**
     * 総換金額を取得
     */
    @Query("SELECT COALESCE(SUM(st.amount), 0) FROM SfrtTransaction st " +
           "WHERE st.spaceId = :spaceId AND st.transactionType = 'EXCHANGE_TO_JPY' " +
           "AND st.status = 'COMPLETED'")
    BigDecimal getTotalExchangedAmount(@Param("spaceId") Long spaceId);

    /**
     * プラットフォーム準備金を取得
     */
    @Query("SELECT COALESCE(SUM(st.amount), 0) FROM SfrtTransaction st " +
           "WHERE st.spaceId = :spaceId AND st.transactionType = 'PLATFORM_RESERVE' " +
           "AND st.status = 'COMPLETED'")
    BigDecimal getPlatformReserveAmount(@Param("spaceId") Long spaceId);

    /**
     * 最近の取引アクティビティを取得
     */
    @Query("SELECT st FROM SfrtTransaction st WHERE st.spaceId = :spaceId " +
           "AND st.status = 'COMPLETED' AND st.createdAt >= :since " +
           "ORDER BY st.createdAt DESC")
    List<SfrtTransaction> getRecentActivity(
        @Param("spaceId") Long spaceId, 
        @Param("since") LocalDateTime since,
        Pageable pageable);

    /**
     * 失敗した取引を取得（リトライ対象）
     */
    @Query("SELECT st FROM SfrtTransaction st WHERE st.status = 'FAILED' " +
           "AND st.createdAt >= :since ORDER BY st.createdAt ASC")
    List<SfrtTransaction> getFailedTransactionsForRetry(@Param("since") LocalDateTime since);
}
