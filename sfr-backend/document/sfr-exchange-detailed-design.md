# SFR換算システム：詳細設計理解と提案

## 🔍 既存システム分析に基づく理解

### 📊 SFR精度・計算ポリシー（既存実装から）

#### 現在のSFR精度基準
```typescript
// 既存SDKより確認済み
- 内部計算精度: 40桁 (decimal.js-light)
- 丸めルール: ROUND_HALF_UP
- 入力許容: 小数点10桁まで
- 表示デフォルト: 2桁（UI用）、8桁（詳細用）
- 内部保持: 18桁精度維持
```

#### 既存の金額処理パターン
```typescript
// packages/sfr-sdk/src/utils/decimal.ts
export function div(a: SfrAmount, b: SfrAmount, scale = 18): SfrAmount {
  return d(a).dividedBy(d(b)).toFixed(scale);
}

// 18桁精度が標準 = 換算計算もこれに合わせる
```

## 🎯 私の具体的な換算システム理解

### 1. 換算レート管理API設計

#### A. レート決定メカニズム（私の理解）
```
【初期段階】: 管理者による固定レート設定
- 1 SFR = 10 JPY（想定開始値）
- 手動更新（週1回程度）
- プラットフォーム成長に応じて段階的調整

【将来拡張】: 動的算出要素
- SFR総供給量vs流通量比率
- プラットフォーム利用者数・取引量
- 外部指標（暗号資産市場動向）
```

#### B. API設計（具体的）
```java
// 換算レート管理REST API
GET  /api/exchange/rates/current           # 現在有効レート
GET  /api/exchange/rates/history           # レート変動履歴  
POST /api/exchange/rates                   # 新レート設定（管理者のみ）
PUT  /api/exchange/rates/{id}/activate     # レート有効化

// レスポンス例
{
  "from": "JPY",
  "to": "SFR", 
  "rate": "10.00000000",        // 1 SFR = 10.00 JPY
  "inverseRate": "0.10000000",  // 1 JPY = 0.10 SFR
  "effectiveFrom": "2025-09-11T03:00:00Z",
  "source": "manual",
  "metadata": {
    "setBy": "admin-user-id",
    "reason": "monthly_adjustment"
  }
}
```

### 2. SFR⇔JPY変換ロジック（具体例）

#### A. JPY → SFR変換（Stripe決済時）
```java
// 実際の計算例
決済額: 1000 JPY
Stripe手数料: 1000 × 0.036 = 36 JPY
プラットフォーム手数料: 1000 × 0.02 = 20 JPY (2%と仮定)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Net金額: 1000 - 36 - 20 = 944 JPY

現在レート: 1 SFR = 10 JPY (rate = 10.00000000)
換算計算: 944 ÷ 10 = 94.40000000 SFR
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
ユーザー付与: 94.40000000 SFR (18桁精度)
```

#### B. SFR → JPY換算（決済表示時）
```java
// 商品価格表示例
商品価格: 100.00000000 SFR
現在レート: 1 SFR = 10 JPY
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
JPY相当額: 100 × 10 = 1000 JPY
表示: "100.00 SFR (≈ 1,000円)"
```

### 3. Stripe決済 → SFR自動付与フロー

#### 詳細なWebhookフロー
```java
1. Stripe Webhook受信: payment_intent.succeeded
   ↓
2. 署名検証 & PaymentIntentメタデータ確認
   - userId確認
   - 重複処理防止チェック
   ↓
3. 手数料計算
   - Stripe手数料: amount × 0.036
   - Platform手数料: amount × platform_rate (設定可変)
   ↓
4. 換算処理
   - Net金額 = 決済額 - 総手数料
   - 現在レート取得 (cache + fallback)
   - SFR付与額 = Net金額 ÷ レート (18桁精度)
   ↓
5. SFR発行 & ウォレット付与
   - 既存 CryptoClient.issueSfr() 利用
   - トランザクション履歴記録
   ↓
6. 通知 & ログ
   - ユーザー通知（プッシュ/メール）
   - 監査ログ記録
```

## 🤔 設計判断が必要な具体的ポイント

### A. 手数料率の詳細設定
**私の提案**:
```
- Stripe固定手数料: 3.6% (変更不可)
- Platform基本手数料: 2.0% 
- SFR決済時の優遇: Platform手数料 50%割引 → 1.0%
- 評議員特権: Platform手数料 免除 → 0%
- VIP会員: 取引量に応じて段階的割引
```

### B. 換算タイミング・レート適用
**私の提案**:
```
- レート取得: Webhook受信時点の最新レート
- レート有効期間: 設定時から次回更新まで
- キャッシュ: Redis 30分キャッシュ + DB fallback
- レート更新通知: 事前アナウンス（24時間前）
```

### C. 精度・丸め処理
**既存システムに合わせた提案**:
```
- 計算精度: decimal.js-light 40桁精度
- 中間計算: 18桁保持（既存SFRシステム準拠）
- 最終結果: 18桁精度でDB保存
- UI表示: 状況に応じて2桁・8桁切り替え
- 丸めルール: ROUND_HALF_UP（既存準拠）
```

### D. エラー・例外処理
**私の提案**:
```
- レート取得失敗: 直前の有効レート + アラート
- 計算オーバーフロー: エラーログ + 管理者通知  
- Webhook重複: idempotency key で重複防止
- SFR発行失敗: リトライ機能 + マニュアル修正手順
```

## 📋 確認したい設計意図

1. **レート更新頻度**: 手動 vs 自動（API経由）
2. **手数料収益の扱い**: プラットフォームウォレット蓄積 vs 別管理
3. **キャンセル・返金**: Stripe返金時のSFR回収ルール
4. **最小決済額**: 手数料考慮した最小JPY決済額設定
5. **レート履歴**: どの程度の期間保持するか

この理解で実装を進めて良いでしょうか？特に修正・追加したい点があれば教えてください。
