package com.sfr.tokyo.sfr_backend.controller.compat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * rewardCombinedForCalculate / rewardCombinedForIssue のピュア計算境界テスト。
 * 重み/スケール定義:
 * CALCULATE: activity*0.6 + evaluation*20*0.4
 * ISSUE: activity*0.5 + evaluation*20*0.5 = activity*0.5 + evaluation*10
 * 評価スコアは [1.0,5.0], 活動スコアは [0.0,100.0] を想定。
 */
class RewardCombinedPureFunctionTest {

    private static final double EPS = 1e-9;
    // 定義された重み/スケール
    private static final double CALC_W_ACTIVITY = 0.6;
    private static final double CALC_W_EVAL = 0.4;
    private static final double ISSUE_W_ACTIVITY = 0.5;
    private static final double ISSUE_W_EVAL = 0.5;
    private static final double EVAL_SCALE = 20.0;

    private double expectedCalc(double activity, double eval) {
        return activity * CALC_W_ACTIVITY + eval * EVAL_SCALE * CALC_W_EVAL;
    }

    private double expectedIssue(double activity, double eval) {
        return activity * ISSUE_W_ACTIVITY + eval * EVAL_SCALE * ISSUE_W_EVAL;
    }

    @Nested
    @DisplayName("rewardCombinedForCalculate 境界")
    class Calculate {
        @Test
        void minActivity_minEval() {
            double v = RewardCompatController.rewardCombinedForCalculate(0.0, 1.0);
            assertEquals(expectedCalc(0.0, 1.0), v, EPS);
        }

        @Test
        void minActivity_maxEval() {
            double v = RewardCompatController.rewardCombinedForCalculate(0.0, 5.0);
            assertEquals(expectedCalc(0.0, 5.0), v, EPS);
        }

        @Test
        void maxActivity_minEval() {
            double v = RewardCompatController.rewardCombinedForCalculate(100.0, 1.0);
            assertEquals(expectedCalc(100.0, 1.0), v, EPS);
        }

        @Test
        void maxActivity_maxEval() {
            double v = RewardCompatController.rewardCombinedForCalculate(100.0, 5.0);
            assertEquals(expectedCalc(100.0, 5.0), v, EPS);
        }

        @Test
        void midActivity_midEval() { // 追加: 中間ケース
            double v = RewardCompatController.rewardCombinedForCalculate(50.0, 3.0);
            assertEquals(expectedCalc(50.0, 3.0), v, EPS);
        }
    }

    @Nested
    @DisplayName("rewardCombinedForIssue 境界")
    class Issue {
        @Test
        void minActivity_minEval() {
            double v = RewardCompatController.rewardCombinedForIssue(0.0, 1.0);
            assertEquals(expectedIssue(0.0, 1.0), v, EPS);
        }

        @Test
        void minActivity_maxEval() {
            double v = RewardCompatController.rewardCombinedForIssue(0.0, 5.0);
            assertEquals(expectedIssue(0.0, 5.0), v, EPS);
        }

        @Test
        void maxActivity_minEval() {
            double v = RewardCompatController.rewardCombinedForIssue(100.0, 1.0);
            assertEquals(expectedIssue(100.0, 1.0), v, EPS);
        }

        @Test
        void maxActivity_maxEval() {
            double v = RewardCompatController.rewardCombinedForIssue(100.0, 5.0);
            assertEquals(expectedIssue(100.0, 5.0), v, EPS);
        }

        @Test
        void midActivity_midEval() { // 追加: 中間ケース
            double v = RewardCompatController.rewardCombinedForIssue(50.0, 3.0);
            assertEquals(expectedIssue(50.0, 3.0), v, EPS);
        }
    }

    @Test
    @DisplayName("単調性: activity 増加で値も増加 (evaluation 固定)")
    void monotonicActivity() {
        double low = RewardCompatController.rewardCombinedForCalculate(10.0, 3.0);
        double high = RewardCompatController.rewardCombinedForCalculate(20.0, 3.0);
        assertTrue(high > low, "activity を増やすと計算値は増加するはず");
    }

    @Test
    @DisplayName("単調性: evaluation 増加で値も増加 (activity 固定) - issue")
    void monotonicEvaluationIssue() {
        double low = RewardCompatController.rewardCombinedForIssue(30.0, 2.0);
        double high = RewardCompatController.rewardCombinedForIssue(30.0, 4.0);
        assertTrue(high > low, "evaluation を増やすと計算値は増加するはず");
    }
}
