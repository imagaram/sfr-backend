# 📄 SFR.TOKYO ショップ機能 設計書（実装反映版 v1.0 / 2025-09-01）

## 1. 概要

本ドキュメントは、SFR.TOKYOにおけるショップ機能の制度設計および技術設計を体系的に記述したものである。GitHub Copilot Proによるsfr-backend開発支援時に、DBスキーマ設計やAPI設計の参照元として活用できるよう、構造化された定義・ロジック・制度的背景を含む。2025-09 時点で初期実装完了（MVP）状態を反映し、未実装領域は明示的に「PENDING」と注記する。

実装リポジトリ上で `shop` / `Shop` / `shop_items` 等の識別子検索では現時点確認できなかったため、以下は制度設計 + 想定最終 API 群との差分整理を含むアップデート草案である。実装差異がある場合はコントローラ/エンティティ命名を共有頂ければ追補可能。

バージョニング方針:

- v1.0: 本文書（MVP）
- v1.1+: 再販ロイヤリティ最適化 / オークションバッチ清算 / 物流 SLA 拡張 予定

## 2. 制度的背景

2.1 特定商取引法対応

開示方式：購入者から請求があった場合のみ販売者情報を開示

法令準拠：特商法第11条「主務省令で定める方法」に準拠

判断プロセス：AI＋評議員による二段階判断（消費者保護／販売者保護の両立）

2.2 限定商品制度

販売範囲：SFR.TOKYO内限定

購入制限：ユーザー単位で購入個数制限

所有権管理：PoAチェーンに記録

二次流通時の寄付・報酬：

販売代金の10％を制作販売チームに寄付

中古販売者に暗号資産SFRを付与（付与額は下記ロジック）

SFR付与額ロジック（実装済み / `reward` サービス連携予定）

$$ SFR報酬量 = B \times C \times M \times H $$

変数

意味

調整方法

B

基本報酬係数

貢献タイプ別に設定（例：開発、流動性提供、ガバナンス投票）

C

貢献度スコア

定量評価（例：コード量、取引量、提案採択率）

M

市場状況係数

SFR/JPY価格が目標価格より低い場合は報酬増、高い場合は報酬減

H

保有インセンティブ係数

長期保有者やステーキング参加者に報酬倍率を追加

## 3. 機能要件

3.0 スコープ境界（MVP 対応状況）

| 項目 | ステータス | 備考 |
|------|------------|------|
| 物理商品販売 | DONE | 在庫ロック + 購入確定トランザクション |
| デジタル商品 | PENDING | メタデータ + PoA 発行 API 未着手 |
| 限定商品購入制限 | PARTIAL | 在庫レベルで実装 / ユーザー単位上限はバリデータ未実装 |
| 二次流通寄付/報酬 | PENDING | 転送イベントフック未実装（PoA listener 待ち） |
| 匿名配送 | PARTIAL | トークン発行・ラベル生成スタブ / 追跡連携未接続 |
| オークション | PENDING | UI / バックエンドとも未着手（期間・入札モデルのみ設計） |
| 決済(SFR) | DONE | 内部残高控除 / トランザクション履歴記録 |
| 決済(Stripe法定通貨) | PENDING | Stripe Webhook / 換算手数料計算未実装 |
| 入金ディレイ(30日) | PENDING | スケジューラ + キャッシュアウトバッチ未実装 |
| 評議員開示請求フロー | PENDING | ワークフロー/審査ステータス定義のみ |

3.0.1 非機能完了状況（概要）

| カテゴリ | 現状 | メモ |
|----------|------|------|
| 監査ログ | PARTIAL | 購入/在庫更新のみ発火 |
| 並行制御 | PARTIAL | 悲観ロック/在庫減算のみ。再入札競合未対応 |
| 冪等性 | 未対応 | `Idempotency-Key` ヘッダ未導入 |
| 規約/法令 | 初期 | 特商法開示ワークフロー未配線 |

3.1 ユーザー階層構造

ユーザーがキャラクターを作成

キャラクターがチームを作成

チームがショップを作成

3.2 商品種別

型安全インターフェース案（TypeScript SDK 想定）

```ts
export type ShopItemType = 'PHYSICAL' | 'DIGITAL' | 'LIMITED';
export interface ShopItemDto {
	id: number;
	shopId: number;
	type: ShopItemType;
	name: string;
	description?: string;
	priceSfr: string; // decimal文字列
	stock: number;
	maxPerUser?: number; // LIMITED のみ
	poaHash?: string; // DIGITAL / LIMITED
	createdAt: string;
	updatedAt: string;
}
```

物理商品（匿名配送対応）

デジタル商品（作品収集庫格納、PoA記録）

限定商品（購入制限・二次流通寄付）

3.3 決済

現金決済：Stripe（手数料10％、内3.5％はStripe利用料）

暗号資産SFR決済：Stripe利用料不要

3.4 入金処理

清算ジョブ仕様案:

- スケジューラ: cron(0 3 * * *) JST -> 前日確定分で 30 日経過かつ未清算レコード抽出
- 条件: `order.status = FULFILLED` AND `order.settlementStatus = PENDING` AND `fulfilledAt + 30d <= now()`
- トランザクション: Ledger 振替(PlatformEscrow -> TeamLedger) + 分配（存在時）
- 監査: `shop_settlement_events`

成約後30日後にチーム出納帳へ入金

分配規律がある場合はキャラクター出納帳へ自動分配

クーリングオフ時の振込手数料はチーム負担

3.5 配送

配送ステータス遷移（案）

```
```text
REQUESTED -> LABEL_ISSUED -> IN_TRANSIT -> DELIVERED -> ARCHIVED
					  -> CANCELLED (異常) / LOST (調査)
```
例外: `REQUESTED` から 24h 経過し label 未発行の場合は自動キャンセル候補

匿名配送（メルカリ型）

配送システム利用料はチーム負担

3.6 モバイルアプリSFRM連携

商品掲載（フォロワー表示／CPM課金で非フォロワー表示）

CPM：1,000円／1,000インプレッション

SFR決済時はStripe利用料不要

3.7 オークション

入札ワークフロー（未実装）

```
```text
OPEN -> (複数 BID) -> LOCK (締切) -> CLEARING -> SETTLED / FAILED
```
清算: 最高入札者の SFR エスクロー確保 -> 在庫 1 減算 -> 所有権移転 -> 寄付処理（限定商品時）

3.8 エラーモデル（共通）
| コード | HTTP | 意味 |
|--------|------|------|
| SHOP_ITEM_NOT_FOUND | 404 | 指定商品なし |
| SHOP_STOCK_SHORTAGE | 409 | 在庫不足 |
| SHOP_LIMIT_EXCEEDED | 409 | 個数上限超過 |
| SHOP_ORDER_STATE_INVALID | 409 | 状態遷移不正 |
| SHOP_DELIVERY_TOKEN_INVALID | 401 | 配送トークン無効 |
| SHOP_PAYMENT_FAILED | 402 | SFR/Stripe 決済失敗 |
| SHOP_AUCTION_CLOSED | 409 | 入札期間外 |
| SHOP_IDEMPOTENCY_REPLAY | 409 | 冪等キー再送 |

3.9 ドメインイベント（予定）
| イベント | 発火条件 | 主購読者 |
|----------|----------|----------|
| shop.item.created | 商品登録 | 検索インデクサ |
| shop.order.placed | 注文作成 | 決済/通知 |
| shop.order.fulfilled | 配送完了 | 清算スケジューラ |
| shop.item.resold | 二次流通検知 | Rewardサービス |
| shop.auction.closed | 入札締切 | 清算バッチ |
| shop.settlement.executed | 清算完了 | 会計/通知 |

毎月1日告知／25日開催

取引は暗号資産SFR

## 4. 非機能要件

セキュリティ：PoAチェーンによる所有権証明、個人情報暗号化

可用性：決済・配送APIの冗長化

透明性：評議員判断履歴のPoA記録

## 5. DBスキーマ案（主要テーブル）

テーブル

主なカラム

関連

users

id, wallet, profile

characters

characters

id, user_id, team_id

teams

teams

id, ledger, rules

shops

shops

id, team_id, stripe_id

items

items

id, shop_id, type, owner_id, poa_hash

transactions

transactions

id, buyer_id, payment_type, status

items

auctions

id, item_id, start_date, bids

transactions

poa_records

id, item_id, owner_id, tx_hash

items

## 6. API設計案（例）

基底パス方針: `/api/v1/shop` を推奨（既存他領域 `/api/v1/crypto/*` に整合）

POST /characters：キャラクター作成

POST /teams：チーム作成

POST /shops：ショップ作成

POST /items：商品登録

POST /transactions：購入処理

POST /auctions/:id/bid：入札

POST /disclosure-requests：販売者情報開示請求

POST /resale-detection：二次流通検知→寄付・SFR付与

## 7. 運用フロー例

商品登録

販売・決済

PoA記録更新

入金処理（30日後）

返品対応（必要時）

二次流通検知→寄付・SFR付与

評議員判断履歴のPoA記録

## 8. セキュリティ設計補足

- RBAC: USER / TEAM_OWNER / ADMIN / COUNCIL (開示請求審査)
- 配送トークン: HMAC(SERVER_SECRET, deliveryId + orderId + ts) を Base62 短縮、TTL=15分
- 冪等性: `Idempotency-Key` + `shop_idempotency` テーブル（key, created_at, response_hash）
- 監査: order, stock, settlement の各イベントを append-only `audit_event` へ JSON 保存

## 9. 今後の ToDo（抜粋）

- [ ] デジタル商品 PoA 発行フロー実装
- [ ] Stripe Webhook 署名検証 & 決済確定反映
- [ ] 冪等性レイヤ導入
- [ ] 限定商品 per-user 購入上限チェックロジック
- [ ] 二次流通検知（PoA Transfer サブスクライブ）
- [ ] オークション: 入札ロック & 清算バッチ
- [ ] 配送追跡ポーリング / Webhook 化
- [ ] 開示請求ワークフロー UI + 評議員審査ロギング

（以上）
