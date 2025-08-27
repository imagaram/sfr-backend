# 評議員機能 API 設計書 (Council Module REST API)

最終更新: 2025-08-26
ベースパス: `/api/governance/council`

## 1. 基本方針

- 認証: JWT (既存セッション) 必須
- 権限ロール例: USER / COUNCIL / ADMIN
- レスポンス: JSON UTF-8, snake_case or existing project style (既存 DTO に合わせる)
- ページング: `?page=&size=` (Spring Pageable) 必要エンドポイントのみ
- エラー: 共通エラーフォーマット `{ code, message, details? }`

## 2. エンティティと DTO 概要

| モデル | DTO | 説明 |
|--------|-----|------|
| 選挙 | ElectionDto | 現在/指定選挙の基本情報 |
| 候補 | CandidateDto | 候補状態 (得票は結果API) |
| 公約 | ManifestoDto | 候補の公約全文/QA |
| 投票 | VoteResultDto | 集計結果 (POST_ELECTION) |
| 評議員 | CouncilMemberDto | 任期/役割 |
| 評価 | EvaluationSummaryDto | 各スコア集計 |
| 報酬 | RewardRecordDto | 報酬決定内容 |
| ブロック | BlockDto | PoA ブロックメタ |

## 3. エンドポイント詳細

### 3.1 選挙 (Election)

GET /elections/current

- 説明: 現在アクティブ(または最新)選挙を返す
- 認可: PUBLIC (ログイン不要でも可) ※要件次第で USER
- クエリ: `includeCandidates=true|false`
- 200: { election: ElectionDto, candidates?: CandidateDto[] }

POST /elections

- 説明: 新規選挙作成 (PRE_ELECTION)
- 認可: ADMIN
- Body: { startAt, endAt, seats }
- 201: ElectionDto

PATCH /elections/{id}/phase

- 説明: フェーズ遷移
- 認可: ADMIN
- Body: { phase }
- 409: 不正遷移

GET /elections/{id}

- 説明: 指定選挙取得
- 認可: PUBLIC/USER

### 3.2 候補 / 公約 (Candidate / Manifesto)

POST /elections/{id}/candidates

- 自薦エントリー
- 認可: USER (条件: キャラクター所持 & 既に候補でない)
- Body: { characterId }

GET /elections/{id}/candidates

- 候補一覧

GET /candidates/{candidateId}

- 候補詳細単体

POST /candidates/{candidateId}/manifesto

- 公約登録/更新 (フェーズ: PRE_ELECTION)
- 認可: OWNER(候補本人) or ADMIN
- Body: { title, summary, details[], endorsements[] }

GET /candidates/{candidateId}/manifesto

- 公約閲覧

POST /candidates/{candidateId}/manifesto/qa

- Q&A 追加 (PRE_ELECTION)
- 認可: OWNER or ADMIN
- Body: { question, answer }

### 3.3 投票 (Vote)

POST /elections/{id}/vote

- 投票実行 (1ユーザー1票)
- 認可: USER
- Body: { candidateId }
- 400: 期間外 / 資格不足 / 重複投票

GET /elections/{id}/votes/me

- 自分の投票確認

GET /elections/{id}/results

- フェーズ: COUNTING/POST_ELECTION
- 結果: [{ candidateId, voteCount, rank }]

### 3.4 評議員 (Council Members)

GET /members?active=true|false

- 現任/過去の評議員一覧

GET /members/{id}

- 1名詳細

GET /members/{id}/reward

- finalized 報酬

### 3.5 評価 (Evaluations)

POST /members/{id}/evaluation-items

- ユーザー評価項目提案 (期間内)
- Body: { label, description }

GET /members/{id}/evaluation-items

- 項目一覧

POST /members/{id}/user-scores

- 一括送信: [{ itemId, score }]

POST /members/{id}/peer-score

- 評議員同士 (COUNCIL ロール)
- Body: { score, comment }

POST /members/{id}/admin-score

- 管理者
- Body: { score, comment }

GET /members/{id}/evaluation-summary

- 集計: { userAvg, peerAvg, adminScore, weighted }

### 3.6 報酬 (Reward)

GET /members/{id}/reward/preview

- 認可: ADMIN
- 返却: 集計 + baseRewardSfr (入力想定) → finalRewardSfr 計算例

POST /members/{id}/reward/finalize

- 認可: ADMIN
- Body: { baseRewardSfr, commentPlain }
- 処理: commentHash 保存 & weighted 計算 & finalized=true

### 3.7 PoA ブロック (Blocks)

GET /blocks?limit=20&beforeIndex=

- 直近ブロックページング

GET /blocks/{index}

- 指定 index ブロック

POST /blocks

- 認可: ADMIN/SYSTEM
- Body: { index, previousHash, rewardRecordIds[], summaries[], commentMerkleRoot }
- 算出: hash 計算 (サーバ側)

POST /blocks/{id}/signatures

- 認可: COUNCIL
- Body: { signature }

POST /blocks/{id}/finalize

- 認可: ADMIN
- Body: { adminOverride? }

### 3.8 共通 / セキュリティ

- Rate limit: 投票 / 評価投稿エンドポイント (IP + user)
- 署名検証: signature 書式 & council_member 存在検証
- フェーズガード: AOP or Service 前置チェック

## 4. DTO スキーマ (例示 JSON)

ElectionDto:

```json
{
  "id": 12,
  "phase": "VOTING",
  "startAt": "2025-09-01T00:00:00Z",
  "endAt": "2025-10-01T00:00:00Z",
  "seats": 2,
  "voteCount": 345
}
```

CandidateDto:

```json
{
  "id": 88,
  "electionId": 12,
  "characterId": 501,
  "status": "ACTIVE"
}
```

ManifestoDto:

```json
{
  "candidateId": 88,
  "title": "公共性と創作支援の両立",
  "summary": "創作支援インフラを2年で倍増",
  "details": ["AI支援ツール強化", "学習空間UX改善"],
  "endorsements": ["SFR Dev Team"],
  "qa": [{"question":"資金源?","answer":"SFR配分最適化"}]
}
```

RewardRecordDto:

```json
{
  "councilMemberId": 42,
  "baseRewardSfr": "12000.0000000000",
  "finalRewardSfr": "9500.0000000000",
  "userScoreAvg": 78,
  "peerScoreAvg": 81,
  "adminScore": 85,
  "weightedScore": 81,
  "commentHash": "a3f9...",
  "finalized": true
}
```

BlockDto:

```json
{
  "index": 5,
  "timestamp": "2026-01-05T10:00:00Z",
  "previousHash": "0000abc...",
  "hash": "0000def...",
  "validatorId": "validator-node-1",
  "commentMerkleRoot": "ff12...",
  "signatures": [{"councilMemberId":42,"signature":"MEQC..."}],
  "rewardRecordIds": [12,13]
}
```

## 5. バリデーション / エラー例

| ケース | HTTP | code | message |
|--------|------|------|---------|
| 二重投票 | 400 | DUPLICATE_VOTE | Already voted |
| フェーズ外投票 | 400 | INVALID_PHASE | Voting not active |
| 非候補 | 404 | CANDIDATE_NOT_FOUND | Candidate not found |
| 評価期間外 | 400 | EVALUATION_WINDOW_CLOSED | Evaluation window closed |
| 報酬再確定 | 409 | ALREADY_FINALIZED | Reward already finalized |
| ブロック署名不足 finalize | 409 | NOT_ENOUGH_SIGNATURES | Signatures insufficient |

## 6. セキュリティ / オーソリゼーション

| エンドポイントカテゴリ | ロール要件 |
|------------------------|------------|
| 選挙取得/候補閲覧 | PUBLIC/USER |
| 投票 | USER |
| 自薦/公約編集 | USER(本人) or ADMIN |
| 評価 (user) | USER |
| 評価 (peer) | COUNCIL |
| 評価 (admin) | ADMIN |
| 報酬 finalize / ブロック操作 | ADMIN |
| ブロック署名 | COUNCIL |

## 7. 非機能要件

- レイテンシ: 投票 < 200ms (DB Insert + キャッシュ更新)
- スループット: 投票ピークは選挙終盤 (バッチ集計 or カウンタ更新最適化)
- 監査ログ: finalize, phase change, block finalize

## 8. 今後の拡張

- GraphQL 化 (集計/サマリー効率化)
- SSE / WebSocket で投票リアルタイム更新
- 多言語公約 (i18n テーブル) 対応

---
更新時は変更理由と日付を最上部に追記してください。
