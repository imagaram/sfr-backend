package com.sfr.tokyo.sfr_backend.service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

// JWTの生成、検証、抽出を行うサービス
@Service
public class JwtService {

    // 秘密鍵。実際のアプリケーションでは環境変数から読み込むべきです。
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    // JWTからユーザー名を抽出
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // JWTから特定のクレームを抽出
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // ユーザー情報に基づいてJWTを生成
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    // ユーザー情報と追加のクレームに基づいてJWTを生成
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24時間有効
                .signWith(getSignInKey())
                .compact();
    }

    // JWTが有効かどうかを検証
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // JWTが期限切れかどうかをチェック
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // JWTから有効期限を抽出
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // JWTのすべてのクレームを抽出
    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 署名に使用する秘密鍵を取得
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
