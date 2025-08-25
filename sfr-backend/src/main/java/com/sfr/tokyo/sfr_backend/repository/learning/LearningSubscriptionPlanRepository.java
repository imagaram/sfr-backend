package com.sfr.tokyo.sfr_backend.repository.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningSubscriptionPlan;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningSubscriptionPlan.TierLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
 * LearningSubscriptionPlan Repository
 * サブスクリプションプラン管理リポジトリ
 */
@Repository
public interface LearningSubscriptionPlanRepository extends JpaRepository<LearningSubscriptionPlan, Long> {

    // ========== 基本検索系 ==========

    /**
     * アクティブプラン検索
     */
    @Query("SELECT p FROM LearningSubscriptionPlan p WHERE p.isActive = true AND p.deletedAt IS NULL ORDER BY p.sortOrder ASC")
    List<LearningSubscriptionPlan> findActivePlans();

    /**
     * 層級別プラン検索
     */
    @Query("SELECT p FROM LearningSubscriptionPlan p WHERE p.tierLevel = :tierLevel AND p.isActive = true AND p.deletedAt IS NULL ORDER BY p.sortOrder ASC")
    List<LearningSubscriptionPlan> findByTierLevel(@Param("tierLevel") TierLevel tierLevel);

    /**
     * 推奨プラン検索
     */
    @Query("SELECT p FROM LearningSubscriptionPlan p WHERE p.isRecommended = true AND p.isActive = true AND p.deletedAt IS NULL ORDER BY p.sortOrder ASC")
    List<LearningSubscriptionPlan> findRecommendedPlans();

    /**
     * 人気プラン検索
     */
    @Query("SELECT p FROM LearningSubscriptionPlan p WHERE p.isPopular = true AND p.isActive = true AND p.deletedAt IS NULL ORDER BY p.sortOrder ASC")
    List<LearningSubscriptionPlan> findPopularPlans();

    /**
     * 無料プラン検索
     */
    @Query("SELECT p FROM LearningSubscriptionPlan p WHERE p.monthlyFee = 0 AND p.isActive = true AND p.deletedAt IS NULL ORDER BY p.sortOrder ASC")
    List<LearningSubscriptionPlan> findFreePlans();

    /**
     * 試用期間ありプラン検索
     */
    @Query("SELECT p FROM LearningSubscriptionPlan p WHERE p.hasTrial = true AND p.isActive = true AND p.deletedAt IS NULL ORDER BY p.sortOrder ASC")
    List<LearningSubscriptionPlan> findTrialPlans();

    /**
     * プラン名での検索
     */
    @Query("SELECT p FROM LearningSubscriptionPlan p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.deletedAt IS NULL ORDER BY p.sortOrder ASC")
    List<LearningSubscriptionPlan> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * プラン名での完全一致検索
     */
    @Query("SELECT p FROM LearningSubscriptionPlan p WHERE p.name = :name AND p.deletedAt IS NULL")
    Optional<LearningSubscriptionPlan> findByNameExact(@Param("name") String name);

    // ========== 価格範囲検索系 ==========

    /**
     * 月額料金範囲検索
     */
    @Query("SELECT p FROM LearningSubscriptionPlan p WHERE p.monthlyFee BETWEEN :minFee AND :maxFee AND p.isActive = true AND p.deletedAt IS NULL ORDER BY p.monthlyFee ASC")
    List<LearningSubscriptionPlan> findByMonthlyFeeBetween(@Param("minFee") BigDecimal minFee,
            @Param("maxFee") BigDecimal maxFee);

    /**
     * 月額料金以下検索
     */
    @Query("SELECT p FROM LearningSubscriptionPlan p WHERE p.monthlyFee <= :maxFee AND p.isActive = true AND p.deletedAt IS NULL ORDER BY p.monthlyFee ASC")
    List<LearningSubscriptionPlan> findByMonthlyFeeLessThanEqual(@Param("maxFee") BigDecimal maxFee);

    /**
     * 年額料金範囲検索
     */
    @Query("SELECT p FROM LearningSubscriptionPlan p WHERE p.yearlyFee BETWEEN :minFee AND :maxFee AND p.isActive = true AND p.deletedAt IS NULL ORDER BY p.yearlyFee ASC")
    List<LearningSubscriptionPlan> findByYearlyFeeBetween(@Param("minFee") BigDecimal minFee,
            @Param("maxFee") BigDecimal maxFee);

    // ========== 条件組み合わせ検索系 ==========

    /**
     * 複合条件検索
     */
    @Query("""
            SELECT p FROM LearningSubscriptionPlan p
            WHERE (:tierLevel IS NULL OR p.tierLevel = :tierLevel)
            AND (:minFee IS NULL OR p.monthlyFee >= :minFee)
            AND (:maxFee IS NULL OR p.monthlyFee <= :maxFee)
            AND (:hasTrial IS NULL OR p.hasTrial = :hasTrial)
            AND (:maxUsers IS NULL OR p.maxUsers >= :maxUsers)
            AND p.isActive = true AND p.deletedAt IS NULL
            ORDER BY p.sortOrder ASC
            """)
    Page<LearningSubscriptionPlan> searchPlans(
            @Param("tierLevel") TierLevel tierLevel,
            @Param("minFee") BigDecimal minFee,
            @Param("maxFee") BigDecimal maxFee,
            @Param("hasTrial") Boolean hasTrial,
            @Param("maxUsers") Integer maxUsers,
            Pageable pageable);

    /**
     * エンタープライズプラン検索
     */
    @Query("SELECT p FROM LearningSubscriptionPlan p WHERE p.tierLevel IN ('ENTERPRISE', 'BUSINESS', 'UNLIMITED') AND p.isActive = true AND p.deletedAt IS NULL ORDER BY p.sortOrder ASC")
    List<LearningSubscriptionPlan> findEnterprisePlans();

    /**
     * 教育プラン検索
     */
    @Query("SELECT p FROM LearningSubscriptionPlan p WHERE p.tierLevel IN ('STUDENT', 'TEACHER') AND p.isActive = true AND p.deletedAt IS NULL ORDER BY p.sortOrder ASC")
    List<LearningSubscriptionPlan> findEducationalPlans();

    /**
     * 個人プラン検索
     */
    @Query("SELECT p FROM LearningSubscriptionPlan p WHERE p.tierLevel IN ('FREE', 'BASIC', 'STANDARD', 'PREMIUM') AND p.isActive = true AND p.deletedAt IS NULL ORDER BY p.sortOrder ASC")
    List<LearningSubscriptionPlan> findPersonalPlans();

    // ========== ユーザー容量・制限検索系 ==========

    /**
     * 最大ユーザー数以上のプラン検索
     */
    @Query("SELECT p FROM LearningSubscriptionPlan p WHERE (p.maxUsers IS NULL OR p.maxUsers >= :requiredUsers) AND p.isActive = true AND p.deletedAt IS NULL ORDER BY p.monthlyFee ASC")
    List<LearningSubscriptionPlan> findPlansForUserCount(@Param("requiredUsers") Integer requiredUsers);

    /**
     * ストレージ容量以上のプラン検索
     */
    @Query("SELECT p FROM LearningSubscriptionPlan p WHERE (p.maxStorageGb IS NULL OR p.maxStorageGb >= :requiredStorage) AND p.isActive = true AND p.deletedAt IS NULL ORDER BY p.monthlyFee ASC")
    List<LearningSubscriptionPlan> findPlansForStorageRequirement(@Param("requiredStorage") Integer requiredStorage);

    /**
     * 試用期間日数以上のプラン検索
     */
    @Query("SELECT p FROM LearningSubscriptionPlan p WHERE p.hasTrial = true AND p.trialDays >= :minTrialDays AND p.isActive = true AND p.deletedAt IS NULL ORDER BY p.trialDays DESC")
    List<LearningSubscriptionPlan> findPlansWithMinimumTrialDays(@Param("minTrialDays") Integer minTrialDays);

    // ========== 統計・集計系 ==========

    /**
     * 層級別プラン数カウント
     */
    @Query("SELECT p.tierLevel, COUNT(p) FROM LearningSubscriptionPlan p WHERE p.isActive = true AND p.deletedAt IS NULL GROUP BY p.tierLevel")
    List<Object[]> countPlansByTierLevel();

    /**
     * 価格帯別プラン数統計
     */
    @Query("""
            SELECT
                CASE
                    WHEN p.monthlyFee = 0 THEN 'FREE'
                    WHEN p.monthlyFee <= 1000 THEN 'LOW'
                    WHEN p.monthlyFee <= 5000 THEN 'MEDIUM'
                    WHEN p.monthlyFee <= 10000 THEN 'HIGH'
                    ELSE 'PREMIUM'
                END as priceRange,
                COUNT(p)
            FROM LearningSubscriptionPlan p
            WHERE p.isActive = true AND p.deletedAt IS NULL
            GROUP BY
                CASE
                    WHEN p.monthlyFee = 0 THEN 'FREE'
                    WHEN p.monthlyFee <= 1000 THEN 'LOW'
                    WHEN p.monthlyFee <= 5000 THEN 'MEDIUM'
                    WHEN p.monthlyFee <= 10000 THEN 'HIGH'
                    ELSE 'PREMIUM'
                END
            """)
    List<Object[]> getPriceRangeStatistics();

    /**
     * 平均月額料金計算
     */
    @Query("SELECT AVG(p.monthlyFee) FROM LearningSubscriptionPlan p WHERE p.monthlyFee > 0 AND p.isActive = true AND p.deletedAt IS NULL")
    BigDecimal calculateAverageMonthlyFee();

    /**
     * 最安・最高価格取得
     */
    @Query("SELECT MIN(p.monthlyFee), MAX(p.monthlyFee) FROM LearningSubscriptionPlan p WHERE p.monthlyFee > 0 AND p.isActive = true AND p.deletedAt IS NULL")
    Object[] getMinMaxPrices();

    /**
     * 試用期間統計
     */
    @Query("SELECT COUNT(p), AVG(p.trialDays) FROM LearningSubscriptionPlan p WHERE p.hasTrial = true AND p.isActive = true AND p.deletedAt IS NULL")
    Object[] getTrialStatistics();

    // ========== 表示順序・ソート系 ==========

    /**
     * 表示順序での取得
     */
    @Query("SELECT p FROM LearningSubscriptionPlan p WHERE p.isActive = true AND p.deletedAt IS NULL ORDER BY p.sortOrder ASC, p.monthlyFee ASC")
    List<LearningSubscriptionPlan> findAllOrderBySortOrder();

    /**
     * 価格順での取得
     */
    @Query("SELECT p FROM LearningSubscriptionPlan p WHERE p.isActive = true AND p.deletedAt IS NULL ORDER BY p.monthlyFee ASC, p.sortOrder ASC")
    List<LearningSubscriptionPlan> findAllOrderByPrice();

    /**
     * 人気順での取得
     */
    @Query("SELECT p FROM LearningSubscriptionPlan p WHERE p.isActive = true AND p.deletedAt IS NULL ORDER BY p.isPopular DESC, p.isRecommended DESC, p.sortOrder ASC")
    List<LearningSubscriptionPlan> findAllOrderByPopularity();

    /**
     * 最新順での取得
     */
    @Query("SELECT p FROM LearningSubscriptionPlan p WHERE p.isActive = true AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<LearningSubscriptionPlan> findAllOrderByNewest();

    // ========== 管理系操作 ==========

    /**
     * 表示順序更新
     */
    @Modifying
    @Transactional
    @Query("UPDATE LearningSubscriptionPlan p SET p.sortOrder = :sortOrder, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :planId")
    void updateSortOrder(@Param("planId") Long planId, @Param("sortOrder") Integer sortOrder);

    /**
     * アクティブ状態更新
     */
    @Modifying
    @Transactional
    @Query("UPDATE LearningSubscriptionPlan p SET p.isActive = :isActive, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :planId")
    void updateActiveStatus(@Param("planId") Long planId, @Param("isActive") Boolean isActive);

    /**
     * 推奨状態更新
     */
    @Modifying
    @Transactional
    @Query("UPDATE LearningSubscriptionPlan p SET p.isRecommended = :isRecommended, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :planId")
    void updateRecommendedStatus(@Param("planId") Long planId, @Param("isRecommended") Boolean isRecommended);

    /**
     * 人気状態更新
     */
    @Modifying
    @Transactional
    @Query("UPDATE LearningSubscriptionPlan p SET p.isPopular = :isPopular, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :planId")
    void updatePopularStatus(@Param("planId") Long planId, @Param("isPopular") Boolean isPopular);

    /**
     * 月額料金更新
     */
    @Modifying
    @Transactional
    @Query("UPDATE LearningSubscriptionPlan p SET p.monthlyFee = :monthlyFee, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :planId")
    void updateMonthlyFee(@Param("planId") Long planId, @Param("monthlyFee") BigDecimal monthlyFee);

    /**
     * 年額料金更新
     */
    @Modifying
    @Transactional
    @Query("UPDATE LearningSubscriptionPlan p SET p.yearlyFee = :yearlyFee, p.yearlyDiscountPercent = :discountPercent, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :planId")
    void updateYearlyFee(@Param("planId") Long planId, @Param("yearlyFee") BigDecimal yearlyFee,
            @Param("discountPercent") BigDecimal discountPercent);

    /**
     * 試用期間更新
     */
    @Modifying
    @Transactional
    @Query("UPDATE LearningSubscriptionPlan p SET p.hasTrial = :hasTrial, p.trialDays = :trialDays, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :planId")
    void updateTrialSettings(@Param("planId") Long planId, @Param("hasTrial") Boolean hasTrial,
            @Param("trialDays") Integer trialDays);

    /**
     * 容量制限更新
     */
    @Modifying
    @Transactional
    @Query("UPDATE LearningSubscriptionPlan p SET p.maxUsers = :maxUsers, p.maxStorageGb = :maxStorageGb, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :planId")
    void updateCapacityLimits(@Param("planId") Long planId, @Param("maxUsers") Integer maxUsers,
            @Param("maxStorageGb") Integer maxStorageGb);

    /**
     * 機能一覧更新
     */
    @Modifying
    @Transactional
    @Query("UPDATE LearningSubscriptionPlan p SET p.features = :features, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :planId")
    void updateFeatures(@Param("planId") Long planId, @Param("features") String features);

    /**
     * 論理削除
     */
    @Modifying
    @Transactional
    @Query("UPDATE LearningSubscriptionPlan p SET p.deletedAt = CURRENT_TIMESTAMP WHERE p.id = :planId")
    void softDeletePlan(@Param("planId") Long planId);

    /**
     * 削除復旧
     */
    @Modifying
    @Transactional
    @Query("UPDATE LearningSubscriptionPlan p SET p.deletedAt = NULL, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :planId")
    void restorePlan(@Param("planId") Long planId);

    // ========== 推奨プラン管理系 ==========

    /**
     * 全推奨状態リセット
     */
    @Modifying
    @Transactional
    @Query("UPDATE LearningSubscriptionPlan p SET p.isRecommended = false, p.updatedAt = CURRENT_TIMESTAMP WHERE p.isRecommended = true")
    void clearAllRecommendedStatus();

    /**
     * 全人気状態リセット
     */
    @Modifying
    @Transactional
    @Query("UPDATE LearningSubscriptionPlan p SET p.isPopular = false, p.updatedAt = CURRENT_TIMESTAMP WHERE p.isPopular = true")
    void clearAllPopularStatus();

    /**
     * 層級内での表示順序最大値取得
     */
    @Query("SELECT COALESCE(MAX(p.sortOrder), 0) FROM LearningSubscriptionPlan p WHERE p.tierLevel = :tierLevel AND p.deletedAt IS NULL")
    Integer getMaxSortOrderByTierLevel(@Param("tierLevel") TierLevel tierLevel);

    /**
     * 層級内での表示順序再整理
     */
    @Modifying
    @Transactional
    @Query(value = """
            UPDATE learning_subscription_plans p1
            SET sort_order = (
                SELECT row_number - 1
                FROM (
                    SELECT id, ROW_NUMBER() OVER (ORDER BY sort_order, id) as row_number
                    FROM learning_subscription_plans
                    WHERE tier_level = :tierLevel AND deleted_at IS NULL
                ) p2
                WHERE p1.id = p2.id
            ),
            updated_at = CURRENT_TIMESTAMP
            WHERE tier_level = :tierLevel AND deleted_at IS NULL
            """, nativeQuery = true)
    void reorderPlansInTierLevel(@Param("tierLevel") String tierLevel);

    // ========== キーワード検索系 ==========

    /**
     * 全文検索
     */
    @Query("""
            SELECT p FROM LearningSubscriptionPlan p
            WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.features) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.specialFeatures) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND p.isActive = true AND p.deletedAt IS NULL
            ORDER BY p.sortOrder ASC
            """)
    Page<LearningSubscriptionPlan> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // ========== バッチ処理系 ==========

    /**
     * 非アクティブプラン検索（管理者用）
     */
    @Query("SELECT p FROM LearningSubscriptionPlan p WHERE p.isActive = false AND p.deletedAt IS NULL ORDER BY p.updatedAt DESC")
    List<LearningSubscriptionPlan> findInactivePlans();

    /**
     * 削除済みプラン検索（管理者用）
     */
    @Query("SELECT p FROM LearningSubscriptionPlan p WHERE p.deletedAt IS NOT NULL ORDER BY p.deletedAt DESC")
    List<LearningSubscriptionPlan> findDeletedPlans();

    /**
     * 更新が古いプラン検索
     */
    @Query("SELECT p FROM LearningSubscriptionPlan p WHERE p.updatedAt < :cutoffDate AND p.isActive = true AND p.deletedAt IS NULL ORDER BY p.updatedAt ASC")
    List<LearningSubscriptionPlan> findPlansNotUpdatedSince(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * 一括アクティブ化
     */
    @Modifying
    @Transactional
    @Query("UPDATE LearningSubscriptionPlan p SET p.isActive = true, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id IN :planIds")
    void activateMultiplePlans(@Param("planIds") List<Long> planIds);

    /**
     * 一括非アクティブ化
     */
    @Modifying
    @Transactional
    @Query("UPDATE LearningSubscriptionPlan p SET p.isActive = false, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id IN :planIds")
    void deactivateMultiplePlans(@Param("planIds") List<Long> planIds);

    /**
     * 表示順序一括更新
     */
    @Modifying
    @Transactional
    @Query(value = """
            UPDATE learning_subscription_plans
            SET sort_order = :sortOrder + (ROW_NUMBER() OVER (ORDER BY sort_order, id) - 1),
                updated_at = CURRENT_TIMESTAMP
            WHERE id IN :planIds
            """, nativeQuery = true)
    void updateMultipleSortOrders(@Param("planIds") List<Long> planIds, @Param("sortOrder") Integer sortOrder);

    // ========== カスタム統計クエリ ==========

    /**
     * 詳細統計情報
     */
    @Query("""
            SELECT
                COUNT(p) as totalPlans,
                COUNT(CASE WHEN p.isActive = true THEN 1 END) as activePlans,
                COUNT(CASE WHEN p.monthlyFee = 0 THEN 1 END) as freePlans,
                COUNT(CASE WHEN p.hasTrial = true THEN 1 END) as trialPlans,
                COUNT(CASE WHEN p.isRecommended = true THEN 1 END) as recommendedPlans,
                COUNT(CASE WHEN p.isPopular = true THEN 1 END) as popularPlans,
                AVG(p.monthlyFee) as avgMonthlyFee,
                MIN(p.monthlyFee) as minMonthlyFee,
                MAX(p.monthlyFee) as maxMonthlyFee
            FROM LearningSubscriptionPlan p
            WHERE p.deletedAt IS NULL
            """)
    Object[] getDetailedStatistics();

    /**
     * 月別作成統計
     */
    @Query("""
            SELECT DATE_FORMAT(p.createdAt, '%Y-%m') as month, COUNT(p) as count
            FROM LearningSubscriptionPlan p
            WHERE p.createdAt >= :fromDate AND p.deletedAt IS NULL
            GROUP BY DATE_FORMAT(p.createdAt, '%Y-%m')
            ORDER BY month
            """)
    List<Object[]> getMonthlyCreationStatistics(@Param("fromDate") LocalDateTime fromDate);
}
