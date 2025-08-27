# 評議員機能 DB 設計書 (Council Module RDB Schema)

最終更新: 2025-08-26
対象: `council` (評議員 / 選挙 / 評価 / 報酬 / PoAブロック)

## 1. 設計方針

- 正規化優先 + 一部 UX 目的の冗長列 (集計キャッシュ / weighted_score など)
- 監査性・再計算可能性: 元データ(個別評価) → 集計列は再構築可能
- 変更容易性: 初期は JSON カラムで柔軟性確保 (manifesto details / endorsements / QA)
- ID: BIGINT (AUTO INCREMENT) / ハッシュ類は CHAR(64)

## 2. ER 図 (テキスト簡易表現)

```text
Election (1)---(N) Candidate (1)---(1) Manifesto (1)---(N) ManifestoQA
Election (1)---(N) Vote
CouncilMember (1)---(N) CouncilMemberRole
CouncilMember (1)---(1) CouncilRewardRecord (N)---(1) CouncilBlock
CouncilMember (1)---(N) UserEvaluationScore (through EvaluationItemProposal)
CouncilMember (1)---(N) CouncilPeerEvaluation
CouncilMember (1)---(1) AdminEvaluation
CouncilBlock (1)---(N) CouncilBlockSignature
```

## 3. テーブル詳細

### 3.1 選挙関連

#### `council_election`

| 列 | 型 | NOT NULL | 説明 |
|----|----|----------|------|
| id | BIGINT PK | Y | 選挙ID |
| phase | VARCHAR(32) | Y | PRE_ELECTION/VOTING/COUNTING/POST_ELECTION |
| start_at | DATETIME | Y | 選挙開始 (準備期間開始) |
| end_at | DATETIME | Y | 投票終了日時 (以降 COUNTING) |
| vote_count | INT | N | 集計キャッシュ (冗長) |
| seats | INT | Y | 本選挙で選出する定員数 |
| locked | BOOLEAN | Y | 投票締切ロック (true で以降投票不可) |
| created_at | DATETIME | Y | 作成時刻 |
| updated_at | DATETIME | Y | 更新時刻 |

注: INDEX (phase), (start_at), (end_at)

#### `council_candidate`

| 列 | 型 | NOT NULL | 説明 |
|----|----|----------|------|
| id | BIGINT PK | Y | 候補ID |
| election_id | BIGINT FK | Y | -> council_election.id |
| character_id | BIGINT | Y | キャラクターID |
| status | VARCHAR(32) | Y | ACTIVE/WITHDRAWN/ELECTED/NOT_ELECTED |
| published_at | DATETIME | N | 公約公開時刻 |

UNIQUE(election_id, character_id)

#### `candidate_manifesto`

| 列 | 型 | NOT NULL | 説明 |
|----|----|----------|------|
| id | BIGINT PK | Y |  |
| candidate_id | BIGINT FK | Y | -> council_candidate.id |
| title | VARCHAR(200) | Y |  |
| summary | VARCHAR(500) | Y | 要約 |
| details_json | TEXT | Y | 詳細配列 JSON |
| endorsements_json | TEXT | N | 支持者配列 JSON |
| published_at | DATETIME | Y | 公開日時 |

UNIQUE(candidate_id)

#### `manifesto_qa`

| 列 | 型 | NOT NULL | 説明 |
|----|----|----------|------|
| id | BIGINT PK | Y |  |
| candidate_id | BIGINT FK | Y |  |
| question | TEXT | Y |  |
| answer | TEXT | Y |  |
| created_at | DATETIME | Y |  |

INDEX(candidate_id)

#### `council_vote`

| 列 | 型 | NOT NULL | 説明 |
|----|----|----------|------|
| id | BIGINT PK | Y |  |
| election_id | BIGINT FK | Y |  |
| voter_user_id | BIGINT | Y | ユーザーID |
| candidate_id | BIGINT FK | Y | 投票先 |
| voted_at | DATETIME | Y | 投票時刻 |

UNIQUE(election_id, voter_user_id)
INDEX(candidate_id, election_id)

### 3.2 評議員 / 任期

#### `council_member`

| 列 | 型 | NOT NULL | 説明 |
|----|----|----------|------|
| id | BIGINT PK | Y |  |
| character_id | BIGINT | Y | 任命キャラ |
| term_start | DATETIME | Y | 就任 |
| term_end | DATETIME | Y | 任期終了予定 |
| reelected | BOOLEAN | Y | 再任フラグ |
| active | BOOLEAN | Y | 現任中 |
| created_at | DATETIME | Y |  |
| updated_at | DATETIME | Y |  |

UNIQUE(character_id, active=true) (論理 / アプリ層検証)
INDEX(active), INDEX(term_end)

#### `council_member_role`

| 列 | 型 | NOT NULL | 説明 |
| id | BIGINT PK | Y | |
| council_member_id | BIGINT FK | Y | |
| role_code | VARCHAR(40) | Y | ENUM 値 |
UNIQUE(council_member_id, role_code)

### 3.3 評価 / 報酬

#### `evaluation_item_proposal`

| 列 | 型 | NOT NULL | 説明 |
| id | BIGINT PK | Y | |
| council_member_id | BIGINT FK | Y | 対象評議員 |
| label | VARCHAR(120) | Y | |
| description | VARCHAR(500) | N | |
| phase | VARCHAR(16) | Y | USER_COLLECTION/LOCKED |
| created_at | DATETIME | Y | |
INDEX(council_member_id)

#### `user_evaluation_score`

| 列 | 型 | NOT NULL | 説明 |
| id | BIGINT PK | Y | |
| council_member_id | BIGINT FK | Y | |
| user_id | BIGINT | Y | 評価ユーザー |
| item_id | BIGINT FK | Y | 評価項目 |
| score | INT | Y | 0-100 |
UNIQUE(council_member_id, user_id, item_id)
INDEX(council_member_id)

#### `council_peer_evaluation`

| 列 | 型 | NOT NULL | 説明 |
| id | BIGINT PK | Y | |
| target_council_member_id | BIGINT FK | Y | 評価される側 |
| evaluator_council_member_id | BIGINT FK | Y | 評価者 |
| score | INT | Y | |
| comment | TEXT | N | 寸評 |
UNIQUE(target_council_member_id, evaluator_council_member_id)
INDEX(target_council_member_id)

#### `admin_evaluation`

| 列 | 型 | NOT NULL | 説明 |
| id | BIGINT PK | Y | |
| council_member_id | BIGINT FK | Y | |
| score | INT | Y | |
| comment | TEXT | N | |
UNIQUE(council_member_id)

#### `council_reward_record`

| 列 | 型 | NOT NULL | 説明 |
| id | BIGINT PK | Y | |
| council_member_id | BIGINT FK | Y | |
| base_reward_sfr | DECIMAL(38,10) | Y | 基礎報酬 |
| final_reward_sfr | DECIMAL(38,10) | Y | 算出後 |
| user_score_avg | INT | Y | 0-100 |
| peer_score_avg | INT | Y | 0-100 |
| admin_score | INT | Y | 0-100 |
| weighted_score | INT | Y | 0-100 (冗長) |
| comment_hash | CHAR(64) | Y | 評価コメントハッシュ |
| finalized | BOOLEAN | Y | 確定済 |
| block_id | BIGINT FK | N | PoA ブロック |
UNIQUE(council_member_id)
INDEX(finalized), INDEX(block_id)

### 3.4 PoA ブロック

#### `council_block`

| 列 | 型 | NOT NULL | 説明 |
| id | BIGINT PK | Y | |
| index_no | BIGINT | Y | 連番 (チェーン) |
| timestamp | DATETIME | Y | |
| previous_hash | CHAR(64) | Y | |
| hash | CHAR(64) | Y | |
| validator_id | VARCHAR(100) | Y | PoA バリデータ |
| comment_merkle_root | CHAR(64) | Y | 評価コメントルート |
| created_at | DATETIME | Y | |
UNIQUE(index_no)
INDEX(index_no)

#### `council_block_signature`

| 列 | 型 | NOT NULL | 説明 |
| id | BIGINT PK | Y | |
| block_id | BIGINT FK | Y | |
| council_member_id | BIGINT FK | Y | 署名者 |
| signature | VARCHAR(512) | Y | 電子署名 |
UNIQUE(block_id, council_member_id)
INDEX(block_id)

### 3.5 キャッシュ (任意後日)

`election_summary_cache` (省略)

## 4. 主な制約 / ビジネスルール

- 1ユーザー1票: UNIQUE(election_id, voter_user_id)
- 同一任期重複禁止: character_id + active=true をアプリ層/トランザクションで保証
- 評議員定数最大: アクティブ行 COUNT <= 6
- 評価期間: term_end ±14 日（Service 層検証）
- 報酬 finalize 前提: 全必要評価揃っている (user avg, peer avg, admin)
- ブロック finalize: 署名数 >= requiredCouncilCount OR adminOverride

## 5. インデックス戦略

| 目的 | テーブル | INDEX |
| 集計/フェーズ取得 | council_election | (phase), (end_at) |
| 得票集計 | council_vote | (candidate_id, election_id) |
| 評価集計 | user_evaluation_score | (council_member_id) |
| ブロック探索 | council_block | (index_no) |
| 報酬検索 | council_reward_record | (finalized), (block_id) |

## 6. 将来拡張候補

- `validator_node` テーブル追加 (PoA バリデータ一覧)
- manifest 公約国際化: `candidate_manifesto_i18n`
- 評価項目テンプレート: グローバルプリセットテーブル
- 署名方式 ECDSA/Ed25519 複数サポート: `signature_algo` 列追加

## 7. マイグレーション方針

初期: JPA + schema-generation (dev) → 本番/CI: Flyway 導入 (V1__create_council_module.sql)

## 8. データ整合性テスト観点

- 二重投票試行 → ConstraintViolation
- 報酬 finalize 後の再 finalize 拒否
- ブロック index_no 連番欠落チェック (gap 不許可)
- Merkle root 再計算一致

## 9. リスクと対策

| リスク | 対策 |
| JSON カラム無秩序肥大 | サイズ監視 / 正規化移行計画 |
| 評価スパム | 最低 SFR 保有 & アクティビティ条件で API ガード |
| ブロック署名不足で停滞 | adminOverride 手動介入パス |
| 高負荷集計 | 集計結果キャッシュ + バッチ再計算 |

---
疑問点/追加要件が出た場合は本書を更新してください。
