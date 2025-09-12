# 評議員制度 機能設計書 (Council Governance Module)

最終更新: 2025-09-01  バージョン: 1.2

---

## 1. ファイル目的

本書は sfr.tokyo の評議員 (Council) 制度に関する制度要件 / 技術要件 / アルゴリズム / イベント / API / 報酬決定 / PoA 署名モデルを統合し、実装一貫性と変更容易性を確保するための基礎仕様を提供する。

## 2. 制度概要

| 項目 | 現行仕様 | 備考 |
|------|----------|------|
| 定員 | 最大 6 名 | 偶数固定（多数決回避） |
| 任命単位 | キャラクター | キャラ = 表現人格 |
| 初期構成 | 運営任命 3 名 (特例 3 年) | サービス初年度のみ |
| 通常任期 | 2 年 | ずらし任期 (毎年2名入替) |
| 再任 | 可能 | 連続制限なし (将来上限制御パラメータ化) |
| 選挙方式 | 自薦 + 公約公開 + Q&A | PRE_ELECTION フェーズ |
| 投票権 | 1ユーザー1票 | 将来 weighting 拡張検討 |
| 決定方式 | 議論 → 合意 / 分岐時 AI+運営裁定 | 多数決廃止 |
| 報酬 | SFR トークン | base + 評価加重 |
| 報酬レンジ表示 | JPY 換算レンジ | AI + 運営調整 |

### 2.1 役割境界

| 役割 | 権限 | 責務 |
|------|------|------|
| USER | 投票 / 評価 (期間内) | 候補選好表明・任期評価 |
| COUNCIL | ピア評価 / ブロック署名 | 提言・モデレーション |
| ADMIN | フェーズ遷移 / 報酬確定 / Override | 最終責任 / インシデント対応 |
| AI(サービス) | 補助分析 / 裁定支援 | 評価補助スコア / 合意不能時推奨 |

### 2.2 運営関与範囲

- 規約違反対応 / モデレーション
- 社会貢献プロジェクト選定 (予算内)
- 機能改善ロードマップ提言受理 / 調整

> モデル比喩: 「AI=摂政 / 評議員=関白 / 運営=最終承認者」。多数決依存を避け、説明責任と柔軟性を両立する意図。

---

## 3. ライフサイクルとフェーズ

### 3.2 評価フェーズ（任期末）

| 期間 | 内容 | データ | 検証 |
|------|------|--------|------|
| T_end -14d ~ T_end | ユーザー評価項目提案 | evaluation_item_proposal | 重複ラベル防止 |
| T_end ~ T_end +14d | ユーザー / ピア / 管理者評価 | user_evaluation_score / council_peer_evaluation / admin_evaluation | スコア範囲 0-100 |
| 収集後 | 報酬計算 & finalize | council_reward_record | 必須全スコア揃い |

---

## 4. アルゴリズム

### 4.1 報酬レンジ

```pseudo
base = circulation_supply * sfr_jpy_rate
min_jpy = floor(base * 0.03)
max_jpy = floor(base * 0.05)
```

`sfr_jpy_rate` は オラクル (price feed) or 運営入力 + AI 補助。

### 4.2 最終報酬算定 (パラメータ化済)

```pseudo
W_user   = system_parameters['council.reward.weight.user']
W_peer   = system_parameters['council.reward.weight.peer']
W_admin  = system_parameters['council.reward.weight.admin']
assert approxEqual(W_user + W_peer + W_admin, 1.0)

weighted = W_user * userScoreAvg + W_peer * peerScoreAvg + W_admin * adminScore
finalReward = floor(baseReward * (weighted / 100))
```

重みは `PATCH /api/governance/parameters/{key}` (ADMIN) で更新。更新前に合計=1.0 非負検証。監査は `system_parameter_audit` に記録。

### 4.3 投票資格判定 (パラメータ適用)

```pseudo
eligible = phase == VOTING
           && !alreadyVoted(user,election)
           && sfrBalance(user) >= system_parameters['council.vote.min.balance']
           && activityScoreDays(user,30d) >= system_parameters['council.vote.min.activity_days']
```

### 4.4 ブロック確定 (PoA)

```pseudo
validSignatures = unique(councilSignatures) >= requiredCouncilCount
finalized = validSignatures || adminOverride
```

---

## 5. PoA 連携 / Merkle 監査

報酬コメント (寸評) は `comment_hash` (SHA-256) を葉として Merkle root をブロックに格納。

```ts
interface CouncilRewardRecord { councilId: string; baseReward: number; finalReward: number; evaluation: CouncilEvaluation; commentHash: string; }

function finalizeRewardRecord(record, merkleRoot, block): boolean {
  return verifyCommentIntegrity(record.evaluation.councilComment, merkleRoot,
    block.rewardRecords.map(r => ({ councilId: r.councilId, comment: r.evaluation.councilComment })));
}
```

メリット: 監査再現性 / 後日再計算整合性 / 改ざん検知。

---

## 6. データモデル対応 (DB 参照)

| 概念 | テーブル | 主キー | 備考 |
|------|----------|--------|------|
| 選挙 | council_election | id | フェーズ / 定員 |
| 候補 | council_candidate | id | status 遷移 |
| 公約 | candidate_manifesto | id | details_json/endorsements_json |
| Q&A | manifesto_qa | id | candidate_id 紐付 |
| 投票 | council_vote | id | UNIQUE(election_id,voter_user_id) |
| 評議員 | council_member | id | active 管理 |
| 役割 | council_member_role | id | role_code ENUM 化想定 |
| 評価項目案 | evaluation_item_proposal | id | phase=USER_COLLECTION/LOCKED |
| ユーザー評価 | user_evaluation_score | id | 0-100 clamped |
| ピア評価 | council_peer_evaluation | id | comment optional |
| 管理者評価 | admin_evaluation | id | 1:1 |
| 報酬記録 | council_reward_record | id | finalized フラグ |
| ブロック | council_block | id | index_no 連番 |
| 署名 | council_block_signature | id | block + member |

---

## 7. ドメインイベント (候補)

| Event | トリガ | ペイロード要素 | 消費者 | 目的 |
|-------|--------|----------------|--------|------|
| COUNCIL.ELECTION.CREATED | 選挙作成 | electionId,startAt,seats | Notification | 告知 |
| COUNCIL.ELECTION.PHASE_CHANGED | フェーズ遷移 | electionId,oldPhase,newPhase | UI Cache | 状態同期 |
| COUNCIL.VOTE.CAST | 投票確定 | electionId,candidateId,userId | Analytics | 集計 |
| COUNCIL.MEMBER.APPOINTED | 任命 | memberId,termStart,termEnd | Auth | 権限付与 |
| COUNCIL.EVALUATION.LOCKED | 評価収集完了 | memberId | RewardJob | 報酬計算起動 |
| COUNCIL.REWARD.FINALIZED | 報酬確定 | memberId,finalReward,weighted | Ledger | トークン発行連携 |
| COUNCIL.BLOCK.FINALIZED | ブロック確定 | blockIndex,hash | Explorer | 監査表示 |

---

## 8. エラーモデル / コード案

| Code | HTTP | 説明 | 対処 |
|------|------|------|------|
| CE-001 | 400 | フェーズ外操作 | フェーズ確認 |
| CE-002 | 409 | 二重投票 | UI で不可視化 |
| CE-003 | 404 | 候補不存在 | ID/権限確認 |
| CE-004 | 400 | 評価期間外 | 期間再表示 |
| CE-005 | 409 | 報酬確定済 | 再確定禁止 |
| CE-006 | 409 | 署名不足 finalize | 追加署名待ち |
| CE-007 | 422 | スコア範囲外 | 入力バリデーション |
| CE-008 | 403 | 権限不正 | ロール確認 |

---

## 9. セキュリティ / ガード

| 項目 | 内容 |
|------|------|
| 認証 | JWT 必須 (閲覧一部 Public) |
| アクセス制御 | ROLE_USER / ROLE_COUNCIL / ROLE_ADMIN |
| Rate Limit | 投票 / 評価 POST (IP+User) |
| 署名検証 | ブロック署名 ECDSA (アルゴ更新余地) |
| 冪等 | 投票 UNIQUE 制約 / 報酬 finalize フラグ |
| 監査 | フェーズ遷移 / finalize / override ログ |

---

## 10. パラメータ (system_parameters 実装状況)

| Param Key | 生成キー (DB) | 初期値 (V6) | 型 | 用途 |
|-----------|---------------|-------------|----|------|
| council.reward.weight.user | value_number | 0.50 | DECIMAL | 報酬重み (ユーザー) |
| council.reward.weight.peer | value_number | 0.30 | DECIMAL | 報酬重み (ピア) |
| council.reward.weight.admin | value_number | 0.20 | DECIMAL | 報酬重み (管理) |
| council.vote.min.balance | value_number | 100 | NUMBER | 投票最小 SFR 残高 |
| council.vote.min.activity_days | value_number | 7 | NUMBER | 必要活動日数 |
| council.evaluation.window.days | value_number | 30 | NUMBER | 評価集計 rolling window |
| council.size.max | value_number | 21 | NUMBER | 評議員上限 |

将来予定 (未シード):

| 想定キー | 説明 |
|----------|------|
| council.block.finalization.quorum | ブロック確定署名必要割合 (例 0.667) |
| council.outlier.iqr.multiplier | IQR 乗数 (初期 1.5) |
| council.signature.algorithm | 署名アルゴリズム (secp256k1) |
| council.reward.base.amount | ベース報酬基準額 (将来) |

---

## 11. 実装インタフェース抜粋

```ts
type ElectionPhase = 'PRE_ELECTION' | 'VOTING' | 'COUNTING' | 'POST_ELECTION';
interface CouncilEvaluation { userScore: number; councilScore: number; councilComment: string; adminScore: number; }
interface CouncilRewardRecord { councilId: string; baseReward: number; finalReward: number; evaluation: CouncilEvaluation; commentHash: string; }

function calculateFinalReward(baseReward: number, e: CouncilEvaluation): number {
  const weighted = e.userScore * 0.4 + e.councilScore * 0.3 + e.adminScore * 0.3;
  return Math.floor(baseReward * (weighted / 100));
}
```

## 15. パラメータ外部化 & マイグレーション実績

| 項目 | 状態 |
|------|------|
| V6__create_system_parameters_and_seed_council.sql | 適用済 (重み/閾値/期間/定員) |
| ParameterService | 取得 + TTL キャッシュ + 更新監査 + 重み合計検証 |
| SystemParameterAudit | 監査エンティティ/リポジトリ実装 |
| CouncilParameterController | CRUD(一覧/更新/検証/キャッシュ操作) |

読み込み戦略: リクエスト都度 lazy + TTL(5分) / 更新時個別無効化 / 長時間ジョブ前に `validate/weights`。

監査: `system_parameter_audit` (old/new/changed_by/reason/timestamp)。

権限: 更新系は ROLE_ADMIN (SecurityConfiguration + @PreAuthorize)。

## 12. 将来拡張

| 項目 | 概要 | 優先度 | 状態 |
|------|------|--------|------|
| Weighted Voting | SFR 保有 / 活動加重投票 | M | 検討 |
| Multi-lang Manifesto | 公約 i18n | M | 仕様中 |
| Reputation Integration | 評価履歴→ロール重み | H | 設計中 |
| Signature Algo 多様化 | Ed25519 追加 | L | 未着手 |
| DAO Parameter Votes | reward_weights ガバナンス化 | H | 検討 |

---

## 13. リスク / 対策

| リスク | 影響 | 対策 |
|--------|------|------|
| 投票スパム | 集計歪み | 最低 SFR + 活動閾値 + Rate Limit |
| 評価バイアス | 不公平報酬 | 加重 + 外れ値除外 (IQR) |
| 署名停滞 | ブロック未確定 | admin override + quorum パラメータ化予定 |
| 公約改ざん | 信頼低下 | Manifesto ハッシュ化検討 |
| コメント漏洩 | 評価負荷 | Merkle ハッシュのみ公開案 |

## 16. API 設計同期 (実装差分反映)

| メソッド | パス | 用途 | Auth | 備考 |
|----------|------|------|------|------|
| GET | /api/governance/parameters | パラメータ一覧 (prefix フィルタ) | USER | prefix=? optional |
| GET | /api/governance/parameters/validate/weights | 重み検証 | ADMIN | 合計=1.0 チェック |
| PATCH | /api/governance/parameters/{key} | 数値更新 | ADMIN | 監査 + キャッシュ無効化 |
| GET | /api/governance/parameters/cache/status | キャッシュ状態 | ADMIN | TTL / age |
| POST | /api/governance/parameters/cache/clear | 全キャッシュクリア | ADMIN | |
| POST | /api/governance/parameters/cache/evict/{key} | 単一キャッシュ削除 | ADMIN | |
| POST | /api/governance/parameters/cache/ttl | TTL 変更 | ADMIN | 最小 1000ms |
| POST | /api/council/rewards/finalize | 任期報酬計算実行(予定) | ADMIN | Idempotent-Key 予定 |
| POST | /api/council/blocks | ブロック作成(予定) | COUNCIL+ADMIN | |
| POST | /api/council/blocks/{id}/signatures | 署名追加(予定) | COUNCIL | |
| POST | /api/council/blocks/{id}/finalize | ブロック確定(予定) | ADMIN | 閾値 or override |

---

## 14. TODO (短期)

- [x] reward_weight_* 外部化 (V6)
- [x] Parameter API 実装 & 監査
- [x] 重み検証エンドポイント
- [x] キャッシュ TTL / 操作用 API
- [ ] 統合テスト (更新/監査/TTL)
- [ ] Outlier IQR 実装 & テスト
- [ ] Explorer 仕様起案 (Merkle/署名)
- [ ] Manifesto i18n テーブル案ドラフト

---

（本仕様はスプリント末レビューで差分管理し、パラメータ変更は監査ログへ記録する）

---

## 15. パラメータ外部化 & マイグレーション計画

対象パラメータ:

| Key | 用途 | 初期値 | 変更頻度 | 備考 |
|-----|------|--------|----------|------|
| reward_weight_user | 報酬計算重み | 0.40 | 低 | ガバナンス化予定 |
| reward_weight_peer | 同上 | 0.30 | 低 | 〃 |
| reward_weight_admin | 同上 | 0.30 | 低 | 〃 |
| min_vote_balance | 投票最小保有量 | 1 | 中 | 経済状況で調整 |
| min_vote_activity | 投票活動閾値 | 50 | 中 | ボット対策チューニング |
| evaluation_window_days | 評価収集期間 | 14 | 低 | 法令/運営判断 |
| council_max_size | 評議員定員 | 6 | 低 | 増員時のみ変更 |

保存: 既存 `system_parameters` (想定: key (PK), value, value_type, description, updated_at)。

マイグレーション例 (Flyway/V1__council_parameters.sql):

```sql
INSERT INTO system_parameters (param_key, param_value, value_type, description)
VALUES
 ('reward_weight_user','0.40','DECIMAL','Council reward weight user'),
 ('reward_weight_peer','0.30','DECIMAL','Council reward weight peer'),
 ('reward_weight_admin','0.30','DECIMAL','Council reward weight admin'),
 ('min_vote_balance','1','INTEGER','Minimum SFR balance for voting'),
 ('min_vote_activity','50','INTEGER','Minimum 30d activity score'),
 ('evaluation_window_days','14','INTEGER','Evaluation window length'),
 ('council_max_size','6','INTEGER','Max council members')
ON CONFLICT (param_key) DO NOTHING;
```

アプリ読み込み戦略:

1. 起動時キャッシュ (ParameterService) → 変更検知用 updated_at フィールド ポーリング (例: 60s) or Event。
2. 変更 API 実行時: 監査ログ (who, old, new, reason) 追記 + キャッシュ即時更新。
3. 報酬計算ジョブは計算開始時に最新重みを再取得（長時間バッチの古い値使用防止）。

監査テーブル案: `system_parameter_audit(id, param_key, old_value, new_value, changed_by, changed_at, reason)`。

ロール: 変更は `ROLE_ADMIN` のみ (将来: ガバナンス投票結果が自動 PUT)。

## 16. API 設計同期 (追加 / 修正)

| メソッド | パス | 用途 | Auth | RateLimit | 備考 |
|----------|------|------|------|----------|------|
| GET | /api/council/parameters | パラメータ一覧 | ADMIN | Low | キャッシュ可 |
| GET | /api/council/parameters/{key} | 単一取得 | ADMIN | Low | 404 if missing |
| PUT | /api/council/parameters/{key} | 更新 | ADMIN | Low | 監査記録 |
| POST | /api/council/blocks | ブロック作成 (暫定) | COUNCIL+ADMIN | Medium | 署名前ステージング |
| POST | /api/council/blocks/{id}/signatures | 署名追加 | COUNCIL | Medium | ECDSA 検証 |
| POST | /api/council/blocks/{id}/finalize | 最終確定 | ADMIN | Low | 署名数閾値/override |
| POST | /api/council/rewards/finalize | 任期報酬計算実行 | ADMIN (ジョブ) | Low | Idempotent-Key 必須 |
| GET | /api/council/elections/{id}/phase | 現在フェーズ | Public | High | CDN可 |
| POST | /api/council/elections/{id}/advance | フェーズ遷移 | ADMIN | Low | Guard ルール適用 |

Idempotent-Key: 報酬 finalize / ブロック finalize に HTTP ヘッダ `Idempotency-Key` (UUID) を要求し、リトライ安全化。

エラーパターン追記:

| Code | 意味 |
|------|------|
| CE-009 | 不正遷移要求 |
| CE-010 | Idempotency-Key 重複衝突 (本体差異) |
| CE-011 | 署名検証失敗 |
| CE-012 | パラメータ更新禁止 (ロック) |

## 17. 署名 / Merkle 仕様確定

Merkle Tree:

- ハッシュ: SHA-256
- 葉データ: UTF-8 JSON `{"councilId":"...","comment":"..."}` から `comment` を先に trim → 連続空白正規化後シリアライズ。
- 葉ハッシュ: `H0 = SHA256(json)`
- 同層結合: `H = SHA256(left || right)` (奇数は最後ノードをコピーし自己結合)
- ルート: `merkle_root`
- 順序: `councilId` の昇順 → 安定再現性確保。

PoA ブロック署名:

- 曲線: secp256k1 (既存暗号資産コンポーネントと統一)
- 署名対象メッセージ: `block_header_hash = SHA256(index || prev_hash || timestamp || merkle_root)`
- 形式: 64byte (R||S) 固定長。受信時 (DER 長さ検出) の場合 DER→RS 変換正規化。
- 妥当性検証: 公開鍵は council_member に紐づく `public_key`。複数鍵ローテーションは `council_member_key(history)` テーブルで管理 (active=true 現行)。
- 必要署名数: `ceil(council_active_count * 2 / 3)` (例: 6 名 → 4)。
- Override: ADMIN は `adminOverride=true` で閾値不足でも finalize (監査ログ理由必須)。

検証 API レスポンス例:

```json
{
  "blockId": 123,
  "requiredSignatures": 4,
  "collected": 3,
  "status": "PENDING",
  "merkleRoot": "ab34...",
  "missingCouncilIds": ["c4","c5","c6"]
}
```

## 18. フェーズ遷移ガード / 再実行戦略

状態: PRE_ELECTION → VOTING → COUNTING → POST_ELECTION → CLOSED (内部終端)。

| From | To | 条件 | ガード失敗例 |
|------|----|------|--------------|
| PRE_ELECTION | VOTING | now >= startAt & 候補確定 >=1 | startAt 前, 候補0 |
| VOTING | COUNTING | now > endAt | 期間内 / 既集計 |
| COUNTING | POST_ELECTION | 集計成功 & 監査OK | 集計エラー |
| POST_ELECTION | CLOSED | 任命処理完了 | 未任命残あり |

実装案:

1. 行ロック: `SELECT ... FOR UPDATE` on `council_election` id 遷移時。
2. バージョン列 (optimistic) + リトライ最大3回。
3. 遷移履歴テーブル `election_phase_audit(election_id, from_phase, to_phase, actor, at)`。
4. 遷移 API は冪等: 既に To 状態なら 200 + no-op。

再実行 (報酬計算 / ブロック生成):

- Idempotency-Key で `idempotent_request(request_hash, key, result_ref)` 保持。
- 同一 Key で Body 差異 → CE-010。
- 再計算差異 (パラメータ変更後) は新 Key で再実行、旧結果は `superseded=true` マーク。

## 19. 評価スコア外れ値処理アルゴリズム (IQR 案)

目的: 極端なスコア (悪意/ノイズ) を除外し公平性向上。

適用対象: user_evaluation_score の平均算出前。ピア/管理者は件数少のため除外しない。

IQR 手順:

1. 対象スコア集合 S (0–100) 抽出 (min 件数 `N_min`=20 未満なら未適用)。
2. ソートし Q1 (25%), Q3 (75%) 算出 (Tukey 法, 線形補間)。
3. IQR = Q3 - Q1。
4. 下限 L = Q1 - 1.5×IQR, 上限 U = Q3 + 1.5×IQR。
5. フィルタ S' = { x ∈ S | L <= x <= U }。
6. |S'| < N_min/2 の場合 → 外れ値除外無効化 (過剰除外防止) し元集合採用。
7. 平均 = floor(avg(S'))。

擬似コード:

```pseudo
scores = fetchUserScores(councilId)
if len(scores) < N_min: return floor(avg(scores))
sorted = sort(scores)
q1 = quantile(sorted, 0.25)
q3 = quantile(sorted, 0.75)
iqr = q3 - q1
L = q1 - 1.5 * iqr
U = q3 + 1.5 * iqr
filtered = [x for x in sorted if x >= L && x <= U]
if len(filtered) < len(scores)/2: filtered = scores
return floor(avg(filtered))
```

監査: 除外されたスコアは `evaluation_outlier_log(council_id, user_id, score, reason, run_id)` に保持 (復元可能性)。

将来拡張: 分位ベース (P10-P90) クリッピング / MAD (Median Absolute Deviation) 比較評価。

## 14.1 TODO 更新 (反映後)

- [x] reward_weight_* 外部化計画記述
- [x] ブロック署名(Merkle/署名仕様) 詳細化
- [x] フェーズ遷移ガード / 再実行戦略記述
- [x] 外れ値アルゴリズム IQR 案記述
- [ ] Migration SQL 実装 & Flyway 適用 (別リポ)
- [ ] API 実装 (controller + service + repository) & 統合テスト
- [ ] Outlier 処理ユニット/統計テスト (境界ケース)
- [ ] Explorer 仕様起案 (ブロック/評価差分表示)
- [ ] i18n スキーマ & マイグレーション

