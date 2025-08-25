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

3️⃣ DB設計（テーブル定義）

詳細は章ごとに分割して記載。Copilotはこの章を参照してDTOやRepositoryを生成します。

user（個人／法人情報、認証、役割）

character_lifecycle（キャラクター構造、寿命、活動量）

team_entity / team_member（チーム構造、役割、経済活動）

wallet / wallet_entry（収益記録、暗号資産連携）

post_entity / product / order / transaction（投稿、商品、注文、決済）

subscription / plan / coupon（サブスク、プラン、割引）

council_member / crypto_reward（評議員制度、報酬履歴）

learning_space / learning_history / learning_progress（学習空間、履歴、進捗）

インデックス設計・クエリ最適化・ENUM定義は章7で記載

4️⃣ API設計

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

DTO: CharacterDTO（※定義予定）

初期 lifespan_points = 365

ステータスは ACTIVE

GET /characters?user_id={uuid}

ユーザーのキャラクター一覧取得

DTO: CharacterDTO[]

並び順: created_at DESC

PATCH /characters/{id}/status

ステータス変更（例：DECEASED）

DTO: CharacterStatusUpdateDTO（※定義予定）

削除不可 → 共同墓地へ移動

👥 チーム関連

POST /teams

チーム作成

DTO: TeamDTO

リーダーキャラクターID必須

承認フローあり（isApproved）

PATCH /teams/{id}/transfer

チーム運営権の移譲

DTO: TeamTransferDTO（※定義予定）

個人→法人の移譲も可能

💰 経済活動関連

POST /wallet/entry

出納記録追加

DTO: WalletEntryDTO

金額・説明・タイプ（INCOME/EXPENSE）

GET /wallet/balance?user_id={uuid}

残高取得（集計）

DTO: WalletBalanceDTO（※定義予定）

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

DTO: OrderDTO（※定義予定）

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

DTO: LearningProgressDTO[]（※定義予定）

並び順: last_accessed DESC

🪙 暗号資産関連

POST /crypto/reward

lifespan_points に応じた報酬付与

DTO: CryptoRewardDTO

mirrored_to_admin = true で運営にも同量付与

🏛️ ガバナンス関連

POST /council/members

評議員登録

DTO: CouncilMemberDTO（※定義予定）

任期開始日・担当領域（ENUM）

GET /council/members?status=active

現職評議員一覧取得

DTO: CouncilMemberDTO[]

🔧 DTO定義は章6、ENUM定義は章5、DBとの整合性は章3を参照

5️⃣ ENUM定義一覧

この章では、sfr.tokyoで使用されるすべてのENUM型を一元管理します。Copilot Proはこの章を参照して、DTO・Entity・Controller・Service層の条件分岐や状態管理を正しく補完します。

👤 ユーザー関連

Role

USER：一般ユーザー

ADMIN：管理者

使用箇所：user.role, UserDTO

🧍 キャラクター関連

CharacterStatus

ACTIVE：活動中

INACTIVE：非活動

DECEASED：死亡（共同墓地へ移動）

使用箇所：character_lifecycle.status, CharacterDTO

👥 チーム関連

TeamRole

ARTIST：創作担当

STAFF：運営・補助担当

使用箇所：team_member.role

EconomicRole

SHOP_OWNER：ショップ運営者

TEACHER：教育者

MENTOR：指導者

NONE：役割なし

使用箇所：team_member.economic_role

TeamType

LITERATURE：文学

MUSIC：音楽

VISUAL_ART：視覚芸術

PERFORMANCE：パフォーマンス

OTHER：その他

使用箇所：team_entity.type

💰 経済活動関連

TransactionType

INCOME：収入

EXPENSE：支出

使用箇所：wallet_entry.type, WalletEntryDTO

ProductType

DIGITAL：デジタル商品

PHYSICAL：フィジカル商品

使用箇所：product.type

ProductStatus

PUBLIC：公開中

PRIVATE：非公開

ARCHIVED：アーカイブ済み

使用箇所：product.status

OrderStatus

PENDING：未処理

COMPLETED：完了

CANCELLED：キャンセル

使用箇所：order.status

PaymentMethod

CREDIT_CARD

BANK_TRANSFER

CRYPTO

PAYPAL

使用箇所：transaction.payment_method

📚 学習関連

LearningSpaceType

SCHOOL：学校

ONLINE_SALON：オンラインサロン

OFFICIAL_FANCLUB：公式ファンクラブ

使用箇所：learning_space.type

CompletionStatus

COMPLETED：完了

IN_PROGRESS：進行中

FAILED：未達成

使用箇所：learning_progress.completion_status

🏛️ ガバナンス関連

CouncilRole

MEDIATION：仲裁

PROPOSAL：提案

ARBITRATION：裁定

使用箇所：council_member.role

📦 サブスク関連（予定）

SubscriptionStatus（拡張予定）

ACTIVE：契約中

CANCELLED：キャンセル済み

EXPIRED：期限切れ

使用箇所：subscription.status（ENUM定義はDB設計書に未記載）

🔧 DTO・Entity・API設計との整合性は章6・章4・章3を参照

6️⃣ DTO命名規則

この章では、sfr.tokyoで使用されるDTOの命名ルール・責務・対応テーブル・使用箇所を記載します。Copilot Proはこの章を参照して、DTOの生成・Mapper・Service層との整合性を保ちます。

🧩 命名ルール

DTO名はCamelCaseで、末尾に DTO を付ける（例：UserDTO）

DTO名はエンドポイントの機能に基づき、明確かつ一貫性のある命名を行う

DTOのフィールド名はDB設計書のカラム名と一致させる

DTOの型はDB設計書の型と整合性を保つ

DTOのバリデーションルールはAPI設計書と一致させる

👤 ユーザー関連

UserDTO

対応テーブル：user

使用箇所：登録・取得・認証後の表示

フィールド例：id, firstname, lastname, email, role, idVerified, myNumberVerified

🧍 キャラクター関連

CharacterDTO（定義予定）

対応テーブル：character_lifecycle

使用箇所：作成・一覧取得・ステータス変更

フィールド例：characterId, name, status, lifespanPoints, description

CharacterStatusUpdateDTO（定義予定）

使用箇所：PATCH /characters/{id}/status

フィールド例：status（ENUM）

👥 チーム関連

TeamDTO

対応テーブル：team_entity

使用箇所：作成・移譲・表示

フィールド例：id, name, description, leaderCharacterId, memberCharacterIds, isApproved, isTransferred

TeamTransferDTO（定義予定）

使用箇所：PATCH /teams/{id}/transfer

フィールド例：newOwnerId, consentFlag

💰 経済活動関連

WalletEntryDTO

対応テーブル：wallet_entry

使用箇所：出納記録追加

フィールド例：id, teamId, amount, description, transactionType

WalletBalanceDTO（定義予定）

使用箇所：GET /wallet/balance

フィールド例：walletId, totalBalance

🛍️ 商品・注文関連

ProductDTO

対応テーブル：product

使用箇所：登録・一覧取得

フィールド例：id, name, description, price, creatorId, isAvailable

OrderDTO（定義予定）

対応テーブル：order

使用箇所：注文・履歴取得

フィールド例：id, userId, productId, quantity, totalPrice, status, orderedAt

📚 学習関連

LearningSpaceDTO

対応テーブル：learning_space

使用箇所：空間作成・表示

フィールド例：id, title, description, creatorId, isActive

LearningProgressDTO（定義予定）

対応テーブル：learning_progress

使用箇所：進捗取得

フィールド例：id, userId, learningSpaceId, completedPercent, badges, lastAccessed

🪙 暗号資産関連

CryptoRewardDTO

対応テーブル：crypto_reward

使用箇所：報酬付与

フィールド例：id, ownerId, rewardAmount, rewardType

🏛️ ガバナンス関連

CouncilMemberDTO（定義予定）

対応テーブル：council_member

使用箇所：登録・一覧取得

フィールド例：id, userId, termStart, termEnd, role, notes

🔧 DTOとEntityの変換はMapperで行い、Service層で責務を分離します🔧 DTOとAPI設計の整合性は章4、DB設計との整合性は章3を参照

7️⃣ クエリ最適化とインデックス

この章では、sfr.tokyoで頻繁に使用されるクエリと、それに対応するインデックス設計を記載します。Copilot Proはこの章を参照して、Repository層のクエリ補完・パフォーマンス改善を行います。

📌 インデックス追加推奨カラム一覧

| テーブル名 | カラム名 | 用途例 | 備考 | | --- | --- | --- | --- | | user | email | ログイン、ユーザー検索 | UNIQUE INDEX 済み | | character_lifecycle | user_id, status | キャラ一覧取得、状態フィルタ | user_id は外部キー | | post_entity | character_id, created_at | 投稿タイムライン表示 | 並び順の基本軸 | | order | user_id, ordered_at | 購入履歴、期間検索 | 複合インデックス推奨 | | wallet_entry | wallet_id, timestamp | 月別収支分析 | 金融系処理では必須 | | subscription | user_id, status | 契約状態管理 | 多数ユーザー同時参照時 | | learning_progress | user_id, learning_space_id | 学習進捗取得・集計 | JSON分析の補助にもなる |

🔍 よく使われるクエリ例と最適化案

ユーザー情報取得（ログイン・プロフィール表示）

SELECT * FROM user WHERE email = ?;

最適化：email に UNIQUE INDEX

キャラクター一覧取得（マイキャラクター表示）

SELECT * FROM character_lifecycle WHERE user_id = ? AND status = 'ACTIVE';

最適化：user_id, status に INDEX

投稿一覧取得（キャラ投稿のタイムライン表示）

SELECT content, created_at FROM post_entity WHERE character_id = ? ORDER BY created_at DESC LIMIT 10;

最適化：character_id, created_at に複合 INDEX

購入履歴取得（自己購入履歴／管理画面分析）

SELECT * FROM order WHERE user_id = ? ORDER BY ordered_at DESC;

最適化：user_id, ordered_at に複合 INDEX

出納帳分析（月別入出金グラフ表示など）

SELECT wallet_id, SUM(amount) FROM wallet_entry WHERE wallet_id = ? AND timestamp BETWEEN ? AND ? GROUP BY wallet_id;

最適化：wallet_id, timestamp に複合 INDEX

サブスク契約状態取得（契約中の確認）

SELECT plan_id, status FROM subscription WHERE user_id = ? AND status = 'ACTIVE';

最適化：user_id, status に INDEX

学習進捗取得（ダッシュボード表示）

SELECT learning_space_id, completed_percent FROM learning_progress WHERE user_id = ? ORDER BY last_accessed DESC LIMIT 5;

最適化：user_id, learning_space_id に INDEX

🧠 クエリ最適化の設計指針

LIMIT：件数制限でDB負荷軽減

ORDER：並び順指定で効率的取得

JOIN：必要なデータを結合取得

INDEX：WHERE句・ORDER句に合わせて設計

キャッシュ：頻繁なクエリ結果を保持

非正規化：残高など即時取得用に検討

バッチ処理：月次集計などに活用

🔧 DTOとの整合性は章6、API設計との対応は章4、DB構造は章3を参照

8️⃣ Copilot活用ガイド

この章では、GitHub Copilot Proがこの統合設計書を最大限活用するための参照方法・指示例・章ごとの役割を記載します。開発者がCopilotに対して明確な文脈を与えることで、コード生成の精度と一貫性が向上します。

🧭 参照の基本方針

CopilotはこのPages全体を文脈として参照する

各章は責務ごとに分離されており、目的に応じて参照すべき章が異なる

指示は明確・具体的・章番号付きで行うと効果的

📌 章ごとの参照ガイド

| 目的 | 参照すべき章 | 指示例 | | --- | --- | --- | | DTOの生成 | 章3（DB設計）＋章6（DTO命名） | 「UserDTOをDB設計に基づいて生成して」 | | Controllerの生成 | 章4（API設計）＋章6（DTO）＋章5（ENUM） | 「POST /characters に対応するControllerを生成して」 | | Service層の責務分離 | 章6（DTO）＋章4（API設計） | 「TeamDTOを使ったService層の責務分離を提案して」 | | Repositoryクエリ補完 | 章7（クエリ最適化）＋章3（DB設計） | 「キャラクター一覧取得用のRepositoryメソッドを生成して」 | | 状態管理・条件分岐 | 章5（ENUM定義） | 「OrderStatusを使った条件分岐コードを提案して」 | | テストコード生成 | 章6（DTO）＋章4（API設計） | 「CharacterDTOに対する単体テストコードを生成して」 | | 拡張設計の検討 | 章9（拡張予定）＋章1（思想） | 「coupon_usageテーブルを追加する設計案を提案して」 |

🧠 指示のコツ

「この設計書に基づいて…」という前置きを使うと、Copilotが文脈を正しく認識しやすい

DTO名・エンドポイント・テーブル名は正確に記述する

複数章をまたぐ指示（例：DTO＋API＋DB）には、章番号を添えると精度が上がる

「責務を分離して」「バリデーションを含めて」「Mapperも生成して」など、粒度の指定が効果的

🧪 よく使う指示テンプレート

この統合設計書に基づいて、CharacterDTOを使ったControllerとService層を生成してください。 DTO定義は章6、DB構造は章3、API設計は章4を参照してください。

TeamDTOに対応するMapperクラスを生成してください。命名規則は章6、フィールド構造は章3を参照。 POST /orders に対応するOrderDTOのバリデーション付きControllerを生成してください。

--- 🔧 CopilotはこのPagesを「設計思想の保存庫」として扱います。更新・拡張があれば章9に記載してください。 ---

9️⃣ 拡張予定と注意点

この章では、今後追加予定の機能・テーブル・ENUM・設計方針を記録します。Copilotはこの章を参照することで、将来的な拡張に備えたコード生成や設計提案が可能になります。

📐 拡張予定テーブル

| テーブル名 | 目的 | 備考 | | --- | --- | --- | | coupon_usage | クーポンの使用履歴を記録 | user_id, coupon_id, used_at を含む | | creator_profile | クリエイターの詳細プロフィール | SNSリンク、自己紹介、活動ジャンルなど | | fan_activity_log | ファンの活動履歴 | いいね、コメント、投稿閲覧などを記録 | | governance_vote | ガバナンス投票履歴 | council_id, voter_id, vote_type, timestamp |

🧬 拡張予定フィールド

| 対象DTO | 追加フィールド | 型 | 備考 | | --- | --- | --- | --- | | UserDTO | is_verified | boolean | 認証済みユーザーかどうか | | CharacterDTO | popularity_score | int | 人気度スコア（集計用） | | TeamDTO | emblem_url | string | チームのエンブレム画像URL |

🧾 拡張予定ENUM

| ENUM名 | 値 | 用途 | | --- | --- | --- | | VoteType | YES, NO, ABSTAIN | ガバナンス投票 | | ActivityType | LIKE, COMMENT, VIEW | ファン活動ログ | | VerificationStatus | UNVERIFIED, PENDING, VERIFIED | ユーザー認証状態 |

🧠 設計方針（拡張時）

新規テーブルは章3の命名規則・構造に準拠すること

DTO拡張時は章6の命名・責務分離ルールを踏襲すること

ENUM追加時は章5に定義し、Controller/Serviceでの使用例も記載すること

拡張設計はこの章に記録し、Copilotが未来の文脈として参照できるようにする

🧪 Copilotへの指示例（拡張対応）

この設計書に基づいて、coupon_usageテーブルを追加するDB定義とDTOを生成してください。 章3と章9を参照してください。

VoteType ENUMを使った投票Controllerを生成してください。章5と章9を参照。creator_profileテーブルに対応するDTOとMapperを生成してください。章3と章6と章9を参照。

🧭 拡張設計は随時この章に追加してください。Copilotはこの章を「未来の文脈」として扱います