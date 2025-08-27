# エラーコード一覧

本ドキュメントは `ErrorCode` 列挙体（`com.sfr.tokyo.sfr_backend.exception.ErrorCode`）で定義されている標準化エラーコードと HTTP ステータス・意味・主用途をまとめたものです。
バックエンドはドメイン/バリデーション/認証/リソース種別に応じて `BusinessException` を送出し、`GlobalExceptionHandler` が統一レスポンス(JSON)へ変換します。

レスポンス共通フィールド例:

```jsonc
{
  "timestamp": "2025-08-27T02:40:12.345+09:00",
  "status": 401,
  "error": "AUTH_INVALID_CREDENTIALS",
  "message": "Invalid credentials",
  "traceId": "a1b2c3d4e5f6",
  "path": "/api/v1/auth/authenticate"
}
```

## 区分一覧

- Validation / Format: リクエスト構造・入力値エラー
- Business Rules: 選挙 / マニフェスト / 投票などドメイン固有ルール違反
- Resource: 存在しない/アクセス不可リソース
- Auth: 認証・認可関連
- Generic: 上記に当てはまらないビジネス汎用
- Server: システム内部エラー

## エラーコード詳細

| コード | HTTP | 区分 | デフォルトメッセージ | 主な発生条件 / 例 | クライアント推奨対応 |
|--------|------|------|-----------------------|--------------------|------------------------|
| VALIDATION_ERROR | 400 | Validation | Validation error | Bean Validation 失敗 (`@Valid`) | 入力値再確認・再送 |
| BAD_REQUEST | 400 | Validation | Bad request | 手続き的な不正 / 解析不能ボディ | フォーマット修正 |
| ELECTION_PHASE_INVALID | 400 | Business | Election phase invalid | 不正フェーズでの操作 | UI 側で操作制御更新 |
| ELECTION_TIME_WINDOW | 400 | Business | Outside election time window | 投票/登録許可時間外 | カウントダウン表示/再試行誘導 |
| DUPLICATE_CANDIDATE | 409 | Business | Candidate already registered | 同一ユーザー重複立候補 | 重複警告表示 |
| DUPLICATE_VOTE | 400 | Business | Already voted | 同一ユーザー二重投票 | 既投票状態表示 |
| RESULTS_UNAVAILABLE | 400 | Business | Results not available yet | 集計前の結果参照 | 待機/リロード誘導 |
| MANIFESTO_EDIT_CLOSED | 400 | Business | Manifesto editing closed | 編集期間外 | 編集UI非活性化 |
| MANIFESTO_QA_CLOSED | 400 | Business | Manifesto Q&A closed | Q&A 期間外 | 入力フォーム閉鎖 |
| BUSINESS_RULE_VIOLATION | 400 | Business(Generic) | Business rule violation | 個別コード化未対応のルール違反 | 問題内容表示 |
| VOTER_INSUFFICIENT_BALANCE | 400 | Business | Insufficient SFR balance | 投票最低残高条件未達 | 残高不足警告 / 残高取得導線 |
| VOTER_INSUFFICIENT_ACTIVITY | 400 | Business | Insufficient activity score | 投票最低アクティビティスコア未達 | 活動促進ガイド表示 |
| NOT_FOUND | 404 | Resource | Resource not found | 汎用リソース未存在 | 404 表示 / リダイレクト |
| ELECTION_NOT_FOUND | 404 | Resource | Election not found | 選挙 ID 不存在 | 画面遷移/再取得 |
| CANDIDATE_NOT_FOUND | 404 | Resource | Candidate not found | 候補者 ID 不存在 | 再検索 |
| MANIFESTO_NOT_FOUND | 404 | Resource | Manifesto not found | マニフェスト ID 不存在 | 再検索 |
| FORBIDDEN | 403 | Resource/Auth | Access denied | 権限不足 | 権限要求/ログイン誘導 |
| AUTH_INVALID_CREDENTIALS | 401 | Auth | Invalid credentials | メールまたはパスワード不一致 / 存在しないユーザー | エラーメッセージ表示・再入力 |
| INTERNAL_ERROR | 500 | Server | Internal server error | 想定外例外 | 退避表示 / 再試行 / 問い合わせ |

## 実装上の注意

1. 新しいドメインルール追加時は既存コード再利用より専用コード追加を優先（観測性向上）。
2. メッセージは `BusinessException` 生成時に上書き可。未指定の場合 `ErrorCode#getDefaultMessage()` が採用される。
3. 認証失敗は Spring Security の `AuthenticationException` を `BusinessException(AUTH_INVALID_CREDENTIALS)` にラップし 401 を返却。500 へ漏らさないこと。
4. フロントエンドは `error` フィールドで分岐し UX を最適化（例: `AUTH_INVALID_CREDENTIALS` → パスワードフィールド強調）。
5. 既存汎用 `BUSINESS_RULE_VIOLATION` 使用箇所は観測後、発生頻度高いものから個別コード化する。

## 変更履歴

| 日付 | 変更 | 備考 |
|------|------|------|
| 2025-08-27 | 初版作成 / `AUTH_INVALID_CREDENTIALS` 追記 | 認証失敗ハンドリング整備に合わせドキュメント化 |
| 2025-08-27 | `VOTER_INSUFFICIENT_BALANCE`, `VOTER_INSUFFICIENT_ACTIVITY` 追加 | 投票資格(残高/活動)プレチェック導入 |

---
今後追加予定の候補: RATE_LIMIT_EXCEEDED, TOKEN_EXPIRED, USER_SUSPENDED など。
