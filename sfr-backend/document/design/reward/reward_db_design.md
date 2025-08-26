# SFR報酬システム DB設計書

## 概要

このドキュメントは、SFR暗号資産の報酬システムに関するデータベース設計を記述します。
報酬計算式 `SFR報酬量 = B × C × M × H` を実装するための各種テーブル設計を含みます。

## テーブル一覧

### 1. 貢献記録テーブル (contribution_records)

ユーザーの各種貢献活動を記録するメインテーブル

```sql
CREATE TABLE contribution_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id CHAR(36) NOT NULL COMMENT 'ユーザーID (UUID)',
    contribution_type ENUM(
        'development', 'liquidity', 'governance', 
        'education', 'commerce', 'ux'
    ) NOT NULL COMMENT '貢献タイプ',
    
    -- 貢献詳細
    activity_type VARCHAR(50) NOT NULL COMMENT '具体的な活動タイプ',
    reference_id VARCHAR(100) COMMENT '関連するエンティティのID',
    reference_type VARCHAR(50) COMMENT '関連エンティティのタイプ',
    
    -- メトリクス
    metrics JSON COMMENT '貢献度計算用のメトリクス',
    contribution_score DECIMAL(10,4) NOT NULL COMMENT 'C係数（貢献度スコア）',
    
    -- タイムスタンプ
    activity_date DATETIME NOT NULL COMMENT '活動日時',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_contribution (user_id, contribution_type),
    INDEX idx_activity_date (activity_date),
    INDEX idx_reference (reference_type, reference_id),
    
    FOREIGN KEY (user_id) REFERENCES _user(id)
);
```

### 2. 報酬係数マスタテーブル (reward_factors)

B係数（基本報酬係数）とM係数（市場状況係数）を管理

```sql
CREATE TABLE reward_factors (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    -- B係数（基本報酬係数）
    contribution_type ENUM(
        'development', 'liquidity', 'governance', 
        'education', 'commerce', 'ux'
    ) NOT NULL,
    base_factor DECIMAL(6,4) NOT NULL DEFAULT 1.0000 COMMENT 'B係数',
    
    -- M係数（市場状況係数）用のパラメータ
    target_price DECIMAL(10,2) COMMENT '目標価格（円）',
    price_range_min DECIMAL(10,2) COMMENT '価格帯最小値',
    price_range_max DECIMAL(10,2) COMMENT '価格帯最大値',
    market_factor DECIMAL(6,4) COMMENT 'M係数',
    
    -- 動的調整用
    activity_volume_threshold INT COMMENT '活動量閾値',
    scarcity_multiplier DECIMAL(6,4) DEFAULT 1.0000 COMMENT '希少性倍率',
    
    -- 有効期間
    effective_from DATETIME NOT NULL,
    effective_to DATETIME COMMENT '無効化日時（NULLの場合は現在有効）',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_contribution_type_date (contribution_type, effective_from),
    INDEX idx_effective_period (effective_from, effective_to),
    INDEX idx_price_range (price_range_min, price_range_max)
);
```

### 3. 保有インセンティブテーブル (holding_incentives)

H係数（保有インセンティブ係数）を計算するための保有履歴

```sql
CREATE TABLE holding_incentives (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id CHAR(36) NOT NULL COMMENT 'ユーザーID',
    
    -- 保有期間情報
    holding_start_date DATETIME NOT NULL COMMENT '保有開始日',
    holding_days INT NOT NULL DEFAULT 0 COMMENT '保有日数',
    
    -- 価格支持力計算用
    average_holding_price DECIMAL(15,8) COMMENT '平均保有時価格',
    current_price DECIMAL(15,8) COMMENT '計算時点での価格',
    price_support_ratio DECIMAL(10,6) COMMENT '価格支持力比率',
    
    -- ステーキング情報
    staking_months INT DEFAULT 0 COMMENT 'ステーキング期間（月）',
    staking_amount DECIMAL(20,8) DEFAULT 0 COMMENT 'ステーキング量',
    
    -- SFR決済利用
    payment_usage_count INT DEFAULT 0 COMMENT 'SFR決済利用回数',
    payment_usage_amount DECIMAL(20,8) DEFAULT 0 COMMENT 'SFR決済利用総額',
    
    -- H係数計算結果
    holding_factor DECIMAL(10,6) NOT NULL COMMENT 'H係数',
    
    -- 計算日時
    calculation_date DATETIME NOT NULL COMMENT '係数計算日時',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_calculation (user_id, calculation_date),
    INDEX idx_holding_days (holding_days),
    INDEX idx_staking (staking_months, staking_amount),
    
    FOREIGN KEY (user_id) REFERENCES _user(id)
);
```

### 4. 報酬計算履歴テーブル (reward_calculations)

実際の報酬計算結果を記録

```sql
CREATE TABLE reward_calculations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id CHAR(36) NOT NULL COMMENT 'ユーザーID',
    contribution_record_id BIGINT NOT NULL COMMENT '貢献記録ID',
    
    -- 計算要素
    base_factor DECIMAL(6,4) NOT NULL COMMENT 'B係数',
    contribution_score DECIMAL(10,4) NOT NULL COMMENT 'C係数',
    market_factor DECIMAL(6,4) NOT NULL COMMENT 'M係数',
    holding_factor DECIMAL(10,6) NOT NULL COMMENT 'H係数',
    
    -- 計算結果
    calculated_amount DECIMAL(20,8) NOT NULL COMMENT '計算された報酬量',
    final_amount DECIMAL(20,8) NOT NULL COMMENT '最終配布量（上限調整後）',
    
    -- 計算詳細
    calculation_formula TEXT COMMENT '計算式の詳細',
    market_price_jpy DECIMAL(10,2) COMMENT '計算時のSFR/JPY価格',
    
    -- ステータス
    status ENUM('CALCULATED', 'APPROVED', 'DISTRIBUTED', 'REJECTED') NOT NULL DEFAULT 'CALCULATED',
    
    -- 配布情報
    distribution_id BIGINT COMMENT '配布記録ID（reward_distributionsテーブル）',
    approved_by CHAR(36) COMMENT '承認者ID',
    approved_at DATETIME COMMENT '承認日時',
    distributed_at DATETIME COMMENT '配布完了日時',
    
    -- タイムスタンプ
    calculated_at DATETIME NOT NULL COMMENT '計算日時',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_status (user_id, status),
    INDEX idx_contribution_record (contribution_record_id),
    INDEX idx_calculated_date (calculated_at),
    INDEX idx_distribution (distribution_id),
    
    FOREIGN KEY (user_id) REFERENCES _user(id),
    FOREIGN KEY (contribution_record_id) REFERENCES contribution_records(id),
    FOREIGN KEY (distribution_id) REFERENCES reward_distributions(id)
);
```

### 5. 市場価格履歴テーブル (market_price_history)

M係数計算用の市場価格データ

```sql
CREATE TABLE market_price_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    -- 価格情報
    price_jpy DECIMAL(10,2) NOT NULL COMMENT 'SFR/JPY価格',
    price_source VARCHAR(50) NOT NULL COMMENT '価格取得元',
    
    -- 市場データ
    volume_24h DECIMAL(20,8) COMMENT '24時間取引量',
    market_cap DECIMAL(20,2) COMMENT '時価総額',
    
    -- M係数
    market_factor DECIMAL(6,4) NOT NULL COMMENT '該当価格でのM係数',
    target_price DECIMAL(10,2) NOT NULL COMMENT '基準となる目標価格',
    
    -- タイムスタンプ
    price_timestamp DATETIME NOT NULL COMMENT '価格取得日時',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_price_timestamp (price_timestamp),
    INDEX idx_price_value (price_jpy),
    INDEX idx_market_factor (market_factor)
);
```

### 6. 活動別メトリクステーブル (activity_metrics)

各活動タイプごとの詳細メトリクス

```sql
CREATE TABLE activity_metrics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contribution_record_id BIGINT NOT NULL COMMENT '貢献記録ID',
    
    -- 開発関連メトリクス
    commits_count INT DEFAULT 0 COMMENT 'コミット数',
    merged_prs_count INT DEFAULT 0 COMMENT 'マージされたPR数',
    issues_closed_count INT DEFAULT 0 COMMENT 'クローズしたIssue数',
    code_lines_added INT DEFAULT 0 COMMENT '追加コード行数',
    code_lines_deleted INT DEFAULT 0 COMMENT '削除コード行数',
    
    -- 流動性提供関連
    lp_amount DECIMAL(20,8) DEFAULT 0 COMMENT 'LP提供量',
    lp_duration_days INT DEFAULT 0 COMMENT 'LP提供期間（日）',
    trading_volume DECIMAL(20,8) DEFAULT 0 COMMENT '取引量',
    
    -- ガバナンス関連
    votes_cast_count INT DEFAULT 0 COMMENT '投票回数',
    proposals_submitted_count INT DEFAULT 0 COMMENT '提案提出数',
    discussion_contributions INT DEFAULT 0 COMMENT '議論貢献数',
    
    -- 教育・普及関連
    course_attendees INT DEFAULT 0 COMMENT '講座参加者数',
    course_rating DECIMAL(3,2) DEFAULT 0 COMMENT '講座評価',
    completion_rate DECIMAL(5,2) DEFAULT 0 COMMENT '完了率',
    community_followers INT DEFAULT 0 COMMENT 'コミュニティフォロワー数',
    
    -- 商用利用関連
    sfr_payment_count INT DEFAULT 0 COMMENT 'SFR決済回数',
    sfr_payment_amount DECIMAL(20,8) DEFAULT 0 COMMENT 'SFR決済金額',
    sales_revenue DECIMAL(15,2) DEFAULT 0 COMMENT '売上高',
    usage_continuity_months INT DEFAULT 0 COMMENT '継続利用月数',
    
    -- UX改善関連
    feedback_submitted_count INT DEFAULT 0 COMMENT 'フィードバック提出数',
    feedback_accepted_count INT DEFAULT 0 COMMENT 'フィードバック採用数',
    ui_proposals_count INT DEFAULT 0 COMMENT 'UI提案数',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_contribution_record (contribution_record_id),
    
    FOREIGN KEY (contribution_record_id) REFERENCES contribution_records(id)
);
```

### 7. 報酬配布制限テーブル (reward_distribution_limits)

報酬配布の制限・上限管理

```sql
CREATE TABLE reward_distribution_limits (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    -- 制限タイプ
    limit_type ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'CATEGORY', 'USER') NOT NULL,
    category VARCHAR(50) COMMENT '対象カテゴリ（limit_type=CATEGORYの場合）',
    user_id CHAR(36) COMMENT '対象ユーザー（limit_type=USERの場合）',
    
    -- 制限値
    max_amount DECIMAL(20,8) NOT NULL COMMENT '最大配布量',
    current_amount DECIMAL(20,8) DEFAULT 0 COMMENT '現在の配布済み量',
    
    -- 期間
    period_start DATETIME NOT NULL COMMENT '期間開始',
    period_end DATETIME NOT NULL COMMENT '期間終了',
    
    -- ステータス
    is_active BOOLEAN DEFAULT TRUE COMMENT '有効フラグ',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_limit_type_period (limit_type, period_start, period_end),
    INDEX idx_category_period (category, period_start, period_end),
    INDEX idx_user_period (user_id, period_start, period_end),
    
    FOREIGN KEY (user_id) REFERENCES _user(id)
);
```

## エンティティ関係図

```
_user (1) ←→ (N) contribution_records
contribution_records (1) ←→ (1) activity_metrics
contribution_records (1) ←→ (N) reward_calculations
reward_calculations (N) ←→ (1) reward_distributions
_user (1) ←→ (N) holding_incentives
_user (1) ←→ (N) reward_distribution_limits

market_price_history ←→ reward_factors (価格ベースのM係数計算)
```

## インデックス戦略

### パフォーマンス最適化のためのインデックス

1. **時系列検索用**: `activity_date`, `calculation_date`, `price_timestamp`
2. **ユーザー別検索用**: `user_id` を含む複合インデックス
3. **カテゴリ別集計用**: `contribution_type`, `category`
4. **ステータス別検索用**: `status` を含む複合インデックス

## パーティショニング戦略

大量データ対応のため、以下のテーブルで月次パーティショニングを推奨：

- `contribution_records`: `activity_date`
- `reward_calculations`: `calculated_at`
- `market_price_history`: `price_timestamp`

## 報酬計算フロー

1. **貢献記録**: `contribution_records` + `activity_metrics`
2. **係数取得**: `reward_factors` (B, M係数) + `holding_incentives` (H係数)
3. **報酬計算**: `reward_calculations` での計算実行
4. **制限チェック**: `reward_distribution_limits` での上限確認
5. **配布実行**: `reward_distributions` での配布記録

## データ保持期間

- `contribution_records`: 永続保持（監査用）
- `reward_calculations`: 3年間
- `market_price_history`: 2年間
- `activity_metrics`: 2年間
- `holding_incentives`: 1年間（最新のみ保持）

## セキュリティ考慮事項

1. **個人情報保護**: ユーザーIDは外部キーのみ、詳細情報は`_user`テーブルで管理
2. **改ざん防止**: 重要なテーブルには`created_at`と`updated_at`を必須設定
3. **監査ログ**: 報酬計算と配布の全履歴を保持
4. **アクセス制御**: 報酬関連テーブルは管理者権限のみアクセス可能

## 今後の拡張性

1. **多通貨対応**: `market_price_history`にcurrency_pairカラム追加
2. **複数取引所対応**: price_sourceの拡張
3. **AIによる動的調整**: 機械学習モデルの予測結果格納テーブル追加
4. **クロスチェーン対応**: blockchain_networkカラムの追加
