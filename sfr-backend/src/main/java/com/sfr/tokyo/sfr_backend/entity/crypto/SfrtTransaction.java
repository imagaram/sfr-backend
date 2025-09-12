package com.sfr.tokyo.sfr_backend.entity.crypto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "sfrt_transactions", indexes = {
		@Index(name = "idx_sfrt_transactions_user_id", columnList = "user_id"),
		@Index(name = "idx_sfrt_transactions_type", columnList = "type"),
		@Index(name = "idx_sfrt_transactions_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SfrtTransaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false, length = 64)
	private String userId;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 32)
	private SfrtTransactionType type;

	@Column(name = "amount", nullable = false, precision = 20, scale = 8)
	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 16)
	@Builder.Default
	private SfrtTransactionStatus status = SfrtTransactionStatus.COMPLETED;

	@Column(name = "related_sfr_transaction_id")
	private Long relatedSfrTransactionId;

	@Column(name = "description", length = 255)
	private String description;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
}
