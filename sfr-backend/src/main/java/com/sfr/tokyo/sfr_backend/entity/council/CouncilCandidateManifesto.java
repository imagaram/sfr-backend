package com.sfr.tokyo.sfr_backend.entity.council;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "council_candidate_manifestos")
public class CouncilCandidateManifesto {

    @Id
    @Column(name = "candidate_id")
    private Long candidateId; // 1:1 主キー (FK)

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private CouncilCandidate candidate;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 500)
    private String summary;

    @Lob
    @Column(columnDefinition = "JSON")
    private String details; // JSON文字列

    @Lob
    @Column(columnDefinition = "JSON")
    private String endorsements;

    @Lob
    @Column(columnDefinition = "JSON")
    private String qa;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    void touch() { this.updatedAt = Instant.now(); }
}
