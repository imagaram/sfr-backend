package com.sfr.tokyo.sfr_backend.repository.shop;

import com.sfr.tokyo.sfr_backend.entity.shop.LimitedItemPurchase;
import com.sfr.tokyo.sfr_backend.entity.shop.LimitedItemPurchase.PurchaseStatus;
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

/**
 * LimitedItemPurchase Repository - 限定品購入取引データアクセス層
 * Phase 2.2: SFR決済システム
 * 
 * @author SFR Development Team
 * @version 1.0
 * @since 2025-01-09
 */
@Repository
public interface LimitedItemPurchaseRepository 
        extends JpaRepository<LimitedItemPurchase, Long>, JpaSpecificationExecutor<LimitedItemPurchase> {

    // ===== 基本検索メソッド =====

    /**
     * 購入者の取引履歴を取得
     */
    Page<LimitedItemPurchase> findByBuyerIdOrderByCreatedAtDesc(String buyerId, Pageable pageable);

    /**
     * 販売者の取引履歴を取得
     */
    Page<LimitedItemPurchase> findBySellerIdOrderByCreatedAtDesc(String sellerId, Pageable pageable);

    /**
     * 商品別の購入履歴を取得
     */
    List<LimitedItemPurchase> findByItemIdAndStatusOrderByCreatedAtDesc(Long itemId, PurchaseStatus status);

    /**
     * 購入者とステータスで検索
     */
    List<LimitedItemPurchase> findByBuyerIdAndStatus(String buyerId, PurchaseStatus status);

    /**
     * 販売者とステータスで検索
     */
    List<LimitedItemPurchase> findBySellerIdAndStatus(String sellerId, PurchaseStatus status);

    // ===== 統計・集計メソッド =====

    /**
     * 購入者の総購入額（SFR）
     */
    @Query("SELECT COALESCE(SUM(p.paymentAmountSfr), 0) FROM LimitedItemPurchase p WHERE " +
           "p.buyerId = :buyerId AND p.status IN ('COMPLETED', 'SHIPPED', 'RECEIVED')")
    BigDecimal getTotalPurchaseAmountForBuyer(@Param("buyerId") String buyerId);

    /**
     * 販売者の総売上額（SFR）
     */
    @Query("SELECT COALESCE(SUM(p.totalPriceSfr), 0) FROM LimitedItemPurchase p WHERE " +
           "p.sellerId = :sellerId AND p.status IN ('COMPLETED', 'SHIPPED', 'RECEIVED')")
    BigDecimal getTotalSalesAmountForSeller(@Param("sellerId") String sellerId);

    /**
     * 商品の総販売数量
     */
    @Query("SELECT COALESCE(SUM(p.quantity), 0) FROM LimitedItemPurchase p WHERE " +
           "p.itemId = :itemId AND p.status IN ('COMPLETED', 'SHIPPED', 'RECEIVED')")
    Integer getTotalSoldQuantityForItem(@Param("itemId") Long itemId);

    /**
     * 商品の総売上額（SFR）
     */
    @Query("SELECT COALESCE(SUM(p.totalPriceSfr), 0) FROM LimitedItemPurchase p WHERE " +
           "p.itemId = :itemId AND p.status IN ('COMPLETED', 'SHIPPED', 'RECEIVED')")
    BigDecimal getTotalSalesAmountForItem(@Param("itemId") Long itemId);

    // ===== 期間別統計 =====

    /**
     * 購入者の期間別購入額
     */
    @Query("SELECT COALESCE(SUM(p.paymentAmountSfr), 0) FROM LimitedItemPurchase p WHERE " +
           "p.buyerId = :buyerId AND " +
           "p.createdAt BETWEEN :startDate AND :endDate AND " +
           "p.status IN ('COMPLETED', 'SHIPPED', 'RECEIVED')")
    BigDecimal getPurchaseAmountByPeriod(@Param("buyerId") String buyerId, 
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    /**
     * 販売者の期間別売上額
     */
    @Query("SELECT COALESCE(SUM(p.totalPriceSfr), 0) FROM LimitedItemPurchase p WHERE " +
           "p.sellerId = :sellerId AND " +
           "p.createdAt BETWEEN :startDate AND :endDate AND " +
           "p.status IN ('COMPLETED', 'SHIPPED', 'RECEIVED')")
    BigDecimal getSalesAmountByPeriod(@Param("sellerId") String sellerId, 
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * 日別売上統計
     */
    @Query("SELECT " +
           "DATE(p.createdAt) as saleDate, " +
           "COUNT(p) as transactionCount, " +
           "SUM(p.totalPriceSfr) as totalAmount " +
           "FROM LimitedItemPurchase p WHERE " +
           "p.createdAt BETWEEN :startDate AND :endDate AND " +
           "p.status IN ('COMPLETED', 'SHIPPED', 'RECEIVED') " +
           "GROUP BY DATE(p.createdAt) " +
           "ORDER BY saleDate DESC")
    List<Object[]> getDailySalesStats(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    // ===== 購入制限チェック =====

    /**
     * ユーザーの商品別購入数量をチェック
     */
    @Query("SELECT COALESCE(SUM(p.quantity), 0) FROM LimitedItemPurchase p WHERE " +
           "p.buyerId = :buyerId AND p.itemId = :itemId AND " +
           "p.status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'SHIPPED', 'RECEIVED')")
    Integer getUserPurchaseQuantityForItem(@Param("buyerId") String buyerId, 
                                          @Param("itemId") Long itemId);

    /**
     * ユーザーの日別購入額（購入制限チェック用）
     */
    @Query("SELECT COALESCE(SUM(p.paymentAmountSfr), 0) FROM LimitedItemPurchase p WHERE " +
           "p.buyerId = :buyerId AND " +
           "DATE(p.createdAt) = DATE(:targetDate) AND " +
           "p.status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'SHIPPED', 'RECEIVED')")
    BigDecimal getDailyPurchaseAmount(@Param("buyerId") String buyerId, 
                                     @Param("targetDate") LocalDateTime targetDate);

    // ===== 管理・運用メソッド =====

    /**
     * 処理待ち状態の古い取引を取得
     */
    @Query("SELECT p FROM LimitedItemPurchase p WHERE " +
           "p.status = 'PENDING' AND p.createdAt < :threshold")
    List<LimitedItemPurchase> findStuckPendingTransactions(@Param("threshold") LocalDateTime threshold);

    /**
     * 配送が必要な完了済み取引
     */
    @Query("SELECT p FROM LimitedItemPurchase p WHERE " +
           "p.status = 'COMPLETED' AND p.shippingInfo IS NOT NULL")
    List<LimitedItemPurchase> findCompletedPurchasesRequiringShipping();

    /**
     * 長期間配送中の取引（遅延アラート用）
     */
    @Query("SELECT p FROM LimitedItemPurchase p WHERE " +
           "p.status = 'SHIPPED' AND p.shippedAt < :threshold")
    List<LimitedItemPurchase> findLongShippingTransactions(@Param("threshold") LocalDateTime threshold);

    /**
     * 人気商品ランキング（購入数ベース）
     */
    @Query("SELECT " +
           "p.itemId, " +
           "p.itemTitleSnapshot, " +
           "COUNT(p) as purchaseCount, " +
           "SUM(p.quantity) as totalQuantity, " +
           "SUM(p.totalPriceSfr) as totalAmount " +
           "FROM LimitedItemPurchase p WHERE " +
           "p.createdAt BETWEEN :startDate AND :endDate AND " +
           "p.status IN ('COMPLETED', 'SHIPPED', 'RECEIVED') " +
           "GROUP BY p.itemId, p.itemTitleSnapshot " +
           "ORDER BY totalQuantity DESC")
    List<Object[]> getPopularItemsRanking(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate,
                                         Pageable pageable);

    /**
     * トップ販売者ランキング
     */
    @Query("SELECT " +
           "p.sellerId, " +
           "COUNT(p) as transactionCount, " +
           "SUM(p.totalPriceSfr) as totalSales " +
           "FROM LimitedItemPurchase p WHERE " +
           "p.createdAt BETWEEN :startDate AND :endDate AND " +
           "p.status IN ('COMPLETED', 'SHIPPED', 'RECEIVED') " +
           "GROUP BY p.sellerId " +
           "ORDER BY totalSales DESC")
    List<Object[]> getTopSellersRanking(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate,
                                       Pageable pageable);

    /**
     * エラー取引の統計
     */
    @Query("SELECT p.status, COUNT(p) FROM LimitedItemPurchase p WHERE " +
           "p.status IN ('FAILED', 'CANCELLED') AND " +
           "p.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY p.status")
    List<Object[]> getErrorTransactionStats(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    // ===== ユーザー体験・推奨機能 =====

    /**
     * ユーザーが最近購入した商品カテゴリ
     */
    @Query("SELECT DISTINCT li.category FROM LimitedItemPurchase p " +
           "JOIN LimitedItem li ON p.itemId = li.id WHERE " +
           "p.buyerId = :buyerId AND " +
           "p.createdAt >= :recentThreshold AND " +
           "p.status IN ('COMPLETED', 'SHIPPED', 'RECEIVED')")
    List<String> getRecentPurchaseCategories(@Param("buyerId") String buyerId,
                                            @Param("recentThreshold") LocalDateTime recentThreshold);

    /**
     * 類似購入者が購入した商品
     */
    @Query("SELECT DISTINCT p2.itemId FROM LimitedItemPurchase p1 " +
           "JOIN LimitedItemPurchase p2 ON p1.sellerId = p2.sellerId " +
           "WHERE p1.buyerId = :buyerId AND p1.buyerId != p2.buyerId AND " +
           "p1.status IN ('COMPLETED', 'SHIPPED', 'RECEIVED') AND " +
           "p2.status IN ('COMPLETED', 'SHIPPED', 'RECEIVED')")
    List<Long> getRecommendedItemIds(@Param("buyerId") String buyerId);
}
