-- ===================================
-- フェーズ2: データベーススキーマ更新とデュアル運用開始
-- 
-- 目的: 
-- 1. 新しいspaceテーブルを作成
-- 2. 既存learning_spaceとの同期システム構築
-- 3. ゼロダウンタイムでの並行運用開始
-- 
-- 実行順序:
-- 1. spaceテーブル作成
-- 2. 初期データ移行
-- 3. 同期トリガー設定
-- 4. 新しいAPIエンドポイント有効化
-- ===================================

-- 1. spaceテーブル作成
CREATE TABLE IF NOT EXISTS space (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    mode ENUM('SCHOOL', 'SALON', 'FANCLUB') NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'PENDING') NOT NULL DEFAULT 'ACTIVE',
    is_public BOOLEAN NOT NULL DEFAULT TRUE,
    max_members INT NOT NULL DEFAULT 1000,
    member_count INT NOT NULL DEFAULT 0,
    owner_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- インデックス作成
    INDEX idx_space_mode (mode),
    INDEX idx_space_status (status),
    INDEX idx_space_owner (owner_id),
    INDEX idx_space_public (is_public),
    INDEX idx_space_created (created_at),
    INDEX idx_space_member_count (member_count),
    INDEX idx_space_popularity (member_count, updated_at),
    INDEX idx_space_search (name, description(255))
);

-- 2. 初期データ移行 (learning_space -> space)
INSERT INTO space (
    id, name, description, mode, status, is_public, 
    max_members, member_count, owner_id, created_at, updated_at
)
SELECT 
    ls.id,
    ls.name,
    COALESCE(ls.description, '') as description,
    CASE 
        WHEN ls.mode = 'SCHOOL' THEN 'SCHOOL'
        WHEN ls.mode = 'SALON' THEN 'SALON'
        WHEN ls.mode = 'FANCLUB' THEN 'FANCLUB'
        ELSE 'SCHOOL'
    END as mode,
    CASE 
        WHEN ls.status = 'ACTIVE' THEN 'ACTIVE'
        WHEN ls.status = 'INACTIVE' THEN 'INACTIVE'
        ELSE 'ACTIVE'
    END as status,
    COALESCE(ls.is_public, TRUE) as is_public,
    COALESCE(ls.max_members, 1000) as max_members,
    COALESCE(ls.member_count, 0) as member_count,
    COALESCE(ls.owner_id, '1') as owner_id,
    ls.created_at,
    ls.updated_at
FROM learning_space ls
WHERE NOT EXISTS (SELECT 1 FROM space s WHERE s.id = ls.id);

-- 3. 同期トリガー設定（learning_space -> space）
DELIMITER ;;

-- 新規作成時の同期
CREATE TRIGGER sync_learning_to_space_insert
AFTER INSERT ON learning_space
FOR EACH ROW
BEGIN
    INSERT INTO space (
        id, name, description, mode, status, is_public,
        max_members, member_count, owner_id, created_at, updated_at
    ) VALUES (
        NEW.id,
        NEW.name,
        COALESCE(NEW.description, ''),
        CASE 
            WHEN NEW.mode = 'SCHOOL' THEN 'SCHOOL'
            WHEN NEW.mode = 'SALON' THEN 'SALON'
            WHEN NEW.mode = 'FANCLUB' THEN 'FANCLUB'
            ELSE 'SCHOOL'
        END,
        CASE 
            WHEN NEW.status = 'ACTIVE' THEN 'ACTIVE'
            WHEN NEW.status = 'INACTIVE' THEN 'INACTIVE'
            ELSE 'ACTIVE'
        END,
        COALESCE(NEW.is_public, TRUE),
        COALESCE(NEW.max_members, 1000),
        COALESCE(NEW.member_count, 0),
        COALESCE(NEW.owner_id, '1'),
        NEW.created_at,
        NEW.updated_at
    ) ON DUPLICATE KEY UPDATE
        name = VALUES(name),
        description = VALUES(description),
        mode = VALUES(mode),
        status = VALUES(status),
        is_public = VALUES(is_public),
        max_members = VALUES(max_members),
        member_count = VALUES(member_count),
        owner_id = VALUES(owner_id),
        updated_at = VALUES(updated_at);
END;;

-- 更新時の同期
CREATE TRIGGER sync_learning_to_space_update
AFTER UPDATE ON learning_space
FOR EACH ROW
BEGIN
    UPDATE space SET
        name = NEW.name,
        description = COALESCE(NEW.description, ''),
        mode = CASE 
            WHEN NEW.mode = 'SCHOOL' THEN 'SCHOOL'
            WHEN NEW.mode = 'SALON' THEN 'SALON'
            WHEN NEW.mode = 'FANCLUB' THEN 'FANCLUB'
            ELSE 'SCHOOL'
        END,
        status = CASE 
            WHEN NEW.status = 'ACTIVE' THEN 'ACTIVE'
            WHEN NEW.status = 'INACTIVE' THEN 'INACTIVE'
            ELSE 'ACTIVE'
        END,
        is_public = COALESCE(NEW.is_public, TRUE),
        max_members = COALESCE(NEW.max_members, 1000),
        member_count = COALESCE(NEW.member_count, 0),
        owner_id = COALESCE(NEW.owner_id, '1'),
        updated_at = NEW.updated_at
    WHERE id = NEW.id;
END;;

-- 削除時の同期（論理削除対応）
CREATE TRIGGER sync_learning_to_space_delete
AFTER DELETE ON learning_space
FOR EACH ROW
BEGIN
    UPDATE space SET
        status = 'INACTIVE',
        updated_at = CURRENT_TIMESTAMP
    WHERE id = OLD.id;
END;;

DELIMITER ;

-- 4. 逆方向同期トリガー設定（space -> learning_space）
-- デュアル運用期間中の整合性保証
DELIMITER ;;

CREATE TRIGGER sync_space_to_learning_update
AFTER UPDATE ON space
FOR EACH ROW
BEGIN
    -- learning_spaceが存在する場合のみ更新
    UPDATE learning_space SET
        name = NEW.name,
        description = NEW.description,
        mode = NEW.mode,
        status = NEW.status,
        is_public = NEW.is_public,
        max_members = NEW.max_members,
        member_count = NEW.member_count,
        owner_id = NEW.owner_id,
        updated_at = NEW.updated_at
    WHERE id = NEW.id;
END;;

DELIMITER ;

-- 5. データ整合性チェッククエリ
-- 移行後の検証用
SELECT 
    'Data Count Check' as check_type,
    (SELECT COUNT(*) FROM learning_space) as learning_space_count,
    (SELECT COUNT(*) FROM space) as space_count,
    CASE 
        WHEN (SELECT COUNT(*) FROM learning_space) = (SELECT COUNT(*) FROM space) 
        THEN 'MATCH' 
        ELSE 'MISMATCH' 
    END as status;

-- 6. サンプルデータ検証クエリ
SELECT 
    ls.id,
    ls.name as ls_name,
    s.name as s_name,
    ls.mode as ls_mode,
    s.mode as s_mode,
    ls.member_count as ls_members,
    s.member_count as s_members,
    CASE 
        WHEN ls.name = s.name AND ls.mode = s.mode AND ls.member_count = s.member_count 
        THEN 'SYNCED' 
        ELSE 'OUT_OF_SYNC' 
    END as sync_status
FROM learning_space ls
LEFT JOIN space s ON ls.id = s.id
LIMIT 10;

-- ===================================
-- フェーズ2完了確認事項:
-- 
-- □ spaceテーブルが正常に作成された
-- □ 既存データが正しく移行された  
-- □ 同期トリガーが設定された
-- □ データ整合性が保たれている
-- □ 新しいSpace entityが正常動作する
-- 
-- 次のステップ: フェーズ3（段階的移行）の準備
-- ===================================
