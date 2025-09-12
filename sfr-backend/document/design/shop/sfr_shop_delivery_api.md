# 1. はじめに (v1.0 / 2025-09-01)

本ドキュメントは SFR.TOKYO ショップ機能における匿名配送レイヤの制度・技術設計。`sfr_shop_design.md` の配送節詳細版。

対象配送事業者（MVP）: ヤマト運輸 / 日本郵便  (拡張: 佐川, PUDO)

想定ユースケース: DAOマーケットプレイス / NFT物販 / 再販流通 / 限定商品の寄付付与トレーサビリティ

関連設計: `sfr_shop_API.md`, `sfr_shop_DB.md`

## 2. 共通仕様

| 項目 | 内容 |
|------|------|
| ベースパス | `/api/v1/shop/deliveries` |
| データ保持 | label_url (外部S3署名URL), tracking_number, status |
| ステータス | REQUESTED / LABEL_ISSUED / IN_TRANSIT / DELIVERED / ARCHIVED / CANCELLED / LOST |
| 匿名化 | 受取人実住所は外部暗号ストア or Vault。DB には aliasId のみ。 |
| トークン | HMAC 派生短命トークン (TTL=900s) |
| 監査 | `audit_event` (event_type=shop.delivery.*) |

配送ライフサイクル:

```text
ORDER_PLACED -> DELIVERY.REQUESTED -> LABEL_ISSUED -> IN_TRANSIT -> DELIVERED -> ARCHIVED
							  -> CANCELLED / LOST
```

## 3. データフロー

1. 注文確定時 `shop_deliveries` レコード (status=REQUESTED) 仮作成
2. ラベル生成 API 呼出で外部キャリア API (スタブ or 本番) 連携
3. 追跡ポーリング (将来 Webhook 化) により status 遷移
4. DELIVERED 遷移時 `shop_settlements` 清算タイマー起動
5. ARCHIVED バッチ (90日後) でラベル URL 失効/最小化

## 4. エンドポイント詳細

### 4.1 ラベル作成 (ヤマト)

POST /api/v1/shop/deliveries/yamato/labels
```jsonc
// Request
{
	"orderId": 9001,
	"recipient": {
		"aliasId": "anon-xyz",
		"zip": "1000001",
		"pref": "東京都",
		"addr1": "千代田区",
		"addr2": "丸の内1-1"
	},
	"poaToken": "..." // DIGITAL/LIMITED 所有権検証トークン (PENDING)
}
```
201
```json
{
	"deliveryId": 501,
	"carrier": "YAMATO",
	"trackingNumber": "ABC123456789JP",
	"labelUrl": "https://s3.../label_501.pdf?sig=...",
	"status": "LABEL_ISSUED"
}
```
バリデーション: 注文状態要 PLACED/PAID, 既存 delivery.status != LABEL_ISSUED

### 4.2 追跡 (ヤマト)

GET /api/v1/shop/deliveries/yamato/track?trackingNumber=ABC123456789JP
```json
{"trackingNumber":"ABC123456789JP","status":"IN_TRANSIT","history":[{"at":"2025-09-01T12:00:00Z","code":"PICKED"}]}
```

### 4.3 ラベル作成 (日本郵便)

POST /api/v1/shop/deliveries/japanpost/labels
レスポンスは `labelPdf` (Base64) or 署名URL。フィールドは 4.1 と同等。

### 4.4 追跡 (日本郵便)

GET /api/v1/shop/deliveries/japanpost/track?trackingNumber=ZZ999

### 4.5 配送詳細取得

GET /api/v1/shop/deliveries/{deliveryId}

### 4.6 配送トークン発行

POST /api/v1/shop/deliveries/{deliveryId}/token

```json
{"poaToken":"..."}
```
200
```json
{"token":"shorttok_xxx","expiresIn":900}
```

 
### 4.7 配送トークン検証
POST /api/v1/shop/deliveries/token/verify

```json
{"token":"shorttok_xxx"}
```

200 `{ "valid": true, "deliveryId": 501 }`

## 5. セキュリティ / 認証

| 項目 | 内容 |
|------|------|
| JWT | 標準ユーザー認可 (TEAM_OWNER が自チーム注文を操作) |
| 内部APIキー | キャリアラッパー内部呼び出し用 (Gateway -> Service) |
| HMAC配送トークン | `Base62(HMAC(secret, deliveryId + orderId + ts))` + TTL |
| PoA検証 | LIMITED / DIGITAL 時に poaToken 検証 (PENDING) |
| 暗号化 | 住所は Vault(KMS) で AES256-GCM 保存 (aliasId のみ平文) |

リスク軽減: ラベルURL 失効 (署名URL TTL 24h), 追跡番号ブルートフォース防止 rate-limit。

## 6. エラーモデル (追加)

| code | 説明 | HTTP |
|------|------|------|
| SHOP_DELIVERY_TOKEN_INVALID | トークン無効/期限切れ | 401 |
| SHOP_ORDER_STATE_INVALID | 注文状態不正 | 409 |
| SHOP_DELIVERY_NOT_FOUND | 配送ID/追跡番号なし | 404 |
| SHOP_DELIVERY_LABEL_EXISTS | 既にラベル発行済 | 409 |
| SHOP_CARRIER_ERROR | 外部キャリアAPI失敗 | 502 |

## 7. 今後の拡張案

- PUDO ステーション連携 (ピックアップ地点別の aliasId)
- Webhook 方式追跡更新 (push型) / 現在は pull 間隔 15min
- 再販流通時 再利用配送ID 生成ポリシー
- オフチェーン to PoA 配送履歴アンカー (Merkle root 単位)

（以上）

