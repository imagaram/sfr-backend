package com.sfr.tokyo.sfr.backend.repository;

import com.sfr.tokyo.sfr.backend.entity.SfrPurchaseTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SfrPurchaseTransactionRepository extends JpaRepository<SfrPurchaseTransaction, Long> {
    
    // ユーザーの購入履歴取得
    List<SfrPurchaseTransaction> findByUserIdOrderByCreatedAtDesc(String userId);
    
    // ユーザーの特定期間の購入履歴
    @Query("SELECT spt FROM SfrPurchaseTransaction spt WHERE spt.userId = :userId " +
           "AND spt.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY spt.createdAt DESC")
    List<SfrPurchaseTransaction> findByUserIdAndDateRange(
        @Param("userId") String userId,
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate);
    
    // Stripe Payment Intent IDで検索
    Optional<SfrPurchaseTransaction> findByStripePaymentIntentId(String paymentIntentId);
    
    // ステータス別取得
    List<SfrPurchaseTransaction> findByStatusOrderByCreatedAtDesc(
        SfrPurchaseTransaction.TransactionStatus status);
    
    // ユーザーの日次購入限度額チェック用
    @Query("SELECT COALESCE(SUM(spt.yenAmount), 0) FROM SfrPurchaseTransaction spt " +
           "WHERE spt.userId = :userId " +
           "AND spt.status = 'COMPLETED' " +
           "AND DATE(spt.completedAt) = CURRENT_DATE")
    BigDecimal getDailyPurchaseAmount(@Param("userId") String userId);
    
    // ユーザーの月次購入限度額チェック用
    @Query("SELECT COALESCE(SUM(spt.yenAmount), 0) FROM SfrPurchaseTransaction spt " +
           "WHERE spt.userId = :userId " +
           "AND spt.status = 'COMPLETED' " +
           "AND YEAR(spt.completedAt) = YEAR(CURRENT_DATE) " +
           "AND MONTH(spt.completedAt) = MONTH(CURRENT_DATE)")
    BigDecimal getMonthlyPurchaseAmount(@Param("userId") String userId);
    
    // ユーザーの総購入額
    @Query("SELECT COALESCE(SUM(spt.yenAmount), 0) FROM SfrPurchaseTransaction spt " +
           "WHERE spt.userId = :userId AND spt.status = 'COMPLETED'")
    BigDecimal getTotalPurchaseAmount(@Param("userId") String userId);
    
    // ユーザーの総SFR取得量
    @Query("SELECT COALESCE(SUM(spt.sfrAmount), 0) FROM SfrPurchaseTransaction spt " +
           "WHERE spt.userId = :userId AND spt.status = 'COMPLETED'")
    BigDecimal getTotalSfrPurchased(@Param("userId") String userId);
    
    // SFRT配布対象の取引一覧
    @Query("SELECT spt FROM SfrPurchaseTransaction spt " +
           "WHERE spt.status = 'COMPLETED' " +
           "AND spt.sfrtDistributed = false " +
           "ORDER BY spt.completedAt ASC")
    List<SfrPurchaseTransaction> findPendingSfrtDistribution();
    
    // 管理画面用：期間別統計
    @Query("SELECT " +
           "COUNT(spt), " +
           "COALESCE(SUM(spt.yenAmount), 0), " +
           "COALESCE(SUM(spt.sfrAmount), 0) " +
           "FROM SfrPurchaseTransaction spt " +
           "WHERE spt.status = 'COMPLETED' " +
           "AND spt.completedAt BETWEEN :startDate AND :endDate")
    Object[] getPurchaseStatistics(@Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate);
    
    // 日別購入統計
    @Query("SELECT DATE(spt.completedAt) as date, " +
           "COUNT(spt) as transactionCount, " +
           "SUM(spt.yenAmount) as totalYenAmount, " +
           "SUM(spt.sfrAmount) as totalSfrAmount " +
           "FROM SfrPurchaseTransaction spt " +
           "WHERE spt.status = 'COMPLETED' " +
           "AND spt.completedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(spt.completedAt) " +
           "ORDER BY DATE(spt.completedAt)")
    List<Object[]> getDailyPurchaseStatistics(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);
    
    // 失敗した取引の取得（リトライ対象）
    @Query("SELECT spt FROM SfrPurchaseTransaction spt " +
           "WHERE spt.status = 'FAILED' " +
           "AND spt.createdAt > :retryThreshold " +
           "ORDER BY spt.createdAt ASC")
    List<SfrPurchaseTransaction> findFailedTransactionsForRetry(
        @Param("retryThreshold") LocalDateTime retryThreshold);
    
    // 長時間PENDINGの取引（タイムアウト対象）
    @Query("SELECT spt FROM SfrPurchaseTransaction spt " +
           "WHERE spt.status IN ('PENDING', 'PROCESSING') " +
           "AND spt.createdAt < :timeoutThreshold")
    List<SfrPurchaseTransaction> findTimedOutTransactions(
        @Param("timeoutThreshold") LocalDateTime timeoutThreshold);
    
    // スペース別購入統計
    @Query("SELECT spt.spaceId, " +
           "COUNT(spt) as transactionCount, " +
           "SUM(spt.yenAmount) as totalYenAmount, " +
           "SUM(spt.sfrAmount) as totalSfrAmount " +
           "FROM SfrPurchaseTransaction spt " +
           "WHERE spt.status = 'COMPLETED' " +
           "AND spt.completedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY spt.spaceId " +
           "ORDER BY SUM(spt.yenAmount) DESC")
    List<Object[]> getSpacePurchaseStatistics(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);
}
