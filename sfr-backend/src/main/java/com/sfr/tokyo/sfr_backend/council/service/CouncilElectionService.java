package com.sfr.tokyo.sfr_backend.council.service;

import com.sfr.tokyo.sfr_backend.council.dto.CouncilElectionDto;
import com.sfr.tokyo.sfr_backend.council.mapper.CouncilElectionMapper;
import com.sfr.tokyo.sfr_backend.entity.council.CouncilElection;
import com.sfr.tokyo.sfr_backend.entity.council.ElectionPhase;
import com.sfr.tokyo.sfr_backend.repository.council.CouncilElectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouncilElectionService {

    private final CouncilElectionRepository electionRepository;
    private final CouncilElectionMapper electionMapper;

    public Optional<CouncilElectionDto> getCurrentElection() {
        return electionRepository.findTopByOrderByStartAtDesc().map(electionMapper::toDto);
    }

    public Optional<CouncilElectionDto> getCurrentPhase(ElectionPhase phase) {
        return electionRepository.findFirstByPhaseOrderByStartAtDesc(phase).map(electionMapper::toDto);
    }

    public List<CouncilElectionDto> listAll() { return electionMapper.toDtoList(electionRepository.findAll()); }

    @Transactional
    public CouncilElectionDto create(CouncilElectionDto dto) {
        CouncilElection entity = electionMapper.toEntity(dto);
        // 簡易バリデーション
        if (entity.getStartAt() == null || entity.getEndAt() == null || entity.getEndAt().isBefore(entity.getStartAt())) {
            throw new com.sfr.tokyo.sfr_backend.exception.BusinessException(
                com.sfr.tokyo.sfr_backend.exception.ErrorCode.ELECTION_TIME_WINDOW,
                "Invalid election time range");
        }
        if (entity.getPhase() == null) entity.setPhase(ElectionPhase.PRE_ELECTION);
        CouncilElection saved = electionRepository.save(entity);
        return electionMapper.toDto(saved);
    }

    @Transactional
    public Optional<CouncilElectionDto> update(Long id, CouncilElectionDto patch) {
        return electionRepository.findById(id).map(entity -> {
            electionMapper.update(entity, patch);
            CouncilElection saved = electionRepository.save(entity);
            return electionMapper.toDto(saved);
        });
    }

    public Optional<CouncilElectionDto> findById(Long id) {
        return electionRepository.findById(id).map(electionMapper::toDto);
    }

    @Transactional
    public boolean advancePhase(Long id, ElectionPhase next) {
        return electionRepository.findById(id).map(e -> {
            e.setPhase(next);
            electionRepository.save(e);
            return true;
        }).orElse(false);
    }

    public boolean isElectionOpenForVoting(CouncilElectionDto election) {
        Instant now = Instant.now();
        return election.getPhase() == ElectionPhase.VOTING && !now.isBefore(election.getStartAt()) && !now.isAfter(election.getEndAt());
    }
}
