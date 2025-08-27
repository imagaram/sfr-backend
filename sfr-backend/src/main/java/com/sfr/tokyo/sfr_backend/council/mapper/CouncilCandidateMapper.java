package com.sfr.tokyo.sfr_backend.council.mapper;

import com.sfr.tokyo.sfr_backend.council.dto.CouncilCandidateDto;
import com.sfr.tokyo.sfr_backend.entity.council.CouncilCandidate;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CouncilCandidateMapper {
    @Mapping(source = "election.id", target = "electionId")
    @Mapping(source = "user.id", target = "userId")
    CouncilCandidateDto toDto(CouncilCandidate entity);
    List<CouncilCandidateDto> toDtoList(List<CouncilCandidate> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "election", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CouncilCandidate toEntity(CouncilCandidateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "election", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(@MappingTarget CouncilCandidate entity, CouncilCandidateDto dto);
}
