package com.smartcare.hrms.common.repository.attendance;

import com.smartcare.hrms.common.bean.attendance.EmployeeAttendanceSummaryBean;
import com.smartcare.hrms.common.entity.attendance.EmployeeAttendanceSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeAttendanceSummaryRepository extends JpaRepository<EmployeeAttendanceSummary, UUID> {

    List<EmployeeAttendanceSummary> findByAttendanceSalaryLinkIdIn(List<String> linkIds);

    Optional<EmployeeAttendanceSummary> findByAttendanceSalaryLinkId(UUID linkId);

    @Query(value="SELECT ed.id AS empId,asl.id attendanceSalaryLinkId, ed.formal_name AS empName, ed.emp_code AS empCode,ttl_mnth_days AS totalMonthDays , " +
            "ttl_present_days AS totalPresentDays, ttl_week_off_days AS totalWeekOfdDays,ttl_paid_lvs AS totalPaidLeaves, " +
            "ttl_holidays AS totalHolidays, ttl_late_mark AS totalLateMark,ttl_late_abs AS totalLateAbs,  " +
            "ttl_late_lvs AS totalLateLeaves,ttl_abs AS totalAbs,ttl_unpaid_lvs AS totalUnpaidLeaves,asl.a_status AS status " +
            "FROM attendance_salary_link asl " +
            "INNER JOIN emp_dtls ed ON ed.id= asl.employee_id  " +
            "LEFT JOIN emp_attendance_summary eas ON  eas.attendance_salary_link_id= asl.id "+
            "WHERE asl.month_year=:monthYear " +
            "GROUP BY asl.id  ", nativeQuery = true)
    List<EmployeeAttendanceSummaryBean> getEmployeeAttendanceSummary(@Param("monthYear") String monthYear);

}

