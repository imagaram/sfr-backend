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
import com.sfr.tokyo.sfr_backend.dto.UserStatusDto;
import com.sfr.tokyo.sfr_backend.repository.UserRepository;
import com.sfr.tokyo.sfr_backend.user.Status;
import com.sfr.tokyo.sfr_backend.user.User;

import lombok.RequiredArgsConstructor;

// ユーザー情報に関連するREST APIエンドポイントを定義するコントローラー
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    /**
     * DTOからエンティティに変換する際の、UserDtoを生成するヘルパーメソッド。
     * @param user 変換元のUserエンティティ
     * @return 変換されたUserDto
     */
    private UserDto convertToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .status(user.getStatus())
                .build();
    }

    /**
     * 認証ユーザー自身の基本情報を取得します。
     * @param authentication 現在認証されているユーザー情報
     * @return ユーザー基本情報とHTTPステータス (200 OK or 404 Not Found)
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto> getAuthenticatedUser(Authentication authentication) {
        // authenticationからユーザーを取得
        User currentUser = (User) authentication.getPrincipal();

        // ユーザーIDを使用してDBから最新のユーザー情報を取得
        return userRepository.findById(currentUser.getId())
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * 認証ユーザー自身の基本情報を更新します。
     * @param userDto 更新するユーザー情報
     * @param authentication 現在認証されているユーザー情報
     * @return 更新されたユーザー情報とHTTPステータス (200 OK or 404 Not Found)
     */
    @PutMapping("/me")
    public ResponseEntity<UserDto> updateUser(@RequestBody UserDto userDto, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        
        return userRepository.findById(currentUser.getId())
                .map(existingUser -> {
                    // DTOの情報でユーザー情報を更新
                    existingUser.setFirstname(userDto.getFirstname());
                    existingUser.setLastname(userDto.getLastname());
                    // パスワードなど、他の重要な情報は別のエンドポイントで更新するのが一般的です。

                    User updatedUser = userRepository.save(existingUser);
                    return ResponseEntity.ok(convertToDto(updatedUser));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * 認証ユーザーのステータスを切り替えます。
     * @param userStatusDto 更新するステータス情報
     * @param authentication 現在認証されているユーザー情報
     * @return 更新されたユーザー情報とHTTPステータス (200 OK or 400 Bad Request)
     */
    @PutMapping("/me/status")
    public ResponseEntity<UserDto> updateStatus(@RequestBody UserStatusDto userStatusDto, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        Optional<User> userOptional = userRepository.findById(currentUser.getId());
        
        // ユーザーが見つからない場合は404 Not Foundを返す
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            User existingUser = userOptional.get();
            // 文字列からEnumに変換
            Status newStatus = Status.valueOf(userStatusDto.getStatus().toUpperCase());
            existingUser.setStatus(newStatus);
            User updatedUser = userRepository.save(existingUser);
            // 成功した場合は更新されたUserDtoを返す
            return ResponseEntity.ok(convertToDto(updatedUser));
        } catch (IllegalArgumentException e) {
            // 無効なステータスが指定された場合は400 Bad Requestを返す
            return ResponseEntity.badRequest().build();
        }
    }
}
