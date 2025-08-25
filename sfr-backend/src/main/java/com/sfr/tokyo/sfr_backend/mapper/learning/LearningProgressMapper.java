package com.sfr.tokyo.sfr_backend.mapper.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningProgressDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningProgress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LearningProgressMapper {

    /**
     * Entity → DTO変換（レスポンス用）
     */
    @Mappings({
            @Mapping(source = "learningContent.id", target = "contentId"),
            @Mapping(source = "learningContent.title", target = "contentTitle"),
            @Mapping(source = "user.id", target = "userId")
    })
    LearningProgressDto toDto(LearningProgress entity);

    /**
     * Entity List → DTO List変換
     */
    List<LearningProgressDto> toDtoList(List<LearningProgress> entities);

    /**
     * DTO → Entity変換（作成用）
     * Note: userとlearningContentはServiceで設定
     */
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "user", ignore = true),
            @Mapping(target = "learningContent", ignore = true),
            @Mapping(target = "completedAt", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true)
    })
    LearningProgress toEntity(LearningProgressDto dto);
}
