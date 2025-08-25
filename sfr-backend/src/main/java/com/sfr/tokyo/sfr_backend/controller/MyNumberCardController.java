package com.sfr.tokyo.sfr_backend.controller;

import com.sfr.tokyo.sfr_backend.service.MyNumberCardOCRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth/mynumber")
@CrossOrigin(origins = "http://localhost:3002")
public class MyNumberCardController {

    @Autowired
    private MyNumberCardOCRService ocrService;

    // Step 1: マイナンバーカード画像のOCR処理（Google Cloud Vision API使用）
    @PostMapping("/ocr")
    public ResponseEntity<Map<String, Object>> processOCR(@RequestParam("image") MultipartFile image) {
        try {
            // 入力検証
            if (image.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "画像ファイルが指定されていません");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // ファイルサイズチェック（20MB制限）
            if (image.getSize() > 20 * 1024 * 1024) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "ファイルサイズが大きすぎます（20MB以下にしてください）");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // 対応ファイル形式チェック
            String contentType = image.getContentType();
            if (contentType == null || !isValidImageType(contentType)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "対応していないファイル形式です");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Google Cloud Vision APIを使用してOCR処理
            Map<String, Object> ocrResult = ocrService.extractMyNumberCardData(image);

            if (!(Boolean) ocrResult.get("success")) {
                return ResponseEntity.badRequest().body(ocrResult);
            }

            // レスポンス構築
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", ocrResult.get("extractedData"));
            response.put("confidence", ocrResult.get("confidence"));
            response.put("validationResults", ocrResult.get("validationResults"));
            response.put("sessionId", UUID.randomUUID().toString());
            response.put("ocrProvider", "Google Cloud Vision API");
            response.put("processedAt", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "OCR処理中にエラーが発生しました: " + e.getMessage());
            errorResponse.put("provider", "Google Cloud Vision API");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 有効な画像形式かチェック
     */
    private boolean isValidImageType(String contentType) {
        return contentType.equals("image/jpeg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/gif") ||
                contentType.equals("image/bmp") ||
                contentType.equals("image/webp");
    }

    // Google Cloud Vision API設定情報取得
    @GetMapping("/ocr/config")
    public ResponseEntity<Map<String, Object>> getOCRConfig() {
        Map<String, Object> config = ocrService.getVisionAPIConfig();
        return ResponseEntity.ok(config);
    }

    // Step 2: NFC読み取りデータの検証
    @PostMapping("/verify-nfc")
    public ResponseEntity<Map<String, Object>> verifyNFC(@RequestBody Map<String, Object> nfcData) {
        try {
            // 実際の実装では、ICチップデータの暗号化検証を行う
            String sessionId = (String) nfcData.get("sessionId");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("verified", true);
            response.put("sessionId", sessionId);
            response.put("digitalSignature", "verified_signature_" + UUID.randomUUID().toString());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "NFC検証に失敗しました");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Step 3: 本人確認トークンの生成
    @PostMapping("/generate-token")
    public ResponseEntity<Map<String, Object>> generateVerificationToken(
            @RequestBody Map<String, Object> verificationData) {
        try {
            String sessionId = (String) verificationData.get("sessionId");
            Map<String, Object> ocrData = (Map<String, Object>) verificationData.get("ocrData");
            String digitalSignature = (String) verificationData.get("digitalSignature");

            // 実際の実装では、デジタル署名とOCRデータの整合性を検証
            String verificationToken = "mnc_verified_" + UUID.randomUUID().toString();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("verificationToken", verificationToken);
            response.put("verificationLevel", "highest");
            response.put("validUntil", System.currentTimeMillis() + (24 * 60 * 60 * 1000)); // 24時間有効

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "トークン生成に失敗しました");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Step 4: マイナンバーカード認証済みユーザー登録
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerWithMyNumber(@RequestBody Map<String, Object> registrationData) {
        try {
            String verificationToken = (String) registrationData.get("verificationToken");
            String username = (String) registrationData.get("username");
            String email = (String) registrationData.get("email");
            String password = (String) registrationData.get("password");
            Map<String, Object> myNumberData = (Map<String, Object>) registrationData.get("myNumberData");

            // 実際の実装では、データベースにユーザーを保存
            // パスワードの暗号化、トークンの検証など

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userId", UUID.randomUUID().toString());
            response.put("message", "マイナンバーカード認証付きアカウントが作成されました");
            response.put("verificationLevel", "highest");
            response.put("features", Map.of(
                    "highAmountTransactions", true,
                    "corporateTransactions", true,
                    "legalContracts", true,
                    "prioritySupport", true));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "アカウント作成に失敗しました");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
