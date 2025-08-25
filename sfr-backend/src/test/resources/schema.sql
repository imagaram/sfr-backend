-- H2 Database Test Schema - SFR Cryptocurrency System

-- User Balance table
CREATE TABLE IF NOT EXISTS user_balances (
    user_id VARCHAR(36) NOT NULL,
    current_balance DECIMAL(20,8) NOT NULL DEFAULT 0.0,
    total_earned DECIMAL(20,8) NOT NULL DEFAULT 0.0,
    total_spent DECIMAL(20,8) NOT NULL DEFAULT 0.0,
    frozen_balance DECIMAL(20,8) NOT NULL DEFAULT 0.0,
    available_balance DECIMAL(20,8) NOT NULL DEFAULT 0.0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    space_id BIGINT NOT NULL DEFAULT 1,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, space_id)
);

-- Token Pool table
CREATE TABLE IF NOT EXISTS token_pools (
    id BIGINT PRIMARY KEY IDENTITY,
    space_id BIGINT NOT NULL,
    pool_name VARCHAR(100) NOT NULL,
    total_supply DECIMAL(20,8) NOT NULL DEFAULT 0.0,
    current_balance DECIMAL(20,8) NOT NULL DEFAULT 0.0,
    max_supply DECIMAL(20,8) NOT NULL DEFAULT 21000000.0,
    pool_type VARCHAR(50) NOT NULL DEFAULT 'MAIN',
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

-- Token Transaction table
CREATE TABLE IF NOT EXISTS token_transactions (
    id BIGINT PRIMARY KEY IDENTITY,
    transaction_id VARCHAR(100) UNIQUE NOT NULL,
    from_user_id VARCHAR(36),
    to_user_id VARCHAR(36),
    amount DECIMAL(20,8) NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    description TEXT,
    space_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

-- Balance History table  
CREATE TABLE IF NOT EXISTS balance_histories (
    id BIGINT PRIMARY KEY IDENTITY,
    user_id VARCHAR(36) NOT NULL,
    balance_before DECIMAL(20,8) NOT NULL,
    balance_after DECIMAL(20,8) NOT NULL,
    change_amount DECIMAL(20,8) NOT NULL,
    change_type VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(100),
    space_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

-- Insert test data
INSERT INTO user_balances (user_id, current_balance, total_earned, space_id) VALUES 
('test-user-1', 1000.00000000, 1000.00000000, 1),
('test-user-2', 500.00000000, 500.00000000, 1);

INSERT INTO token_pools (space_id, pool_name, total_supply, current_balance, pool_type) VALUES
(1, 'Main Pool', 1000000.00000000, 998500.00000000, 'MAIN');
