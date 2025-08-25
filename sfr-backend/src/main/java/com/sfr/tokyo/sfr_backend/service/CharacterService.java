package com.sfr.tokyo.sfr_backend.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.sfr.tokyo.sfr_backend.dto.CharacterDto;
import com.sfr.tokyo.sfr_backend.entity.CharacterLifecycle;
import com.sfr.tokyo.sfr_backend.entity.CharacterStatus;
import com.sfr.tokyo.sfr_backend.mapper.CharacterMapper;
import com.sfr.tokyo.sfr_backend.repository.CharacterRepository;
import com.sfr.tokyo.sfr_backend.repository.UserRepository;
import com.sfr.tokyo.sfr_backend.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CharacterService {

    private final CharacterRepository characterRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final CharacterMapper characterMapper;

    /**
     * キャラクター作成（DTOベース）
     */
    @Transactional
    public CharacterDto createCharacter(CharacterDto characterDto) {
        // ユーザーを取得
        User user = userRepository.findById(characterDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + characterDto.getUserId()));

        // 名前の重複チェック
        if (characterRepository.findByNameAndUser_Id(characterDto.getName(), characterDto.getUserId()) != null) {
            throw new IllegalArgumentException("Character with this name already exists for the user.");
        }

        // DTOからエンティティに変換（Mapperを使用）
        CharacterLifecycle character = characterMapper.toEntity(characterDto);
        character.setUser(user); // userは別途設定

        // 保存
        CharacterLifecycle savedCharacter = characterRepository.save(character);

        return characterMapper.toDto(savedCharacter);
    }

    /**
     * キャラクターのライフスパンポイントを減少させる
     */
    @Transactional
    public void decreaseLifespanPoints(Long characterId, int points) {
        CharacterLifecycle character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found with ID: " + characterId));

        int newPoints = Math.max(0, character.getLifespanPoints() - points);
        character.setLifespanPoints(newPoints);

        // ライフスパンポイントが0になった場合、ステータスをDECEASEDに変更
        if (newPoints == 0) {
            character.setStatus(CharacterStatus.DECEASED);
        }

        characterRepository.save(character);
    }

    /**
     * キャラクターステータスの更新
     */
    @Transactional
    public CharacterDto updateCharacterStatus(Long characterId, UUID userId, CharacterStatus status) {
        CharacterLifecycle character = characterRepository.findByIdAndUser_Id(characterId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found or access denied"));

        // DECEASEDから他のステータスへの変更は禁止
        if (character.getStatus() == CharacterStatus.DECEASED && status != CharacterStatus.DECEASED) {
            throw new IllegalArgumentException("Cannot revive a deceased character");
        }

        character.setStatus(status);
        CharacterLifecycle updatedCharacter = characterRepository.save(character);

        return characterMapper.toDto(updatedCharacter);
    }

    /**
     * キャラクター作成（従来のマルチパートファイル対応）
     */
    @Transactional
    public CharacterDto createCharacter(UUID userId, String name, String profile, MultipartFile imageFile) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        if (characterRepository.findByNameAndUser_Id(name, userId) != null) {
            throw new IllegalArgumentException("Character with this name already exists for the user.");
        }

        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = fileStorageService.storeFile(imageFile);
            imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/characters/downloadFile/")
                    .path(fileName)
                    .toUriString();
        }

        CharacterLifecycle newCharacter = CharacterLifecycle.builder()
                .name(name)
                .profile(profile)
                .imageUrl(imageUrl)
                .lifespanPoints(365) // 初期値365を設定
                .status(CharacterStatus.ACTIVE) // 初期ステータス
                .user(user)
                .build();

        CharacterLifecycle savedCharacter = characterRepository.save(newCharacter);
        return characterMapper.toDto(savedCharacter);
    }

    /**
     * 自分のキャラクター一覧取得
     */
    @Transactional(readOnly = true)
    public List<CharacterDto> getMyCharacters(UUID userId) {
        List<CharacterLifecycle> characters = characterRepository.findByUser_Id(userId);
        return characters.stream()
                .map(characterMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * キャラクター詳細取得
     */
    @Transactional(readOnly = true)
    public Optional<CharacterDto> getCharacterById(Long id) {
        return characterRepository.findById(id).map(characterMapper::toDto);
    }

    /**
     * キャラクター更新
     */
    @Transactional
    public Optional<CharacterDto> updateCharacter(Long characterId, UUID userId, String name, String profile,
            MultipartFile imageFile) {
        return characterRepository.findByIdAndUser_Id(characterId, userId)
                .map(characterToUpdate -> {
                    // 名前変更時の重複チェック
                    if (!characterToUpdate.getName().equals(name) &&
                            characterRepository.findByNameAndUser_Id(name, userId) != null) {
                        throw new IllegalArgumentException("Character name already exists");
                    }

                    characterToUpdate.setName(name);
                    characterToUpdate.setProfile(profile);

                    if (imageFile != null && !imageFile.isEmpty()) {
                        if (characterToUpdate.getImageUrl() != null) {
                            fileStorageService.deleteFile(characterToUpdate.getImageUrl());
                        }

                        String fileName = fileStorageService.storeFile(imageFile);
                        String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                                .path("/api/characters/downloadFile/")
                                .path(fileName)
                                .toUriString();
                        characterToUpdate.setImageUrl(imageUrl);
                    }

                    CharacterLifecycle updatedCharacter = characterRepository.save(characterToUpdate);
                    return characterMapper.toDto(updatedCharacter);
                });
    }

    /**
     * キャラクター削除（物理削除ではなくDECEASEDステータスに変更）
     */
    @Transactional
    public void deleteCharacter(Long characterId, UUID userId) {
        characterRepository.findByIdAndUser_Id(characterId, userId)
                .ifPresentOrElse(character -> {
                    // 物理削除ではなく、DECEASEDステータスに変更
                    character.setStatus(CharacterStatus.DECEASED);
                    characterRepository.save(character);

                    // 画像ファイルは保持（共同墓地での表示のため）
                }, () -> {
                    throw new IllegalArgumentException("Character not found or access denied");
                });
    }
}
