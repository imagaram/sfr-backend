-- Council governance module schema

CREATE TABLE council_elections (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  phase VARCHAR(20) NOT NULL, -- PRE_ELECTION, VOTING, COUNTING, POST_ELECTION
  start_at TIMESTAMP NOT NULL,
  end_at TIMESTAMP NOT NULL,
  seats INT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT chk_council_election_phase CHECK (phase IN ('PRE_ELECTION','VOTING','COUNTING','POST_ELECTION')),
  CONSTRAINT chk_council_election_time CHECK (end_at > start_at)
);

CREATE TABLE council_candidates (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  election_id BIGINT NOT NULL,
  user_id BINARY(16) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, WITHDRAWN, DISQUALIFIED
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_candidate_election FOREIGN KEY (election_id) REFERENCES council_elections(id) ON DELETE CASCADE,
  CONSTRAINT chk_candidate_status CHECK (status IN ('ACTIVE','WITHDRAWN','DISQUALIFIED')),
  UNIQUE (election_id, user_id)
);

CREATE TABLE council_candidate_manifestos (
  candidate_id BIGINT PRIMARY KEY,
  title VARCHAR(200) NOT NULL,
  summary VARCHAR(500),
  details JSON,
  endorsements JSON,
  qa JSON,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_manifesto_candidate FOREIGN KEY (candidate_id) REFERENCES council_candidates(id) ON DELETE CASCADE
);

CREATE TABLE council_votes (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  election_id BIGINT NOT NULL,
  candidate_id BIGINT NOT NULL,
  user_id BINARY(16) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_vote_election FOREIGN KEY (election_id) REFERENCES council_elections(id) ON DELETE CASCADE,
  CONSTRAINT fk_vote_candidate FOREIGN KEY (candidate_id) REFERENCES council_candidates(id) ON DELETE CASCADE,
  UNIQUE (election_id, user_id)
);

CREATE TABLE council_evaluation_items (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  council_member_id BINARY(16) NOT NULL,
  label VARCHAR(120) NOT NULL,
  description VARCHAR(500),
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE council_user_evaluation_scores (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  council_member_id BINARY(16) NOT NULL,
  user_id BINARY(16) NOT NULL,
  item_id BIGINT NOT NULL,
  score INT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (council_member_id, user_id, item_id)
);

CREATE TABLE council_peer_evaluations (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  council_member_id BINARY(16) NOT NULL,
  evaluator_member_id BINARY(16) NOT NULL,
  score INT NOT NULL,
  comment TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (council_member_id, evaluator_member_id)
);

CREATE TABLE council_admin_evaluations (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  council_member_id BINARY(16) NOT NULL,
  score INT NOT NULL,
  comment TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE council_reward_records (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  council_member_id BINARY(16) NOT NULL,
  base_reward_sfr DECIMAL(20,10) NOT NULL,
  final_reward_sfr DECIMAL(20,10) NOT NULL,
  user_score_avg DECIMAL(5,2),
  peer_score_avg DECIMAL(5,2),
  admin_score DECIMAL(5,2),
  weighted_score DECIMAL(5,2),
  comment_hash VARCHAR(128),
  finalized BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (council_member_id)
);

CREATE TABLE council_blocks (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  block_index BIGINT NOT NULL UNIQUE,
  timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  previous_hash VARCHAR(128),
  hash VARCHAR(128) NOT NULL,
  validator_id VARCHAR(64),
  comment_merkle_root VARCHAR(128),
  reward_record_ids JSON,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE council_block_signatures (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  block_id BIGINT NOT NULL,
  council_member_id BINARY(16) NOT NULL,
  signature VARCHAR(512) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_block_signature_block FOREIGN KEY (block_id) REFERENCES council_blocks(id) ON DELETE CASCADE,
  UNIQUE (block_id, council_member_id)
);

-- Indexes
CREATE INDEX idx_council_candidates_election ON council_candidates(election_id);
CREATE INDEX idx_council_votes_election ON council_votes(election_id);
CREATE INDEX idx_council_votes_candidate ON council_votes(candidate_id);
CREATE INDEX idx_eval_items_member ON council_evaluation_items(council_member_id);
CREATE INDEX idx_user_scores_member ON council_user_evaluation_scores(council_member_id);
CREATE INDEX idx_peer_eval_member ON council_peer_evaluations(council_member_id);
CREATE INDEX idx_admin_eval_member ON council_admin_evaluations(council_member_id);
CREATE INDEX idx_reward_record_member ON council_reward_records(council_member_id);
CREATE INDEX idx_council_blocks_index ON council_blocks(block_index);
