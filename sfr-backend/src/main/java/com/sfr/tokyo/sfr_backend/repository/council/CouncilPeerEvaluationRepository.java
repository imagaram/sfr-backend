package com.sfr.tokyo.sfr_backend.repository.council;

import com.sfr.tokyo.sfr_backend.entity.council.CouncilPeerEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.UUID;

public interface CouncilPeerEvaluationRepository extends JpaRepository<CouncilPeerEvaluation, Long> {
    boolean existsByCouncilMemberIdAndEvaluatorId(UUID councilMemberId, UUID evaluatorId);

    @Query("select coalesce(avg(p.score),0) from CouncilPeerEvaluation p where p.councilMemberId = ?1")
    double averageScore(UUID councilMemberId);

    long countByCouncilMemberId(UUID councilMemberId);
}
