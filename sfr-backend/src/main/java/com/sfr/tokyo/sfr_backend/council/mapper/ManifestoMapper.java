package com.sfr.tokyo.sfr_backend.council.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sfr.tokyo.sfr_backend.council.dto.ManifestoDto;
import com.sfr.tokyo.sfr_backend.entity.council.CouncilCandidateManifesto;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class ManifestoMapper {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ManifestoDto toDto(CouncilCandidateManifesto entity) {
        if (entity == null) return null;
        return ManifestoDto.builder()
                .candidateId(entity.getCandidateId())
                .title(entity.getTitle())
                .summary(entity.getSummary())
                .details(readList(entity.getDetails()))
                .endorsements(readList(entity.getEndorsements()))
                .qa(readQa(entity.getQa()))
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public void updateEntity(CouncilCandidateManifesto entity, ManifestoDto dto) {
        entity.setTitle(dto.getTitle());
        entity.setSummary(dto.getSummary());
        entity.setDetails(writeList(dto.getDetails()));
        entity.setEndorsements(writeList(dto.getEndorsements()));
        entity.setQa(writeQa(dto.getQa()));
    }

    private List<String> readList(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try { return objectMapper.readValue(json, new TypeReference<>(){}); } catch (Exception e) { return Collections.emptyList(); }
    }

    private String writeList(List<String> list) {
        if (list == null) return null;
        try { return objectMapper.writeValueAsString(list); } catch (JsonProcessingException e) { throw new com.sfr.tokyo.sfr_backend.exception.BusinessException(
            com.sfr.tokyo.sfr_backend.exception.ErrorCode.BAD_REQUEST, "Invalid list"); }
    }

    private List<ManifestoDto.QA> readQa(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try { return objectMapper.readValue(json, new TypeReference<>(){}); } catch (Exception e) { return Collections.emptyList(); }
    }

    private String writeQa(List<ManifestoDto.QA> qa) {
        if (qa == null) return null;
        try { return objectMapper.writeValueAsString(qa); } catch (JsonProcessingException e) { throw new com.sfr.tokyo.sfr_backend.exception.BusinessException(
            com.sfr.tokyo.sfr_backend.exception.ErrorCode.BAD_REQUEST, "Invalid qa"); }
    }
}
