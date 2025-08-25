package com.sfr.tokyo.sfr_backend.repository.crypto;

import com.sfr.tokyo.sfr_backend.entity.crypto.BalanceHistory;
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
 * BalanceHistoryRepository
 * SFR 残高履歴のデータアクセス層
 */
@Repository
public interface BalanceHistoryRepository
                extends JpaRepository<BalanceHistory, String>, JpaSpecificationExecutor<BalanceHistory> {

        // ===== 基本検索メソッド =====
        List<BalanceHistory> findByUserId(UUID userId);

        Page<BalanceHistory> findByUserId(UUID userId, Pageable pageable);

        // String userIdバージョン
        Page<BalanceHistory> findByUserId(String userId, Pageable pageable);

        List<BalanceHistory> findByTransactionType(BalanceHistory.TransactionType transactionType);

        // フィルタリング用メソッド
        Page<BalanceHistory> findByUserIdAndTransactionType(String userId,
                        BalanceHistory.TransactionType transactionType, Pageable pageable);

        Page<BalanceHistory> findByUserIdAndCreatedAtBetween(String userId,
                        LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

        Page<BalanceHistory> findByUserIdAndTransactionTypeAndCreatedAtBetween(String userId,
                        BalanceHistory.TransactionType transactionType,
                        LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

        // ===== 金額・日時ベース検索 =====
        @Query("SELECT bh FROM BalanceHistory bh WHERE bh.amount >= :minAmount ORDER BY bh.amount DESC")
        List<BalanceHistory> findByAmountGreaterThanEqual(@Param("minAmount") BigDecimal minAmount, Pageable pageable);

        @Query("SELECT bh FROM BalanceHistory bh WHERE bh.balanceAfter >= :minBalance ORDER BY bh.balanceAfter DESC")
        List<BalanceHistory> findByBalanceAfterGreaterThanEqual(@Param("minBalance") BigDecimal minBalance,
                        Pageable pageable);

        @Query("SELECT bh FROM BalanceHistory bh WHERE bh.createdAt BETWEEN :startDate AND :endDate ORDER BY bh.createdAt DESC")
        Page<BalanceHistory> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);

        @Query("SELECT bh FROM BalanceHistory bh WHERE bh.createdAt >= :since ORDER BY bh.createdAt DESC")
        Page<BalanceHistory> findByCreatedAtAfter(@Param("since") LocalDateTime since, Pageable pageable);

        // ===== ユーザー別統計メソッド =====
        @Query("SELECT SUM(bh.amount) FROM BalanceHistory bh WHERE bh.userId = :userId AND bh.transactionType = 'INCREASE'")
        BigDecimal getTotalIncreaseByUser(@Param("userId") UUID userId);

        @Query("SELECT SUM(bh.amount) FROM BalanceHistory bh WHERE bh.userId = :userId AND bh.transactionType = 'DECREASE'")
        BigDecimal getTotalDecreaseByUser(@Param("userId") UUID userId);

        @Query("SELECT COUNT(bh) FROM BalanceHistory bh WHERE bh.userId = :userId")
        Long countBalanceChangesByUser(@Param("userId") UUID userId);

        @Query("SELECT MAX(bh.balanceAfter) FROM BalanceHistory bh WHERE bh.userId = :userId")
        BigDecimal getMaxBalanceByUser(@Param("userId") UUID userId);

        @Query("SELECT MIN(bh.balanceAfter) FROM BalanceHistory bh WHERE bh.userId = :userId")
        BigDecimal getMinBalanceByUser(@Param("userId") UUID userId);

        // ===== 時間ベース集計 =====
        @Query("SELECT DATE(bh.createdAt) as date, COUNT(bh), SUM(bh.amount) FROM BalanceHistory bh WHERE bh.createdAt >= :since GROUP BY DATE(bh.createdAt) ORDER BY DATE(bh.createdAt) DESC")
        List<Object[]> getDailyBalanceChangeStatistics(@Param("since") LocalDateTime since);

        @Query("SELECT HOUR(bh.createdAt) as hour, COUNT(bh), AVG(bh.amount) FROM BalanceHistory bh WHERE bh.createdAt >= :since GROUP BY HOUR(bh.createdAt) ORDER BY HOUR(bh.createdAt)")
        List<Object[]> getHourlyBalanceChangeStatistics(@Param("since") LocalDateTime since);

        // ===== 残高トレンド分析 =====
        @Query("SELECT DATE(bh.createdAt) as date, AVG(bh.balanceAfter) as avgBalance FROM BalanceHistory bh WHERE bh.userId = :userId AND bh.createdAt >= :since GROUP BY DATE(bh.createdAt) ORDER BY DATE(bh.createdAt) ASC")
        List<Object[]> getUserBalanceTrend(@Param("userId") UUID userId, @Param("since") LocalDateTime since);

        @Query("SELECT bh.transactionType, COUNT(bh), SUM(bh.amount), AVG(bh.amount) FROM BalanceHistory bh WHERE bh.userId = :userId GROUP BY bh.transactionType ORDER BY SUM(bh.amount) DESC")
        List<Object[]> getUserBalanceChangeTypeStatistics(@Param("userId") UUID userId);

        // ===== 複合条件検索 =====
        @Query("SELECT bh FROM BalanceHistory bh WHERE bh.userId = :userId AND bh.transactionType = :transactionType AND bh.createdAt BETWEEN :startDate AND :endDate ORDER BY bh.createdAt DESC")
        Page<BalanceHistory> findByUserAndTypeAndDateRange(@Param("userId") UUID userId,
                        @Param("transactionType") BalanceHistory.TransactionType transactionType,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);
}
