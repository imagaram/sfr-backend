package com.sfr.tokyo.sfr_backend.entity.crypto;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 残高変動履歴エンティティ
 * SFRトークン残高の変動記録を管理
 * 
 * @author SFR Development Team
 * @version 1.0
 * @since 2025-08-20
 */
@Entity
@Table(name = "balance_history", indexes = {
        @Index(name = "idx_balance_history_user_id", columnList = "user_id"),
        @Index(name = "idx_balance_history_created_at", columnList = "created_at"),
        @Index(name = "idx_balance_history_type", columnList = "transaction_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceHistory {

    /**
     * 履歴ID（プライマリキー）
     */
    @Id
    @Column(name = "history_id", length = 36, nullable = false)
    private String historyId;

    /**
     * ユーザーID
     */
    @Column(name = "user_id", nullable = false)
    @NotNull(message = "ユーザーIDは必須です")
    private UUID userId;

    /**
     * トランザクション種別
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 20, nullable = false)
    @NotNull(message = "トランザクション種別は必須です")
    private TransactionType transactionType;

    /**
     * 変動金額
     */
    @Column(name = "amount", precision = 20, scale = 8, nullable = false)
    @NotNull(message = "変動金額は必須です")
    private BigDecimal amount;

    /**
     * 変動前残高
     */
    @Column(name = "balance_before", precision = 20, scale = 8, nullable = false)
    @DecimalMin(value = "0.0", message = "変動前残高は0以上である必要があります")
    private BigDecimal balanceBefore;

    /**
     * 変動後残高
     */
    @Column(name = "balance_after", precision = 20, scale = 8, nullable = false)
    @DecimalMin(value = "0.0", message = "変動後残高は0以上である必要があります")
    private BigDecimal balanceAfter;

    /**
     * 変動理由
     */
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    /**
     * 関連トランザクションID
     */
    @Column(name = "reference_id", length = 36)
    private String referenceId;

    /**
     * 作成日時
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // =============================================================================
    // トランザクション種別Enum
    // =============================================================================

    /**
     * トランザクション種別
     */
    public enum TransactionType {
        /** 報酬獲得 */
        EARN("報酬獲得"),
        /** 使用・支払い */
        SPEND("使用・支払い"),
        /** 徴収 */
        COLLECT("徴収"),
        /** バーン（焼却） */
        BURN("バーン"),
        /** 送金・転送 */
        TRANSFER("送金・転送");

        private final String displayName;

        TransactionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // =============================================================================
    // ビジネスメソッド
    // =============================================================================

    /**
     * 変動金額が正の値かどうかを判定
     * 
     * @return 正の値の場合true
     */
    public boolean isPositiveChange() {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 変動金額が負の値かどうかを判定
     * 
     * @return 負の値の場合true
     */
    public boolean isNegativeChange() {
        return amount != null && amount.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * 残高計算の整合性をチェック
     * 
     * @return 整合性に問題がない場合true
     */
    public boolean isBalanceCalculationValid() {
        if (balanceBefore == null || balanceAfter == null || amount == null) {
            return false;
        }

        BigDecimal expectedBalance = balanceBefore.add(amount);
        return expectedBalance.compareTo(balanceAfter) == 0;
    }

    /**
     * 収入系トランザクションかどうかを判定
     * 
     * @return 収入系の場合true
     */
    public boolean isIncomeTransaction() {
        return transactionType == TransactionType.EARN;
    }

    /**
     * 支出系トランザクションかどうかを判定
     * 
     * @return 支出系の場合true
     */
    public boolean isExpenseTransaction() {
        return transactionType == TransactionType.SPEND ||
                transactionType == TransactionType.COLLECT ||
                transactionType == TransactionType.BURN ||
                (transactionType == TransactionType.TRANSFER && amount.compareTo(BigDecimal.ZERO) < 0);
    }

    /**
     * システム管理トランザクションかどうかを判定
     * 
     * @return システム管理の場合true
     */
    public boolean isSystemTransaction() {
        return transactionType == TransactionType.COLLECT ||
                transactionType == TransactionType.BURN;
    }

    // =============================================================================
    // 静的ファクトリーメソッド
    // =============================================================================

    /**
     * 報酬獲得履歴を作成
     * 
     * @param historyId     履歴ID
     * @param userId        ユーザーID
     * @param amount        報酬額
     * @param balanceBefore 変動前残高
     * @param balanceAfter  変動後残高
     * @param reason        理由
     * @param referenceId   関連ID
     * @return BalanceHistory
     */
    public static BalanceHistory createEarnHistory(String historyId, UUID userId,
            BigDecimal amount, BigDecimal balanceBefore,
            BigDecimal balanceAfter, String reason,
            String referenceId) {
        return BalanceHistory.builder()
                .historyId(historyId)
                .userId(userId)
                .transactionType(TransactionType.EARN)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .reason(reason)
                .referenceId(referenceId)
                .build();
    }

    /**
     * 徴収履歴を作成
     * 
     * @param historyId     履歴ID
     * @param userId        ユーザーID
     * @param amount        徴収額（負の値）
     * @param balanceBefore 変動前残高
     * @param balanceAfter  変動後残高
     * @param reason        理由
     * @param referenceId   関連ID
     * @return BalanceHistory
     */
    public static BalanceHistory createCollectHistory(String historyId, UUID userId,
            BigDecimal amount, BigDecimal balanceBefore,
            BigDecimal balanceAfter, String reason,
            String referenceId) {
        return BalanceHistory.builder()
                .historyId(historyId)
                .userId(userId)
                .transactionType(TransactionType.COLLECT)
                .amount(amount.negate()) // 負の値として記録
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .reason(reason)
                .referenceId(referenceId)
                .build();
    }

    /**
     * 送金履歴を作成
     * 
     * @param historyId     履歴ID
     * @param userId        ユーザーID
     * @param amount        送金額（送金者は負、受金者は正）
     * @param balanceBefore 変動前残高
     * @param balanceAfter  変動後残高
     * @param reason        理由
     * @param referenceId   関連ID
     * @return BalanceHistory
     */
    public static BalanceHistory createTransferHistory(String historyId, UUID userId,
            BigDecimal amount, BigDecimal balanceBefore,
            BigDecimal balanceAfter, String reason,
            String referenceId) {
        return BalanceHistory.builder()
                .historyId(historyId)
                .userId(userId)
                .transactionType(TransactionType.TRANSFER)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .reason(reason)
                .referenceId(referenceId)
                .build();
    }

    // =============================================================================
    // Object methods
    // =============================================================================

    @Override
    public String toString() {
        return String.format("BalanceHistory{historyId='%s', userId='%s', type=%s, amount=%s}",
                historyId, userId, transactionType, amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof BalanceHistory))
            return false;
        BalanceHistory that = (BalanceHistory) o;
        return historyId != null && historyId.equals(that.historyId);
    }

    @Override
    public int hashCode() {
        return historyId != null ? historyId.hashCode() : 0;
    }
}
