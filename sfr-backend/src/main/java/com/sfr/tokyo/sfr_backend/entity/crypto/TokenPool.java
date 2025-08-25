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
 * TokenPool Entity - トークンプールの管理
 * 各スペースでのSFRトークンのプールを管理
 */
@Entity
@Table(name = "token_pools", indexes = {
        @Index(name = "idx_token_pools_space_id", columnList = "space_id"),
        @Index(name = "idx_token_pools_status", columnList = "status"),
        @Index(name = "idx_token_pools_updated_at", columnList = "updated_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenPool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "space_id", nullable = false)
    @NotNull(message = "スペースIDは必須です")
    private Long spaceId;

    @Column(name = "total_supply", nullable = false, precision = 20, scale = 8)
    @NotNull(message = "総供給量は必須です")
    @DecimalMin(value = "0.0", message = "総供給量は0以上である必要があります")
    @Builder.Default
    private BigDecimal totalSupply = BigDecimal.ZERO;

    @Column(name = "issued_amount", nullable = false, precision = 20, scale = 8)
    @NotNull(message = "発行済み量は必須です")
    @DecimalMin(value = "0.0", message = "発行済み量は0以上である必要があります")
    @Builder.Default
    private BigDecimal issuedAmount = BigDecimal.ZERO;

    @Column(name = "burned_amount", nullable = false, precision = 20, scale = 8)
    @NotNull(message = "バーン済み量は必須です")
    @DecimalMin(value = "0.0", message = "バーン済み量は0以上である必要があります")
    @Builder.Default
    private BigDecimal burnedAmount = BigDecimal.ZERO;

    @Column(name = "circulating_supply", nullable = false, precision = 20, scale = 8)
    @NotNull(message = "流通量は必須です")
    @DecimalMin(value = "0.0", message = "流通量は0以上である必要があります")
    @Builder.Default
    private BigDecimal circulatingSupply = BigDecimal.ZERO;

    @Column(name = "reserve_pool", nullable = false, precision = 20, scale = 8)
    @NotNull(message = "リザーブプールは必須です")
    @DecimalMin(value = "0.0", message = "リザーブプールは0以上である必要があります")
    @Builder.Default
    private BigDecimal reservePool = BigDecimal.ZERO;

    @Column(name = "reward_pool", nullable = false, precision = 20, scale = 8)
    @NotNull(message = "リワードプールは必須です")
    @DecimalMin(value = "0.0", message = "リワードプールは0以上である必要があります")
    @Builder.Default
    private BigDecimal rewardPool = BigDecimal.ZERO;

    @Column(name = "governance_pool", nullable = false, precision = 20, scale = 8)
    @NotNull(message = "ガバナンスプールは必須です")
    @DecimalMin(value = "0.0", message = "ガバナンスプールは0以上である必要があります")
    @Builder.Default
    private BigDecimal governancePool = BigDecimal.ZERO;

    @Column(name = "ecosystem_pool", nullable = false, precision = 20, scale = 8)
    @NotNull(message = "エコシステムプールは必須です")
    @DecimalMin(value = "0.0", message = "エコシステムプールは0以上である必要があります")
    @Builder.Default
    private BigDecimal ecosystemPool = BigDecimal.ZERO;

    @Column(name = "issue_rate", nullable = false, precision = 10, scale = 6)
    @NotNull(message = "発行レートは必須です")
    @DecimalMin(value = "0.000001", message = "発行レートは0.000001以上である必要があります")
    @DecimalMax(value = "1.0", message = "発行レートは1.0以下である必要があります")
    @Builder.Default
    private BigDecimal issueRate = new BigDecimal("0.001000"); // デフォルト0.1%

    @Column(name = "burn_rate", nullable = false, precision = 10, scale = 6)
    @NotNull(message = "バーンレートは必須です")
    @DecimalMin(value = "0.0", message = "バーンレートは0以上である必要があります")
    @DecimalMax(value = "1.0", message = "バーンレートは1.0以下である必要があります")
    @Builder.Default
    private BigDecimal burnRate = new BigDecimal("0.000500"); // デフォルト0.05%

    @Column(name = "collection_threshold", nullable = false, precision = 20, scale = 8)
    @NotNull(message = "回収閾値は必須です")
    @DecimalMin(value = "0.0", message = "回収閾値は0以上である必要があります")
    @Builder.Default
    private BigDecimal collectionThreshold = new BigDecimal("1000.00000000");

    @Column(name = "max_supply", precision = 20, scale = 8)
    @DecimalMin(value = "0.0", message = "最大供給量は0以上である必要があります")
    private BigDecimal maxSupply;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "ステータスは必須です")
    @Builder.Default
    private PoolStatus status = PoolStatus.ACTIVE;

    @Column(name = "last_reward_distribution", nullable = true)
    private LocalDateTime lastRewardDistribution;

    @Column(name = "last_collection_check", nullable = true)
    private LocalDateTime lastCollectionCheck;

    @Column(name = "last_burn_decision", nullable = true)
    private LocalDateTime lastBurnDecision;

    @Column(name = "admin_user_id", nullable = false)
    @NotNull(message = "管理者IDは必須です")
    private UUID adminUserId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ステータス列挙型
    public enum PoolStatus {
        ACTIVE, // アクティブ
        PAUSED, // 一時停止
        MIGRATING, // 移行中
        DEPRECATED // 非推奨
    }

    // ビジネスロジックメソッド

    /**
     * トークンを発行する
     * 
     * @param amount 発行量
     * @return 発行が成功したかどうか
     */
    public boolean issueTokens(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        if (status != PoolStatus.ACTIVE) {
            return false;
        }

        // 最大供給量のチェック
        if (maxSupply != null) {
            BigDecimal newTotalSupply = totalSupply.add(amount);
            if (newTotalSupply.compareTo(maxSupply) > 0) {
                return false;
            }
        }

        // プール配分の計算 (デフォルト配分)
        BigDecimal rewardAllocation = amount.multiply(new BigDecimal("0.40")); // 40%
        BigDecimal governanceAllocation = amount.multiply(new BigDecimal("0.20")); // 20%
        BigDecimal ecosystemAllocation = amount.multiply(new BigDecimal("0.20")); // 20%
        BigDecimal reserveAllocation = amount.multiply(new BigDecimal("0.20")); // 20%

        // プール残高の更新
        this.totalSupply = this.totalSupply.add(amount);
        this.issuedAmount = this.issuedAmount.add(amount);
        this.circulatingSupply = this.circulatingSupply.add(amount);
        this.rewardPool = this.rewardPool.add(rewardAllocation);
        this.governancePool = this.governancePool.add(governanceAllocation);
        this.ecosystemPool = this.ecosystemPool.add(ecosystemAllocation);
        this.reservePool = this.reservePool.add(reserveAllocation);

        return true;
    }

    /**
     * トークンをバーンする
     * 
     * @param amount バーン量
     * @return バーンが成功したかどうか
     */
    public boolean burnTokens(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        if (status != PoolStatus.ACTIVE) {
            return false;
        }

        if (circulatingSupply.compareTo(amount) < 0) {
            return false;
        }

        this.burnedAmount = this.burnedAmount.add(amount);
        this.circulatingSupply = this.circulatingSupply.subtract(amount);
        this.lastBurnDecision = LocalDateTime.now();

        return true;
    }

    /**
     * リワードプールからトークンを配布する
     * 
     * @param amount 配布量
     * @return 配布が成功したかどうか
     */
    public boolean distributeRewards(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        if (status != PoolStatus.ACTIVE) {
            return false;
        }

        if (rewardPool.compareTo(amount) < 0) {
            return false;
        }

        this.rewardPool = this.rewardPool.subtract(amount);
        this.lastRewardDistribution = LocalDateTime.now();

        return true;
    }

    /**
     * 回収対象かどうかを判定する
     * 
     * @param userBalance ユーザーの残高
     * @return 回収対象かどうか
     */
    public boolean isCollectionTarget(BigDecimal userBalance) {
        return userBalance != null &&
                userBalance.compareTo(collectionThreshold) > 0 &&
                status == PoolStatus.ACTIVE;
    }

    /**
     * プール状態の更新
     */
    public void updatePoolStatus() {
        if (circulatingSupply.compareTo(BigDecimal.ZERO) == 0) {
            // 流通量が0の場合は何もしない
            return;
        }

        // 各種チェックのタイムスタンプを更新
        this.lastCollectionCheck = LocalDateTime.now();
    }

    /**
     * プールの健全性チェック
     * 
     * @return プールが健全な状態かどうか
     */
    public boolean isHealthy() {
        // 基本的な整合性チェック
        BigDecimal calculatedCirculating = issuedAmount.subtract(burnedAmount);
        if (circulatingSupply.compareTo(calculatedCirculating) != 0) {
            return false;
        }

        // 総供給量との整合性
        return totalSupply.compareTo(BigDecimal.ZERO) >= 0 &&
                issuedAmount.compareTo(BigDecimal.ZERO) >= 0 &&
                burnedAmount.compareTo(BigDecimal.ZERO) >= 0 &&
                circulatingSupply.compareTo(BigDecimal.ZERO) >= 0;
    }

    /**
     * 発行可能量を計算する
     * 
     * @return 発行可能量
     */
    public BigDecimal getIssuableAmount() {
        if (maxSupply == null) {
            return new BigDecimal("1000000.00000000"); // デフォルト上限
        }
        return maxSupply.subtract(totalSupply).max(BigDecimal.ZERO);
    }
}
