package com.sfr.tokyo.sfr_backend.controller.compat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import static com.sfr.tokyo.sfr_backend.controller.compat.RewardCompatController.*;
import static com.sfr.tokyo.sfr_backend.compat.CompatApiConstants.*;

/**
 * Reward計算ピュア関数の単体テスト。
 */
public class RewardFormulaUnitTest {

    @Test
    @DisplayName("calculate combined (calculate endpoint) middle case")
    void calc_combined_calculate_mid() {
        double v = rewardCombinedForCalculate(50.0, 4.5); // 4.5 * 20 = 90
        double expected = 50.0 * REWARD_CALC_ACTIVITY_WEIGHT_CALC
                + 4.5 * EVALUATION_SCORE_SCALE * REWARD_CALC_EVALUATION_WEIGHT_CALC;
        assertEquals(expected, v, 1e-9);
    }

    @Test
    @DisplayName("issue combined (issue endpoint) weighting")
    void calc_combined_issue_mid() {
        double v = rewardCombinedForIssue(70.0, 5.0);
        double expected = 70.0 * REWARD_CALC_ACTIVITY_WEIGHT_ISSUE
                + 5.0 * EVALUATION_SCORE_SCALE * REWARD_CALC_EVALUATION_WEIGHT_ISSUE;
        assertEquals(expected, v, 1e-9);
    }

    @Test
    @DisplayName("calculate combined boundaries (activity 0, eval min)")
    void calc_combined_calculate_min() {
        double v = rewardCombinedForCalculate(0.0, EVALUATION_SCORE_MIN); // eval min 1.0
        double expected = 0.0 + EVALUATION_SCORE_MIN * EVALUATION_SCORE_SCALE * REWARD_CALC_EVALUATION_WEIGHT_CALC;
        assertEquals(expected, v, 1e-9);
    }

    @Test
    @DisplayName("issue combined boundaries (activity max, eval max)")
    void calc_combined_issue_max() {
        double v = rewardCombinedForIssue(ACTIVITY_SCORE_MAX, EVALUATION_SCORE_MAX);
        double expected = ACTIVITY_SCORE_MAX * REWARD_CALC_ACTIVITY_WEIGHT_ISSUE
                + EVALUATION_SCORE_MAX * EVALUATION_SCORE_SCALE * REWARD_CALC_EVALUATION_WEIGHT_ISSUE;
        assertEquals(expected, v, 1e-9);
    }
}
