package com.sfr.tokyo.sfr_backend.controller.compat;

import com.sfr.tokyo.sfr_backend.dto.crypto.api.ApiRewardDtos.*;
import com.sfr.tokyo.sfr_backend.service.crypto.UserBalanceService;
import com.sfr.tokyo.sfr_backend.entity.crypto.UserBalance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import static com.sfr.tokyo.sfr_backend.compat.CompatApiConstants.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sfr/rewards")
@RequiredArgsConstructor
@Slf4j
public class RewardCompatController {

    private final UserBalanceService userBalanceService; // 簡易: 本来は reward/pool サービス利用

    private static String fmt8(BigDecimal v) {
        return v == null ? "0.00000000" : v.setScale(8, RoundingMode.DOWN).toPlainString();
    }

    @PostMapping("/calculate")
    public ResponseEntity<RewardCalculateResponseDto> calculate(@Valid @RequestBody RewardCalculateRequestDto req) {
        double combined = rewardCombinedForCalculate(req.getActivity_score(), req.getEvaluation_score());
        BigDecimal poolTotal = new BigDecimal("1000000.123456789").setScale(8, RoundingMode.DOWN);
        BigDecimal estimated = BigDecimal.valueOf(combined).divide(new BigDecimal(String.valueOf(CALCULATE_DIVISOR)), 8,
                RoundingMode.DOWN);
        RewardCalculationDetails calcDetails = RewardCalculationDetails.builder()
                .activity_score(req.getActivity_score())
                .evaluation_score(req.getEvaluation_score())
                .normalized_evaluation_score(req.getEvaluation_score() * EVALUATION_SCORE_SCALE)
                .weights(RewardCalculationWeights.builder()
                        .activity(REWARD_CALC_ACTIVITY_WEIGHT_CALC)
                        .evaluation(REWARD_CALC_EVALUATION_WEIGHT_CALC)
                        .range_min(REWARD_WEIGHT_MIN)
                        .range_max(REWARD_WEIGHT_MAX)
                        .build())
                .divisors(RewardCalculationDivisors.builder()
                        .calculate(CALCULATE_DIVISOR)
                        .issue(ISSUE_DIVISOR)
                        .build())
                .combined_before_divisor(combined)
                .rounding_mode(REWARD_ROUNDING_MODE)
                .formula_version(REWARD_FORMULA_VERSION)
                .build();
        RewardCalculateResponseDto resp = RewardCalculateResponseDto.builder()
                .user_id(req.getUser_id())
                .estimated_reward(fmt8(estimated))
                .combined_score(combined)
                .current_pool_total(fmt8(poolTotal))
                .current_pool_remaining(fmt8(poolTotal.subtract(estimated)))
                .calculation_details(calcDetails)
                .calculated_at(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/issue")
    public ResponseEntity<RewardIssueResponseDto> issue(@Valid @RequestBody RewardIssueRequestDto req) {
        Long spaceId = 1L;
        UserBalance ub = userBalanceService.getUserBalance(req.getUser_id(), spaceId)
                .orElseGet(() -> userBalanceService.createUserBalance(req.getUser_id(), spaceId, BigDecimal.ZERO));

        double combined = rewardCombinedForIssue(req.getActivity_score(), req.getEvaluation_score());
        BigDecimal reward = BigDecimal.valueOf(combined).divide(new BigDecimal(String.valueOf(ISSUE_DIVISOR)), 8,
                RoundingMode.DOWN);
        BigDecimal newBal = ub.getCurrentBalance().add(reward);
        userBalanceService.updateUserBalance(req.getUser_id(), spaceId, newBal);

        String formulaStr = "(act*" + REWARD_CALC_ACTIVITY_WEIGHT_ISSUE + " + eval*" + EVALUATION_SCORE_SCALE + "*"
                + REWARD_CALC_EVALUATION_WEIGHT_ISSUE + ")/" + ISSUE_DIVISOR;
        RewardCalculationDetails calcDetails = RewardCalculationDetails.builder()
                .activity_score(req.getActivity_score())
                .evaluation_score(req.getEvaluation_score())
                .normalized_evaluation_score(req.getEvaluation_score() * EVALUATION_SCORE_SCALE)
                .weights(RewardCalculationWeights.builder()
                        .activity(REWARD_CALC_ACTIVITY_WEIGHT_ISSUE)
                        .evaluation(REWARD_CALC_EVALUATION_WEIGHT_ISSUE)
                        .range_min(REWARD_WEIGHT_MIN)
                        .range_max(REWARD_WEIGHT_MAX)
                        .build())
                .divisors(RewardCalculationDivisors.builder()
                        .calculate(CALCULATE_DIVISOR)
                        .issue(ISSUE_DIVISOR)
                        .build())
                .combined_before_divisor(combined)
                .rounding_mode(REWARD_ROUNDING_MODE)
                .formula_version(REWARD_FORMULA_VERSION)
                .formula(formulaStr)
                .build();

        RewardIssueResponseDto resp = RewardIssueResponseDto.builder()
                .reward_id(UUID.randomUUID().toString())
                .user_id(req.getUser_id())
                .reward_amount(fmt8(reward))
                .pool_date(LocalDate.now())
                .combined_score(combined)
                .total_pool_score(1000000.0)
                .calculation_details(calcDetails)
                .issued_at(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(resp);
    }

    // ---- 抽出: ピュア関数 (後で単体テスト対象) ----
    static double rewardCombinedForCalculate(double activityScore, double evaluationScore) {
        return activityScore * REWARD_CALC_ACTIVITY_WEIGHT_CALC
                + evaluationScore * EVALUATION_SCORE_SCALE * REWARD_CALC_EVALUATION_WEIGHT_CALC;
    }

    static double rewardCombinedForIssue(double activityScore, double evaluationScore) {
        return activityScore * REWARD_CALC_ACTIVITY_WEIGHT_ISSUE
                + evaluationScore * EVALUATION_SCORE_SCALE * REWARD_CALC_EVALUATION_WEIGHT_ISSUE;
    }
}
