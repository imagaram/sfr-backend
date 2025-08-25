package com.sfr.tokyo.sfr_backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Google Cloud Vision APIを使用したマイナンバーカードOCR処理サービス
 */
@Service
public class MyNumberCardOCRService {

    // Google Cloud Vision API クライアント（実際の実装では依存性注入）
    // private ImageAnnotatorClient visionClient;

    /**
     * マイナンバーカード画像からテキストを抽出し、構造化データを返す
     * 
     * @param image マイナンバーカード画像ファイル
     * @return 抽出されたデータ
     */
    public Map<String, Object> extractMyNumberCardData(MultipartFile image) {
        try {
            // 実際の実装：Google Cloud Vision APIを使用してOCR処理
            String ocrText = performVisionAPICall(image);

            // OCR結果からマイナンバーカードの各項目を抽出
            Map<String, String> extractedData = parseMyNumberCardText(ocrText);

            // 抽出データの検証
            ValidationResult validation = validateExtractedData(extractedData);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("extractedData", extractedData);
            result.put("confidence", validation.confidence);
            result.put("validationResults", validation.results);

            return result;

        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", "OCR処理中にエラーが発生しました: " + e.getMessage());
            return errorResult;
        }
    }

    /**
     * Google Cloud Vision API を使用してOCR処理を実行
     * 実際の実装では以下のようになります：
     * 
     * import com.google.cloud.vision.v1.*;
     * import com.google.protobuf.ByteString;
     * 
     * @param image 画像ファイル
     * @return 抽出されたテキスト
     */
    private String performVisionAPICall(MultipartFile image) throws Exception {
        // デモ実装：実際にはGoogle Cloud Vision APIを呼び出し
        /*
         * // 実際の実装例:
         * try (ImageAnnotatorClient visionClient = ImageAnnotatorClient.create()) {
         * ByteString imgBytes = ByteString.copyFrom(image.getBytes());
         * Image img = Image.newBuilder().setContent(imgBytes).build();
         * Feature feat =
         * Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
         * AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
         * .addFeatures(feat)
         * .setImage(img)
         * .build();
         * 
         * BatchAnnotateImagesResponse response = visionClient.batchAnnotateImages(
         * BatchAnnotateImagesRequest.newBuilder()
         * .addRequests(request)
         * .build());
         * 
         * List<AnnotateImageResponse> responses = response.getResponsesList();
         * AnnotateImageResponse res = responses.get(0);
         * 
         * if (res.hasError()) {
         * throw new Exception("Vision API Error: " + res.getError().getMessage());
         * }
         * 
         * return res.getFullTextAnnotation().getText();
         * }
         */

        // デモ用の模擬OCRテキスト
        return """
                マイナンバーカード
                個人番号カード

                山田太郎
                YAMADA TARO

                昭和60年1月1日
                男

                12345678901234567890123456789012

                東京都千代田区霞が関1-1-1

                令和6年8月23日まで有効
                """;
    }

    /**
     * OCRテキストからマイナンバーカードの各項目を抽出
     * 
     * @param ocrText OCRで抽出されたテキスト
     * @return 構造化されたデータ
     */
    private Map<String, String> parseMyNumberCardText(String ocrText) {
        Map<String, String> data = new HashMap<>();

        // 照合番号B（32桁の数字）のパターン
        Pattern collationNumberPattern = Pattern.compile("(\\d{32})");
        Matcher collationMatcher = collationNumberPattern.matcher(ocrText);
        if (collationMatcher.find()) {
            data.put("照合番号B", collationMatcher.group(1));
        }

        // 氏名のパターン（カタカナの前の漢字）
        Pattern namePattern = Pattern.compile("([\\p{IsHan}]+)\\s*\\n\\s*([A-Z\\s]+)");
        Matcher nameMatcher = namePattern.matcher(ocrText);
        if (nameMatcher.find()) {
            data.put("氏名", nameMatcher.group(1));
            data.put("氏名_カナ", nameMatcher.group(2));
        }

        // 生年月日のパターン
        Pattern birthdatePattern = Pattern.compile("([昭和|平成|令和]\\d+年\\d+月\\d+日)");
        Matcher birthdateMatcher = birthdatePattern.matcher(ocrText);
        if (birthdateMatcher.find()) {
            data.put("生年月日", birthdateMatcher.group(1));
        }

        // 性別のパターン
        Pattern genderPattern = Pattern.compile("(男|女)");
        Matcher genderMatcher = genderPattern.matcher(ocrText);
        if (genderMatcher.find()) {
            data.put("性別", genderMatcher.group(1));
        }

        // 住所のパターン（都道府県から始まる）
        Pattern addressPattern = Pattern.compile("([\\p{IsHan}]+[都道府県][\\p{IsHan}\\d\\-]+)");
        Matcher addressMatcher = addressPattern.matcher(ocrText);
        if (addressMatcher.find()) {
            data.put("住所", addressMatcher.group(1));
        }

        // 有効期限のパターン
        Pattern expiryPattern = Pattern.compile("([令和|平成]\\d+年\\d+月\\d+日)まで有効");
        Matcher expiryMatcher = expiryPattern.matcher(ocrText);
        if (expiryMatcher.find()) {
            data.put("有効期限", expiryMatcher.group(1));
        }

        return data;
    }

    /**
     * 抽出されたデータの検証
     * 
     * @param data 抽出されたデータ
     * @return 検証結果
     */
    private ValidationResult validateExtractedData(Map<String, String> data) {
        ValidationResult result = new ValidationResult();
        Map<String, String> validationResults = new HashMap<>();

        // 照合番号Bの検証
        String collationNumber = data.get("照合番号B");
        if (collationNumber != null && collationNumber.matches("\\d{32}")) {
            validationResults.put("照合番号B", "✅ 有効");
            result.confidence += 20;
        } else {
            validationResults.put("照合番号B", "❌ 無効または未検出");
        }

        // 氏名の検証
        String name = data.get("氏名");
        if (name != null && name.matches("[\\p{IsHan}]+")) {
            validationResults.put("氏名", "✅ 有効");
            result.confidence += 20;
        } else {
            validationResults.put("氏名", "❌ 無効または未検出");
        }

        // 生年月日の検証
        String birthdate = data.get("生年月日");
        if (birthdate != null && birthdate.matches("([昭和|平成|令和])\\d+年\\d+月\\d+日")) {
            validationResults.put("生年月日", "✅ 有効");
            result.confidence += 20;
        } else {
            validationResults.put("生年月日", "❌ 無効または未検出");
        }

        // 性別の検証
        String gender = data.get("性別");
        if (gender != null && (gender.equals("男") || gender.equals("女"))) {
            validationResults.put("性別", "✅ 有効");
            result.confidence += 20;
        } else {
            validationResults.put("性別", "❌ 無効または未検出");
        }

        // 住所の検証
        String address = data.get("住所");
        if (address != null && address.matches(".*[都道府県].*")) {
            validationResults.put("住所", "✅ 有効");
            result.confidence += 20;
        } else {
            validationResults.put("住所", "❌ 無効または未検出");
        }

        result.results = validationResults;
        return result;
    }

    /**
     * 検証結果を格納するクラス
     */
    private static class ValidationResult {
        int confidence = 0;
        Map<String, String> results;
    }

    /**
     * Google Cloud Vision APIの設定情報を取得
     * 
     * @return 設定情報
     */
    public Map<String, Object> getVisionAPIConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("provider", "Google Cloud Vision API");
        config.put("version", "v1");
        config.put("features", new String[] {
                "TEXT_DETECTION",
                "DOCUMENT_TEXT_DETECTION",
                "OBJECT_LOCALIZATION"
        });
        config.put("supportedFormats", new String[] {
                "JPEG", "PNG", "GIF", "BMP", "WEBP", "RAW", "ICO", "PDF", "TIFF"
        });
        config.put("maxFileSize", "20MB");
        config.put("maxResolution", "75MP");

        return config;
    }
}
