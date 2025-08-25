package com.sfr.tokyo.sfr_backend.repository.crypto;

import com.sfr.tokyo.sfr_backend.entity.crypto.GovernanceProposal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * GovernanceProposalRepository
 * SFR ガバナンス提案のデータアクセス層
 */
@Repository
public interface GovernanceProposalRepository
        extends JpaRepository<GovernanceProposal, Long>, JpaSpecificationExecutor<GovernanceProposal> {

    // ===== 基本検索メソッド =====
    List<GovernanceProposal> findByProposerId(UUID proposerId);

    Page<GovernanceProposal> findByProposerId(UUID proposerId, Pageable pageable);

    List<GovernanceProposal> findBySpaceId(Long spaceId);

    Page<GovernanceProposal> findBySpaceId(Long spaceId, Pageable pageable);

    List<GovernanceProposal> findByProposalType(String proposalType);

    List<GovernanceProposal> findByStatus(String status);

    // ===== 日時ベース検索 =====
    @Query("SELECT gp FROM GovernanceProposal gp WHERE gp.createdAt BETWEEN :startDate AND :endDate ORDER BY gp.createdAt DESC")
    Page<GovernanceProposal> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT gp FROM GovernanceProposal gp WHERE gp.votingEndDate >= :now AND gp.status = 'ACTIVE' ORDER BY gp.votingEndDate ASC")
    List<GovernanceProposal> findActiveProposals(@Param("now") LocalDateTime now);

    // ===== 統計メソッド =====
    @Query("SELECT gp.proposalType, COUNT(gp) FROM GovernanceProposal gp GROUP BY gp.proposalType ORDER BY COUNT(gp) DESC")
    List<Object[]> getProposalTypeStatistics();

    @Query("SELECT gp.status, COUNT(gp) FROM GovernanceProposal gp GROUP BY gp.status ORDER BY COUNT(gp) DESC")
    List<Object[]> getProposalStatusStatistics();

    @Query("SELECT COUNT(gp) FROM GovernanceProposal gp WHERE gp.proposerId = :proposerId")
    Long countProposalsByUser(@Param("proposerId") String proposerId);

    @Query("SELECT COUNT(gp) FROM GovernanceProposal gp WHERE gp.spaceId = :spaceId")
    Long countProposalsBySpace(@Param("spaceId") Long spaceId);

    // ===== 複合条件検索 =====
    @Query("SELECT gp FROM GovernanceProposal gp WHERE gp.spaceId = :spaceId AND gp.proposalType = :proposalType AND gp.status = :status ORDER BY gp.createdAt DESC")
    Page<GovernanceProposal> findBySpaceAndTypeAndStatus(@Param("spaceId") Long spaceId,
            @Param("proposalType") String proposalType,
            @Param("status") String status,
            Pageable pageable);
}
