CREATE TABLE IF NOT EXISTS persistence_keys (
  persistence_key BIGINT NOT NULL AUTO_INCREMENT,
  persistence_id VARCHAR(255) NOT NULL,
  PRIMARY KEY (persistence_key),
  UNIQUE (persistence_id)
) ENGINE = MyISAM;

CREATE TABLE IF NOT EXISTS events_journal(
  persistence_id VARCHAR(255) NOT NULL,
  sequence_nr BIGINT NOT NULL,
  content VARBINARY(255) NOT NULL,
  created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  removed BOOLEAN DEFAULT FALSE,
  PRIMARY KEY (persistence_id, sequence_nr)
) ENGINE = MyISAM;

CREATE TABLE IF NOT EXISTS snapshots (
  persistence_id VARCHAR(255) NOT NULL,
  sequence_nr BIGINT NOT NULL,
  created_at BIGINT NOT NULL,
  snapshot BLOB NOT NULL,
  PRIMARY KEY (persistence_id, sequence_nr)
) ENGINE = MyISAM;