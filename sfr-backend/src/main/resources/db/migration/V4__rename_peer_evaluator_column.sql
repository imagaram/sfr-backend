-- 1. Add new nullable column (MySQL doesn't support IF NOT EXISTS for ADD COLUMN, use a conditional approach)
-- Check if column already exists before adding
SET @column_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                      WHERE TABLE_SCHEMA = DATABASE() 
                      AND TABLE_NAME = 'council_peer_evaluations' 
                      AND COLUMN_NAME = 'evaluator_id');

SET @sql = IF(@column_exists = 0, 
              'ALTER TABLE council_peer_evaluations ADD COLUMN evaluator_id BINARY(16)', 
              'SELECT "Column evaluator_id already exists" AS message');
              
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. Copy data
UPDATE council_peer_evaluations SET evaluator_id = evaluator_member_id WHERE evaluator_id IS NULL;

-- 3. Set NOT NULL (use MySQL syntax; H2 also accepts MODIFY for compatibility in recent versions)
ALTER TABLE council_peer_evaluations MODIFY COLUMN evaluator_id BINARY(16) NOT NULL;

-- 4. Add new unique constraint (check if constraint already exists)
SET @constraint_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
                          WHERE TABLE_SCHEMA = DATABASE() 
                          AND TABLE_NAME = 'council_peer_evaluations' 
                          AND CONSTRAINT_NAME = 'uk_peer_eval_target_evaluator');

SET @sql = IF(@constraint_exists = 0, 
              'ALTER TABLE council_peer_evaluations ADD CONSTRAINT uk_peer_eval_target_evaluator UNIQUE (council_member_id, evaluator_id)', 
              'SELECT "Constraint uk_peer_eval_target_evaluator already exists" AS message');
              
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5. Drop old column (MySQL doesn't support IF EXISTS for DROP COLUMN, use conditional approach)
SET @column_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                      WHERE TABLE_SCHEMA = DATABASE() 
                      AND TABLE_NAME = 'council_peer_evaluations' 
                      AND COLUMN_NAME = 'evaluator_member_id');

SET @sql = IF(@column_exists > 0, 
              'ALTER TABLE council_peer_evaluations DROP COLUMN evaluator_member_id', 
              'SELECT "Column evaluator_member_id does not exist" AS message');
              
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- NOTE: This migration is written to be idempotent for repeated executions in non-prod only.