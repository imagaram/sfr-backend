暗号資産SFR機能仕様書：発行・流通・徴収アルゴリズム定義

1. 用語定義・前提

SFR（Soundtrack For Revolution）：sfr.tokyoが発行するERC-20準拠の暗号資産。

目的：教育・創造・投票・報酬などの活動に対するインセンティブ提供。

ユースケース例：

教育活動への参加報酬

作品投稿・販売による報酬

評議員による評価・投票

システム利用料支払い

アイテム購入・プレゼント送付

2. 機能一覧と概要

機能名

概要

関連DB

関連API

SFR発行

ユーザー活動に応じたトークン発行

UserActivity, TokenPool

POST /sfr/issue

SFR徴収

保有残高に応じた利用料徴収

UserBalance, FeePolicy

POST /sfr/collect

SFRバーン

AI判断による供給量削減

BurnPolicy

POST /sfr/burn

リザーブ蓄積

決済・広告収入から準備金蓄積

ReserveFund

POST /sfr/reserve

3. アルゴリズム定義（発行・徴収・バーン）

3.1 発行アルゴリズム

for each user:
  score = 0.6 * 評価スコア + 0.4 * 活動量
  配分量 = score / total_score * 発行プール

3.2 徴収アルゴリズム

if user_balance > 徴収閾値:
  rate = 基本料率 + 活動量係数 + 流通量係数
  fee = user_balance * rate
  if AI判断 == 'burn':
    burn(fee)
  else:
    transfer_to_wallet(fee)

3.3 バーン判断ロジック（AI）

input: 流通量, 発行量, 市場価格
output: 'burn' or 'wallet'

4. DB設計連携用セクション（抜粋）

4.1 正規化とインデックス設計

UserActivity

主キー: activity_id（日付＋user_idの複合キー）

外部キー: user_id → User

インデックス: user_id, posts, votes

TokenPool

主キー: date

インデックス: date, total_limit

EvaluationScore

主キー: evaluation_id（user_id＋evaluator_id＋timestamp）

外部キー: user_id, evaluator_id → User

インデックス: user_id, evaluator_id, score

UserEvaluation

主キー: user_id

インデックス: 評価平均, 評価数

CouncilTerm

主キー: term_id（user_id＋start_date）

外部キー: user_id → User

インデックス: user_id, status

RewardHistory

主キー: reward_id（user_id＋timestamp）

外部キー: user_id, source_token_pool_id → TokenPool

インデックス: user_id, reason

TokenState

主キー: token_id

インデックス: state, timestamp

BurnDecisionLog

主キー: decision_id

インデックス: result, triggered_by

UserBalanceHistory

主キー: balance_id（user_id＋date）

外部キー: user_id → User

インデックス: user_id, balance

Proposal

主キー: proposal_id

外部キー: created_by → User

インデックス: status, created_by

Vote

主キー: vote_id

外部キー: proposal_id → Proposal, user_id → User

インデックス: proposal_id, user_id, choice

GovernanceSnapshot

主キー: snapshot_id（user_id＋proposal_id）

外部キー: user_id, proposal_id

インデックス: role, eligibility

OracleFeed

主キー: feed_id（source＋timestamp）

インデックス: source, value

AdjustmentLog

主キー: adjustment_id

インデックス: parameter, timestamp

5. API設計連携用セクション（抜粋）

5.1 POST /sfr/issue

{
  "user_id": "abc-123",
  "score": 0.85
}

5.2 POST /sfr/collect

{
  "user_id": "abc-123",
  "balance": 120.0
}

6. 拡張・将来構想

OpenAPI仕様への落とし込み

動的料率モデル（流通量×徴収率）シミュレーション

コミュニティ投票での料率ガバナンス設計

徴収分の再分配ルール（教育者報酬／評議員報酬）

SFR発生条件の拡張（作品販売、投稿、教育活動、評議員評価など）

SFR使途の拡張（選挙参加、アイテム購入、プレゼント、システム利用料支払い）

SFR交換機能の整備（オフィシャルマーケット、API公開、取引所連携）

オラクル駆動の自動調整（為替レートAPIと連携し、リアルタイムで発行量・バーン率・徴収率をプログラム的に変更）

リザーブ（準備金）による価格下支え（ショッピングカート決済時の徴収や広告収入を原資に蓄積）

マーケティング／パートナーシップ戦略（真っ当に運営していれば企業や取引所側から声がかかる想定）