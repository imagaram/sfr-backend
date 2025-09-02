package com.sfr.tokyo.sfr_backend.council.service;

import org.springframework.stereotype.Service;
import com.sfr.tokyo.sfr_backend.service.system.ParameterService;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * 投票資格判定サービス。
 * 現段階では SFR 残高およびアクティビティスコア取得部分はスタブ実装。
 * 後続で暗号資産ウォレット・行動ログ集計サービスと統合することを前提にインターフェース化しやすい形に保持。
 */
@Service
public class VotingEligibilityService {

    private final ParameterService parameterService;

    public VotingEligibilityService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    // 将来: CryptoBalanceService / ActivityScoreService などに差し替え
    protected BigDecimal fetchSfrBalance(UUID userId) {
        // TODO integrate with token balance system
        return BigDecimal.ONE; // 仮に1 SFR 保有として扱う
    }

    protected int fetchActivityScore(UUID userId) {
        // TODO integrate with user activity scoring system
        return 80; // 十分なアクティビティスコアを仮定
    }

    public EligibilityResult evaluate(UUID userId) {
        BigDecimal balance = fetchSfrBalance(userId);
        int activity = fetchActivityScore(userId);
    BigDecimal minBalance = parameterService.getDecimal(ParameterService.MIN_VOTE_BALANCE);
    int minActivityDays = parameterService.getInt(ParameterService.MIN_VOTE_ACTIVITY_DAYS);
    boolean balanceOk = balance.compareTo(minBalance) >= 0;
    // NOTE: 現在 activityScore は「直近活動日数」と等価ではないスタブ。将来的に別サービスから取得し日数比較へ変更予定。
    boolean activityOk = activity >= minActivityDays; 
        return new EligibilityResult(balanceOk, activityOk, balance, activity);
    }

    public record EligibilityResult(boolean balanceOk, boolean activityOk, BigDecimal balance, int activityScore) {
        public boolean eligible() { return balanceOk && activityOk; }
    }
}
