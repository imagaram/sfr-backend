package com.sfr.tokyo.sfr_backend.entity.council;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "council_admin_evaluations",
        uniqueConstraints = @UniqueConstraint(name = "uk_admin_eval_member", columnNames = {"council_member_id"}))
public class CouncilAdminEvaluation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "council_member_id", nullable = false)
    private UUID councilMemberId; // 評価対象評議員

    @Column(nullable = false)
    private int score; // 0-100

    @Column(length = 300)
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist void prePersist(){ this.createdAt = Instant.now(); }
}
