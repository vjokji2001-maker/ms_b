    -- employment master tables
-- Table: designation
CREATE TABLE designation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    code VARCHAR(10) UNIQUE
);

CREATE TABLE company_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    code VARCHAR(10) UNIQUE,
    reg_name VARCHAR(255) NOT NULL UNIQUE,
    reg_no VARCHAR(50) NOT NULL UNIQUE,
    inc_dt DATE NULL,
    pan_no VARCHAR(50) UNIQUE,
    tan_no VARCHAR(50) UNIQUE,
    cin_no VARCHAR(50) UNIQUE,
    gstin VARCHAR(50) UNIQUE,
    pt_reg_no VARCHAR(50) UNIQUE,
    pf_reg_no VARCHAR(50) UNIQUE,
    esic_reg_no VARCHAR(50) UNIQUE,
    msme_reg_no VARCHAR(255) UNIQUE,
    iec_code VARCHAR(255) UNIQUE,
    pt_zone VARCHAR(255),
    pf_zone VARCHAR(255),
    esic_zone VARCHAR(255),
    industry_type VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    country_id INT,
    state_id INT,
    city_id INT,
    pin_code VARCHAR(30),
    contact_prsn_name VARCHAR(255),
    desig_id BIGINT,
    contact_prsn_email VARCHAR(255),
    country_code VARCHAR(5),
    phone VARCHAR(255),
    email VARCHAR(255),
    website VARCHAR(255),
    twitter VARCHAR(255),
    facebook VARCHAR(255),
    instagram VARCHAR(255),
    image_url VARCHAR(255),
    FOREIGN KEY (country_id) REFERENCES country_master(id),
    FOREIGN KEY (state_id) REFERENCES state_master(id),
    FOREIGN KEY (city_id) REFERENCES city_master(id),
    FOREIGN KEY (desig_id) REFERENCES designation(id)
);


-- Table: branch_details
CREATE TABLE branch_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    code VARCHAR(10) UNIQUE,
    address TEXT,
    country_id INT,
    state_id INT,
    city_id INT,
    FOREIGN KEY (country_id) REFERENCES country_master(id),
    FOREIGN KEY (state_id) REFERENCES state_master(id),
    FOREIGN KEY (city_id) REFERENCES city_master(id),
);

-- Table: department
CREATE TABLE department (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    code VARCHAR(10) UNIQUE
);

-- Table: category
CREATE TABLE category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    code VARCHAR(10) UNIQUE
);

-- Table: wrk_loc
CREATE TABLE wrk_loc (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    code VARCHAR(10)
);

-- Table: sub_wrk_loc
CREATE TABLE sub_wrk_loc (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    code VARCHAR(10) UNIQUE,
    wrk_loc_id BIGINT,
    FOREIGN KEY (wrk_loc_id) REFERENCES wrk_loc(id)
);

CREATE TABLE grades (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    code VARCHAR(10) UNIQUE
);

-- Attendance master tables


-- Table: shift_tmplt
CREATE TABLE shift_tmplt (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(30),
    code VARCHAR(30) UNIQUE,
    night_shift BOOLEAN,
    hour_base BOOLEAN,
    half_day BOOLEAN,
    in_time TIME,
    out_time TIME,
    ttl_wrk_hr TIME,
    fh_out_time TIME,
    sh_in_time TIME,
    ttl_half_day_hr TIME NULL,
    buffer_min INT,
    end_before INT,
   half_rate DOUBLE   DEFAULT 0,
   full_rate DOUBLE   DEFAULT 0,
   cutoff_hr DOUBLE  NOT NULL  DEFAULT 0
);

-- Table: shift_lt_rl
CREATE TABLE shift_lt_rl (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shift_id BIGINT,
    type ENUM('LATE_IN', 'EARLY_OUT', 'BOTH'),               
    from_min INT,
    to_min INT,
    lt_mrk_cnt FLOAT,
    ttl_lt_mrk_cnt FLOAT,
    FOREIGN KEY (shift_id) REFERENCES shift_tmplt(id)
);

-- Table: shift_co_rl
CREATE TABLE shift_co_rl (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shift_id BIGINT,
    co_hd_hr TIME,
    co_fd_hr TIME,
    FOREIGN KEY (shift_id) REFERENCES shift_tmplt(id)
);

-- Table: wk_off_prnt (Work-Off Parent)
CREATE TABLE wk_off_prnt (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(30),
    code VARCHAR(30) UNIQUE
);

-- Table: wk_off_dtl (Work-Off Details)
CREATE TABLE `wk_off_dtl` (
   `id` bigint(20) NOT NULL AUTO_INCREMENT,
   `wk_off_id` bigint(20) DEFAULT NULL,
   `week_no` enum('WEEK_1','WEEK_2','WEEK_3','WEEK_4','WEEK_5','WEEK_6') DEFAULT NULL,
   `day` enum('SUNDAY','MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY') DEFAULT NULL,
   PRIMARY KEY (`id`),
   UNIQUE KEY `wk_off_id` (`wk_off_id`,`day`,`week_no`),
   CONSTRAINT `wk_off_dtl_ibfk_1` FOREIGN KEY (`wk_off_id`) REFERENCES `wk_off_prnt` (`id`)
 ) ENGINE=InnoDB AUTO_INCREMENT=133 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

 CREATE TABLE `holiday_prnt` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `name` varchar(30) DEFAULT NULL,
    `code` varchar(30) DEFAULT NULL,
    `year` year(4) DEFAULT NULL,
    `total_days` bigint(20) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `code` (`code`)
  ) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

  CREATE TABLE `holiday_chld` (
     `id` bigint(20) NOT NULL AUTO_INCREMENT,
     `holiday_id` bigint(20) DEFAULT NULL,
     `name` varchar(30) DEFAULT NULL,
     `date` date DEFAULT NULL,
     `is_checked` tinyint(1) DEFAULT NULL,
     PRIMARY KEY (`id`),
     UNIQUE KEY `name` (`name`),
     KEY `holiday_id` (`holiday_id`),
     CONSTRAINT `holiday_chld_ibfk_1` FOREIGN KEY (`holiday_id`) REFERENCES `holiday_prnt` (`id`)
   ) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci



CREATE TABLE leave_name (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(30),
    code VARCHAR(30) UNIQUE,
    colour_code varchar(45)
);

CREATE TABLE leave_tmplt (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(30),
    code VARCHAR(30) UNIQUE,
    total_leave_days DOUBLE
) ENGINE=InnoDB;

CREATE TABLE leave_tmplt_dtls (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    leave_id BIGINT,
    leave_name_id INT,
    yrly_count DOUBLE,
    qtrly_count DOUBLE,
    mnthly_count DOUBLE,
    pst_days INT,
    future_days INT,
    half_day_allowed BOOLEAN,
    FOREIGN KEY (leave_id) REFERENCES leave_tmplt(id)
) ENGINE=InnoDB;


--Approval group query

CREATE TABLE aprvl_grp (
    id CHAR(36) PRIMARY KEY,  -- UUID stored as a 36-character string
    name VARCHAR(255),
    code VARCHAR(255)
);

CREATE TABLE aprvl_grp_dtls (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    aprvl_grp_id CHAR(36),
    employee_id CHAR(36),
    sequence INT,
    CONSTRAINT fk_aprvl_grp FOREIGN KEY (aprvl_grp_id) REFERENCES aprvl_grp(id),
    CONSTRAINT fk_employee FOREIGN KEY (employee_id) REFERENCES emp_dtls(id)
);

CREATE TABLE country_master(
     id INT AUTO_INCREMENT PRIMARY KEY,
     name varchar(255) NOT NULL
);

CREATE TABLE state_master(
     id INT AUTO_INCREMENT PRIMARY KEY,
     name varchar(255) NOT NULL,
     country_id INT NOT NULL,
     FOREIGN KEY (country_id) REFERENCES country_master(id)
);

CREATE TABLE city_master(
     id INT AUTO_INCREMENT PRIMARY KEY,
     name varchar(255) NOT NULL,
     state_id INT NOT NULL,
     FOREIGN KEY (state_id) REFERENCES state_master(id)
);

-- Table: Year cycle
CREATE TABLE year_cycle(
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    code VARCHAR(10) UNIQUE,
    from_month_year DATE,  -- Store as first day of the month e.g. '2025-01-01'
    to_month_year DATE
);

-- Table: Month cycle
CREATE TABLE month_cycle(
     id INT AUTO_INCREMENT PRIMARY KEY,
     name VARCHAR(255),
     code VARCHAR(10) UNIQUE,
     start_date INT,
     end_date INT,
     grade_id BIGINT,
     FOREIGN KEY (grade_id) REFERENCES grades(id)
);






