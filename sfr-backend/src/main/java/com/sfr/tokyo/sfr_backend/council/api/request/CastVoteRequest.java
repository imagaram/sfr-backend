package com.sfr.tokyo.sfr_backend.council.api.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CastVoteRequest {
    @NotNull
    private Long candidateId;
}
