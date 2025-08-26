package com.sfr.tokyo.sfr_backend.mapper;

import com.sfr.tokyo.sfr_backend.dto.CharacterDto;
import com.sfr.tokyo.sfr_backend.entity.CharacterLifecycle;
import com.sfr.tokyo.sfr_backend.entity.CharacterStatus;
import com.sfr.tokyo.sfr_backend.user.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static com.sfr.tokyo.sfr_backend.assertions.CharacterAssertions.assertThatPair;

class CharacterMapperTest {

    private final CharacterMapper characterMapper = Mappers.getMapper(CharacterMapper.class);

    @Test
    void toDto_shouldMapCharacterLifecycleToCharacterDto() {
        // given
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        CharacterLifecycle character = CharacterLifecycle.builder()
                .id(1L)
                .name("Test Character")
                .profile("Test Profile")
                .imageUrl("http://example.com/image.png")
                .lifespanPoints(100)
                .status(CharacterStatus.ACTIVE)
                .user(user)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // when
        CharacterDto dto = characterMapper.toDto(character);

    // then
    assertThat(dto).isNotNull();
    assertThatPair(character, dto).hasSameCoreFields();
    assertThat(dto.getId()).isEqualTo(character.getId());
    assertThat(dto.getUserId()).isEqualTo(userId);
    assertThat(dto.getCreatedAt()).isEqualTo(character.getCreatedAt());
    assertThat(dto.getUpdatedAt()).isEqualTo(character.getUpdatedAt());
    }

    @Test
    void toEntity_shouldMapCharacterDtoToCharacterLifecycle() {
        // given
        CharacterDto dto = CharacterDto.builder()
                .name("Test DTO")
                .profile("DTO Profile")
                .imageUrl("http://example.com/dto.png")
                .lifespanPoints(50)
                .status(CharacterStatus.DECEASED)
                .build();

        // when
        CharacterLifecycle entity = characterMapper.toEntity(dto);

        // then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull(); // IDはマッピングされないはず
        assertThat(entity.getName()).isEqualTo(dto.getName());
        assertThat(entity.getProfile()).isEqualTo(dto.getProfile());
        assertThat(entity.getImageUrl()).isEqualTo(dto.getImageUrl());
        assertThat(entity.getLifespanPoints()).isEqualTo(dto.getLifespanPoints());
        assertThat(entity.getStatus()).isEqualTo(dto.getStatus());
        assertThat(entity.getUser()).isNull(); // Userはマッピングされないはず
    }

    @Test
    void updateEntityFromDto_shouldUpdateCharacterLifecycleFromCharacterDto() {
        // given
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        CharacterLifecycle character = CharacterLifecycle.builder()
                .id(1L)
                .name("Original Name")
                .profile("Original Profile")
                .user(user)
                .status(CharacterStatus.ACTIVE)
                .build();

        CharacterDto dto = CharacterDto.builder()
                .name("Updated Name")
                .profile("Updated Profile")
                .imageUrl("http://example.com/updated.png")
                .build();

        // when
        characterMapper.updateEntityFromDto(dto, character);

        // then
        assertThat(character.getId()).isEqualTo(1L); // IDは変更されない
        assertThat(character.getName()).isEqualTo("Updated Name");
        assertThat(character.getProfile()).isEqualTo("Updated Profile");
        assertThat(character.getImageUrl()).isEqualTo("http://example.com/updated.png");
        assertThat(character.getUser().getId()).isEqualTo(userId); // Userは変更されない
        assertThat(character.getStatus()).isEqualTo(CharacterStatus.ACTIVE); // マッピング対象外のフィールドは変更されない
    }

    @Test
    void roundTrip_shouldPreserveCoreFields() {
        User user = User.builder().id(UUID.randomUUID()).build();
        CharacterLifecycle original = CharacterLifecycle.builder()
                .id(5L)
                .name("Round")
                .profile("Trip")
                .imageUrl("http://ex.com/r.png")
                .lifespanPoints(300)
                .status(CharacterStatus.ACTIVE)
                .user(user)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        CharacterDto dto = characterMapper.toDto(original);
        CharacterLifecycle mapped = characterMapper.toEntity(dto); // user / timestamps ignored

    assertThatPair(mapped, dto).hasSameCoreFields();
    }

    @Test
    void nullHandling_shouldReturnNull() {
        assertThat(characterMapper.toDto(null)).isNull();
        assertThat(characterMapper.toEntity(null)).isNull();
        CharacterLifecycle entity = CharacterLifecycle.builder().name("Keep").build();
        characterMapper.updateEntityFromDto(null, entity); // no change
        assertThat(entity.getName()).isEqualTo("Keep");
    }

    @Test
    void emptyList_shouldReturnEmptyList() {
        assertThat(characterMapper.toDtoList(List.of())).isEmpty();
    }
}
