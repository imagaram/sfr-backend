-- System parameters store & initial council governance parameters
-- Provides dynamic (DB-backed) configuration for council reward weights, voting thresholds, evaluation windows, and council size.
-- Idempotent inserts allow safe re-execution in non-prod.

CREATE TABLE IF NOT EXISTS system_parameters (
  param_key VARCHAR(120) NOT NULL,
  value_string VARCHAR(500) NULL,
  value_number DECIMAL(20,10) NULL,
  value_json JSON NULL,
  value_type VARCHAR(20) NOT NULL, -- STRING, NUMBER, DECIMAL, JSON, BOOLEAN
  description VARCHAR(300),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (param_key),
  CONSTRAINT chk_system_param_type CHECK (value_type IN ('STRING','NUMBER','DECIMAL','JSON','BOOLEAN'))
);

CREATE TABLE IF NOT EXISTS system_parameter_audit (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  param_key VARCHAR(120) NOT NULL,
  old_value_string VARCHAR(500),
  old_value_number DECIMAL(20,10),
  new_value_string VARCHAR(500),
  new_value_number DECIMAL(20,10),
  changed_by VARCHAR(120), -- user identifier (username / system)
  reason VARCHAR(300),
  changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_param_audit_param FOREIGN KEY (param_key) REFERENCES system_parameters(param_key) ON DELETE CASCADE
);

-- Helper: insert parameter if absent (MySQL compatible)
-- Pattern: INSERT ... SELECT ... WHERE NOT EXISTS subquery

-- Council reward weight: user evaluations (default 0.50)
INSERT INTO system_parameters (param_key, value_number, value_type, description)
SELECT 'council.reward.weight.user', 0.50, 'DECIMAL', 'Weight of user evaluations in council reward weighted score (0-1)'
WHERE NOT EXISTS (SELECT 1 FROM system_parameters WHERE param_key = 'council.reward.weight.user');

-- Council reward weight: peer evaluations (default 0.30)
INSERT INTO system_parameters (param_key, value_number, value_type, description)
SELECT 'council.reward.weight.peer', 0.30, 'DECIMAL', 'Weight of peer evaluations in council reward weighted score (0-1)'
WHERE NOT EXISTS (SELECT 1 FROM system_parameters WHERE param_key = 'council.reward.weight.peer');

-- Council reward weight: admin evaluation (default 0.20)
INSERT INTO system_parameters (param_key, value_number, value_type, description)
SELECT 'council.reward.weight.admin', 0.20, 'DECIMAL', 'Weight of admin evaluation in council reward weighted score (0-1)'
WHERE NOT EXISTS (SELECT 1 FROM system_parameters WHERE param_key = 'council.reward.weight.admin');

-- Minimum SFR balance required to cast council vote (example: 100 SFR)
INSERT INTO system_parameters (param_key, value_number, value_type, description)
SELECT 'council.vote.min.balance', 100, 'NUMBER', 'Minimum SFR balance required for voting eligibility'
WHERE NOT EXISTS (SELECT 1 FROM system_parameters WHERE param_key = 'council.vote.min.balance');

-- Minimum account activity days (recent activity threshold)
INSERT INTO system_parameters (param_key, value_number, value_type, description)
SELECT 'council.vote.min.activity_days', 7, 'NUMBER', 'Minimum recent activity days required for voting eligibility'
WHERE NOT EXISTS (SELECT 1 FROM system_parameters WHERE param_key = 'council.vote.min.activity_days');

-- Evaluation aggregation window (days)
INSERT INTO system_parameters (param_key, value_number, value_type, description)
SELECT 'council.evaluation.window.days', 30, 'NUMBER', 'Rolling window (days) for council member evaluation aggregation'
WHERE NOT EXISTS (SELECT 1 FROM system_parameters WHERE param_key = 'council.evaluation.window.days');

-- Maximum active council seats (upper governance bound)
INSERT INTO system_parameters (param_key, value_number, value_type, description)
SELECT 'council.size.max', 21, 'NUMBER', 'Maximum number of active council members allowed in an election'
WHERE NOT EXISTS (SELECT 1 FROM system_parameters WHERE param_key = 'council.size.max');

-- Guard: ensure reward weights sum (0.50 + 0.30 + 0.20) = 1.00
-- (Enforced in application service layer; optional DB CHECK would require computed expression not portable across MySQL/H2.)

-- Future parameters (documented but not yet seeded):
--  - council.block.finalization.quorum (e.g., 0.667)
--  - council.outlier.iqr.multiplier (e.g., 1.5)
--  - council.signature.algorithm (e.g., 'secp256k1')
--  - council.reward.base.amount (base SFR before weighting)
