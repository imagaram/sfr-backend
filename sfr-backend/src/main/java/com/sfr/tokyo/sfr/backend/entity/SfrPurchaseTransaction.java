package com.sfr.tokyo.sfr.backend.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sfr_purchase_transactions")
@EntityListeners(AuditingEntityListener.class)
public class SfrPurchaseTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "space_id", nullable = false)
    private Long spaceId;
    
    @Column(name = "yen_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal yenAmount;
    
    @Column(name = "sfr_amount", precision = 18, scale = 8, nullable = false)
    private BigDecimal sfrAmount;
    
    @Column(name = "exchange_rate", precision = 18, scale = 2, nullable = false)
    private BigDecimal exchangeRate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;
    
    @Column(name = "stripe_payment_intent_id", length = 100)
    private String stripePaymentIntentId;
    
    @Column(name = "stripe_payment_status", length = 50)
    private String stripePaymentStatus;
    
    @Column(name = "memo", length = 500)
    private String memo;
    
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "sfrt_distributed", nullable = false)
    private Boolean sfrtDistributed = false;
    
    @Column(name = "sfrt_distribution_id")
    private Long sfrtDistributionId;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum TransactionStatus {
        PENDING,     // 処理中
        PROCESSING,  // 決済処理中
        COMPLETED,   // 完了
        FAILED,      // 失敗
        CANCELLED,   // キャンセル
        REFUNDED     // 返金済み
    }
    
    // コンストラクタ
    public SfrPurchaseTransaction() {}
    
    public SfrPurchaseTransaction(String userId, Long spaceId, BigDecimal yenAmount, 
                                BigDecimal sfrAmount, BigDecimal exchangeRate) {
        this.userId = userId;
        this.spaceId = spaceId;
        this.yenAmount = yenAmount;
        this.sfrAmount = sfrAmount;
        this.exchangeRate = exchangeRate;
        this.status = TransactionStatus.PENDING;
        this.sfrtDistributed = false;
    }
    
    // ビジネスメソッド
    public void completeTransaction() {
        this.status = TransactionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
    
    public void failTransaction(String errorMessage) {
        this.status = TransactionStatus.FAILED;
        this.errorMessage = errorMessage;
    }
    
    public void cancelTransaction() {
        this.status = TransactionStatus.CANCELLED;
    }
    
    public void refundTransaction() {
        this.status = TransactionStatus.REFUNDED;
    }
    
    public void markSfrtDistributed(Long distributionId) {
        this.sfrtDistributed = true;
        this.sfrtDistributionId = distributionId;
    }
    
    public boolean isCompleted() {
        return TransactionStatus.COMPLETED.equals(this.status);
    }
    
    public boolean canDistributeSfrt() {
        return isCompleted() && !sfrtDistributed;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public Long getSpaceId() {
        return spaceId;
    }
    
    public void setSpaceId(Long spaceId) {
        this.spaceId = spaceId;
    }
    
    public BigDecimal getYenAmount() {
        return yenAmount;
    }
    
    public void setYenAmount(BigDecimal yenAmount) {
        this.yenAmount = yenAmount;
    }
    
    public BigDecimal getSfrAmount() {
        return sfrAmount;
    }
    
    public void setSfrAmount(BigDecimal sfrAmount) {
        this.sfrAmount = sfrAmount;
    }
    
    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }
    
    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }
    
    public TransactionStatus getStatus() {
        return status;
    }
    
    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
    
    public String getStripePaymentIntentId() {
        return stripePaymentIntentId;
    }
    
    public void setStripePaymentIntentId(String stripePaymentIntentId) {
        this.stripePaymentIntentId = stripePaymentIntentId;
    }
    
    public String getStripePaymentStatus() {
        return stripePaymentStatus;
    }
    
    public void setStripePaymentStatus(String stripePaymentStatus) {
        this.stripePaymentStatus = stripePaymentStatus;
    }
    
    public String getMemo() {
        return memo;
    }
    
    public void setMemo(String memo) {
        this.memo = memo;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public Boolean getSfrtDistributed() {
        return sfrtDistributed;
    }
    
    public void setSfrtDistributed(Boolean sfrtDistributed) {
        this.sfrtDistributed = sfrtDistributed;
    }
    
    public Long getSfrtDistributionId() {
        return sfrtDistributionId;
    }
    
    public void setSfrtDistributionId(Long sfrtDistributionId) {
        this.sfrtDistributionId = sfrtDistributionId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
