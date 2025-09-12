package com.sfr.tokyo.sfr_backend.entity.shop;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 限定品購入取引エンティティ
 * Phase 2.2: SFR決済システム
 * 
 * SFRポイントによる限定品購入取引を管理
 * 
 * @author SFR Development Team
 * @version 1.0
 * @since 2025-01-09
 */
@Entity
@Table(name = "limited_item_purchases", indexes = {
    @Index(name = "idx_limited_item_purchases_buyer_id", columnList = "buyer_id"),
    @Index(name = "idx_limited_item_purchases_seller_id", columnList = "seller_id"),
    @Index(name = "idx_limited_item_purchases_item_id", columnList = "item_id"),
    @Index(name = "idx_limited_item_purchases_status", columnList = "status"),
    @Index(name = "idx_limited_item_purchases_created_at", columnList = "created_at"),
    @Index(name = "idx_limited_item_purchases_buyer_created", columnList = "buyer_id, created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LimitedItemPurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 購入者ユーザーID
     */
    @Column(name = "buyer_id", nullable = false, length = 36)
    @NotBlank(message = "購入者IDは必須です")
    private String buyerId;

    /**
     * 販売者ユーザーID
     */
    @Column(name = "seller_id", nullable = false, length = 36)
    @NotBlank(message = "販売者IDは必須です")
    private String sellerId;

    /**
     * 商品ID
     */
    @Column(name = "item_id", nullable = false)
    @NotNull(message = "商品IDは必須です")
    private Long itemId;

    /**
     * 購入数量
     */
    @Column(name = "quantity", nullable = false)
    @NotNull(message = "購入数量は必須です")
    @Min(value = 1, message = "購入数量は1以上である必要があります")
    private Integer quantity;

    /**
     * 単価（SFR）
     */
    @Column(name = "unit_price_sfr", precision = 15, scale = 8, nullable = false)
    @NotNull(message = "単価は必須です")
    @DecimalMin(value = "0.00000001", message = "単価は0.00000001以上である必要があります")
    private BigDecimal unitPriceSfr;

    /**
     * 総額（SFR）
     */
    @Column(name = "total_price_sfr", precision = 15, scale = 8, nullable = false)
    @NotNull(message = "総額は必須です")
    @DecimalMin(value = "0.00000001", message = "総額は0.00000001以上である必要があります")
    private BigDecimal totalPriceSfr;

    /**
     * 取引手数料（SFR）
     */
    @Column(name = "transaction_fee_sfr", precision = 15, scale = 8)
    @Builder.Default
    private BigDecimal transactionFeeSfr = BigDecimal.ZERO;

    /**
     * 実際の決済額（SFR）
     */
    @Column(name = "payment_amount_sfr", precision = 15, scale = 8, nullable = false)
    @NotNull(message = "決済額は必須です")
    private BigDecimal paymentAmountSfr;

    /**
     * 購入取引ステータス
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "ステータスは必須です")
    @Builder.Default
    private PurchaseStatus status = PurchaseStatus.PENDING;

    /**
     * 購入時の商品タイトル（履歴保存）
     */
    @Column(name = "item_title_snapshot", nullable = false, length = 200)
    @NotBlank(message = "商品タイトルは必須です")
    private String itemTitleSnapshot;

    /**
     * 購入時の商品説明（履歴保存）
     */
    @Column(name = "item_description_snapshot", columnDefinition = "TEXT")
    private String itemDescriptionSnapshot;

    /**
     * デジタル商品ダウンロードURL
     */
    @Column(name = "download_url", length = 500)
    private String downloadUrl;

    /**
     * 購入者メモ
     */
    @Column(name = "buyer_memo", length = 500)
    private String buyerMemo;

    /**
     * 配送先情報（JSON）
     */
    @Column(name = "shipping_info", columnDefinition = "JSON")
    private String shippingInfo;

    /**
     * 追跡番号
     */
    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    /**
     * エラーメッセージ
     */
    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    /**
     * キャンセル理由
     */
    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    /**
     * 処理完了日時
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * キャンセル日時
     */
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    /**
     * 配送完了日時
     */
    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    /**
     * 受け取り確認日時
     */
    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    /**
     * 作成日時
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 更新日時
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ===== Enum定義 =====

    /**
     * 購入取引ステータス列挙型
     */
    public enum PurchaseStatus {
        PENDING,     // 処理待ち
        PROCESSING,  // 処理中
        COMPLETED,   // 完了
        SHIPPED,     // 配送済み
        RECEIVED,    // 受取確認済み
        CANCELLED,   // キャンセル
        FAILED,      // 失敗
        REFUNDED     // 返金済み
    }

    // ===== ビジネスロジックメソッド =====

    /**
     * 総額の計算・検証
     */
    public void calculateAndValidateTotalPrice() {
        BigDecimal calculatedTotal = unitPriceSfr.multiply(new BigDecimal(quantity));
        
        if (totalPriceSfr.compareTo(calculatedTotal) != 0) {
            throw new IllegalStateException("総額が正しく計算されていません。計算値: " + calculatedTotal + ", 設定値: " + totalPriceSfr);
        }
        
        // 決済額 = 総額 + 手数料
        this.paymentAmountSfr = totalPriceSfr.add(transactionFeeSfr);
    }

    /**
     * 取引の完了処理
     */
    public void markAsCompleted() {
        this.status = PurchaseStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 取引のキャンセル処理
     */
    public void markAsCancelled(String reason) {
        if (status == PurchaseStatus.COMPLETED || status == PurchaseStatus.RECEIVED) {
            throw new IllegalStateException("完了済みの取引はキャンセルできません");
        }
        
        this.status = PurchaseStatus.CANCELLED;
        this.cancelReason = reason;
        this.cancelledAt = LocalDateTime.now();
    }

    /**
     * 配送完了処理
     */
    public void markAsShipped(String trackingNumber) {
        if (status != PurchaseStatus.COMPLETED) {
            throw new IllegalStateException("完了していない取引は配送できません");
        }
        
        this.status = PurchaseStatus.SHIPPED;
        this.trackingNumber = trackingNumber;
        this.shippedAt = LocalDateTime.now();
    }

    /**
     * 受取確認処理
     */
    public void markAsReceived() {
        if (status != PurchaseStatus.SHIPPED) {
            throw new IllegalStateException("配送されていない取引は受取確認できません");
        }
        
        this.status = PurchaseStatus.RECEIVED;
        this.receivedAt = LocalDateTime.now();
    }

    /**
     * 取引失敗処理
     */
    public void markAsFailed(String errorMessage) {
        this.status = PurchaseStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    /**
     * 円換算総額を取得（150円/SFR）
     */
    public BigDecimal getTotalPriceInYen() {
        return totalPriceSfr.multiply(new BigDecimal("150"));
    }

    /**
     * 円換算決済額を取得（150円/SFR）
     */
    public BigDecimal getPaymentAmountInYen() {
        return paymentAmountSfr.multiply(new BigDecimal("150"));
    }

    /**
     * デジタル商品かどうか
     */
    public boolean isDigitalProduct() {
        return downloadUrl != null && !downloadUrl.isEmpty();
    }

    /**
     * 配送が必要かどうか
     */
    public boolean requiresShipping() {
        return shippingInfo != null && !shippingInfo.isEmpty();
    }

    /**
     * キャンセル可能かどうか
     */
    public boolean isCancellable() {
        return status == PurchaseStatus.PENDING || status == PurchaseStatus.PROCESSING;
    }

    /**
     * 返金可能かどうか
     */
    public boolean isRefundable() {
        return status == PurchaseStatus.COMPLETED || status == PurchaseStatus.SHIPPED;
    }

    /**
     * 失敗理由を設定
     */
    public void setFailureReason(String reason) {
        this.cancelReason = reason; // cancelReasonフィールドを流用
    }

    /**
     * 取引IDを設定
     */
    public void setTransactionId(String transactionId) {
        this.buyerMemo = (this.buyerMemo != null ? this.buyerMemo : "") + 
                       "|transactionId:" + transactionId;
    }

    /**
     * 支払額（円）を設定
     */
    public void setPaymentAmountJpy(BigDecimal amount) {
        // buyerMemoに円建て情報を保存
        this.buyerMemo = (this.buyerMemo != null ? this.buyerMemo : "") + 
                       "|jpyAmount:" + amount.toString();
    }
}
