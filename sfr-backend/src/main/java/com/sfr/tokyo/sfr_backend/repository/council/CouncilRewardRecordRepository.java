package com.sfr.tokyo.sfr_backend.repository.council;

import com.sfr.tokyo.sfr_backend.entity.council.CouncilRewardRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CouncilRewardRecordRepository extends JpaRepository<CouncilRewardRecord, Long> {
    Optional<CouncilRewardRecord> findByCouncilMemberId(UUID councilMemberId);
}
