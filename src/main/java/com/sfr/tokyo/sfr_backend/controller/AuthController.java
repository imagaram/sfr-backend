package com.sfr.tokyo.sfr_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sfr.tokyo.sfr_backend.dto.AuthenticationRequest;
import com.sfr.tokyo.sfr_backend.dto.AuthenticationResponse;
import com.sfr.tokyo.sfr_backend.dto.RegisterRequest;
import com.sfr.tokyo.sfr_backend.service.AuthenticationService;

import lombok.RequiredArgsConstructor;

// ユーザー認証を扱うRESTコントローラー
@RestController
@RequestMapping("/api/v1/auth") // パスを"/api/v1/auth"に修正
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    // ユーザー登録のエンドポイント
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    // ユーザーログインのエンドポイント
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }
}
