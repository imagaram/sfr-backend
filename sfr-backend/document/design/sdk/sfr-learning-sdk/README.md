# SFR Learning API SDK

SFR.TOKYO学習プラットフォーム用のTypeScript/JavaScript SDKです。

## 🚀 インストール

```bash
npm install @sfr-tokyo/learning-sdk
# または
yarn add @sfr-tokyo/learning-sdk
```

## 📖 基本的な使用方法

### 初期化

```typescript
import { SfrLearningSDK } from '@sfr-tokyo/learning-sdk';

// 基本的な初期化
const sdk = new SfrLearningSDK({
  baseURL: 'https://api.sfr.tokyo/api/learning',
  debug: true // 開発時のみ
});

// 認証トークンの設定
sdk.setAccessToken('your-jwt-token');
```

### ファクトリー関数を使用した初期化

```typescript
import { createDevSDK, createProdSDK } from '@sfr-tokyo/learning-sdk';

// 開発環境用
const devSdk = createDevSDK('your-token');

// 本番環境用
const prodSdk = createProdSDK('your-token', 'optional-api-key');
```

## 🎓 主要機能の使用例

### 1. 教材一覧取得

```typescript
// 基本的な取得
const courses = await sdk.getCourses();

// フィルタリング付き取得
const schoolCourses = await sdk.getCourses({
  mode: 'SCHOOL',
  page: 0,
  size: 20
});
```

### 2. 履修登録

```typescript
// 基本的な履修登録
await sdk.enrollCourse('123');

// キャラクター指定での履修登録
await sdk.enrollCourse(123, 'character-uuid');
```

### 3. 評価投稿

```typescript
// 基本的な評価投稿
await sdk.submitEvaluation({
  contentId: 456,
  rating: 5,
  comment: '素晴らしいコンテンツでした！'
});

// キャラクター指定での評価投稿
await sdk.submitEvaluation({
  contentId: 456,
  rating: 4,
  comment: 'とても勉強になりました',
  characterId: 'character-uuid'
});
```

### 4. 学習進捗の記録

```typescript
// コンテンツ完了の記録
await sdk.recordProgress(123, 456, {
  progressType: 'COMPLETED',
  timeSpent: 30,
  rating: 5,
  notes: '完全に理解できました'
});
```

## 🔧 高度な使用方法

### 個別クライアントの使用

```typescript
// 学習空間管理
const spaceDetails = await sdk.spaces.getCourse(123);
await sdk.spaces.updateCourse(123, { name: '新しい名前' });

// 学習コンテンツ管理
const content = await sdk.content.getContent(123);
await sdk.content.createContent(123, {
  title: '新しいレッスン',
  contentType: 'VIDEO',
  difficulty: 'BEGINNER'
});

// クイズ管理
const quizzes = await sdk.quiz.getQuizzes(123);
const result = await sdk.quiz.submitQuizAnswer(123, 456, {
  answers: [
    { questionIndex: 0, answer: 'A' },
    { questionIndex: 1, answer: 'B' }
  ]
});

// 評価管理
const evaluations = await sdk.evaluations.getEvaluations(456);
await sdk.evaluations.updateEvaluation(789, {
  rating: 4,
  comment: '更新されたコメント'
});
```

### ファイルアップロード

```typescript
// コンテンツ作成時のファイルアップロード
const fileInput = document.getElementById('file') as HTMLInputElement;
const file = fileInput.files[0];

await sdk.content.createContent(123, {
  title: 'ビデオレッスン',
  contentType: 'VIDEO',
  difficulty: 'INTERMEDIATE',
  file: file
});
```

### エラーハンドリング

```typescript
try {
  await sdk.enrollCourse('invalid-id');
} catch (error: any) {
  if (error.response?.status === 404) {
    console.error('学習空間が見つかりません');
  } else if (error.response?.status === 409) {
    console.error('既に履修済みです');
  } else {
    console.error('エラーが発生しました:', error.message);
  }
}
```

### 統計情報の取得

```typescript
// 包括的な学習統計
const stats = await sdk.getLearningStats(123);
console.log('進捗率:', stats.completionRate);
console.log('学習時間:', stats.totalTimeSpent);
console.log('獲得成果:', stats.achievements);

// 完全な学習空間情報
const spaceInfo = await sdk.getCompleteSpaceInfo(123);
console.log('学習空間:', spaceInfo.space);
console.log('コンテンツ一覧:', spaceInfo.content);
console.log('統計:', spaceInfo.stats);
```

## 🔐 認証

### JWT認証

```typescript
// ログイン後にトークンを設定
const response = await fetch('/api/v1/auth/authenticate', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email, password })
});
const { access_token } = await response.json();

sdk.setAccessToken(access_token);
```

### トークンの管理

```typescript
// トークンの保存（例：localStorage）
localStorage.setItem('access_token', access_token);

// 起動時にトークンを復元
const savedToken = localStorage.getItem('access_token');
if (savedToken) {
  sdk.setAccessToken(savedToken);
}

// ログアウト時
sdk.clearAccessToken();
localStorage.removeItem('access_token');
```

## 📊 型定義

SDKには完全な型定義が含まれており、TypeScriptでの開発を強力にサポートします。

```typescript
import type {
  LearningSpace,
  LearningContent,
  Quiz,
  EvaluationDto,
  ProgressRecordRequest
} from '@sfr-tokyo/learning-sdk';
```

## 🛠️ 設定オプション

```typescript
const sdk = new SfrLearningSDK({
  baseURL: 'https://api.sfr.tokyo/api/learning',
  timeout: 10000,           // リクエストタイムアウト（ミリ秒）
  retryAttempts: 3,         // リトライ回数
  debug: false,             // デバッグログの有効化
  apiKey: 'optional-key'    // オプションのAPIキー
});
```

## 🔧 開発者向け情報

### ビルド

```bash
npm run build
```

### テスト

```bash
npm test
npm run test:coverage
```

### 型チェック

```bash
npm run type-check
```

## 📝 ライセンス

MIT License

## 🤝 サポート

- 公式ドキュメント: https://docs.sfr.tokyo
- GitHub Issues: https://github.com/sfr-tokyo/learning-sdk/issues
- 開発者フォーラム: https://forum.sfr.tokyo
