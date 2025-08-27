package com.sfr.tokyo.sfr_backend.council.dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouncilVoteDto {
    private Long id;
    private Long electionId;
    private Long candidateId;
    private UUID userId; // 投票者
    private Instant createdAt;
}
