package com.sfr.tokyo.sfr_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.sfr.tokyo.sfr_backend.dto.UserStatusDto;
import com.sfr.tokyo.sfr_backend.repository.UserRepository;
import com.sfr.tokyo.sfr_backend.user.UserState;
import com.sfr.tokyo.sfr_backend.user.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/status")
@RequiredArgsConstructor
public class StatusController {

    private final UserRepository userRepository;

    private User getCurrentAuthenticatedUser() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));
    }

    /**
     * ユーザーの状態（UserState）を取得するエンドポイント
     */
    @GetMapping("/me")
    public ResponseEntity<UserStatusDto> getUserStatus(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return userRepository.findById(currentUser.getId())
                .map(User::getState)
                .map(state -> new UserStatusDto(state.name()))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * ユーザーの状態（UserState）を更新するエンドポイント
     */
    @PutMapping("/me")
    public ResponseEntity<String> updateStatus(@Valid @RequestBody UserStatusDto userStatusDto) {
        User currentUser = getCurrentAuthenticatedUser();

        try {
            UserState state = UserState.valueOf(userStatusDto.getStatus().toUpperCase());
            currentUser.setState(state);
            userRepository.save(currentUser);
            return ResponseEntity.ok("User state updated to " + state);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid state: " + userStatusDto.getStatus());
        }
    }
}
