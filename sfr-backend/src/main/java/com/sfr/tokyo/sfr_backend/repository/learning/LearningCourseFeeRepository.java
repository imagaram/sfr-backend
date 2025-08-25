package com.sfr.tokyo.sfr_backend.repository.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningCourseFee;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningCourseFee.FeeType;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningCourseFee.CurrencyType;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningCourseFee.SfrRewardCondition;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningCourseFee.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * LearningCourseFee Repository
 * コース料金データアクセス層
 */
@Repository
public interface LearningCourseFeeRepository extends JpaRepository<LearningCourseFee, Long> {

    // ========== 基本検索メソッド ==========

    /**
     * アクティブな料金設定を取得
     */
    @Query("SELECT f FROM LearningCourseFee f WHERE f.isActive = true AND f.deletedAt IS NULL ORDER BY f.createdAt DESC")
    List<LearningCourseFee> findActiveFees();

    /**
     * コースID別料金設定取得
     */
    @Query("SELECT f FROM LearningCourseFee f WHERE f.courseId = :courseId AND f.isActive = true AND f.deletedAt IS NULL ORDER BY f.createdAt DESC")
    List<LearningCourseFee> findByCourseId(@Param("courseId") Long courseId);

    /**
     * コースIDの現在有効な料金設定取得
     */
    @Query("SELECT f FROM LearningCourseFee f WHERE f.courseId = :courseId AND f.isActive = true AND f.deletedAt IS NULL ORDER BY f.updatedAt DESC")
    Optional<LearningCourseFee> findCurrentFeeByCoursId(@Param("courseId") Long courseId);

    /**
     * 料金タイプ別検索
     */
    @Query("SELECT f FROM LearningCourseFee f WHERE f.feeType = :feeType AND f.isActive = true AND f.deletedAt IS NULL ORDER BY f.priceAmount ASC")
    List<LearningCourseFee> findByFeeType(@Param("feeType") FeeType feeType);

    /**
     * 通貨タイプ別検索
     */
    @Query("SELECT f FROM LearningCourseFee f WHERE f.currencyType = :currencyType AND f.isActive = true AND f.deletedAt IS NULL ORDER BY f.priceAmount ASC")
    List<LearningCourseFee> findByCurrencyType(@Param("currencyType") CurrencyType currencyType);

    // ========== 価格範囲検索 ==========

    /**
     * 価格範囲検索
     */
    @Query("SELECT f FROM LearningCourseFee f WHERE f.priceAmount BETWEEN :minPrice AND :maxPrice AND f.isActive = true AND f.deletedAt IS NULL ORDER BY f.priceAmount ASC")
    List<LearningCourseFee> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice);

    /**
     * 予算以下の料金検索
     */
    @Query("SELECT f FROM LearningCourseFee f WHERE f.priceAmount <= :budget AND f.isActive = true AND f.deletedAt IS NULL ORDER BY f.priceAmount DESC")
    List<LearningCourseFee> findWithinBudget(@Param("budget") BigDecimal budget);

    /**
     * 無料コース検索
     */
    @Query("SELECT f FROM LearningCourseFee f WHERE f.isFree = true AND f.isActive = true AND f.deletedAt IS NULL ORDER BY f.createdAt DESC")
    List<LearningCourseFee> findFreeCourseFees();

    /**
     * 有料コース検索
     */
    @Query("SELECT f FROM LearningCourseFee f WHERE f.isFree = false AND f.priceAmount > 0 AND f.isActive = true AND f.deletedAt IS NULL ORDER BY f.priceAmount ASC")
    List<LearningCourseFee> findPaidCourseFees();

    // ========== SFR報酬関連検索 ==========

    /**
     * SFR報酬ありコース検索
     */
    @Query("SELECT f FROM LearningCourseFee f WHERE f.sfrRewardAmount > 0 AND f.sfrRewardCondition IS NOT NULL AND f.sfrRewardCondition != 'NONE' AND f.isActive = true AND f.deletedAt IS NULL ORDER BY f.sfrRewardAmount DESC")
    List<LearningCourseFee> findCoursesWithSfrReward();

    /**
     * SFR報酬条件別検索
     */
    @Query("SELECT f FROM LearningCourseFee f WHERE f.sfrRewardCondition = :condition AND f.sfrRewardAmount > 0 AND f.isActive = true AND f.deletedAt IS NULL ORDER BY f.sfrRewardAmount DESC")
    List<LearningCourseFee> findBySfrRewardCondition(@Param("condition") SfrRewardCondition condition);

    /**
     * SFR報酬額範囲検索
     */
    @Query("SELECT f FROM LearningCourseFee f WHERE f.sfrRewardAmount BETWEEN :minReward AND :maxReward AND f.isActive = true AND f.deletedAt IS NULL ORDER BY f.sfrRewardAmount DESC")
    List<LearningCourseFee> findBySfrRewardRange(@Param("minReward") BigDecimal minReward,
            @Param("maxReward") BigDecimal maxReward);

    /**
     * 高SFR報酬コース検索（上位N件）
     */
    @Query("SELECT f FROM LearningCourseFee f WHERE f.sfrRewardAmount > 0 AND f.isActive = true AND f.deletedAt IS NULL ORDER BY f.sfrRewardAmount DESC")
    List<LearningCourseFee> findTopSfrRewardCourses(Pageable pageable);

    // ========== 割引関連検索 ==========

    /**
     * 割引中のコース検索
     */
    @Query("SELECT f FROM LearningCourseFee f WHERE ((f.discountPercent > 0 AND f.discountStartAt <= :now AND f.discountEndAt >= :now) OR (f.earlyBirdDiscount = true AND f.earlyBirdPercent > 0 AND f.earlyBirdDeadline >= :now)) AND f.isActive = true AND f.deletedAt IS NULL ORDER BY f.discountPercent DESC, f.earlyBirdPercent DESC")
    List<LearningCourseFee> findDiscountedCourses(@Param("now") LocalDateTime now);

    /**
     * 早期割引中のコース検索
     */
    @Query("SELECT f FROM LearningCourseFee f WHERE f.earlyBirdDiscount = true AND f.earlyBirdPercent > 0 AND f.earlyBirdDeadline >= :now AND f.isActive = true AND f.deletedAt IS NULL ORDER BY f.earlyBirdPercent DESC")
    List<LearningCourseFee> findEarlyBirdDiscountCourses(@Param("now") LocalDateTime now);

    /**
     * 通常割引中のコース検索
     */
    @Query("SELECT f FROM LearningCourseFee f WHERE f.discountPercent > 0 AND f.discountStartAt <= :now AND f.discountEndAt >= :now AND f.isActive = true AND f.deletedAt IS NULL ORDER BY f.discountPercent DESC")
    List<LearningCourseFee> findRegularDiscountCourses(@Param("now") LocalDateTime now);

    /**
     * 特別価格コース検索
     */
    @Query("SELECT f FROM LearningCourseFee f WHERE f.specialPrice = true AND f.isActive = true AND f.deletedAt IS NULL ORDER BY f.priceAmount ASC")
    List<LearningCourseFee> findSpecialPriceCourses();

    // ========== 支払い方法関連検索 ==========

    /**
     * 支払い方法別検索
     */
    @Query("SELECT f FROM LearningCourseFee f WHERE f.paymentMethod = :paymentMethod AND f.isActive = true AND f.deletedAt IS NULL ORDER BY f.priceAmount ASC")
    List<LearningCourseFee> findByPaymentMethod(@Param("paymentMethod") PaymentMethod paymentMethod);

    /**
     * 分割払い可能コース検索
     */
    @Query("SELECT f FROM LearningCourseFee f WHERE f.installmentAvailable = true AND f.installmentCount > 1 AND f.isActive = true AND f.deletedAt IS NULL ORDER BY f.installmentCount ASC")
    List<LearningCourseFee> findInstallmentAvailableCourses();

    /**
     * 返金可能コース検索
     */
    @Query("SELECT f FROM LearningCourseFee f WHERE f.refundable = true AND f.refundPeriodDays > 0 AND f.isActive = true AND f.deletedAt IS NULL ORDER BY f.refundPeriodDays DESC")
    List<LearningCourseFee> findRefundableCourses();

    // ========== 複合条件検索 ==========

    /**
     * 複合条件検索
     */
    @Query("SELECT f FROM LearningCourseFee f WHERE " +
            "(:feeType IS NULL OR f.feeType = :feeType) AND " +
            "(:currencyType IS NULL OR f.currencyType = :currencyType) AND " +
            "(:minPrice IS NULL OR f.priceAmount >= :minPrice) AND " +
            "(:maxPrice IS NULL OR f.priceAmount <= :maxPrice) AND " +
            "(:isFree IS NULL OR f.isFree = :isFree) AND " +
            "(:hasSfrReward IS NULL OR (:hasSfrReward = true AND f.sfrRewardAmount > 0) OR (:hasSfrReward = false AND f.sfrRewardAmount = 0)) AND "
            +
            "(:paymentMethod IS NULL OR f.paymentMethod = :paymentMethod) AND " +
            "(:installmentAvailable IS NULL OR f.installmentAvailable = :installmentAvailable) AND " +
            "(:refundable IS NULL OR f.refundable = :refundable) AND " +
            "f.isActive = true AND f.deletedAt IS NULL " +
            "ORDER BY f.priceAmount ASC")
    Page<LearningCourseFee> searchCourses(
            @Param("feeType") FeeType feeType,
            @Param("currencyType") CurrencyType currencyType,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("isFree") Boolean isFree,
            @Param("hasSfrReward") Boolean hasSfrReward,
            @Param("paymentMethod") PaymentMethod paymentMethod,
            @Param("installmentAvailable") Boolean installmentAvailable,
            @Param("refundable") Boolean refundable,
            Pageable pageable);

    // ========== 統計・分析クエリ ==========

    /**
     * 料金タイプ別統計
     */
    @Query("SELECT f.feeType, COUNT(f), AVG(f.priceAmount), MIN(f.priceAmount), MAX(f.priceAmount) FROM LearningCourseFee f WHERE f.isActive = true AND f.deletedAt IS NULL GROUP BY f.feeType")
    List<Object[]> getFeeTypeStatistics();

    /**
     * 通貨タイプ別統計
     */
    @Query("SELECT f.currencyType, COUNT(f), AVG(f.priceAmount), SUM(f.priceAmount) FROM LearningCourseFee f WHERE f.isActive = true AND f.deletedAt IS NULL GROUP BY f.currencyType")
    List<Object[]> getCurrencyTypeStatistics();

    /**
     * SFR報酬統計
     */
    @Query("SELECT COUNT(f), AVG(f.sfrRewardAmount), SUM(f.sfrRewardAmount), MIN(f.sfrRewardAmount), MAX(f.sfrRewardAmount) FROM LearningCourseFee f WHERE f.sfrRewardAmount > 0 AND f.isActive = true AND f.deletedAt IS NULL")
    Object[] getSfrRewardStatistics();

    /**
     * 価格統計
     */
    @Query("SELECT COUNT(f), AVG(f.priceAmount), SUM(f.priceAmount), MIN(f.priceAmount), MAX(f.priceAmount) FROM LearningCourseFee f WHERE f.isFree = false AND f.isActive = true AND f.deletedAt IS NULL")
    Object[] getPriceStatistics();

    /**
     * 割引統計
     */
    @Query("SELECT COUNT(f), AVG(f.discountPercent), COUNT(CASE WHEN f.earlyBirdDiscount = true THEN 1 END), AVG(f.earlyBirdPercent) FROM LearningCourseFee f WHERE (f.discountPercent > 0 OR f.earlyBirdDiscount = true) AND f.isActive = true AND f.deletedAt IS NULL")
    Object[] getDiscountStatistics();

    /**
     * 月別作成統計
     */
    @Query("SELECT YEAR(f.createdAt), MONTH(f.createdAt), COUNT(f), AVG(f.priceAmount) FROM LearningCourseFee f WHERE f.createdAt >= :fromDate AND f.deletedAt IS NULL GROUP BY YEAR(f.createdAt), MONTH(f.createdAt) ORDER BY YEAR(f.createdAt), MONTH(f.createdAt)")
    List<Object[]> getMonthlyCreationStatistics(@Param("fromDate") LocalDateTime fromDate);

    /**
     * 人気価格帯統計
     */
    @Query("SELECT CASE " +
            "WHEN f.priceAmount = 0 THEN '無料' " +
            "WHEN f.priceAmount <= 1000 THEN '1000円以下' " +
            "WHEN f.priceAmount <= 5000 THEN '1001-5000円' " +
            "WHEN f.priceAmount <= 10000 THEN '5001-10000円' " +
            "WHEN f.priceAmount <= 50000 THEN '10001-50000円' " +
            "ELSE '50000円超' END as priceRange, " +
            "COUNT(f) FROM LearningCourseFee f WHERE f.isActive = true AND f.deletedAt IS NULL GROUP BY " +
            "CASE " +
            "WHEN f.priceAmount = 0 THEN '無料' " +
            "WHEN f.priceAmount <= 1000 THEN '1000円以下' " +
            "WHEN f.priceAmount <= 5000 THEN '1001-5000円' " +
            "WHEN f.priceAmount <= 10000 THEN '5001-10000円' " +
            "WHEN f.priceAmount <= 50000 THEN '10001-50000円' " +
            "ELSE '50000円超' END ORDER BY COUNT(f) DESC")
    List<Object[]> getPopularPriceRanges();

    // ========== 更新・削除操作 ==========

    /**
     * 料金額更新
     */
    @Modifying
    @Query("UPDATE LearningCourseFee f SET f.priceAmount = :priceAmount, f.updatedAt = :now WHERE f.id = :feeId")
    int updatePriceAmount(@Param("feeId") Long feeId, @Param("priceAmount") BigDecimal priceAmount,
            @Param("now") LocalDateTime now);

    /**
     * アクティブ状態更新
     */
    @Modifying
    @Query("UPDATE LearningCourseFee f SET f.isActive = :isActive, f.updatedAt = :now WHERE f.id = :feeId")
    int updateActiveStatus(@Param("feeId") Long feeId, @Param("isActive") Boolean isActive,
            @Param("now") LocalDateTime now);

    /**
     * SFR報酬設定更新
     */
    @Modifying
    @Query("UPDATE LearningCourseFee f SET f.sfrRewardAmount = :rewardAmount, f.sfrRewardCondition = :condition, f.updatedAt = :now WHERE f.id = :feeId")
    int updateSfrReward(@Param("feeId") Long feeId, @Param("rewardAmount") BigDecimal rewardAmount,
            @Param("condition") SfrRewardCondition condition, @Param("now") LocalDateTime now);

    /**
     * 割引設定更新
     */
    @Modifying
    @Query("UPDATE LearningCourseFee f SET f.discountPercent = :discountPercent, f.discountStartAt = :startAt, f.discountEndAt = :endAt, f.updatedAt = :now WHERE f.id = :feeId")
    int updateDiscount(@Param("feeId") Long feeId, @Param("discountPercent") BigDecimal discountPercent,
            @Param("startAt") LocalDateTime startAt, @Param("endAt") LocalDateTime endAt,
            @Param("now") LocalDateTime now);

    /**
     * 早期割引設定更新
     */
    @Modifying
    @Query("UPDATE LearningCourseFee f SET f.earlyBirdDiscount = :earlyBirdDiscount, f.earlyBirdPercent = :earlyBirdPercent, f.earlyBirdDeadline = :deadline, f.updatedAt = :now WHERE f.id = :feeId")
    int updateEarlyBirdDiscount(@Param("feeId") Long feeId, @Param("earlyBirdDiscount") Boolean earlyBirdDiscount,
            @Param("earlyBirdPercent") BigDecimal earlyBirdPercent, @Param("deadline") LocalDateTime deadline,
            @Param("now") LocalDateTime now);

    /**
     * 支払い設定更新
     */
    @Modifying
    @Query("UPDATE LearningCourseFee f SET f.paymentMethod = :paymentMethod, f.installmentAvailable = :installmentAvailable, f.installmentCount = :installmentCount, f.updatedAt = :now WHERE f.id = :feeId")
    int updatePaymentSettings(@Param("feeId") Long feeId, @Param("paymentMethod") PaymentMethod paymentMethod,
            @Param("installmentAvailable") Boolean installmentAvailable,
            @Param("installmentCount") Integer installmentCount, @Param("now") LocalDateTime now);

    /**
     * 論理削除
     */
    @Modifying
    @Query("UPDATE LearningCourseFee f SET f.deletedAt = :now WHERE f.id = :feeId")
    int softDelete(@Param("feeId") Long feeId, @Param("now") LocalDateTime now);

    /**
     * 復旧
     */
    @Modifying
    @Query("UPDATE LearningCourseFee f SET f.deletedAt = NULL, f.updatedAt = :now WHERE f.id = :feeId")
    int restore(@Param("feeId") Long feeId, @Param("now") LocalDateTime now);

    // ========== バッチ処理 ==========

    /**
     * 期限切れ早期割引を無効化
     */
    @Modifying
    @Query("UPDATE LearningCourseFee f SET f.earlyBirdDiscount = false, f.updatedAt = :now WHERE f.earlyBirdDiscount = true AND f.earlyBirdDeadline < :now")
    int disableExpiredEarlyBirdDiscounts(@Param("now") LocalDateTime now);

    /**
     * 期限切れ通常割引を無効化
     */
    @Modifying
    @Query("UPDATE LearningCourseFee f SET f.discountPercent = 0, f.updatedAt = :now WHERE f.discountPercent > 0 AND f.discountEndAt < :now")
    int disableExpiredDiscounts(@Param("now") LocalDateTime now);

    /**
     * コース毎の料金履歴
     */
    @Query("SELECT f FROM LearningCourseFee f WHERE f.courseId = :courseId AND f.deletedAt IS NULL ORDER BY f.createdAt DESC")
    List<LearningCourseFee> findFeeHistoryByCourse(@Param("courseId") Long courseId);

    /**
     * 非アクティブ料金設定取得
     */
    @Query("SELECT f FROM LearningCourseFee f WHERE f.isActive = false AND f.deletedAt IS NULL ORDER BY f.updatedAt DESC")
    List<LearningCourseFee> findInactiveFees();

    /**
     * 削除済み料金設定取得
     */
    @Query("SELECT f FROM LearningCourseFee f WHERE f.deletedAt IS NOT NULL ORDER BY f.deletedAt DESC")
    List<LearningCourseFee> findDeletedFees();

    /**
     * 更新が古い料金設定取得
     */
    @Query("SELECT f FROM LearningCourseFee f WHERE f.updatedAt < :cutoffDate AND f.isActive = true AND f.deletedAt IS NULL ORDER BY f.updatedAt ASC")
    List<LearningCourseFee> findOutdatedFees(@Param("cutoffDate") LocalDateTime cutoffDate);

    // ========== カスタム集計クエリ ==========

    /**
     * SFR総報酬額計算
     */
    @Query("SELECT COALESCE(SUM(f.sfrRewardAmount), 0) FROM LearningCourseFee f WHERE f.sfrRewardAmount > 0 AND f.isActive = true AND f.deletedAt IS NULL")
    BigDecimal calculateTotalSfrRewards();

    /**
     * 平均価格計算
     */
    @Query("SELECT COALESCE(AVG(f.priceAmount), 0) FROM LearningCourseFee f WHERE f.isFree = false AND f.isActive = true AND f.deletedAt IS NULL")
    BigDecimal calculateAveragePrice();

    /**
     * 無料コース数カウント
     */
    @Query("SELECT COUNT(f) FROM LearningCourseFee f WHERE f.isFree = true AND f.isActive = true AND f.deletedAt IS NULL")
    Long countFreeCourses();

    /**
     * 有料コース数カウント
     */
    @Query("SELECT COUNT(f) FROM LearningCourseFee f WHERE f.isFree = false AND f.priceAmount > 0 AND f.isActive = true AND f.deletedAt IS NULL")
    Long countPaidCourses();

    /**
     * SFR報酬付きコース数カウント
     */
    @Query("SELECT COUNT(f) FROM LearningCourseFee f WHERE f.sfrRewardAmount > 0 AND f.isActive = true AND f.deletedAt IS NULL")
    Long countSfrRewardCourses();

    /**
     * 割引中コース数カウント
     */
    @Query("SELECT COUNT(f) FROM LearningCourseFee f WHERE ((f.discountPercent > 0 AND f.discountStartAt <= :now AND f.discountEndAt >= :now) OR (f.earlyBirdDiscount = true AND f.earlyBirdPercent > 0 AND f.earlyBirdDeadline >= :now)) AND f.isActive = true AND f.deletedAt IS NULL")
    Long countDiscountedCourses(@Param("now") LocalDateTime now);
}
