package com.sfr.tokyo.sfr_backend.entity.council;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "council_block_signatures", uniqueConstraints = @UniqueConstraint(columnNames = {"block_id", "council_member_id"}))
public class CouncilBlockSignature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "block_id")
    private CouncilBlock block;

    @Column(name = "council_member_id", nullable = false)
    private UUID councilMemberId;

    @Lob
    @Column(nullable = false)
    private String signature;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() { this.createdAt = Instant.now(); }
}
