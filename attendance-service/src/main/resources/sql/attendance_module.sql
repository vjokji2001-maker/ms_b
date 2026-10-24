--(Employee Shift)
CREATE TABLE `emp_shift` (
   `id` bigint(20) NOT NULL AUTO_INCREMENT,
   `emp_id` char(36) DEFAULT NULL,
   `shift_id` bigint(20) DEFAULT NULL,
   `date` date DEFAULT NULL,
   `is_default` tinyint(1) DEFAULT 0,
   `shift_strt_time` datetime NOT NULL,
   `emp_in_time` datetime NOT NULL,
   `emp_fh_out_time` datetime DEFAULT NULL,
   `emp_sh_in_time` datetime DEFAULT NULL,
   `emp_out_time` datetime NOT NULL,
   `shift_end_time` datetime NOT NULL,
   `cutoff_hr` decimal(5,2) DEFAULT NULL,
   `grace_time_mins` int(11) NOT NULL,
   `auto_adjust_time` time DEFAULT NULL,
   PRIMARY KEY (`id`),
   KEY `emp_id` (`emp_id`),
   KEY `shift_id` (`shift_id`),
   CONSTRAINT `emp_shift_ibfk_1` FOREIGN KEY (`emp_id`) REFERENCES `emp_dtls` (`id`),
   CONSTRAINT `emp_shift_ibfk_2` FOREIGN KEY (`shift_id`) REFERENCES `shift_tmplt` (`id`)
 ) ENGINE=InnoDB AUTO_INCREMENT=12101 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

--(Employee Swipe Request)
CREATE TABLE emp_swp_rqst (
    id CHAR(36) PRIMARY KEY,
    emp_id CHAR(36) NOT NULL,
    swp_date DATE NOT NULL,
    day_type ENUM('FULL_DAY', 'FIRST_HALF', 'SECOND_HALF') NOT NULL,
    direction ENUM('IN', 'OUT', 'BOTH') NOT NULL,
    emp_shift_id BIGINT,
    req_in_time TIME,
    req_out_time TIME,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'LAPSED', 'IN_PROGRESS') DEFAULT 'PENDING',
    remark VARCHAR(50),
    created_date_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_date_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    modified_by VARCHAR(50),

    FOREIGN KEY (emp_id) REFERENCES emp_dtls(id),
    FOREIGN KEY (emp_shift_id) REFERENCES emp_shift(id)
);

-- (Swipe Request Approver Details)
CREATE TABLE swp_rqst_aprvr_dtl (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    swp_rqst_id CHAR(36) NOT NULL,
    sequence INT NOT NULL,
    approver_id CHAR(36) NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    remark VARCHAR(50),
    action_date_time DATETIME NULL,

    FOREIGN KEY (swp_rqst_id) REFERENCES emp_swp_rqst(id),
    FOREIGN KEY (approver_id) REFERENCES emp_dtls(id)
);

-- (Employee Daily Punch Log)
CREATE TABLE emp_daily_punch_log(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    emp_code VARCHAR(36) NOT NULL,
    punch_date DATE NOT NULL,
    punch_date_time DATETIME NOT NULL,
    punch_type ENUM('MACHINE', 'MANUAL') NOT NULL,
    direction ENUM('IN', 'OUT') NOT NULL
);

-- emp attendance table
CREATE TABLE emp_atndnce (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
     employee_id CHAR(36) NOT NULL,
    emp_shift_id BIGINT NOT NULL,
    atndnce_date DATE NOT NULL,
    in_time TIME,
    out_time TIME,
    day_type VARCHAR(10),
    day_type1 VARCHAR(10),
    `portion` DOUBLE(2,1),
    late_in_mark DOUBLE(3,1),
    early_out_mark DOUBLE(3,1),
    nsa_amt double DEFAULT 0,
    nsa_days double DEFAULT 0,
    status ENUM('PENDING', 'INPROCESS', 'PROCESED', 'LOCKED', 'FREEZED') DEFAULT 'PENDING',
    leave_rqst_id char(36),
    FOREIGN KEY (emp_shift_id) REFERENCES emp_shift(id),
    FOREIGN KEY (employee_id) REFERENCES emp_dtls(id),
     UNIQUE KEY uq_emp_atndnce_date (employee_id, atndnce_date)
);

CREATE TABLE `lv_od_rqst` (
   `id` CHAR(36) NOT NULL,
   `emp_id` CHAR(36) DEFAULT NULL,
   `app_type` ENUM('LEAVE_RQST','OD_RQST','LEAVE_CANCEL','OD_CANCEL'),
   `app_dt` DATE DEFAULT NULL,
   `lv_nm_id` BIGINT(20) DEFAULT NULL,
   `yr_id` INT(11) DEFAULT NULL,
   `frm_dt` DATE DEFAULT NULL,
   `frm_dt_typ` ENUM('FIRST_HALF','SECOND_HALF','FULL_DAY') DEFAULT NULL,
   `to_dt` DATE DEFAULT NULL,
   `to_dt_typ` ENUM('FIRST_HALF','SECOND_HALF','FULL_DAY') DEFAULT NULL,
   `status` ENUM('PENDING','IN_PROGRESS','APPROVED','REJECTED','CANCELED') DEFAULT 'PENDING',
   `reason` TEXT DEFAULT NULL,
   `ttl_cnt` DOUBLE DEFAULT NULL,
   `apprvr_rmrk` TEXT DEFAULT NULL,
   `created_by` CHAR(36) DEFAULT NULL,
   `created_datetime` DATETIME DEFAULT CURRENT_TIMESTAMP,
   `modified_by` CHAR(36) DEFAULT NULL,
   `modified_datetime` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    leave_rqst_id char(36) DEFAULT NULL ,
    app_no bigint DEFAULT NULL,
   PRIMARY KEY (`id`),
   KEY `idx_lv_od_emp_id` (`emp_id`),
   KEY `idx_lv_od_yr_id` (`yr_id`),
   KEY `idx_lv_od_lv_nm_id` (`lv_nm_id`),
   CONSTRAINT `fk_lv_od_emp_id`
   FOREIGN KEY (`emp_id`) REFERENCES `emp_dtls` (`id`),
   CONSTRAINT `fk_lv_od_yr_id`
   FOREIGN KEY (`yr_id`) REFERENCES `year_cycle` (`id`),
   CONSTRAINT `fk_lv_od_lv_nm_id`
   FOREIGN KEY (`lv_nm_id`) REFERENCES `leave_name` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `lv_od_rqst_dtls` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `lv_rqst_id` CHAR(36) DEFAULT NULL,
  `sequence` INT(11) DEFAULT NULL,
  `aprvr_id` CHAR(36) DEFAULT NULL,
  `status` ENUM('PENDING','APPROVED','REJECTED') DEFAULT NULL,
  `rmrk` TEXT DEFAULT NULL,
  `action_date_time` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_lv_od_rqst_dtls_lv_rqst_id` (`lv_rqst_id`),
  KEY `idx_lv_od_rqst_dtls_aprvr_id` (`aprvr_id`),
  CONSTRAINT `fk_lv_od_rqst_dtls_lv_rqst_id`
  FOREIGN KEY (`lv_rqst_id`) REFERENCES `lv_od_rqst` (`id`),
  CONSTRAINT `fk_lv_od_rqst_dtls_aprvr_id`
   FOREIGN KEY (`aprvr_id`) REFERENCES `emp_dtls` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


-- ===============================
-- Attendance–Salary Linking Table
-- ===============================
CREATE TABLE attendance_salary_link (
    id CHAR(36) PRIMARY KEY,
    employee_id CHAR(36) NOT NULL,
    month_year VARCHAR(15) NOT NULL, -- YYYY-MM(2026-September)
    night_shift_exist TINYINT NOT NULL DEFAULT 0,
    nsa_amt double  DEFAULT 0,
   nsa_days double DEFAULT 0,
    a_status ENUM(
        'PENDING',
        'IN_PROCESS',
        'PROCESSED',
        'LOCKED',
        'FROZEN',
        "RECALCULATE"
    ) NOT NULL DEFAULT 'PENDING',
    p_status ENUM(
        'PENDING',   -- attendance not locked yet
        'IN_PROCESS',           -- system calculated salary
        'PROCESSED',            -- user reviewed & saved
        'LOCKED',               -- payroll locked
        'FROZEN'                -- financial freeze
    ) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_emp_month (employee_id, month_year),
	FOREIGN KEY (employee_id) REFERENCES emp_dtls (id)
);
-- ===============================
-- Employee Monthly Attendance Summary
-- ===============================
CREATE TABLE emp_attendance_summary (
    id CHAR(36) PRIMARY KEY,
    attendance_salary_link_id CHAR(36) NOT NULL,
    ttl_mnth_days      DECIMAL(5,2) NOT NULL,
    ttl_present_days   DECIMAL(5,2) NOT NULL,
    ttl_night_shifts   DECIMAL(5,2) NOT NULL,
	ttl_week_off_days  DECIMAL(5,2) NOT NULL,
    ttl_paid_lvs       DECIMAL(5,2) NOT NULL,
    ttl_holidays       DECIMAL(5,2) NOT NULL,
    ttl_late_mark      DECIMAL(5,2) NOT NULL,
    ttl_late_abs       DECIMAL(5,2) NOT NULL,
    ttl_late_lvs       DECIMAL(5,2) NOT NULL,
    ttl_abs            DECIMAL(5,2) NOT NULL,
    ttl_unpaid_lvs     DECIMAL(5,2) NOT NULL,
    CONSTRAINT fk_att_link_att
        FOREIGN KEY (attendance_salary_link_id)
        REFERENCES attendance_salary_link(id)
);
ALTER TABLE attendance_salary_link ADD COLUMN nsa_amt double DEFAULT 0 AFTER night_shift_exist ;
ALTER TABLE emp_atndnce ADD COLUMN nsa_amt double DEFAULT 0 AFTER early_out_mark ;
ALTER TABLE emp_atndnce ADD COLUMN nsa_days double default 0;
ALTER TABLE attendance_salary_link ADD COLUMN nsa_days double default 0;