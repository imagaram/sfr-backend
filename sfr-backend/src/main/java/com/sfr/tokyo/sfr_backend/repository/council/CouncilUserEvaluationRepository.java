package com.sfr.tokyo.sfr_backend.repository.council;

import com.sfr.tokyo.sfr_backend.entity.council.CouncilUserEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.UUID;

public interface CouncilUserEvaluationRepository extends JpaRepository<CouncilUserEvaluation, Long> {
    boolean existsByCouncilMemberIdAndUserId(UUID councilMemberId, UUID userId);

    @Query("select coalesce(avg(e.score),0) from CouncilUserEvaluation e where e.councilMemberId = ?1")
    double averageScore(UUID councilMemberId);

    long countByCouncilMemberId(UUID councilMemberId);
}
