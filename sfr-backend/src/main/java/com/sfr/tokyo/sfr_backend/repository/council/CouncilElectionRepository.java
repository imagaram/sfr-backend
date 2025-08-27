package com.sfr.tokyo.sfr_backend.repository.council;

import com.sfr.tokyo.sfr_backend.entity.council.CouncilElection;
import com.sfr.tokyo.sfr_backend.entity.council.ElectionPhase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouncilElectionRepository extends JpaRepository<CouncilElection, Long> {
    Optional<CouncilElection> findFirstByPhaseOrderByStartAtDesc(ElectionPhase phase);
    Optional<CouncilElection> findTopByOrderByStartAtDesc();
}
