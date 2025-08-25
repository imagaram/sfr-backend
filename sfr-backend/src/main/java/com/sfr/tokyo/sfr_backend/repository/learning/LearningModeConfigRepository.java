package com.sfr.tokyo.sfr_backend.repository.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningModeConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LearningModeConfigRepository extends JpaRepository<LearningModeConfig, Long> {

    Optional<LearningModeConfig> findBySpaceId(Long spaceId);

    void deleteBySpaceId(Long spaceId);
}
