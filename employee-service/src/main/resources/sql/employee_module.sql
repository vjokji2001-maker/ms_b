/*emp module related queries*/
CREATE TABLE emp_dtls (
    id CHAR(36) PRIMARY KEY,

    -- Basic Personal Details
    salutation ENUM('MR', 'MS', 'MRS', 'DR', 'PROF') DEFAULT 'MR',
    first_name VARCHAR(50),
    middle_name VARCHAR(50),
    last_name VARCHAR(50),
    formal_name VARCHAR(100),
    gender ENUM('MALE', 'FEMALE', 'OTHER'),
    dob DATE,
    marital_status ENUM('SINGLE', 'MARRIED', 'DIVORCED', 'WIDOWED', 'SEPARATED'),
    married_since DATE,

    -- Contact Details
    contact_country_code VARCHAR(5),
    contact_number VARCHAR(15),
    alternate_country_code VARCHAR(5),
    alternate_number VARCHAR(15),
    formal_email VARCHAR(100),
    personal_email VARCHAR(100),

    -- Identity Details
    aadhaar_number VARCHAR(20),          -- Optional & may contain duplicates (no UNIQUE here)
    aadhaar_file_name VARCHAR(255),
    document_country VARCHAR(50),
    pan_number VARCHAR(20),             -- Optional & may contain duplicates (no UNIQUE here)
    pan_file_name VARCHAR(255),

    -- Location / Nationality Details (FK-based)
    nationality VARCHAR(50),
    birth_country_id INT,
    state_id INT,
    city_id INT,
    blood_group ENUM('A_POSITIVE', 'A_NEGATIVE', 'B_POSITIVE', 'B_NEGATIVE', 'AB_POSITIVE', 'AB_NEGATIVE', 'O_POSITIVE', 'O_NEGATIVE'),

    -- Employment Info (FK-based)
    company_id BIGINT,
    branch_id BIGINT,
    designation_id BIGINT,
    department_id BIGINT,
    work_location_id BIGINT,
    sub_work_location_id BIGINT,
    category_id BIGINT,
    grade_id BIGINT,
    status ENUM('ACTIVE', 'INACTIVE'),
    emp_code VARCHAR(50),
    biometric_code VARCHAR(50),

    -- Pre-Joining Dates
    offer_date DATE,
    offer_acceptance_date DATE,

    -- Onboarding Dates
    joining_date DATE,
    contract_start_date DATE,

    -- Probation & Confirmation
    probation_start_date DATE,
    probation_end_date DATE,
    confirmation_date DATE,

    -- Exit Process
    resignation_date DATE,
    notice_period_start_date DATE,
    notice_period_end_date DATE,
    termination_date DATE,
    last_working_date DATE,
    smartcare_user_created BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (birth_country_id) REFERENCES country_master(id),
    FOREIGN KEY (state_id) REFERENCES state_master(id),
    FOREIGN KEY (city_id) REFERENCES city_master(id),,
    FOREIGN KEY (company_id) REFERENCES company_details(id),
    FOREIGN KEY (branch_id) REFERENCES branch_details(id),
    FOREIGN KEY (designation_id) REFERENCES designation(id),
    FOREIGN KEY (department_id) REFERENCES department(id),
    FOREIGN KEY (work_location_id) REFERENCES wrk_loc(id),
    FOREIGN KEY (sub_work_location_id) REFERENCES sub_wrk_loc(id),
    FOREIGN KEY (category_id) REFERENCES category(id),
    FOREIGN KEY (grade_id) REFERENCES grades(id)
);

CREATE TABLE emp_address (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,  -- Auto-incremented numeric ID
    emp_id CHAR(36),                       -- FK to emp_dtls (UUID)

    country_id INT,
    state_id INT,
    city_id INT,

    address TEXT,
    pincode VARCHAR(10),
    from_date DATE,
    to_date DATE,
    address_type ENUM('CURRENT', 'PAST'),

    -- Foreign Key Constraints
    FOREIGN KEY (emp_id) REFERENCES emp_dtls(id),

    FOREIGN KEY (country_id) REFERENCES country_master(id),
    FOREIGN KEY (state_id) REFERENCES state_master(id),
    FOREIGN KEY (city_id) REFERENCES city_master(id)
);


CREATE TABLE emp_family_dtls (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    emp_id CHAR(36),                   -- FK to emp_dtls (UUID)
    name VARCHAR(100) DEFAULT NULL,
    relation VARCHAR(50) DEFAULT NULL,
    contact_number VARCHAR(15),
    dob DATE,
    FOREIGN KEY (emp_id) REFERENCES emp_dtls(id)
);

CREATE TABLE emp_experience (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    emp_id CHAR(36),

    experience_type ENUM('FULL_TIME', 'PART_TIME', 'INTERNSHIP', 'CONTRACT', 'FREELANCE', 'TEMPORARY'),
    company_name VARCHAR(255),
    address TEXT,
    designation VARCHAR(100),
    from_date DATE,
    to_date DATE,
    total_span INT,

    reference_name VARCHAR(100),
    reference_contact VARCHAR(15),
    reference_designation VARCHAR(100),

    from_month_year DATE,
    to_month_year DATE,

    salary_slips_name VARCHAR(255),            -- renamed here
    experience_certificate_name VARCHAR(255),
    relieving_certificate_name VARCHAR(255),

    FOREIGN KEY (emp_id) REFERENCES emp_dtls(id)
);

CREATE TABLE emp_education (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    emp_id CHAR(36),  -- FK to emp_dtls (UUID)

    education_type ENUM('TENTH', 'TWELFTH', 'GRADUATION', 'POST_GRADUATION', 'PHD', 'DIPLOMA', 'OTHERS'),
    institute_university_name VARCHAR(255),
    school_college_name VARCHAR(255),

    from_month_year DATE,  -- Store as first day of the month e.g. '2025-01-01'
    to_month_year DATE,

    document_name VARCHAR(255),

    FOREIGN KEY (emp_id) REFERENCES emp_dtls(id)
);

CREATE TABLE emp_skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    emp_id CHAR(36),  -- FK to emp_dtls (UUID)

    skill VARCHAR(255),
    institute_university_name VARCHAR(255),
    rating VARCHAR(50),

    from_month_year DATE,
    to_month_year DATE,

    document_name VARCHAR(255),

    FOREIGN KEY (emp_id) REFERENCES emp_dtls(id)
);

CREATE TABLE employee_statutory_info (
    id CHAR(36) PRIMARY KEY,
    emp_id CHAR(36),  -- FK to employee_dtls

    -- Provident Fund (PF) Details
    pf_applicable BOOLEAN DEFAULT FALSE,
    uan VARCHAR(50),
    pf_number VARCHAR(50),
    pf_joining_date DATE,
    pf_zone VARCHAR(100),
    pf_verified BOOLEAN DEFAULT FALSE,

    -- ESIC Details
    esic_applicable BOOLEAN DEFAULT FALSE,
    esic_number VARCHAR(50),
    esic_joining_date DATE,
    esic_zone VARCHAR(100),
    esic_verified BOOLEAN DEFAULT FALSE,

    -- NPS Details
    nps_applicable BOOLEAN DEFAULT FALSE,
    nps_join_date DATE,
    nps_scheme_type VARCHAR(100),
    nps_verified BOOLEAN DEFAULT FALSE,

    -- Payment Details
    payment_method ENUM('CASH', 'CARD', 'UPI', 'BANK_TRANSFER') DEFAULT NULL,
    bank_name VARCHAR(100),
    account_number VARCHAR(50),
    ifsc_code VARCHAR(20),
    payroll_state VARCHAR(45),
    FOREIGN KEY (emp_id) REFERENCES emp_dtls(id)
);

/* Week Off config related queries*/

CREATE TABLE emp_config (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  emp_id CHAR(36) NOT NULL,
  week_off_id BIGINT(20) DEFAULT NULL,
  aprvl_grp_id CHAR(36) DEFAULT NULL,
  holiday_clndr_id BIGINT(20) DEFAULT NULL,
  leave_tmplt_id BIGINT(20) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_emp_id (emp_id),
  CONSTRAINT fk_emp_config_emp_dtls FOREIGN KEY (emp_id) REFERENCES emp_dtls(id),
  CONSTRAINT fk_emp_config_wk_off_prnt FOREIGN KEY (week_off_id) REFERENCES wk_off_prnt(id),
  CONSTRAINT fk_emp_config_aprvl_grp FOREIGN KEY (aprvl_grp_id) REFERENCES aprvl_grp(id),
  CONSTRAINT fk_emp_config_holiday_prnt FOREIGN KEY (holiday_clndr_id) REFERENCES holiday_prnt(id)
) ENGINE=InnoDB;

CREATE TABLE emp_leave_prnt(
   id BIGINT PRIMARY KEY AUTO_INCREMENT,
   emp_id CHAR(36),
   year_id INT,
   leave_tmplt_id BIGINT,
   balance VARCHAR(50),
   FOREIGN KEY (emp_id) REFERENCES emp_dtls(id),
   FOREIGN KEY (year_id) REFERENCES year_cycle(id),
   FOREIGN KEY (leave_tmplt_id) REFERENCES leave_tmplt(id)
) ENGINE=InnoDB;


 CREATE TABLE `emp_leave_dtls` (
   `id` bigint(20) NOT NULL AUTO_INCREMENT,
   `enp_leave_id` bigint(20) DEFAULT NULL,
   `leave_name_id` bigint(20) DEFAULT NULL,
   `yrly_count` double DEFAULT NULL,
   `qtrly_count` double DEFAULT NULL,
   `mnthly_count` double DEFAULT NULL,
   `pst_days` int(11) DEFAULT NULL,
   `future_days` int(11) DEFAULT NULL,
   `half_day_allowed` tinyint(1) DEFAULT NULL,
   PRIMARY KEY (`id`),
   KEY `enp_leave_id` (`enp_leave_id`),
   KEY `fk_emp_leave_dtls_leave_name_id` (`leave_name_id`),
   CONSTRAINT `emp_leave_dtls_ibfk_1` FOREIGN KEY (`enp_leave_id`) REFERENCES `emp_leave_prnt` (`id`),
   CONSTRAINT `fk_emp_leave_dtls_leave_name_id` FOREIGN KEY (`leave_name_id`) REFERENCES `leave_name` (`id`)
 ) ENGINE=InnoDB AUTO_INCREMENT=92 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci

ALTER TABLE emp_leave_dtls MODIFY COLUMN pst_days int(11) default 0;
ALTER TABLE emp_leave_dtls MODIFY COLUMN future_days int(11) default 0;

