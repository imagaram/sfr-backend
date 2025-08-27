package com.sfr.tokyo.sfr_backend.council.dto;

import com.sfr.tokyo.sfr_backend.entity.council.CandidateStatus;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouncilCandidateDto {
    private Long id;
    private Long electionId;
    private UUID userId;
    private CandidateStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
