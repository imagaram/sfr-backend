package com.sfr.tokyo.sfr_backend.repository.council;

import com.sfr.tokyo.sfr_backend.entity.council.CouncilEvaluationItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CouncilEvaluationItemRepository extends JpaRepository<CouncilEvaluationItem, Long> {
    List<CouncilEvaluationItem> findByCouncilMemberIdAndActive(UUID councilMemberId, boolean active);
}
