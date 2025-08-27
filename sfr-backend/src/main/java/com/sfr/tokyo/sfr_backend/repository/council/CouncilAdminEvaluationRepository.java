package com.sfr.tokyo.sfr_backend.repository.council;

import com.sfr.tokyo.sfr_backend.entity.council.CouncilAdminEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CouncilAdminEvaluationRepository extends JpaRepository<CouncilAdminEvaluation, Long> {
    Optional<CouncilAdminEvaluation> findByCouncilMemberId(UUID councilMemberId);
}
