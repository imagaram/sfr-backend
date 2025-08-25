package com.sfr.tokyo.sfr_backend.filter;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sfr.tokyo.sfr_backend.service.JwtService;

import lombok.RequiredArgsConstructor;

// JWT認証を処理するためのカスタムフィルター
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Authorizationヘッダーがない、または"Bearer "で始まらない場合は、次のフィルターへ進む
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // AuthorizationヘッダーからJWTトークンを抽出
        // "Bearer "の7文字分をスキップし、さらにtrim()で前後の空白を除去
        jwt = authHeader.substring(7).trim();

        // JWTトークンからユーザーのメールアドレスを抽出
        userEmail = jwtService.extractUsername(jwt);

        // ユーザーメールが抽出され、かつまだ認証されていない場合
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // UserDetailsServiceからユーザー詳細情報を取得
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // トークンが有効な場合は、SecurityContextを更新
            if (jwtService.isTokenValid(jwt, userDetails)) {
                // 認証トークンを作成
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                // SecurityContextHolderに認証情報を設定
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        // 次のフィルターへ進む
        filterChain.doFilter(request, response);
    }
}
