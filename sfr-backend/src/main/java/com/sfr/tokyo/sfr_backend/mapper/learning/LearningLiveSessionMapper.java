package com.sfr.tokyo.sfr_backend.mapper.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningLiveSessionDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningLiveSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LearningLiveSessionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    LearningLiveSession toEntity(LearningLiveSessionDto dto);

    LearningLiveSessionDto toDto(LearningLiveSession entity);

    List<LearningLiveSessionDto> toDtoList(List<LearningLiveSession> entities);
}
