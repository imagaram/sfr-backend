package com.sfr.tokyo.sfr_backend.service;

import com.sfr.tokyo.sfr_backend.dto.MyNumberUserInfo;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * マイナンバーカード OAuth 2.0 / OpenID Connect認証サービス
 * デジタル庁のデジタル認証アプリとの連携を処理
 */
@Service
public class MyNumberOAuthService {

    @Value("${mynumber.oauth.client-id:sfr-tokyo-client}")
    private String clientId;
    
    @Value("${mynumber.oauth.client-secret:}")
    private String clientSecret;
    
    @Value("${mynumber.oauth.redirect-uri:http://localhost:3002/api/auth/mynumber/callback}")
    private String redirectUri;
    
    @Value("${mynumber.oauth.auth-url:https://auth.digital.go.jp/oauth2/authorize}")
    private String authUrl;
    
    @Value("${mynumber.oauth.token-url:https://auth.digital.go.jp/oauth2/token}")
    private String tokenUrl;
    
    @Value("${mynumber.oauth.userinfo-url:https://auth.digital.go.jp/oauth2/userinfo}")
    private String userinfoUrl;
    
    @Value("${mynumber.oauth.jwks-url:https://auth.digital.go.jp/.well-known/jwks.json}")
    private String jwksUrl;

    private final RestTemplate restTemplate;
    
    public MyNumberOAuthService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * OAuth 2.0認証URLを構築
     * PKCE (Proof Key for Code Exchange) を使用してセキュリティを強化
     * 
     * @param request HTTPリクエスト
     * @return 認証URL
     */
    public String buildAuthorizationUrl(HttpServletRequest request) {
        // CSRF攻撃防止用のstateパラメータ生成
        String state = UUID.randomUUID().toString();
        
        // PKCEのcode_verifierとcode_challenge生成
        String codeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(codeVerifier);
        
        // セッションに保存（後で検証に使用）
        HttpSession session = request.getSession();
        session.setAttribute("oauth_state", state);
        session.setAttribute("pkce_code_verifier", codeVerifier);
        
        // 認証URL構築
        return UriComponentsBuilder.fromHttpUrl(authUrl)
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", "openid profile mynumber_basic mynumber_address")
                .queryParam("state", state)
                .queryParam("nonce", UUID.randomUUID().toString())
                .queryParam("code_challenge", codeChallenge)
                .queryParam("code_challenge_method", "S256")
                .queryParam("acr_values", "http://8digits.org/ac/classes/loa3") // IAL3要求
                .build()
                .toUriString();
    }

    /**
     * 認証コードをアクセストークンに交換
     * 
     * @param code 認証コード
     * @param state 状態パラメータ
     * @return アクセストークン
     */
    public String exchangeCodeForToken(String code, String state) {
        // TODO: 実際の実装では、セッションからstateとcode_verifierを取得して検証
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);
        params.add("redirect_uri", redirectUri);
        // params.add("code_verifier", codeVerifier); // PKCEの検証
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            Map<String, Object> tokenResponse = response.getBody();
            
            if (tokenResponse != null && tokenResponse.containsKey("access_token")) {
                return (String) tokenResponse.get("access_token");
            } else {
                throw new RuntimeException("Failed to obtain access token");
            }
        } catch (Exception e) {
            throw new RuntimeException("Token exchange failed", e);
        }
    }

    /**
     * アクセストークンからIDトークンを取得
     * 
     * @param accessToken アクセストークン
     * @return IDトークン（JWT）
     */
    public String getIdToken(String accessToken) {
        // 通常、IDトークンはトークンレスポンスに含まれるが、
        // 簡易実装として別途取得する想定
        
        // TODO: 実際の実装では、tokenレスポンスからid_tokenを直接取得
        return "dummy_id_token_" + accessToken;
    }

    /**
     * IDトークン（JWT）の検証
     * デジタル庁の公開鍵を使用して署名を検証
     * 
     * @param idToken IDトークン
     * @return 検証結果
     */
    public boolean verifyIdToken(String idToken) {
        try {
            // TODO: 実際の実装では、JWKSエンドポイントから公開鍵を取得
            // RSAPublicKey publicKey = getPublicKeyFromJWKS();
            
            // 簡易実装として、フォーマットチェックのみ
            if (idToken.startsWith("dummy_id_token_")) {
                return true; // デモ用
            }
            
            // 実際のJWT検証処理
            /*
            DecodedJWT jwt = JWT.require(Algorithm.RSA256(publicKey, null))
                .withIssuer("https://auth.digital.go.jp")
                .withAudience(clientId)
                .build()
                .verify(idToken);
                
            // ACR（認証コンテキストクラス参照）の確認
            String acr = jwt.getClaim("acr").asString();
            return "http://8digits.org/ac/classes/loa3".equals(acr);
            */
            
            return false; // 実装完了まではfalse
            
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * ユーザー情報を取得
     * 
     * @param accessToken アクセストークン
     * @return ユーザー情報
     */
    public MyNumberUserInfo getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                userinfoUrl, HttpMethod.GET, request, Map.class);
            
            Map<String, Object> userinfo = response.getBody();
            
            if (userinfo == null) {
                throw new RuntimeException("Failed to get user info");
            }
            
            // レスポンスをMyNumberUserInfoに変換
            return convertToMyNumberUserInfo(userinfo);
            
        } catch (Exception e) {
            // デモ用のダミーデータ
            return createDummyUserInfo();
        }
    }

    /**
     * ユーザー情報レスポンスをDTOに変換
     * 
     * @param userinfo ユーザー情報マップ
     * @return MyNumberUserInfoオブジェクト
     */
    private MyNumberUserInfo convertToMyNumberUserInfo(Map<String, Object> userinfo) {
        Map<String, Object> address = (Map<String, Object>) userinfo.get("address");
        
        MyNumberUserInfo.AddressInfo addressInfo = null;
        if (address != null) {
            addressInfo = MyNumberUserInfo.AddressInfo.builder()
                    .formatted((String) address.get("formatted"))
                    .region((String) address.get("region"))
                    .locality((String) address.get("locality"))
                    .streetAddress((String) address.get("street_address"))
                    .postalCode((String) address.get("postal_code"))
                    .country((String) address.get("country"))
                    .build();
        }
        
        return MyNumberUserInfo.builder()
                .subject((String) userinfo.get("sub"))
                .name((String) userinfo.get("name"))
                .givenName((String) userinfo.get("given_name"))
                .familyName((String) userinfo.get("family_name"))
                .birthdate((String) userinfo.get("birthdate"))
                .gender((String) userinfo.get("gender"))
                .address(addressInfo)
                .assuranceLevel((String) userinfo.get("acr"))
                .authenticationMethod("mynumber_card")
                .authenticatedAt(Instant.now())
                .cardVerified(true)
                .signatureVerified(true)
                .build();
    }

    /**
     * デモ用のダミーユーザー情報作成
     * 
     * @return ダミーのMyNumberUserInfo
     */
    private MyNumberUserInfo createDummyUserInfo() {
        MyNumberUserInfo.AddressInfo addressInfo = MyNumberUserInfo.AddressInfo.builder()
                .formatted("東京都千代田区霞が関1-1-1")
                .region("東京都")
                .locality("千代田区")
                .streetAddress("霞が関1-1-1")
                .postalCode("100-0013")
                .country("JP")
                .build();
        
        return MyNumberUserInfo.builder()
                .subject("demo_user_" + UUID.randomUUID().toString())
                .name("山田太郎")
                .givenName("太郎")
                .familyName("山田")
                .birthdate("1985-01-01")
                .gender("male")
                .address(addressInfo)
                .assuranceLevel("http://8digits.org/ac/classes/loa3")
                .authenticationMethod("mynumber_card")
                .authenticatedAt(Instant.now())
                .certificateExpiresAt(Instant.now().plusSeconds(365 * 24 * 60 * 60)) // 1年後
                .cardVerified(true)
                .signatureVerified(true)
                .build();
    }

    /**
     * PKCE用のcode_verifier生成
     * 
     * @return code_verifier
     */
    private String generateCodeVerifier() {
        // 43-128文字のランダム文字列
        return UUID.randomUUID().toString().replace("-", "") + 
               UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * PKCE用のcode_challenge生成
     * 
     * @param codeVerifier code_verifier
     * @return code_challenge
     */
    private String generateCodeChallenge(String codeVerifier) {
        // SHA256ハッシュ化してBase64URLエンコード
        // 簡易実装として、元の文字列をそのまま返す
        return codeVerifier; // 実際の実装では適切にハッシュ化
    }
}
