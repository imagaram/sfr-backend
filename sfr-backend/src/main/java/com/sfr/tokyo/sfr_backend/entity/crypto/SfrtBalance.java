package com.sfr.tokyo.sfr_backend.entity.crypto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * SFRT残高エンティティ (Phase3 基盤復元)
 * 高頻度報酬配布と出金・転送の元台帳。
 */
@Entity
@Table(name = "sfrt_balances", indexes = {
		@Index(name = "idx_sfrt_balances_user_id", columnList = "user_id"),
		@Index(name = "idx_sfrt_balances_space_id", columnList = "space_id"),
		@Index(name = "idx_sfrt_balances_updated_at", columnList = "updated_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SfrtBalance {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false, length = 64)
	@NotBlank
	private String userId;

	@Column(name = "space_id", nullable = false)
	@Builder.Default
	private Long spaceId = 1L;

	@Column(name = "balance", nullable = false, precision = 20, scale = 8)
	@NotNull
	@DecimalMin("0.0")
	@Builder.Default
	private BigDecimal balance = BigDecimal.ZERO;

	@Column(name = "total_earned", nullable = false, precision = 20, scale = 8)
	@Builder.Default
	private BigDecimal totalEarned = BigDecimal.ZERO;

	@Column(name = "total_withdrawn", nullable = false, precision = 20, scale = 8)
	@Builder.Default
	private BigDecimal totalWithdrawn = BigDecimal.ZERO;

	@Column(name = "profit_amount", nullable = false, precision = 20, scale = 8)
	@Builder.Default
	private BigDecimal profitAmount = BigDecimal.ZERO;

	@Version
	private Long version;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	public void addReward(BigDecimal amount) {
		if (amount.signum() <= 0) return;
		balance = balance.add(amount);
		totalEarned = totalEarned.add(amount);
		profitAmount = profitAmount.add(amount);
	}

	public boolean withdraw(BigDecimal amount) {
		if (amount.signum() <= 0) return false;
		if (balance.compareTo(amount) < 0) return false;
		balance = balance.subtract(amount);
		totalWithdrawn = totalWithdrawn.add(amount);
		return true;
	}

	public boolean transferOut(BigDecimal amount) {
		return withdraw(amount);
	}

	public void transferIn(BigDecimal amount) {
		if (amount.signum() <= 0) return;
		balance = balance.add(amount);
	}
}
