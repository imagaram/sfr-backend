package com.sfr.tokyo.sfr_backend.mapper.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningMaterialDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningMaterial;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LearningMaterialMapper {

    LearningMaterialDto toDto(LearningMaterial entity);

    List<LearningMaterialDto> toDtoList(List<LearningMaterial> entities);
}
