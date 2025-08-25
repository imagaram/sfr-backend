package com.sfr.tokyo.sfr_backend.mapper.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningContentCreateDto;
import com.sfr.tokyo.sfr_backend.dto.learning.LearningContentDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningContent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = { LearningSectionMapper.class })
public interface LearningContentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "sections", ignore = true)
    LearningContent toEntity(LearningContentCreateDto dto);

    LearningContentDto toDto(LearningContent entity);

    List<LearningContentDto> toDtoList(List<LearningContent> entities);
}
