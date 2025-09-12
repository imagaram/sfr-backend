package com.serendipity.tokyo.sfrbackend.entity.sfrt;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SFRT取引履歴エンティティ
 * - 報酬配布記録
 * - 転送・交換履歴
 * - 外部取引所連携記録
 */
@Entity
@Table(name = "sfrt_transactions",
       indexes = {
           @Index(name = "idx_sfrt_transactions_user_id", columnList = "user_id"),
           @Index(name = "idx_sfrt_transactions_space_id", columnList = "space_id"),
           @Index(name = "idx_sfrt_transactions_type", columnList = "transaction_type"),
           @Index(name = "idx_sfrt_transactions_status", columnList = "status"),
           @Index(name = "idx_sfrt_transactions_created", columnList = "created_at"),
           @Index(name = "idx_sfrt_transactions_related", columnList = "related_sfr_transaction_id")
       })
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SfrtTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ユーザーID
     */
    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    /**
     * スペースID
     */
    @Column(name = "space_id", nullable = false)
    private Long spaceId;

    /**
     * SFRT取引量（18桁精度）
     */
    @Column(name = "amount", precision = 18, scale = 8, nullable = false)
    private BigDecimal amount;

    /**
     * 取引タイプ
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private SfrtTransactionType transactionType;

    /**
     * 関連SFR取引ID（報酬の場合）
     */
    @Column(name = "related_sfr_transaction_id")
    private Long relatedSfrTransactionId;

    /**
     * 転送先ユーザーID（転送の場合）
     */
    @Column(name = "to_user_id", length = 36)
    private String toUserId;

    /**
     * 転送元ユーザーID（転送の場合）
     */
    @Column(name = "from_user_id", length = 36)
    private String fromUserId;

    /**
     * 外部取引所ID
     */
    @Column(name = "external_exchange_id", length = 100)
    private String externalExchangeId;

    /**
     * 外部取引ハッシュ
     */
    @Column(name = "external_transaction_hash", length = 100)
    private String externalTransactionHash;

    /**
     * 手数料（外部取引所）
     */
    @Column(name = "fee_amount", precision = 18, scale = 8)
    private BigDecimal feeAmount;

    /**
     * 換金レート（法定通貨）
     */
    @Column(name = "exchange_rate", precision = 18, scale = 8)
    private BigDecimal exchangeRate;

    /**
     * 換金金額（円）
     */
    @Column(name = "jpy_amount", precision = 18, scale = 2)
    private BigDecimal jpyAmount;

    /**
     * 取引前残高
     */
    @Column(name = "balance_before", precision = 18, scale = 8)
    private BigDecimal balanceBefore;

    /**
     * 取引後残高
     */
    @Column(name = "balance_after", precision = 18, scale = 8)
    private BigDecimal balanceAfter;

    /**
     * 取引説明
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * メタデータ（JSON）
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    /**
     * 取引状態
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private SfrtTransactionStatus status = SfrtTransactionStatus.PENDING;

    /**
     * エラーメッセージ
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 完了日時
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * 作成日時
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * SFRT取引タイプ
     */
    public enum SfrtTransactionType {
        PURCHASE_REWARD,    // 購入報酬
        SALES_REWARD,       // 販売報酬
        PLATFORM_RESERVE,   // プラットフォーム報酬
        TRANSFER_IN,        // 転送受信
        TRANSFER_OUT,       // 転送送信
        EXCHANGE_TO_JPY,    // 法定通貨交換
        EXCHANGE_TO_CRYPTO, // 他暗号資産交換
        STAKING_REWARD,     // ステーキング報酬
        GOVERNANCE_REWARD,  // ガバナンス報酬
        LIQUIDITY_REWARD,   // 流動性提供報酬
        PENALTY,            // ペナルティ
        ADJUSTMENT,         // 調整
        MIGRATION           // マイグレーション
    }

    /**
     * SFRT取引状態
     */
    public enum SfrtTransactionStatus {
        PENDING,     // 処理待ち
        PROCESSING,  // 処理中
        COMPLETED,   // 完了
        FAILED,      // 失敗
        CANCELLED,   // キャンセル
        REVERSED     // 取り消し
    }

    /**
     * 取引完了
     */
    public void complete() {
        this.status = SfrtTransactionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 取引失敗
     */
    public void fail(String errorMessage) {
        this.status = SfrtTransactionStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    /**
     * 報酬取引チェック
     */
    public boolean isRewardTransaction() {
        return transactionType == SfrtTransactionType.PURCHASE_REWARD ||
               transactionType == SfrtTransactionType.SALES_REWARD ||
               transactionType == SfrtTransactionType.STAKING_REWARD ||
               transactionType == SfrtTransactionType.GOVERNANCE_REWARD ||
               transactionType == SfrtTransactionType.LIQUIDITY_REWARD;
    }

    /**
     * 転送取引チェック
     */
    public boolean isTransferTransaction() {
        return transactionType == SfrtTransactionType.TRANSFER_IN ||
               transactionType == SfrtTransactionType.TRANSFER_OUT;
    }

    /**
     * 交換取引チェック
     */
    public boolean isExchangeTransaction() {
        return transactionType == SfrtTransactionType.EXCHANGE_TO_JPY ||
               transactionType == SfrtTransactionType.EXCHANGE_TO_CRYPTO;
    }
}
