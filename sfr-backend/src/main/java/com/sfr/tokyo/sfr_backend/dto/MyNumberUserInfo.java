package com.sfr.tokyo.sfr_backend.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * マイナンバーカード認証で取得したユーザー情報
 * デジタル認証アプリから取得される情報を格納
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MyNumberUserInfo {
    
    /**
     * デジタル庁発行の一意識別子（subject）
     * マイナンバー自体ではなく、デジタル庁が発行する永続的な識別子
     */
    private String subject;
    
    /**
     * 氏名（姓）
     */
    private String familyName;
    
    /**
     * 氏名（名）
     */
    private String givenName;
    
    /**
     * フルネーム
     */
    private String name;
    
    /**
     * 生年月日（ISO 8601形式: YYYY-MM-DD）
     */
    private String birthdate;
    
    /**
     * 性別（male/female/other）
     */
    private String gender;
    
    /**
     * 住所情報
     */
    private AddressInfo address;
    
    /**
     * 認証保証レベル
     * IAL3: http://8digits.org/ac/classes/loa3
     */
    private String assuranceLevel;
    
    /**
     * 認証方法
     */
    private String authenticationMethod;
    
    /**
     * 認証時刻
     */
    private Instant authenticatedAt;
    
    /**
     * 電子証明書の有効期限
     */
    private Instant certificateExpiresAt;
    
    /**
     * マイナンバーカードの有効性確認済みフラグ
     */
    private boolean cardVerified;
    
    /**
     * デジタル署名検証済みフラグ
     */
    private boolean signatureVerified;

    /**
     * 住所情報クラス
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AddressInfo {
        /**
         * 整形済み住所
         */
        private String formatted;
        
        /**
         * 都道府県
         */
        private String region;
        
        /**
         * 市区町村
         */
        private String locality;
        
        /**
         * 町域・番地
         */
        private String streetAddress;
        
        /**
         * 郵便番号
         */
        private String postalCode;
        
        /**
         * 国コード（常にJP）
         */
        private String country;
    }
}
