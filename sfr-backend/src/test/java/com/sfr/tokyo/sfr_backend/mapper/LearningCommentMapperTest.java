package com.sfr.tokyo.sfr_backend.mapper;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningCommentDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningComment;
import com.sfr.tokyo.sfr_backend.mapper.learning.LearningCommentMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static com.sfr.tokyo.sfr_backend.assertions.LearningCommentAssertions.assertThatPair;

class LearningCommentMapperTest {

    private final LearningCommentMapper commentMapper = Mappers.getMapper(LearningCommentMapper.class);

    @Test
    void toDto_shouldMapLearningCommentToLearningCommentDto() {
        // given
        UUID authorId = UUID.randomUUID();
        Long topicId = 10L;
        LocalDateTime now = LocalDateTime.now();

        LearningComment comment = new LearningComment();
        comment.setId(1L);
        comment.setTopicId(topicId);
        comment.setAuthorId(authorId);
        comment.setParentCommentId(null);
        comment.setContent("This is a test learning comment.");
        comment.setCommentType(LearningComment.CommentType.COMMENT);
        comment.setCommentStatus(LearningComment.CommentStatus.ACTIVE);
        comment.setLikeCount(5);
        comment.setReplyCount(2);
        comment.setQualityScore(BigDecimal.valueOf(4.5));
        comment.setIsBestAnswer(false);
        comment.setIsPinned(false);
        comment.setIsSolution(false);
        comment.setCreatedAt(now);
        comment.setUpdatedAt(now);

        // when
        LearningCommentDto dto = commentMapper.toDto(comment);

        // then
    assertThat(dto).isNotNull();
    assertThatPair(comment, dto).hasSameCoreFields();
    assertThat(dto.getId()).isEqualTo(comment.getId());
    assertThat(dto.getParentCommentId()).isEqualTo(comment.getParentCommentId());
    assertThat(dto.getLikeCount()).isEqualTo(comment.getLikeCount());
    assertThat(dto.getReplyCount()).isEqualTo(comment.getReplyCount());
    assertThat(dto.getQualityScore()).isEqualTo(comment.getQualityScore());
    assertThat(dto.getIsBestAnswer()).isEqualTo(comment.getIsBestAnswer());
    assertThat(dto.getIsPinned()).isEqualTo(comment.getIsPinned());
    assertThat(dto.getIsSolution()).isEqualTo(comment.getIsSolution());
    assertThat(dto.getCreatedAt()).isEqualTo(comment.getCreatedAt());
    assertThat(dto.getUpdatedAt()).isEqualTo(comment.getUpdatedAt());
    }

    @Test
    void toEntity_shouldMapLearningCommentDtoToLearningComment() {
        // given
        UUID authorId = UUID.randomUUID();
        Long topicId = 20L;

        LearningCommentDto dto = new LearningCommentDto();
        dto.setTopicId(topicId);
        dto.setAuthorId(authorId);
        dto.setContent("New learning comment content");
        dto.setCommentType(LearningComment.CommentType.QUESTION);
        dto.setCommentStatus(LearningComment.CommentStatus.PENDING);

        // when
        LearningComment entity = commentMapper.toEntity(dto);

        // then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull(); // IDは無視される
        assertThat(entity.getTopicId()).isEqualTo(dto.getTopicId());
        assertThat(entity.getAuthorId()).isEqualTo(dto.getAuthorId());
        assertThat(entity.getContent()).isEqualTo(dto.getContent());
        assertThat(entity.getCommentType()).isEqualTo(dto.getCommentType());
        assertThat(entity.getCommentStatus()).isEqualTo(dto.getCommentStatus());
        // 以下のフィールドは無視される（デフォルト値になる）
        assertThat(entity.getLikeCount()).isEqualTo(0);
        assertThat(entity.getReplyCount()).isEqualTo(0);
        assertThat(entity.getQualityScore()).isEqualTo(BigDecimal.ZERO);
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getUpdatedAt()).isNull();
    }

    @Test
    void nullHandling_shouldReturnNull() {
        assertThat(commentMapper.toDto(null)).isNull();
        assertThat(commentMapper.toEntity(null)).isNull();
        assertThat(commentMapper.toDtoList(null)).isNull();
        assertThat(commentMapper.toEntityList(null)).isNull();
    }

    @Test
    void emptyList_shouldReturnEmptyList() {
        assertThat(commentMapper.toDtoList(Collections.emptyList())).isEmpty();
        assertThat(commentMapper.toEntityList(Collections.emptyList())).isEmpty();
    }

    @Test
    void roundTrip_listMappingConsistency() {
        UUID authorId = UUID.randomUUID();
        LearningComment c1 = new LearningComment();
        c1.setTopicId(1L); c1.setAuthorId(authorId); c1.setContent("c1"); c1.setCommentType(LearningComment.CommentType.COMMENT); c1.setCommentStatus(LearningComment.CommentStatus.ACTIVE);
        LearningComment c2 = new LearningComment();
        c2.setTopicId(2L); c2.setAuthorId(authorId); c2.setContent("c2"); c2.setCommentType(LearningComment.CommentType.QUESTION); c2.setCommentStatus(LearningComment.CommentStatus.PENDING);

        List<LearningComment> entities = List.of(c1, c2);
        List<LearningCommentDto> dtos = commentMapper.toDtoList(entities);
        List<LearningComment> mappedBack = commentMapper.toEntityList(dtos);

        assertThat(dtos).hasSize(2);
        assertThat(mappedBack).hasSize(2);
        assertThat(mappedBack.get(0).getContent()).isEqualTo("c1");
        assertThat(mappedBack.get(1).getContent()).isEqualTo("c2");
    }
}
