package com.sfr.tokyo.sfr_backend.entity.council;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "council_user_evaluations",
       uniqueConstraints = @UniqueConstraint(name = "uk_user_eval_member_user", columnNames = {"council_member_id","user_id"}))
public class CouncilUserEvaluation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "council_member_id", nullable = false)
    private UUID councilMemberId; // 評価対象評議員

    @Column(name = "user_id", nullable = false)
    private UUID userId; // 評価した一般ユーザー

    @Column(nullable = false)
    private int score; // 0-100

    @Column(length = 300)
    private String comment; // 任意

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist void prePersist(){ this.createdAt = Instant.now(); }
}
