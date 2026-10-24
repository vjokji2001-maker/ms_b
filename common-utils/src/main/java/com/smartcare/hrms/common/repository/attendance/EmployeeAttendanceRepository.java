package com.smartcare.hrms.common.repository.attendance;

import com.smartcare.hrms.common.bean.attendance.AttendanceBean;
import com.smartcare.hrms.common.bean.attendance.AttendanceDetailsBean;
import com.smartcare.hrms.common.bean.attendance.EmployeeAttendanceBean;
import com.smartcare.hrms.common.entity.attendance.EmployeeAttendance;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeAttendanceRepository  extends JpaRepository<EmployeeAttendance, Long> {

    Optional<EmployeeAttendance> findByEmployeeIdAndAttendanceDate(
            String employeeId,
            LocalDate atndnceDate
    );

    @Query(value = "SELECT es.emp_id AS employeeId, DATE_FORMAT(ea.atndnce_date,'%d/%m/%Y') AS `date`, " +
            "DATE_FORMAT(ea.atndnce_date,'%a') AS `day`, " +
            "st.code AS shiftCode, st.in_time AS shiftInTime, st.out_time AS shiftOutTime, " +
            "ea.in_time AS empInTime, ea.out_time AS empOutTime, " +
            "ea.day_type AS dayType1, ea.day_type1 AS dayType2, " +
            "ea.`portion` AS `portion`, ea.late_in_mark AS lateInMark, ea.early_out_mark AS earlyOutMark, " +
            "(ea.late_in_mark + ea.early_out_mark) AS totalMarks, " +
            "st.ttl_wrk_hr AS totalHours, ea.status AS status " +
            "FROM emp_atndnce ea " +
            "INNER JOIN emp_shift es ON es.id = ea.emp_shift_id " +
            "INNER JOIN shift_tmplt st ON st.id = es.shift_id " +
            "WHERE es.emp_id = :empId " +
            "AND ea.atndnce_date BETWEEN :fromDate AND :toDate " +
            "ORDER BY ea.atndnce_date ASC ",
            nativeQuery = true)
    List<EmployeeAttendanceBean> findByEmployeeIdAndDateRange(
            @Param("empId") String empId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );



    @Query(value = "SELECT  ed.formal_name AS empName,ed.emp_code AS empCode,st.code AS shiftCode, st.name AS shiftName, st.in_time AS shiftInTime, st.out_time AS shiftOutTime, es.date AS shiftDate, " +
            "ea.in_time AS empInTime, ea.out_time AS empOutTime, ln.name AS LeaveName, " +
            "lor.app_type AS appType,   lor.reason AS leaveReason,lor.status AS status, " +
            "CASE WHEN DATE(lor.frm_dt) = DATE(lor.to_dt) THEN lor.frm_dt  WHEN es.date = DATE(lor.frm_dt) THEN lor.frm_dt" +
            " WHEN es.date = DATE(lor.to_dt) THEN lor.to_dt  ELSE es.date END AS leaveDate, "+
            "CASE WHEN DATE(lor.frm_dt) = DATE(lor.to_dt) THEN lor.frm_dt_typ WHEN es.date = DATE(lor.frm_dt)         THEN lor.frm_dt_typ " +
            "WHEN es.date = DATE(lor.to_dt)          THEN lor.to_dt_typ ELSE 'FULL_DAY' END AS leaveType, "+
            "sr.swp_date AS swipeDate, sr.req_in_time AS swipeRqstInTime, sr.req_out_time AS swipeRqstOutTime, sr.remark AS swipeRemark, " +
            "el.punch_date_time AS punchTime, el.punch_type AS punchType, el.direction AS direction " +
            "FROM emp_atndnce ea " +
            "INNER JOIN emp_dtls ed ON ed.id = ea.employee_id " +
            "INNER JOIN emp_shift es ON es.emp_id = ed.id " +
            "INNER JOIN shift_tmplt st ON st.id = es.shift_id " +
            "LEFT JOIN lv_od_rqst lor ON lor.emp_id = ea.employee_id " +
            "AND es.date BETWEEN DATE(lor.frm_dt) AND DATE(lor.to_dt)   "+
            "AND lor.app_type NOT IN ('LEAVE_CANCEL', 'OD_CANCEL') " +
            "LEFT JOIN emp_daily_punch_log el ON el.emp_code = ed.emp_code " +
            "AND DATE(el.punch_date_time) = es.date " +
            "LEFT JOIN emp_swp_rqst sr ON sr.emp_id = ea.employee_id " +
            "LEFT JOIN leave_name ln ON ln.id = lor.lv_nm_id " +
            "WHERE ea.employee_id = :employeeId " +
            "AND es.date = :date " +
            "ORDER BY el.punch_date_time",
            nativeQuery = true)
    List<AttendanceDetailsBean> getAttendanceDetails(
            @Param("employeeId") String employeeId,
            @Param("date") LocalDate date
    );

    @Modifying
    @Transactional
    @Query(value =
            "INSERT INTO emp_atndnce " +
                    "  (employee_id, emp_shift_id, atndnce_date, in_time, out_time, " +
                    "   day_type, day_type1, `portion`, late_in_mark, early_out_mark,nsa_amt,nsa_days, status) " +
                    "VALUES " +
                    "  (:employeeId, :empShiftId, :attendanceDate, :inTime, :outTime, " +
                    "   :dayType, :dayType1, :portion, :lateInMark, :earlyOutMark,:nightShiftAmount,:nightShiftDays, :status) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "  emp_shift_id   = VALUES(emp_shift_id), " +
                    "  in_time        = VALUES(in_time), " +
                    "  out_time       = VALUES(out_time), " +
                    "  day_type       = VALUES(day_type), " +
                    "  day_type1      = VALUES(day_type1), " +
                    "  `portion`      = VALUES(`portion`), " +
                    "  late_in_mark   = VALUES(late_in_mark), " +
                    "  early_out_mark = VALUES(early_out_mark), " +
                    "  nsa_amt        = VALUES(nsa_amt),  "+
                    "  nsa_days        = VALUES(nsa_days),  "+
                    "  status         = VALUES(status)",
            nativeQuery = true)
    void upsertAttendance(
            @Param("employeeId")     String    employeeId,
            @Param("empShiftId")     Long      empShiftId,
            @Param("attendanceDate") LocalDate attendanceDate,
            @Param("inTime")         LocalTime inTime,
            @Param("outTime")        LocalTime outTime,
            @Param("dayType")        String    dayType,
            @Param("dayType1")       String    dayType1,
            @Param("portion")        Double    portion,
            @Param("lateInMark")     Double    lateInMark,
            @Param("earlyOutMark")   Double    earlyOutMark,
            @Param("nightShiftAmount")   Double    nightShiftAmount,
            @Param("nightShiftDays")   Double    nightShiftDays,
            @Param("status")         String    status
    );

    @Query(value =" SELECT DATE_FORMAT(hc.date, '%Y-%m-%d') AS date FROM emp_config ec " +
            "INNER JOIN holiday_prnt hp ON hp.id=ec.holiday_clndr_id " +
            "INNER JOIN holiday_chld hc ON hc.holiday_id= hp.id " +
            "WHERE ec.emp_id=:empId AND hc.date BETWEEN :fromDate AND :toDate", nativeQuery = true)
    List<String> getHolidayDatesForMonth(@Param("empId") String empId, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

@Query(value="SELECT atndnce_date FROM emp_atndnce " +
        "WHERE employee_id =:empId " +
        " AND atndnce_date BETWEEN :fromDate AND :toDate " +
        " AND status='LOCKED' ",nativeQuery = true)
List<String> findLockedDatesByEmployeeAndMonth(@Param("empId") String empId, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    @Query(value = "SELECT  ea.day_type AS dayType1,ea.day_type1 AS dayType2,ea.`portion` AS `portion`, ea.nsa_amt AS nightShiftAllowanceAmount,ea.nsa_days AS nightShiftDays, " +
                    "ea.late_in_mark  AS lateInMark,ea.early_out_mark AS earlyOutMark,ea.in_time AS inTime, ea.out_time AS outTime, asl.id AS attendanceSalaryLinkId,ea.status AS status  " +
                    "FROM emp_atndnce ea " +
                    "INNER JOIN attendance_salary_link asl ON  asl.employee_id = ea.employee_id " +
                    " AND DATE_FORMAT(ea.atndnce_date, '%Y-%m') = asl.month_year " +
                    "WHERE asl.id = :attendanceSalaryLinkId", nativeQuery = true)
    List<EmployeeAttendanceBean> findAttendanceByLinkId(
            @Param("attendanceSalaryLinkId") String attendanceSalaryLinkId);


    @Modifying
    @Transactional
    @Query(value="UPDATE emp_atndnce SET status ='LOCKED' WHERE employee_id=:empId AND atndnce_date IN (:dates) ", nativeQuery = true)
    int lockAttendance( @Param("empId") String empId, @Param("dates") List<String> dates);


    @Query(value= " SELECT * FROM vw_employee_attendance "
                + "WHERE attendanceDate  BETWEEN :fromDate AND :toDate "
                + "AND (:name IS NULL OR employeeName LIKE CONCAT('%',:name,'%')) "
                + "AND (:code IS NULL OR employeeCode=:code ) "
                + "AND (:departmentId IS NULL OR departmentId=:departmentId) "
                + "AND (:companyId IS NULL OR companyId=:companyId) "
                + "AND (:branchId IS NULL OR branchId=:branchId) "
                + "AND (:designationId IS NULL OR designationId=:designationId) "
                + "AND (:categoryId IS NULL OR categoryId=:categoryId) "
                + "AND (:workLocationId IS NULL OR workLocationId=:workLocationId) "
                + "AND (:gradeId IS NULL OR gradeId=:gradeId) "
                 , nativeQuery = true)
    List<AttendanceBean> getAttendanceReport(@Param("fromDate") String fromDate, @Param("toDate") String toDate,
                                             @Param("name") String name, @Param("code") String code,
                                             @Param("departmentId") Integer departmentId, @Param("companyId") Integer companyId,
                                             @Param("branchId") Integer branchId, @Param("designationId") Integer designationId,
                                             @Param("categoryId") Integer categoryId, @Param("workLocationId") Integer workLocationId,
                                             @Param("gradeId") Integer gradeId);

}
