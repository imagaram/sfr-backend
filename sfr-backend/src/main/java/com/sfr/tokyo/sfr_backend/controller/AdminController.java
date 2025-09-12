package com.sfr.tokyo.sfr_backend.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sfr.tokyo.sfr_backend.dto.UserDto;
import com.sfr.tokyo.sfr_backend.repository.UserRepository;
import com.sfr.tokyo.sfr_backend.service.UserService;
import com.sfr.tokyo.sfr_backend.user.User;

import lombok.RequiredArgsConstructor;

/**
 * 管理者機能のためのREST APIエンドポイントを定義するコントローラー
 * マイナンバー認証管理、ユーザー管理などの管理者専用機能を提供
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3002")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final UserService userService;

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
     * 全ユーザー一覧を取得（ページネーション対応）
     * 
     * @param page ページ番号（0ベース）
     * @param size 1ページあたりの件数
     * @param sort ソート順（createdAt, email, etc.）
     * @param direction ソート方向（asc, desc）
     * @return ユーザー一覧のページネーション結果
     */
    @GetMapping("/users")
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "email") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<User> users = userRepository.findAll(pageable);
        Page<UserDto> userDtos = users.map(this::convertToDto);
        
        return ResponseEntity.ok(userDtos);
    }

    /**
     * マイナンバー認証済みユーザー一覧を取得
     * 
     * @return マイナンバー認証済みユーザーのリスト
     */
    @GetMapping("/users/mynumber-verified")
    public ResponseEntity<List<UserDto>> getMyNumberVerifiedUsers() {
        List<User> verifiedUsers = userRepository.findByMyNumberVerifiedTrue();
        List<UserDto> userDtos = verifiedUsers.stream()
                .map(this::convertToDto)
                .toList();
        
        return ResponseEntity.ok(userDtos);
    }

    /**
     * 本人確認済みユーザー一覧を取得
     * 
     * @return 本人確認済みユーザーのリスト
     */
    @GetMapping("/users/id-verified")
    public ResponseEntity<List<UserDto>> getIdVerifiedUsers() {
        List<User> verifiedUsers = userRepository.findByIdVerifiedTrue();
        List<UserDto> userDtos = verifiedUsers.stream()
                .map(this::convertToDto)
                .toList();
        
        return ResponseEntity.ok(userDtos);
    }

    /**
     * 特定ユーザーの詳細情報を取得
     * 
     * @param userId ユーザーID
     * @return ユーザー詳細情報
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID userId) {
        return userRepository.findById(userId)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * ユーザーの認証ステータスを更新
     * 
     * @param userId ユーザーID
     * @param request 更新する認証ステータス情報
     * @return 更新されたユーザー情報
     */
    @PutMapping("/users/{userId}/verification-status")
    public ResponseEntity<UserDto> updateVerificationStatus(
            @PathVariable UUID userId,
            @RequestBody Map<String, Boolean> request) {
        
        boolean idVerified = request.getOrDefault("idVerified", false);
        boolean myNumberVerified = request.getOrDefault("myNumberVerified", false);
        
        return userService.updateVerificationStatus(userId, idVerified, myNumberVerified)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * 管理画面用の統計情報を取得
     * 
     * @return ユーザー認証状況の統計情報
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        long totalUsers = userRepository.count();
        long idVerifiedUsers = userRepository.findByIdVerifiedTrue().size();
        long myNumberVerifiedUsers = userRepository.findByMyNumberVerifiedTrue().size();
        
        Map<String, Object> statistics = Map.of(
            "totalUsers", totalUsers,
            "idVerifiedUsers", idVerifiedUsers,
            "myNumberVerifiedUsers", myNumberVerifiedUsers,
            "idVerificationRate", totalUsers > 0 ? (double) idVerifiedUsers / totalUsers * 100 : 0.0,
            "myNumberVerificationRate", totalUsers > 0 ? (double) myNumberVerifiedUsers / totalUsers * 100 : 0.0
        );
        
        return ResponseEntity.ok(statistics);
    }

    /**
     * ユーザー検索（名前またはメールアドレスで部分一致検索）
     * 
     * @param query 検索クエリ
     * @return 検索にマッチしたユーザーのリスト
     */
    @GetMapping("/users/search")
    public ResponseEntity<List<UserDto>> searchUsers(@RequestParam String query) {
        List<User> users = userRepository.findByEmailContainingIgnoreCaseOrFirstnameContainingIgnoreCaseOrLastnameContainingIgnoreCase(query);
        List<UserDto> userDtos = users.stream()
                .map(this::convertToDto)
                .toList();
        
        return ResponseEntity.ok(userDtos);
    }
}
