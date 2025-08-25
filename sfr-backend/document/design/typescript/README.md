# SFR暗号資産システム TypeScript SDK

SFR暗号資産APIとの通信とデータ処理のための包括的なTypeScript SDKです。

## 特徴

- 🔒 **型安全性**: 完全なTypeScript型定義によるコンパイル時エラー検出
- 🚀 **包括的API**: 40以上のDTO、35以上のAPIエンドポイント対応
- 🛡️ **バリデーション**: 強力なデータバリデーション機能
- 📱 **React対応**: React Hooks（オプション）サポート
- 🌐 **国際化**: 日本語エラーメッセージ・表示名対応
- ⚡ **軽量**: ゼロ依存関係（Reactは除く）

## インストール

```bash
npm install @sfr/crypto-sdk
# または
yarn add @sfr/crypto-sdk
```

## 基本使用方法

### APIクライアントの初期化

```typescript
import { SfrCryptoApiClient } from '@sfr/crypto-sdk';

const client = new SfrCryptoApiClient({
  baseURL: 'https://api.sfr.example.com/v1',
  token: 'your-jwt-token-here'
});
```

### ユーザー残高の取得

```typescript
import { UserBalanceDto } from '@sfr/crypto-sdk';

try {
  const response = await client.getUserBalance('user-uuid');
  const balance: UserBalanceDto = response.data;
  
  console.log(`現在残高: ${balance.currentBalance} SFR`);
  console.log(`累計獲得: ${balance.totalEarned} SFR`);
} catch (error) {
  console.error('残高取得エラー:', error.message);
}
```

### 報酬の発行

```typescript
import { RewardIssueRequestDto } from '@sfr/crypto-sdk';

const rewardRequest: RewardIssueRequestDto = {
  userId: 'user-uuid',
  activityScore: 85,
  evaluationScore: 4.2,
  rewardReason: '優秀な学習活動による報酬'
};

try {
  const response = await client.issueReward(rewardRequest);
  console.log(`報酬発行完了: ${response.rewardAmount} SFR`);
} catch (error) {
  console.error('報酬発行エラー:', error.message);
}
```

### ガバナンス提案の作成

```typescript
import { CreateProposalRequestDto, ProposalType } from '@sfr/crypto-sdk';

const proposalRequest: CreateProposalRequestDto = {
  title: 'SFR徴収率の調整について',
  description: '現在の徴収率0.1%を0.15%に変更する提案',
  proposalType: ProposalType.PARAMETER,
  votingDurationHours: 168, // 1週間
};

try {
  const response = await client.createProposal(proposalRequest);
  console.log(`提案作成完了: ${response.proposalId}`);
} catch (error) {
  console.error('提案作成エラー:', error.message);
}
```

## データバリデーション

### SFR金額のバリデーション

```typescript
import { validateSFRAmount, ValidationError } from '@sfr/crypto-sdk';

const amount = '123.45678901';
const validation = validateSFRAmount(amount);

if (!validation.isValid) {
  console.error('バリデーションエラー:', validation.errors);
  throw new ValidationError(validation.errors);
}
```

### 複合バリデーション

```typescript
import { 
  validateSFRAmount, 
  validateUUID, 
  mergeValidationResults,
  throwIfInvalid 
} from '@sfr/crypto-sdk';

const amountValidation = validateSFRAmount('100.5');
const userIdValidation = validateUUID('550e8400-e29b-41d4-a716-446655440000');

const overallValidation = mergeValidationResults(
  amountValidation,
  userIdValidation
);

// エラーがあれば例外をスロー
throwIfInvalid(overallValidation);
```

## データフォーマット

### SFR金額のフォーマット

```typescript
import { formatSFRAmount } from '@sfr/crypto-sdk';

const amount = '123.45678901';

// 基本フォーマット
const basic = formatSFRAmount(amount);
// 出力: "123.45678901 SFR"

// カスタムフォーマット
const custom = formatSFRAmount(amount, {
  decimals: 2,
  thousandsSeparator: true,
  currency: false
});
// 出力: "123.46"
```

### 日時のフォーマット

```typescript
import { formatDate, formatDateTime, formatRelativeTime } from '@sfr/crypto-sdk';

const date = '2024-12-20T15:30:00';

const dateOnly = formatDate(date);
// 出力: "2024年12月20日"

const dateTime = formatDateTime(date);
// 出力: "2024年12月20日 15:30:00"

const relative = formatRelativeTime(date);
// 出力: "3時間前" (現在時刻によって変動)
```

## Enum表示名

```typescript
import { 
  getTransactionTypeDisplayName,
  getProposalStatusDisplayName,
  TransactionType,
  ProposalStatus 
} from '@sfr/crypto-sdk';

const transactionName = getTransactionTypeDisplayName(TransactionType.EARN);
// 出力: "報酬獲得"

const statusName = getProposalStatusDisplayName(ProposalStatus.VOTING);
// 出力: "投票中"
```

## React Hooks（React使用時）

```typescript
import { useUserBalance, useProposals } from '@sfr/crypto-sdk';

function UserDashboard({ userId }: { userId: string }) {
  const { data: balance, loading, error, retry } = useUserBalance(userId);
  const { data: proposals } = useProposals({ status: 'VOTING' });

  if (loading) return <div>読み込み中...</div>;
  if (error) return <div>エラー: {error.message}</div>;

  return (
    <div>
      <h2>残高: {balance?.currentBalance} SFR</h2>
      <h3>投票中の提案: {proposals?.data.length}件</h3>
    </div>
  );
}
```

## エラーハンドリング

```typescript
import { SfrApiError, getLocalizedErrorMessage } from '@sfr/crypto-sdk';

try {
  await client.transferTokens({
    fromUserId: 'user1',
    toUserId: 'user2',
    amount: '100.0',
    reason: '送金テスト'
  });
} catch (error) {
  if (error instanceof SfrApiError) {
    const localizedMessage = getLocalizedErrorMessage(error);
    console.error('API Error:', localizedMessage);
    
    // HTTPステータスコードで分岐
    switch (error.status) {
      case 400:
        console.error('リクエストが無効です');
        break;
      case 401:
        console.error('認証が必要です');
        break;
      case 403:
        console.error('権限がありません');
        break;
      case 500:
        console.error('サーバーエラーです');
        break;
    }
  }
}
```

## 便利なエイリアス

```typescript
import { SFR } from '@sfr/crypto-sdk';

// クライアント作成
const client = SFR.createClient({
  baseURL: 'https://api.example.com',
  token: 'token'
});

// バリデーション
const isValidAmount = SFR.validate.amount('100.5').isValid;
const isValidUUID = SFR.validate.uuid('550e8400-e29b-41d4-a716-446655440000').isValid;

// フォーマット
const formattedAmount = SFR.format.amount('123.456');
const formattedDate = SFR.format.date('2024-12-20');

// 表示名
const transactionName = SFR.display.transactionType('EARN');
```

## 設定例

### 環境設定

```typescript
// 開発環境
const devClient = new SfrCryptoApiClient({
  baseURL: 'https://dev-api.sfr.example.com/v1',
  timeout: 10000,
});

// 本番環境
const prodClient = new SfrCryptoApiClient({
  baseURL: 'https://api.sfr.example.com/v1',
  timeout: 30000,
});
```

### 認証トークンの動的設定

```typescript
const client = new SfrCryptoApiClient();

// ログイン後にトークンを設定
function handleLogin(token: string) {
  client.setAuthToken(token);
}

// ログアウト時にトークンを削除
function handleLogout() {
  client.removeAuthToken();
}
```

## ページネーション

```typescript
import { hasNextPage, getNextPage } from '@sfr/crypto-sdk';

let page = 1;
let allData = [];

while (true) {
  const response = await client.getBalanceHistory(userId, { 
    page, 
    limit: 50 
  });
  
  allData.push(...response.data);
  
  if (!hasNextPage(response)) {
    break;
  }
  
  page = getNextPage(response)!;
}

console.log(`全${allData.length}件のデータを取得`);
```

## 型定義

SDKには以下の主要な型が含まれています：

### 基底型
- `UUID`: UUIDv4文字列
- `SFRAmount`: SFR金額（文字列、8桁小数）
- `DateString`: 日付文字列（YYYY-MM-DD）
- `DateTimeString`: 日時文字列（YYYY-MM-DDTHH:mm:ss）

### DTO（Data Transfer Objects）
- Token Management: `UserBalanceDto`, `TransferRequestDto`, etc.
- Rewards System: `RewardIssueRequestDto`, `RewardHistoryDto`, etc.
- Collections System: `CollectionRequestDto`, `BurnDecisionResponseDto`, etc.
- Governance: `ProposalDto`, `VoteRequestDto`, `CouncilMemberDto`, etc.
- Statistics: `StatsOverviewDto`, `CirculationStatsDto`, etc.

### Enums
- `TransactionType`: 取引種別
- `ProposalStatus`: 提案ステータス
- `VoteChoice`: 投票選択
- `StatsPeriod`: 統計期間
- その他多数

## 開発・貢献

```bash
# 開発環境セットアップ
git clone https://github.com/sfr-project/crypto-sdk.git
cd crypto-sdk
npm install

# ビルド
npm run build

# テスト実行
npm run test

# リント
npm run lint

# ドキュメント生成
npm run docs
```

## ライセンス

MIT License

## サポート

- 📖 [ドキュメント](https://docs.sfr-project.com/sdk)
- 🐛 [Issues](https://github.com/sfr-project/crypto-sdk/issues)
- 💬 [Discussions](https://github.com/sfr-project/crypto-sdk/discussions)
- 📧 [メール](mailto:support@sfr-project.com)

## 更新履歴

### v1.0.0 (2025-01-20)
- 初回リリース
- 完全なTypeScript型定義
- 35以上のAPIエンドポイント対応
- React Hooks サポート
- 包括的なバリデーション機能
