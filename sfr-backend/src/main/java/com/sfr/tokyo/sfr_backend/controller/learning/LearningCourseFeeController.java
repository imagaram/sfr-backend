package com.sfr.tokyo.sfr_backend.controller.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningCourseFeeDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningCourseFee.FeeType;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningCourseFee.CurrencyType;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningCourseFee.SfrRewardCondition;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningCourseFee.PaymentMethod;
import com.sfr.tokyo.sfr_backend.service.learning.LearningCourseFeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * LearningCourseFee Controller
 * コース料金管理REST API
 */
@RestController
@RequestMapping("/api/learning/course-fees")
@RequiredArgsConstructor
@Slf4j
public class LearningCourseFeeController {

    private final LearningCourseFeeService courseFeeService;

    // ========== 基本CRUD操作 ==========

    /**
     * コース料金設定作成
     */
    @PostMapping
    public ResponseEntity<LearningCourseFeeDto> createCourseFee(
            @Valid @RequestBody LearningCourseFeeDto courseFeeDto) {
        log.info("コース料金設定作成API呼び出し: courseId={}", courseFeeDto.getCourseId());

        LearningCourseFeeDto created = courseFeeService.createCourseFee(courseFeeDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * コース料金設定更新
     */
    @PutMapping("/{feeId}")
    public ResponseEntity<LearningCourseFeeDto> updateCourseFee(
            @PathVariable Long feeId,
            @Valid @RequestBody LearningCourseFeeDto courseFeeDto) {
        log.info("コース料金設定更新API呼び出し: feeId={}", feeId);

        LearningCourseFeeDto updated = courseFeeService.updateCourseFee(feeId, courseFeeDto);
        return ResponseEntity.ok(updated);
    }

    /**
     * コース料金設定削除
     */
    @DeleteMapping("/{feeId}")
    public ResponseEntity<Void> deleteCourseFee(@PathVariable Long feeId) {
        log.info("コース料金設定削除API呼び出し: feeId={}", feeId);

        courseFeeService.deleteCourseFee(feeId);
        return ResponseEntity.noContent().build();
    }

    /**
     * コース料金設定取得
     */
    @GetMapping("/{feeId}")
    public ResponseEntity<LearningCourseFeeDto> getCourseFee(@PathVariable Long feeId) {
        LearningCourseFeeDto courseFee = courseFeeService.getCourseFeeById(feeId);
        return ResponseEntity.ok(courseFee);
    }

    /**
     * コース別現在料金設定取得
     */
    @GetMapping("/course/{courseId}/current")
    public ResponseEntity<LearningCourseFeeDto> getCurrentFeeByCoursId(@PathVariable Long courseId) {
        Optional<LearningCourseFeeDto> courseFee = courseFeeService.getCurrentFeeByCoursId(courseId);
        return courseFee.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ========== 検索機能 ==========

    /**
     * アクティブ料金設定一覧取得
     */
    @GetMapping("/active")
    public ResponseEntity<List<LearningCourseFeeDto>> getActiveFees() {
        List<LearningCourseFeeDto> activeFees = courseFeeService.getActiveFees();
        return ResponseEntity.ok(activeFees);
    }

    /**
     * 無料コース検索
     */
    @GetMapping("/free")
    public ResponseEntity<List<LearningCourseFeeDto>> getFreeCourses() {
        List<LearningCourseFeeDto> freeCourses = courseFeeService.getFreeCourses();
        return ResponseEntity.ok(freeCourses);
    }

    /**
     * 有料コース検索
     */
    @GetMapping("/paid")
    public ResponseEntity<List<LearningCourseFeeDto>> getPaidCourses() {
        List<LearningCourseFeeDto> paidCourses = courseFeeService.getPaidCourses();
        return ResponseEntity.ok(paidCourses);
    }

    // ========== SFR報酬関連API ==========

    /**
     * SFR報酬付きコース検索
     */
    @GetMapping("/sfr-reward")
    public ResponseEntity<List<LearningCourseFeeDto>> getCoursesWithSfrReward() {
        List<LearningCourseFeeDto> sfrRewardCourses = courseFeeService.getCoursesWithSfrReward();
        return ResponseEntity.ok(sfrRewardCourses);
    }

    /**
     * 高SFR報酬コース取得
     */
    @GetMapping("/sfr-reward/top")
    public ResponseEntity<List<LearningCourseFeeDto>> getTopSfrRewardCourses(
            @RequestParam(defaultValue = "10") int limit) {
        List<LearningCourseFeeDto> topCourses = courseFeeService.getTopSfrRewardCourses(limit);
        return ResponseEntity.ok(topCourses);
    }

    /**
     * SFR報酬設定更新
     */
    @PutMapping("/{feeId}/sfr-reward")
    public ResponseEntity<Void> updateSfrReward(
            @PathVariable Long feeId,
            @RequestParam BigDecimal rewardAmount,
            @RequestParam SfrRewardCondition condition) {
        courseFeeService.updateSfrReward(feeId, rewardAmount, condition);
        return ResponseEntity.ok().build();
    }

    // ========== 割引関連API ==========

    /**
     * 割引中コース検索
     */
    @GetMapping("/discounted")
    public ResponseEntity<List<LearningCourseFeeDto>> getDiscountedCourses() {
        List<LearningCourseFeeDto> discountedCourses = courseFeeService.getDiscountedCourses();
        return ResponseEntity.ok(discountedCourses);
    }

    /**
     * 早期割引コース検索
     */
    @GetMapping("/early-bird-discount")
    public ResponseEntity<List<LearningCourseFeeDto>> getEarlyBirdDiscountCourses() {
        List<LearningCourseFeeDto> earlyBirdCourses = courseFeeService.getEarlyBirdDiscountCourses();
        return ResponseEntity.ok(earlyBirdCourses);
    }

    // ========== 複合検索API ==========

    /**
     * 複合条件検索
     */
    @GetMapping("/search")
    public ResponseEntity<Page<LearningCourseFeeDto>> searchCourses(
            @RequestParam(required = false) FeeType feeType,
            @RequestParam(required = false) CurrencyType currencyType,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean isFree,
            @RequestParam(required = false) Boolean hasSfrReward,
            @RequestParam(required = false) PaymentMethod paymentMethod,
            @RequestParam(required = false) Boolean installmentAvailable,
            @RequestParam(required = false) Boolean refundable,
            Pageable pageable) {

        Page<LearningCourseFeeDto> searchResults = courseFeeService.searchCourses(
                feeType, currencyType, minPrice, maxPrice, isFree,
                hasSfrReward, paymentMethod, installmentAvailable, refundable, pageable);

        return ResponseEntity.ok(searchResults);
    }

    // ========== 統計・分析API ==========

    /**
     * SFR報酬統計
     */
    @GetMapping("/statistics/sfr-reward")
    public ResponseEntity<Map<String, Object>> getSfrRewardStatistics() {
        Map<String, Object> statistics = courseFeeService.getSfrRewardStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * 価格統計
     */
    @GetMapping("/statistics/price")
    public ResponseEntity<Map<String, Object>> getPriceStatistics() {
        Map<String, Object> statistics = courseFeeService.getPriceStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * コース数統計
     */
    @GetMapping("/aggregation/course-counts")
    public ResponseEntity<Map<String, Long>> getCourseCounts() {
        Map<String, Long> courseCounts = courseFeeService.getCourseCounts();
        return ResponseEntity.ok(courseCounts);
    }

    // ========== 管理機能API ==========

    /**
     * 料金額更新
     */
    @PutMapping("/{feeId}/price")
    public ResponseEntity<Void> updatePriceAmount(
            @PathVariable Long feeId,
            @RequestParam BigDecimal priceAmount) {
        courseFeeService.updatePriceAmount(feeId, priceAmount);
        return ResponseEntity.ok().build();
    }

    /**
     * アクティブ状態更新
     */
    @PutMapping("/{feeId}/active-status")
    public ResponseEntity<Void> updateActiveStatus(
            @PathVariable Long feeId,
            @RequestParam Boolean isActive) {
        courseFeeService.updateActiveStatus(feeId, isActive);
        return ResponseEntity.ok().build();
    }

    /**
     * 期限切れ割引無効化
     */
    @PostMapping("/batch/disable-expired-discounts")
    public ResponseEntity<Void> disableExpiredDiscounts() {
        log.info("期限切れ割引無効化バッチ処理API呼び出し");
        courseFeeService.disableExpiredDiscounts();
        return ResponseEntity.ok().build();
    }

    /**
     * SFR総報酬額計算
     */
    @GetMapping("/aggregation/total-sfr-rewards")
    public ResponseEntity<BigDecimal> calculateTotalSfrRewards() {
        BigDecimal totalRewards = courseFeeService.calculateTotalSfrRewards();
        return ResponseEntity.ok(totalRewards);
    }

    /**
     * 平均価格計算
     */
    @GetMapping("/aggregation/average-price")
    public ResponseEntity<BigDecimal> calculateAveragePrice() {
        BigDecimal averagePrice = courseFeeService.calculateAveragePrice();
        return ResponseEntity.ok(averagePrice);
    }
}
