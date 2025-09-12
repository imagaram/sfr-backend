package com.sfr.tokyo.sfr_backend.service.crypto;

import com.sfr.tokyo.sfr_backend.entity.crypto.SfrPoint;
import com.sfr.tokyo.sfr_backend.entity.crypto.UserBalance;
import com.sfr.tokyo.sfr_backend.repository.crypto.SfrPointRepository;
import com.sfr.tokyo.sfr_backend.repository.crypto.UserBalanceRepository;
import com.sfr.tokyo.sfr_backend.repository.crypto.SfrPurchaseTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * SFR統合サービス
 * Phase 1.4: 既存システム統合
 * 
 * UserBalance（既存暗号資産システム）とSfrPoint（新ポイントシステム）の
 * 相互運用性を提供し、データ整合性を保証
 * 
 * @author SFR Development Team  
 * @version 1.0
 * @since 2025-01-09
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SfrIntegrationService {

    private final SfrPointRepository sfrPointRepository;
    private final UserBalanceRepository userBalanceRepository;
    private final SfrPurchaseTransactionRepository purchaseTransactionRepository;
    
    // デフォルトスペースID
    private static final Long DEFAULT_SPACE_ID = 1L;

    /**
     * SFR購入完了時の両システム残高同期
     * Phase 1.2で購入されたSFRを既存UserBalanceシステムにも反映
     * 
     * @param userId ユーザーID
     * @param sfrAmount 購入されたSFR数量
     * @param purchaseTransactionId 購入取引ID
     */
    @Transactional
    public void syncPurchaseToLegacyBalance(String userId, BigDecimal sfrAmount, Long purchaseTransactionId) {
        log.info("SFR購入の既存システム同期開始: userId={}, amount={}, transactionId={}", 
                userId, sfrAmount, purchaseTransactionId);
        
        try {
            // 1. SfrPoint残高を更新（Phase 1.1で作成済み）
            SfrPoint sfrPoint = sfrPointRepository.findByUserIdAndSpaceId(userId, DEFAULT_SPACE_ID)
                .orElseGet(() -> createInitialSfrPoint(userId));
            
            BigDecimal previousBalance = sfrPoint.getCurrentBalance();
            sfrPoint.setCurrentBalance(sfrPoint.getCurrentBalance().add(sfrAmount));
            sfrPoint.setTotalPurchased(sfrPoint.getTotalPurchased().add(sfrAmount));
            // lastTransactionAtフィールドは存在しないため削除
            // updatedAtは@UpdateTimestampで自動更新
            
            sfrPointRepository.save(sfrPoint);
            log.info("SfrPoint残高更新完了: {} -> {}", previousBalance, sfrPoint.getCurrentBalance());
            
            // 2. 既存UserBalance残高も同期（後方互換性）
            UserBalance userBalance = userBalanceRepository.findBySpaceIdAndUserId(DEFAULT_SPACE_ID, userId)
                .orElseGet(() -> createInitialUserBalance(userId));
                
            BigDecimal previousLegacyBalance = userBalance.getCurrentBalance();
            userBalance.setCurrentBalance(userBalance.getCurrentBalance().add(sfrAmount));
            userBalance.setTotalEarned(userBalance.getTotalEarned().add(sfrAmount));
            // lastTransactionAtフィールドは存在しないため削除
            // updatedAtは@UpdateTimestampで自動更新
            
            userBalanceRepository.save(userBalance);
            log.info("UserBalance残高同期完了: {} -> {}", previousLegacyBalance, userBalance.getCurrentBalance());
            
            // 3. 購入取引の統合状態を更新（フィールド存在しないため、memoで管理）
            purchaseTransactionRepository.findById(purchaseTransactionId)
                .ifPresent(transaction -> {
                    String integrationMemo = (transaction.getMemo() != null ? transaction.getMemo() : "") + 
                                           " [統合完了:" + LocalDateTime.now() + "]";
                    transaction.setMemo(integrationMemo);
                    purchaseTransactionRepository.save(transaction);
                    log.info("購入取引統合状態更新完了: transactionId={}", purchaseTransactionId);
                });
                
        } catch (Exception e) {
            log.error("SFR購入の既存システム同期でエラー発生", e);
            throw new RuntimeException("既存システム同期に失敗しました: " + e.getMessage(), e);
        }
    }

    /**
     * 残高整合性チェック
     * SfrPointとUserBalanceの残高が一致しているかを確認
     * 
     * @param userId ユーザーID
     * @return 整合性チェック結果
     */
    @Transactional(readOnly = true)
    public BalanceConsistencyResult checkBalanceConsistency(String userId) {
        Optional<SfrPoint> sfrPointOpt = sfrPointRepository.findByUserIdAndSpaceId(userId, DEFAULT_SPACE_ID);
        Optional<UserBalance> userBalanceOpt = userBalanceRepository.findBySpaceIdAndUserId(DEFAULT_SPACE_ID, userId);
        
        BigDecimal sfrPointBalance = sfrPointOpt.map(SfrPoint::getCurrentBalance).orElse(BigDecimal.ZERO);
        BigDecimal userBalanceAmount = userBalanceOpt.map(UserBalance::getCurrentBalance).orElse(BigDecimal.ZERO);
        
        boolean isConsistent = sfrPointBalance.compareTo(userBalanceAmount) == 0;
        
        return BalanceConsistencyResult.builder()
            .userId(userId)
            .sfrPointBalance(sfrPointBalance)
            .userBalanceAmount(userBalanceAmount)
            .isConsistent(isConsistent)
            .discrepancy(sfrPointBalance.subtract(userBalanceAmount))
            .checkedAt(LocalDateTime.now())
            .build();
    }

    /**
     * 残高同期修復
     * SfrPointを正として、UserBalanceの残高を同期修復
     * 
     * @param userId ユーザーID
     */
    @Transactional
    public void repairBalanceConsistency(String userId) {
        log.info("残高同期修復開始: userId={}", userId);
        
        SfrPoint sfrPoint = sfrPointRepository.findByUserIdAndSpaceId(userId, DEFAULT_SPACE_ID)
            .orElseThrow(() -> new IllegalStateException("SfrPointが見つかりません: " + userId));
            
        UserBalance userBalance = userBalanceRepository.findBySpaceIdAndUserId(DEFAULT_SPACE_ID, userId)
            .orElseGet(() -> createInitialUserBalance(userId));
            
        // SfrPointの残高をUserBalanceに同期
        userBalance.setCurrentBalance(sfrPoint.getCurrentBalance());
        userBalance.setTotalEarned(sfrPoint.getTotalEarned());
        userBalance.setTotalSpent(sfrPoint.getTotalSpent());
        // lastTransactionAtフィールドは存在しないため削除
        // updatedAtは@UpdateTimestampで自動更新
        
        userBalanceRepository.save(userBalance);
        
        log.info("残高同期修復完了: userId={}, 修復後残高={}", userId, userBalance.getCurrentBalance());
    }

    /**
     * 既存転送システムとの互換性確保
     * 既存のUserBalance転送機能がSfrPointにも反映されるようにする
     * 
     * @param fromUserId 送信者ユーザーID
     * @param toUserId 受信者ユーザーID
     * @param amount 転送金額
     * @param transactionType 取引タイプ
     * @param description 説明
     */
    @Transactional
    public void syncLegacyTransferToSfrPoint(String fromUserId, String toUserId, 
                                           BigDecimal amount, String transactionType, String description) {
        log.info("既存転送のSfrPoint同期: from={}, to={}, amount={}, type={}", 
                fromUserId, toUserId, amount, transactionType);
        
        try {
            // 送信者のSfrPoint残高を減算
            if (fromUserId != null) {
                SfrPoint fromSfrPoint = sfrPointRepository.findByUserIdAndSpaceId(fromUserId, DEFAULT_SPACE_ID)
                    .orElseGet(() -> createInitialSfrPoint(fromUserId));
                
                fromSfrPoint.setCurrentBalance(fromSfrPoint.getCurrentBalance().subtract(amount));
                fromSfrPoint.setTotalSpent(fromSfrPoint.getTotalSpent().add(amount));
                // lastTransactionAtフィールドは存在しないため削除
                // updatedAtは@UpdateTimestampで自動更新
                
                sfrPointRepository.save(fromSfrPoint);
                log.info("送信者SfrPoint更新: userId={}, 残高={}", fromUserId, fromSfrPoint.getCurrentBalance());
            }
            
            // 受信者のSfrPoint残高を加算
            if (toUserId != null) {
                SfrPoint toSfrPoint = sfrPointRepository.findByUserIdAndSpaceId(toUserId, DEFAULT_SPACE_ID)
                    .orElseGet(() -> createInitialSfrPoint(toUserId));
                
                toSfrPoint.setCurrentBalance(toSfrPoint.getCurrentBalance().add(amount));
                toSfrPoint.setTotalEarned(toSfrPoint.getTotalEarned().add(amount));
                // lastTransactionAtフィールドは存在しないため削除
                // updatedAtは@UpdateTimestampで自動更新
                
                sfrPointRepository.save(toSfrPoint);
                log.info("受信者SfrPoint更新: userId={}, 残高={}", toUserId, toSfrPoint.getCurrentBalance());
            }
            
        } catch (Exception e) {
            log.error("既存転送のSfrPoint同期でエラー発生", e);
            throw new RuntimeException("転送同期に失敗しました: " + e.getMessage(), e);
        }
    }

    /**
     * ユーザーのSFR残高を取得
     * Phase 2.2: SFR決済システム用
     * 
     * @param userId ユーザーID
     * @return SFR残高
     */
    @Transactional(readOnly = true)
    public BigDecimal getSfrBalance(String userId) {
        log.info("SFR残高取得: userId={}", userId);
        
        Optional<SfrPoint> sfrPointOpt = sfrPointRepository.findByUserIdAndSpaceId(userId, DEFAULT_SPACE_ID);
        
        if (sfrPointOpt.isPresent()) {
            BigDecimal balance = sfrPointOpt.get().getCurrentBalance();
            log.info("SFR残高取得完了: userId={}, balance={}", userId, balance);
            return balance;
        } else {
            log.info("SFR残高が存在しません、0を返却: userId={}", userId);
            return BigDecimal.ZERO;
        }
    }

    /**
     * SFR転送処理
     * Phase 2.2: SFR決済システム用
     * 
     * @param fromUserId 送信者ID
     * @param toUserId 受信者ID
     * @param amount 転送金額
     * @param description 説明
     * @return 転送結果
     */
    @Transactional
    public com.sfr.tokyo.sfr_backend.dto.shop.TransactionResult transferSfr(
            String fromUserId, String toUserId, BigDecimal amount, String description) {
        
        log.info("SFR転送開始: from={}, to={}, amount={}, description={}", 
                fromUserId, toUserId, amount, description);
        
        try {
            // 1. 送信者の残高確認
            SfrPoint fromSfrPoint = sfrPointRepository.findByUserIdAndSpaceId(fromUserId, DEFAULT_SPACE_ID)
                .orElseThrow(() -> new IllegalArgumentException("送信者のSFRポイントが見つかりません: " + fromUserId));
            
            if (fromSfrPoint.getCurrentBalance().compareTo(amount) < 0) {
                return com.sfr.tokyo.sfr_backend.dto.shop.TransactionResult.failure(
                    "SFR残高不足です。必要額: " + amount + ", 現在残高: " + fromSfrPoint.getCurrentBalance());
            }
            
            // 2. 受信者のSFRポイント取得または作成
            SfrPoint toSfrPoint = sfrPointRepository.findByUserIdAndSpaceId(toUserId, DEFAULT_SPACE_ID)
                .orElseGet(() -> createInitialSfrPoint(toUserId));
            
            // 3. 転送実行
            fromSfrPoint.setCurrentBalance(fromSfrPoint.getCurrentBalance().subtract(amount));
            fromSfrPoint.setTotalSpent(fromSfrPoint.getTotalSpent().add(amount));
            
            toSfrPoint.setCurrentBalance(toSfrPoint.getCurrentBalance().add(amount));
            toSfrPoint.setTotalEarned(toSfrPoint.getTotalEarned().add(amount));
            
            // 4. 保存
            sfrPointRepository.save(fromSfrPoint);
            sfrPointRepository.save(toSfrPoint);
            
            // 5. 既存UserBalanceシステムにも同期
            syncLegacyTransferToSfrPoint(fromUserId, toUserId, amount, "TRANSFER", description);
            
            String transactionId = "SFR_TRANSFER_" + System.currentTimeMillis();
            
            log.info("SFR転送完了: transactionId={}, from={}, to={}, amount={}", 
                    transactionId, fromUserId, toUserId, amount);
            
            return com.sfr.tokyo.sfr_backend.dto.shop.TransactionResult.success(transactionId);
            
        } catch (Exception e) {
            log.error("SFR転送でエラーが発生しました", e);
            return com.sfr.tokyo.sfr_backend.dto.shop.TransactionResult.failure(
                "SFR転送処理に失敗しました: " + e.getMessage());
        }
    }

    /**
     * システム全体の残高同期状況を取得
     * 
     * @return 同期状況サマリー
     */
    @Transactional(readOnly = true)
    public SystemSyncSummary getSystemSyncSummary() {
        long totalSfrPointUsers = sfrPointRepository.count();
        long totalUserBalanceUsers = userBalanceRepository.count();
        
        // 簡易的な不整合数計算（完全な実装ではより効率的なクエリを使用）
        long inconsistentCount = 0; // TODO: 効率的なクエリで実装
        
        return SystemSyncSummary.builder()
            .totalSfrPointUsers(totalSfrPointUsers)
            .totalUserBalanceUsers(totalUserBalanceUsers)
            .inconsistentUsers(inconsistentCount)
            .lastSyncCheck(LocalDateTime.now())
            .build();
    }

    // ===== プライベートヘルパーメソッド =====

    private SfrPoint createInitialSfrPoint(String userId) {
        return SfrPoint.builder()
            .userId(userId)
            .spaceId(DEFAULT_SPACE_ID)
            .currentBalance(BigDecimal.ZERO)
            .totalEarned(BigDecimal.ZERO)
            .totalSpent(BigDecimal.ZERO)
            .totalPurchased(BigDecimal.ZERO)
            .sfrtEligible(false)
            .build();
    }

    private UserBalance createInitialUserBalance(String userId) {
        return UserBalance.builder()
            .userId(userId)
            .spaceId(DEFAULT_SPACE_ID)
            .currentBalance(BigDecimal.ZERO)
            .totalEarned(BigDecimal.ZERO)
            .totalSpent(BigDecimal.ZERO)
            .totalCollected(BigDecimal.ZERO)
            .build();
    }

    // ===== 内部クラス =====

    /**
     * 残高整合性チェック結果
     */
    @lombok.Data
    @lombok.Builder
    public static class BalanceConsistencyResult {
        private String userId;
        private BigDecimal sfrPointBalance;
        private BigDecimal userBalanceAmount;
        private boolean isConsistent;
        private BigDecimal discrepancy;
        private LocalDateTime checkedAt;
    }

    /**
     * システム同期サマリー
     */
    @lombok.Data
    @lombok.Builder
    public static class SystemSyncSummary {
        private long totalSfrPointUsers;
        private long totalUserBalanceUsers;
        private long inconsistentUsers;
        private LocalDateTime lastSyncCheck;
    }
}
