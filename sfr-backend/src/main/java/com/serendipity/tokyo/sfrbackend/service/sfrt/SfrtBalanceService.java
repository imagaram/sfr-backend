package com.serendipity.tokyo.sfrbackend.service.sfrt;

import com.serendipity.tokyo.sfrbackend.entity.sfrt.SfrtBalance;
import com.serendipity.tokyo.sfrbackend.entity.sfrt.SfrtTransaction;
import com.serendipity.tokyo.sfrbackend.repository.sfrt.SfrtBalanceRepository;
import com.serendipity.tokyo.sfrbackend.repository.sfrt.SfrtTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * SFRT残高管理サービス
 * - SFRT残高の作成・更新
 * - 取引履歴記録
 * - 残高検証
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SfrtBalanceService {

    private final SfrtBalanceRepository sfrtBalanceRepository;
    private final SfrtTransactionRepository sfrtTransactionRepository;

    /**
     * SFRT残高を取得（存在しない場合は作成）
     */
    @Transactional
    public SfrtBalance getOrCreateSfrtBalance(String userId, Long spaceId) {
        return sfrtBalanceRepository.findByUserIdAndSpaceId(userId, spaceId)
            .orElseGet(() -> createNewSfrtBalance(userId, spaceId));
    }

    /**
     * 新しいSFRT残高を作成
     */
    private SfrtBalance createNewSfrtBalance(String userId, Long spaceId) {
        SfrtBalance balance = SfrtBalance.builder()
            .userId(userId)
            .spaceId(spaceId)
            .currentBalance(BigDecimal.ZERO)
            .totalEarnedPurchase(BigDecimal.ZERO)
            .totalEarnedSales(BigDecimal.ZERO)
            .totalRedeemed(BigDecimal.ZERO)
            .totalTransferredIn(BigDecimal.ZERO)
            .totalTransferredOut(BigDecimal.ZERO)
            .externalExchangeEnabled(true)
            .status(SfrtBalance.SfrtStatus.ACTIVE)
            .build();

        SfrtBalance saved = sfrtBalanceRepository.save(balance);
        log.info("新しいSFRT残高を作成しました: userId={}, spaceId={}", userId, spaceId);
        return saved;
    }

    /**
     * SFRT残高を増加（報酬配布）
     */
    @Transactional
    public SfrtTransaction addSfrtBalance(
        String userId,
        Long spaceId,
        BigDecimal amount,
        SfrtTransaction.SfrtTransactionType transactionType,
        String description
    ) {
        return addSfrtBalance(userId, spaceId, amount, transactionType, description, null);
    }

    /**
     * SFRT残高を増加（関連SFR取引あり）
     */
    @Transactional
    public SfrtTransaction addSfrtBalance(
        String userId,
        Long spaceId,
        BigDecimal amount,
        SfrtTransaction.SfrtTransactionType transactionType,
        String description,
        Long relatedSfrTransactionId
    ) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("追加金額は正の値である必要があります");
        }

        // SFRT残高を取得または作成
        SfrtBalance balance = getOrCreateSfrtBalance(userId, spaceId);
        BigDecimal balanceBefore = balance.getCurrentBalance();

        // 残高を増加
        balance.increaseBalance(amount);

        // 取引タイプ別の累計を更新
        updateCumulativeAmounts(balance, amount, transactionType);

        // 最後の報酬配布日時を更新
        if (isRewardTransaction(transactionType)) {
            balance.setLastRewardDistribution(LocalDateTime.now());
        }

        // 残高を保存
        SfrtBalance savedBalance = sfrtBalanceRepository.save(balance);

        // 取引履歴を記録
        SfrtTransaction transaction = SfrtTransaction.builder()
            .userId(userId)
            .spaceId(spaceId)
            .amount(amount)
            .transactionType(transactionType)
            .relatedSfrTransactionId(relatedSfrTransactionId)
            .balanceBefore(balanceBefore)
            .balanceAfter(savedBalance.getCurrentBalance())
            .description(description)
            .status(SfrtTransaction.SfrtTransactionStatus.COMPLETED)
            .completedAt(LocalDateTime.now())
            .build();

        SfrtTransaction savedTransaction = sfrtTransactionRepository.save(transaction);
        
        log.info("SFRT残高を増加しました: userId={}, amount={}, type={}, newBalance={}", 
            userId, amount, transactionType, savedBalance.getCurrentBalance());

        return savedTransaction;
    }

    /**
     * SFRT残高を減少（交換・転送）
     */
    @Transactional
    public SfrtTransaction deductSfrtBalance(
        String userId,
        Long spaceId,
        BigDecimal amount,
        SfrtTransaction.SfrtTransactionType transactionType,
        String description
    ) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("減少金額は正の値である必要があります");
        }

        // SFRT残高を取得
        SfrtBalance balance = sfrtBalanceRepository.findByUserIdAndSpaceId(userId, spaceId)
            .orElseThrow(() -> new IllegalArgumentException("SFRT残高が見つかりません"));

        // 残高不足チェック
        if (balance.hasInsufficientBalance(amount)) {
            throw new IllegalArgumentException("SFRT残高が不足しています");
        }

        // アクティブ状態チェック
        if (!balance.isActive()) {
            throw new IllegalArgumentException("SFRT残高が利用停止状態です");
        }

        BigDecimal balanceBefore = balance.getCurrentBalance();

        // 残高を減少
        balance.decreaseBalance(amount);

        // 取引タイプ別の累計を更新
        if (transactionType == SfrtTransaction.SfrtTransactionType.EXCHANGE_TO_JPY ||
            transactionType == SfrtTransaction.SfrtTransactionType.EXCHANGE_TO_CRYPTO) {
            balance.setTotalRedeemed(balance.getTotalRedeemed().add(amount));
        } else if (transactionType == SfrtTransaction.SfrtTransactionType.TRANSFER_OUT) {
            balance.setTotalTransferredOut(balance.getTotalTransferredOut().add(amount));
        }

        // 残高を保存
        SfrtBalance savedBalance = sfrtBalanceRepository.save(balance);

        // 取引履歴を記録
        SfrtTransaction transaction = SfrtTransaction.builder()
            .userId(userId)
            .spaceId(spaceId)
            .amount(amount.negate()) // 減少は負の値で記録
            .transactionType(transactionType)
            .balanceBefore(balanceBefore)
            .balanceAfter(savedBalance.getCurrentBalance())
            .description(description)
            .status(SfrtTransaction.SfrtTransactionStatus.COMPLETED)
            .completedAt(LocalDateTime.now())
            .build();

        SfrtTransaction savedTransaction = sfrtTransactionRepository.save(transaction);
        
        log.info("SFRT残高を減少しました: userId={}, amount={}, type={}, newBalance={}", 
            userId, amount, transactionType, savedBalance.getCurrentBalance());

        return savedTransaction;
    }

    /**
     * 取引タイプ別累計金額を更新
     */
    private void updateCumulativeAmounts(
        SfrtBalance balance, 
        BigDecimal amount, 
        SfrtTransaction.SfrtTransactionType transactionType
    ) {
        switch (transactionType) {
            case PURCHASE_REWARD:
                balance.setTotalEarnedPurchase(balance.getTotalEarnedPurchase().add(amount));
                break;
            case SALES_REWARD:
                balance.setTotalEarnedSales(balance.getTotalEarnedSales().add(amount));
                break;
            case TRANSFER_IN:
                balance.setTotalTransferredIn(balance.getTotalTransferredIn().add(amount));
                break;
            default:
                // その他の取引タイプは特別な累計更新なし
                break;
        }
    }

    /**
     * 統計情報取得メソッド群
     */
    
    public BigDecimal getTotalSfrtSupply() {
        try {
            return sfrtBalanceRepository.getTotalSupply();
        } catch (Exception e) {
            log.error("総SFRT供給量取得エラー", e);
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getActiveSfrtSupply() {
        try {
            return sfrtBalanceRepository.getActiveTotalSupply();
        } catch (Exception e) {
            log.error("アクティブSFRT供給量取得エラー", e);
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getFrozenSfrtSupply() {
        try {
            return sfrtBalanceRepository.getFrozenTotalSupply();
        } catch (Exception e) {
            log.error("凍結SFRT供給量取得エラー", e);
            return BigDecimal.ZERO;
        }
    }

    public Map<String, Object> getSfrtDistributionStats() {
        try {
            Long totalAccounts = sfrtBalanceRepository.getTotalAccountsCount();
            Long activeAccounts = sfrtBalanceRepository.getActiveAccountsCount();
            BigDecimal averageBalance = sfrtBalanceRepository.getAverageBalance();
            BigDecimal medianBalance = sfrtBalanceRepository.getMedianBalance();

            return Map.of(
                "totalAccounts", totalAccounts,
                "activeAccounts", activeAccounts,
                "averageBalance", averageBalance,
                "medianBalance", medianBalance
            );
        } catch (Exception e) {
            log.error("SFRT分布統計取得エラー", e);
            return Map.of();
        }
    }

    public BigDecimal getSpaceSfrtSupply(Long spaceId) {
        try {
            return sfrtBalanceRepository.getSpaceTotalSupply(spaceId);
        } catch (Exception e) {
            log.error("Space SFRT供給量取得エラー: spaceId={}", spaceId, e);
            return BigDecimal.ZERO;
        }
    }

    public Long getActiveUsersCount(Long spaceId) {
        try {
            return sfrtBalanceRepository.getActiveUsersCountBySpace(spaceId);
        } catch (Exception e) {
            log.error("Space活动用户数取得エラー: spaceId={}", spaceId, e);
            return 0L;
        }
    }

    public Long getTotalAccountsCount() {
        try {
            return sfrtBalanceRepository.getTotalAccountsCount();
        } catch (Exception e) {
            log.error("総アカウント数取得エラー", e);
            return 0L;
        }
    }

    public Long getActiveAccountsCount() {
        try {
            return sfrtBalanceRepository.getActiveAccountsCount();
        } catch (Exception e) {
            log.error("アクティブアカウント数取得エラー", e);
            return 0L;
        }
    }

    /**
     * 報酬取引かどうかをチェック
     */
    private boolean isRewardTransaction(SfrtTransaction.SfrtTransactionType transactionType) {
        return transactionType == SfrtTransaction.SfrtTransactionType.PURCHASE_REWARD ||
               transactionType == SfrtTransaction.SfrtTransactionType.SALES_REWARD ||
               transactionType == SfrtTransaction.SfrtTransactionType.STAKING_REWARD ||
               transactionType == SfrtTransaction.SfrtTransactionType.GOVERNANCE_REWARD ||
               transactionType == SfrtTransaction.SfrtTransactionType.LIQUIDITY_REWARD;
    }

    /**
     * SFRT残高を凍結
     */
    @Transactional
    public void freezeSfrtBalance(String userId, Long spaceId) {
        sfrtBalanceRepository.updateStatus(userId, spaceId, SfrtBalance.SfrtStatus.FROZEN);
        log.info("SFRT残高を凍結しました: userId={}, spaceId={}", userId, spaceId);
    }

    /**
     * SFRT残高の凍結を解除
     */
    @Transactional
    public void unfreezeSfrtBalance(String userId, Long spaceId) {
        sfrtBalanceRepository.updateStatus(userId, spaceId, SfrtBalance.SfrtStatus.ACTIVE);
        log.info("SFRT残高の凍結を解除しました: userId={}, spaceId={}", userId, spaceId);
    }

    /**
     * 外部取引所連携を設定
     */
    @Transactional
    public void setExternalExchangeEnabled(String userId, Long spaceId, boolean enabled) {
        sfrtBalanceRepository.updateExternalExchangeEnabled(userId, spaceId, enabled);
        log.info("外部取引所連携を{}しました: userId={}, spaceId={}", 
            enabled ? "有効" : "無効", userId, spaceId);
    }

    /**
     * SFRT残高の検証
     */
    public boolean validateSfrtBalance(String userId, Long spaceId) {
        Optional<SfrtBalance> balanceOpt = sfrtBalanceRepository.findByUserIdAndSpaceId(userId, spaceId);
        if (balanceOpt.isEmpty()) {
            return true; // 残高なしは有効
        }

        SfrtBalance balance = balanceOpt.get();
        
        // 負の残高チェック
        if (balance.getCurrentBalance().compareTo(BigDecimal.ZERO) < 0) {
            log.error("SFRT残高が負の値です: userId={}, balance={}", userId, balance.getCurrentBalance());
            return false;
        }

        return true;
    }
}
