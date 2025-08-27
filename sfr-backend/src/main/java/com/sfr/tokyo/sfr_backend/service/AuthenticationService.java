package com.sfr.tokyo.sfr_backend.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import com.sfr.tokyo.sfr_backend.dto.AuthenticationRequest;
import com.sfr.tokyo.sfr_backend.dto.AuthenticationResponse;
import com.sfr.tokyo.sfr_backend.dto.RegisterRequest;
import com.sfr.tokyo.sfr_backend.repository.UserRepository;
import com.sfr.tokyo.sfr_backend.user.User; // Userエンティティの正しいインポート

import lombok.RequiredArgsConstructor;

// ユーザーの認証と登録を管理するサービス
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // ユーザー登録処理
    public AuthenticationResponse register(RegisterRequest request) {
        // Userエンティティを作成し、パスワードをハッシュ化
        var user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        // ユーザー情報をデータベースに保存
        repository.save(user);
        // JWTトークンを生成
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    // ユーザー認証（ログイン）処理
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // 認証マネージャを使用してユーザーを認証
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (AuthenticationException ex) {
            throw new com.sfr.tokyo.sfr_backend.exception.BusinessException(
                    com.sfr.tokyo.sfr_backend.exception.ErrorCode.AUTH_INVALID_CREDENTIALS,
                    "Invalid credentials");
        }
        // 認証に成功したら、データベースからユーザーを取得
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new com.sfr.tokyo.sfr_backend.exception.BusinessException(
                        com.sfr.tokyo.sfr_backend.exception.ErrorCode.AUTH_INVALID_CREDENTIALS,
                        "Invalid credentials"));
        // JWTトークンを生成
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
