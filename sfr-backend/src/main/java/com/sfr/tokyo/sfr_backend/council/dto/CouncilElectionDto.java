package com.sfr.tokyo.sfr_backend.council.dto;

import com.sfr.tokyo.sfr_backend.entity.council.ElectionPhase;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouncilElectionDto {
    private Long id;
    private ElectionPhase phase;
    private Instant startAt;
    private Instant endAt;
    private int seats;
    private Instant createdAt;
    private Instant updatedAt;
}
