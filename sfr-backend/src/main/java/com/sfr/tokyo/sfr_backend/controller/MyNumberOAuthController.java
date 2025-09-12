package com.sfr.tokyo.sfr_backend.controller;

import com.sfr.tokyo.sfr_backend.service.MyNumberOAuthService;
import com.sfr.tokyo.sfr_backend.service.UserService;
import com.sfr.tokyo.sfr_backend.dto.MyNumberUserInfo;
import com.sfr.tokyo.sfr_backend.user.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * マイナンバーカード認証（デジタル認証アプリ連携）コントローラー
 * OAuth 2.0 / OpenID Connectを使用した政府公式認証システムとの連携
 */
@Controller
@RequestMapping("/api/auth/mynumber")
@CrossOrigin(origins = "http://localhost:3002")
public class MyNumberOAuthController {

    @Autowired
    private MyNumberOAuthService oauthService;
    
    @Autowired
    private UserService userService;

    /**
     * マイナンバーカード認証開始
     * デジタル認証アプリにリダイレクトして認証を開始
     * 
     * @param request HTTPリクエスト
     * @return デジタル認証アプリの認証URLへのリダイレクト
     */
    @GetMapping("/login")
    public RedirectView initiateAuth(HttpServletRequest request) {
        try {
            // OAuth 2.0認証URLを構築
            String authUrl = oauthService.buildAuthorizationUrl(request);
            
            // セッションに状態情報を保存
            HttpSession session = request.getSession();
            session.setAttribute("mynumber_auth_initiated", System.currentTimeMillis());
            
            return new RedirectView(authUrl);
            
        } catch (Exception e) {
            // エラー時はエラーページにリダイレクト
            return new RedirectView("/auth/mynumber/error?reason=init_failed");
        }
    }

    /**
     * 認証コールバック処理
     * デジタル認証アプリから認証結果を受け取って処理
     * 
     * @param code 認証コード
     * @param state CSRF攻撃防止用状態パラメータ
     * @param request HTTPリクエスト
     * @return 認証成功時はダッシュボード、失敗時はエラーページにリダイレクト
     */
    @GetMapping("/callback")
    public RedirectView handleCallback(
            @RequestParam String code,
            @RequestParam String state,
            @RequestParam(required = false) String error,
            HttpServletRequest request) {
        
        try {
            // エラーパラメータチェック
            if (error != null) {
                return new RedirectView("/auth/mynumber/error?reason=" + error);
            }
            
            // セッション検証
            HttpSession session = request.getSession();
            Long authInitiated = (Long) session.getAttribute("mynumber_auth_initiated");
            if (authInitiated == null) {
                return new RedirectView("/auth/mynumber/error?reason=session_invalid");
            }
            
            // 認証コードをアクセストークンに交換
            String accessToken = oauthService.exchangeCodeForToken(code, state);
            
            // IDトークンを検証
            String idToken = oauthService.getIdToken(accessToken);
            boolean isValidToken = oauthService.verifyIdToken(idToken);
            
            if (!isValidToken) {
                return new RedirectView("/auth/mynumber/error?reason=token_invalid");
            }
            
            // ユーザー情報取得
            MyNumberUserInfo userInfo = oauthService.getUserInfo(accessToken);
            
            // 保証レベル確認（IAL3必須）
            if (!"http://8digits.org/ac/classes/loa3".equals(userInfo.getAssuranceLevel())) {
                return new RedirectView("/auth/mynumber/error?reason=insufficient_assurance");
            }
            
            // ユーザー登録・ログイン処理
            User user = userService.processMyNumberAuthentication(userInfo);
            
            // セッションにユーザー情報設定
            session.setAttribute("user_id", user.getId());
            session.setAttribute("mynumber_verified", true);
            session.setAttribute("verification_level", "loa3");
            
            // セッション清理
            session.removeAttribute("mynumber_auth_initiated");
            
            return new RedirectView("/dashboard?mynumber_verified=true");
            
        } catch (Exception e) {
            return new RedirectView("/auth/mynumber/error?reason=processing_failed");
        }
    }

    /**
     * 認証状態確認API
     * フロントエンドから認証状態を確認するためのエンドポイント
     * 
     * @param request HTTPリクエスト
     * @return 認証状態情報
     */
    @GetMapping("/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAuthStatus(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Map<String, Object> response = new HashMap<>();
        
        if (session != null) {
            UUID userId = (UUID) session.getAttribute("user_id");
            Boolean myNumberVerified = (Boolean) session.getAttribute("mynumber_verified");
            String verificationLevel = (String) session.getAttribute("verification_level");
            
            response.put("authenticated", userId != null);
            response.put("mynumber_verified", myNumberVerified != null && myNumberVerified);
            response.put("verification_level", verificationLevel);
            response.put("user_id", userId != null ? userId.toString() : null);
        } else {
            response.put("authenticated", false);
            response.put("mynumber_verified", false);
            response.put("verification_level", null);
            response.put("user_id", null);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * マイナンバー認証解除
     * ユーザーのマイナンバー認証を解除（管理者機能）
     * 
     * @param userId ユーザーID
     * @param request HTTPリクエスト
     * @return 処理結果
     */
    @PostMapping("/revoke/{userId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> revokeMyNumberAuth(
            @PathVariable UUID userId,
            HttpServletRequest request) {
        
        try {
            // 管理者権限チェック（簡易実装）
            HttpSession session = request.getSession(false);
            if (session == null || !isAdmin(session)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "管理者権限が必要です");
                return ResponseEntity.status(403).body(errorResponse);
            }
            
            // マイナンバー認証解除処理
            boolean revoked = userService.revokeMyNumberVerification(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", revoked);
            response.put("message", revoked ? "マイナンバー認証を解除しました" : "解除に失敗しました");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "処理中にエラーが発生しました");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * デジタル認証アプリ連携情報取得
     * フロントエンドでの認証開始前の情報表示用
     * 
     * @return デジタル認証アプリの情報
     */
    @GetMapping("/info")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDigitalAuthInfo() {
        Map<String, Object> info = new HashMap<>();
        
        info.put("auth_method", "digital_agency_official");
        info.put("security_level", "ial3_aal3_fal3");
        info.put("required_app", "デジタル認証アプリ");
        info.put("app_download_url", "https://services.digital.go.jp/auth-and-sign/");
        info.put("supported_features", Map.of(
            "ic_chip_verification", true,
            "digital_certificate", true,
            "pin_authentication", true,
            "anti_forgery", true,
            "government_certified", true
        ));
        
        return ResponseEntity.ok(info);
    }

    /**
     * 簡易管理者権限チェック
     * 
     * @param session HTTPセッション
     * @return 管理者かどうか
     */
    private boolean isAdmin(HttpSession session) {
        // 実際の実装では、より厳密な権限チェックを行う
        String userRole = (String) session.getAttribute("user_role");
        return "ADMIN".equals(userRole);
    }
}
