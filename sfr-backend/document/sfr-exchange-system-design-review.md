# SFR換算システム設計理解・検討

## 🎯 私の理解する換算システムロジック

### 1. 換算レート管理API
```
【基本概念】
- 1 SFR = X JPY の換算レートを動的管理
- レート変動要因: 供給量、需要、プラットフォーム成長
- 更新頻度: リアルタイム～定期更新

【実装想定】
GET /api/exchange/rates/current     # 現在レート取得
GET /api/exchange/rates/history     # レート履歴
POST /api/exchange/rates/update     # レート更新(管理者)
```

### 2. SFR⇔JPY変換ロジック
```
【変換方向】
A) JPY → SFR: Stripe決済後のSFR付与
   例: 1000円決済 → 手数料差し引き → SFR付与

B) SFR → JPY: SFR決済時の円換算
   例: 商品100 SFR → 現在レートでJPY換算表示

【手数料構造案】
- Stripe手数料: 3.6% (固定)
- プラットフォーム手数料: 2-5% (変動可能)
- SFR決済時: 手数料削減インセンティブ
```

### 3. Stripe決済 → SFR自動付与
```
【フロー想定】
1. ユーザーがStripe決済実行 (1000円)
2. Webhook受信・署名検証
3. 手数料計算: 1000円 - (36円 + α)
4. 換算レート適用: 残額 ÷ 現在レート = SFR数量
5. SFR自動発行・ユーザーウォレットに付与
6. トランザクション履歴記録

【トランザクション例】
決済額: 1000円
Stripe手数料: 36円 (3.6%)
Platform手数料: 20円 (2%)
Net額: 944円
現在レート: 1 SFR = 10円
付与SFR: 94.4 SFR
```

## 🤔 設計上の疑問・確認点

### A. 換算レート決定方法
**私の想定**: 管理者設定による固定レート
**疑問**: 
- 市場原理による動的レート？
- SFR総供給量・流通量との連動？
- 外部API（仮想通貨相場等）参照？

### B. 手数料構造詳細
**私の想定**: シンプルな固定％
**疑問**:
- SFR決済時の手数料優遇率は？
- 決済額による段階的手数料？
- 特定ユーザー（評議員等）の優遇？

### C. SFR付与タイミング
**私の想定**: Stripe決済完了即時
**疑問**:
- 決済確定待ち（24-48時間後）？
- バッチ処理による一括付与？
- リアルタイム処理の負荷対策？

### D. 換算精度・丸め処理
**私の想定**: 小数点8桁精度
**疑問**:
- SFR最小単位の定義？
- 端数処理ルール（切り上げ・切り捨て・四捨五入）？
- 累積誤差の対策？

## 📊 実装アーキテクチャ案

### データベース設計
```sql
-- 換算レートテーブル
CREATE TABLE exchange_rates (
    id BIGINT PRIMARY KEY,
    from_currency VARCHAR(10), -- 'JPY'
    to_currency VARCHAR(10),   -- 'SFR'
    rate DECIMAL(18,8),        -- 1 SFR = rate JPY
    effective_from TIMESTAMP,
    effective_to TIMESTAMP,
    created_by UUID,           -- 設定管理者
    source VARCHAR(100),       -- 'manual', 'api', 'algorithm'
    metadata JSON              -- 追加情報
);

-- 換算トランザクションテーブル
CREATE TABLE exchange_transactions (
    id BIGINT PRIMARY KEY,
    stripe_payment_intent_id VARCHAR(255),
    user_id UUID,
    jpy_amount DECIMAL(10,2),  -- 決済金額
    fees_total DECIMAL(10,2),  -- 総手数料
    sfr_amount DECIMAL(18,8),  -- 付与SFR
    exchange_rate DECIMAL(18,8), -- 適用レート
    status VARCHAR(50),        -- 'pending', 'completed', 'failed'
    processed_at TIMESTAMP
);
```

### サービス階層設計
```java
@Service
public class ExchangeRateService {
    // レート管理
    getCurrentRate(), updateRate(), getRateHistory()
}

@Service  
public class CurrencyConversionService {
    // 変換ロジック
    convertJpyToSfr(), convertSfrToJpy(), calculateFees()
}

@Service
public class StripeToSfrService {
    // 統合処理
    processPaymentToSfr(), handleWebhook(), issueRefund()
}
```

## ❓ あなたの設計意図確認

上記の理解で正しいでしょうか？特に以下の点について教えてください：

1. **換算レート**: 固定設定 vs 動的算出
2. **手数料構造**: 具体的な料率・優遇条件
3. **付与タイミング**: 即時 vs 遅延処理
4. **精度要件**: 小数点桁数・丸め方針
5. **特殊ケース**: 返金・キャンセル時の処理

この理解を基に実装を進めて良いでしょうか？
