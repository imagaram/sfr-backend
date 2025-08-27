package com.sfr.tokyo.sfr_backend.council.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateResultDto {
    private Long candidateId;
    private long voteCount;
    private int rank; // 1-based
}
