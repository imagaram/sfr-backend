package com.sfr.tokyo.sfr_backend.repository.council;

import com.sfr.tokyo.sfr_backend.entity.council.CouncilUserEvaluationScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CouncilUserEvaluationScoreRepository extends JpaRepository<CouncilUserEvaluationScore, Long> {
    List<CouncilUserEvaluationScore> findByCouncilMemberId(UUID councilMemberId);
    Optional<CouncilUserEvaluationScore> findByCouncilMemberIdAndUserIdAndItemId(UUID councilMemberId, UUID userId, Long itemId);
}
