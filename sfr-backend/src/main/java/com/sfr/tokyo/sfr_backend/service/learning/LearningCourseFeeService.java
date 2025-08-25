package com.sfr.tokyo.sfr_backend.service.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningCourseFee;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningCourseFee.FeeType;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningCourseFee.CurrencyType;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningCourseFee.SfrRewardCondition;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningCourseFee.PaymentMethod;
import com.sfr.tokyo.sfr_backend.dto.learning.LearningCourseFeeDto;
import com.sfr.tokyo.sfr_backend.repository.learning.LearningCourseFeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * LearningCourseFee Service
 * コース料金ビジネスロジック層
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LearningCourseFeeService {

    private final LearningCourseFeeRepository courseFeeRepository;

    // ========== CRUD操作 ==========

    /**
     * 新規料金設定作成
     */
    @Transactional
    public LearningCourseFeeDto createCourseFee(LearningCourseFeeDto dto) {
        log.info("コース料金設定作成開始: courseId={}", dto.getCourseId());

        LearningCourseFee entity = convertToEntity(dto);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setIsActive(true);

        // 同一コースの既存料金設定を無効化
        if (dto.getCourseId() != null) {
            deactivateExistingCourseFees(dto.getCourseId());
        }

        LearningCourseFee saved = courseFeeRepository.save(entity);
        log.info("コース料金設定作成完了: id={}, courseId={}", saved.getId(), saved.getCourseId());

        return convertToDto(saved);
    }

    /**
     * 料金設定更新
     */
    @Transactional
    public LearningCourseFeeDto updateCourseFee(Long feeId, LearningCourseFeeDto dto) {
        log.info("コース料金設定更新開始: feeId={}", feeId);

        LearningCourseFee entity = courseFeeRepository.findById(feeId)
                .orElseThrow(() -> new EntityNotFoundException("料金設定が見つかりません: " + feeId));

        updateEntityFromDto(entity, dto);
        entity.setUpdatedAt(LocalDateTime.now());

        LearningCourseFee saved = courseFeeRepository.save(entity);
        log.info("コース料金設定更新完了: id={}", saved.getId());

        return convertToDto(saved);
    }

    /**
     * 料金設定削除
     */
    @Transactional
    public void deleteCourseFee(Long feeId) {
        log.info("コース料金設定削除開始: feeId={}", feeId);

        int updated = courseFeeRepository.softDelete(feeId, LocalDateTime.now());
        if (updated == 0) {
            throw new EntityNotFoundException("料金設定が見つかりません: " + feeId);
        }

        log.info("コース料金設定削除完了: feeId={}", feeId);
    }

    /**
     * 料金設定復旧
     */
    @Transactional
    public void restoreCourseFee(Long feeId) {
        log.info("コース料金設定復旧開始: feeId={}", feeId);

        int updated = courseFeeRepository.restore(feeId, LocalDateTime.now());
        if (updated == 0) {
            throw new EntityNotFoundException("料金設定が見つかりません: " + feeId);
        }

        log.info("コース料金設定復旧完了: feeId={}", feeId);
    }

    /**
     * ID別料金設定取得
     */
    public LearningCourseFeeDto getCourseFeeById(Long feeId) {
        LearningCourseFee entity = courseFeeRepository.findById(feeId)
                .orElseThrow(() -> new EntityNotFoundException("料金設定が見つかりません: " + feeId));

        return convertToDto(entity);
    }

    /**
     * コース毎の現在料金設定取得
     */
    public Optional<LearningCourseFeeDto> getCurrentFeeByCoursId(Long courseId) {
        return courseFeeRepository.findCurrentFeeByCoursId(courseId)
                .map(this::convertToDto);
    }

    // ========== 検索機能 ==========

    /**
     * アクティブな料金設定一覧取得
     */
    public List<LearningCourseFeeDto> getActiveFees() {
        return courseFeeRepository.findActiveFees().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * コース別料金設定一覧取得
     */
    public List<LearningCourseFeeDto> getFeesByCourseId(Long courseId) {
        return courseFeeRepository.findByCourseId(courseId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 料金タイプ別検索
     */
    public List<LearningCourseFeeDto> getFeesByType(FeeType feeType) {
        return courseFeeRepository.findByFeeType(feeType).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 通貨タイプ別検索
     */
    public List<LearningCourseFeeDto> getFeesByCurrency(CurrencyType currencyType) {
        return courseFeeRepository.findByCurrencyType(currencyType).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 価格範囲検索
     */
    public List<LearningCourseFeeDto> getFeesByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return courseFeeRepository.findByPriceRange(minPrice, maxPrice).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 予算内料金検索
     */
    public List<LearningCourseFeeDto> getFeesWithinBudget(BigDecimal budget) {
        return courseFeeRepository.findWithinBudget(budget).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 無料コース検索
     */
    public List<LearningCourseFeeDto> getFreeCourses() {
        return courseFeeRepository.findFreeCourseFees().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 有料コース検索
     */
    public List<LearningCourseFeeDto> getPaidCourses() {
        return courseFeeRepository.findPaidCourseFees().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ========== SFR報酬関連 ==========

    /**
     * SFR報酬付きコース検索
     */
    public List<LearningCourseFeeDto> getCoursesWithSfrReward() {
        return courseFeeRepository.findCoursesWithSfrReward().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * SFR報酬条件別検索
     */
    public List<LearningCourseFeeDto> getCoursesBySfrRewardCondition(SfrRewardCondition condition) {
        return courseFeeRepository.findBySfrRewardCondition(condition).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * SFR報酬額範囲検索
     */
    public List<LearningCourseFeeDto> getCoursesBySfrRewardRange(BigDecimal minReward, BigDecimal maxReward) {
        return courseFeeRepository.findBySfrRewardRange(minReward, maxReward).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 高SFR報酬コース取得
     */
    public List<LearningCourseFeeDto> getTopSfrRewardCourses(int limit) {
        return courseFeeRepository.findTopSfrRewardCourses(Pageable.ofSize(limit)).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * SFR報酬設定更新
     */
    @Transactional
    public void updateSfrReward(Long feeId, BigDecimal rewardAmount, SfrRewardCondition condition) {
        log.info("SFR報酬設定更新開始: feeId={}, rewardAmount={}, condition={}", feeId, rewardAmount, condition);

        int updated = courseFeeRepository.updateSfrReward(feeId, rewardAmount, condition, LocalDateTime.now());
        if (updated == 0) {
            throw new EntityNotFoundException("料金設定が見つかりません: " + feeId);
        }

        log.info("SFR報酬設定更新完了: feeId={}", feeId);
    }

    // ========== 割引関連 ==========

    /**
     * 割引中コース検索
     */
    public List<LearningCourseFeeDto> getDiscountedCourses() {
        LocalDateTime now = LocalDateTime.now();
        return courseFeeRepository.findDiscountedCourses(now).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 早期割引コース検索
     */
    public List<LearningCourseFeeDto> getEarlyBirdDiscountCourses() {
        LocalDateTime now = LocalDateTime.now();
        return courseFeeRepository.findEarlyBirdDiscountCourses(now).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 通常割引コース検索
     */
    public List<LearningCourseFeeDto> getRegularDiscountCourses() {
        LocalDateTime now = LocalDateTime.now();
        return courseFeeRepository.findRegularDiscountCourses(now).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 特別価格コース検索
     */
    public List<LearningCourseFeeDto> getSpecialPriceCourses() {
        return courseFeeRepository.findSpecialPriceCourses().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 割引設定更新
     */
    @Transactional
    public void updateDiscount(Long feeId, BigDecimal discountPercent, LocalDateTime startAt, LocalDateTime endAt) {
        log.info("割引設定更新開始: feeId={}, discountPercent={}", feeId, discountPercent);

        int updated = courseFeeRepository.updateDiscount(feeId, discountPercent, startAt, endAt, LocalDateTime.now());
        if (updated == 0) {
            throw new EntityNotFoundException("料金設定が見つかりません: " + feeId);
        }

        log.info("割引設定更新完了: feeId={}", feeId);
    }

    /**
     * 早期割引設定更新
     */
    @Transactional
    public void updateEarlyBirdDiscount(Long feeId, Boolean earlyBirdDiscount, BigDecimal earlyBirdPercent,
            LocalDateTime deadline) {
        log.info("早期割引設定更新開始: feeId={}, earlyBirdPercent={}", feeId, earlyBirdPercent);

        int updated = courseFeeRepository.updateEarlyBirdDiscount(feeId, earlyBirdDiscount, earlyBirdPercent, deadline,
                LocalDateTime.now());
        if (updated == 0) {
            throw new EntityNotFoundException("料金設定が見つかりません: " + feeId);
        }

        log.info("早期割引設定更新完了: feeId={}", feeId);
    }

    // ========== 支払い関連 ==========

    /**
     * 支払い方法別検索
     */
    public List<LearningCourseFeeDto> getFeesByPaymentMethod(PaymentMethod paymentMethod) {
        return courseFeeRepository.findByPaymentMethod(paymentMethod).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 分割払い可能コース検索
     */
    public List<LearningCourseFeeDto> getInstallmentAvailableCourses() {
        return courseFeeRepository.findInstallmentAvailableCourses().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 返金可能コース検索
     */
    public List<LearningCourseFeeDto> getRefundableCourses() {
        return courseFeeRepository.findRefundableCourses().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 支払い設定更新
     */
    @Transactional
    public void updatePaymentSettings(Long feeId, PaymentMethod paymentMethod, Boolean installmentAvailable,
            Integer installmentCount) {
        log.info("支払い設定更新開始: feeId={}, paymentMethod={}", feeId, paymentMethod);

        int updated = courseFeeRepository.updatePaymentSettings(feeId, paymentMethod, installmentAvailable,
                installmentCount, LocalDateTime.now());
        if (updated == 0) {
            throw new EntityNotFoundException("料金設定が見つかりません: " + feeId);
        }

        log.info("支払い設定更新完了: feeId={}", feeId);
    }

    // ========== 複合検索 ==========

    /**
     * 複合条件検索
     */
    public Page<LearningCourseFeeDto> searchCourses(
            FeeType feeType,
            CurrencyType currencyType,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean isFree,
            Boolean hasSfrReward,
            PaymentMethod paymentMethod,
            Boolean installmentAvailable,
            Boolean refundable,
            Pageable pageable) {

        return courseFeeRepository.searchCourses(
                feeType, currencyType, minPrice, maxPrice, isFree,
                hasSfrReward, paymentMethod, installmentAvailable, refundable, pageable)
                .map(this::convertToDto);
    }

    // ========== 統計・分析 ==========

    /**
     * 料金タイプ別統計取得
     */
    public Map<String, Object> getFeeTypeStatistics() {
        List<Object[]> results = courseFeeRepository.getFeeTypeStatistics();
        Map<String, Object> statistics = new HashMap<>();

        for (Object[] result : results) {
            FeeType feeType = (FeeType) result[0];
            Long count = (Long) result[1];
            BigDecimal avgPrice = (BigDecimal) result[2];
            BigDecimal minPrice = (BigDecimal) result[3];
            BigDecimal maxPrice = (BigDecimal) result[4];

            Map<String, Object> typeStats = new HashMap<>();
            typeStats.put("count", count);
            typeStats.put("averagePrice", avgPrice);
            typeStats.put("minPrice", minPrice);
            typeStats.put("maxPrice", maxPrice);

            statistics.put(feeType.name(), typeStats);
        }

        return statistics;
    }

    /**
     * 通貨タイプ別統計取得
     */
    public Map<String, Object> getCurrencyTypeStatistics() {
        List<Object[]> results = courseFeeRepository.getCurrencyTypeStatistics();
        Map<String, Object> statistics = new HashMap<>();

        for (Object[] result : results) {
            CurrencyType currencyType = (CurrencyType) result[0];
            Long count = (Long) result[1];
            BigDecimal avgPrice = (BigDecimal) result[2];
            BigDecimal totalPrice = (BigDecimal) result[3];

            Map<String, Object> currencyStats = new HashMap<>();
            currencyStats.put("count", count);
            currencyStats.put("averagePrice", avgPrice);
            currencyStats.put("totalPrice", totalPrice);

            statistics.put(currencyType.name(), currencyStats);
        }

        return statistics;
    }

    /**
     * SFR報酬統計取得
     */
    public Map<String, Object> getSfrRewardStatistics() {
        Object[] result = courseFeeRepository.getSfrRewardStatistics();

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalCourses", result[0]);
        statistics.put("averageReward", result[1]);
        statistics.put("totalRewards", result[2]);
        statistics.put("minReward", result[3]);
        statistics.put("maxReward", result[4]);

        return statistics;
    }

    /**
     * 価格統計取得
     */
    public Map<String, Object> getPriceStatistics() {
        Object[] result = courseFeeRepository.getPriceStatistics();

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalCourses", result[0]);
        statistics.put("averagePrice", result[1]);
        statistics.put("totalRevenue", result[2]);
        statistics.put("minPrice", result[3]);
        statistics.put("maxPrice", result[4]);

        return statistics;
    }

    /**
     * 割引統計取得
     */
    public Map<String, Object> getDiscountStatistics() {
        Object[] result = courseFeeRepository.getDiscountStatistics();

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalDiscountedCourses", result[0]);
        statistics.put("averageDiscountPercent", result[1]);
        statistics.put("earlyBirdDiscountCourses", result[2]);
        statistics.put("averageEarlyBirdPercent", result[3]);

        return statistics;
    }

    /**
     * 月別作成統計取得
     */
    public List<Map<String, Object>> getMonthlyCreationStatistics(int months) {
        LocalDateTime fromDate = LocalDateTime.now().minusMonths(months);
        List<Object[]> results = courseFeeRepository.getMonthlyCreationStatistics(fromDate);

        return results.stream()
                .map(result -> {
                    Map<String, Object> monthStats = new HashMap<>();
                    monthStats.put("year", result[0]);
                    monthStats.put("month", result[1]);
                    monthStats.put("count", result[2]);
                    monthStats.put("averagePrice", result[3]);
                    return monthStats;
                })
                .collect(Collectors.toList());
    }

    /**
     * 人気価格帯統計取得
     */
    public List<Map<String, Object>> getPopularPriceRanges() {
        List<Object[]> results = courseFeeRepository.getPopularPriceRanges();

        return results.stream()
                .map(result -> {
                    Map<String, Object> rangeStats = new HashMap<>();
                    rangeStats.put("priceRange", result[0]);
                    rangeStats.put("count", result[1]);
                    return rangeStats;
                })
                .collect(Collectors.toList());
    }

    // ========== バッチ処理 ==========

    /**
     * 期限切れ割引無効化
     */
    @Transactional
    public void disableExpiredDiscounts() {
        LocalDateTime now = LocalDateTime.now();

        log.info("期限切れ割引無効化開始");

        int earlyBirdUpdated = courseFeeRepository.disableExpiredEarlyBirdDiscounts(now);
        int regularUpdated = courseFeeRepository.disableExpiredDiscounts(now);

        log.info("期限切れ割引無効化完了: 早期割引={}, 通常割引={}", earlyBirdUpdated, regularUpdated);
    }

    /**
     * 料金額更新
     */
    @Transactional
    public void updatePriceAmount(Long feeId, BigDecimal priceAmount) {
        log.info("料金額更新開始: feeId={}, priceAmount={}", feeId, priceAmount);

        int updated = courseFeeRepository.updatePriceAmount(feeId, priceAmount, LocalDateTime.now());
        if (updated == 0) {
            throw new EntityNotFoundException("料金設定が見つかりません: " + feeId);
        }

        log.info("料金額更新完了: feeId={}", feeId);
    }

    /**
     * アクティブ状態更新
     */
    @Transactional
    public void updateActiveStatus(Long feeId, Boolean isActive) {
        log.info("アクティブ状態更新開始: feeId={}, isActive={}", feeId, isActive);

        int updated = courseFeeRepository.updateActiveStatus(feeId, isActive, LocalDateTime.now());
        if (updated == 0) {
            throw new EntityNotFoundException("料金設定が見つかりません: " + feeId);
        }

        log.info("アクティブ状態更新完了: feeId={}", feeId);
    }

    // ========== 集計計算 ==========

    /**
     * SFR総報酬額計算
     */
    public BigDecimal calculateTotalSfrRewards() {
        return courseFeeRepository.calculateTotalSfrRewards();
    }

    /**
     * 平均価格計算
     */
    public BigDecimal calculateAveragePrice() {
        return courseFeeRepository.calculateAveragePrice();
    }

    /**
     * コース数カウント
     */
    public Map<String, Long> getCourseCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("freeCourses", courseFeeRepository.countFreeCourses());
        counts.put("paidCourses", courseFeeRepository.countPaidCourses());
        counts.put("sfrRewardCourses", courseFeeRepository.countSfrRewardCourses());
        counts.put("discountedCourses", courseFeeRepository.countDiscountedCourses(LocalDateTime.now()));
        return counts;
    }

    // ========== ヘルパーメソッド ==========

    /**
     * 同一コースの既存料金設定を無効化
     */
    @Transactional
    private void deactivateExistingCourseFees(Long courseId) {
        List<LearningCourseFee> existingFees = courseFeeRepository.findByCourseId(courseId);
        for (LearningCourseFee fee : existingFees) {
            if (fee.getIsActive()) {
                fee.setIsActive(false);
                fee.setUpdatedAt(LocalDateTime.now());
                courseFeeRepository.save(fee);
            }
        }
    }

    /**
     * DTOからEntityへの変換
     */
    private LearningCourseFee convertToEntity(LearningCourseFeeDto dto) {
        LearningCourseFee entity = new LearningCourseFee();
        updateEntityFromDto(entity, dto);
        return entity;
    }

    /**
     * DTOからEntityへのデータ移行
     */
    private void updateEntityFromDto(LearningCourseFee entity, LearningCourseFeeDto dto) {
        entity.setCourseId(dto.getCourseId());
        entity.setFeeType(dto.getFeeType());
        entity.setCurrencyType(dto.getCurrencyType());
        entity.setPriceAmount(dto.getPriceAmount());
        entity.setTaxIncluded(dto.getTaxIncluded());
        entity.setTaxRate(dto.getTaxRate());
        entity.setIsFree(dto.getIsFree());
        entity.setSpecialPrice(dto.getSpecialPrice());
        entity.setDiscountPercent(dto.getDiscountPercent());
        entity.setDiscountStartAt(dto.getDiscountStartAt());
        entity.setDiscountEndAt(dto.getDiscountEndAt());
        entity.setEarlyBirdDiscount(dto.getEarlyBirdDiscount());
        entity.setEarlyBirdPercent(dto.getEarlyBirdPercent());
        entity.setEarlyBirdDeadline(dto.getEarlyBirdDeadline());
        entity.setSfrRewardAmount(dto.getSfrRewardAmount());
        entity.setSfrRewardCondition(dto.getSfrRewardCondition());
        entity.setPaymentMethod(dto.getPaymentMethod());
        entity.setInstallmentAvailable(dto.getInstallmentAvailable());
        entity.setInstallmentCount(dto.getInstallmentCount());
        entity.setRefundable(dto.getRefundable());
        entity.setRefundPeriodDays(dto.getRefundPeriodDays());
        entity.setNotes(dto.getNotes());
        entity.setIsActive(dto.getIsActive());
    }

    /**
     * EntityからDTOへの変換
     */
    private LearningCourseFeeDto convertToDto(LearningCourseFee entity) {
        return LearningCourseFeeDto.builder()
                .feeId(entity.getId() != null ? entity.getId().toString() : null)
                .courseId(entity.getCourseId())
                .feeType(entity.getFeeType())
                .currencyType(entity.getCurrencyType())
                .priceAmount(entity.getPriceAmount())
                .taxIncluded(entity.getTaxIncluded())
                .taxRate(entity.getTaxRate())
                .isFree(entity.getIsFree())
                .specialPrice(entity.getSpecialPrice())
                .discountPercent(entity.getDiscountPercent())
                .discountStartAt(entity.getDiscountStartAt())
                .discountEndAt(entity.getDiscountEndAt())
                .earlyBirdDiscount(entity.getEarlyBirdDiscount())
                .earlyBirdPercent(entity.getEarlyBirdPercent())
                .earlyBirdDeadline(entity.getEarlyBirdDeadline())
                .sfrRewardAmount(entity.getSfrRewardAmount())
                .sfrRewardCondition(entity.getSfrRewardCondition())
                .paymentMethod(entity.getPaymentMethod())
                .installmentAvailable(entity.getInstallmentAvailable())
                .installmentCount(entity.getInstallmentCount())
                .refundable(entity.getRefundable())
                .refundPeriodDays(entity.getRefundPeriodDays())
                .notes(entity.getNotes())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
