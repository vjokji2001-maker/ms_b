CREATE TABLE hiring_candidate_interview_round (
                                                  id BIGINT NOT NULL AUTO_INCREMENT,

                                                  candidate_application_id BIGINT NOT NULL,

                                                  round_number INT NOT NULL,

                                                  interview_date DATE,
                                                  interview_time TIME,

                                                  interview_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

                                                  result VARCHAR(30),

                                                  feedback TEXT,
                                                  rating INT,
                                                  remarks TEXT,

                                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                                  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                                                      ON UPDATE CURRENT_TIMESTAMP,

                                                  PRIMARY KEY (id),

                                                  UNIQUE KEY uk_candidate_round
                                                      (candidate_application_id, round_number),

                                                  INDEX idx_candidate_application
                                                      (candidate_application_id),

                                                  CONSTRAINT fk_candidate_interview
                                                      FOREIGN KEY (candidate_application_id)
                                                          REFERENCES hiring_candidate_application(id)
                                                          ON DELETE CASCADE
);