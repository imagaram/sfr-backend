package com.sfr.tokyo.sfr_backend.mapper;

import com.sfr.tokyo.sfr_backend.dto.UserDto;
import com.sfr.tokyo.sfr_backend.user.Role;
import com.sfr.tokyo.sfr_backend.user.Status;
import com.sfr.tokyo.sfr_backend.user.User;
import com.sfr.tokyo.sfr_backend.user.UserState;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static com.sfr.tokyo.sfr_backend.assertions.MapperAssertions.assertThatPair;

class UserMapperTest {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Test
    void toDto_shouldMapUserToUserDto() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .firstname("Taro")
                .lastname("Yamada")
                .email("taro@example.com")
                .status(Status.ARTIST)
                .role(Role.USER)
                .state(UserState.ACTIVE)
                .idVerified(true)
                .myNumberVerified(false)
                .build();

    UserDto dto = userMapper.toDto(user);

    assertThat(dto).isNotNull();
    assertThatPair(user, dto).hasSameCoreFields();
    assertThat(dto.getRole()).isEqualTo(Role.USER);
    assertThat(dto.isIdVerified()).isTrue();
    assertThat(dto.isMyNumberVerified()).isFalse();
    }

    @Test
    void toEntity_shouldMapUserDtoToUser() {
        UserDto dto = UserDto.builder()
                .firstname("Hanako")
                .lastname("Suzuki")
                .email("hanako@example.com")
                .role(Role.ADMIN)
                .idVerified(false)
                .myNumberVerified(true)
                .build();

        User user = userMapper.toEntity(dto);

        assertThat(user).isNotNull();
        assertThat(user.getId()).isNull(); // ignore id
        assertThat(user.getFirstname()).isEqualTo("Hanako");
        assertThat(user.getLastname()).isEqualTo("Suzuki");
        assertThat(user.getEmail()).isEqualTo("hanako@example.com");
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
        // password/status/state ignored
        assertThat(user.getPassword()).isNull();
        assertThat(user.getStatus()).isNull();
        assertThat(user.getState()).isNull();
    }

    @Test
    void updateEntityFromDto_shouldUpdateAllowedFieldsOnly() {
        User existing = User.builder()
                .id(UUID.randomUUID())
                .firstname("Old")
                .lastname("Name")
                .email("old@example.com")
                .password("secret")
                .status(Status.FAN)
                .role(Role.USER)
                .state(UserState.ACTIVE)
                .idVerified(false)
                .myNumberVerified(false)
                .build();

        UserDto dto = UserDto.builder()
                .firstname("New")
                .lastname("Name")
                .email("new@example.com") // should be ignored
                .role(Role.ADMIN) // ignored
                .idVerified(true)
                .myNumberVerified(true)
                .build();

        userMapper.updateEntityFromDto(dto, existing);

        assertThat(existing.getFirstname()).isEqualTo("New");
        assertThat(existing.getLastname()).isEqualTo("Name");
        assertThat(existing.getEmail()).isEqualTo("old@example.com"); // unchanged
        assertThat(existing.getRole()).isEqualTo(Role.USER); // unchanged
    assertThat(existing.getStatus()).isEqualTo(Status.FAN); // unchanged
        assertThat(existing.getState()).isEqualTo(UserState.ACTIVE); // unchanged
        assertThat(existing.isIdVerified()).isTrue();
        assertThat(existing.isMyNumberVerified()).isTrue();
    }

    @Test
    void toDtoList_shouldMapAll() {
        List<User> users = List.of(
                User.builder().id(UUID.randomUUID()).firstname("A").lastname("B").email("a@example.com").role(Role.USER).build(),
                User.builder().id(UUID.randomUUID()).firstname("C").lastname("D").email("c@example.com").role(Role.ADMIN).build()
        );
        List<UserDto> dtos = userMapper.toDtoList(users);
        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getFirstname()).isEqualTo("A");
        assertThat(dtos.get(1).getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void roundTrip_shouldPreserveBasicFields() {
        User original = User.builder()
                .id(UUID.randomUUID())
                .firstname("Rt")
                .lastname("Test")
                .email("rt@example.com")
                .role(Role.USER)
                .idVerified(true)
                .myNumberVerified(false)
                .build();
    UserDto dto = userMapper.toDto(original);
    User mapped = userMapper.toEntity(dto);
    assertThatPair(mapped, dto).hasSameCoreFields();
    assertThat(mapped.getRole()).isEqualTo(original.getRole());
    }

    @Test
    void nullHandling_shouldReturnNull() {
        assertThat(userMapper.toDto(null)).isNull();
        assertThat(userMapper.toEntity(null)).isNull();
    }

    @Test
    void emptyList_shouldReturnEmptyList() {
        assertThat(userMapper.toDtoList(List.of())).isEmpty();
    }
}
