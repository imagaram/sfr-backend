package com.sfr.tokyo.sfr_backend.council.api.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class CreateElectionRequest {
    @NotNull
    private Instant startAt;
    @NotNull @Future
    private Instant endAt;
    @Min(1)
    private int seats;
}
