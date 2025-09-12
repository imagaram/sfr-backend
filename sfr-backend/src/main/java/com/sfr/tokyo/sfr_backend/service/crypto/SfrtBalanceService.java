package com.sfr.tokyo.sfr_backend.service.crypto;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sfr.tokyo.sfr_backend.entity.crypto.SfrtBalance;
import com.sfr.tokyo.sfr_backend.entity.crypto.SfrtTransaction;
import com.sfr.tokyo.sfr_backend.entity.crypto.SfrtTransactionType;
import com.sfr.tokyo.sfr_backend.repository.crypto.SfrtBalanceRepository;
import com.sfr.tokyo.sfr_backend.repository.crypto.SfrtTransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SfrtBalanceService {

    private final SfrtBalanceRepository balanceRepository;
    private final SfrtTransactionRepository txRepository;

    private static final String PLATFORM_USER_ID = "PLATFORM";

    @Transactional
    public SfrtBalance getOrCreate(String userId) {
        return balanceRepository.findByUserId(userId).orElseGet(() -> balanceRepository.save(
                SfrtBalance.builder().userId(userId).build()));
    }

    public SfrtBalance getSfrtBalance(String userId) { return getOrCreate(userId); }

    @Transactional
    public void addReward(String userId, BigDecimal amount, SfrtTransactionType type, Long relatedTxId, String desc) {
        SfrtBalance bal = getOrCreate(userId);
        bal.addReward(amount);
        balanceRepository.save(bal);
        txRepository.save(SfrtTransaction.builder()
                .userId(userId)
                .amount(amount)
                .type(type)
                .relatedSfrTransactionId(relatedTxId)
                .description(desc)
                .build());
    }

    @Transactional
    public boolean withdraw(String userId, BigDecimal amount) {
        SfrtBalance bal = getOrCreate(userId);
        if (!bal.withdraw(amount)) return false;
        balanceRepository.save(bal);
        txRepository.save(SfrtTransaction.builder()
                .userId(userId)
                .amount(amount)
                .type(SfrtTransactionType.WITHDRAWAL)
                .description("withdraw")
                .build());
        return true;
    }

    public SfrtBalance getPlatformSfrtBalance() { return getOrCreate(PLATFORM_USER_ID); }

    // 互換性維持: 旧実装が呼んでいた addSfrtReward シグネチャ
    @Transactional
    public void addSfrtReward(String userId, BigDecimal amount, SfrtTransactionType type,
            String description) {
        // legacy wrapper (no related transaction id)
        addReward(userId, amount, type, null, description);
    }

    @Transactional
    public void addSfrtReward(String userId, BigDecimal amount, SfrtTransactionType type,
            String description, Long relatedSfrTxId, Long ignoredLegacyParam) { // legacy signature with extra Long
        addReward(userId, amount, type, relatedSfrTxId, description);
    }

    // NOTE: Removed private recursive helper that caused infinite recursion & duplicate signature error
}
