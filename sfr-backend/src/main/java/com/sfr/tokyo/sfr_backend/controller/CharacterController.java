package com.sfr.tokyo.sfr_backend.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.sfr.tokyo.sfr_backend.dto.CharacterDto;
import com.sfr.tokyo.sfr_backend.entity.CharacterLifecycle;
import com.sfr.tokyo.sfr_backend.repository.CharacterRepository;
import com.sfr.tokyo.sfr_backend.service.ImageUploadService;
import com.sfr.tokyo.sfr_backend.user.User;

import lombok.RequiredArgsConstructor;

// キャラクター関連のREST APIエンドポイントを定義するコントローラー
@RestController
@RequestMapping("/api/characters")
@RequiredArgsConstructor
public class CharacterController {

    private final CharacterRepository characterRepository;
    private final ImageUploadService imageUploadService; // 画像アップロードサービスをインジェクション

    /**
     * DTOからエンティティに変換する際の、userIdを含んだDTOを生成するヘルパーメソッド。
     * 
     * @param character 変換元のCharacterエンティティ
     * @return 変換されたCharacterDto
     */
    private CharacterDto convertToDto(CharacterLifecycle character) {
        CharacterDto dto = new CharacterDto();
        dto.setId(character.getId());
        dto.setName(character.getName());
        dto.setProfile(character.getProfile());
        dto.setImageUrl(character.getImageUrl());
        dto.setUserId(character.getUser().getId());
        return dto;
    }

    /**
     * 新しいキャラクターを作成します。
     * 
     * @param name           キャラクターの名前
     * @param profile        キャラクターのプロフィール
     * @param imageFile      アップロードされる画像ファイル
     * @param authentication 現在認証されているユーザー情報
     * @return 作成されたキャラクター情報とHTTPステータス (201 Created or 409 Conflict)
     */
    @PostMapping
    public ResponseEntity<CharacterDto> createCharacter(
            @RequestParam("name") String name,
            @RequestParam("profile") String profile,
            @RequestParam("imageFile") MultipartFile imageFile,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        // 既に同じ名前のキャラクターが存在するかチェック
        if (characterRepository.findByNameAndUser_Id(name, currentUser.getId()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }

        String imageUrl;
        try {
            // 画像アップロードサービスを使用して画像をアップロード
            imageUrl = imageUploadService.uploadImage(imageFile);
        } catch (Exception e) {
            // アップロードに失敗した場合、HTTP 500エラーを返す
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image", e);
        }

        // DTOからエンティティに変換
        CharacterLifecycle newCharacter = CharacterLifecycle.builder()
                .name(name)
                .profile(profile)
                .imageUrl(imageUrl)
                .user(currentUser)
                .build();

        CharacterLifecycle savedCharacter = characterRepository.save(newCharacter);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(savedCharacter));
    }

    /**
     * 認証ユーザーに紐づくすべてのキャラクターを取得します。
     * 
     * @param authentication 現在認証されているユーザー情報
     * @return ユーザーのキャラクターリスト
     */
    @GetMapping
    public ResponseEntity<List<CharacterDto>> getAllCharacters(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        List<CharacterLifecycle> characters = characterRepository.findByUser_Id(currentUser.getId());

        List<CharacterDto> characterDtos = characters.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(characterDtos);
    }

    /**
     * 特定のキャラクターをIDで取得します。
     * 
     * @param id             取得するキャラクターのID
     * @param authentication 現在認証されているユーザー情報
     * @return 取得したキャラクター情報 (200 OK or 404 Not Found)
     */
    @GetMapping("/{id}")
    public ResponseEntity<CharacterDto> getCharacterById(@PathVariable Long id, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        return characterRepository.findById(id)
                .filter(character -> character.getUser().getId().equals(currentUser.getId()))
                .map(character -> ResponseEntity.ok(convertToDto(character)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 特定のキャラクターを更新します。
     * 
     * @param id             更新するキャラクターのID
     * @param characterDto   更新情報
     * @param authentication 現在認証されているユーザー情報
     * @return 更新されたキャラクター情報 (200 OK or 404 Not Found or 403 Forbidden or 409
     *         Conflict)
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCharacter(@PathVariable Long id, @RequestParam("name") String name,
            @RequestParam("profile") String profile,
            @RequestParam("imageFile") MultipartFile imageFile,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        Optional<CharacterLifecycle> characterOptional = characterRepository.findById(id);

        if (characterOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        CharacterLifecycle existingCharacter = characterOptional.get();

        // ユーザーがキャラクターの所有者であることを確認
        if (!existingCharacter.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }

        // キャラクター名が変更された場合、名前の重複をチェック
        if (!existingCharacter.getName().equals(name) &&
                characterRepository.findByNameAndUser_Id(name, currentUser.getId()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Character name already exists");
        }

        String imageUrl;
        try {
            // 画像アップロードサービスを使用して画像をアップロード
            imageUrl = imageUploadService.uploadImage(imageFile);
        } catch (Exception e) {
            // アップロードに失敗した場合、HTTP 500エラーを返す
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image", e);
        }

        // 更新処理
        existingCharacter.setName(name);
        existingCharacter.setProfile(profile);
        existingCharacter.setImageUrl(imageUrl);
        CharacterLifecycle updatedCharacter = characterRepository.save(existingCharacter);

        return ResponseEntity.ok(convertToDto(updatedCharacter));
    }

    /**
     * 特定のキャラクターを削除します。
     * 
     * @param id             削除するキャラクターのID
     * @param authentication 現在認証されているユーザー情報
     * @return HTTPステータス (204 No Content or 404 Not Found)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCharacter(@PathVariable Long id, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        return characterRepository.findById(id)
                .filter(character -> character.getUser().getId().equals(currentUser.getId()))
                .map(character -> {
                    characterRepository.delete(character);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().<Void>build());
    }
}
