DROP VIEW IF EXISTS vw_employee_attendance;

CREATE VIEW vw_employee_attendance AS
SELECT
    ed.id AS employee_id,
    ed.formal_name AS employee_code,
    CONCAT(ed.formal_name) AS employee_name,

    ed.company_id,
    ed.branch_id,
    ed.department_id,
    ed.designation_id,
    ed.work_location_id,
    ed.category_id,
    ed.grade_id,

    c.name AS company,
    b.name AS branch,
    dp.name AS department,
    dg.name AS designation,
    ct.name AS category,
    loc.name AS location,
    g.name AS grade,

    ea.atndnce_date AS attendance_date,
    DATE_FORMAT(ea.atndnce_date,'%d/%m/%Y') AS formatted_date,
    DATE_FORMAT(ea.atndnce_date,'%a') AS day,

    st.code AS shift_code,
    st.in_time AS shift_in_time,
    st.out_time AS shift_out_time,

    ea.in_time AS emp_in_time,
    ea.out_time AS emp_out_time,

    IFNULL(ea.late_in_mark,0) AS late_in_mark,
    IFNULL(ea.early_out_mark,0) AS early_out_mark,
    (IFNULL(ea.late_in_mark,0) + IFNULL(ea.early_out_mark,0)) AS total_marks,

    st.ttl_wrk_hr AS total_hours,

    ea.status,
    ea.day_type,
    ea.day_type1

FROM emp_atndnce ea
INNER JOIN emp_dtls ed ON ed.id = ea.employee_id
INNER JOIN emp_shift es ON es.id = ea.emp_shift_id
INNER JOIN shift_tmplt st ON st.id = es.shift_id
INNER JOIN company_details c ON c.id = ed.company_id
INNER JOIN branch_details b ON b.id = ed.branch_id
INNER JOIN department dp ON dp.id = ed.department_id
INNER JOIN designation dg ON dg.id = ed.designation_id
INNER JOIN category ct ON ct.id = ed.category_id
INNER JOIN wrk_loc loc ON loc.id = ed.work_location_id
INNER JOIN grades g ON g.id = ed.grade_id;


DROP VIEW IF EXISTS vw_employee_leave_report;

CREATE VIEW vw_employee_leave_report AS
SELECT
    ed.id AS employeeId,
    ed.emp_code AS employeeCode,
    ed.formal_name AS employeeName,

    -- 🔥 ADD THESE IDs (IMPORTANT)
    ed.company_id AS companyId,
    ed.branch_id AS branchId,
    ed.department_id AS departmentId,
    ed.designation_id AS designationId,
    ed.category_id AS categoryId,
    ed.work_location_id AS workLocationId,
    ed.grade_id AS gradeId,

    -- Names
    c.name AS company,
    b.name AS branch,
    dp.name AS department,
    dg.name AS designation,
    ct.name AS category,
    loc.name AS location,
    g.name AS grade,

    lor.id AS requestId,

    CASE
        WHEN lor.app_type = 'LEAVE_RQST' THEN 'Leave Request'
        WHEN lor.app_type = 'OD_RQST' THEN 'OD Request'
        WHEN lor.app_type = 'OD_CANCEL' THEN 'OD Cancel'
        ELSE lor.app_type
    END AS requestType,

    lor.app_dt AS applicationDate,

    lor.frm_dt AS fromDate,
    lor.frm_dt_typ AS fromDayType,
    lor.to_dt AS toDate,
    lor.to_dt_typ AS toDayType,

    lor.ttl_cnt AS totalDays,
    lor.status,
    lor.reason

FROM lv_od_rqst lor
INNER JOIN emp_dtls ed ON ed.id = lor.emp_id
INNER JOIN company_details c ON c.id = ed.company_id
INNER JOIN branch_details b ON b.id = ed.branch_id
INNER JOIN department dp ON dp.id = ed.department_id
INNER JOIN designation dg ON dg.id = ed.designation_id
INNER JOIN category ct ON ct.id = ed.category_id
INNER JOIN wrk_loc loc ON loc.id = ed.work_location_id
INNER JOIN grades g ON g.id = ed.grade_id;