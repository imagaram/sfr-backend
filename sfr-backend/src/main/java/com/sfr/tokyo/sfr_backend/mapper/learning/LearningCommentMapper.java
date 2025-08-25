package com.sfr.tokyo.sfr_backend.mapper.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningCommentDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningComment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * LearningComment エンティティと DTO のマッピング
 */
@Mapper(componentModel = "spring")
public interface LearningCommentMapper {

    LearningCommentMapper INSTANCE = Mappers.getMapper(LearningCommentMapper.class);

    /**
     * エンティティからDTOに変換
     */
    LearningCommentDto toDto(LearningComment learningComment);

    /**
     * DTOからエンティティに変換
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "likeCount", ignore = true)
    @Mapping(target = "dislikeCount", ignore = true)
    @Mapping(target = "replyCount", ignore = true)
    @Mapping(target = "reportCount", ignore = true)
    @Mapping(target = "isBestAnswer", ignore = true)
    @Mapping(target = "isSolution", ignore = true)
    @Mapping(target = "isPinned", ignore = true)
    @Mapping(target = "isFeatured", ignore = true)
    @Mapping(target = "isHighlighted", ignore = true)
    @Mapping(target = "requiresModeration", ignore = true)
    @Mapping(target = "moderationStatus", ignore = true)
    @Mapping(target = "moderatorId", ignore = true)
    @Mapping(target = "moderationNotes", ignore = true)
    @Mapping(target = "moderatedAt", ignore = true)
    @Mapping(target = "qualityScore", ignore = true)
    @Mapping(target = "helpfulnessScore", ignore = true)
    @Mapping(target = "relevanceScore", ignore = true)
    @Mapping(target = "overallScore", ignore = true)
    @Mapping(target = "selectedAsBestAt", ignore = true)
    @Mapping(target = "selectedAsBestBy", ignore = true)
    @Mapping(target = "bestAnswerPoints", ignore = true)
    @Mapping(target = "depthLevel", ignore = true)
    @Mapping(target = "threadPosition", ignore = true)
    @Mapping(target = "lastEditedAt", ignore = true)
    @Mapping(target = "lastEditedBy", ignore = true)
    @Mapping(target = "editReason", ignore = true)
    @Mapping(target = "editCount", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "mentions", ignore = true)
    @Mapping(target = "metadata", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    LearningComment toEntity(LearningCommentDto learningCommentDto);
}
