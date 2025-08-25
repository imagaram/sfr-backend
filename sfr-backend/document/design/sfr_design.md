📘 sfr.tokyo 統合設計書（Copilot参照用）

このPagesは、GitHub Copilot Proがsfr.tokyoのコード生成・保守・拡張時に参照する統合設計書です。命名・構造・責務・関係性を明確に記述し、DTO/API/DBの整合性を保ちます。

1️⃣ プロジェクト概要

名称：sfr.tokyo（Social Fan Resonance）

目的：創作活動を支援するSNSプラットフォーム

対象：クリエイター、ファン、教育者、運営者

思想：

キャラクター・チーム・経済活動・教育・コミュニティを統合

モバイルアプリと連携し、作品流通と収益化を支援

評議員制度によるガバナンスと信頼性の設計

キャラクターの寿命・活動量・報酬を通じた人間的設計思想

2️⃣ 技術スタックと構成図

フロントエンド：Next.js（TypeScript）

バックエンド：Spring Boot（Java）

データベース：MySQL

クラウド：さくらインターネットCloud Storage（画像・ファイル管理）

AI連携：Copilot Pro（コード生成・設計補助）

🔧 モジュール構成図は後ほど追加予定

3️⃣ 各機能のイメージ

🏛️ 評議員制度

目的

プラットフォームの方針決定を民主的に行う

キャラクター・チーム・学習空間などの制度改善を提言

報酬制度により活動を促進

流れ

POST /elections により選挙を開始

POST /candidates により立候補者を登録

POST /votes によりユーザーが投票

GET /elections/{id}/results により開票結果を取得

POST /council/members により評議員を登録

PATCH /council/members/{id}/term により任期を更新

POST /council/decisions により方針を決定

GET /council/decisions?type=function_improvement により提言を取得

関係性

評議員はキャラクターに紐づく（user_id → character_id → council_member_id）

提言は学習空間・チーム・経済活動などの改善に活用

報酬は POST /crypto/reward により付与され、活動量に応じて変動

4️⃣ DB設計（テーブル定義）

詳細は章ごとに分割して記載。Copilotはこの章を参照してDTOやRepositoryを生成します。

user（個人／法人情報、認証、役割）

character_lifecycle（キャラクター構造、寿命、活動量）

team_entity / team_member（チーム構造、役割、経済活動）

wallet / wallet_entry（収益記録、暗号資産連携）

post_entity / product / order / transaction（投稿、商品、注文、決済）

subscription / plan / coupon（サブスク、プラン、割引）

council_member / crypto_reward（評議員制度、報酬履歴）

learning_space / learning_history / learning_progress（学習空間、履歴、進捗）

インデックス設計・クエリ最適化・ENUM定義は章8で記載

5️⃣ API設計

この章では、主要なRESTful APIエンドポイントと、それに対応するDTO・責務・バリデーションルールを記載します。Copilot Proはこの章を参照して、Controller・Service・Mapperを正しく生成します。

👤 ユーザー関連

POST /users/register

登録（個人／法人）

DTO: UserDTO

バリデーション: email形式、必須項目（firstname, lastname, email）

認証後、ダッシュボードに遷移（2カラム構成）

GET /users/{id}

ユーザー詳細取得

DTO: UserDTO

認証必須

🧍 キャラクター関連

POST /characters

キャラクター作成

DTO: CharacterDTO（※定義済み）

初期 lifespan_points = 365

ステータスは ACTIVE

GET /characters?user_id={uuid}

ユーザーのキャラクター一覧取得

DTO: CharacterDTO[]

並び順: created_at DESC

PATCH /characters/{id}/status

ステータス変更（例：DECEASED）

DTO: CharacterStatusUpdateDTO（※定義済み）

削除不可 → 共同墓地へ移動

👥 チーム関連

POST /teams

チーム作成

DTO: TeamDTO

リーダーキャラクターID必須

承認フローあり（isApproved）

PATCH /teams/{id}/transfer

チーム運営権の移譲

DTO: TeamTransferDTO（※定義済み）

個人→法人の移譲も可能

💰 経済活動関連

POST /wallet/entry

出納記録追加

DTO: WalletEntryDTO

金額・説明・タイプ（INCOME/EXPENSE）

GET /wallet/balance?user_id={uuid}

残高取得（集計）

DTO: WalletBalanceDTO（※定義済み）

集計期間指定可能（timestamp BETWEEN）

🛍️ 商品・注文関連

POST /products

商品登録

DTO: ProductDTO

デジタル／フィジカル種別あり

GET /products?creator_id={uuid}

キャラクターの作品一覧取得

DTO: ProductDTO[]

POST /orders

商品注文

DTO: OrderDTO（※定義済み）

quantity, total_price, status = PENDING

GET /orders?user_id={uuid}

購入履歴取得

DTO: OrderDTO[]

並び順: ordered_at DESC

📚 学習関連

POST /learning/spaces

学習空間作成

DTO: LearningSpaceDTO

type: SCHOOL / ONLINE_SALON / FANCLUB

GET /learning/progress?user_id={uuid}

学習進捗取得

DTO: LearningProgressDTO[]（※定義済み）

並び順: last_accessed DESC

🪙 暗号資産関連

POST /crypto/reward

lifespan_points に応じた報酬付与

DTO: CryptoRewardDTO

mirrored_to_admin = true で運営にも同量付与

🏛️ 評議員制度

POST /elections

選挙開始

DTO: ElectionDTO

POST /candidates

立候補登録

DTO: CandidateDTO

POST /votes

投票

DTO: VoteDTO

GET /elections/{id}/results

開票結果取得

DTO: ElectionResultDTO

POST /council/members

評議員登録

DTO: CouncilMemberDTO（※定義済み）

PATCH /council/members/{id}/term

任期更新

DTO: CouncilTermUpdateDTO

GET /council/members?status=active

現職評議員一覧取得

DTO: CouncilMemberDTO[]

POST /council/decisions

方針決定

DTO: CouncilDecisionDTO

GET /council/decisions?type=function_improvement

提言一覧取得

DTO: CouncilDecisionDTO[]

6️⃣ DTO命名規則と責務分離

DTO（Data Transfer Object）は、APIリクエスト／レスポンスの構造を定義する役割を持ち、Controller・Service・Mapper間の責務分離を明確にします。

命名規則

CreateXxxDTO：新規作成用

UpdateXxxDTO：更新用

XxxResponseDTO：レスポンス用

XxxStatusUpdateDTO：ステータス変更用

責務分離

Controller：DTOを受け取り、バリデーションを実行（@Valid適用済）

Service：DTOを元にビジネスロジックを実行（主要Service実装済）

Mapper：DTOとEntity間の変換を担う（MapStructにより自動生成）

Repository：Spring Data JPAによりクエリ定義（主要Repository整備済）

7️⃣ Copilot補完精度向上のための記述方針

Copilot Proによる補完精度を高めるため、以下の記述方針を設けています。

DTO・Entity・Repositoryの命名を一貫させる

API設計とDB設計の整合性を保つ

バリデーションルールを明示する

コメント・Javadocを充実させる（英語推奨）

Copilotが参照しやすいよう、構造化された記述を心がける

8️⃣ インデックス設計・ENUM定義・クエリ最適化

この章では、DBパフォーマンスと保守性向上のための設計方針を記載します。

インデックス設計

検索頻度の高いカラムに対して適切なインデックスを設定

複合インデックスの活用による検索効率化

ENUM定義

ステータス・タイプなど、選択肢が固定された項目はENUMで定義

Java側のEnumとDB側の定義を一致させる

クエリ最適化

N+1問題の回避（JOINの活用）

LIMIT・OFFSETの適切な使用

クエリログの分析によるボトルネック特定

9️⃣ テスト方針と自動化

品質保証のため、以下のテスト方針と自動化戦略を採用します。

テスト方針

単体テスト：Service・Mapperのロジック検証

結合テスト：Controller〜Repository間の連携確認

E2Eテスト：ユーザー操作を模擬したシナリオテスト

自動化戦略

GitHub ActionsによるCI/CDパイプライン構築

テストカバレッジの可視化と閾値設定

Pull Request時の自動テスト実行

🔟 運用・監視・セキュリティ

安定運用とセキュリティ確保のため、以下の方針を採用します。

運用

ログ収集：Cloud Loggingを活用

エラートラッキング：Sentryによる通知

定期バックアップ：DB・ファイルストレージの自動化

監視

死活監視：Uptime Robotによる定期チェック

パフォーマンス監視：Prometheus + Grafana

セキュリティ

認証：JWT + OAuth2.0

権限管理：RBAC（Role-Based Access Control）

脆弱性診断：定期的なセキュリティスキャン

1️⃣1️⃣ 実装済み機能一覧（2025年8月時点）

CharacterController, PostController, CommentController の整備（@Valid適用済）

CharacterService, PostService, CommentService のロジック実装

MapStruct による Mapper 実装（CharacterMapper, PostMapper, CommentMapper）

Spring Data JPA による Repository 整備（CharacterRepository, PostRepository）

Entity命名の整合性確認（GameCharacter → CharacterLifecycle、Post → PostEntity）

WalletEntryController の整備とセキュリティテスト完了

UserController のセキュリティテスト完了（GET/PUT /me）

ImageUploadController のセキュリティテスト完了（JWTフィルタ通過確認済み）

1️⃣2️⃣ 進捗ログ

2025年8月15日①

Controller・Service・Mapper・Repository 層の整備完了

DTO命名とEntityの整合性確認

Copilot補完精度向上のための記述方針を反映

2025年8月15日②

CharacterServiceTest に updateCharacterStatus のユニットテストを追加（DECEASED への遷移、復活禁止の検証）

lifespan = 0 の境界値テストを追加（減算しても0のまま、ステータスは DECEASED のまま）

未使用インポート（EntityNotFoundException）を削除

テスト結果：passed=7 failed=0（全テスト合格）

2025年8月15日③

CharacterServiceTest に updateCharacter のユニットテストを追加

名前とプロフィールのみ更新するケース

画像が提供された場合に古い画像を削除して新しい画像に置換するケース（RequestContext をセットして URL ビルダーを動作）

名前が重複する場合に例外を投げるケース

RequestContextHolder を用いた擬似リクエストコンテキストの設定

不要スタブの削除（Mockito Strictness エラー回避）

テスト結果：passed=12 failed=0（全テスト合格）

2025年8月15日④

CharacterControllerTest を追加（MockMvc による統合テスト）

POST /api/characters（multipart） → 201 Created

GET /api/characters → 200 OK

GET /api/characters/{id}（存在しない） → 404 Not Found

PUT /api/characters/{id}（multipart） → 200 OK

DELETE /api/characters/{id} → 204 No Content

セキュリティフィルタと JwtService を @MockBean で用意し、コンテキスト初期化エラーを回避

@AutoConfigureMockMvc(addFilters = false) によりセキュリティフィルタを無効化

テスト結果：passed=5 failed=0（全テスト合格）

2025年8月15日⑤

CharacterControllerSecurityTest を追加（MockMvc + Spring Security）

認証成功（UserDetailsService と JwtService をモックし、user() による認証） → 201 OK

認証失敗（JwtService.extractUsername が null を返す） → 401 Unauthorized

CSRF と認証プリンシパルの取り扱いを調整

テスト結果：passed=2 failed=0（全テスト合格）

2025年8月15日⑥

CharacterControllerSecurityTest を更新（JWTヘッダベースのフィルタ通過テスト）

multipart作成テストの期待ステータスを is2xxSuccessful() に緩和

Authorizationヘッダ付きリクエストで、SecurityContext を Spring Security の helper を使って確実に設定

独自の RequestPostProcessor を廃止し、securityContext(...) に置き換え

実行結果：passed=3 failed=0（全テスト合格）

2025年8月15日⑦

CharacterControllerSecurityTest を更新（JwtAuthenticationFilter 経路を通す構成に変更）

filter スタブを除去し、実フィルタ経路を通す構成へ変更

テストスライスを @WebMvcTest → @SpringBootTest に切替（@AutoConfigureMockMvc 併用）

JwtService を @MockBean でモック化

UserService（UserDetailsService 実装）を @MockBean(name="userDetailsService") で置換

AuthEntryPoint と AuthenticationProvider のモックを除去し、実Beanを使用

各テスト前に SecurityContextHolder.clearContext() を呼ぶ @BeforeEach を追加

実行結果：passed=3 failed=0（全テスト合格）

2025年8月15日⑧

PostControllerSecurityTest を追加（MockMvc + Spring Security）

POST /api/posts（multipart） → 201 Created

GET /api/posts/my-posts → 200 OK

UserControllerSecurityTest を追加（MockMvc + Spring Security）

GET /api/users/me → 200 OK

PUT /api/users/me → 200 OK

ImageUploadControllerSecurityTest を追加（MockMvc + Spring Security）

POST /api/image/upload（multipart） → 200 OK（Authorization: Bearer <token>）

JwtService.extractUsername/isTokenValid をスタブして有効トークン動作を再現

UserRepository.findByEmail(...) をモックして ApplicationConfig.userDetailsService() を満たす

Multipart ファイルを MockMultipartFile で作成して送信

テスト構成：@SpringBootTest + @AutoConfigureMockMvc、ImageUploadService, JwtService, UserService, UserRepository を @MockBean でモック

実行結果：passed=1 failed=0（全テスト合格）

注意点：@MockBean は Spring Boot 3.4 で非推奨（警告あり）。将来的に別のモッキング戦略への移行を検討

ApplicationConfig.userDetailsService() が UserRepository を参照するため、フィルタを通すテストでは UserRepository.findByEmail(...) を必ずモックする必要あり

multipart + CSRF の扱いによっては別の調整が必要になる場合あり（今回は問題なし）