package com.sfr.tokyo.sfr_backend.entity.council;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "council_peer_evaluations",
        uniqueConstraints = @UniqueConstraint(name = "uk_peer_eval_target_evaluator", columnNames = {"council_member_id","evaluator_id"}))
public class CouncilPeerEvaluation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "council_member_id", nullable = false)
    private UUID councilMemberId; // 評価対象評議員

    @Column(name = "evaluator_id", nullable = false)
    private UUID evaluatorId; // 評価した同僚評議員

    @Column(nullable = false)
    private int score; // 0-100

    @Column(length = 300)
    private String comment; // 寸評

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist void prePersist(){ this.createdAt = Instant.now(); }
}
