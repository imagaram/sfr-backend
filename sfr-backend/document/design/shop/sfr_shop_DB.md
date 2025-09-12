# SFR.TOKYO ショップ機能 DB設計書 v1.0 (2025-09-01)

## テーブル一覧

命名規約: 物理テーブルは `shop_` プレフィックス。タイムスタンプは UTC。金額は DECIMAL(30,10) を最終ターゲット（MVP は 10,2）。

### shop_items（商品情報）

| カラム名         | 型         | 制約         | 説明                   |
|------------------|------------|--------------|------------------------|
| id               | BIGINT     | PK           | 商品ID                 |
| name             | VARCHAR(64)| NOT NULL     | 商品名                 |
| description      | TEXT       |              | 商品説明               |
| price            | DECIMAL(10,2)| NOT NULL   | 価格（SFR）           |
| stock            | INT        | NOT NULL     | 在庫数                 |
| owner_id         | BIGINT     | FK(users)    | 所有者ユーザーID       |
| created_at       | TIMESTAMP  | NOT NULL     | 登録日時               |
| updated_at       | TIMESTAMP  |              | 更新日時               |

インデックス案:

- IX_shop_items_shop_id (shop_id)
- IX_shop_items_type (type)
- IX_shop_items_owner (owner_id)
- IX_shop_items_poa (poa_hash) PARTIAL (poa_hash IS NOT NULL)

### shop_orders（注文情報）

| カラム名         | 型         | 制約         | 説明                   |
|------------------|------------|--------------|------------------------|
| id               | BIGINT     | PK           | 注文ID                 |
| item_id          | BIGINT     | FK(shop_items)| 商品ID                |
| buyer_id         | BIGINT     | FK(users)    | 購入者ユーザーID       |
| quantity         | INT        | NOT NULL     | 注文個数               |
| total_price      | DECIMAL(10,2)| NOT NULL   | 合計金額（SFR）        |
| status           | VARCHAR(16)| NOT NULL     | 注文ステータス         |
| created_at       | TIMESTAMP  | NOT NULL     | 注文日時               |
| updated_at       | TIMESTAMP  |              | 更新日時               |

インデックス案:

- IX_shop_orders_item (item_id)
- IX_shop_orders_buyer (buyer_id)
- IX_shop_orders_status (status)
- IX_shop_orders_created (created_at DESC)

### shop_deliveries（配送情報）

| カラム名         | 型         | 制約         | 説明                   |
|------------------|------------|--------------|------------------------|
| id               | BIGINT     | PK           | 配送ID                 |
| order_id         | BIGINT     | FK(shop_orders)| 注文ID                |
| carrier          | VARCHAR(32)| NOT NULL     | 配送業者（yamato/japanpost等）|
| tracking_number  | VARCHAR(64)|              | 追跡番号               |
| label_url        | VARCHAR(256)|              | ラベルURL/PDF          |
| status           | VARCHAR(16)| NOT NULL     | 配送ステータス         |
| created_at       | TIMESTAMP  | NOT NULL     | 登録日時               |
| updated_at       | TIMESTAMP  |              | 更新日時               |

インデックス案:

- IX_shop_deliveries_order (order_id)
- IX_shop_deliveries_carrier_status (carrier, status)
- IX_shop_deliveries_tracking (tracking_number) UNIQUE NULLS NOT DISTINCT

### shop_delivery_tokens（配送トークン情報）

| カラム名         | 型         | 制約         | 説明                   |
|------------------|------------|--------------|------------------------|
| id               | BIGINT     | PK           | トークンID             |
| delivery_id      | BIGINT     | FK(shop_deliveries)| 配送ID              |
| poa_token        | VARCHAR(128)| NOT NULL    | PoAチェーン連携トークン|
| created_at       | TIMESTAMP  | NOT NULL     | 登録日時               |

インデックス案:

- IX_shop_delivery_tokens_delivery (delivery_id)
- IX_shop_delivery_tokens_created (created_at DESC)

### shop_settlements（清算予定・実行）

| カラム名 | 型 | 制約 | 説明 |
|----------|----|------|------|
| id | BIGINT | PK | 清算ID |
| order_id | BIGINT | FK(shop_orders) UNIQUE | 対象注文 |
| scheduled_at | TIMESTAMP | NOT NULL | 清算予定日時 (fulfilled_at + 30d) |
| executed_at | TIMESTAMP |  | 実行日時 |
| status | VARCHAR(16) | NOT NULL | PENDING/EXECUTED/CANCELLED |
| amount_sfr | DECIMAL(10,2) | NOT NULL | 清算金額 |
| created_at | TIMESTAMP | NOT NULL | 作成 |
| updated_at | TIMESTAMP |  | 更新 |

### shop_idempotency（冪等性キー保管 / 予定）

| カラム名 | 型 | 制約 | 説明 |
|----------|----|------|------|
| key | VARCHAR(64) | PK | Idempotency-Key |
| request_hash | VARCHAR(64) | NOT NULL | リクエストボディ hash |
| response_hash | VARCHAR(64) |  | レスポンス hash |
| status | VARCHAR(16) | NOT NULL | IN_PROGRESS / COMPLETED / FAILED |
| created_at | TIMESTAMP | NOT NULL | 作成 |
| expires_at | TIMESTAMP |  | TTL |

### audit_event（監査ログ 汎用）

| カラム名 | 型 | 制約 | 説明 |
|----------|----|------|------|
| id | BIGINT | PK | 連番 |
| event_type | VARCHAR(64) | NOT NULL | shop.order.placed 等 |
| ref_type | VARCHAR(32) | NOT NULL | ORDER/ITEM/DELIVERY/SETTLEMENT |
| ref_id | BIGINT | NOT NULL | 参照ID |
| payload | JSONB | NOT NULL | 付随データ |
| created_at | TIMESTAMP | NOT NULL | 発生日時 |

### 将来テーブル (PENDING)

- shop_auctions, shop_bids, shop_resales

## 制約と整合性

- 在庫減算は `shop_items.stock >= 0` を CHECK + トランザクション内更新
- 外部キーは ON DELETE RESTRICT（履歴保持）
- `shop_orders.total_price = items.price * quantity` をアプリ層計算 + 将来的に GENERATED 列化可

## トランザクション/ロック指針

| 操作 | 推奨ロック | 備考 |
|------|------------|------|
| 注文作成 | SELECT ... FOR UPDATE (対象 item 行) | 在庫競合防止 |
| 清算実行 | SELECT settlement FOR UPDATE | 二重実行防止 |
| PoA 所有権更新 | シリアライズレベル or 楽観リトライ | 高頻度でなければ悲観も可 |

## パーティショニング (将来)

- `audit_event` を月次パーティション化
- `shop_orders` を created_at 年単位パーティション検討

## 備考

- ユーザー情報は users テーブル参照
- 商品所有権・配送履歴は PoA チェーン連携
- 匿名配送ID・トークンは shop_delivery_tokens で管理
- 金額拡張時は DECIMAL(30,10) へマイグレーション計画要
