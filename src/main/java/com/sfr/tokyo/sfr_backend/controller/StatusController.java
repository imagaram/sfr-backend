package com.sfr.tokyo.sfr_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sfr.tokyo.sfr_backend.dto.UserStatusDto;
import com.sfr.tokyo.sfr_backend.repository.UserRepository;
import com.sfr.tokyo.sfr_backend.user.Status;
import com.sfr.tokyo.sfr_backend.user.User;

import lombok.RequiredArgsConstructor;

// ユーザーのステータス関連のAPIエンドポイントを定義するコントローラー
@RestController
@RequestMapping("/api/status")
@RequiredArgsConstructor
public class StatusController {

    private final UserRepository userRepository;

    // 現在認証されているユーザーを取得
    private User getCurrentAuthenticatedUser() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));
    }

    /**
     * ユーザーのステータスを取得するエンドポイント
     * @return ユーザーの現在のステータスとHTTPステータス (200 OK or 404 Not Found)
     */
    @GetMapping("/me")
    public ResponseEntity<UserStatusDto> getUserStatus(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return userRepository.findById(currentUser.getId())
                .map(User::getStatus)
                .map(status -> new UserStatusDto(status.name()))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    /**
     * ユーザーのステータスを更新するエンドポイント
     * @param userStatusDto 更新するステータス情報
     * @return 更新されたユーザー情報とHTTPステータス (200 OK or 400 Bad Request)
     */
    @PutMapping("/me")
    public ResponseEntity<String> updateStatus(@RequestBody UserStatusDto userStatusDto) {
        User currentUser = getCurrentAuthenticatedUser();
        
        try {
            // リクエストされた文字列をStatus enumに変換
            Status status = Status.valueOf(userStatusDto.getStatus().toUpperCase());
            currentUser.setStatus(status);
            userRepository.save(currentUser);
            return ResponseEntity.ok("User status updated to " + status);
        } catch (IllegalArgumentException e) {
            // 無効なステータスがリクエストされた場合
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid status: " + userStatusDto.getStatus());
        }
    }
}
