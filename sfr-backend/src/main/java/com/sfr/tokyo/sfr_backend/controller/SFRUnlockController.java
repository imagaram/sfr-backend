package com.sfr.tokyo.sfr_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/sfr")
@CrossOrigin(origins = "http://localhost:3002")
public class SFRUnlockController {

    // マイナンバーカード認証によるSFR機能解除
    @PostMapping("/unlock-features")
    public ResponseEntity<Map<String, Object>> unlockSFRFeatures(@RequestBody Map<String, Object> requestData) {
        try {
            String verificationToken = (String) requestData.get("verificationToken");
            String userId = (String) requestData.get("userId");
            String unlockType = (String) requestData.get("unlockType");

            // トークン検証（実際の実装では詳細な検証を行う）
            if (verificationToken == null || !verificationToken.startsWith("mnc_token_")) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "無効な認証トークンです");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // データベースでユーザーのSFR機能を有効化（実際の実装）
            // userService.unlockSFRFeatures(userId, verificationToken);

            // SFR機能解除のレスポンス
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "SFR暗号資産機能が正常に解除されました");
            response.put("userId", userId);
            response.put("unlockTimestamp", LocalDateTime.now().toString());
            response.put("verificationLevel", "highest");

            // 解除された機能一覧
            Map<String, Object> unlockedFeatures = new HashMap<>();
            unlockedFeatures.put("sfrTransfers", true);
            unlockedFeatures.put("fullTransactionHistory", true);
            unlockedFeatures.put("corporateTransactions", true);
            unlockedFeatures.put("digitalContracts", true);
            unlockedFeatures.put("highAmountTransactions", true);
            unlockedFeatures.put("prioritySupport", true);
            unlockedFeatures.put("advancedAnalytics", true);

            response.put("unlockedFeatures", unlockedFeatures);

            // SFR残高情報（初期値）
            Map<String, Object> sfrBalance = new HashMap<>();
            sfrBalance.put("balance", "0.00000000");
            sfrBalance.put("currency", "SFR");
            sfrBalance.put("status", "active");
            sfrBalance.put("lastUpdated", LocalDateTime.now().toString());

            response.put("sfrAccount", sfrBalance);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "SFR機能解除処理中にエラーが発生しました: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // SFR機能解除状態の確認
    @GetMapping("/unlock-status/{userId}")
    public ResponseEntity<Map<String, Object>> checkUnlockStatus(@PathVariable String userId) {
        try {
            // 実際の実装では、データベースからユーザーのSFR解除状態を取得

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userId", userId);
            response.put("isUnlocked", true); // デモ用
            response.put("unlockMethod", "mynumber_verification");
            response.put("unlockDate", LocalDateTime.now().toString());
            response.put("verificationLevel", "highest");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "ステータス確認中にエラーが発生しました");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // SFR機能の詳細情報取得
    @GetMapping("/features-info")
    public ResponseEntity<Map<String, Object>> getSFRFeaturesInfo() {
        Map<String, Object> featuresInfo = new HashMap<>();

        // 基本機能（認証前でも利用可能）
        Map<String, Object> basicFeatures = new HashMap<>();
        basicFeatures.put("viewBalance", "SFR残高の表示");
        basicFeatures.put("basicTransactionHistory", "基本的な取引履歴表示");
        basicFeatures.put("profileManagement", "プロフィール管理");

        // プレミアム機能（マイナンバー認証後に解除）
        Map<String, Object> premiumFeatures = new HashMap<>();
        premiumFeatures.put("sfrTransfers", "SFR暗号資産の送受信");
        premiumFeatures.put("fullTransactionHistory", "完全な取引履歴アクセス");
        premiumFeatures.put("corporateTransactions", "企業間取引機能");
        premiumFeatures.put("digitalContracts", "法的効力のあるデジタル契約");
        premiumFeatures.put("highAmountTransactions", "高額取引（制限解除）");
        premiumFeatures.put("prioritySupport", "優先サポート");
        premiumFeatures.put("advancedAnalytics", "高度な分析機能");

        featuresInfo.put("basicFeatures", basicFeatures);
        featuresInfo.put("premiumFeatures", premiumFeatures);
        featuresInfo.put("unlockMethod", "マイナンバーカード認証");
        featuresInfo.put("requiredVerificationLevel", "highest");

        return ResponseEntity.ok(featuresInfo);
    }
}
