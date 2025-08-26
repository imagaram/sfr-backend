package com.sfr.tokyo.sfr_backend.mapper;

import com.sfr.tokyo.sfr_backend.dto.CommentDto;
import com.sfr.tokyo.sfr_backend.entity.Comment;
import com.sfr.tokyo.sfr_backend.entity.PostEntity;
import com.sfr.tokyo.sfr_backend.user.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static com.sfr.tokyo.sfr_backend.assertions.CommentAssertions.assertThatPair;

class CommentMapperTest {

	private final CommentMapper commentMapper = Mappers.getMapper(CommentMapper.class);

	@Test
	void toDto_shouldMapEntityToDto() {
		UUID userId = UUID.randomUUID();
		User user = User.builder().id(userId).firstname("Alice").build();
		PostEntity post = PostEntity.builder().id(10L).title("Post").build();
		Comment entity = Comment.builder()
				.id(5L)
				.content("Nice work")
				.post(post)
				.user(user)
				.createdAt(LocalDateTime.now())
				.build();

		CommentDto dto = commentMapper.toDto(entity);

		assertThat(dto).isNotNull();
		assertThatPair(entity, dto).hasSameCoreFields();
		assertThat(dto.getId()).isEqualTo(entity.getId());
		assertThat(dto.getPostId()).isEqualTo(post.getId());
		assertThat(dto.getUserId()).isEqualTo(userId);
		assertThat(dto.getUsername()).isEqualTo(user.getFirstname());
		assertThat(dto.getCreatedAt()).isEqualTo(entity.getCreatedAt());
	}

	@Test
	void toEntity_shouldMapDtoToEntity_withIgnoredFieldsNull() {
		CommentDto dto = CommentDto.builder()
				.content("Comment body")
				.postId(20L)
				.userId(UUID.randomUUID())
				.username("Bob")
				.build();

		Comment entity = commentMapper.toEntity(dto);

		assertThat(entity).isNotNull();
		assertThat(entity.getId()).isNull();
		assertThat(entity.getContent()).isEqualTo(dto.getContent());
		assertThat(entity.getPost()).isNull(); // ignored
		assertThat(entity.getUser()).isNull(); // ignored
		assertThat(entity.getCreatedAt()).isNull(); // ignored
	}

	@Test
	void roundTrip_entityToDtoToEntity_shouldPreserveContentAndResetIgnored() {
		User user = User.builder().id(UUID.randomUUID()).firstname("Carol").build();
		PostEntity post = PostEntity.builder().id(99L).title("P").build();
		Comment original = Comment.builder()
				.id(77L)
				.content("Round trip comment")
				.post(post)
				.user(user)
				.createdAt(LocalDateTime.now())
				.build();

		CommentDto dto = commentMapper.toDto(original);
		Comment mappedBack = commentMapper.toEntity(dto);

		assertThat(mappedBack.getId()).isNull();
		assertThat(mappedBack.getContent()).isEqualTo(original.getContent());
		assertThat(mappedBack.getPost()).isNull();
		assertThat(mappedBack.getUser()).isNull();
	}

	@Test
	void nullHandling_shouldReturnNull() {
		assertThat(commentMapper.toDto(null)).isNull();
		assertThat(commentMapper.toDtoList(null)).isNull();
		assertThat(commentMapper.toEntity(null)).isNull();
	}

	@Test
	void emptyList_shouldReturnEmptyList() {
		List<Comment> empty = Collections.emptyList();
		assertThat(commentMapper.toDtoList(empty)).isEmpty();
	}
}

