package com.sfr.tokyo.sfr_backend.entity.council;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "council_reward_records", uniqueConstraints = @UniqueConstraint(columnNames = {"council_member_id"}))
public class CouncilRewardRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "council_member_id", nullable = false)
    private UUID councilMemberId;

    @Column(name = "base_reward_sfr", precision = 20, scale = 10, nullable = false)
    private BigDecimal baseRewardSfr;

    @Column(name = "final_reward_sfr", precision = 20, scale = 10, nullable = false)
    private BigDecimal finalRewardSfr;

    @Column(name = "user_score_avg", precision = 5, scale = 2)
    private BigDecimal userScoreAvg;

    @Column(name = "peer_score_avg", precision = 5, scale = 2)
    private BigDecimal peerScoreAvg;

    @Column(name = "admin_score", precision = 5, scale = 2)
    private BigDecimal adminScore;

    @Column(name = "weighted_score", precision = 5, scale = 2)
    private BigDecimal weightedScore;

    @Column(name = "comment_hash", length = 128)
    private String commentHash;

    @Column(nullable = false)
    @Builder.Default
    private boolean finalized = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() { this.createdAt = this.updatedAt = Instant.now(); }

    @PreUpdate
    void preUpdate() { this.updatedAt = Instant.now(); }
}
