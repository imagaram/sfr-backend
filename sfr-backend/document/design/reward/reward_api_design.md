# SFR報酬システム API設計書

## 概要

このドキュメントは、SFR暗号資産の報酬システムに関するAPI設計を記述します。
報酬計算式 `SFR報酬量 = B × C × M × H` を実装するためのRESTful APIを定義します。

## API仕様概要

- **ベースURL**: `/api/v1/rewards`
- **認証**: JWT Bearer Token
- **レスポンス形式**: JSON
- **エラーハンドリング**: RFC 7807 Problem Details

## エンドポイント一覧

### 1. 貢献記録管理

#### 1.1 貢献記録作成
```http
POST /api/v1/rewards/contributions
Authorization: Bearer {jwt_token}
Content-Type: application/json
```

**リクエストボディ**
```json
{
  "contributionType": "development|liquidity|governance|education|commerce|ux",
  "activityType": "commit|merge_pr|close_issue|provide_liquidity|submit_proposal|vote|create_course|sfr_payment",
  "referenceId": "string",
  "referenceType": "string",
  "metrics": {
    "commitsCount": 5,
    "mergedPrsCount": 2,
    "codeAdded": 150,
    "codeDeleted": 30
  },
  "activityDate": "2025-08-26T10:00:00Z"
}
```

**レスポンス**
```json
{
  "contributionRecordId": 12345,
  "contributionScore": 2.5,
  "status": "RECORDED",
  "message": "貢献記録が正常に作成されました"
}
```

#### 1.2 貢献記録一覧取得
```http
GET /api/v1/rewards/contributions?userId={userId}&contributionType={type}&from={date}&to={date}&page={page}&size={size}
Authorization: Bearer {jwt_token}
```

**レスポンス**
```json
{
  "content": [
    {
      "id": 12345,
      "contributionType": "development",
      "activityType": "commit",
      "contributionScore": 2.5,
      "activityDate": "2025-08-26T10:00:00Z",
      "metrics": {
        "commitsCount": 5,
        "mergedPrsCount": 2
      }
    }
  ],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8
  }
}
```

### 2. 報酬計算

#### 2.1 報酬計算実行
```http
POST /api/v1/rewards/calculate
Authorization: Bearer {jwt_token}
Content-Type: application/json
```

**リクエストボディ**
```json
{
  "contributionRecordId": 12345,
  "forceRecalculate": false
}
```

**レスポンス**
```json
{
  "calculationId": 67890,
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "contributionRecordId": 12345,
  "factors": {
    "baseFactor": 1.4,
    "contributionScore": 2.5,
    "marketFactor": 1.2,
    "holdingFactor": 1.8
  },
  "calculatedAmount": 7.56,
  "finalAmount": 7.56,
  "calculationFormula": "1.4 × 2.5 × 1.2 × 1.8 = 7.56 SFR",
  "marketPriceJpy": 145.50,
  "status": "CALCULATED",
  "calculatedAt": "2025-08-26T10:15:00Z"
}
```

#### 2.2 バッチ報酬計算
```http
POST /api/v1/rewards/calculate/batch
Authorization: Bearer {jwt_token}
Content-Type: application/json
```

**リクエストボディ**
```json
{
  "contributionRecordIds": [12345, 12346, 12347],
  "calculationDate": "2025-08-26T10:00:00Z"
}
```

**レスポンス**
```json
{
  "batchId": "batch_001",
  "processedCount": 3,
  "successCount": 3,
  "failureCount": 0,
  "totalCalculatedAmount": 22.68,
  "results": [
    {
      "contributionRecordId": 12345,
      "calculationId": 67890,
      "finalAmount": 7.56,
      "status": "CALCULATED"
    }
  ]
}
```

### 3. 報酬係数管理

#### 3.1 現在の報酬係数取得
```http
GET /api/v1/rewards/factors/current
Authorization: Bearer {jwt_token}
```

**レスポンス**
```json
{
  "baseFactors": {
    "development": 1.4,
    "liquidity": 1.3,
    "governance": 1.0,
    "education": 1.3,
    "commerce": 1.2,
    "ux": 0.9
  },
  "marketFactor": {
    "currentPrice": 145.50,
    "targetPrice": 150.00,
    "factor": 1.2
  },
  "effectiveFrom": "2025-08-01T00:00:00Z",
  "lastUpdated": "2025-08-26T09:00:00Z"
}
```

#### 3.2 報酬係数更新（管理者のみ）
```http
PUT /api/v1/rewards/factors
Authorization: Bearer {admin_jwt_token}
Content-Type: application/json
```

**リクエストボディ**
```json
{
  "baseFactors": {
    "development": 1.5,
    "liquidity": 1.4
  },
  "targetPrice": 155.00,
  "effectiveFrom": "2025-09-01T00:00:00Z",
  "reason": "市場状況の変化に伴う調整"
}
```

### 4. 保有インセンティブ

#### 4.1 ユーザーの保有インセンティブ係数取得
```http
GET /api/v1/rewards/holding-incentive/{userId}
Authorization: Bearer {jwt_token}
```

**レスポンス**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "holdingFactor": 1.8,
  "breakdown": {
    "baseHolding": 1.2,
    "stakingBonus": 0.3,
    "paymentUsageBonus": 0.1,
    "priceSupportBonus": 0.2
  },
  "details": {
    "holdingDays": 365,
    "stakingMonths": 6,
    "stakingAmount": 1000.0,
    "paymentUsageCount": 25,
    "averageHoldingPrice": 140.00,
    "currentPrice": 145.50,
    "priceSupportRatio": 0.96
  },
  "calculationDate": "2025-08-26T10:00:00Z"
}
```

#### 4.2 保有インセンティブ履歴
```http
GET /api/v1/rewards/holding-incentive/{userId}/history?from={date}&to={date}
Authorization: Bearer {jwt_token}
```

### 5. 報酬配布

#### 5.1 報酬配布承認（管理者のみ）
```http
POST /api/v1/rewards/approve
Authorization: Bearer {admin_jwt_token}
Content-Type: application/json
```

**リクエストボディ**
```json
{
  "calculationIds": [67890, 67891, 67892],
  "approvalComment": "2025年8月分の報酬配布を承認"
}
```

**レスポンス**
```json
{
  "approvedCount": 3,
  "totalApprovedAmount": 22.68,
  "distributionIds": [100001, 100002, 100003],
  "approvedAt": "2025-08-26T11:00:00Z",
  "approvedBy": "admin_user_id"
}
```

#### 5.2 報酬配布実行
```http
POST /api/v1/rewards/distribute
Authorization: Bearer {admin_jwt_token}
Content-Type: application/json
```

**リクエストボディ**
```json
{
  "distributionIds": [100001, 100002, 100003],
  "executionDate": "2025-08-26T12:00:00Z"
}
```

### 6. 統計・レポート

#### 6.1 ユーザー報酬サマリー
```http
GET /api/v1/rewards/summary/{userId}?period={monthly|weekly|daily}&from={date}&to={date}
Authorization: Bearer {jwt_token}
```

**レスポンス**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "period": "monthly",
  "summary": {
    "totalEarned": 125.50,
    "totalDistributed": 100.00,
    "pending": 25.50,
    "contributionBreakdown": {
      "development": 80.00,
      "governance": 20.00,
      "education": 25.50
    },
    "averageHoldingFactor": 1.6,
    "contributionCount": 15
  },
  "fromDate": "2025-08-01T00:00:00Z",
  "toDate": "2025-08-31T23:59:59Z"
}
```

#### 6.2 システム全体統計（管理者のみ）
```http
GET /api/v1/rewards/statistics/system?period={daily|weekly|monthly}
Authorization: Bearer {admin_jwt_token}
```

**レスポンス**
```json
{
  "period": "monthly",
  "totalCalculated": 10000.00,
  "totalDistributed": 8500.00,
  "pendingDistribution": 1500.00,
  "participatingUsers": 250,
  "topContributionTypes": [
    {
      "type": "development",
      "amount": 4000.00,
      "percentage": 40.0
    },
    {
      "type": "education",
      "amount": 3000.00,
      "percentage": 30.0
    }
  ],
  "averageRewardPerUser": 34.00,
  "marketMetrics": {
    "averagePrice": 148.50,
    "priceVolatility": 0.05,
    "averageMarketFactor": 1.1
  }
}
```

### 7. 管理機能

#### 7.1 報酬配布制限設定
```http
POST /api/v1/rewards/limits
Authorization: Bearer {admin_jwt_token}
Content-Type: application/json
```

**リクエストボディ**
```json
{
  "limitType": "MONTHLY",
  "category": "development",
  "maxAmount": 5000.00,
  "periodStart": "2025-09-01T00:00:00Z",
  "periodEnd": "2025-09-30T23:59:59Z"
}
```

#### 7.2 市場価格更新
```http
POST /api/v1/rewards/market-price
Authorization: Bearer {system_jwt_token}
Content-Type: application/json
```

**リクエストボディ**
```json
{
  "priceJpy": 147.25,
  "priceSource": "exchange_api",
  "volume24h": 50000.00,
  "marketCap": 14725000.00,
  "priceTimestamp": "2025-08-26T10:30:00Z"
}
```

## データ型定義

### 共通レスポンス形式

```typescript
interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: {
    code: string;
    message: string;
    details?: any;
  };
  timestamp: string;
}

interface PageResponse<T> {
  content: T[];
  page: {
    number: number;
    size: number;
    totalElements: number;
    totalPages: number;
  };
}
```

### 貢献タイプ
```typescript
type ContributionType = 
  | 'development'   // 開発貢献
  | 'liquidity'     // 流動性提供
  | 'governance'    // ガバナンス
  | 'education'     // 教育・普及
  | 'commerce'      // 商用利用
  | 'ux';           // UX改善
```

### 計算ステータス
```typescript
type CalculationStatus = 
  | 'CALCULATED'    // 計算完了
  | 'APPROVED'      // 承認済み
  | 'DISTRIBUTED'   // 配布完了
  | 'REJECTED';     // 却下
```

## エラーコード

| コード | 説明 | HTTPステータス |
|--------|------|----------------|
| REWARD_001 | 貢献記録が見つかりません | 404 |
| REWARD_002 | 報酬計算に失敗しました | 500 |
| REWARD_003 | 配布制限に達しています | 400 |
| REWARD_004 | 承認権限がありません | 403 |
| REWARD_005 | 無効な貢献タイプです | 400 |
| REWARD_006 | 計算済みの記録です | 409 |
| REWARD_007 | 市場価格の取得に失敗しました | 500 |

## セキュリティ考慮事項

### 認証・認可
- すべてのエンドポイントでJWT認証が必要
- 管理者機能は`ADMIN`ロールが必須
- システム機能は`SYSTEM`ロールが必須

### レート制限
- 一般ユーザー: 100 requests/minute
- 管理者: 500 requests/minute
- システム: 無制限

### データ検証
- 入力値のバリデーション
- SQLインジェクション防止
- XSS防止

### 監査ログ
- すべての報酬関連操作をログ記録
- 管理者操作の詳細ログ
- 不正アクセスの検知

## バッチ処理API

### 日次バッチ処理
```http
POST /api/v1/rewards/batch/daily
Authorization: Bearer {system_jwt_token}
```

- 市場価格の更新
- 保有インセンティブの再計算
- 係数の動的調整

### 月次バッチ処理
```http
POST /api/v1/rewards/batch/monthly
Authorization: Bearer {system_jwt_token}
```

- 月次統計の生成
- 古いデータのアーカイブ
- 制限値のリセット

## Webhook通知

### 報酬配布完了通知
```json
{
  "event": "reward.distributed",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "amount": 7.56,
  "contributionType": "development",
  "distributedAt": "2025-08-26T12:00:00Z"
}
```

### 係数変更通知
```json
{
  "event": "factors.updated",
  "changes": {
    "baseFactor.development": {
      "from": 1.4,
      "to": 1.5
    }
  },
  "effectiveFrom": "2025-09-01T00:00:00Z"
}
```

## テスト用エンドポイント

開発・テスト環境でのみ利用可能

### サンプルデータ生成
```http
POST /api/v1/rewards/test/generate-sample-data
Authorization: Bearer {test_jwt_token}
```

### 計算結果リセット
```http
DELETE /api/v1/rewards/test/reset-calculations
Authorization: Bearer {test_jwt_token}
```

## パフォーマンス考慮事項

### キャッシュ戦略
- 報酬係数: Redis, TTL 1時間
- 市場価格: Redis, TTL 5分
- ユーザー統計: Redis, TTL 15分

### 非同期処理
- バッチ計算: RabbitMQ
- 通知送信: RabbitMQ
- レポート生成: バックグラウンドタスク

### データベース最適化
- 適切なインデックス設定
- パーティショニング
- 読み取り専用レプリカの活用
