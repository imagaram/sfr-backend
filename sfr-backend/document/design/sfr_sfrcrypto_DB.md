# 🪙 SFR 暗号資産 データベース設計書

**プロジェト**: SFR.TOKYO 暗号資産システム  
**最終更新日**: 2025年8月19日  
**バージョン**: 1.0  
**対象**: SFR（Soundtrack For Revolution）暗号資産の発行・流通・徴収システム

---

## 📋 目次

1. [概要](#概要)
2. [テーブル設計](#テーブル設計)
3. [ER図構造](#er図構造)
4. [インデックス設計](#インデックス設計)
5. [ユースケース別テーブル利用例](#ユースケース別テーブル利用例)
6. [API連携設計](#api連携設計)
7. [セキュリティ・制約](#セキュリティ制約)

---

## 🎯 概要

### 暗号資産SFRの特徴
- **ERC-20準拠**: イーサリアム互換の暗号資産
- **教育・創造インセンティブ**: 学習・創作活動への報酬
- **動的供給調整**: AI判断によるバーン機能
- **ガバナンス機能**: 評議員制度による意思決定

### 主要ユースケース
- 🎓 教育活動参加報酬
- 🎨 作品投稿・販売報酬
- 🗳️ 評議員による評価・投票
- 💳 システム利用料支払い
- 🎁 アイテム購入・プレゼント送付

---

## 🗃️ テーブル設計

### 1. ユーザー活動管理

#### 📊 `user_activities`
ユーザーの日次活動データを記録

```sql
CREATE TABLE user_activities (
    activity_id VARCHAR(64) PRIMARY KEY,  -- "{user_id}_{date}" 形式
    user_id VARCHAR(36) NOT NULL,
    activity_date DATE NOT NULL,
    posts_count INT DEFAULT 0,
    votes_count INT DEFAULT 0,
    learning_hours DECIMAL(4,2) DEFAULT 0.00,
    creation_submissions INT DEFAULT 0,
    evaluation_activities INT DEFAULT 0,
    total_activity_score DECIMAL(8,4) DEFAULT 0.0000,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_activities_user_date (user_id, activity_date),
    INDEX idx_user_activities_score (total_activity_score DESC),
    INDEX idx_user_activities_date (activity_date)
);
```

#### 🏆 `user_evaluations`
ユーザーの評価情報集約テーブル

```sql
CREATE TABLE user_evaluations (
    user_id VARCHAR(36) PRIMARY KEY,
    average_score DECIMAL(3,2) DEFAULT 0.00,  -- 0.00-5.00
    total_evaluations INT DEFAULT 0,
    recent_score DECIMAL(3,2) DEFAULT 0.00,   -- 直近30日平均
    evaluator_count INT DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_evaluations_avg_score (average_score DESC),
    INDEX idx_user_evaluations_recent (recent_score DESC)
);
```

#### ⭐ `evaluation_scores`
個別評価データ（詳細記録）

```sql
CREATE TABLE evaluation_scores (
    evaluation_id VARCHAR(128) PRIMARY KEY,  -- "{user_id}_{evaluator_id}_{timestamp}"
    user_id VARCHAR(36) NOT NULL,
    evaluator_id VARCHAR(36) NOT NULL,
    score DECIMAL(3,2) NOT NULL,  -- 1.00-5.00
    evaluation_type ENUM('LEARNING', 'CREATION', 'CONTRIBUTION', 'GENERAL') NOT NULL,
    comment TEXT,
    evidence_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (evaluator_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_evaluation_scores_user (user_id, created_at DESC),
    INDEX idx_evaluation_scores_evaluator (evaluator_id, created_at DESC),
    INDEX idx_evaluation_scores_type_score (evaluation_type, score DESC),
    
    CONSTRAINT chk_evaluation_score_range CHECK (score >= 1.00 AND score <= 5.00),
    CONSTRAINT chk_no_self_evaluation CHECK (user_id != evaluator_id)
);
```

### 2. SFRトークン管理

#### 💰 `token_pools`
日次発行プール管理

```sql
CREATE TABLE token_pools (
    pool_date DATE PRIMARY KEY,
    total_limit DECIMAL(18,8) NOT NULL,
    issued_amount DECIMAL(18,8) DEFAULT 0.00000000,
    remaining_amount DECIMAL(18,8) AS (total_limit - issued_amount) STORED,
    base_rate DECIMAL(8,6) DEFAULT 1.000000,  -- 基本発行レート
    adjustment_factor DECIMAL(6,4) DEFAULT 1.0000,  -- 調整係数
    status ENUM('ACTIVE', 'COMPLETED', 'SUSPENDED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    
    INDEX idx_token_pools_status (status, pool_date),
    INDEX idx_token_pools_remaining (remaining_amount DESC),
    
    CONSTRAINT chk_token_pool_amounts CHECK (issued_amount <= total_limit),
    CONSTRAINT chk_token_pool_positive CHECK (total_limit > 0)
);
```

#### 💸 `user_balances`
ユーザーSFR残高管理

```sql
CREATE TABLE user_balances (
    user_id VARCHAR(36) PRIMARY KEY,
    current_balance DECIMAL(18,8) DEFAULT 0.00000000,
    total_earned DECIMAL(18,8) DEFAULT 0.00000000,
    total_spent DECIMAL(18,8) DEFAULT 0.00000000,
    total_collected DECIMAL(18,8) DEFAULT 0.00000000,  -- 徴収総額
    last_collection_date DATE NULL,
    collection_exempt BOOLEAN DEFAULT FALSE,  -- 徴収免除フラグ
    frozen BOOLEAN DEFAULT FALSE,  -- 残高凍結フラグ
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_balances_current (current_balance DESC),
    INDEX idx_user_balances_collection_date (last_collection_date),
    INDEX idx_user_balances_frozen (frozen, current_balance DESC),
    
    CONSTRAINT chk_user_balance_non_negative CHECK (current_balance >= 0)
);
```

#### 📈 `balance_history`
残高変動履歴

```sql
CREATE TABLE balance_history (
    history_id VARCHAR(64) PRIMARY KEY,  -- "{user_id}_{timestamp}"
    user_id VARCHAR(36) NOT NULL,
    transaction_type ENUM('EARN', 'SPEND', 'COLLECT', 'BURN', 'TRANSFER') NOT NULL,
    amount DECIMAL(18,8) NOT NULL,
    balance_before DECIMAL(18,8) NOT NULL,
    balance_after DECIMAL(18,8) NOT NULL,
    reason VARCHAR(100) NOT NULL,
    reference_id VARCHAR(64),  -- 関連トランザクションID
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_balance_history_user_time (user_id, created_at DESC),
    INDEX idx_balance_history_type (transaction_type, created_at DESC),
    INDEX idx_balance_history_reference (reference_id)
);
```

### 3. 報酬・徴収システム

#### 🎁 `reward_history`
報酬発行履歴

```sql
CREATE TABLE reward_history (
    reward_id VARCHAR(64) PRIMARY KEY,  -- "{user_id}_{timestamp}"
    user_id VARCHAR(36) NOT NULL,
    pool_date DATE NOT NULL,
    reward_amount DECIMAL(18,8) NOT NULL,
    activity_score DECIMAL(8,4) NOT NULL,
    evaluation_score DECIMAL(3,2) NOT NULL,
    combined_score DECIMAL(8,4) NOT NULL,  -- 0.6*評価 + 0.4*活動
    total_pool_score DECIMAL(12,4) NOT NULL,  -- 当日の全体スコア合計
    reward_reason VARCHAR(200) NOT NULL,
    calculation_details JSON,  -- 計算詳細のJSON
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (pool_date) REFERENCES token_pools(pool_date) ON DELETE RESTRICT,
    INDEX idx_reward_history_user (user_id, created_at DESC),
    INDEX idx_reward_history_pool_date (pool_date, reward_amount DESC),
    INDEX idx_reward_history_amount (reward_amount DESC),
    
    CONSTRAINT chk_reward_positive CHECK (reward_amount > 0)
);
```

#### 💸 `fee_collections`
徴収履歴

```sql
CREATE TABLE fee_collections (
    collection_id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    collection_date DATE NOT NULL,
    balance_before DECIMAL(18,8) NOT NULL,
    collection_rate DECIMAL(6,4) NOT NULL,  -- 徴収率（例：0.0050 = 0.5%）
    collection_amount DECIMAL(18,8) NOT NULL,
    destination ENUM('BURN', 'RESERVE', 'REDISTRIBUTE') NOT NULL,
    ai_decision_id VARCHAR(64),  -- AI判断ログとの紐付け
    collection_reason VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_fee_collections_user (user_id, collection_date DESC),
    INDEX idx_fee_collections_date (collection_date, collection_amount DESC),
    INDEX idx_fee_collections_destination (destination, created_at DESC),
    
    CONSTRAINT chk_collection_positive CHECK (collection_amount > 0)
);
```

#### 🔥 `burn_decisions`
AI判断によるバーン決定ログ

```sql
CREATE TABLE burn_decisions (
    decision_id VARCHAR(64) PRIMARY KEY,
    decision_date DATE NOT NULL,
    total_circulation DECIMAL(18,8) NOT NULL,  -- 流通量
    total_issued DECIMAL(18,8) NOT NULL,      -- 発行総量
    market_price DECIMAL(10,4),               -- 市場価格（参考）
    ai_confidence DECIMAL(5,4) NOT NULL,      -- AI信頼度 0.0000-1.0000
    decision_result ENUM('BURN', 'RESERVE') NOT NULL,
    burned_amount DECIMAL(18,8) DEFAULT 0.00000000,
    reserved_amount DECIMAL(18,8) DEFAULT 0.00000000,
    reasoning TEXT,  -- AI判断理由
    triggered_by VARCHAR(100),  -- 判断トリガー
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_burn_decisions_date (decision_date DESC),
    INDEX idx_burn_decisions_result (decision_result, created_at DESC),
    INDEX idx_burn_decisions_confidence (ai_confidence DESC)
);
```

### 4. ガバナンス機能

#### 🏛️ `council_terms`
評議員任期管理

```sql
CREATE TABLE council_terms (
    term_id VARCHAR(64) PRIMARY KEY,  -- "{user_id}_{start_date}"
    user_id VARCHAR(36) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status ENUM('ACTIVE', 'COMPLETED', 'RESIGNED', 'REMOVED') DEFAULT 'ACTIVE',
    voting_power DECIMAL(8,4) DEFAULT 1.0000,  -- 投票力
    evaluation_count INT DEFAULT 0,  -- 実施した評価数
    proposal_count INT DEFAULT 0,    -- 提出した提案数
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_council_terms_user (user_id, start_date DESC),
    INDEX idx_council_terms_status (status, start_date DESC),
    INDEX idx_council_terms_active (status, end_date),
    
    CONSTRAINT chk_council_term_dates CHECK (end_date > start_date)
);
```

#### 📜 `proposals`
提案管理

```sql
CREATE TABLE proposals (
    proposal_id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    proposal_type ENUM('POLICY', 'PARAMETER', 'FEATURE', 'GOVERNANCE') NOT NULL,
    created_by VARCHAR(36) NOT NULL,
    voting_start DATETIME NOT NULL,
    voting_end DATETIME NOT NULL,
    status ENUM('DRAFT', 'VOTING', 'PASSED', 'REJECTED', 'EXPIRED') DEFAULT 'DRAFT',
    quorum_required INT DEFAULT 3,  -- 必要定足数
    approval_threshold DECIMAL(4,2) DEFAULT 0.60,  -- 可決閾値（60%）
    total_votes INT DEFAULT 0,
    yes_votes INT DEFAULT 0,
    no_votes INT DEFAULT 0,
    abstain_votes INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_proposals_status (status, voting_end DESC),
    INDEX idx_proposals_creator (created_by, created_at DESC),
    INDEX idx_proposals_voting_period (voting_start, voting_end),
    
    CONSTRAINT chk_proposal_voting_period CHECK (voting_end > voting_start)
);
```

#### 🗳️ `votes`
投票記録

```sql
CREATE TABLE votes (
    vote_id VARCHAR(64) PRIMARY KEY,  -- "{proposal_id}_{user_id}"
    proposal_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    vote_choice ENUM('YES', 'NO', 'ABSTAIN') NOT NULL,
    voting_power DECIMAL(8,4) DEFAULT 1.0000,
    vote_weight DECIMAL(8,4) DEFAULT 1.0000,  -- 実際の重み
    comment TEXT,
    cast_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (proposal_id) REFERENCES proposals(proposal_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_votes_proposal (proposal_id, cast_at DESC),
    INDEX idx_votes_user (user_id, cast_at DESC),
    INDEX idx_votes_choice (vote_choice, proposal_id),
    
    UNIQUE KEY unique_user_proposal (proposal_id, user_id)
);
```

### 5. システム管理・監査

#### 🔍 `oracle_feeds`
外部データフィード

```sql
CREATE TABLE oracle_feeds (
    feed_id VARCHAR(64) PRIMARY KEY,  -- "{source}_{timestamp}"
    source VARCHAR(50) NOT NULL,  -- 'COINBASE', 'BINANCE', 'UNISWAP' etc.
    data_type ENUM('PRICE', 'VOLUME', 'LIQUIDITY', 'RATE') NOT NULL,
    value DECIMAL(18,8) NOT NULL,
    confidence DECIMAL(5,4) DEFAULT 1.0000,  -- データ信頼度
    timestamp TIMESTAMP NOT NULL,
    metadata JSON,  -- 追加メタデータ
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_oracle_feeds_source_time (source, timestamp DESC),
    INDEX idx_oracle_feeds_type_time (data_type, timestamp DESC),
    INDEX idx_oracle_feeds_confidence (confidence DESC)
);
```

#### ⚙️ `system_parameters`
システムパラメータ管理

```sql
CREATE TABLE system_parameters (
    parameter_id VARCHAR(64) PRIMARY KEY,
    parameter_name VARCHAR(100) NOT NULL,
    parameter_value TEXT NOT NULL,
    parameter_type ENUM('STRING', 'NUMBER', 'BOOLEAN', 'JSON') NOT NULL,
    description TEXT,
    last_updated_by VARCHAR(36),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (last_updated_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_system_parameters_name (parameter_name),
    UNIQUE KEY unique_parameter_name (parameter_name)
);
```

#### 📊 `adjustment_logs`
パラメータ調整ログ

```sql
CREATE TABLE adjustment_logs (
    adjustment_id VARCHAR(64) PRIMARY KEY,
    parameter_name VARCHAR(100) NOT NULL,
    old_value TEXT,
    new_value TEXT NOT NULL,
    adjustment_reason VARCHAR(200),
    adjusted_by VARCHAR(36),  -- NULL = AI調整
    trigger_type ENUM('MANUAL', 'AUTO_AI', 'ORACLE', 'GOVERNANCE') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (adjusted_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_adjustment_logs_parameter (parameter_name, created_at DESC),
    INDEX idx_adjustment_logs_trigger (trigger_type, created_at DESC)
);
```

---

## 🔗 ER図構造

### テーブル関係図（テキストベース）

```
users (1) ----< user_activities (M)
users (1) ----< user_evaluations (1)
users (1) ----< evaluation_scores (M) >---- users (1) [evaluator]
users (1) ----< user_balances (1)
users (1) ----< balance_history (M)
users (1) ----< reward_history (M) >---- token_pools (1)
users (1) ----< fee_collections (M)
users (1) ----< council_terms (M)
users (1) ----< proposals (M)
users (1) ----< votes (M) >---- proposals (1)

token_pools (1) ----< reward_history (M)
burn_decisions (1) ----< fee_collections (M) [ai_decision_id]

system_parameters (1) ----< adjustment_logs (M)
```

### 主要エンティティ関係

1. **User → Activity/Evaluation**: 1対多関係
2. **User → Balance**: 1対1関係
3. **TokenPool → Reward**: 1対多関係
4. **Proposal → Vote**: 1対多関係
5. **User → Council**: 1対多関係（任期ごと）

---

## 📇 インデックス設計

### パフォーマンス最適化インデックス

#### 📈 高頻度クエリ対応
```sql
-- ユーザー活動スコア検索（日次報酬計算用）
CREATE INDEX idx_user_activities_score_date ON user_activities(activity_date, total_activity_score DESC);

-- 評価スコア集計用
CREATE INDEX idx_evaluation_scores_user_recent ON evaluation_scores(user_id, created_at DESC) 
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY);

-- 残高ランキング用
CREATE INDEX idx_user_balances_ranking ON user_balances(current_balance DESC, user_id);

-- 徴収対象ユーザー特定用
CREATE INDEX idx_collection_targets ON user_balances(current_balance DESC, last_collection_date, collection_exempt)
WHERE current_balance > 100.0 AND collection_exempt = FALSE;
```

#### 🔍 分析・レポート用
```sql
-- 月次報酬分析用
CREATE INDEX idx_reward_monthly ON reward_history(DATE_FORMAT(created_at, '%Y-%m'), reward_amount DESC);

-- トークン流通量分析用
CREATE INDEX idx_balance_circulation ON balance_history(created_at, transaction_type, amount);

-- ガバナンス活動分析用
CREATE INDEX idx_governance_activity ON votes(cast_at, vote_choice, voting_power);
```

---

## 💼 ユースケース別テーブル利用例

### 1. 🎓 日次報酬分配処理

**関連テーブル**: `user_activities`, `user_evaluations`, `token_pools`, `reward_history`

```sql
-- ステップ1: 対象ユーザーのスコア計算
SELECT 
    ua.user_id,
    ua.total_activity_score,
    ue.average_score as evaluation_score,
    (0.6 * ue.average_score + 0.4 * ua.total_activity_score) as combined_score
FROM user_activities ua
JOIN user_evaluations ue ON ua.user_id = ue.user_id
WHERE ua.activity_date = CURDATE()
    AND ua.total_activity_score > 0;

-- ステップ2: 報酬分配実行
INSERT INTO reward_history (reward_id, user_id, pool_date, reward_amount, ...)
SELECT 
    CONCAT(user_id, '_', UNIX_TIMESTAMP()),
    user_id,
    CURDATE(),
    (combined_score / total_score) * pool_amount,
    ...
FROM calculated_scores;
```

### 2. 💸 月次徴収処理

**関連テーブル**: `user_balances`, `fee_collections`, `burn_decisions`

```sql
-- ステップ1: 徴収対象ユーザー特定
SELECT user_id, current_balance
FROM user_balances
WHERE current_balance > 100.0  -- 徴収閾値
    AND collection_exempt = FALSE
    AND (last_collection_date IS NULL 
         OR last_collection_date < DATE_SUB(CURDATE(), INTERVAL 30 DAY));

-- ステップ2: AI判断結果取得
SELECT decision_result, ai_confidence
FROM burn_decisions
WHERE decision_date = CURDATE()
ORDER BY created_at DESC LIMIT 1;

-- ステップ3: 徴収実行
INSERT INTO fee_collections (collection_id, user_id, collection_amount, destination, ...)
VALUES (UUID(), ?, calculated_fee, ai_decision, ...);
```

### 3. 🗳️ ガバナンス投票処理

**関連テーブル**: `proposals`, `votes`, `council_terms`

```sql
-- ステップ1: 投票権確認
SELECT ct.user_id, ct.voting_power
FROM council_terms ct
WHERE ct.status = 'ACTIVE'
    AND CURDATE() BETWEEN ct.start_date AND ct.end_date
    AND ct.user_id = ?;

-- ステップ2: 投票記録
INSERT INTO votes (vote_id, proposal_id, user_id, vote_choice, voting_power)
VALUES (CONCAT(?, '_', ?), ?, ?, ?, ?);

-- ステップ3: 提案集計更新
UPDATE proposals SET 
    total_votes = total_votes + 1,
    yes_votes = yes_votes + CASE WHEN ? = 'YES' THEN 1 ELSE 0 END,
    no_votes = no_votes + CASE WHEN ? = 'NO' THEN 1 ELSE 0 END
WHERE proposal_id = ?;
```

### 4. 📊 SFR統計レポート生成

**関連テーブル**: `user_balances`, `reward_history`, `fee_collections`, `burn_decisions`

```sql
-- 流通量・発行量サマリー
SELECT 
    SUM(current_balance) as total_circulation,
    COUNT(*) as holder_count,
    AVG(current_balance) as avg_balance,
    MAX(current_balance) as max_balance
FROM user_balances
WHERE current_balance > 0;

-- 月次発行・バーン統計
SELECT 
    DATE_FORMAT(created_at, '%Y-%m') as month,
    SUM(reward_amount) as total_issued,
    COUNT(*) as reward_transactions
FROM reward_history
WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH)
GROUP BY DATE_FORMAT(created_at, '%Y-%m');
```

---

## 🔌 API連携設計

### REST API エンドポイント対応

#### 💰 SFR発行 API
```http
POST /api/v1/sfr/issue
Content-Type: application/json

{
  "user_id": "abc-123",
  "activity_score": 0.85,
  "evaluation_score": 4.2
}
```

**対応テーブル**: `user_activities`, `user_evaluations`, `reward_history`

#### 💸 SFR徴収 API
```http
POST /api/v1/sfr/collect
Content-Type: application/json

{
  "user_id": "abc-123", 
  "force_collection": false
}
```

**対応テーブル**: `user_balances`, `fee_collections`, `burn_decisions`

#### 📊 残高照会 API
```http
GET /api/v1/sfr/balance/{user_id}

Response:
{
  "user_id": "abc-123",
  "current_balance": "123.45678900",
  "total_earned": "500.00000000",
  "total_spent": "376.54321100",
  "last_collection_date": "2025-07-15"
}
```

**対応テーブル**: `user_balances`

#### 📈 統計情報 API
```http
GET /api/v1/sfr/stats

Response:
{
  "total_circulation": "50000.12345678",
  "total_holders": 1250,
  "daily_issuance": "100.00000000",
  "burn_rate": "0.0025"
}
```

**対応テーブル**: 複数テーブルからの集計

---

## 🔐 セキュリティ・制約

### データ整合性制約

#### ✅ ビジネスルール制約
```sql
-- 残高非負制約
CONSTRAINT chk_user_balance_non_negative 
CHECK (current_balance >= 0);

-- 評価スコア範囲制約
CONSTRAINT chk_evaluation_score_range 
CHECK (score >= 1.00 AND score <= 5.00);

-- 自己評価禁止
CONSTRAINT chk_no_self_evaluation 
CHECK (user_id != evaluator_id);

-- 発行量上限制約
CONSTRAINT chk_token_pool_amounts 
CHECK (issued_amount <= total_limit);
```

#### 🔒 アクセス制御
```sql
-- 読み取り専用ユーザー（分析用）
CREATE USER 'sfr_analyst'@'%' IDENTIFIED BY 'secure_password';
GRANT SELECT ON sfr_crypto.* TO 'sfr_analyst'@'%';

-- API用ユーザー（制限付き書き込み）
CREATE USER 'sfr_api'@'%' IDENTIFIED BY 'api_secure_password';
GRANT SELECT, INSERT, UPDATE ON sfr_crypto.user_balances TO 'sfr_api'@'%';
GRANT SELECT, INSERT ON sfr_crypto.reward_history TO 'sfr_api'@'%';
GRANT SELECT, INSERT ON sfr_crypto.fee_collections TO 'sfr_api'@'%';
```

### 監査・ログ機能

#### 📝 変更追跡
- `balance_history`: 全残高変動の追跡
- `adjustment_logs`: システムパラメータ変更ログ
- `created_at`, `updated_at`: 全テーブルでタイムスタンプ管理

#### 🚨 異常検知
```sql
-- 異常な残高変動検知
CREATE VIEW suspicious_transactions AS
SELECT bh.* 
FROM balance_history bh
WHERE ABS(bh.amount) > (
    SELECT AVG(amount) + 3 * STDDEV(amount) 
    FROM balance_history 
    WHERE transaction_type = bh.transaction_type
);
```

---

## 📋 運用・メンテナンス

### 定期メンテナンス

#### 🗂️ データアーカイブ
```sql
-- 1年以上前の履歴データアーカイブ
CREATE TABLE balance_history_archive LIKE balance_history;
INSERT INTO balance_history_archive 
SELECT * FROM balance_history 
WHERE created_at < DATE_SUB(CURDATE(), INTERVAL 1 YEAR);
```

#### 📊 統計情報更新
```sql
-- 日次統計更新
CALL update_daily_stats(CURDATE());

-- インデックス最適化
OPTIMIZE TABLE user_activities, reward_history, balance_history;
```

### バックアップ戦略

- **フル バックアップ**: 毎日 3:00 AM
- **差分バックアップ**: 4時間毎
- **重要テーブルレプリケーション**: `user_balances`, `reward_history`

---

*このDB設計書は、SFR暗号資産の安全で効率的な運用を目的として設計されています。定期的な見直しと最適化を継続してください。*
