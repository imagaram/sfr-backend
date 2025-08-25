package com.sfr.tokyo.sfr_backend.repository.learning;

import com.sfr.tokyo.sfr_backend.entity.learning.LearningSpace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningSpaceRepository extends JpaRepository<LearningSpace, Long> {

    List<LearningSpace> findByMode(LearningSpace.LearningMode mode);

    Optional<LearningSpace> findByName(String name);

    boolean existsByName(String name);
}
