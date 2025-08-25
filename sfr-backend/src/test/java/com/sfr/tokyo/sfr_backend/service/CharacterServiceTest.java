package com.sfr.tokyo.sfr_backend.service;

import com.sfr.tokyo.sfr_backend.dto.CharacterDto;
import com.sfr.tokyo.sfr_backend.entity.CharacterLifecycle;
import com.sfr.tokyo.sfr_backend.entity.CharacterStatus;
import com.sfr.tokyo.sfr_backend.mapper.CharacterMapper;
import com.sfr.tokyo.sfr_backend.repository.CharacterRepository;
import com.sfr.tokyo.sfr_backend.repository.UserRepository;
import com.sfr.tokyo.sfr_backend.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CharacterServiceTest {

    @Mock
    private CharacterRepository characterRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CharacterMapper characterMapper;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private CharacterService characterService;

    private User user;
    private CharacterLifecycle character;
    private CharacterDto characterDto;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder().id(userId).build();

        character = CharacterLifecycle.builder()
                .id(1L)
                .name("Test Character")
                .user(user)
                .status(CharacterStatus.ACTIVE)
                .lifespanPoints(100)
                .build();

        characterDto = CharacterDto.builder()
                .id(1L)
                .name("Test Character")
                .userId(userId)
                .status(CharacterStatus.ACTIVE)
                .lifespanPoints(100)
                .build();
    }

    @Test
    void createCharacter_shouldReturnSavedCharacterDto() {
        // given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(characterMapper.toEntity(any(CharacterDto.class))).thenReturn(character);
        when(characterRepository.save(any(CharacterLifecycle.class))).thenReturn(character);
        when(characterMapper.toDto(any(CharacterLifecycle.class))).thenReturn(characterDto);

        // when
        CharacterDto createdDto = characterService.createCharacter(characterDto);

        // then
        assertThat(createdDto).isNotNull();
        assertThat(createdDto.getName()).isEqualTo("Test Character");
        assertThat(createdDto.getUserId()).isEqualTo(userId);
    }

    @Test
    void decreaseLifespanPoints_shouldDecreasePoints() {
        // given
        when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
        character.setLifespanPoints(100);

        // when
        characterService.decreaseLifespanPoints(1L, 10);

        // then
        ArgumentCaptor<CharacterLifecycle> captor = ArgumentCaptor.forClass(CharacterLifecycle.class);
        verify(characterRepository).save(captor.capture());
        CharacterLifecycle saved = captor.getValue();
        assertThat(saved.getLifespanPoints()).isEqualTo(90);
        assertThat(saved.getStatus()).isEqualTo(CharacterStatus.ACTIVE);
    }

    @Test
    void decreaseLifespanPoints_shouldUpdateStatusToDeceasedWhenPointsReachZero() {
        // given
        when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
        character.setLifespanPoints(10);

        // when
        characterService.decreaseLifespanPoints(1L, 10);

        // then
        ArgumentCaptor<CharacterLifecycle> captor = ArgumentCaptor.forClass(CharacterLifecycle.class);
        verify(characterRepository).save(captor.capture());
        CharacterLifecycle saved = captor.getValue();
        assertThat(saved.getLifespanPoints()).isZero();
        assertThat(saved.getStatus()).isEqualTo(CharacterStatus.DECEASED);
    }

    @Test
    void decreaseLifespanPoints_shouldThrowExceptionWhenCharacterNotFound() {
        // given
        when(characterRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> characterService.decreaseLifespanPoints(1L, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Character not found with ID: 1");
    }

    @Test
    void decreaseLifespanPoints_whenAlreadyZero_shouldRemainZeroAndDeceased() {
        // given
        when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
        character.setLifespanPoints(0);
        character.setStatus(CharacterStatus.DECEASED);

        // when
        characterService.decreaseLifespanPoints(1L, 10);

        // then
        ArgumentCaptor<CharacterLifecycle> captor = ArgumentCaptor.forClass(CharacterLifecycle.class);
        verify(characterRepository).save(captor.capture());
        CharacterLifecycle saved = captor.getValue();
        assertThat(saved.getLifespanPoints()).isZero();
        assertThat(saved.getStatus()).isEqualTo(CharacterStatus.DECEASED);
    }

    @Test
    void updateCharacterStatus_shouldSetToDeceased() {
        // given
        when(characterRepository.findByIdAndUser_Id(1L, userId)).thenReturn(Optional.of(character));
        ArgumentCaptor<CharacterLifecycle> captor = ArgumentCaptor.forClass(CharacterLifecycle.class);

        CharacterDto deceasedDto = CharacterDto.builder()
                .id(1L)
                .name("Test Character")
                .userId(userId)
                .status(CharacterStatus.DECEASED)
                .lifespanPoints(character.getLifespanPoints())
                .build();

        when(characterRepository.save(any(CharacterLifecycle.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(characterMapper.toDto(any(CharacterLifecycle.class))).thenReturn(deceasedDto);

        // when
        CharacterDto result = characterService.updateCharacterStatus(1L, userId, CharacterStatus.DECEASED);

        // then
        verify(characterRepository).save(captor.capture());
        CharacterLifecycle saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(CharacterStatus.DECEASED);
        assertThat(result.getStatus()).isEqualTo(CharacterStatus.DECEASED);
    }

    @Test
    void updateCharacterStatus_shouldThrowWhenReviveAttempt() {
        // given
        character.setStatus(CharacterStatus.DECEASED);
        when(characterRepository.findByIdAndUser_Id(1L, userId)).thenReturn(Optional.of(character));

        // when & then
        assertThatThrownBy(() -> characterService.updateCharacterStatus(1L, userId, CharacterStatus.ACTIVE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot revive a deceased character");
    }

    @Test
    void deleteCharacter_shouldSetStatusToDeceasedWhenFound() {
        // given
        character.setStatus(CharacterStatus.ACTIVE);
        when(characterRepository.findByIdAndUser_Id(1L, userId)).thenReturn(Optional.of(character));
        when(characterRepository.save(any(CharacterLifecycle.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        characterService.deleteCharacter(1L, userId);

        // then
        ArgumentCaptor<CharacterLifecycle> captor = ArgumentCaptor.forClass(CharacterLifecycle.class);
        verify(characterRepository).save(captor.capture());
        CharacterLifecycle saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(CharacterStatus.DECEASED);
    }

    @Test
    void deleteCharacter_shouldThrowWhenNotFoundOrAccessDenied() {
        // given
        when(characterRepository.findByIdAndUser_Id(1L, userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> characterService.deleteCharacter(1L, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Character not found or access denied");
    }

    @Test
    void updateCharacter_shouldUpdateNameAndProfileWithoutImage() {
        // given
        when(characterRepository.findByIdAndUser_Id(1L, userId)).thenReturn(Optional.of(character));
        when(characterRepository.findByNameAndUser_Id("New Name", userId)).thenReturn(null);
        when(characterRepository.save(any(CharacterLifecycle.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CharacterDto updatedDto = CharacterDto.builder()
                .id(1L)
                .name("New Name")
                .userId(userId)
                .profile("new profile")
                .status(character.getStatus())
                .lifespanPoints(character.getLifespanPoints())
                .build();
        when(characterMapper.toDto(any(CharacterLifecycle.class))).thenReturn(updatedDto);

        // when
        Optional<CharacterDto> result = characterService.updateCharacter(1L, userId, "New Name", "new profile", null);

        // then
        assertThat(result).isPresent();
        CharacterDto res = result.get();
        assertThat(res.getName()).isEqualTo("New Name");
        assertThat(res.getProfile()).isEqualTo("new profile");
    }

    @Test
    void updateCharacter_shouldReplaceImageWhenProvided() {
        // given
        character.setImageUrl("oldfile.jpg");
        when(characterRepository.findByIdAndUser_Id(1L, userId)).thenReturn(Optional.of(character));

        MultipartFile multipartFile = org.mockito.Mockito.mock(MultipartFile.class);
        when(multipartFile.isEmpty()).thenReturn(false);
        when(fileStorageService.storeFile(multipartFile)).thenReturn("newfile.png");

        when(characterRepository.save(any(CharacterLifecycle.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CharacterDto dtoWithImage = CharacterDto.builder()
                .id(1L)
                .name(character.getName())
                .userId(userId)
                .profile(character.getProfile())
                .status(character.getStatus())
                .lifespanPoints(character.getLifespanPoints())
                .build();
        when(characterMapper.toDto(any(CharacterLifecycle.class))).thenReturn(dtoWithImage);

        // when
        // set up request context so
        // ServletUriComponentsBuilder.fromCurrentContextPath() works
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));

        // when
        Optional<CharacterDto> result = characterService.updateCharacter(1L, userId, character.getName(),
                character.getProfile(), multipartFile);

        // then
        assertThat(result).isPresent();
        ArgumentCaptor<CharacterLifecycle> captor = ArgumentCaptor.forClass(CharacterLifecycle.class);
        verify(characterRepository).save(captor.capture());
        CharacterLifecycle saved = captor.getValue();
        assertThat(saved.getImageUrl()).contains("newfile.png");
        verify(fileStorageService).deleteFile("oldfile.jpg");
        verify(fileStorageService).storeFile(multipartFile);
    }

    @Test
    void updateCharacter_shouldThrowWhenNameDuplicate() {
        // given
        when(characterRepository.findByIdAndUser_Id(1L, userId)).thenReturn(Optional.of(character));
        // Simulate another character exists with the same new name
        CharacterLifecycle other = CharacterLifecycle.builder().id(2L).name("Existing").user(user).build();
        when(characterRepository.findByNameAndUser_Id("Existing", userId)).thenReturn(other);

        // when & then
        assertThatThrownBy(() -> characterService.updateCharacter(1L, userId, "Existing", "profile", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Character name already exists");
    }
}
