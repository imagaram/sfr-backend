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
@Table(name = "council_blocks")
public class CouncilBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "block_index", nullable = false, unique = true)
    private Long blockIndex;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "previous_hash", length = 128)
    private String previousHash;

    @Column(name = "hash", nullable = false, length = 128)
    private String hash;

    @Column(name = "validator_id", length = 64)
    private String validatorId;

    @Column(name = "comment_merkle_root", length = 128)
    private String commentMerkleRoot;

    @Lob
    @Column(name = "reward_record_ids", columnDefinition = "JSON")
    private String rewardRecordIds; // JSON array

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() { this.createdAt = Instant.now(); if (timestamp == null) timestamp = Instant.now(); }
}
