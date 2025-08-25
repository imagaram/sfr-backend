package com.sfr.tokyo.sfr_backend.mapper.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningSectionDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningSection;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = { LearningMaterialMapper.class })
public interface LearningSectionMapper {

    LearningSectionDto toDto(LearningSection entity);

    List<LearningSectionDto> toDtoList(List<LearningSection> entities);
}
