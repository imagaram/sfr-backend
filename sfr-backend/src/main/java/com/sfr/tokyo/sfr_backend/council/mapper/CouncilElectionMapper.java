package com.sfr.tokyo.sfr_backend.council.mapper;

import com.sfr.tokyo.sfr_backend.council.dto.CouncilElectionDto;
import com.sfr.tokyo.sfr_backend.entity.council.CouncilElection;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CouncilElectionMapper {

    CouncilElectionDto toDto(CouncilElection entity);
    List<CouncilElectionDto> toDtoList(List<CouncilElection> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CouncilElection toEntity(CouncilElectionDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(@MappingTarget CouncilElection entity, CouncilElectionDto dto);
}
