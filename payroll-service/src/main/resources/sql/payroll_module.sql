    CREATE TABLE employee_statutory_info (
    id CHAR(36) PRIMARY KEY,
    emp_id CHAR(36),  -- FK to employee_dtls
    payroll_state VARCHAR(45),

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

    FOREIGN KEY (emp_id) REFERENCES emp_dtls(id)
);

CREATE TABLE compensation_head (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    type VARCHAR(20) NOT NULL,
    CONSTRAINT uk_compensation_head_code UNIQUE (code),
    CONSTRAINT chk_compensation_head_type
        CHECK (type IN ('EARNINGS', 'DEDUCTIONS','STATUTORY_DEDUCTIONS'))
);

INSERT INTO compensation_head (name, code, type)
VALUES
-- Earnings
('Arrears', 'ARREARS', 'EARNINGS'),
('Basic', 'BSC', 'EARNINGS'),
('Dearness Allowance', 'DA', 'EARNINGS'),
('House Rent Allowance', 'HRA', 'EARNINGS'),
('Night Shift Allowance', 'NSA', 'EARNINGS'),

-- Deductions
('Professional Tax', 'PT', 'DEDUCTIONS'),
('Loan Deduction', 'LD', 'DEDUCTIONS'),
('Advance Salary Deduction', 'ASD', 'DEDUCTIONS'),
('Unpaid Leave Deduction', 'ULD', 'DEDUCTIONS'),
('Notice Period Recovery', 'NPR', 'DEDUCTIONS'),
('Penalty Deduction', 'PD', 'DEDUCTIONS'),

-- Statutory Deductions
('Provident Fund', 'PF', 'STATUTORY_DEDUCTIONS'),
('Employee State Insurance', 'ESI', 'STATUTORY_DEDUCTIONS'),
('Tax Deducted at Source', 'TDS', 'STATUTORY_DEDUCTIONS'),
('Labour Welfare Fund', 'LWF', 'STATUTORY_DEDUCTIONS'),
('Professional Tax', 'PTX', 'STATUTORY_DEDUCTIONS');

CREATE TABLE compensation_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    grade_id BIGINT NOT NULL,
    grs_min_amount DOUBLE NOT NULL,
    grs_max_amount DOUBLE NOT NULL,
    component_dtls JSON NOT NULL,
    CONSTRAINT uk_comp_template_code UNIQUE (code),
    CONSTRAINT fk_comp_template_grade
        FOREIGN KEY (grade_id) REFERENCES grades(id)
);

CREATE TABLE `emp_compensation_hdr` (
  `id` char(36) NOT NULL,
  `emp_id` char(36) NOT NULL,
  `comp_tmplt_id` bigint(20) NOT NULL,
  `gross_amt` decimal(12,2) NOT NULL,
  `std_deduction_amt` decimal(12,2) NOT NULL,
  `other_deduction_amt` decimal(12,2) NOT NULL,
  `net_amt` decimal(12,2) NOT NULL,
  `created_date_time` datetime DEFAULT current_timestamp(),
  `modified_date_time` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `created_by` varchar(50) DEFAULT NULL,
  `modified_by` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_emp_comp_hdr_emp` (`emp_id`),
  CONSTRAINT `fk_emp_comp_hdr_emp` FOREIGN KEY (`emp_id`) REFERENCES `emp_dtls` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `emp_compensation_dtls` (
  `id` char(36) NOT NULL,
  `seq` int(11) NOT NULL,
  `emp_cmpnstn_id` char(36) NOT NULL,
  `component_id` bigint(20) NOT NULL,
  `component_type` enum('EARNINGS','DEDUCTIONS','STATUTORY_DEDUCTIONS') DEFAULT NULL,
  `cal_typ` enum('FIXED','FORMULA') NOT NULL,
  `formula_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `amt` decimal(12,2) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `fk_emp_compensation_hdr` (`emp_cmpnstn_id`),
  CONSTRAINT `fk_emp_compensation_hdr` FOREIGN KEY (`emp_cmpnstn_id`) REFERENCES `emp_compensation_hdr` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


-- ===============================
-- Monthly Salary Summary
-- ===============================
CREATE TABLE emp_monthly_payroll_summary (
    id CHAR(36) NOT NULL,
    attendance_salary_link_id CHAR(36) NOT NULL,
    payable_days DOUBLE,
    lop_days DOUBLE,
    ttl_earnings DECIMAL(18,2),
    ttl_std_deductions DECIMAL(18,2),
    ttl_othr_deductions DECIMAL(18,2),
    ttl_payable_earnings DECIMAL(18,2),
    ttl_payable_std_deductions DECIMAL(18,2),
    ttl_payable_othr_deductions DECIMAL(18,2),
    net_payable DECIMAL(18,2),
    grs_salary_dtls JSON,
    crnt_salary_dtls JSON,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_payroll_att_salary_link
        FOREIGN KEY (attendance_salary_link_id)
        REFERENCES attendance_salary_link(id),
    UNIQUE KEY uk_payroll_att_salary_link (attendance_salary_link_id)
);

ALTER TABLE emp_monthly_payroll_summary ADD COLUMN emp_compensation JSON AFTER ttl_othr_deductions;
ALTER TABLE emp_monthly_payroll_summary ADD COLUMN salary_dtls longtext AFTER net_payable;
ALTER TABLE `attendance_salary_link`
CHANGE COLUMN `p_status` `p_status` ENUM('PENDING', 'DRAFT', 'LOCKED', 'PUBLISHED') NOT NULL DEFAULT 'PENDING' ;
 ALTER TABLE employee_statutory_info ADD COLUMN pt_applicable BOOLEAN DEFAULT FALSE;

 -- =========================
 -- 1. BANK TABLE
 -- =========================
 CREATE TABLE bank (
     id BIGINT AUTO_INCREMENT PRIMARY KEY,

     name VARCHAR(150) NOT NULL,
     short_name VARCHAR(50),
     code VARCHAR(50),

     is_active BOOLEAN DEFAULT TRUE,

     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
 );


 -- =========================
 -- 2. BANK BRANCH TABLE
 -- =========================
 CREATE TABLE bank_branch (
     id BIGINT AUTO_INCREMENT PRIMARY KEY,

     bank_id BIGINT NOT NULL,

     -- Core fields
     name VARCHAR(150) NOT NULL,
     ifsc_code VARCHAR(20) NOT NULL UNIQUE,

     -- Searchable fields
     city VARCHAR(100),
     state VARCHAR(100),

     -- Flexible data
     details JSON,

     is_active BOOLEAN DEFAULT TRUE,

     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

     CONSTRAINT fk_bank_branch_bank
         FOREIGN KEY (bank_id) REFERENCES bank(id)
 );


 -- =========================
 -- 3. EMPLOYEE BANK ACCOUNT
 -- =========================
 CREATE TABLE employee_bank_account (
     id CHAR(36) PRIMARY KEY,   -- keep UUID here

     emp_id CHAR(36) NOT NULL,

     bank_id BIGINT NOT NULL,
     branch_id BIGINT NOT NULL,

     account_number VARCHAR(50) NOT NULL,
     account_holder_name VARCHAR(150),

     account_type VARCHAR(20), -- SAVINGS, CURRENT, SALARY

     is_primary BOOLEAN DEFAULT TRUE,
     is_active BOOLEAN DEFAULT TRUE,

     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

     CONSTRAINT fk_emp_bank_account_emp
         FOREIGN KEY (emp_id) REFERENCES emp_dtls(id),

     CONSTRAINT fk_emp_bank_account_bank
         FOREIGN KEY (bank_id) REFERENCES bank(id),

     CONSTRAINT fk_emp_bank_account_branch
         FOREIGN KEY (branch_id) REFERENCES bank_branch(id)
 );


 -- =========================
 -- INDEXES
 -- =========================
 CREATE INDEX idx_bank_branch_ifsc ON bank_branch(ifsc_code);
 CREATE INDEX idx_employee_bank_emp_id ON employee_bank_account(emp_id);
 ALTER TABLE employee_statutory_info
 ADD CONSTRAINT uk_employee_statutory_emp_id UNIQUE (emp_id);