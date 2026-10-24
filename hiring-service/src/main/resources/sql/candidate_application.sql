CREATE TABLE hiring_candidate_application (
                                              id BINARY(16) NOT NULL,

                                              candidate_id VARCHAR(50) NOT NULL,

                                              candidate_name VARCHAR(150) NOT NULL,
                                              mobile_number VARCHAR(20) NOT NULL,
                                              email VARCHAR(150) NOT NULL,

                                              gender VARCHAR(20),
                                              date_of_birth DATE,

                                              experience VARCHAR(50),
                                              current_company VARCHAR(150),

                                              current_ctc VARCHAR(50),
                                              expected_ctc VARCHAR(50),

                                              notice_period VARCHAR(50),

                                              applied_position VARCHAR(150) NOT NULL,

                                              hiring_request_id BINARY(16),

                                              resume_file_name VARCHAR(255),
                                              resume_file_path VARCHAR(500),

                                              address VARCHAR(500),
                                              remarks VARCHAR(1000),

                                              status VARCHAR(30) NOT NULL DEFAULT 'APPLIED',

                                              applied_date DATE,

                                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                                                  ON UPDATE CURRENT_TIMESTAMP,

                                              PRIMARY KEY (id),

                                              UNIQUE KEY uk_candidate_id (candidate_id),

                                              INDEX idx_email (email),
                                              INDEX idx_mobile_number (mobile_number),
                                              INDEX idx_hiring_request_id (hiring_request_id),
                                              INDEX idx_status (status)
);