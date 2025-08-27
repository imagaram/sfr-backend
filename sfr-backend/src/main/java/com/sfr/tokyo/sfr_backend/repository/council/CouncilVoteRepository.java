package com.sfr.tokyo.sfr_backend.repository.council;

import com.sfr.tokyo.sfr_backend.entity.council.CouncilVote;
import com.sfr.tokyo.sfr_backend.entity.council.CouncilElection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CouncilVoteRepository extends JpaRepository<CouncilVote, Long> {
    Optional<CouncilVote> findByElectionAndUserId(CouncilElection election, UUID userId);
    Optional<CouncilVote> findByElectionIdAndUserId(Long electionId, UUID userId);
    long countByCandidateId(Long candidateId);
    List<CouncilVote> findByElection(CouncilElection election);

    interface CandidateVoteCount {
        Long getCandidateId();
        long getCount();
    }

    @org.springframework.data.jpa.repository.Query("SELECT v.candidate.id as candidateId, COUNT(v.id) as count FROM CouncilVote v WHERE v.election.id = :electionId GROUP BY v.candidate.id ORDER BY count DESC")
    List<CandidateVoteCount> aggregateByElection(@org.springframework.data.repository.query.Param("electionId") Long electionId);
}
