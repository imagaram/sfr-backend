package com.sfr.tokyo.sfr_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.sfr.tokyo.sfr_backend.filter.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

// JWT認証を有効にするためのセキュリティ設定クラス
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    // JwtAuthenticationFilterをインジェクション
    private final JwtAuthenticationFilter jwtAuthFilter;
    // 認証プロバイダーをインジェクション
    private final AuthenticationProvider authenticationProvider;
    // 作成したカスタム認証エントリーポイントをインジェクション
    private final AuthEntryPoint authEntryPoint; // <-- 追加

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF保護を無効化（JWTはステートレスなため）
            .csrf(AbstractHttpConfigurer::disable)
            // HTTPリクエストの認可ルールを設定
            .authorizeHttpRequests(authRequest ->
                authRequest
                    // 認証エンドポイント（/api/v1/auth/**）へのアクセスは認証なしで許可
                    .requestMatchers(
                        "/api/v1/auth/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**"
                    )
                    .permitAll()
                    // それ以外のすべてのリクエストは認証を要求
                    .anyRequest()
                    .authenticated()
            )
            // セッション管理をステートレスに設定
            .sessionManagement(sessionManagement ->
                sessionManagement
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // カスタム認証プロバイダーを登録
            .authenticationProvider(authenticationProvider)
            // 作成したJWTフィルターをUsernamePasswordAuthenticationFilterの前に実行
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            // 認証失敗時のカスタムハンドラーを登録
            .exceptionHandling(exceptionHandling ->
                exceptionHandling
                    .authenticationEntryPoint(authEntryPoint) // <-- 追加
            );
        
        return http.build();
    }
}
