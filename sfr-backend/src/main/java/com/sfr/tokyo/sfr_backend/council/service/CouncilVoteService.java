package com.sfr.tokyo.sfr_backend.council.service;

import com.sfr.tokyo.sfr_backend.council.dto.CouncilVoteDto;
import com.sfr.tokyo.sfr_backend.council.mapper.CouncilVoteMapper;
import com.sfr.tokyo.sfr_backend.entity.council.*;
import com.sfr.tokyo.sfr_backend.repository.council.CouncilCandidateRepository;
import com.sfr.tokyo.sfr_backend.repository.council.CouncilElectionRepository;
import com.sfr.tokyo.sfr_backend.repository.council.CouncilVoteRepository;
import com.sfr.tokyo.sfr_backend.repository.council.CouncilVoteRepository.CandidateVoteCount;
import com.sfr.tokyo.sfr_backend.council.dto.CandidateResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import com.sfr.tokyo.sfr_backend.exception.BusinessException;
import com.sfr.tokyo.sfr_backend.exception.ErrorCode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouncilVoteService {

    private final CouncilVoteRepository voteRepository;
    private final CouncilElectionRepository electionRepository;
    private final CouncilCandidateRepository candidateRepository;
    private final CouncilVoteMapper voteMapper;
    private final VotingEligibilityService votingEligibilityService;

    @Transactional
    public CouncilVoteDto castVote(Long electionId, Long candidateId, UUID userId) {
    CouncilElection election = electionRepository.findById(electionId)
        .orElseThrow(() -> new BusinessException(ErrorCode.ELECTION_NOT_FOUND, "Election not found: id=" + electionId));
    CouncilCandidate candidate = candidateRepository.findById(candidateId)
        .orElseThrow(() -> new BusinessException(ErrorCode.CANDIDATE_NOT_FOUND, "Candidate not found: id=" + candidateId));
        // 同一選挙内チェック
        if (!candidate.getElection().getId().equals(election.getId())) {
            throw new com.sfr.tokyo.sfr_backend.exception.BusinessException(
                com.sfr.tokyo.sfr_backend.exception.ErrorCode.BAD_REQUEST,
                "Candidate not in election");
        }
        // フェーズ & 期間チェック
    if (election.getPhase() != ElectionPhase.VOTING) {
        throw new com.sfr.tokyo.sfr_backend.exception.BusinessException(
            com.sfr.tokyo.sfr_backend.exception.ErrorCode.ELECTION_PHASE_INVALID,
            "Voting not active");
    }
        java.time.Instant now = java.time.Instant.now();
    if (now.isBefore(election.getStartAt()) || now.isAfter(election.getEndAt())) {
        throw new com.sfr.tokyo.sfr_backend.exception.BusinessException(
            com.sfr.tokyo.sfr_backend.exception.ErrorCode.ELECTION_TIME_WINDOW,
            "Voting not active");
    }
        // 投票資格判定 (残高 / アクティビティ)
        var eligibility = votingEligibilityService.evaluate(userId);
        if (!eligibility.balanceOk()) {
            throw new com.sfr.tokyo.sfr_backend.exception.BusinessException(
                com.sfr.tokyo.sfr_backend.exception.ErrorCode.VOTER_INSUFFICIENT_BALANCE,
                "Insufficient SFR balance to vote");
        }
        if (!eligibility.activityOk()) {
            throw new com.sfr.tokyo.sfr_backend.exception.BusinessException(
                com.sfr.tokyo.sfr_backend.exception.ErrorCode.VOTER_INSUFFICIENT_ACTIVITY,
                "Insufficient activity score to vote");
        }

        // 二重投票チェック
    voteRepository.findByElectionIdAndUserId(electionId, userId).ifPresent(v -> { throw new com.sfr.tokyo.sfr_backend.exception.BusinessException(
        com.sfr.tokyo.sfr_backend.exception.ErrorCode.DUPLICATE_VOTE, "Already voted"); });
        CouncilVote vote = CouncilVote.builder()
                .election(election)
                .candidate(candidate)
                .user(com.sfr.tokyo.sfr_backend.user.User.builder().id(userId).build())
                .build();
        return voteMapper.toDto(voteRepository.save(vote));
    }

    public java.util.List<CandidateResultDto> getResults(Long electionId) {
        CouncilElection election = electionRepository.findById(electionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ELECTION_NOT_FOUND, "Election not found: id=" + electionId));
    if (election.getPhase() != ElectionPhase.COUNTING && election.getPhase() != ElectionPhase.POST_ELECTION) {
        throw new com.sfr.tokyo.sfr_backend.exception.BusinessException(
            com.sfr.tokyo.sfr_backend.exception.ErrorCode.RESULTS_UNAVAILABLE,
            "Results not available");
    }
        java.util.List<CandidateVoteCount> rows = voteRepository.aggregateByElection(electionId);
        java.util.List<CandidateResultDto> result = new java.util.ArrayList<>();
        long prevCount = -1;
        int rank = 0;
        int index = 0;
        for (CandidateVoteCount r : rows) {
            index++;
            if (r.getCount() != prevCount) {
                rank = index; // standard competition ranking
                prevCount = r.getCount();
            }
            result.add(CandidateResultDto.builder()
                    .candidateId(r.getCandidateId())
                    .voteCount(r.getCount())
                    .rank(rank)
                    .build());
        }
        return result;
    }
}
