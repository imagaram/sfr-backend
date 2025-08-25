# 🗄️ SFR.TOKYO データベース設計書

**最終更新日**: 2025年8月18日  
**バージョン**: 2.0  

## 📋 概要

このドキュメントは、SFR.TOKYO プラットフォームのデータベース設計について記載しています。
実装済みのエンティティとテーブル構造を基に更新されています。

---

## 📊 実装済みテーブル一覧

### 🔐 認証・ユーザー管理

#### ユーザーテーブル：`_user`

| カラム名 | 型 | 制約 | 説明 |
|---------|-----|------|------|
| id | UUID | PRIMARY KEY | ユーザー識別子 |
| firstname | VARCHAR | NOT NULL | 名 |
| lastname | VARCHAR | NOT NULL | 姓 |
| email | VARCHAR | UNIQUE | メールアドレス |
| password | VARCHAR | NOT NULL | パスワード（ハッシュ化） |
| status | ENUM('ACTIVE', 'INACTIVE', 'PENDING') | NOT NULL | アカウント状態 |
| role | ENUM('USER', 'ADMIN') | DEFAULT 'USER' | 権限区分 |
| state | ENUM('ENABLED', 'DISABLED') | NOT NULL | ユーザー状態 |
| id_verified | BOOLEAN | DEFAULT FALSE | 運転免許証認証済みか |
| my_number_verified | BOOLEAN | DEFAULT FALSE | マイナンバー認証済みか |

### 🎭 キャラクター管理

#### キャラクターテーブル：`character_lifecycle`

| カラム名 | 型 | 制約 | 説明 |
|---------|-----|------|------|
| id | BIGINT | PRIMARY KEY AUTO_INCREMENT | キャラクター識別子 |
| user_id | UUID | FOREIGN KEY → _user.id | 所有ユーザー |
| name | VARCHAR | UNIQUE NOT NULL | キャラクター名 |
| status | ENUM('ACTIVE', 'INACTIVE', 'DECEASED') | NOT NULL | 生存状態 |
| lifespan_points | INT | DEFAULT 365 | 寿命ポイント |
| last_active | DATETIME | NULLABLE | 最終活動日時 |
| created_at | DATETIME | NOT NULL | キャラクター誕生日 |
| updated_at | DATETIME | NOT NULL | 更新日時 |
| description | TEXT | NULLABLE | キャラ説明文 |
| image_url | TEXT | NULLABLE | キャラクター画像URL |

### 💰 経済・出納管理

#### 出納帳テーブル：`wallet_entry`

| カラム名 | 型 | 制約 | 説明 |
|---------|-----|------|------|
| id | BIGINT | PRIMARY KEY AUTO_INCREMENT | 出納記録ID |
| team_id | BIGINT | NOT NULL | チームID |
| amount | DOUBLE | NOT NULL | 金額 |
| description | VARCHAR | NOT NULL | 内容説明 |
| transaction_type | ENUM('INCOME', 'EXPENSE') | NOT NULL | 収入／支出 |
| timestamp | DATETIME | NOT NULL | 記録日時 |

### 📝 投稿・コメント管理

#### 投稿テーブル：`post`

| カラム名 | 型 | 制約 | 説明 |
|---------|-----|------|------|
| id | BIGINT | PRIMARY KEY AUTO_INCREMENT | 投稿識別子 |
| user_id | UUID | FOREIGN KEY → _user.id | 投稿者ユーザー |
| content | TEXT | NOT NULL | 投稿内容 |
| created_at | DATETIME | NOT NULL | 投稿日時 |
| updated_at | DATETIME | NOT NULL | 更新日時 |

#### コメントテーブル：`comment`

| カラム名 | 型 | 制約 | 説明 |
|---------|-----|------|------|
| id | BIGINT | PRIMARY KEY AUTO_INCREMENT | コメント識別子 |
| post_id | BIGINT | FOREIGN KEY → post.id | 投稿ID |
| user_id | UUID | FOREIGN KEY → _user.id | コメント投稿者 |
| content | TEXT | NOT NULL | コメント内容 |
| created_at | DATETIME | NOT NULL | コメント日時 |

### 👥 評議員管理

#### 評議員テーブル：`council_member`

| カラム名 | 型 | 制約 | 説明 |
|---------|-----|------|------|
| id | BIGINT | PRIMARY KEY AUTO_INCREMENT | 評議員識別子 |
| user_id | UUID | FOREIGN KEY → _user.id | 評議員ユーザー |
| role | ENUM('MEDIATION', 'PROPOSAL', 'ARBITRATION') | NOT NULL | 担当領域 |
| term_start | DATE | NOT NULL | 任期開始日 |
| term_end | DATE | NULLABLE | 任期終了日 |
| notes | TEXT | NULLABLE | 備考欄 |

---

## 🎓 学習機能（実装済み）

### 学習空間テーブル：`learning_space`

| カラム名 | 型 | 制約 | 説明 |
|---------|-----|------|------|
| id | BIGINT | PRIMARY KEY AUTO_INCREMENT | 学習空間ID |
| name | VARCHAR(100) | NOT NULL | 空間名 |
| mode | ENUM('SCHOOL', 'SALON', 'FANCLUB') | NOT NULL | 学習モード |
| created_at | DATETIME | NOT NULL | 作成日時 |
| updated_at | DATETIME | NOT NULL | 更新日時 |

### 学習コンテンツテーブル：`learning_content`

| カラム名 | 型 | 制約 | 説明 |
|---------|-----|------|------|
| id | BIGINT | PRIMARY KEY AUTO_INCREMENT | コンテンツID |
| space_id | BIGINT | FOREIGN KEY → learning_space.id | 学習空間ID |
| title | VARCHAR(255) | NOT NULL | コンテンツタイトル |
| type | ENUM('VIDEO', 'TEXT', 'QUIZ', 'DOCUMENT') | NOT NULL | コンテンツ種別 |
| url | TEXT | NULLABLE | コンテンツURL |
| description | TEXT | NULLABLE | 説明文 |
| created_at | DATETIME | NOT NULL | 作成日時 |
| updated_at | DATETIME | NOT NULL | 更新日時 |

### 学習進捗テーブル：`learning_progress`

| カラム名 | 型 | 制約 | 説明 |
|---------|-----|------|------|
| id | BIGINT | PRIMARY KEY AUTO_INCREMENT | 進捗ID |
| user_id | UUID | FOREIGN KEY → _user.id | 学習者 |
| content_id | BIGINT | FOREIGN KEY → learning_content.id | 学習コンテンツ |
| progress_percent | DECIMAL(5,2) | NOT NULL DEFAULT 0.00 | 完了率（0.00-100.00） |
| completed_at | DATETIME | NULLABLE | 完了日時 |
| created_at | DATETIME | NOT NULL | 開始日時 |
| updated_at | DATETIME | NOT NULL | 更新日時 |

**制約**: UNIQUE(user_id, content_id)

### 学習コメントテーブル：`learning_comment`

| カラム名 | 型 | 制約 | 説明 |
|---------|-----|------|------|
| id | BIGINT | PRIMARY KEY AUTO_INCREMENT | コメントID |
| topic_id | BIGINT | NOT NULL | トピックID |
| author_id | UUID | NOT NULL | 作成者ID |
| parent_comment_id | BIGINT | NULLABLE | 親コメントID（返信の場合） |
| content | TEXT | NOT NULL | コメント内容 |
| comment_type | ENUM('QUESTION', 'ANSWER', 'DISCUSSION', 'ANNOUNCEMENT') | NOT NULL | コメント種別 |
| comment_status | ENUM('ACTIVE', 'HIDDEN', 'DELETED', 'PENDING_MODERATION') | NOT NULL | コメント状態 |
| like_count | INT | DEFAULT 0 | いいね数 |
| dislike_count | INT | DEFAULT 0 | よくないね数 |
| reply_count | INT | DEFAULT 0 | 返信数 |
| report_count | INT | DEFAULT 0 | 報告数 |
| is_best_answer | BOOLEAN | DEFAULT FALSE | ベストアンサーか |
| is_solution | BOOLEAN | DEFAULT FALSE | 解決策か |
| is_pinned | BOOLEAN | DEFAULT FALSE | ピン留めか |
| is_featured | BOOLEAN | DEFAULT FALSE | 注目コメントか |
| is_highlighted | BOOLEAN | DEFAULT FALSE | ハイライト表示か |
| requires_moderation | BOOLEAN | DEFAULT FALSE | モデレーション必要か |
| moderation_status | ENUM('APPROVED', 'REJECTED', 'PENDING', 'AUTO_APPROVED') | DEFAULT 'AUTO_APPROVED' | モデレーション状態 |
| moderator_id | UUID | NULLABLE | モデレーター |
| moderation_notes | TEXT | NULLABLE | モデレーション備考 |
| moderated_at | DATETIME | NULLABLE | モデレーション日時 |
| quality_score | DECIMAL(3,2) | NULLABLE | 品質スコア |
| helpfulness_score | DECIMAL(3,2) | NULLABLE | 有用性スコア |
| relevance_score | DECIMAL(3,2) | NULLABLE | 関連性スコア |
| overall_score | DECIMAL(3,2) | NULLABLE | 総合スコア |
| created_at | DATETIME | NOT NULL | 作成日時 |
| updated_at | DATETIME | NOT NULL | 更新日時 |
| deleted_at | DATETIME | NULLABLE | 削除日時 |

**インデックス**:
- idx_comment_topic_id (topic_id)
- idx_comment_author_id (author_id)
- idx_comment_parent_id (parent_comment_id)
- idx_comment_status (comment_status)
- idx_comment_type (comment_type)
- idx_comment_created_at (created_at)
- idx_comment_like_count (like_count)
- idx_comment_quality_score (quality_score)
- idx_comment_is_best_answer (is_best_answer)
- idx_comment_is_pinned (is_pinned)
- idx_comment_is_solution (is_solution)
- idx_comment_thread (topic_id, parent_comment_id, created_at)
- idx_comment_moderation (requires_moderation, moderation_status)
- idx_comment_active (comment_status, created_at)
- idx_comment_popular (like_count, quality_score)

---

## 📋 学習機能拡張テーブル（実装済み）

### 学習セクションテーブル：`learning_section`
### 学習クイズテーブル：`learning_quiz`
### 学習ライブセッションテーブル：`learning_live_session`
### 学習フォーラムテーブル：`learning_forum`
### 学習トピックテーブル：`learning_topic`
### 学習フィードバックテーブル：`learning_feedback`
### 学習バッジテーブル：`learning_badge`
### 学習アナリティクステーブル：`learning_analytics_report`
### 学習AIログテーブル：`learning_ai_log`
### 学習AIFAQテーブル：`learning_ai_faq`
### 学習写経テーブル：`learning_shakyo`
### 学習シミュレーションテーブル：`learning_simulation`
### 学習サブスクリプションプランテーブル：`learning_subscription_plan`
### 学習アンケートテーブル：`learning_survey`
### 学習ランキングテーブル：`learning_ranking`
### 学習教材テーブル：`learning_material`
### 学習ポイントテーブル：`learning_point`
### 学習モード設定テーブル：`learning_mode_config`
### 学習ユーザーバッジテーブル：`learning_user_badge`
### 学習コース料金テーブル：`learning_course_fee`

*詳細なスキーマは個別のエンティティクラスを参照してください。*

---

## 🔧 実装済みENUM定義

### ユーザー関連
- **Role**: USER, ADMIN
- **Status**: ACTIVE, INACTIVE, PENDING
- **UserState**: ENABLED, DISABLED

### キャラクター関連
- **CharacterStatus**: ACTIVE, INACTIVE, DECEASED

### 経済関連
- **TransactionType**: INCOME, EXPENSE

### 評議員関連
- **CouncilRole**: MEDIATION, PROPOSAL, ARBITRATION

### 学習関連
- **LearningMode**: SCHOOL, SALON, FANCLUB
- **ContentType**: VIDEO, TEXT, QUIZ, DOCUMENT
- **CommentType**: QUESTION, ANSWER, DISCUSSION, ANNOUNCEMENT
- **CommentStatus**: ACTIVE, HIDDEN, DELETED, PENDING_MODERATION
- **ModerationStatus**: APPROVED, REJECTED, PENDING, AUTO_APPROVED

---

## 📌 今後の実装予定

### 🛒 ECサイト機能
- 商品テーブル：`product`
- 注文テーブル：`order`
- 決済履歴テーブル：`transaction`

### 💳 サブスクリプション機能
- サブスク管理テーブル：`subscription`
- プラン定義テーブル：`plan`

### 🎟️ クーポン機能
- クーポンテーブル：`coupon`
- クーポン使用履歴テーブル：`coupon_usage`

### 👥 チーム機能
- チームテーブル：`team_entity`
- チーム構成テーブル：`team_member`

### 🪙 暗号資産機能
- 暗号資産付与履歴テーブル：`crypto_reward`
- 暗号資産ウォレットテーブル：`wallet`

---

## 🔗 リレーション図

```
_user (1) -----> (N) character_lifecycle
_user (1) -----> (N) post
_user (1) -----> (N) comment
_user (1) -----> (N) council_member
_user (1) -----> (N) learning_progress

post (1) -----> (N) comment

learning_space (1) -----> (N) learning_content
learning_content (1) -----> (N) learning_progress
learning_content (1) -----> (N) learning_section

learning_comment (1) -----> (N) learning_comment [parent_comment_id]
```

---

## 📝 実装ノート

1. **UUID vs BIGINT**: ユーザーIDはUUID、その他は性能を考慮してBIGINT AUTO_INCREMENTを使用
2. **タイムスタンプ**: `created_at`, `updated_at`は`@CreationTimestamp`, `@UpdateTimestamp`で自動管理
3. **インデックス**: 検索性能を考慮して適切なインデックスを設定
4. **ENUM管理**: 集中管理のため`sfr_enum_definitions.md`での管理を検討中
5. **セキュリティ**: 機密情報は暗号化、個人情報は適切なアクセス制御を実装

---

**📚 関連ドキュメント**
- [API設計書](./API設計書.docx)
- [開発要件](../開発要件.txt)
- [フロントエンドロードマップ](../frontendRoadmap.txt)