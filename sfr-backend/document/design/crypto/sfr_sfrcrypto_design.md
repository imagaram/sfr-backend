# SFR 暗号資産 機能設計書（発行 / 流通 / 徴収 / バーン / リザーブ）

最終更新: 2025-09-01  バージョン: 1.1  対象システム: SFR.TOKYO Core Crypto Domain

---

## 1. 概要 / 目的

SFR (Soundtrack For Revolution) は教育・創造・ガバナンス行動をインセンティブ化する内部経済トークン（将来 ERC-20 / L2 ブリッジ対応予定）。

目的:

- 行動ベース報酬最適化（学習 / 創作 / 評価）
- 貨幣的 + 非貨幣的価値（投票権, 評議員評価影響）
- 需給安定：AI 駆動のバーン / リザーブ調整
- 自律ガバナンス：パラメータ更新を提案 / 投票 / 適用

主要コンポーネント:

- 発行プール管理 (token_pools)
- 活動スコア / 評価スコア統合 (user_activities, user_evaluations)
- 報酬確定 (reward_history)
- 残高 / 履歴 (user_balances, balance_history)
- 徴収 (fee_collections)
- バーン意思決定 (burn_decisions)
- 準備金 / リザーブ (reserve_funds; 追加予定)
- 外部指標 (oracle_feeds)
- パラメータ / 調整 (system_parameters, adjustment_logs)
- ガバナンス (proposals, votes, council_terms)

---

## 2. 用語定義

| 用語 | 定義 | 備考 |
|------|------|------|
| 発行プール (Issuance Pool) | 1 日の最大新規発行許容量 | `token_pools.total_limit` |
| 活動スコア (Activity Score) | 投稿/学習/評価など正規化後合成 | `user_activities.total_activity_score` |
| 評価スコア (Evaluation Score) | 他者からの評価平均 / 重み付 | `user_evaluations.average_score` |
| 統合スコア (Combined Score) | 0.6*評価 + 0.4*活動（可変） | ガバナンス可能パラメータ |
| 徴収 (Collection) | 残高比例 / 動的係数による定期控除 | `fee_collections` |
| バーン (Burn) | トークン供給量縮減 | on/off + 率調整可 |
| リザーブ (Reserve) | 市場安定 / 再分配用積立 | `reserve_funds` 予定 |
| オラクル (Oracle) | 外部価格/流動性データ | `oracle_feeds` |
| パラメータ (System Parameter) | 発行/徴収/バーン制御値 | `system_parameters` |

---

## 3. トークンエコノミクス概要

### 3.1 供給モデル (High Level)

`新規供給 = Σ(当日報酬) - バーン量` で日次決済。目標: インフレ上限 (年間 <= X%) を `annual_inflation_cap` パラメータで管理。

### 3.2 発行プール決定式（例）

```text
pool_limit(d) = base_daily_limit * adj_factor(d) * market_safety_multiplier(d)
where:
  adj_factor(d) = f(activity_velocity_index, evaluation_quality_trend)
  market_safety_multiplier(d) = clamp(target_price / oracle_price , 0.75 , 1.25)
```

### 3.3 スコア正規化

```text
normalized_activity(u) = activity_score(u) / P95(activity_scores)
normalized_evaluation(u) = eval_score(u) / 5.0
combined_score(u) = W_eval * normalized_evaluation(u) + W_activity * normalized_activity(u)
reward(u) = combined_score(u) / Σ(combined_score(all eligible)) * remaining_pool
```

パラメータ: `W_eval=0.6`, `W_activity=0.4` はガバナンス可。閾値: `min_activity_threshold`, `min_eval_participant_count`。

### 3.4 徴収モデル（動的）

```text
if balance(u) > collection_threshold:
  dynamic_rate = base_rate
                 + k_circ * (circulation / target_circulation - 1)
                 + k_vol  * volatility_index
  fee = balance(u) * clamp(dynamic_rate, min_rate, max_rate)
  destination = AI_decision in {BURN, RESERVE, REDISTRIBUTE}
```

### 3.5 バーン判定 AI 概要

特徴量例: `circulation_growth`, `price_deviation`, `issuance_utilization`, `reserve_ratio`。モデル出力 `burn_score ∈ [0,1]` をしきい値判定。

---

## 4. 機能一覧

| 機能 | 説明 | 主テーブル | 主API (想定) | 状態/補足 |
|------|------|------------|-------------|-----------|
| 発行確定 | 日次スコアから報酬配分 | token_pools, reward_history | POST /api/v1/crypto/issue/close | ACTIVE→COMPLETED |
| 活動集計 | 投稿/学習/評価集約 | user_activities | POST /api/v1/crypto/activity/ingest | 冪等 (activity_id) |
| 評価集計 | 個別評価蓄積/平均更新 | evaluation_scores,user_evaluations | POST /api/v1/crypto/evaluation | 重複防止 evaluation_id |
| 残高更新 | 報酬/支出/バーン反映 | user_balances,balance_history | POST /api/v1/crypto/balance/apply | トランザクション境界 |
| 徴収処理 | 動的料率徴収 | fee_collections | POST /api/v1/crypto/collect/run | 1日1回/ユーザー |
| バーン決定 | AIモデル推論ログ化 | burn_decisions | POST /api/v1/crypto/burn/decision | explainability 付与 |
| リザーブ積立 | 手数料/広告流入記録 | reserve_funds | POST /api/v1/crypto/reserve/record | consolidated 日次 |
| パラメータ変更 | 提案→投票→適用 | system_parameters,adjustment_logs,proposals,votes | POST /api/v1/crypto/params/apply | ガバナンスフロー |
| オラクル同期 | 価格/流動性取得 | oracle_feeds | POST /api/v1/crypto/oracle/ingest | 供給AI依存 |

---

## 5. アルゴリズム詳細

### 5.1 発行（Pseudo）

```pseudo
load pool_limit, issued_amount
remaining = pool_limit - issued_amount
if remaining <= 0: mark pool COMPLETED; exit
scores = buildCombinedScores(eligible_users)
total = sum(scores.values)
for u in scores:
  share = scores[u] / total
  reward = roundDown(share * remaining, 8)
  persist reward_history(u)
  update user_balances (+reward)
update token_pools.issued_amount += Σ(reward)
if issued_amount == total_limit => status=COMPLETED
```

### 5.2 徴収

```pseudo
for each user with balance > threshold:
  rate = computeDynamicRate(user, global_metrics)
  fee = roundDown(balance * rate, 8)
  destination = aiRoute(fee)
  if destination == BURN: decrease supply, log burn_decisions link
  elif destination == RESERVE: add reserve_funds inflow
  else: redistribute queue
  update user_balances (-fee, +total_collected)
  append fee_collections
```

### 5.3 バーン判断

```pseudo
features = buildFeatures(circulation, issuance, reserve_ratio, volatility, oracle_price)
burn_score = model.predict(features)
if burn_score > burn_threshold: decision = BURN else RESERVE
log burn_decisions(decision, features, burn_score)
```

### 5.4 リザーブ積立モデル

`reserve_target_ratio = reserve_target / circulation` を維持。過剰時は部分的に再分配 (`REDISTRIBUTE`) へ送る。

---

## 6. パラメータ一覧（抜粋）

| Key | 用途 | 初期値例 | 範囲 / 制約 | ガバナンス | 変更影響 |
|-----|------|----------|-------------|------------|----------|
| base_daily_limit | 日次発行基準 | 100000.0 | >0 | YES | 供給量 |
| W_eval / W_activity | スコア重み | 0.6 / 0.4 | 0-1, 和=1 | YES | 報酬分布偏重 |
| collection_threshold | 徴収閾値 | 50.0 | >=0 | YES | 小口免除 |
| base_rate | 基本徴収率 | 0.0050 | 0-0.05 | YES | 徴収総額 |
| min_rate / max_rate | 動的率上下限 | 0.002 / 0.02 | 0 < min < max | YES | 変動幅 |
| burn_threshold | バーン判定閾値 | 0.65 | 0-1 | YES | 供給収縮速度 |
| annual_inflation_cap | 年間インフレ上限 | 0.08 | 0-0.5 | YES | 長期供給 |
| reserve_target_ratio | リザーブ比率目標 | 0.12 | 0-1 | YES | 安定性 |
| p95_window_days | P95 計算期間 | 30 | >0 | NO | 平滑性 |

---

## 7. ドメインイベント

| Event | 発火条件 | Payload 主キー | 消費者 | 目的 |
|-------|----------|----------------|--------|------|
| SFR.ACTIVITY.INGESTED | 活動集計完了 | activity_id,user_id,score | IssuanceJob | 次ステップ入力 |
| SFR.ISSUANCE.CALCULATED | 報酬計算完了 | user_id,reward,pool_date | BalanceUpdater, Analytics | 可観測性 |
| SFR.COLLECTION.EXECUTED | 徴収確定 | user_id,fee,destination | ReserveService,BurnService | 供給調整 |
| SFR.BURN.DECIDED | AI 判定生成 | decision_id,result,score | IssuanceJob, Dashboard | 意思決定トレース |
| SFR.PARAM.UPDATED | ガバナンス適用 | parameter_name,new_value | All services | 動的再構成 |
| SFR.ORACLE.INGESTED | 外部指標更新 | source,value,timestamp | AI Model, RateCalc | モデル入力 |

---

## 8. エラーモデル / コード案

| Code | HTTP | 意味 | 対応策 |
|------|------|------|--------|
| SFRE-001 | 400 | 不正スコア入力 | 正規化前検証強化 |
| SFRE-002 | 409 | 二重発行要求 | 冪等キー確認 |
| SFRE-003 | 422 | プール残量不足 | 再スケジュール |
| SFRE-004 | 403 | 徴収免除ユーザー | 状態確認 |
| SFRE-005 | 503 | オラクル未更新 | リトライ / フォールバック |
| SFRE-006 | 500 | モデル推論失敗 | デフォルト方針使用 |

---

## 9. API マッピング（対象サービス統合）

| API | 操作 | 備考 |
|-----|------|------|
| GET /api/v1/crypto/balance | 残高取得 | フロントダッシュボード |
| GET /api/v1/crypto/balance-history | 残高履歴 | ページング |
| POST /api/v1/crypto/activity/ingest | 活動送信 | 冪等 activity_id |
| POST /api/v1/crypto/issue/close | 日次発行確定 | CRON / 手動 |
| POST /api/v1/crypto/collect/run | 徴収バッチ | CRON |
| POST /api/v1/crypto/burn/decision | バーン判定実行 | AI / ルール fallback |
| POST /api/v1/crypto/reserve/record | リザーブ反映 | 決済連携 |
| POST /api/v1/crypto/params/apply | パラメータ適用 | 監査ログ必須 |
| POST /api/v1/crypto/oracle/ingest | 価格/流動性入力 | 署名検証 |

---

## 10. セキュリティ / 監査

| 項目 | 内容 |
|------|------|
| 認可 | RBAC + 評議員ロール + システム内部署名キー |
| 署名 | オラクル / バッチは HMAC-SHA256 ヘッダ `X-SFR-SIGNATURE` |
| 冪等 | 発行・活動・徴収は idempotency key (activity_id, reward_id 等) |
| 監査 | 全パラメータ変更は `adjustment_logs` + trace id |
| 整合性 | 発行合計 = Σ(reward_history.reward_amount) 日次整合検査 |
| インシデント | 供給異常 > 1% 即時フラグ → 自動 SAFE モード (新規発行停止) |

---

## 11. ガバナンスフロー（簡略）

1. 提案作成 (proposal_type=PARAMETER)
2. 投票期間 (quorum + threshold 判定)
3. 可決 → `adjustment_logs` へ適用予約
4. 実行バッチ適用 → `system_parameters` 更新 → イベント発火 `SFR.PARAM.UPDATED`

---

## 12. テスト戦略

| レイヤ | 内容 | ツール |
|--------|------|-------|
| 単体 | スコア計算 / 端数丸め / 係数境界 | JUnit, Property-based |
| 統合 | 日次発行→残高→履歴 | Testcontainers (DB) |
| 負荷 | 10万ユーザー活動集計 | Gatling / k6 |
| モデル | バーン判定精度 / DR シミュレーション | Python sidecar |
| 回帰 | パラメータ変更後供給整合 | Golden snapshot |

---

## 13. 将来拡張（優先度 / 状態）

| 項目 | 説明 | 優先度 | 状態 |
|------|------|--------|------|
| オフチェーン→オンチェーンブリッジ | L2 デプロイ & クロスチェーン | M | 構想 |
| AI 強化 (Explainable) | SHAP 値ロギング | M | 仕様中 |
| 再分配ポリシー | 教育者 / 評議員 / クリエイター比率 | H | 設計初期 |
| 価格安定メカニズム v2 | TWAP / AMM 連動 | M | 構想 |
| ガバナンス重み動的化 | 評価実績による voting_power | H | 実装中 |

元の「拡張構想」は上表へ統合。詳細未定義項目は backlog 管理。

---

## 14. テーブル命名マッピング（設計 ⇄ 実装）

| 設計名 | 実装テーブル | 備考 |
|--------|--------------|------|
| UserActivity | user_activities | activity_id 複合キー表現 |
| UserEvaluation | user_evaluations | 集約値 |
| EvaluationScore | evaluation_scores | 個別評価 |
| TokenPool | token_pools | 日次上限 |
| RewardHistory | reward_history | 報酬確定 |
| UserBalanceHistory | balance_history | 種別列 transaction_type |
| BurnDecisionLog | burn_decisions | AI ログ |
| ReserveFund | reserve_funds(予定) | 追加 PR 予定 |

---

## 15. TODO (短期)

- [ ] reserve_funds テーブル実装 & マイグレーション
- [ ] collection_rate 計算ユニットテスト境界ケース (最小/最大)
- [ ] burn_decisions に explainability JSON フィールド追加
- [ ] system_parameters に version 列追加（楽観ロック）
- [ ] p95 正規化を分位点キャッシュ化し DB IO 削減

---

（本ドキュメントは継続的に 1 Sprint 毎にレビューし、パラメータ/アルゴリズム差分を記録する）
