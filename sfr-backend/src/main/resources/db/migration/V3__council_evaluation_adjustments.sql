-- Council evaluation adjustments: add aggregated user evaluations table & enforce admin uniqueness

CREATE TABLE IF NOT EXISTS council_user_evaluations (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  council_member_id BINARY(16) NOT NULL,
  user_id BINARY(16) NOT NULL,
  score INT NOT NULL,
  comment VARCHAR(300),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (council_member_id, user_id)
);

-- CREATE INDEX idx_user_eval_member ON council_user_evaluations(council_member_id);

-- Add unique constraint for single admin evaluation per member (matches entity unique constraint)
ALTER TABLE council_admin_evaluations
  ADD CONSTRAINT uk_admin_eval_member UNIQUE (council_member_id);

-- NOTE: Existing peer table column name is evaluator_member_id; entity mapping updated accordingly.