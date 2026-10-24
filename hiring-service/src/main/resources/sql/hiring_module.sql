USE hr_staging;

DROP TABLE IF EXISTS hiring_request;

CREATE TABLE hiring_request (
                                id BIGINT NOT NULL AUTO_INCREMENT,
                                request_id VARCHAR(50) UNIQUE,

department_id BIGINT NOT NULL,
designation_id BIGINT NOT NULL,

country_id BIGINT NOT NULL,
state_id BIGINT NOT NULL,
city_id BIGINT NOT NULL,

vacancies INT NOT NULL,
employment_type VARCHAR(50) NOT NULL,
experience VARCHAR(100) NOT NULL,
joining_date DATE NOT NULL,
priority VARCHAR(50) NOT NULL,

reporting_manager VARCHAR(255) NOT NULL,
reason TEXT NOT NULL,
remarks TEXT NULL,

status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
hired_count INT NOT NULL DEFAULT 0,

created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
created_by VARCHAR(255),

PRIMARY KEY (id)
);

CREATE INDEX idx_hiring_request_status
    ON hiring_request(status);

CREATE INDEX idx_hiring_request_priority
    ON hiring_request(priority);

CREATE INDEX idx_hiring_request_department
    ON hiring_request(department_id);

CREATE INDEX idx_hiring_request_designation
    ON hiring_request(designation_id);

CREATE INDEX idx_hiring_request_country
    ON hiring_request(country_id);

CREATE INDEX idx_hiring_request_state
    ON hiring_request(state_id);

CREATE INDEX idx_hiring_request_city
    ON hiring_request(city_id);