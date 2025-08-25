package com.sfr.tokyo.sfr_backend.compat;

/**
 * 互換API層で使用する共通定数の一元管理。
 * 他レイヤーへ漏れないUI/互換仕様専用値をここで集中管理する。
 */
public final class CompatApiConstants {
    private CompatApiConstants() {
    }

    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    // Amount pattern (1〜8桁小数入力 / 出力はゼロパディング8桁)
    public static final String AMOUNT_INPUT_PATTERN = "^[0-9]+\\.[0-9]{1,8}$";
    public static final String AMOUNT_OUTPUT_PATTERN = "^\\d+\\.\\d{8}$";

    // Score ranges
    public static final double ACTIVITY_SCORE_MIN = 0.0;
    public static final double ACTIVITY_SCORE_MAX = 100.0;
    public static final double EVALUATION_SCORE_MIN = 1.0;
    public static final double EVALUATION_SCORE_MAX = 5.0;

    // Weight (将来パラメータ化を見据え一箇所定義)
    public static final double REWARD_CALC_ACTIVITY_WEIGHT_CALC = 0.6; // calculate endpoint
    public static final double REWARD_CALC_EVALUATION_WEIGHT_CALC = 0.4;
    public static final double REWARD_CALC_ACTIVITY_WEIGHT_ISSUE = 0.5; // issue endpoint
    public static final double REWARD_CALC_EVALUATION_WEIGHT_ISSUE = 0.5;
    public static final double REWARD_WEIGHT_MIN = 0.0; // ドキュメント化用 (含む)
    public static final double REWARD_WEIGHT_MAX = 1.0; // ドキュメント化用 (含む)
    public static final double EVALUATION_SCORE_SCALE = 20.0; // evaluation_score を activity_score スケールへ変換係数

    // Divisors
    public static final int CALCULATE_DIVISOR = 1000; // 推定報酬計算
    public static final int ISSUE_DIVISOR = 500; // 発行報酬計算

    // Rounding / Formula metadata
    public static final String REWARD_ROUNDING_MODE = "TRUNCATE_DOWN"; // API 応答表記統一
    public static final String REWARD_FORMULA_VERSION = "v1"; // 現行バージョン (enum 拡張余地)
}
