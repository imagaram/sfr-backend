package com.sfr.tokyo.sfr_backend.repository.crypto;

import com.sfr.tokyo.sfr_backend.entity.crypto.SfrPurchaseTransaction;
import com.sfr.tokyo.sfr_backend.entity.crypto.SfrPurchaseTransaction.PurchaseTransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SFR購入取引リポジトリ
 * Phase 1 実装: SFR購入取引履歴管理
 * 
 * @author SFR Development Team
 * @version 1.0
 * @since 2025-01-09
 */
@Repository
public interface SfrPurchaseTransactionRepository extends JpaRepository<SfrPurchaseTransaction, Long> {

    /**
     * ユーザーの購入履歴を時系列で取得
     */
    List<SfrPurchaseTransaction> findByUserIdAndSpaceIdOrderByCreatedAtDesc(String userId, Long spaceId);

    /**
     * Stripe PaymentIntent IDで取引検索
     */
    Optional<SfrPurchaseTransaction> findByStripePaymentIntentId(String stripePaymentIntentId);

    /**
     * ステータス別の取引検索
     */
    List<SfrPurchaseTransaction> findByStatusAndSpaceIdOrderByCreatedAtDesc(PurchaseTransactionStatus status, Long spaceId);

    /**
     * 期間内の購入取引検索
     */
    List<SfrPurchaseTransaction> findByUserIdAndSpaceIdAndCreatedAtBetween(
            String userId, Long spaceId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * ユーザーの日次購入合計金額
     */
    @Query("SELECT COALESCE(SUM(t.yenAmount), 0) FROM SfrPurchaseTransaction t " +
           "WHERE t.userId = :userId AND t.spaceId = :spaceId " +
           "AND t.status = 'COMPLETED' " +
           "AND DATE(t.createdAt) = DATE(:targetDate)")
    BigDecimal getDailyPurchaseSum(@Param("userId") String userId, 
                                  @Param("spaceId") Long spaceId, 
                                  @Param("targetDate") LocalDateTime targetDate);

    /**
     * ユーザーの月次購入合計金額
     */
    @Query("SELECT COALESCE(SUM(t.yenAmount), 0) FROM SfrPurchaseTransaction t " +
           "WHERE t.userId = :userId AND t.spaceId = :spaceId " +
           "AND t.status = 'COMPLETED' " +
           "AND YEAR(t.createdAt) = YEAR(:targetDate) " +
           "AND MONTH(t.createdAt) = MONTH(:targetDate)")
    BigDecimal getMonthlyPurchaseSum(@Param("userId") String userId, 
                                    @Param("spaceId") Long spaceId, 
                                    @Param("targetDate") LocalDateTime targetDate);

    /**
     * スペース全体の購入統計
     */
    @Query("SELECT COUNT(t), COALESCE(SUM(t.yenAmount), 0), COALESCE(SUM(t.sfrAmount), 0) " +
           "FROM SfrPurchaseTransaction t " +
           "WHERE t.spaceId = :spaceId AND t.status = 'COMPLETED' " +
           "AND t.createdAt >= :startTime")
    Object[] getSpacePurchaseStats(@Param("spaceId") Long spaceId, 
                                  @Param("startTime") LocalDateTime startTime);

    /**
     * 未完了取引のクリーンアップ
     */
    @Query("SELECT t FROM SfrPurchaseTransaction t " +
           "WHERE t.status IN ('PENDING', 'PROCESSING') " +
           "AND t.createdAt < :expiredTime")
    List<SfrPurchaseTransaction> findExpiredPendingTransactions(@Param("expiredTime") LocalDateTime expiredTime);

    /**
     * 購入取引の存在確認
     */
    boolean existsByUserIdAndStripePaymentIntentId(String userId, String stripePaymentIntentId);
}
