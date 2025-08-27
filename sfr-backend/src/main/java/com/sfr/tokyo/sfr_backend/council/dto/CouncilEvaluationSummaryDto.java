package com.sfr.tokyo.sfr_backend.council.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CouncilEvaluationSummaryDto {
    long userEvaluations; // 件数
    double userScoreAvg;  // 平均 0-100
    long peerEvaluations;
    double peerScoreAvg;
    Integer adminScore; // null まだ未登録
    double weightedScore; // 0-100 計算後
}
