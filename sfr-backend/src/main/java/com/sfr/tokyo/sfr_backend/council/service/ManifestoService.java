package com.sfr.tokyo.sfr_backend.council.service;

import com.sfr.tokyo.sfr_backend.council.dto.ManifestoDto;
import com.sfr.tokyo.sfr_backend.council.mapper.ManifestoMapper;
import com.sfr.tokyo.sfr_backend.entity.council.CouncilCandidate;
import com.sfr.tokyo.sfr_backend.entity.council.CouncilCandidateManifesto;
import com.sfr.tokyo.sfr_backend.entity.council.ElectionPhase;
import com.sfr.tokyo.sfr_backend.repository.council.CouncilCandidateManifestoRepository;
import com.sfr.tokyo.sfr_backend.repository.council.CouncilCandidateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.sfr.tokyo.sfr_backend.exception.BusinessException;
import com.sfr.tokyo.sfr_backend.exception.ErrorCode;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManifestoService {

    private final CouncilCandidateRepository candidateRepository;
    private final CouncilCandidateManifestoRepository manifestoRepository;
    private final ManifestoMapper manifestoMapper;

    public ManifestoDto get(Long candidateId) {
        return manifestoRepository.findById(candidateId).map(manifestoMapper::toDto).orElse(null);
    }

    @Transactional
    public ManifestoDto upsert(Long candidateId, ManifestoDto dto) {
        CouncilCandidate candidate = candidateRepository.findById(candidateId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CANDIDATE_NOT_FOUND, "Candidate not found: id=" + candidateId));
        // フェーズチェック (PRE_ELECTION のみ編集可)
    if (candidate.getElection().getPhase() != ElectionPhase.PRE_ELECTION) {
        throw new com.sfr.tokyo.sfr_backend.exception.BusinessException(
            com.sfr.tokyo.sfr_backend.exception.ErrorCode.MANIFESTO_EDIT_CLOSED,
            "Manifesto editing closed");
    }
    // 新規作成時は candidateId を明示設定しない (@MapsId により persist 時にコピー) —
    // 先にIDを埋めると Hibernate が既存行と誤認し UPDATE を発行し StaleObjectState になる
    CouncilCandidateManifesto entity = manifestoRepository.findById(candidateId)
    .orElseGet(() -> CouncilCandidateManifesto.builder().candidate(candidate).build());
        manifestoMapper.updateEntity(entity, dto);
        return manifestoMapper.toDto(manifestoRepository.save(entity));
    }

    @Transactional
    public ManifestoDto addQA(Long candidateId, ManifestoDto.QA qa) {
    CouncilCandidateManifesto entity = manifestoRepository.findById(candidateId)
        .orElseThrow(() -> new BusinessException(ErrorCode.MANIFESTO_NOT_FOUND, "Manifesto not found: candidateId=" + candidateId));
    if (entity.getCandidate().getElection().getPhase() != ElectionPhase.PRE_ELECTION) {
        throw new com.sfr.tokyo.sfr_backend.exception.BusinessException(
            com.sfr.tokyo.sfr_backend.exception.ErrorCode.MANIFESTO_QA_CLOSED,
            "Q&A adding closed");
    }
        ManifestoDto current = manifestoMapper.toDto(entity);
        java.util.ArrayList<ManifestoDto.QA> list = new java.util.ArrayList<>(current.getQa());
        list.add(qa);
        current.setQa(list);
        manifestoMapper.updateEntity(entity, current);
        return manifestoMapper.toDto(manifestoRepository.save(entity));
    }
}
