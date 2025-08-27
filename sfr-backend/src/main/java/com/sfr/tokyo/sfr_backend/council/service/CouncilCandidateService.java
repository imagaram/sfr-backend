package com.sfr.tokyo.sfr_backend.council.service;

import com.sfr.tokyo.sfr_backend.council.dto.CouncilCandidateDto;
import com.sfr.tokyo.sfr_backend.council.mapper.CouncilCandidateMapper;
import com.sfr.tokyo.sfr_backend.entity.council.*;
import com.sfr.tokyo.sfr_backend.repository.council.CouncilCandidateRepository;
import com.sfr.tokyo.sfr_backend.repository.council.CouncilElectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.sfr.tokyo.sfr_backend.exception.BusinessException;
import com.sfr.tokyo.sfr_backend.exception.ErrorCode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouncilCandidateService {

    private final CouncilCandidateRepository candidateRepository;
    private final CouncilElectionRepository electionRepository;
    private final CouncilCandidateMapper candidateMapper;

    public List<CouncilCandidateDto> listByElection(Long electionId) {
        CouncilElection election = electionRepository.getReferenceById(electionId);
        return candidateMapper.toDtoList(candidateRepository.findByElection(election));
    }

    public Optional<CouncilCandidateDto> findByElectionAndUser(Long electionId, UUID userId) {
        CouncilElection election = electionRepository.getReferenceById(electionId);
        return candidateRepository.findByElectionAndUserId(election, userId).map(candidateMapper::toDto);
    }

    @Transactional
    public CouncilCandidateDto registerCandidate(Long electionId, UUID userId) {
    CouncilElection election = electionRepository.findById(electionId)
        .orElseThrow(() -> new BusinessException(ErrorCode.ELECTION_NOT_FOUND, "Election not found: id=" + electionId));
        // 既存チェック
    candidateRepository.findByElectionAndUserId(election, userId).ifPresent(c -> { throw new com.sfr.tokyo.sfr_backend.exception.BusinessException(
        com.sfr.tokyo.sfr_backend.exception.ErrorCode.DUPLICATE_CANDIDATE, "Candidate already registered"); });
        CouncilCandidate entity = CouncilCandidate.builder()
                .election(election)
                .user(com.sfr.tokyo.sfr_backend.user.User.builder().id(userId).build())
                .status(CandidateStatus.ACTIVE)
                .build();
        return candidateMapper.toDto(candidateRepository.save(entity));
    }

    @Transactional
    public boolean updateStatus(Long candidateId, CandidateStatus status) {
        return candidateRepository.findById(candidateId).map(c -> { c.setStatus(status); return true; }).orElse(false);
    }
}
