-- ショップ機能のテーブル作成
-- 商品テーブル
CREATE TABLE IF NOT EXISTS shop_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL COMMENT '商品名',
    description TEXT COMMENT '商品説明',
    price DECIMAL(19,2) NOT NULL COMMENT '価格（SFR）',
    stock INTEGER DEFAULT 0 COMMENT '在庫数',
    owner_id BIGINT NOT NULL COMMENT '販売者ID',
    category VARCHAR(100) COMMENT 'カテゴリ',
    image_url VARCHAR(500) COMMENT '商品画像URL',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',
    INDEX idx_owner_id (owner_id),
    INDEX idx_category (category),
    INDEX idx_price (price),
    INDEX idx_created_at (created_at)
) COMMENT='商品情報テーブル';

-- 注文テーブル
CREATE TABLE IF NOT EXISTS shop_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL COMMENT '商品ID',
    buyer_id BIGINT NOT NULL COMMENT '購入者ID',
    quantity INTEGER NOT NULL COMMENT '購入数量',
    total_price DECIMAL(19,2) NOT NULL COMMENT '合計金額（SFR）',
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' COMMENT '注文ステータス (PENDING, PAID, SHIPPED, DELIVERED, CANCELLED)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '注文日時',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',
    INDEX idx_item_id (item_id),
    INDEX idx_buyer_id (buyer_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (item_id) REFERENCES shop_items(id) ON DELETE CASCADE
) COMMENT='注文情報テーブル';

-- 配送テーブル
CREATE TABLE IF NOT EXISTS shop_delivery (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT '注文ID',
    carrier VARCHAR(100) NOT NULL COMMENT '配送業者',
    tracking_number VARCHAR(255) COMMENT '追跡番号',
    label_url VARCHAR(500) COMMENT '配送ラベルURL',
    status VARCHAR(50) NOT NULL DEFAULT 'LABEL_CREATED' COMMENT '配送ステータス (LABEL_CREATED, PICKED_UP, IN_TRANSIT, DELIVERED)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',
    UNIQUE KEY uk_order_id (order_id),
    INDEX idx_tracking_number (tracking_number),
    INDEX idx_carrier (carrier),
    INDEX idx_status (status),
    FOREIGN KEY (order_id) REFERENCES shop_orders(id) ON DELETE CASCADE
) COMMENT='配送情報テーブル';

-- 配送トークンテーブル（PoA用）
CREATE TABLE IF NOT EXISTS shop_delivery_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    delivery_id BIGINT NOT NULL COMMENT '配送ID',
    poa_token VARCHAR(255) NOT NULL COMMENT 'PoAトークン',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    INDEX idx_delivery_id (delivery_id),
    INDEX idx_poa_token (poa_token),
    FOREIGN KEY (delivery_id) REFERENCES shop_delivery(id) ON DELETE CASCADE
) COMMENT='配送トークンテーブル（PoA用）';

-- PoA提出ログテーブル
CREATE TABLE IF NOT EXISTS shop_poa_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL COMMENT 'ユーザーID',
    item_id BIGINT NOT NULL COMMENT '商品ID',
    poa_text TEXT COMMENT 'PoAテキスト',
    poa_url VARCHAR(500) COMMENT 'PoA URL',
    ip_address VARCHAR(45) COMMENT 'IPアドレス',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '提出日時',
    INDEX idx_user_id (user_id),
    INDEX idx_item_id (item_id),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (item_id) REFERENCES shop_items(id) ON DELETE CASCADE
) COMMENT='PoA提出ログテーブル';
