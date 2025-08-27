-- =============================================================================
-- SFR 暗号資産システム データベーススキーマ
-- 参照: sfr_sfrcrypto_DB.md
-- 作成日: 2025-08-20
-- =============================================================================

-- データベース作成（PostgreSQLの場合）
-- CREATE DATABASE sfr_crypto;

-- =============================================================================
-- 1. ユーザー活動管理
-- =============================================================================

-- ユーザー活動テーブル
CREATE TABLE IF NOT EXISTS user_activities (
    activity_id VARCHAR(64) PRIMARY KEY,  -- "{user_id}_{date}" 形式
    user_id VARCHAR(36) NOT NULL,
    activity_date DATE NOT NULL,
    activity_score DECIMAL(5,2) NOT NULL DEFAULT 0.00,  -- 0-100点
    evaluation_score DECIMAL(3,2) NOT NULL DEFAULT 1.00,  -- 1.0-5.0点
    session_count INTEGER NOT NULL DEFAULT 0,
    total_session_time INTEGER NOT NULL DEFAULT 0,  -- 分単位
    content_created INTEGER NOT NULL DEFAULT 0,
    content_consumed INTEGER NOT NULL DEFAULT 0,
    social_interactions INTEGER NOT NULL DEFAULT 0,
    achievements_unlocked INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_user_activities_user_date UNIQUE (user_id, activity_date),
    CONSTRAINT chk_activity_score CHECK (activity_score >= 0 AND activity_score <= 100),
    CONSTRAINT chk_evaluation_score CHECK (evaluation_score >= 1.0 AND evaluation_score <= 5.0)
);

-- =============================================================================
-- 2. トークン管理
-- =============================================================================

-- ユーザー残高テーブル
CREATE TABLE IF NOT EXISTS user_balances (
    user_id VARCHAR(36) PRIMARY KEY,
    current_balance DECIMAL(20,8) NOT NULL DEFAULT 0.00000000,
    total_earned DECIMAL(20,8) NOT NULL DEFAULT 0.00000000,
    total_spent DECIMAL(20,8) NOT NULL DEFAULT 0.00000000,
    total_collected DECIMAL(20,8) NOT NULL DEFAULT 0.00000000,
    last_collection_date DATE,
    collection_exempt TINYINT(1) NOT NULL DEFAULT 0,
    frozen TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_current_balance CHECK (current_balance >= 0),
    CONSTRAINT chk_total_earned CHECK (total_earned >= 0),
    CONSTRAINT chk_total_spent CHECK (total_spent >= 0),
    CONSTRAINT chk_total_collected CHECK (total_collected >= 0)
);

-- 残高変動履歴テーブル
CREATE TABLE IF NOT EXISTS balance_history (
    history_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,  -- EARN, SPEND, COLLECT, BURN, TRANSFER
    amount DECIMAL(20,8) NOT NULL,
    balance_before DECIMAL(20,8) NOT NULL,
    balance_after DECIMAL(20,8) NOT NULL,
    reason TEXT,
    reference_id VARCHAR(36),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_transaction_type CHECK (transaction_type IN ('EARN', 'SPEND', 'COLLECT', 'BURN', 'TRANSFER')),
    CONSTRAINT chk_amount_not_zero CHECK (amount != 0)
);

-- =============================================================================
-- 3. 報酬システム
-- =============================================================================

-- トークンプールテーブル
CREATE TABLE IF NOT EXISTS token_pools (
    pool_date DATE PRIMARY KEY,
    total_pool_amount DECIMAL(20,8) NOT NULL DEFAULT 1000.00000000,
    distributed_amount DECIMAL(20,8) NOT NULL DEFAULT 0.00000000,
    remaining_amount DECIMAL(20,8) NOT NULL DEFAULT 1000.00000000,
    participant_count INTEGER NOT NULL DEFAULT 0,
    total_score_sum DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    distribution_completed TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_pool_amounts CHECK (
        total_pool_amount >= 0 AND 
        distributed_amount >= 0 AND 
        remaining_amount >= 0 AND
        distributed_amount + remaining_amount = total_pool_amount
    )
);

-- 報酬分配テーブル
CREATE TABLE IF NOT EXISTS reward_distributions (
    reward_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    pool_date DATE NOT NULL,
    activity_score DECIMAL(5,2) NOT NULL,
    evaluation_score DECIMAL(3,2) NOT NULL,
    combined_score DECIMAL(5,2) NOT NULL,
    reward_amount DECIMAL(20,8) NOT NULL,
    reward_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_reward_pool_date FOREIGN KEY (pool_date) REFERENCES token_pools(pool_date),
    CONSTRAINT chk_reward_amount CHECK (reward_amount > 0)
);

-- =============================================================================
-- 4. 徴収システム
-- =============================================================================

-- 徴収履歴テーブル
CREATE TABLE IF NOT EXISTS collection_history (
    collection_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    collection_date DATE NOT NULL,
    collection_amount DECIMAL(20,8) NOT NULL,
    collection_rate DECIMAL(8,6) NOT NULL,
    destination VARCHAR(20) NOT NULL,  -- BURN, RESERVE, REDISTRIBUTE
    collection_reason TEXT,
    ai_decision_id VARCHAR(36),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_collection_destination CHECK (destination IN ('BURN', 'RESERVE', 'REDISTRIBUTE')),
    CONSTRAINT chk_collection_amount CHECK (collection_amount > 0),
    CONSTRAINT chk_collection_rate CHECK (collection_rate > 0 AND collection_rate <= 1)
);

-- AIバーン判断テーブル
CREATE TABLE IF NOT EXISTS burn_decisions (
    decision_id VARCHAR(36) PRIMARY KEY,
    decision_date DATE NOT NULL,
    total_circulation DECIMAL(20,8) NOT NULL,
    total_issued DECIMAL(20,8) NOT NULL,
    market_data JSON,
    ai_confidence DECIMAL(3,2) NOT NULL,
    decision_result VARCHAR(20) NOT NULL,  -- BURN, RESERVE
    burned_amount DECIMAL(20,8) NOT NULL DEFAULT 0,
    reserved_amount DECIMAL(20,8) NOT NULL DEFAULT 0,
    reasoning TEXT,
    triggered_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_decision_result CHECK (decision_result IN ('BURN', 'RESERVE')),
    CONSTRAINT chk_ai_confidence CHECK (ai_confidence >= 0 AND ai_confidence <= 1),
    CONSTRAINT chk_burn_reserve_amounts CHECK (
        (decision_result = 'BURN' AND burned_amount > 0 AND reserved_amount = 0) OR
        (decision_result = 'RESERVE' AND burned_amount = 0 AND reserved_amount > 0)
    )
);

-- =============================================================================
-- 5. ガバナンス
-- =============================================================================

-- 評議員テーブル
CREATE TABLE IF NOT EXISTS council_members (
    term_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE, COMPLETED, RESIGNED, REMOVED
    voting_power DECIMAL(5,2) NOT NULL DEFAULT 1.00,
    evaluation_count INTEGER NOT NULL DEFAULT 0,
    proposal_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_council_status CHECK (status IN ('ACTIVE', 'COMPLETED', 'RESIGNED', 'REMOVED')),
    CONSTRAINT chk_voting_power CHECK (voting_power > 0),
    CONSTRAINT chk_date_range CHECK (end_date > start_date)
);

-- 提案テーブル
CREATE TABLE IF NOT EXISTS governance_proposals (
    proposal_id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    proposal_type VARCHAR(20) NOT NULL,  -- POLICY, PARAMETER, FEATURE, GOVERNANCE
    created_by VARCHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',  -- DRAFT, VOTING, PASSED, REJECTED, EXPIRED
    voting_start TIMESTAMP,
    voting_end TIMESTAMP,
    quorum_required INTEGER NOT NULL DEFAULT 5,
    approval_threshold DECIMAL(3,2) NOT NULL DEFAULT 0.67,
    total_votes INTEGER NOT NULL DEFAULT 0,
    yes_votes INTEGER NOT NULL DEFAULT 0,
    no_votes INTEGER NOT NULL DEFAULT 0,
    abstain_votes INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_proposal_type CHECK (proposal_type IN ('POLICY', 'PARAMETER', 'FEATURE', 'GOVERNANCE')),
    CONSTRAINT chk_proposal_status CHECK (status IN ('DRAFT', 'VOTING', 'PASSED', 'REJECTED', 'EXPIRED')),
    CONSTRAINT chk_approval_threshold CHECK (approval_threshold > 0 AND approval_threshold <= 1),
    CONSTRAINT chk_voting_dates CHECK (voting_end IS NULL OR voting_start IS NULL OR voting_end > voting_start)
);

-- 投票テーブル
CREATE TABLE IF NOT EXISTS governance_votes (
    vote_id VARCHAR(36) PRIMARY KEY,
    proposal_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    vote_choice VARCHAR(10) NOT NULL,  -- YES, NO, ABSTAIN
    voting_power DECIMAL(5,2) NOT NULL,
    vote_weight DECIMAL(8,4) NOT NULL,
    comment TEXT,
    cast_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_vote_proposal FOREIGN KEY (proposal_id) REFERENCES governance_proposals(proposal_id),
    CONSTRAINT chk_vote_choice CHECK (vote_choice IN ('YES', 'NO', 'ABSTAIN')),
    CONSTRAINT uk_vote_user_proposal UNIQUE (proposal_id, user_id)
);

-- =============================================================================
-- 6. 統計・分析
-- =============================================================================

-- 統計サマリーテーブル
CREATE TABLE IF NOT EXISTS stats_summary (
    stats_date DATE PRIMARY KEY,
    period_type VARCHAR(10) NOT NULL,  -- DAILY, WEEKLY, MONTHLY
    total_circulation DECIMAL(20,8) NOT NULL,
    total_issued DECIMAL(20,8) NOT NULL,
    total_burned DECIMAL(20,8) NOT NULL,
    total_holders INTEGER NOT NULL,
    active_holders INTEGER NOT NULL,
    daily_transactions INTEGER NOT NULL,
    average_balance DECIMAL(20,8),
    median_balance DECIMAL(20,8),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_period_type CHECK (period_type IN ('DAILY', 'WEEKLY', 'MONTHLY'))
);

-- =============================================================================
-- 7. Oracle・外部データ
-- =============================================================================

-- Oracleフィードテーブル
CREATE TABLE IF NOT EXISTS oracle_feeds (
    feed_id VARCHAR(36) PRIMARY KEY,
    source VARCHAR(100) NOT NULL,
    data_type VARCHAR(20) NOT NULL,  -- PRICE, VOLUME, LIQUIDITY, RATE
    value DECIMAL(20,8) NOT NULL,
    confidence DECIMAL(3,2) NOT NULL DEFAULT 1.00,
    metadata JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_oracle_data_type CHECK (data_type IN ('PRICE', 'VOLUME', 'LIQUIDITY', 'RATE')),
    CONSTRAINT chk_oracle_confidence CHECK (confidence >= 0 AND confidence <= 1)
);

-- =============================================================================
-- 8. システム管理
-- =============================================================================

-- システムパラメータテーブル
CREATE TABLE IF NOT EXISTS system_parameters (
    parameter_id VARCHAR(36) PRIMARY KEY,
    parameter_name VARCHAR(100) NOT NULL UNIQUE,
    parameter_value TEXT NOT NULL,
    parameter_type VARCHAR(10) NOT NULL,  -- STRING, NUMBER, BOOLEAN, JSON
    description TEXT,
    last_updated_by VARCHAR(36),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_parameter_type CHECK (parameter_type IN ('STRING', 'NUMBER', 'BOOLEAN', 'JSON'))
);

-- 調整ログテーブル
CREATE TABLE IF NOT EXISTS adjustment_logs (
    adjustment_id VARCHAR(36) PRIMARY KEY,
    parameter_name VARCHAR(100) NOT NULL,
    old_value TEXT,
    new_value TEXT NOT NULL,
    adjustment_reason TEXT,
    adjusted_by VARCHAR(36),
    trigger_type VARCHAR(20) NOT NULL,  -- MANUAL, AUTO_AI, ORACLE, GOVERNANCE
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_trigger_type CHECK (trigger_type IN ('MANUAL', 'AUTO_AI', 'ORACLE', 'GOVERNANCE'))
);

-- =============================================================================
-- インデックス作成
-- =============================================================================

-- パフォーマンス向上のためのインデックス
-- 既存テーブルには既にインデックスが存在するため、新規テーブルのみに適用
-- CREATE INDEX idx_user_activities_user_date ON user_activities(user_id, activity_date);
-- CREATE INDEX idx_user_activities_date ON user_activities(activity_date);

-- CREATE INDEX idx_balance_history_user_id ON balance_history(user_id);
-- CREATE INDEX idx_balance_history_created_at ON balance_history(created_at);
-- CREATE INDEX idx_balance_history_type ON balance_history(transaction_type);

-- CREATE INDEX idx_reward_distributions_user_id ON reward_distributions(user_id);
-- CREATE INDEX idx_reward_distributions_pool_date ON reward_distributions(pool_date);

-- CREATE INDEX idx_collection_history_user_id ON collection_history(user_id);
-- CREATE INDEX idx_collection_history_date ON collection_history(collection_date);

-- CREATE INDEX idx_council_members_user_id ON council_members(user_id);
-- CREATE INDEX idx_council_members_status ON council_members(status);

-- CREATE INDEX idx_governance_proposals_status ON governance_proposals(status);
-- CREATE INDEX idx_governance_proposals_created_by ON governance_proposals(created_by);

-- CREATE INDEX idx_governance_votes_proposal_id ON governance_votes(proposal_id);
-- CREATE INDEX idx_governance_votes_user_id ON governance_votes(user_id);

-- CREATE INDEX idx_oracle_feeds_source ON oracle_feeds(source);
-- CREATE INDEX idx_oracle_feeds_data_type ON oracle_feeds(data_type);
-- CREATE INDEX idx_oracle_feeds_created_at ON oracle_feeds(created_at);

-- =============================================================================
-- 初期データ挿入
-- =============================================================================

-- システムパラメータの初期値
INSERT IGNORE INTO system_parameters (parameter_id, parameter_name, parameter_value, parameter_type, description) VALUES
('param_001', 'daily_token_pool', '1000.00000000', 'NUMBER', '日次トークンプール総額'),
('param_002', 'collection_rate', '0.001000', 'NUMBER', '基本徴収率（0.1%）'),
('param_003', 'collection_threshold', '100.00000000', 'NUMBER', '徴収対象最低残高'),
('param_004', 'burn_threshold', '0.75', 'NUMBER', 'バーン判断閾値'),
('param_005', 'governance_quorum', '5', 'NUMBER', 'ガバナンス定足数'),
('param_006', 'max_daily_reward', '500.00000000', 'NUMBER', '個人の日次最大報酬'),
('param_007', 'activity_weight', '0.4', 'NUMBER', '活動スコア重み'),
('param_008', 'evaluation_weight', '0.6', 'NUMBER', '評価スコア重み');

-- 統計サマリーの初期データ
INSERT IGNORE INTO stats_summary (stats_date, period_type, total_circulation, total_issued, total_burned, total_holders, active_holders, daily_transactions) VALUES
(CURRENT_DATE, 'DAILY', 0, 0, 0, 0, 0, 0);

-- =============================================================================
-- ビュー作成（参照用）
-- =============================================================================

-- ユーザー統計ビュー
CREATE OR REPLACE VIEW user_stats_view AS
SELECT 
    ub.user_id,
    ub.current_balance,
    ub.total_earned,
    ub.total_spent,
    ub.total_collected,
    COUNT(rd.reward_id) as total_rewards_received,
    COUNT(ch.collection_id) as total_collections,
    MAX(ua.activity_date) as last_activity_date,
    AVG(ua.activity_score) as avg_activity_score,
    AVG(ua.evaluation_score) as avg_evaluation_score
FROM user_balances ub
LEFT JOIN reward_distributions rd ON ub.user_id = rd.user_id
LEFT JOIN collection_history ch ON ub.user_id = ch.user_id
LEFT JOIN user_activities ua ON ub.user_id = ua.user_id
GROUP BY ub.user_id, ub.current_balance, ub.total_earned, ub.total_spent, ub.total_collected;

-- システム統計ビュー
CREATE OR REPLACE VIEW system_stats_view AS
SELECT 
    (SELECT SUM(current_balance) FROM user_balances) as total_circulation,
    (SELECT SUM(total_earned) FROM user_balances) as total_issued,
    (SELECT SUM(burned_amount) FROM burn_decisions) as total_burned,
    (SELECT COUNT(*) FROM user_balances WHERE current_balance > 0) as total_holders,
    (SELECT COUNT(*) FROM user_balances WHERE updated_at >= DATE_SUB(CURRENT_DATE, INTERVAL 7 DAY)) as active_holders,
    (SELECT COUNT(*) FROM balance_history WHERE created_at >= CURRENT_DATE) as daily_transactions;

-- Flyway V1.1 マイグレーション完了
