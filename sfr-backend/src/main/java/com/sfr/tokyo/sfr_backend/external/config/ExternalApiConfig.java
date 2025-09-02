package com.sfr.tokyo.sfr_backend.external.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 外部連携用OpenAPI設定
 * 
 * 外部SDK生成・DAO連携のためのOpenAPI仕様を設定します。
 * この設定により統一されたAPI仕様書が生成され、各種言語のSDKを自動生成できます。
 * 
 * @author SFR.TOKYO Development Team
 * @version 1.0.0
 * @since 2025-09-02
 */
@Configuration
public class ExternalApiConfig {

    @Value("${sfr.api.version:1.0.0}")
    private String apiVersion;

    @Value("${sfr.api.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${sfr.api.external.enabled:true}")
    private boolean externalApiEnabled;

    /**
     * 外部連携用OpenAPI設定
     */
    @Bean
    public OpenAPI externalOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SFR.TOKYO External API")
                        .description("""
                                SFR.TOKYO 外部連携用API仕様書
                                
                                ## 概要
                                このAPIは、SFR.TOKYOの機能を外部システム・SDK・DAOから利用するための統一インターフェースです。
                                
                                ## 特長
                                - **多言語対応**: ja/en対応、将来的な言語拡張可能
                                - **バージョン管理**: APIとデータの両方でバージョン管理
                                - **統一レスポンス**: すべてのエンドポイントで統一されたレスポンス形式
                                - **ページング対応**: 大量データの効率的な取得
                                - **検索機能**: キーワード・言語別の高速検索
                                
                                ## 利用想定
                                - 外部Webアプリケーション
                                - モバイルアプリケーション
                                - 各種言語のSDK
                                - データアクセスオブジェクト (DAO)
                                - サードパーティ統合
                                
                                ## 認証
                                現在は認証なしで利用可能です。将来的にはAPI Key認証を追加予定です。
                                
                                ## レート制限
                                現在はレート制限なしです。本格運用時には適切なレート制限を設定予定です。
                                """)
                        .version(apiVersion)
                        .contact(new Contact()
                                .name("SFR.TOKYO Development Team")
                                .email("dev@sfr.tokyo")
                                .url("https://sfr.tokyo"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url(baseUrl)
                                .description("開発環境"),
                        new Server()
                                .url("https://api.sfr.tokyo")
                                .description("本番環境（予定）")
                ))
                .tags(List.of(
                        new Tag()
                                .name("Manifesto External API")
                                .description("Manifesto多言語コンテンツの外部アクセス用API"),
                        new Tag()
                                .name("Council External API")
                                .description("評議員・選挙システムの外部アクセス用API（将来拡張）"),
                        new Tag()
                                .name("Crypto External API")
                                .description("SFR暗号資産システムの外部アクセス用API（将来拡張）"),
                        new Tag()
                                .name("Learning External API")
                                .description("学習空間システムの外部アクセス用API（将来拡張）")
                ));
    }

    /**
     * 外部API有効化フラグ
     */
    public boolean isExternalApiEnabled() {
        return externalApiEnabled;
    }
}
