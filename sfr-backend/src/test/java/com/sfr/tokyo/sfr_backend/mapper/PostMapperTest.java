package com.sfr.tokyo.sfr_backend.mapper;

import com.sfr.tokyo.sfr_backend.dto.PostDto;
import com.sfr.tokyo.sfr_backend.entity.PostEntity;
import com.sfr.tokyo.sfr_backend.user.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PostMapperTest {

    private final PostMapper postMapper = Mappers.getMapper(PostMapper.class);

    @Test
    void toDto_shouldMapPostEntityToPostDto() {
        // given
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        PostEntity post = PostEntity.builder()
                .id(1L)
                .title("Test Post")
                .description("Test Description")
                .fileUrl("http://example.com/file.zip")
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        // when
        PostDto dto = postMapper.toDto(post);

        // then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(post.getId());
        assertThat(dto.getTitle()).isEqualTo(post.getTitle());
        assertThat(dto.getDescription()).isEqualTo(post.getDescription());
        assertThat(dto.getFileUrl()).isEqualTo(post.getFileUrl());
        assertThat(dto.getUserId()).isEqualTo(userId);
    }

    @Test
    void toEntity_shouldMapPostDtoToPostEntity() {
        // given
        PostDto dto = PostDto.builder()
                .title("DTO Title")
                .description("DTO Description")
                .fileUrl("http://example.com/dto_file.zip")
                .build();

        // when
        PostEntity entity = postMapper.toEntity(dto);

        // then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull(); // IDは無視される
        assertThat(entity.getTitle()).isEqualTo(dto.getTitle());
        assertThat(entity.getDescription()).isEqualTo(dto.getDescription());
        assertThat(entity.getFileUrl()).isEqualTo(dto.getFileUrl());
        assertThat(entity.getUser()).isNull(); // Userは無視される
    }

    @Test
    void updateEntityFromDto_shouldUpdatePostEntityFromPostDto() {
        // given
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        PostEntity post = PostEntity.builder()
                .id(1L)
                .title("Original Title")
                .description("Original Description")
                .user(user)
                .build();

        PostDto dto = PostDto.builder()
                .title("Updated Title")
                .description("Updated Description")
                .fileUrl("http://example.com/updated.zip")
                .build();

        // when
        postMapper.updateEntityFromDto(dto, post);

        // then
        assertThat(post.getId()).isEqualTo(1L); // IDは変更されない
        assertThat(post.getTitle()).isEqualTo("Updated Title");
        assertThat(post.getDescription()).isEqualTo("Updated Description");
        assertThat(post.getFileUrl()).isEqualTo("http://example.com/updated.zip");
        assertThat(post.getUser().getId()).isEqualTo(userId); // Userは変更されない
    }
}
