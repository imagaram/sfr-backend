package com.sfr.tokyo.sfr_backend.entity.council;

import com.sfr.tokyo.sfr_backend.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "council_candidates", uniqueConstraints = @UniqueConstraint(columnNames = {"election_id", "user_id"}))
public class CouncilCandidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "election_id")
    private CouncilElection election;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CandidateStatus status; // ACTIVE, WITHDRAWN, DISQUALIFIED

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() { this.createdAt = this.updatedAt = Instant.now(); if (status == null) status = CandidateStatus.ACTIVE; }

    @PreUpdate
    void preUpdate() { this.updatedAt = Instant.now(); }
}
