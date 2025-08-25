package com.sfr.tokyo.sfr_backend.entity.crypto;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * TokenTransaction Entity - SFRトークン取引履歴の管理
 * すべてのトークン移動を記録する包括的な取引履歴
 */
@Entity
@Table(name = "token_transactions", indexes = {
        @Index(name = "idx_token_transactions_space_id", columnList = "space_id"),
        @Index(name = "idx_token_transactions_from_user", columnList = "from_user_id"),
        @Index(name = "idx_token_transactions_to_user", columnList = "to_user_id"),
        @Index(name = "idx_token_transactions_type", columnList = "transaction_type"),
        @Index(name = "idx_token_transactions_status", columnList = "status"),
        @Index(name = "idx_token_transactions_timestamp", columnList = "transaction_timestamp"),
        @Index(name = "idx_token_transactions_hash", columnList = "transaction_hash")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "space_id", nullable = false)
    @NotNull(message = "スペースIDは必須です")
    private Long spaceId;

    @Column(name = "transaction_hash", nullable = false, unique = true, length = 100)
    @NotBlank(message = "トランザクションハッシュは必須です")
    @Size(max = 100, message = "トランザクションハッシュは100文字以内で入力してください")
    private String transactionHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    @NotNull(message = "取引タイプは必須です")
    private TransactionType transactionType;

    @Column(name = "from_user_id", nullable = true)
    private UUID fromUserId; // 送信者（nullの場合はシステム発行）

    @Column(name = "to_user_id", nullable = true)
    private UUID toUserId; // 受信者（nullの場合はバーン）

    @Column(name = "amount", nullable = false, precision = 20, scale = 8)
    @NotNull(message = "取引量は必須です")
    @DecimalMin(value = "0.00000001", message = "取引量は0.00000001以上である必要があります")
    private BigDecimal amount;

    @Column(name = "fee_amount", nullable = false, precision = 20, scale = 8)
    @DecimalMin(value = "0.0", message = "手数料は0以上である必要があります")
    @Builder.Default
    private BigDecimal feeAmount = BigDecimal.ZERO;

    @Column(name = "net_amount", nullable = false, precision = 20, scale = 8)
    @NotNull(message = "正味取引量は必須です")
    @DecimalMin(value = "0.0", message = "正味取引量は0以上である必要があります")
    private BigDecimal netAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "ステータスは必須です")
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "transaction_timestamp", nullable = false)
    @NotNull(message = "取引タイムスタンプは必須です")
    private LocalDateTime transactionTimestamp;

    @Column(name = "block_number", nullable = true)
    private Long blockNumber;

    @Column(name = "confirmation_count", nullable = false)
    @Min(value = 0, message = "確認数は0以上である必要があります")
    @Builder.Default
    private Integer confirmationCount = 0;

    @Column(name = "gas_used", nullable = true)
    @Min(value = 0, message = "ガス使用量は0以上である必要があります")
    private Long gasUsed;

    @Column(name = "gas_price", nullable = true, precision = 20, scale = 8)
    @DecimalMin(value = "0.0", message = "ガス価格は0以上である必要があります")
    private BigDecimal gasPrice;

    @Column(name = "nonce", nullable = true)
    @Min(value = 0, message = "ナンスは0以上である必要があります")
    private Long nonce;

    @Column(name = "reference_id", nullable = true, length = 100)
    @Size(max = 100, message = "参照IDは100文字以内で入力してください")
    private String referenceId; // 関連する操作のID（リワード配布、回収、バーンなど）

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", nullable = true, length = 30)
    private ReferenceType referenceType;

    @Column(name = "description", nullable = true, length = 500)
    @Size(max = 500, message = "説明は500文字以内で入力してください")
    private String description;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON形式での追加メタデータ

    @Column(name = "from_balance_before", nullable = true, precision = 20, scale = 8)
    @DecimalMin(value = "0.0", message = "送信者取引前残高は0以上である必要があります")
    private BigDecimal fromBalanceBefore;

    @Column(name = "from_balance_after", nullable = true, precision = 20, scale = 8)
    @DecimalMin(value = "0.0", message = "送信者取引後残高は0以上である必要があります")
    private BigDecimal fromBalanceAfter;

    @Column(name = "to_balance_before", nullable = true, precision = 20, scale = 8)
    @DecimalMin(value = "0.0", message = "受信者取引前残高は0以上である必要があります")
    private BigDecimal toBalanceBefore;

    @Column(name = "to_balance_after", nullable = true, precision = 20, scale = 8)
    @DecimalMin(value = "0.0", message = "受信者取引後残高は0以上である必要があります")
    private BigDecimal toBalanceAfter;

    @Column(name = "batch_id", nullable = true)
    private UUID batchId; // バッチ処理でのグループ化

    @Column(name = "parent_transaction_id", nullable = true)
    private Long parentTransactionId; // 親取引ID（複合取引の場合）

    @Column(name = "is_system_transaction", nullable = false)
    @Builder.Default
    private Boolean isSystemTransaction = false; // システム取引かどうか

    @Column(name = "is_reversible", nullable = false)
    @Builder.Default
    private Boolean isReversible = true; // 取消可能かどうか

    @Column(name = "reversal_transaction_id", nullable = true)
    private Long reversalTransactionId; // 取消取引ID

    @Column(name = "reversal_reason", columnDefinition = "TEXT")
    private String reversalReason; // 取消理由

    @Column(name = "approved_by", nullable = true)
    private UUID approvedBy; // 承認者（手動承認が必要な場合）

    @Column(name = "approved_at", nullable = true)
    private LocalDateTime approvedAt;

    @Column(name = "failed_reason", columnDefinition = "TEXT")
    private String failedReason; // 失敗理由

    @Column(name = "retry_count", nullable = false)
    @Min(value = 0, message = "リトライ回数は0以上である必要があります")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "max_retries", nullable = false)
    @Min(value = 0, message = "最大リトライ回数は0以上である必要があります")
    @Builder.Default
    private Integer maxRetries = 3;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 列挙型定義

    /**
     * 取引タイプ
     */
    public enum TransactionType {
        TRANSFER, // 転送
        ISSUE, // 発行
        BURN, // バーン
        REWARD_DISTRIBUTION, // リワード配布
        COLLECTION, // 回収
        GOVERNANCE_STAKE, // ガバナンスステーク
        GOVERNANCE_UNSTAKE, // ガバナンスアンステーク
        DELEGATION, // 委任
        UNDELEGATION, // 委任解除
        PENALTY, // ペナルティ
        REFUND, // 返金
        AIRDROP, // エアドロップ
        SWAP, // スワップ
        LIQUIDITY_PROVISION, // 流動性提供
        LIQUIDITY_REMOVAL, // 流動性除去
        STAKING_REWARD, // ステーキングリワード
        SYSTEM_ADJUSTMENT // システム調整
    }

    /**
     * 取引ステータス
     */
    public enum TransactionStatus {
        PENDING, // 保留中
        PROCESSING, // 処理中
        CONFIRMED, // 確認済み
        COMPLETED, // 完了
        FAILED, // 失敗
        CANCELLED, // キャンセル
        REVERSED // 取消済み
    }

    /**
     * 参照タイプ
     */
    public enum ReferenceType {
        REWARD_DISTRIBUTION, // リワード配布
        COLLECTION_HISTORY, // 回収履歴
        BURN_DECISION, // バーン決定
        GOVERNANCE_PROPOSAL, // ガバナンス提案
        GOVERNANCE_VOTE, // ガバナンス投票
        USER_BALANCE, // ユーザー残高
        BALANCE_HISTORY, // 残高履歴
        TOKEN_POOL, // トークンプール
        EXTERNAL_TRANSACTION// 外部取引
    }

    // ビジネスロジックメソッド

    /**
     * 取引を処理中にマークする
     */
    public void markAsProcessing() {
        if (status == TransactionStatus.PENDING) {
            this.status = TransactionStatus.PROCESSING;
        }
    }

    /**
     * 取引を確認済みにマークする
     * 
     * @param blockNumber   ブロック番号
     * @param confirmations 確認数
     */
    public void markAsConfirmed(Long blockNumber, Integer confirmations) {
        if (status == TransactionStatus.PROCESSING) {
            this.status = TransactionStatus.CONFIRMED;
            this.blockNumber = blockNumber;
            this.confirmationCount = confirmations;
        }
    }

    /**
     * 取引を完了にマークする
     */
    public void markAsCompleted() {
        if (status == TransactionStatus.CONFIRMED) {
            this.status = TransactionStatus.COMPLETED;
        }
    }

    /**
     * 取引を失敗にマークする
     * 
     * @param reason 失敗理由
     */
    public void markAsFailed(String reason) {
        if (status == TransactionStatus.PROCESSING) {
            this.status = TransactionStatus.FAILED;
            this.failedReason = reason;
        }
    }

    /**
     * 取引をキャンセルする
     * 
     * @param reason キャンセル理由
     */
    public void cancel(String reason) {
        if (status == TransactionStatus.PENDING) {
            this.status = TransactionStatus.CANCELLED;
            this.failedReason = reason;
        }
    }

    /**
     * 取引を取り消す
     * 
     * @param reversalTransactionId 取消取引ID
     * @param reason                取消理由
     */
    public void reverse(Long reversalTransactionId, String reason) {
        if (status == TransactionStatus.COMPLETED && isReversible) {
            this.status = TransactionStatus.REVERSED;
            this.reversalTransactionId = reversalTransactionId;
            this.reversalReason = reason;
        }
    }

    /**
     * 取引を承認する
     * 
     * @param approver 承認者
     */
    public void approve(UUID approver) {
        if (status == TransactionStatus.PENDING) {
            this.approvedBy = approver;
            this.approvedAt = LocalDateTime.now();
        }
    }

    /**
     * リトライを実行する
     * 
     * @return リトライ可能かどうか
     */
    public boolean retry() {
        if (status == TransactionStatus.FAILED && retryCount < maxRetries) {
            this.retryCount++;
            this.status = TransactionStatus.PENDING;
            return true;
        }
        return false;
    }

    /**
     * 正味取引量を計算する
     */
    public void calculateNetAmount() {
        this.netAmount = this.amount.subtract(this.feeAmount);
    }

    /**
     * 取引の妥当性をチェックする
     * 
     * @return 妥当かどうか
     */
    public boolean isValid() {
        // 基本的な妥当性チェック
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        // 送信者が必要な取引タイプのチェック
        if (requiresFromUser() && fromUserId == null) {
            return false;
        }

        // 受信者が必要な取引タイプのチェック
        if (requiresToUser() && toUserId == null) {
            return false;
        }

        // 残高の整合性チェック
        if (fromBalanceBefore != null && fromBalanceAfter != null) {
            BigDecimal expectedBalance = fromBalanceBefore.subtract(amount);
            if (fromBalanceAfter.compareTo(expectedBalance) != 0) {
                return false;
            }
        }

        if (toBalanceBefore != null && toBalanceAfter != null) {
            BigDecimal expectedBalance = toBalanceBefore.add(netAmount);
            if (toBalanceAfter.compareTo(expectedBalance) != 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * 送信者が必要な取引タイプかチェック
     * 
     * @return 送信者が必要かどうか
     */
    private boolean requiresFromUser() {
        return transactionType == TransactionType.TRANSFER ||
                transactionType == TransactionType.COLLECTION ||
                transactionType == TransactionType.BURN ||
                transactionType == TransactionType.GOVERNANCE_STAKE ||
                transactionType == TransactionType.DELEGATION ||
                transactionType == TransactionType.SWAP ||
                transactionType == TransactionType.LIQUIDITY_PROVISION;
    }

    /**
     * 受信者が必要な取引タイプかチェック
     * 
     * @return 受信者が必要かどうか
     */
    private boolean requiresToUser() {
        return transactionType == TransactionType.TRANSFER ||
                transactionType == TransactionType.ISSUE ||
                transactionType == TransactionType.REWARD_DISTRIBUTION ||
                transactionType == TransactionType.GOVERNANCE_UNSTAKE ||
                transactionType == TransactionType.UNDELEGATION ||
                transactionType == TransactionType.REFUND ||
                transactionType == TransactionType.AIRDROP ||
                transactionType == TransactionType.STAKING_REWARD;
    }

    /**
     * 取引が完了しているかチェック
     * 
     * @return 完了しているかどうか
     */
    public boolean isCompleted() {
        return status == TransactionStatus.COMPLETED;
    }

    /**
     * 取引が進行中かチェック
     * 
     * @return 進行中かどうか
     */
    public boolean isPending() {
        return status == TransactionStatus.PENDING ||
                status == TransactionStatus.PROCESSING ||
                status == TransactionStatus.CONFIRMED;
    }

    /**
     * 転送取引ファクトリメソッド
     */
    public static TokenTransaction createTransfer(
            Long spaceId,
            UUID fromUserId,
            UUID toUserId,
            BigDecimal amount,
            String description) {

        TokenTransaction transaction = TokenTransaction.builder()
                .spaceId(spaceId)
                .transactionType(TransactionType.TRANSFER)
                .fromUserId(fromUserId)
                .toUserId(toUserId)
                .amount(amount)
                .description(description)
                .transactionTimestamp(LocalDateTime.now())
                .transactionHash(generateTransactionHash())
                .build();

        transaction.calculateNetAmount();
        return transaction;
    }

    /**
     * リワード配布取引ファクトリメソッド
     */
    public static TokenTransaction createRewardDistribution(
            Long spaceId,
            UUID toUserId,
            BigDecimal amount,
            String rewardId,
            String description) {

        return TokenTransaction.builder()
                .spaceId(spaceId)
                .transactionType(TransactionType.REWARD_DISTRIBUTION)
                .toUserId(toUserId)
                .amount(amount)
                .netAmount(amount)
                .description(description)
                .referenceId(rewardId)
                .referenceType(ReferenceType.REWARD_DISTRIBUTION)
                .transactionTimestamp(LocalDateTime.now())
                .transactionHash(generateTransactionHash())
                .isSystemTransaction(true)
                .build();
    }

    /**
     * 回収取引ファクトリメソッド
     */
    public static TokenTransaction createCollection(
            Long spaceId,
            UUID fromUserId,
            BigDecimal amount,
            String collectionId,
            String description) {

        return TokenTransaction.builder()
                .spaceId(spaceId)
                .transactionType(TransactionType.COLLECTION)
                .fromUserId(fromUserId)
                .amount(amount)
                .netAmount(amount)
                .description(description)
                .referenceId(collectionId)
                .referenceType(ReferenceType.COLLECTION_HISTORY)
                .transactionTimestamp(LocalDateTime.now())
                .transactionHash(generateTransactionHash())
                .isSystemTransaction(true)
                .isReversible(false)
                .build();
    }

    /**
     * バーン取引ファクトリメソッド
     */
    public static TokenTransaction createBurn(
            Long spaceId,
            BigDecimal amount,
            String burnDecisionId,
            String description) {

        return TokenTransaction.builder()
                .spaceId(spaceId)
                .transactionType(TransactionType.BURN)
                .amount(amount)
                .netAmount(amount)
                .description(description)
                .referenceId(burnDecisionId)
                .referenceType(ReferenceType.BURN_DECISION)
                .transactionTimestamp(LocalDateTime.now())
                .transactionHash(generateTransactionHash())
                .isSystemTransaction(true)
                .isReversible(false)
                .build();
    }

    /**
     * トークン発行取引ファクトリメソッド
     */
    public static TokenTransaction createIssuance(
            Long spaceId,
            BigDecimal amount,
            String description) {

        return TokenTransaction.builder()
                .spaceId(spaceId)
                .transactionType(TransactionType.ISSUE)
                .amount(amount)
                .netAmount(amount)
                .description(description)
                .transactionTimestamp(LocalDateTime.now())
                .transactionHash(generateTransactionHash())
                .isSystemTransaction(true)
                .isReversible(false)
                .build();
    }

    /**
     * トランザクションハッシュを生成する
     * 
     * @return 生成されたハッシュ
     */
    private static String generateTransactionHash() {
        return "0x" + UUID.randomUUID().toString().replace("-", "").substring(0, 32);
    }
}
