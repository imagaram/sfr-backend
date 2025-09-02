# SFR.TOKYO 外部SDK生成設定

## OpenAPI Generator設定

### TypeScript SDK生成
```bash
# TypeScript/JavaScript SDK生成
npx @openapitools/openapi-generator-cli generate \
  -i http://localhost:8080/v3/api-docs \
  -g typescript-axios \
  -o ./sdk/typescript \
  --additional-properties=npmName=@sfr-tokyo/api-client,npmVersion=1.0.0

# Node.js専用SDK生成
npx @openapitools/openapi-generator-cli generate \
  -i http://localhost:8080/v3/api-docs \
  -g typescript-node \
  -o ./sdk/nodejs \
  --additional-properties=npmName=@sfr-tokyo/node-client,npmVersion=1.0.0
```

### Python SDK生成
```bash
# Python SDK生成
npx @openapitools/openapi-generator-cli generate \
  -i http://localhost:8080/v3/api-docs \
  -g python \
  -o ./sdk/python \
  --additional-properties=packageName=sfr_tokyo_client,packageVersion=1.0.0
```

### Java SDK生成
```bash
# Java SDK生成
npx @openapitools/openapi-generator-cli generate \
  -i http://localhost:8080/v3/api-docs \
  -g java \
  -o ./sdk/java \
  --additional-properties=groupId=tokyo.sfr,artifactId=sfr-api-client,artifactVersion=1.0.0
```

### Go SDK生成
```bash
# Go SDK生成
npx @openapitools/openapi-generator-cli generate \
  -i http://localhost:8080/v3/api-docs \
  -g go \
  -o ./sdk/go \
  --additional-properties=packageName=sfrapi,packageVersion=1.0.0
```

## SDK使用例

### TypeScript使用例
```typescript
import { ManifestoExternalApiApi, Configuration } from '@sfr-tokyo/api-client';

const config = new Configuration({
  basePath: 'http://localhost:8080'
});

const manifestoApi = new ManifestoExternalApiApi(config);

// 最新Manifesto取得
const manifesto = await manifestoApi.getCurrentManifesto('ja');

// 検索
const searchResults = await manifestoApi.searchManifesto('ガバナンス', 'ja', 0, 10);

// 利用可能言語一覧
const languages = await manifestoApi.getAvailableLanguages();
```

### Python使用例
```python
import sfr_tokyo_client
from sfr_tokyo_client.api import manifesto_external_api
from sfr_tokyo_client.configuration import Configuration

# 設定
config = Configuration(host="http://localhost:8080")

# APIクライアント初期化
api_client = sfr_tokyo_client.ApiClient(config)
manifesto_api = manifesto_external_api.ManifestoExternalApiApi(api_client)

# 最新Manifesto取得
manifesto = manifesto_api.get_current_manifesto(language="ja")

# 検索
search_results = manifesto_api.search_manifesto(
    keyword="ガバナンス", 
    language="ja", 
    page=0, 
    size=10
)

# 利用可能言語一覧
languages = manifesto_api.get_available_languages()
```

### Java使用例
```java
import tokyo.sfr.client.ApiClient;
import tokyo.sfr.client.api.ManifestoExternalApiApi;
import tokyo.sfr.client.model.ApiResponse;
import tokyo.sfr.client.model.ManifestoI18nDto;

// APIクライアント初期化
ApiClient client = new ApiClient();
client.setBasePath("http://localhost:8080");

ManifestoExternalApiApi manifestoApi = new ManifestoExternalApiApi(client);

// 最新Manifesto取得
ApiResponse<ManifestoI18nDto> manifesto = manifestoApi.getCurrentManifesto("ja", null);

// 検索
ApiResponse<List<ContentStructureDto>> searchResults = 
    manifestoApi.searchManifesto("ガバナンス", "ja", 0, 10);

// 利用可能言語一覧
ApiResponse<List<String>> languages = manifestoApi.getAvailableLanguages();
```

## 外部DAO統合パターン

### Spring Boot統合
```java
@Component
public class SfrManifestoDao {
    
    @Value("${sfr.api.base-url}")
    private String sfrApiBaseUrl;
    
    private ManifestoExternalApiApi manifestoApi;
    
    @PostConstruct
    public void init() {
        ApiClient client = new ApiClient();
        client.setBasePath(sfrApiBaseUrl);
        this.manifestoApi = new ManifestoExternalApiApi(client);
    }
    
    public ManifestoI18nDto getCurrentManifesto(String language) {
        return manifestoApi.getCurrentManifesto(language, null).getData();
    }
    
    public List<ContentStructureDto> searchManifesto(String keyword, String language, Pageable pageable) {
        return manifestoApi.searchManifesto(keyword, language, 
                pageable.getPageNumber(), pageable.getPageSize()).getData();
    }
}
```

### Express.js統合
```javascript
const { ManifestoExternalApiApi, Configuration } = require('@sfr-tokyo/node-client');

class SfrManifestoService {
    constructor() {
        const config = new Configuration({
            basePath: process.env.SFR_API_BASE_URL || 'http://localhost:8080'
        });
        this.manifestoApi = new ManifestoExternalApiApi(config);
    }
    
    async getCurrentManifesto(language = 'ja') {
        const response = await this.manifestoApi.getCurrentManifesto(language);
        return response.data.data;
    }
    
    async searchManifesto(keyword, language = 'ja', page = 0, size = 10) {
        const response = await this.manifestoApi.searchManifesto(keyword, language, page, size);
        return response.data;
    }
}

module.exports = SfrManifestoService;
```

## ビルド自動化

### GitHub Actions設定例
```yaml
name: Generate and Publish SDKs

on:
  push:
    branches: [ main ]
    paths: [ 'src/main/java/com/sfr/tokyo/sfr_backend/external/**' ]

jobs:
  generate-sdks:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Setup Java
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Build and Start Application
      run: |
        ./mvnw clean package -DskipTests
        java -jar target/sfr-backend-*.jar &
        sleep 30
    
    - name: Generate TypeScript SDK
      run: |
        npx @openapitools/openapi-generator-cli generate \
          -i http://localhost:8080/v3/api-docs \
          -g typescript-axios \
          -o ./generated-sdks/typescript
    
    - name: Generate Python SDK
      run: |
        npx @openapitools/openapi-generator-cli generate \
          -i http://localhost:8080/v3/api-docs \
          -g python \
          -o ./generated-sdks/python
    
    - name: Generate Java SDK
      run: |
        npx @openapitools/openapi-generator-cli generate \
          -i http://localhost:8080/v3/api-docs \
          -g java \
          -o ./generated-sdks/java
    
    - name: Publish SDKs
      run: |
        # NPM公開（TypeScript）
        cd generated-sdks/typescript
        npm publish
        
        # PyPI公開（Python）
        cd ../python
        python setup.py sdist upload
        
        # Maven Central公開（Java）
        cd ../java
        mvn deploy
```

## 設定ファイル

### application.yml拡張
```yaml
sfr:
  api:
    version: "1.0.0"
    base-url: "http://localhost:8080"
    external:
      enabled: true
      rate-limit:
        enabled: false
        requests-per-minute: 60
      auth:
        enabled: false
        api-key-header: "X-SFR-API-Key"
  
  cors:
    allowed-origins: 
      - "http://localhost:3000"
      - "http://localhost:8080"
      - "https://sfr.tokyo"
    allowed-methods:
      - "GET"
      - "POST"
      - "PUT" 
      - "DELETE"
      - "OPTIONS"
    allowed-headers:
      - "Content-Type"
      - "Authorization"
      - "X-SFR-API-Key"
```
