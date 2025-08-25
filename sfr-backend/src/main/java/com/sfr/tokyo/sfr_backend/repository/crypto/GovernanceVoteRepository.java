package com.sfr.tokyo.sfr_backend.repository.crypto;

import com.sfr.tokyo.sfr_backend.entity.crypto.GovernanceVote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * GovernanceVoteRepository
 * SFR ガバナンス投票のデータアクセス層
 */
@Repository
public interface GovernanceVoteRepository
        extends JpaRepository<GovernanceVote, Long>, JpaSpecificationExecutor<GovernanceVote> {

    // ===== 基本検索メソッド =====
    List<GovernanceVote> findByVoterId(UUID voterId);

    Page<GovernanceVote> findByVoterId(UUID voterId, Pageable pageable);

    List<GovernanceVote> findByProposalId(Long proposalId);

    Page<GovernanceVote> findByProposalId(Long proposalId, Pageable pageable);

    List<GovernanceVote> findByVoteType(GovernanceVote.VoteType voteType);

    // ===== 投票統計メソッド =====
    @Query("SELECT COUNT(gv) FROM GovernanceVote gv WHERE gv.proposalId = :proposalId")
    Long countVotesByProposal(@Param("proposalId") Long proposalId);

    @Query("SELECT gv.voteType, COUNT(gv), SUM(gv.votingPower) FROM GovernanceVote gv WHERE gv.proposalId = :proposalId GROUP BY gv.voteType")
    List<Object[]> getVoteStatsByProposal(@Param("proposalId") Long proposalId);

    @Query("SELECT SUM(gv.votingPower) FROM GovernanceVote gv WHERE gv.proposalId = :proposalId")
    BigDecimal getTotalVotingPowerByProposal(@Param("proposalId") Long proposalId);

    @Query("SELECT COUNT(gv) FROM GovernanceVote gv WHERE gv.voterId = :voterId")
    Long countVotesByUser(@Param("voterId") UUID voterId);

    // ===== 日時ベース検索 =====
    @Query("SELECT gv FROM GovernanceVote gv WHERE gv.votedAt BETWEEN :startDate AND :endDate ORDER BY gv.votedAt DESC")
    Page<GovernanceVote> findByVotedAtBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // ===== 複合条件検索 =====
    @Query("SELECT gv FROM GovernanceVote gv WHERE gv.proposalId = :proposalId AND gv.voteType = :voteType ORDER BY gv.votingPower DESC")
    Page<GovernanceVote> findByProposalAndChoice(@Param("proposalId") Long proposalId,
            @Param("voteType") GovernanceVote.VoteType voteType,
            Pageable pageable);
}
