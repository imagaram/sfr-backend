# 🚀 SFR.TOKYO API 仕様書

**OpenAPI 3.0.0 準拠**  
**最終更新日**: 2025年8月18日  
**バージョン**: 2.0

---

```yaml
openapi: 3.0.0
info:
  title: SFR.TOKYO REST API
  description: |
    SFR.TOKYO プラットフォームの統合REST API
    
    ## 🔐 認証
    - JWT Bearer Token認証を使用
    - `/api/v1/auth`で認証トークンを取得
    
    ## 🛡️ セキュリティ
    - レート制限：IP単位で100req/min、認証エンドポイント10req/min
    - セキュリティヘッダー：CSP, HSTS, X-Frame-Options等
    - アクセスログ・監視機能
    
    ## 📝 エラーハンドリング
    - 標準HTTPステータスコード
    - 詳細なエラーメッセージをレスポンスボディに含む
    
  version: 2.0.0
  contact:
    name: SFR.TOKYO Development Team
    email: dev@sfr.tokyo
  license:
    name: MIT
    url: https://opensource.org/licenses/MIT

servers:
  - url: https://api.sfr.tokyo/api
    description: 本番環境
  - url: https://dev-api.sfr.tokyo/api
    description: 開発環境
  - url: http://localhost:8080/api
    description: ローカル開発環境

security:
  - bearerAuth: []

tags:
  - name: authentication
    description: 🔐 認証・登録機能
  - name: users
    description: 👤 ユーザー管理
  - name: user-status
    description: 📊 ユーザー状態管理
  - name: characters
    description: 🎭 キャラクター管理
  - name: posts
    description: 📝 投稿管理
  - name: comments
    description: 💬 コメント管理
  - name: wallet
    description: 💰 出納帳管理
  - name: uploads
    description: 📁 ファイルアップロード
  - name: learning-spaces
    description: 🎓 学習空間管理
  - name: learning-content
    description: 📚 学習コンテンツ管理
  - name: learning-progress
    description: 📈 学習進捗管理
  - name: learning-comments
    description: 💭 学習コメント管理
  - name: learning-live-sessions
    description: 🎥 ライブセッション管理
  - name: learning-quiz
    description: ❓ クイズ管理
  - name: learning-analytics
    description: 📊 学習分析

paths:
  # ======================================
  # 🔐 認証・登録エンドポイント
  # ======================================
  /v1/auth/register:
    post:
      tags:
        - authentication
      summary: ユーザー新規登録
      description: |
        新しいユーザーアカウントを作成します。
        登録後、自動的にJWTトークンが発行されます。
      operationId: registerUser
      security: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/RegisterRequest'
            example:
              firstname: "太郎"
              lastname: "田中"
              email: "tanaka.taro@example.com"
              password: "SecurePassword123!"
      responses:
        '201':
          description: 登録成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AuthenticationResponse'
        '400':
          description: バリデーションエラー
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '409':
          description: メールアドレス重複エラー
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'

  /v1/auth/authenticate:
    post:
      tags:
        - authentication
      summary: ユーザーログイン
      description: |
        メールアドレスとパスワードでログインし、JWTトークンを取得します。
      operationId: authenticateUser
      security: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/AuthenticationRequest'
            example:
              email: "tanaka.taro@example.com"
              password: "SecurePassword123!"
      responses:
        '200':
          description: ログイン成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AuthenticationResponse'
        '401':
          description: 認証失敗
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '429':
          description: レート制限超過
          headers:
            X-RateLimit-Remaining:
              schema:
                type: integer
              description: 残りリクエスト数
            X-RateLimit-Reset:
              schema:
                type: integer
              description: リセット時刻（UNIX時間）

  # ======================================
  # 👤 ユーザー管理エンドポイント
  # ======================================
  /users/profile:
    get:
      tags:
        - users
      summary: ユーザープロフィール取得
      description: 認証済みユーザー自身のプロフィール情報を取得します。
      operationId: getUserProfile
      responses:
        '200':
          description: プロフィール取得成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UserDto'
        '401':
          description: 認証エラー
        '404':
          description: ユーザーが見つからない

    put:
      tags:
        - users
      summary: ユーザープロフィール更新
      description: 認証済みユーザー自身のプロフィール情報を更新します。
      operationId: updateUserProfile
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UserUpdateRequest'
      responses:
        '200':
          description: 更新成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UserDto'
        '400':
          description: バリデーションエラー
        '401':
          description: 認証エラー
        '404':
          description: ユーザーが見つからない

  # ======================================
  # 📊 ユーザー状態管理エンドポイント
  # ======================================
  /status/me:
    get:
      tags:
        - user-status
      summary: ユーザー状態取得
      description: 認証済みユーザーの状態（ENABLED/DISABLED）を取得します。
      operationId: getUserStatus
      responses:
        '200':
          description: 状態取得成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UserStatusDto'
        '401':
          description: 認証エラー
        '404':
          description: ユーザーが見つからない

    put:
      tags:
        - user-status
      summary: ユーザー状態更新
      description: 認証済みユーザーの状態を更新します。
      operationId: updateUserStatus
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UserStatusDto'
      responses:
        '200':
          description: 更新成功
        '400':
          description: バリデーションエラー
        '401':
          description: 認証エラー

  # ======================================
  # 🎭 キャラクター管理エンドポイント
  # ======================================
  /characters:
    get:
      tags:
        - characters
      summary: キャラクター一覧取得
      description: 認証済みユーザーが所有するキャラクター一覧を取得します。
      operationId: getCharacters
      responses:
        '200':
          description: キャラクター一覧取得成功
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/CharacterDto'

    post:
      tags:
        - characters
      summary: キャラクター新規作成
      description: |
        新しいキャラクターを作成します。
        画像ファイルの同時アップロードが可能です。
      operationId: createCharacter
      requestBody:
        required: true
        content:
          multipart/form-data:
            schema:
              type: object
              properties:
                name:
                  type: string
                  description: キャラクター名
                  example: "桜花"
                profile:
                  type: string
                  description: プロフィール
                  example: "元気で明るい性格の女の子"
                image:
                  type: string
                  format: binary
                  description: キャラクター画像ファイル
      responses:
        '201':
          description: キャラクター作成成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/CharacterDto'
        '400':
          description: バリデーションエラー
        '401':
          description: 認証エラー

  /characters/{id}:
    get:
      tags:
        - characters
      summary: キャラクター詳細取得
      description: 指定されたIDのキャラクター詳細情報を取得します。
      operationId: getCharacterById
      parameters:
        - name: id
          in: path
          required: true
          description: キャラクターID
          schema:
            type: integer
            format: int64
            example: 1
      responses:
        '200':
          description: キャラクター詳細取得成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/CharacterDto'
        '401':
          description: 認証エラー
        '403':
          description: アクセス権限なし
        '404':
          description: キャラクターが見つからない

    put:
      tags:
        - characters
      summary: キャラクター情報更新
      description: 指定されたIDのキャラクター情報を更新します。
      operationId: updateCharacter
      parameters:
        - name: id
          in: path
          required: true
          description: キャラクターID
          schema:
            type: integer
            format: int64
      requestBody:
        required: true
        content:
          multipart/form-data:
            schema:
              type: object
              properties:
                name:
                  type: string
                  description: キャラクター名
                profile:
                  type: string
                  description: プロフィール
                image:
                  type: string
                  format: binary
                  description: キャラクター画像ファイル
      responses:
        '200':
          description: 更新成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/CharacterDto'
        '400':
          description: バリデーションエラー
        '401':
          description: 認証エラー
        '403':
          description: アクセス権限なし
        '404':
          description: キャラクターが見つからない

    delete:
      tags:
        - characters
      summary: キャラクター削除
      description: 指定されたIDのキャラクターを削除します。
      operationId: deleteCharacter
      parameters:
        - name: id
          in: path
          required: true
          description: キャラクターID
          schema:
            type: integer
            format: int64
      responses:
        '204':
          description: 削除成功
        '401':
          description: 認証エラー
        '403':
          description: アクセス権限なし
        '404':
          description: キャラクターが見つからない

  # ======================================
  # 🎓 学習空間管理エンドポイント
  # ======================================
  /learning/spaces:
    post:
      tags:
        - learning-spaces
      summary: 学習空間作成
      description: |
        新しい学習空間を作成します。
        SCHOOL、SALON、FANCLUBの3つのモードから選択可能です。
      operationId: createLearningSpace
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/LearningSpaceCreateDto'
      responses:
        '201':
          description: 学習空間作成成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/LearningSpaceCreateResponse'
        '400':
          description: バリデーションエラー
        '401':
          description: 認証エラー

  /learning/spaces/{id}/config:
    get:
      tags:
        - learning-spaces
      summary: 学習空間設定取得
      description: 指定された学習空間の設定情報を取得します。
      operationId: getLearningSpaceConfig
      parameters:
        - name: id
          in: path
          required: true
          description: 学習空間ID
          schema:
            type: integer
            format: int64
      responses:
        '200':
          description: 設定取得成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/LearningModeConfigDto'
        '401':
          description: 認証エラー
        '404':
          description: 学習空間が見つからない

# ======================================
# 📊 コンポーネント定義
# ======================================
components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
      description: |
        JWT Bearer Token認証
        
        **取得方法:**
        1. `/api/v1/auth/register` または `/api/v1/auth/authenticate` でトークンを取得
        2. `Authorization: Bearer <token>` ヘッダーに設定
        
        **有効期限:** 24時間

  schemas:
    # ======================================
    # 🔐 認証関連スキーマ
    # ======================================
    RegisterRequest:
      type: object
      required:
        - firstname
        - lastname
        - email
        - password
      properties:
        firstname:
          type: string
          minLength: 1
          maxLength: 50
          description: 名
          example: "太郎"
        lastname:
          type: string
          minLength: 1
          maxLength: 50
          description: 姓
          example: "田中"
        email:
          type: string
          format: email
          description: メールアドレス
          example: "tanaka.taro@example.com"
        password:
          type: string
          minLength: 8
          maxLength: 100
          description: パスワード（8文字以上）
          example: "SecurePassword123!"

    AuthenticationRequest:
      type: object
      required:
        - email
        - password
      properties:
        email:
          type: string
          format: email
          description: メールアドレス
          example: "tanaka.taro@example.com"
        password:
          type: string
          description: パスワード
          example: "SecurePassword123!"

    AuthenticationResponse:
      type: object
      properties:
        access_token:
          type: string
          description: JWTアクセストークン
          example: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        token_type:
          type: string
          description: トークンタイプ
          example: "Bearer"
        expires_in:
          type: integer
          description: 有効期限（秒）
          example: 86400

    # ======================================
    # 👤 ユーザー関連スキーマ
    # ======================================
    UserDto:
      type: object
      properties:
        id:
          type: string
          format: uuid
          description: ユーザーID
          example: "123e4567-e89b-12d3-a456-426614174000"
        firstname:
          type: string
          description: 名
          example: "太郎"
        lastname:
          type: string
          description: 姓
          example: "田中"
        email:
          type: string
          format: email
          description: メールアドレス
          example: "tanaka.taro@example.com"
        role:
          $ref: '#/components/schemas/UserRole'
        idVerified:
          type: boolean
          description: 本人確認済みか
          example: false
        myNumberVerified:
          type: boolean
          description: マイナンバー確認済みか
          example: false
        createdAt:
          type: string
          format: date-time
          description: 作成日時
          example: "2025-08-18T10:30:00Z"
        updatedAt:
          type: string
          format: date-time
          description: 更新日時
          example: "2025-08-18T10:30:00Z"

    UserUpdateRequest:
      type: object
      properties:
        firstname:
          type: string
          minLength: 1
          maxLength: 50
          description: 名
        lastname:
          type: string
          minLength: 1
          maxLength: 50
          description: 姓

    UserStatusDto:
      type: object
      required:
        - status
      properties:
        status:
          $ref: '#/components/schemas/UserState'

    UserRole:
      type: string
      enum:
        - USER
        - ADMIN
      description: |
        ユーザー権限
        - `USER`: 一般ユーザー
        - `ADMIN`: 管理者

    UserState:
      type: string
      enum:
        - ENABLED
        - DISABLED
      description: |
        ユーザー状態
        - `ENABLED`: 有効
        - `DISABLED`: 無効

    # ======================================
    # 🎭 キャラクター関連スキーマ
    # ======================================
    CharacterDto:
      type: object
      properties:
        id:
          type: integer
          format: int64
          description: キャラクターID
          example: 1
        name:
          type: string
          description: キャラクター名
          example: "桜花"
        profile:
          type: string
          description: プロフィール
          example: "元気で明るい性格の女の子"
        imageUrl:
          type: string
          format: uri
          description: キャラクター画像URL
          example: "https://api.sfr.tokyo/uploads/character_001.jpg"
        userId:
          type: string
          format: uuid
          description: 所有者ユーザーID
          example: "123e4567-e89b-12d3-a456-426614174000"
        status:
          $ref: '#/components/schemas/CharacterStatus'
        lifespanPoints:
          type: integer
          description: 寿命ポイント
          example: 365
        lastActive:
          type: string
          format: date-time
          description: 最終活動日時
          example: "2025-08-18T10:30:00Z"
        createdAt:
          type: string
          format: date-time
          description: 作成日時
          example: "2025-08-18T10:30:00Z"
        updatedAt:
          type: string
          format: date-time
          description: 更新日時
          example: "2025-08-18T10:30:00Z"

    CharacterStatus:
      type: string
      enum:
        - ACTIVE
        - INACTIVE
        - DECEASED
      description: |
        キャラクター状態
        - `ACTIVE`: アクティブ
        - `INACTIVE`: 非アクティブ
        - `DECEASED`: 死亡

    # ======================================
    # 🎓 学習関連スキーマ
    # ======================================
    LearningSpaceCreateDto:
      type: object
      required:
        - name
        - mode
      properties:
        name:
          type: string
          minLength: 1
          maxLength: 100
          description: 学習空間名
          example: "プログラミング基礎講座"
        mode:
          $ref: '#/components/schemas/LearningMode'

    LearningSpaceCreateResponse:
      type: object
      properties:
        id:
          type: integer
          format: int64
          description: 学習空間ID
          example: 1
        name:
          type: string
          description: 学習空間名
          example: "プログラミング基礎講座"
        mode:
          $ref: '#/components/schemas/LearningMode'
        createdAt:
          type: string
          format: date-time
          description: 作成日時
          example: "2025-08-18T10:30:00Z"

    LearningModeConfigDto:
      type: object
      properties:
        id:
          type: integer
          format: int64
          description: 設定ID
          example: 1
        spaceId:
          type: integer
          format: int64
          description: 学習空間ID
          example: 1
        mode:
          $ref: '#/components/schemas/LearningMode'
        configuration:
          type: object
          description: モード固有の設定
          additionalProperties: true

    LearningMode:
      type: string
      enum:
        - SCHOOL
        - SALON
        - FANCLUB
      description: |
        学習モード
        - `SCHOOL`: 学校形式
        - `SALON`: サロン形式
        - `FANCLUB`: ファンクラブ形式

    # ======================================
    # ⚠️ エラー関連スキーマ
    # ======================================
    ErrorResponse:
      type: object
      properties:
        error:
          type: string
          description: エラーコード
          example: "VALIDATION_ERROR"
        message:
          type: string
          description: エラーメッセージ
          example: "入力値にエラーがあります"
        details:
          type: array
          items:
            type: object
            properties:
              field:
                type: string
                description: エラーフィールド
                example: "email"
              message:
                type: string
                description: フィールド固有のエラーメッセージ
                example: "メールアドレスの形式が正しくありません"
        timestamp:
          type: string
          format: date-time
          description: エラー発生時刻
          example: "2025-08-18T10:30:00Z"
        path:
          type: string
          description: エラーが発生したAPIパス
          example: "/api/v1/auth/register"
```

---

## 📝 実装ノート

### 🔐 セキュリティ機能
- **レート制限**: IP単位で100req/min、認証エンドポイント10req/min
- **セキュリティヘッダー**: CSP, HSTS, X-Frame-Options, X-Content-Type-Options
- **アクセスログ**: 全APIアクセスの詳細ログとセキュリティイベント監視
- **攻撃検知**: SQLインジェクション、XSS、パストラバーサル等の検知

### 📊 監視・分析
- **リクエスト追跡**: 一意のリクエストIDによる追跡
- **パフォーマンス監視**: レスポンス時間とスループットの監視
- **セキュリティアラート**: 怪しいアクセスパターンの自動検知

### 🚀 開発・運用
- **バリデーション**: Bean Validationによる入力値検証
- **エラーハンドリング**: 統一されたエラーレスポンス形式
- **ログ出力**: 構造化ログによる運用効率化

---

## 🔮 今後の実装予定

### 📝 投稿・コメント機能
- **投稿管理**: `/api/posts` エンドポイント群
- **コメント管理**: `/api/comments` エンドポイント群

### 💰 経済機能
- **出納帳管理**: `/api/wallet` エンドポイント群
- **取引履歴**: 詳細な経済活動追跡

### 🎯 学習機能拡張
- **学習コンテンツ**: `/api/learning/content` エンドポイント群
- **進捗管理**: `/api/learning/progress` エンドポイント群
- **クイズ機能**: `/api/learning/quiz` エンドポイント群
- **ライブセッション**: `/api/learning/live-sessions` エンドポイント群

### 📁 ファイル管理
- **画像アップロード**: `/api/uploads` エンドポイント群
- **ファイル管理**: 複数ファイル形式対応

---

## 📚 関連ドキュメント
- [データベース設計書](./sfr_DB.md)
- [API設計書](../API設計書.docx)
- [セキュリティ仕様](../開発要件.txt)
          description: ユーザーが見つからない
    delete:
      summary: ユーザー削除
      tags:
        - users
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
          description: ユーザーID
      responses:
        '204':
          description: 削除成功
        '404':
          description: ユーザーが見つからない

  /characters:
    get:
      summary: キャラクター一覧取得
      tags:
        - characters
      security:
        - bearerAuth: []
      responses:
        '200':
          description: キャラクター一覧を返す
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/Character'
        '401':
          description: 認証エラー
    post:
      summary: キャラクター新規作成
      tags:
        - characters
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CharacterCreateRequest'
      responses:
        '201':
          description: 作成成功
        '400':
          description: リクエスト不正
  /characters/{id}:
    get:
      summary: キャラクター詳細取得
      tags:
        - characters
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
          description: キャラクターID
      responses:
        '200':
          description: キャラクター詳細を返す
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Character'
        '404':
          description: キャラクターが見つからない
    put:
      summary: キャラクター情報更新
      tags:
        - characters
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
          description: キャラクターID
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CharacterUpdateRequest'
      responses:
        '200':
          description: 更新成功
        '400':
          description: リクエスト不正
        '404':
          description: キャラクターが見つからない
    delete:
      summary: キャラクター削除
      tags:
        - characters
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
          description: キャラクターID
      responses:
        '204':
          description: 削除成功
        '404':
          description: キャラクターが見つからない

  /teams:
    get:
      summary: チーム一覧取得
      tags:
        - teams
      security:
        - bearerAuth: []
      responses:
        '200':
          description: チーム一覧を返す
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/Team'
        '401':
          description: 認証エラー
    post:
      summary: チーム新規作成
      tags:
        - teams
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/TeamCreateRequest'
      responses:
        '201':
          description: 作成成功
        '400':
          description: リクエスト不正
  /teams/{id}:
    get:
      summary: チーム詳細取得
      tags:
        - teams
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
          description: チームID
      responses:
        '200':
          description: チーム詳細を返す
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Team'
        '404':
          description: チームが見つからない
    put:
      summary: チーム情報更新
      tags:
        - teams
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
          description: チームID
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/TeamUpdateRequest'
      responses:
        '200':
          description: 更新成功
        '400':
          description: リクエスト不正
        '404':
          description: チームが見つからない
    delete:
      summary: チーム削除
      tags:
        - teams
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
          description: チームID
      responses:
        '204':
          description: 削除成功
        '404':
          description: チームが見つからない

  /products:
    get:
      summary: 商品一覧取得
      tags:
        - products
      security:
        - bearerAuth: []
      responses:
        '200':
          description: 商品一覧を返す
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/Product'
        '401':
          description: 認証エラー
    post:
      summary: 商品新規作成
      tags:
        - products
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ProductCreateRequest'
      responses:
        '201':
          description: 作成成功
        '400':
          description: リクエスト不正
  /products/{id}:
    get:
      summary: 商品詳細取得
      tags:
        - products
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
          description: 商品ID
      responses:
        '200':
          description: 商品詳細を返す
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Product'
        '404':
          description: 商品が見つからない
    put:
      summary: 商品情報更新
      tags:
        - products
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
          description: 商品ID
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ProductUpdateRequest'
      responses:
        '200':
          description: 更新成功
        '400':
          description: リクエスト不正
        '404':
          description: 商品が見つからない
    delete:
      summary: 商品削除
      tags:
        - products
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
          description: 商品ID
      responses:
        '204':
          description: 削除成功
        '404':
          description: 商品が見つからない

  /forums:
    get:
      summary: フォーラム一覧取得
      tags:
        - forums
      security:
        - bearerAuth: []
      responses:
        '200':
          description: フォーラム一覧を返す
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/Forum'
        '401':
          description: 認証エラー
    post:
      summary: フォーラム新規作成
      tags:
        - forums
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ForumCreateRequest'
      responses:
        '201':
          description: 作成成功
        '400':
          description: リクエスト不正
  /forums/{id}:
    get:
      summary: フォーラム詳細取得
      tags:
        - forums
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
          description: フォーラムID
      responses:
        '200':
          description: フォーラム詳細を返す
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Forum'
        '404':
          description: フォーラムが見つからない
    put:
      summary: フォーラム更新
      tags:
        - forums
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
          description: フォーラムID
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ForumUpdateRequest'
      responses:
        '200':
          description: 更新成功
        '400':
          description: リクエスト不正
        '404':
          description: フォーラムが見つからない
    delete:
      summary: フォーラム削除
      tags:
        - forums
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
          description: フォーラムID
      responses:
        '204':
          description: 削除成功
        '404':
          description: フォーラムが見つからない

  /comments:
    get:
      summary: コメント一覧取得
      tags:
        - comments
      security:
        - bearerAuth: []
      responses:
        '200':
          description: コメント一覧を返す
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/Comment'
        '401':
          description: 認証エラー
    post:
      summary: コメント投稿
      tags:
        - comments
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CommentCreateRequest'
      responses:
        '201':
          description: 投稿成功
        '400':
          description: リクエスト不正
  /comments/{id}:
    get:
      summary: コメント詳細取得
      tags:
        - comments
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
          description: コメントID
      responses:
        '200':
          description: コメント詳細を返す
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Comment'
        '404':
          description: コメントが見つからない
    put:
      summary: コメント更新
      tags:
        - comments
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
          description: コメントID
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CommentUpdateRequest'
      responses:
        '200':
          description: 更新成功
        '400':
          description: リクエスト不正
        '404':
          description: コメントが見つからない
    delete:
      summary: コメント削除
      tags:
        - comments
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
          description: コメントID
      responses:
        '204':
          description: 削除成功
        '404':
          description: コメントが見つからない

components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
      description: JWT認証トークンを使用
  schemas:
    User:
      type: object
      properties:
        id:
          type: string
          description: ユーザーID
        name:
          type: string
          description: ユーザー名
        email:
          type: string
          format: email
          description: メールアドレス
    UserCreateRequest:
      type: object
      properties:
        name:
          type: string
        email:
          type: string
          format: email
    UserUpdateRequest:
      type: object
      properties:
        name:
          type: string
        email:
          type: string
          format: email
    Character:
      type: object
      properties:
        id:
          type: string
        name:
          type: string
        type:
          type: string
    CharacterCreateRequest:
      type: object
      properties:
        name:
          type: string
        type:
          type: string
    CharacterUpdateRequest:
      type: object
      properties:
        name:
          type: string
        type:
          type: string
    Team:
      type: object
      properties:
        id:
          type: string
        name:
          type: string
        description:
          type: string
    TeamCreateRequest:
      type: object
      properties:
        name:
          type: string
        description:
          type: string
    TeamUpdateRequest:
      type: object
      properties:
        name:
          type: string
        description:
          type: string
    Product:
      type: object
      properties:
        id:
          type: string
        name:
          type: string
        price:
          type: number
        currency:
          type: string
    ProductCreateRequest:
      type: object
      properties:
        name:
          type: string
        price:
          type: number
        currency:
          type: string
    ProductUpdateRequest:
      type: object
      properties:
        name:
          type: string
        price:
          type: number
        currency:
          type: string
    Forum:
      type: object
      properties:
        id:
          type: string
        title:
          type: string
        description:
          type: string
    ForumCreateRequest:
      type: object
      properties:
        title:
          type: string
        description:
          type: string
    ForumUpdateRequest:
      type: object
      properties:
        title:
          type: string
        description:
          type: string
    Comment:
      type: object
      properties:
        id:
          type: string
        content:
          type: string
        authorId:
          type: string
    CommentCreateRequest:
      type: object
      properties:
        content:
          type: string
        authorId:
          type: string
    CommentUpdateRequest:
      type: object
      properties:
        content:
          type: string
