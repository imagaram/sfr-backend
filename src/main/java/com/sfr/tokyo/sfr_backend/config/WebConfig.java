package com.sfr.tokyo.sfr_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Webアプリケーションの設定クラス
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // application.propertiesで設定されたアップロードディレクトリのパスをインジェクション
    @Value("${file.upload-dir}")
    private String uploadDir;

    // リソースハンドラを設定し、静的リソース（この場合はアップロードされた画像）へのアクセスを許可する
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // "/images/"で始まるURLパスを、ファイルシステム上のuploadDirにマッピングする
        // file:プロトコルを使用して、ローカルファイルシステム上のディレクトリを指定
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }

    // CORS（Cross-Origin Resource Sharing）設定を追加
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // すべてのAPIエンドポイントに適用
                .allowedOrigins("http://localhost:3000", "http://127.0.0.1:3000") // Next.jsが動作するオリジンからのアクセスを許可
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 許可するHTTPメソッド
                .allowedHeaders("*") // すべてのヘッダーを許可
                .allowCredentials(true); // クッキーなどの資格情報の送信を許可
    }
}
