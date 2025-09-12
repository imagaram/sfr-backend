package com.sfr.tokyo.sfr_backend.exchange;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ComplianceStatus {
    boolean apiHealthy;
    boolean tradingAllowed;
    boolean withdrawalAllowed;
    String jurisdictionNote;
    String lastCheckedIso;
}
