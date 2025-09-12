package com.sfr.tokyo.sfr_backend.openapi.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 外部連携用OpenAPI拡張仕様DTO
 * SDK生成および外部DAO接続のためのメタデータ定義
 */
@Data
@Builder
public class ExternalIntegrationSpecDto {
    
    /**
     * API基本情報
     */
    private ApiInfo apiInfo;
    
    /**
     * SDK生成設定
     */
    private SdkGenerationConfig sdkConfig;
    
    /**
     * 外部DAO連携設定
     */
    private ExternalDaoConfig daoConfig;
    
    /**
     * 認証・認可設定
     */
    private AuthenticationConfig authConfig;
    
    /**
     * レート制限設定
     */
    private RateLimitingConfig rateLimiting;
    
    /**
     * WebHook設定
     */
    private WebHookConfig webHookConfig;
    
    /**
     * データ同期設定
     */
    private DataSyncConfig dataSyncConfig;
    
    /**
     * API基本情報
     */
    @Data
    @Builder
    public static class ApiInfo {
        /**
         * API名称
         */
        private String apiName;
        
        /**
         * APIバージョン
         */
        private String version;
        
        /**
         * ベースURL
         */
        private String baseUrl;
        
        /**
         * API説明
         */
        private String description;
        
        /**
         * 利用規約URL
         */
        private String termsOfServiceUrl;
        
        /**
         * 連絡先情報
         */
        private ContactInfo contact;
        
        /**
         * ライセンス情報
         */
        private LicenseInfo license;
        
        /**
         * サポートされているプロトコル
         */
        private List<String> protocols;
        
        /**
         * サポートされているメディアタイプ
         */
        private List<String> mediaTypes;
        
        /**
         * API の成熟度レベル
         */
        private MaturityLevel maturityLevel;
        
        public enum MaturityLevel {
            EXPERIMENTAL("実験的"),
            BETA("ベータ"),
            STABLE("安定版"),
            DEPRECATED("非推奨");
            
            private final String description;
            
            MaturityLevel(String description) {
                this.description = description;
            }
            
            public String getDescription() {
                return description;
            }
        }
    }
    
    /**
     * 連絡先情報
     */
    @Data
    @Builder
    public static class ContactInfo {
        private String name;
        private String email;
        private String url;
    }
    
    /**
     * ライセンス情報
     */
    @Data
    @Builder
    public static class LicenseInfo {
        private String name;
        private String url;
    }
    
    /**
     * SDK生成設定
     */
    @Data
    @Builder
    public static class SdkGenerationConfig {
        /**
         * サポートされているプログラミング言語
         */
        private List<LanguageConfig> supportedLanguages;
        
        /**
         * 生成されるSDKの設定
         */
        private SdkSettings sdkSettings;
        
        /**
         * コード生成オプション
         */
        private CodeGenerationOptions codeGenOptions;
        
        /**
         * ドキュメント生成設定
         */
        private DocumentationConfig documentation;
    }
    
    /**
     * 言語設定
     */
    @Data
    @Builder
    public static class LanguageConfig {
        /**
         * プログラミング言語
         */
        private String language;
        
        /**
         * 言語バージョン
         */
        private String version;
        
        /**
         * パッケージ名
         */
        private String packageName;
        
        /**
         * 依存関係管理ツール
         */
        private String dependencyManager;
        
        /**
         * 追加依存関係
         */
        private List<Dependency> dependencies;
        
        /**
         * 言語固有設定
         */
        private Map<String, Object> languageSpecificConfig;
    }
    
    /**
     * 依存関係
     */
    @Data
    @Builder
    public static class Dependency {
        private String groupId;
        private String artifactId;
        private String version;
        private String scope;
    }
    
    /**
     * SDK設定
     */
    @Data
    @Builder
    public static class SdkSettings {
        /**
         * SDK名
         */
        private String sdkName;
        
        /**
         * 名前空間
         */
        private String namespace;
        
        /**
         * 非同期サポート
         */
        private boolean asyncSupport;
        
        /**
         * リトライ機能
         */
        private RetryConfig retryConfig;
        
        /**
         * ログ設定
         */
        private LoggingConfig loggingConfig;
        
        /**
         * キャッシュ設定
         */
        private CachingConfig cachingConfig;
    }
    
    /**
     * リトライ設定
     */
    @Data
    @Builder
    public static class RetryConfig {
        private boolean enabled;
        private int maxRetries;
        private long retryDelayMs;
        private double backoffMultiplier;
        private List<Integer> retryableStatusCodes;
    }
    
    /**
     * ログ設定
     */
    @Data
    @Builder
    public static class LoggingConfig {
        private boolean enabled;
        private String logLevel;
        private boolean logRequests;
        private boolean logResponses;
        private boolean logErrors;
    }
    
    /**
     * キャッシュ設定
     */
    @Data
    @Builder
    public static class CachingConfig {
        private boolean enabled;
        private long defaultTtlSeconds;
        private int maxCacheSize;
        private List<String> cacheableEndpoints;
    }
    
    /**
     * コード生成オプション
     */
    @Data
    @Builder
    public static class CodeGenerationOptions {
        /**
         * 生成されるファイル構造
         */
        private FileStructure fileStructure;
        
        /**
         * 命名規則
         */
        private NamingConventions namingConventions;
        
        /**
         * 型安全性設定
         */
        private TypeSafetyConfig typeSafety;
        
        /**
         * バリデーション生成
         */
        private ValidationConfig validation;
    }
    
    /**
     * ファイル構造
     */
    @Data
    @Builder
    public static class FileStructure {
        private String modelsDirectory;
        private String servicesDirectory;
        private String exceptionsDirectory;
        private String utilsDirectory;
    }
    
    /**
     * 命名規則
     */
    @Data
    @Builder
    public static class NamingConventions {
        private String classNaming; // "PascalCase", "snake_case", etc.
        private String methodNaming;
        private String propertyNaming;
        private String constantNaming;
    }
    
    /**
     * 型安全性設定
     */
    @Data
    @Builder
    public static class TypeSafetyConfig {
        private boolean strictTypes;
        private boolean nullSafety;
        private boolean immutableObjects;
        private boolean builderPattern;
    }
    
    /**
     * バリデーション設定
     */
    @Data
    @Builder
    public static class ValidationConfig {
        private boolean enabled;
        private List<String> validationAnnotations;
        private boolean customValidators;
    }
    
    /**
     * ドキュメント生成設定
     */
    @Data
    @Builder
    public static class DocumentationConfig {
        /**
         * ドキュメント形式
         */
        private List<DocumentFormat> formats;
        
        /**
         * サンプルコード生成
         */
        private boolean generateExamples;
        
        /**
         * チュートリアル生成
         */
        private boolean generateTutorials;
        
        /**
         * API リファレンス生成
         */
        private boolean generateApiReference;
        
        public enum DocumentFormat {
            MARKDOWN("Markdown"),
            HTML("HTML"),
            PDF("PDF"),
            CONFLUENCE("Confluence"),
            GITBOOK("GitBook");
            
            private final String description;
            
            DocumentFormat(String description) {
                this.description = description;
            }
            
            public String getDescription() {
                return description;
            }
        }
    }
    
    /**
     * 外部DAO設定
     */
    @Data
    @Builder
    public static class ExternalDaoConfig {
        /**
         * サポートされているDAO
         */
        private List<DaoIntegration> supportedDaos;
        
        /**
         * データ交換形式
         */
        private List<DataFormat> dataFormats;
        
        /**
         * 同期方式
         */
        private SyncMethod syncMethod;
        
        /**
         * データマッピング設定
         */
        private DataMappingConfig dataMapping;
        
        public enum SyncMethod {
            REAL_TIME("リアルタイム"),
            BATCH("バッチ"),
            EVENT_DRIVEN("イベント駆動"),
            HYBRID("ハイブリッド");
            
            private final String description;
            
            SyncMethod(String description) {
                this.description = description;
            }
            
            public String getDescription() {
                return description;
            }
        }
    }
    
    /**
     * DAO統合設定
     */
    @Data
    @Builder
    public static class DaoIntegration {
        /**
         * DAO名
         */
        private String daoName;
        
        /**
         * 統合タイプ
         */
        private IntegrationType integrationType;
        
        /**
         * エンドポイント設定
         */
        private EndpointConfig endpointConfig;
        
        /**
         * 認証設定
         */
        private DaoAuthConfig authConfig;
        
        /**
         * データ変換設定
         */
        private DataTransformationConfig dataTransformation;
        
        public enum IntegrationType {
            REST_API("REST API"),
            GRAPHQL("GraphQL"),
            WEBHOOK("WebHook"),
            MESSAGE_QUEUE("メッセージキュー"),
            DATABASE_DIRECT("直接DB接続");
            
            private final String description;
            
            IntegrationType(String description) {
                this.description = description;
            }
            
            public String getDescription() {
                return description;
            }
        }
    }
    
    /**
     * エンドポイント設定
     */
    @Data
    @Builder
    public static class EndpointConfig {
        private String baseUrl;
        private Map<String, String> endpoints;
        private int timeoutMs;
        private int connectionPoolSize;
        private boolean keepAlive;
    }
    
    /**
     * DAO認証設定
     */
    @Data
    @Builder
    public static class DaoAuthConfig {
        private String authType; // "API_KEY", "OAUTH2", "JWT", "BASIC"
        private Map<String, String> credentials;
        private long tokenExpirySeconds;
        private boolean autoRefresh;
    }
    
    /**
     * データ変換設定
     */
    @Data
    @Builder
    public static class DataTransformationConfig {
        private List<FieldMapping> fieldMappings;
        private List<String> transformationRules;
        private Map<String, String> valueTransformations;
    }
    
    /**
     * フィールドマッピング
     */
    @Data
    @Builder
    public static class FieldMapping {
        private String sourceField;
        private String targetField;
        private String dataType;
        private boolean required;
        private String defaultValue;
    }
    
    /**
     * データ形式
     */
    public enum DataFormat {
        JSON("JSON"),
        XML("XML"),
        CSV("CSV"),
        YAML("YAML"),
        PROTOBUF("Protocol Buffers"),
        AVRO("Apache Avro");
        
        private final String description;
        
        DataFormat(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * データマッピング設定
     */
    @Data
    @Builder
    public static class DataMappingConfig {
        /**
         * スキーママッピング
         */
        private Map<String, String> schemaMappings;
        
        /**
         * 型変換ルール
         */
        private List<TypeConversionRule> typeConversions;
        
        /**
         * バリデーションルール
         */
        private List<ValidationRule> validationRules;
    }
    
    /**
     * 型変換ルール
     */
    @Data
    @Builder
    public static class TypeConversionRule {
        private String sourceType;
        private String targetType;
        private String conversionFunction;
    }
    
    /**
     * バリデーションルール
     */
    @Data
    @Builder
    public static class ValidationRule {
        private String fieldName;
        private String validationType;
        private Map<String, Object> parameters;
        private String errorMessage;
    }
    
    /**
     * 認証設定
     */
    @Data
    @Builder
    public static class AuthenticationConfig {
        /**
         * サポートされている認証方式
         */
        private List<AuthMethod> supportedMethods;
        
        /**
         * デフォルト認証方式
         */
        private AuthMethod defaultMethod;
        
        /**
         * トークン設定
         */
        private TokenConfig tokenConfig;
        
        /**
         * OAuth2設定
         */
        private OAuth2Config oauth2Config;
        
        /**
         * API キー設定
         */
        private ApiKeyConfig apiKeyConfig;
    }
    
    /**
     * 認証方式
     */
    public enum AuthMethod {
        API_KEY("APIキー"),
        OAUTH2("OAuth 2.0"),
        JWT("JWT"),
        BASIC_AUTH("Basic認証"),
        BEARER_TOKEN("Bearer Token"),
        CUSTOM("カスタム");
        
        private final String description;
        
        AuthMethod(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * トークン設定
     */
    @Data
    @Builder
    public static class TokenConfig {
        private long expirySeconds;
        private boolean refreshable;
        private String tokenType;
        private List<String> scopes;
    }
    
    /**
     * OAuth2設定
     */
    @Data
    @Builder
    public static class OAuth2Config {
        private String authorizationUrl;
        private String tokenUrl;
        private String revokeUrl;
        private List<String> scopes;
        private List<String> grantTypes;
    }
    
    /**
     * APIキー設定
     */
    @Data
    @Builder
    public static class ApiKeyConfig {
        private String headerName;
        private String queryParam;
        private boolean required;
        private String description;
    }
    
    /**
     * レート制限設定
     */
    @Data
    @Builder
    public static class RateLimitingConfig {
        /**
         * レート制限ルール
         */
        private List<RateLimitRule> rules;
        
        /**
         * 制限超過時の動作
         */
        private LimitExceededBehavior exceedBehavior;
        
        /**
         * ヘッダー情報
         */
        private RateLimitHeaders headers;
    }
    
    /**
     * レート制限ルール
     */
    @Data
    @Builder
    public static class RateLimitRule {
        private String name;
        private int requestsPerPeriod;
        private long periodSeconds;
        private List<String> applicableEndpoints;
        private RateLimitScope scope;
        
        public enum RateLimitScope {
            GLOBAL("グローバル"),
            PER_USER("ユーザー毎"),
            PER_IP("IP毎"),
            PER_API_KEY("APIキー毎");
            
            private final String description;
            
            RateLimitScope(String description) {
                this.description = description;
            }
            
            public String getDescription() {
                return description;
            }
        }
    }
    
    /**
     * 制限超過時の動作
     */
    public enum LimitExceededBehavior {
        REJECT("拒否"),
        QUEUE("キュー"),
        THROTTLE("スロットリング");
        
        private final String description;
        
        LimitExceededBehavior(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * レート制限ヘッダー
     */
    @Data
    @Builder
    public static class RateLimitHeaders {
        private String limitHeader;
        private String remainingHeader;
        private String resetHeader;
        private String retryAfterHeader;
    }
    
    /**
     * WebHook設定
     */
    @Data
    @Builder
    public static class WebHookConfig {
        /**
         * サポートされているイベント
         */
        private List<WebHookEvent> supportedEvents;
        
        /**
         * WebHook エンドポイント設定
         */
        private WebHookEndpointConfig endpointConfig;
        
        /**
         * セキュリティ設定
         */
        private WebHookSecurityConfig securityConfig;
        
        /**
         * 再送設定
         */
        private RetryConfig retryConfig;
    }
    
    /**
     * WebHookイベント
     */
    @Data
    @Builder
    public static class WebHookEvent {
        private String eventType;
        private String description;
        private List<String> payloadFields;
        private String samplePayload;
    }
    
    /**
     * WebHookエンドポイント設定
     */
    @Data
    @Builder
    public static class WebHookEndpointConfig {
        private String callbackUrl;
        private String httpMethod;
        private Map<String, String> headers;
        private int timeoutMs;
    }
    
    /**
     * WebHookセキュリティ設定
     */
    @Data
    @Builder
    public static class WebHookSecurityConfig {
        private boolean signatureVerification;
        private String signatureHeader;
        private String algorithm;
        private boolean ipWhitelisting;
        private List<String> allowedIps;
    }
    
    /**
     * データ同期設定
     */
    @Data
    @Builder
    public static class DataSyncConfig {
        /**
         * 同期方式
         */
        private SyncStrategy syncStrategy;
        
        /**
         * 同期間隔
         */
        private long syncIntervalSeconds;
        
        /**
         * 競合解決方式
         */
        private ConflictResolution conflictResolution;
        
        /**
         * 同期対象エンティティ
         */
        private List<SyncEntity> syncEntities;
        
        public enum SyncStrategy {
            FULL_SYNC("完全同期"),
            INCREMENTAL("増分同期"),
            EVENT_SOURCING("イベントソーシング"),
            SNAPSHOT("スナップショット");
            
            private final String description;
            
            SyncStrategy(String description) {
                this.description = description;
            }
            
            public String getDescription() {
                return description;
            }
        }
        
        public enum ConflictResolution {
            LAST_WRITER_WINS("最終更新優先"),
            FIRST_WRITER_WINS("最初更新優先"),
            MANUAL("手動解決"),
            MERGE("マージ"),
            VERSION_VECTOR("バージョンベクター");
            
            private final String description;
            
            ConflictResolution(String description) {
                this.description = description;
            }
            
            public String getDescription() {
                return description;
            }
        }
    }
    
    /**
     * 同期エンティティ
     */
    @Data
    @Builder
    public static class SyncEntity {
        private String entityName;
        private String primaryKey;
        private List<String> syncFields;
        private String lastModifiedField;
        private boolean bidirectionalSync;
    }
}
