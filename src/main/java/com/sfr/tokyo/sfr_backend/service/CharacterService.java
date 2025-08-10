package com.sfr.tokyo.sfr_backend.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.sfr.tokyo.sfr_backend.dto.CharacterDto;
import com.sfr.tokyo.sfr_backend.entity.Character;
import com.sfr.tokyo.sfr_backend.repository.CharacterRepository;
import com.sfr.tokyo.sfr_backend.repository.UserRepository;
import com.sfr.tokyo.sfr_backend.user.User;

import lombok.RequiredArgsConstructor;

// キャラクター関連のビジネスロジックを実装するサービス
@Service
@RequiredArgsConstructor
public class CharacterService {

    private final CharacterRepository characterRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    // CharacterエンティティをCharacterDtoに変換するヘルパーメソッド
    private CharacterDto convertToDto(Character character) {
        return CharacterDto.builder()
                .id(character.getId())
                .name(character.getName())
                .profile(character.getProfile())
                .imageUrl(character.getImageUrl())
                // ユーザーIDもDTOに含めることで、所有者情報をクライアントに伝える
                .userId(character.getUser().getId()) 
                .build();
    }

    // 新しいキャラクターを作成
    public CharacterDto createCharacter(Long userId, String name, String profile, MultipartFile imageFile) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        // ユーザーごとにキャラクター名の一意性をチェック
        if (characterRepository.findByNameAndUserId(name, userId) != null) {
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

        Character newCharacter = Character.builder()
                .name(name)
                .profile(profile)
                .imageUrl(imageUrl)
                .user(user)
                .build();

        Character savedCharacter = characterRepository.save(newCharacter);
        return convertToDto(savedCharacter);
    }

    // 認証ユーザーIDに基づいてキャラクターリストを取得
    public List<CharacterDto> getMyCharacters(Long userId) {
        List<Character> characters = characterRepository.findByUserId(userId);
        return characters.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 特定のIDを持つキャラクターを取得
    public Optional<CharacterDto> getCharacterById(Long id) {
        return characterRepository.findById(id).map(this::convertToDto);
    }

    // キャラクター情報を更新
    public Optional<CharacterDto> updateCharacter(Long characterId, Long userId, String name, String profile, MultipartFile imageFile) {
        // findByIdAndUserIdを使用することで、ユーザーが所有するキャラクターのみを安全に更新
        return characterRepository.findByIdAndUserId(characterId, userId)
                .map(characterToUpdate -> {
                    characterToUpdate.setName(name);
                    characterToUpdate.setProfile(profile);

                    if (imageFile != null && !imageFile.isEmpty()) {
                        // 古い画像を削除するロジックを追加
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
                    
                    Character updatedCharacter = characterRepository.save(characterToUpdate);
                    return convertToDto(updatedCharacter);
                });
    }

    // キャラクターを削除
    public void deleteCharacter(Long characterId, Long userId) {
        // findByIdAndUserIdを使用することで、ユーザーが所有するキャラクターのみを安全に削除
        characterRepository.findByIdAndUserId(characterId, userId)
                .ifPresent(character -> {
                    // 関連する画像ファイルも削除する
                    if (character.getImageUrl() != null) {
                        fileStorageService.deleteFile(character.getImageUrl());
                    }
                    characterRepository.delete(character);
                });
    }
}
