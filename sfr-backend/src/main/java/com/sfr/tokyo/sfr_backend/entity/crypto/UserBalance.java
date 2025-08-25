package com.sfr.tokyo.sfr_backend.entity.crypto;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ユーザー残高エンティティ
 * SFRトークンの残高情報を管理
 * 
 * @author SFR Development Team
 * @version 1.0
 * @since 2025-08-20
 */
@Entity
@Table(name = "user_balances", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "space_id" }))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(UserBalanceId.class)
public class UserBalance {

    /**
     * ユーザーID（複合主キーの一部）
     */
    @Id
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    /**
     * スペースID（複合主キーの一部）
     */
    @Id
    @Column(name = "space_id", nullable = false)
    @Builder.Default
    private Long spaceId = 1L;

    /**
     * 現在残高（8桁小数まで）
     */
    @Column(name = "current_balance", precision = 20, scale = 8, nullable = false)
    @DecimalMin(value = "0.0", message = "残高は0以上である必要があります")
    @Builder.Default
    private BigDecimal currentBalance = BigDecimal.ZERO;

    /**
     * 累計獲得額
     */
    @Column(name = "total_earned", precision = 20, scale = 8, nullable = false)
    @DecimalMin(value = "0.0", message = "累計獲得額は0以上である必要があります")
    @Builder.Default
    private BigDecimal totalEarned = BigDecimal.ZERO;

    /**
     * 累計使用額
     */
    @Column(name = "total_spent", precision = 20, scale = 8, nullable = false)
    @DecimalMin(value = "0.0", message = "累計使用額は0以上である必要があります")
    @Builder.Default
    private BigDecimal totalSpent = BigDecimal.ZERO;

    /**
     * 累計徴収額
     */
    @Column(name = "total_collected", precision = 20, scale = 8, nullable = false)
    @DecimalMin(value = "0.0", message = "累計徴収額は0以上である必要があります")
    @Builder.Default
    private BigDecimal totalCollected = BigDecimal.ZERO;

    /**
     * 最終徴収日
     */
    @Column(name = "last_collection_date")
    private LocalDate lastCollectionDate;

    /**
     * 徴収免除フラグ
     */
    @Column(name = "collection_exempt", nullable = false)
    @Builder.Default
    private Boolean collectionExempt = false;

    /**
     * 残高凍結フラグ
     */
    @Column(name = "frozen", nullable = false)
    @Builder.Default
    private Boolean frozen = false;

    /**
     * 作成日時
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新日時
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // =============================================================================
    // ビジネスメソッド
    // =============================================================================

    /**
     * 残高を増加させる
     * 
     * @param amount 増加額
     * @throws IllegalArgumentException 金額が0以下の場合
     */
    public void addBalance(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("増加額は0より大きい値である必要があります");
        }
        this.currentBalance = this.currentBalance.add(amount);
        this.totalEarned = this.totalEarned.add(amount);
    }

    /**
     * 残高を減少させる
     * 
     * @param amount 減少額
     * @throws IllegalArgumentException 金額が0以下、または残高不足の場合
     */
    public void subtractBalance(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("減少額は0より大きい値である必要があります");
        }
        if (this.currentBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("残高が不足しています");
        }
        this.currentBalance = this.currentBalance.subtract(amount);
        this.totalSpent = this.totalSpent.add(amount);
    }

    /**
     * 徴収を実行
     * 
     * @param amount 徴収額
     * @throws IllegalArgumentException 金額が0以下、または残高不足の場合
     */
    public void collect(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("徴収額は0より大きい値である必要があります");
        }
        if (this.currentBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("残高が不足しています");
        }
        this.currentBalance = this.currentBalance.subtract(amount);
        this.totalCollected = this.totalCollected.add(amount);
        this.lastCollectionDate = LocalDate.now();
    }

    /**
     * 徴収対象かどうかを判定
     * 
     * @param minimumBalance 徴収対象最低残高
     * @return 徴収対象の場合true
     */
    public boolean isCollectionTarget(BigDecimal minimumBalance) {
        return !this.collectionExempt &&
                !this.frozen &&
                this.currentBalance.compareTo(minimumBalance) >= 0;
    }

    /**
     * アカウントがアクティブかどうかを判定
     * 
     * @return アクティブの場合true
     */
    public boolean isActive() {
        return !this.frozen && this.currentBalance.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 残高を凍結
     */
    public void freeze() {
        this.frozen = true;
    }

    /**
     * 残高凍結を解除
     */
    public void unfreeze() {
        this.frozen = false;
    }

    /**
     * 徴収免除を設定
     */
    public void setCollectionExemption(boolean exempt) {
        this.collectionExempt = exempt;
    }

    // =============================================================================
    // Object methods
    // =============================================================================

    @Override
    public String toString() {
        return String.format("UserBalance{userId='%s', currentBalance=%s, frozen=%s}",
                userId, currentBalance, frozen);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UserBalance))
            return false;
        UserBalance that = (UserBalance) o;
        return userId != null && userId.equals(that.userId);
    }

    @Override
    public int hashCode() {
        return userId != null ? userId.hashCode() : 0;
    }
}
