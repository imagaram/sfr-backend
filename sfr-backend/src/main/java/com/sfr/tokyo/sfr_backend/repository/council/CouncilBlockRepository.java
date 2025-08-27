package com.sfr.tokyo.sfr_backend.repository.council;

import com.sfr.tokyo.sfr_backend.entity.council.CouncilBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouncilBlockRepository extends JpaRepository<CouncilBlock, Long> {
    Optional<CouncilBlock> findByBlockIndex(Long blockIndex);
}
