package com.sfr.tokyo.sfr_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import com.sfr.tokyo.sfr_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

// アプリケーション全体の設定を管理するクラス
@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserRepository repository;

    // UserDetailsServiceを定義するBean
    // メールアドレスからユーザー詳細をロードするロジック
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> repository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    // 認証プロバイダーを定義するBean
    // ユーザー詳細とパスワードエンコーダーを組み合わせて認証を行う
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // AuthenticationManagerを定義するBean
    // 認証設定からAuthenticationManagerを取得
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // パスワードエンコーダーを定義するBean
    // パスワードのハッシュ化と検証に使用
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // RestTemplateを定義するBean
    // 外部APIとの通信に使用
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
