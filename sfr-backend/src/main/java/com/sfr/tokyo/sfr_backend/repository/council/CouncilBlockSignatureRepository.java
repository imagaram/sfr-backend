package com.sfr.tokyo.sfr_backend.repository.council;

import com.sfr.tokyo.sfr_backend.entity.council.CouncilBlockSignature;
import com.sfr.tokyo.sfr_backend.entity.council.CouncilBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CouncilBlockSignatureRepository extends JpaRepository<CouncilBlockSignature, Long> {
    List<CouncilBlockSignature> findByBlock(CouncilBlock block);
    Optional<CouncilBlockSignature> findByBlockAndCouncilMemberId(CouncilBlock block, UUID councilMemberId);
}
