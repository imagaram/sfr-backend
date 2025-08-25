package com.sfr.tokyo.sfr_backend.controller;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sfr.tokyo.sfr_backend.dto.UserDto;
import com.sfr.tokyo.sfr_backend.repository.UserRepository;
import com.sfr.tokyo.sfr_backend.user.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// ユーザー情報に関連するREST APIエンドポイントを定義するコントローラー
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    /**
     * DTOからエンティティに変換する際の、UserDtoを生成するヘルパーメソッド。
     * 
     * @param user 変換元のUserエンティティ
     * @return 変換されたUserDto
     */
    private UserDto convertToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .role(user.getRole())
                .idVerified(user.isIdVerified())
                .myNumberVerified(user.isMyNumberVerified())
                .build();
    }

    /**
     * 認証ユーザー自身の基本情報を取得します。
     * 
     * @param authentication 現在認証されているユーザー情報
     * @return ユーザー基本情報とHTTPステータス (200 OK or 404 Not Found)
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto> getAuthenticatedUser(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        return userRepository.findById(currentUser.getId())
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * 認証ユーザー自身の基本情報を更新します。
     * 
     * @param userDto        更新するユーザー情報
     * @param authentication 現在認証されているユーザー情報
     * @return 更新されたユーザー情報とHTTPステータス (200 OK or 404 Not Found)
     */
    @PutMapping("/me")
    public ResponseEntity<UserDto> updateUser(@Valid @RequestBody UserDto userDto, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        return userRepository.findById(currentUser.getId())
                .map(existingUser -> {
                    existingUser.setFirstname(userDto.getFirstname());
                    existingUser.setLastname(userDto.getLastname());
                    User updatedUser = userRepository.save(existingUser);
                    return ResponseEntity.ok(convertToDto(updatedUser));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * 認証ユーザーの本人確認フラグを更新します。
     * 
     * @param authentication 現在認証されているユーザー情報
     * @return 更新されたユーザー情報とHTTPステータス (200 OK or 404 Not Found)
     */
    @PutMapping("/me/verify")
    public ResponseEntity<UserDto> verifyUser(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        return userRepository.findById(currentUser.getId())
                .map(existingUser -> {
                    existingUser.setIdVerified(true);
                    existingUser.setMyNumberVerified(true);
                    User updatedUser = userRepository.save(existingUser);
                    return ResponseEntity.ok(convertToDto(updatedUser));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
