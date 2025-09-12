package com.sfr.tokyo.sfr_backend.repository.crypto;

import com.sfr.tokyo.sfr_backend.entity.crypto.SfrPoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SFRポイント管理リポジトリ
 * 
 * Phase 1 実装: SFRサイト内ポイントシステムのデータアクセス層
 * 
 * @author SFR Development Team
 * @version 1.0
 * @since 2025-01-09
 */
@Repository
public interface SfrPointRepository extends JpaRepository<SfrPoint, Long> {

    /**
     * ユーザーID+スペースIDでSFRポイントを取得
     */
    Optional<SfrPoint> findByUserIdAndSpaceId(String userId, Long spaceId);

    /**
     * ユーザーIDでSFRポイント一覧を取得
     */
    List<SfrPoint> findByUserId(String userId);

    /**
     * スペースIDでSFRポイント一覧を取得（ページング）
     */
    Page<SfrPoint> findBySpaceId(Long spaceId, Pageable pageable);

    /**
     * アクティブステータスのSFRポイントを取得
     */
    List<SfrPoint> findByUserIdAndStatus(String userId, SfrPoint.SfrPointStatus status);

    /**
     * SFRT対象ユーザーを取得（sfrtEligible = true）
     */
    @Query("SELECT sp FROM SfrPoint sp WHERE sp.sfrtEligible = true AND sp.status = :status")
    List<SfrPoint> findBySfrtEligibleAndStatus(@Param("status") SfrPoint.SfrPointStatus status);

    /**
     * 指定期間にSFRT配布対象となったユーザーを取得
     */
    @Query("SELECT sp FROM SfrPoint sp WHERE sp.sfrtEligible = true AND sp.updatedAt >= :fromDate AND sp.updatedAt <= :toDate")
    List<SfrPoint> findBySfrtEligibleInPeriod(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);

    /**
     * 残高が指定額以上のユーザーを取得
     */
    List<SfrPoint> findByCurrentBalanceGreaterThanEqual(BigDecimal minBalance);

    /**
     * 残高が指定額以下のユーザーを取得
     */
    List<SfrPoint> findByCurrentBalanceLessThanEqual(BigDecimal maxBalance);

    /**
     * スペース内の総SFRポイント残高を計算
     */
    @Query("SELECT COALESCE(SUM(sp.currentBalance), 0) FROM SfrPoint sp WHERE sp.spaceId = :spaceId AND sp.status = :status")
    BigDecimal calculateTotalBalanceBySpace(@Param("spaceId") Long spaceId, @Param("status") SfrPoint.SfrPointStatus status);

    /**
     * ユーザーの全スペース総残高を計算
     */
    @Query("SELECT COALESCE(SUM(sp.currentBalance), 0) FROM SfrPoint sp WHERE sp.userId = :userId AND sp.status = :status")
    BigDecimal calculateTotalBalanceByUser(@Param("userId") String userId, @Param("status") SfrPoint.SfrPointStatus status);

    /**
     * 累計購入額が指定額以上のユーザーを取得
     */
    List<SfrPoint> findByTotalPurchasedGreaterThanEqual(BigDecimal minPurchased);

    /**
     * 累計使用額が指定額以上のユーザーを取得
     */
    List<SfrPoint> findByTotalSpentGreaterThanEqual(BigDecimal minSpent);

    /**
     * アクティブユーザー数をカウント
     */
    long countByStatus(SfrPoint.SfrPointStatus status);

    /**
     * スペース内のアクティブユーザー数をカウント
     */
    long countBySpaceIdAndStatus(Long spaceId, SfrPoint.SfrPointStatus status);

    /**
     * 指定期間に更新されたSFRポイントを取得
     */
    List<SfrPoint> findByUpdatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * SFRT配布が必要なユーザーを検索（最後の配布から指定期間経過）
     */
    @Query("SELECT sp FROM SfrPoint sp WHERE sp.sfrtEligible = true AND sp.status = :status AND " +
           "(sp.lastSfrtDistribution IS NULL OR sp.lastSfrtDistribution <= :thresholdDate)")
    List<SfrPoint> findUsersPendingSfrtDistribution(@Param("status") SfrPoint.SfrPointStatus status, @Param("thresholdDate") LocalDateTime thresholdDate);

    /**
     * ユーザーIDとスペースIDの組み合わせが存在するかチェック
     */
    boolean existsByUserIdAndSpaceId(String userId, Long spaceId);

    /**
     * 残高ゼロのユーザーを取得
     */
    @Query("SELECT sp FROM SfrPoint sp WHERE sp.currentBalance = 0 AND sp.status = :status")
    List<SfrPoint> findUsersWithZeroBalance(@Param("status") SfrPoint.SfrPointStatus status);

    /**
     * 高額保有者を取得（上位N人）
     */
    @Query("SELECT sp FROM SfrPoint sp WHERE sp.status = :status ORDER BY sp.currentBalance DESC")
    List<SfrPoint> findTopBalanceHolders(@Param("status") SfrPoint.SfrPointStatus status, Pageable pageable);

    /**
     * 高額購入者を取得（上位N人）
     */
    @Query("SELECT sp FROM SfrPoint sp WHERE sp.status = :status ORDER BY sp.totalPurchased DESC")
    List<SfrPoint> findTopPurchasers(@Param("status") SfrPoint.SfrPointStatus status, Pageable pageable);

    /**
     * 高額利用者を取得（上位N人）
     */
    @Query("SELECT sp FROM SfrPoint sp WHERE sp.status = :status ORDER BY sp.totalSpent DESC")
    List<SfrPoint> findTopSpenders(@Param("status") SfrPoint.SfrPointStatus status, Pageable pageable);
}
