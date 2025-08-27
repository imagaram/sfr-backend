package com.sfr.tokyo.sfr_backend.repository.council;

import com.sfr.tokyo.sfr_backend.entity.council.CouncilCandidate;
import com.sfr.tokyo.sfr_backend.entity.council.CouncilElection;
import com.sfr.tokyo.sfr_backend.entity.council.CandidateStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CouncilCandidateRepository extends JpaRepository<CouncilCandidate, Long> {
    List<CouncilCandidate> findByElection(CouncilElection election);
    List<CouncilCandidate> findByElectionAndStatus(CouncilElection election, CandidateStatus status);
    Optional<CouncilCandidate> findByElectionAndUserId(CouncilElection election, java.util.UUID userId);
}
