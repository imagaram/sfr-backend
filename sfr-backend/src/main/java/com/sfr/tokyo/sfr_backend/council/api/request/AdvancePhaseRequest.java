package com.sfr.tokyo.sfr_backend.council.api.request;

import com.sfr.tokyo.sfr_backend.entity.council.ElectionPhase;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdvancePhaseRequest {
    @NotNull
    private ElectionPhase phase;
}
