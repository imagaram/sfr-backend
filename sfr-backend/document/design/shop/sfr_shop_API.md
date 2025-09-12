# SFR.TOKYO ショップ機能 API設計書 v1.0 (2025-09-01)

本書は `sfr_shop_design.md` v1.0 の API 具体仕様。MVP 実装状況を反映し PENDING 項目はコメントで明示。ベースパスは `/api/v1/shop` を推奨（他領域整合）。

## 共通

| 要素 | 内容 |
|------|------|
| 認証 | Authorization: Bearer &lt;JWT&gt; |
| エンコード | `application/json; charset=utf-8` |
| エラー | 共通エラーボディ `{ code: string, message: string, details?: any }` |
| 冪等性(予定) | `Idempotency-Key` ヘッダ (POST系) |
| 通貨 | SFR (decimal 文字列) / 未来: JPY (Stripe) |

主要エラーコードは design ドキュメント参照 (例: `SHOP_STOCK_SHORTAGE`).

---
## 商品管理

### 商品一覧取得
GET /api/v1/shop/items?type=PHYSICAL|DIGITAL|LIMITED&ownerId=...&cursor=...&limit=...

レスポンス 200

```json
{
	"items": [
		{"id":1,"type":"PHYSICAL","name":"T-Shirt","priceSfr":"12.50","stock":5,"shopId":10,"createdAt":"2025-09-01T12:00:00Z","updatedAt":"2025-09-01T12:00:00Z"}
	],
	"nextCursor": "eyJpZCI6MX0="
}
```

### 商品登録
POST /api/v1/shop/items
 
```jsonc
// Request
{
	"shopId": 10,
	"type": "PHYSICAL", // PHYSICAL|DIGITAL|LIMITED
	"name": "T-Shirt",
	"description": "Organic cotton",
	"priceSfr": "12.50",
	"stock": 50,
	"maxPerUser": 2 // LIMITED のみ
}
```
201 レスポンス
 
```json
{"id":123,"shopId":10,"type":"PHYSICAL","name":"T-Shirt","priceSfr":"12.50","stock":50,"createdAt":"..."}
```
エラー: `SHOP_LIMIT_EXCEEDED` (LIMITED + maxPerUser < 1) など

### 商品詳細取得
GET /api/v1/shop/items/{itemId}
200 -> `ShopItemDto`

### 商品編集
PATCH /api/v1/shop/items/{itemId}
 
```json
{"name":"New Name","priceSfr":"13.00","stock":40}
```
200 -> 更新後 DTO

### 商品削除
DELETE /api/v1/shop/items/{itemId}
204 (在庫 >0 かつ既存注文あり -> 409 `SHOP_ORDER_STATE_INVALID`)

---
## 注文管理

### 注文作成
POST /api/v1/shop/orders
 
```json
{"itemId":123,"quantity":2,"paymentMethod":"SFR"}
```
201
 
```json
{"id":9001,"itemId":123,"quantity":2,"unitPrice":"12.50","totalPrice":"25.00","status":"PLACED","createdAt":"..."}
```
検証:
- 在庫ロック (悲観/楽観いずれか) 失敗 -> 409 `SHOP_STOCK_SHORTAGE`
- ユーザー購入上限 (将来) -> 409 `SHOP_LIMIT_EXCEEDED`

### 注文一覧取得
GET /api/v1/shop/orders?status=PLACED|FULFILLED&cursor=...

### 注文詳細取得
GET /api/v1/shop/orders/{orderId}

### 注文確定 (発送準備完了などステータス遷移)
POST /api/v1/shop/orders/{orderId}/fulfill
200 -> status=FULFILLED （支払確定 & 清算待ち）

### 注文キャンセル
POST /api/v1/shop/orders/{orderId}/cancel
200 / 409 `SHOP_ORDER_STATE_INVALID`

---
## 配送管理 (PARTIAL 実装想定)

### ラベル作成（ヤマト）
POST /api/v1/shop/deliveries/yamato/labels
 
```json
{"orderId":9001,"recipient":{"aliasId":"anon-xyz","zip":"1000001","pref":"東京都","addr1":"千代田区"},"poaToken":"..."}
```
201
 
```json
{"deliveryId":501,"carrier":"YAMATO","trackingNumber":"ABC123","labelUrl":"https://.../label.pdf","status":"LABEL_ISSUED"}
```

### 追跡（ヤマト）
GET /api/v1/shop/deliveries/yamato/track?trackingNumber=ABC123
200 `{ "status":"IN_TRANSIT","lastUpdate":"..." }`

### ラベル作成（日本郵便）
POST /api/v1/shop/deliveries/japanpost/labels
（フィールド同上／レスポンス `labelPdf` Base64 など）

### 追跡（日本郵便）
GET /api/v1/shop/deliveries/japanpost/track?trackingNumber=ZZ999

### 配送詳細取得
GET /api/v1/shop/deliveries/{deliveryId}

---
## 配送トークン管理

### 配送トークン発行
POST /api/v1/shop/deliveries/{deliveryId}/token
 
```json
{"poaToken":"..."}
```
200 `{ "token":"<short-lived-token>","expiresIn":900 }`

### 配送トークン検証
POST /api/v1/shop/deliveries/token/verify `{ "token":"..." }` -> 200 / 401

---
## オークション (PENDING)

下記は将来案:
| エンドポイント | 概要 |
|----------------|------|
| POST /api/v1/shop/auctions | 開催作成 |
| GET /api/v1/shop/auctions/{id} | 詳細 |
| POST /api/v1/shop/auctions/{id}/bids | 入札 (SFR エスクロー) |
| POST /api/v1/shop/auctions/{id}/close | 締切 (システム/自動) |

---
## 共通レスポンス例

### エラー
```json
{
	"code": "SHOP_STOCK_SHORTAGE",
	"message": "Requested quantity exceeds stock.",
	"details": {"requested":10,"available":4}
}
```

---
## セキュリティ / 認可

- JWT ロール: USER / TEAM_OWNER / ADMIN / COUNCIL
- TEAM_OWNER のみ所属チームの `shop.items` 変更可
- 配送ラベル系は追加で内部 API キー (Gateway) + JWT
- PoA 署名検証 (デジタル / LIMITED アイテム, PENDING)

---
## バージョン管理ポリシー

| 変更種別 | バージョン | 互換性 |
|----------|------------|--------|
| 後方互換追加 | +0.1 (minor) | 既存クライアント動作維持 |
| 非互換変更 | +1.0 (major) | Deprecated 通知後 30 日猶予 |

---
## TODO (API 観点)

- デジタル/限定商品の PoA 発行/検証エンドポイント追加
- Stripe 決済フロー (`/payments/stripe/*`)
- Idempotency-Key 保存層
- Webhook (決済/配送) 受信エンドポイント

（以上）
