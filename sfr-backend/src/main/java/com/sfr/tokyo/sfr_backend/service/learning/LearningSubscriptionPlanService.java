package com.sfr.tokyo.sfr_backend.service.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningSubscriptionPlan;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningSubscriptionPlan.TierLevel;
import com.sfr.tokyo.sfr_backend.dto.learning.LearningSubscriptionPlanDto;
import com.sfr.tokyo.sfr_backend.repository.learning.LearningSubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

/**
 * LearningSubscriptionPlan Service
 * サブスクリプションプラン管理サービス
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningSubscriptionPlanService {

    private final LearningSubscriptionPlanRepository planRepository;

    // ========== CRUD操作 ==========

    /**
     * プラン作成
     */
    @Transactional
    public LearningSubscriptionPlanDto createPlan(LearningSubscriptionPlanDto dto) {
        try {
            // 名前の重複チェック
            if (planRepository.findByNameExact(dto.getName()).isPresent()) {
                throw new RuntimeException("同じ名前のプランが既に存在します: " + dto.getName());
            }

            LearningSubscriptionPlan plan = convertToEntity(dto);

            // 表示順序の自動設定
            if (plan.getSortOrder() == null || plan.getSortOrder() == 0) {
                Integer maxOrder = planRepository.getMaxSortOrderByTierLevel(plan.getTierLevel());
                plan.setSortOrder(maxOrder + 1);
            }

            // 年額割引率の自動計算
            if (plan.getYearlyFee() != null && plan.getMonthlyFee() != null) {
                BigDecimal calculatedDiscount = plan.calculateYearlyDiscountPercent();
                plan.setYearlyDiscountPercent(calculatedDiscount);
            }

            LearningSubscriptionPlan savedPlan = planRepository.save(plan);
            log.info("サブスクリプションプランが作成されました: ID={}, Name={}, Tier={}",
                    savedPlan.getId(), savedPlan.getName(), savedPlan.getTierLevel());

            return convertToDto(savedPlan);
        } catch (Exception e) {
            log.error("プラン作成中にエラーが発生しました: {}", e.getMessage(), e);
            throw new RuntimeException("プランの作成に失敗しました", e);
        }
    }

    /**
     * プラン取得
     */
    public LearningSubscriptionPlanDto getPlan(Long planId) {
        LearningSubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("プランが見つかりません: " + planId));
        return convertToDto(plan);
    }

    /**
     * プラン更新
     */
    @Transactional
    public LearningSubscriptionPlanDto updatePlan(Long planId, LearningSubscriptionPlanDto dto) {
        try {
            LearningSubscriptionPlan plan = planRepository.findById(planId)
                    .orElseThrow(() -> new RuntimeException("プランが見つかりません: " + planId));

            // 名前重複チェック（自分以外）
            Optional<LearningSubscriptionPlan> existingPlan = planRepository.findByNameExact(dto.getName());
            if (existingPlan.isPresent() && !existingPlan.get().getId().equals(planId)) {
                throw new RuntimeException("同じ名前のプランが既に存在します: " + dto.getName());
            }

            updatePlanFields(plan, dto);

            // 年額割引率の再計算
            if (plan.getYearlyFee() != null && plan.getMonthlyFee() != null) {
                BigDecimal calculatedDiscount = plan.calculateYearlyDiscountPercent();
                plan.setYearlyDiscountPercent(calculatedDiscount);
            }

            LearningSubscriptionPlan updatedPlan = planRepository.save(plan);
            log.info("プランが更新されました: ID={}, Name={}", planId, updatedPlan.getName());

            return convertToDto(updatedPlan);
        } catch (Exception e) {
            log.error("プラン更新中にエラーが発生しました: ID={}, Error={}", planId, e.getMessage(), e);
            throw new RuntimeException("プランの更新に失敗しました", e);
        }
    }

    /**
     * プラン削除（論理削除）
     */
    @Transactional
    public void deletePlan(Long planId) {
        try {
            planRepository.softDeletePlan(planId);
            log.info("プランが削除されました: ID={}", planId);
        } catch (Exception e) {
            log.error("プラン削除中にエラーが発生しました: ID={}, Error={}", planId, e.getMessage(), e);
            throw new RuntimeException("プランの削除に失敗しました", e);
        }
    }

    /**
     * プラン復旧
     */
    @Transactional
    public void restorePlan(Long planId) {
        try {
            planRepository.restorePlan(planId);
            log.info("プランが復旧されました: ID={}", planId);
        } catch (Exception e) {
            log.error("プラン復旧中にエラーが発生しました: ID={}, Error={}", planId, e.getMessage(), e);
            throw new RuntimeException("プランの復旧に失敗しました", e);
        }
    }

    // ========== 検索操作 ==========

    /**
     * 全アクティブプラン取得
     */
    public List<LearningSubscriptionPlanDto> getAllActivePlans() {
        List<LearningSubscriptionPlan> plans = planRepository.findActivePlans();
        return plans.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 層級別プラン取得
     */
    public List<LearningSubscriptionPlanDto> getPlansByTierLevel(TierLevel tierLevel) {
        List<LearningSubscriptionPlan> plans = planRepository.findByTierLevel(tierLevel);
        return plans.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 推奨プラン取得
     */
    public List<LearningSubscriptionPlanDto> getRecommendedPlans() {
        List<LearningSubscriptionPlan> plans = planRepository.findRecommendedPlans();
        return plans.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 人気プラン取得
     */
    public List<LearningSubscriptionPlanDto> getPopularPlans() {
        List<LearningSubscriptionPlan> plans = planRepository.findPopularPlans();
        return plans.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 無料プラン取得
     */
    public List<LearningSubscriptionPlanDto> getFreePlans() {
        List<LearningSubscriptionPlan> plans = planRepository.findFreePlans();
        return plans.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 試用期間ありプラン取得
     */
    public List<LearningSubscriptionPlanDto> getTrialPlans() {
        List<LearningSubscriptionPlan> plans = planRepository.findTrialPlans();
        return plans.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * エンタープライズプラン取得
     */
    public List<LearningSubscriptionPlanDto> getEnterprisePlans() {
        List<LearningSubscriptionPlan> plans = planRepository.findEnterprisePlans();
        return plans.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 教育プラン取得
     */
    public List<LearningSubscriptionPlanDto> getEducationalPlans() {
        List<LearningSubscriptionPlan> plans = planRepository.findEducationalPlans();
        return plans.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 個人プラン取得
     */
    public List<LearningSubscriptionPlanDto> getPersonalPlans() {
        List<LearningSubscriptionPlan> plans = planRepository.findPersonalPlans();
        return plans.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 価格範囲でのプラン検索
     */
    public List<LearningSubscriptionPlanDto> getPlansByPriceRange(BigDecimal minFee, BigDecimal maxFee) {
        List<LearningSubscriptionPlan> plans = planRepository.findByMonthlyFeeBetween(minFee, maxFee);
        return plans.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 予算内プラン検索
     */
    public List<LearningSubscriptionPlanDto> getPlansWithinBudget(BigDecimal budget) {
        List<LearningSubscriptionPlan> plans = planRepository.findByMonthlyFeeLessThanEqual(budget);
        return plans.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 複合条件検索
     */
    public Page<LearningSubscriptionPlanDto> searchPlans(
            TierLevel tierLevel, BigDecimal minFee, BigDecimal maxFee,
            Boolean hasTrial, Integer maxUsers, int page, int size, String sortBy) {

        Pageable pageable = createPageable(page, size, sortBy);
        Page<LearningSubscriptionPlan> plans = planRepository.searchPlans(
                tierLevel, minFee, maxFee, hasTrial, maxUsers, pageable);
        return plans.map(this::convertToDto);
    }

    /**
     * キーワード検索
     */
    public Page<LearningSubscriptionPlanDto> searchByKeyword(String keyword, int page, int size, String sortBy) {
        Pageable pageable = createPageable(page, size, sortBy);
        Page<LearningSubscriptionPlan> plans = planRepository.searchByKeyword(keyword, pageable);
        return plans.map(this::convertToDto);
    }

    /**
     * ユーザー数要件に基づくプラン検索
     */
    public List<LearningSubscriptionPlanDto> getPlansForUserCount(Integer requiredUsers) {
        List<LearningSubscriptionPlan> plans = planRepository.findPlansForUserCount(requiredUsers);
        return plans.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * ストレージ要件に基づくプラン検索
     */
    public List<LearningSubscriptionPlanDto> getPlansForStorageRequirement(Integer requiredStorage) {
        List<LearningSubscriptionPlan> plans = planRepository.findPlansForStorageRequirement(requiredStorage);
        return plans.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ========== ソート・表示順序操作 ==========

    /**
     * 表示順序でのプラン取得
     */
    public List<LearningSubscriptionPlanDto> getPlansOrderedBySortOrder() {
        List<LearningSubscriptionPlan> plans = planRepository.findAllOrderBySortOrder();
        return plans.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 価格順でのプラン取得
     */
    public List<LearningSubscriptionPlanDto> getPlansOrderedByPrice() {
        List<LearningSubscriptionPlan> plans = planRepository.findAllOrderByPrice();
        return plans.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 人気順でのプラン取得
     */
    public List<LearningSubscriptionPlanDto> getPlansOrderedByPopularity() {
        List<LearningSubscriptionPlan> plans = planRepository.findAllOrderByPopularity();
        return plans.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 最新順でのプラン取得
     */
    public List<LearningSubscriptionPlanDto> getPlansOrderedByNewest() {
        List<LearningSubscriptionPlan> plans = planRepository.findAllOrderByNewest();
        return plans.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ========== プラン管理操作 ==========

    /**
     * プラン表示順序更新
     */
    @Transactional
    public void updateSortOrder(Long planId, Integer sortOrder) {
        try {
            planRepository.updateSortOrder(planId, sortOrder);
            log.info("プランの表示順序が更新されました: ID={}, SortOrder={}", planId, sortOrder);
        } catch (Exception e) {
            log.error("表示順序更新中にエラーが発生しました: ID={}, Error={}", planId, e.getMessage(), e);
            throw new RuntimeException("表示順序の更新に失敗しました", e);
        }
    }

    /**
     * プランアクティブ状態更新
     */
    @Transactional
    public void updateActiveStatus(Long planId, Boolean isActive) {
        try {
            planRepository.updateActiveStatus(planId, isActive);
            log.info("プランのアクティブ状態が更新されました: ID={}, Active={}", planId, isActive);
        } catch (Exception e) {
            log.error("アクティブ状態更新中にエラーが発生しました: ID={}, Error={}", planId, e.getMessage(), e);
            throw new RuntimeException("アクティブ状態の更新に失敗しました", e);
        }
    }

    /**
     * プラン推奨状態更新
     */
    @Transactional
    public void updateRecommendedStatus(Long planId, Boolean isRecommended) {
        try {
            planRepository.updateRecommendedStatus(planId, isRecommended);
            log.info("プランの推奨状態が更新されました: ID={}, Recommended={}", planId, isRecommended);
        } catch (Exception e) {
            log.error("推奨状態更新中にエラーが発生しました: ID={}, Error={}", planId, e.getMessage(), e);
            throw new RuntimeException("推奨状態の更新に失敗しました", e);
        }
    }

    /**
     * プラン人気状態更新
     */
    @Transactional
    public void updatePopularStatus(Long planId, Boolean isPopular) {
        try {
            planRepository.updatePopularStatus(planId, isPopular);
            log.info("プランの人気状態が更新されました: ID={}, Popular={}", planId, isPopular);
        } catch (Exception e) {
            log.error("人気状態更新中にエラーが発生しました: ID={}, Error={}", planId, e.getMessage(), e);
            throw new RuntimeException("人気状態の更新に失敗しました", e);
        }
    }

    /**
     * 月額料金更新
     */
    @Transactional
    public void updateMonthlyFee(Long planId, BigDecimal monthlyFee) {
        try {
            planRepository.updateMonthlyFee(planId, monthlyFee);

            // 年額割引率の再計算
            LearningSubscriptionPlan plan = planRepository.findById(planId).orElse(null);
            if (plan != null && plan.getYearlyFee() != null) {
                plan.setMonthlyFee(monthlyFee);
                BigDecimal newDiscount = plan.calculateYearlyDiscountPercent();
                planRepository.updateYearlyFee(planId, plan.getYearlyFee(), newDiscount);
            }

            log.info("プランの月額料金が更新されました: ID={}, MonthlyFee={}", planId, monthlyFee);
        } catch (Exception e) {
            log.error("月額料金更新中にエラーが発生しました: ID={}, Error={}", planId, e.getMessage(), e);
            throw new RuntimeException("月額料金の更新に失敗しました", e);
        }
    }

    /**
     * 年額料金・割引率更新
     */
    @Transactional
    public void updateYearlyPricing(Long planId, BigDecimal yearlyFee) {
        try {
            LearningSubscriptionPlan plan = planRepository.findById(planId)
                    .orElseThrow(() -> new RuntimeException("プランが見つかりません: " + planId));

            plan.setYearlyFee(yearlyFee);
            BigDecimal discountPercent = plan.calculateYearlyDiscountPercent();

            planRepository.updateYearlyFee(planId, yearlyFee, discountPercent);
            log.info("プランの年額料金が更新されました: ID={}, YearlyFee={}, Discount={}%",
                    planId, yearlyFee, discountPercent);
        } catch (Exception e) {
            log.error("年額料金更新中にエラーが発生しました: ID={}, Error={}", planId, e.getMessage(), e);
            throw new RuntimeException("年額料金の更新に失敗しました", e);
        }
    }

    /**
     * 試用期間設定更新
     */
    @Transactional
    public void updateTrialSettings(Long planId, Boolean hasTrial, Integer trialDays) {
        try {
            planRepository.updateTrialSettings(planId, hasTrial, trialDays);
            log.info("プランの試用期間設定が更新されました: ID={}, HasTrial={}, TrialDays={}",
                    planId, hasTrial, trialDays);
        } catch (Exception e) {
            log.error("試用期間設定更新中にエラーが発生しました: ID={}, Error={}", planId, e.getMessage(), e);
            throw new RuntimeException("試用期間設定の更新に失敗しました", e);
        }
    }

    /**
     * 容量制限更新
     */
    @Transactional
    public void updateCapacityLimits(Long planId, Integer maxUsers, Integer maxStorageGb) {
        try {
            planRepository.updateCapacityLimits(planId, maxUsers, maxStorageGb);
            log.info("プランの容量制限が更新されました: ID={}, MaxUsers={}, MaxStorage={}GB",
                    planId, maxUsers, maxStorageGb);
        } catch (Exception e) {
            log.error("容量制限更新中にエラーが発生しました: ID={}, Error={}", planId, e.getMessage(), e);
            throw new RuntimeException("容量制限の更新に失敗しました", e);
        }
    }

    /**
     * 機能一覧更新
     */
    @Transactional
    public void updateFeatures(Long planId, String features) {
        try {
            planRepository.updateFeatures(planId, features);
            log.info("プランの機能一覧が更新されました: ID={}", planId);
        } catch (Exception e) {
            log.error("機能一覧更新中にエラーが発生しました: ID={}, Error={}", planId, e.getMessage(), e);
            throw new RuntimeException("機能一覧の更新に失敗しました", e);
        }
    }

    // ========== 統計・分析操作 ==========

    /**
     * プラン統計情報取得
     */
    public PlanStatistics getPlanStatistics() {
        Object[] stats = planRepository.getDetailedStatistics();

        return PlanStatistics.builder()
                .totalPlans(((Number) stats[0]).intValue())
                .activePlans(((Number) stats[1]).intValue())
                .freePlans(((Number) stats[2]).intValue())
                .trialPlans(((Number) stats[3]).intValue())
                .recommendedPlans(((Number) stats[4]).intValue())
                .popularPlans(((Number) stats[5]).intValue())
                .averageMonthlyFee((BigDecimal) stats[6])
                .minMonthlyFee((BigDecimal) stats[7])
                .maxMonthlyFee((BigDecimal) stats[8])
                .build();
    }

    /**
     * 層級別統計取得
     */
    public Map<TierLevel, Long> getTierLevelStatistics() {
        List<Object[]> stats = planRepository.countPlansByTierLevel();
        Map<TierLevel, Long> result = new HashMap<>();

        for (Object[] stat : stats) {
            TierLevel tierLevel = (TierLevel) stat[0];
            Long count = ((Number) stat[1]).longValue();
            result.put(tierLevel, count);
        }

        return result;
    }

    /**
     * 価格帯別統計取得
     */
    public Map<String, Long> getPriceRangeStatistics() {
        List<Object[]> stats = planRepository.getPriceRangeStatistics();
        Map<String, Long> result = new HashMap<>();

        for (Object[] stat : stats) {
            String priceRange = (String) stat[0];
            Long count = ((Number) stat[1]).longValue();
            result.put(priceRange, count);
        }

        return result;
    }

    /**
     * 平均月額料金取得
     */
    public BigDecimal getAverageMonthlyFee() {
        BigDecimal average = planRepository.calculateAverageMonthlyFee();
        return average != null ? average : BigDecimal.ZERO;
    }

    /**
     * 最小・最大価格取得
     */
    public PriceRange getPriceRange() {
        Object[] prices = planRepository.getMinMaxPrices();

        BigDecimal minPrice = prices[0] != null ? (BigDecimal) prices[0] : BigDecimal.ZERO;
        BigDecimal maxPrice = prices[1] != null ? (BigDecimal) prices[1] : BigDecimal.ZERO;

        return new PriceRange(minPrice, maxPrice);
    }

    /**
     * 試用期間統計取得
     */
    public TrialStatistics getTrialStatistics() {
        Object[] stats = planRepository.getTrialStatistics();

        Long trialPlanCount = stats[0] != null ? ((Number) stats[0]).longValue() : 0L;
        BigDecimal averageTrialDays = stats[1] != null ? (BigDecimal) stats[1] : BigDecimal.ZERO;

        return new TrialStatistics(trialPlanCount, averageTrialDays);
    }

    /**
     * 月別作成統計取得
     */
    public Map<String, Long> getMonthlyCreationStatistics(LocalDateTime fromDate) {
        List<Object[]> stats = planRepository.getMonthlyCreationStatistics(fromDate);
        Map<String, Long> result = new HashMap<>();

        for (Object[] stat : stats) {
            String month = (String) stat[0];
            Long count = ((Number) stat[1]).longValue();
            result.put(month, count);
        }

        return result;
    }

    // ========== バッチ処理操作 ==========

    /**
     * 全推奨状態リセット
     */
    @Transactional
    public void clearAllRecommendedStatus() {
        try {
            planRepository.clearAllRecommendedStatus();
            log.info("全プランの推奨状態がリセットされました");
        } catch (Exception e) {
            log.error("推奨状態リセット中にエラーが発生しました: {}", e.getMessage(), e);
            throw new RuntimeException("推奨状態のリセットに失敗しました", e);
        }
    }

    /**
     * 全人気状態リセット
     */
    @Transactional
    public void clearAllPopularStatus() {
        try {
            planRepository.clearAllPopularStatus();
            log.info("全プランの人気状態がリセットされました");
        } catch (Exception e) {
            log.error("人気状態リセット中にエラーが発生しました: {}", e.getMessage(), e);
            throw new RuntimeException("人気状態のリセットに失敗しました", e);
        }
    }

    /**
     * 層級内表示順序再整理
     */
    @Transactional
    public void reorderPlansInTierLevel(TierLevel tierLevel) {
        try {
            planRepository.reorderPlansInTierLevel(tierLevel.name());
            log.info("層級内の表示順序が再整理されました: TierLevel={}", tierLevel);
        } catch (Exception e) {
            log.error("表示順序再整理中にエラーが発生しました: TierLevel={}, Error={}", tierLevel, e.getMessage(), e);
            throw new RuntimeException("表示順序の再整理に失敗しました", e);
        }
    }

    /**
     * 複数プラン一括アクティブ化
     */
    @Transactional
    public void activateMultiplePlans(List<Long> planIds) {
        try {
            planRepository.activateMultiplePlans(planIds);
            log.info("複数プランがアクティブ化されました: Count={}", planIds.size());
        } catch (Exception e) {
            log.error("一括アクティブ化中にエラーが発生しました: {}", e.getMessage(), e);
            throw new RuntimeException("一括アクティブ化に失敗しました", e);
        }
    }

    /**
     * 複数プラン一括非アクティブ化
     */
    @Transactional
    public void deactivateMultiplePlans(List<Long> planIds) {
        try {
            planRepository.deactivateMultiplePlans(planIds);
            log.info("複数プランが非アクティブ化されました: Count={}", planIds.size());
        } catch (Exception e) {
            log.error("一括非アクティブ化中にエラーが発生しました: {}", e.getMessage(), e);
            throw new RuntimeException("一括非アクティブ化に失敗しました", e);
        }
    }

    // ========== 管理者専用操作 ==========

    /**
     * 非アクティブプラン取得（管理者用）
     */
    public List<LearningSubscriptionPlanDto> getInactivePlans() {
        List<LearningSubscriptionPlan> plans = planRepository.findInactivePlans();
        return plans.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 削除済みプラン取得（管理者用）
     */
    public List<LearningSubscriptionPlanDto> getDeletedPlans() {
        List<LearningSubscriptionPlan> plans = planRepository.findDeletedPlans();
        return plans.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 更新が古いプラン取得
     */
    public List<LearningSubscriptionPlanDto> getOutdatedPlans(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        List<LearningSubscriptionPlan> plans = planRepository.findPlansNotUpdatedSince(cutoffDate);
        return plans.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ========== プラン比較・推奨機能 ==========

    /**
     * プラン比較データ生成
     */
    public List<LearningSubscriptionPlanDto> getComparisonPlans(List<Long> planIds) {
        List<LearningSubscriptionPlanDto> plans = planIds.stream()
                .map(this::getPlan)
                .collect(Collectors.toList());

        // 比較用フィールドの初期化
        plans.forEach(LearningSubscriptionPlanDto::initializeUiFields);

        return plans;
    }

    /**
     * ユーザー要件に基づくプラン推奨
     */
    public List<LearningSubscriptionPlanDto> getRecommendedPlansForRequirements(
            Integer requiredUsers, Integer requiredStorage, BigDecimal budget) {

        List<LearningSubscriptionPlan> plans = planRepository.findActivePlans();

        return plans.stream()
                .filter(plan -> {
                    // ユーザー数チェック
                    if (requiredUsers != null && plan.getMaxUsers() != null &&
                            plan.getMaxUsers() < requiredUsers) {
                        return false;
                    }

                    // ストレージチェック
                    if (requiredStorage != null && plan.getMaxStorageGb() != null &&
                            plan.getMaxStorageGb() < requiredStorage) {
                        return false;
                    }

                    // 予算チェック
                    if (budget != null && plan.getMonthlyFee().compareTo(budget) > 0) {
                        return false;
                    }

                    return true;
                })
                .map(this::convertToDto)
                .peek(LearningSubscriptionPlanDto::initializeUiFields)
                .sorted((p1, p2) -> p2.getComparisonScore().compareTo(p1.getComparisonScore()))
                .collect(Collectors.toList());
    }

    // ========== ヘルパーメソッド ==========

    /**
     * DTOからEntityへの変換
     */
    private LearningSubscriptionPlan convertToEntity(LearningSubscriptionPlanDto dto) {
        LearningSubscriptionPlan plan = new LearningSubscriptionPlan();

        plan.setName(dto.getName());
        plan.setDescription(dto.getDescription());
        plan.setMonthlyFee(dto.getMonthlyFee());
        plan.setFeatures(dto.getFeatures());
        plan.setTierLevel(dto.getTierLevel());
        plan.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        plan.setIsRecommended(dto.getIsRecommended() != null ? dto.getIsRecommended() : false);
        plan.setIsPopular(dto.getIsPopular() != null ? dto.getIsPopular() : false);
        plan.setHasTrial(dto.getHasTrial() != null ? dto.getHasTrial() : false);
        plan.setTrialDays(dto.getTrialDays());
        plan.setMaxUsers(dto.getMaxUsers());
        plan.setMaxStorageGb(dto.getMaxStorageGb());
        plan.setYearlyFee(dto.getYearlyFee());
        plan.setYearlyDiscountPercent(dto.getYearlyDiscountPercent());
        plan.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        plan.setPlanColor(dto.getPlanColor());
        plan.setPlanIcon(dto.getPlanIcon());
        plan.setLimitations(dto.getLimitations());
        plan.setSpecialFeatures(dto.getSpecialFeatures());

        return plan;
    }

    /**
     * EntityからDTOへの変換
     */
    private LearningSubscriptionPlanDto convertToDto(LearningSubscriptionPlan plan) {
        LearningSubscriptionPlanDto dto = new LearningSubscriptionPlanDto();

        dto.setPlanId(plan.getId().toString());
        dto.setName(plan.getName());
        dto.setDescription(plan.getDescription());
        dto.setMonthlyFee(plan.getMonthlyFee());
        dto.setFeatures(plan.getFeatures());
        dto.setTierLevel(plan.getTierLevel());
        dto.setIsActive(plan.getIsActive());
        dto.setIsRecommended(plan.getIsRecommended());
        dto.setIsPopular(plan.getIsPopular());
        dto.setHasTrial(plan.getHasTrial());
        dto.setTrialDays(plan.getTrialDays());
        dto.setMaxUsers(plan.getMaxUsers());
        dto.setMaxStorageGb(plan.getMaxStorageGb());
        dto.setYearlyFee(plan.getYearlyFee());
        dto.setYearlyDiscountPercent(plan.getYearlyDiscountPercent());
        dto.setSortOrder(plan.getSortOrder());
        dto.setPlanColor(plan.getPlanColor());
        dto.setPlanIcon(plan.getPlanIcon());
        dto.setLimitations(plan.getLimitations());
        dto.setSpecialFeatures(plan.getSpecialFeatures());
        dto.setCreatedAt(plan.getCreatedAt());
        dto.setUpdatedAt(plan.getUpdatedAt());

        // 計算済みフィールドの設定
        dto.setYearlySavings(plan.calculateYearlySavings());
        dto.setCalculatedYearlyDiscountPercent(plan.calculateYearlyDiscountPercent());
        dto.setDailyFee(plan.calculateDailyFee());
        dto.setValueScore(plan.calculateValueScore());
        dto.setComparisonScore(plan.getComparisonScore());
        dto.setFeatureCount(plan.getFeatureCount());
        dto.setLimitationCount(plan.getLimitationCount());
        dto.setDetailedDescription(plan.generateDetailedDescription());

        // DTO初期化
        dto.initialize();

        return dto;
    }

    /**
     * プランフィールド更新
     */
    private void updatePlanFields(LearningSubscriptionPlan plan, LearningSubscriptionPlanDto dto) {
        if (dto.getName() != null) {
            plan.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            plan.setDescription(dto.getDescription());
        }
        if (dto.getMonthlyFee() != null) {
            plan.setMonthlyFee(dto.getMonthlyFee());
        }
        if (dto.getFeatures() != null) {
            plan.setFeatures(dto.getFeatures());
        }
        if (dto.getTierLevel() != null) {
            plan.setTierLevel(dto.getTierLevel());
        }
        if (dto.getIsActive() != null) {
            plan.setIsActive(dto.getIsActive());
        }
        if (dto.getIsRecommended() != null) {
            plan.setIsRecommended(dto.getIsRecommended());
        }
        if (dto.getIsPopular() != null) {
            plan.setIsPopular(dto.getIsPopular());
        }
        if (dto.getHasTrial() != null) {
            plan.setHasTrial(dto.getHasTrial());
        }
        if (dto.getTrialDays() != null) {
            plan.setTrialDays(dto.getTrialDays());
        }
        if (dto.getMaxUsers() != null) {
            plan.setMaxUsers(dto.getMaxUsers());
        }
        if (dto.getMaxStorageGb() != null) {
            plan.setMaxStorageGb(dto.getMaxStorageGb());
        }
        if (dto.getYearlyFee() != null) {
            plan.setYearlyFee(dto.getYearlyFee());
        }
        if (dto.getSortOrder() != null) {
            plan.setSortOrder(dto.getSortOrder());
        }
        if (dto.getPlanColor() != null) {
            plan.setPlanColor(dto.getPlanColor());
        }
        if (dto.getPlanIcon() != null) {
            plan.setPlanIcon(dto.getPlanIcon());
        }
        if (dto.getLimitations() != null) {
            plan.setLimitations(dto.getLimitations());
        }
        if (dto.getSpecialFeatures() != null) {
            plan.setSpecialFeatures(dto.getSpecialFeatures());
        }
    }

    /**
     * Pageable作成
     */
    private Pageable createPageable(int page, int size, String sortBy) {
        Sort sort = Sort.by(Sort.Direction.ASC, sortBy != null ? sortBy : "sortOrder");
        return PageRequest.of(page, size, sort);
    }

    // ========== 内部クラス（統計用） ==========

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PlanStatistics {
        private Integer totalPlans;
        private Integer activePlans;
        private Integer freePlans;
        private Integer trialPlans;
        private Integer recommendedPlans;
        private Integer popularPlans;
        private BigDecimal averageMonthlyFee;
        private BigDecimal minMonthlyFee;
        private BigDecimal maxMonthlyFee;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class PriceRange {
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class TrialStatistics {
        private Long trialPlanCount;
        private BigDecimal averageTrialDays;
    }
}
