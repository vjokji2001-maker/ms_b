package com.smartcare.hrms.common.repository.employee;

import com.smartcare.hrms.common.bean.attendance.EmployeeLeaveDataBean;
import com.smartcare.hrms.common.bean.employee.EmployeeConfigBean;
import com.smartcare.hrms.common.entity.employee.Employee;
import com.smartcare.hrms.common.entity.employee.EmployeeLeaveParent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeLeaveParentRepository extends JpaRepository<EmployeeLeaveParent, Long> {

    EmployeeLeaveParent findByEmployee(Employee employee);

    @Query(value="SELECT ed.formal_name AS employeeName, ed.emp_code AS employeeCode,  " +
            "cd.name AS companyName, bd.name AS brachName, d.name AS departmentName, dg.name AS designationName ,  " +
            "g.name AS grade, c.name AS category,wl.name AS location,lt.name AS leaveTemplateName, " +
            "yc.name AS yearCycleName,elp.id AS employeeLeaveParentId,eld.future_days AS futureDays, eld.half_day_allowed AS halfDayAllowed, " +
            "eld.mnthly_count AS monthlyCount,eld.pst_days AS pastDays, eld.qtrly_count AS quaterlyCount, eld.yrly_count AS yearlyCount,ln.id AS leaveNameId, ln.name AS leaveName " +
            "FROM emp_dtls ed  " +
            "INNER JOIN company_details cd ON cd.id= ed.company_id  " +
            "INNER JOIN branch_details bd ON bd.id= ed.branch_id  " +
            "INNER JOIN department d ON d.id= ed.department_id  " +
            "INNER JOIN designation  dg ON dg.id= ed.designation_id  " +
            "INNER JOIN grades g ON g.id = ed.grade_id  " +
            "INNER JOIN category c ON c.id=ed.category_id  "+
            "INNER JOIN wrk_loc wl ON wl.id=ed.work_location_id  " +
            "LEFT JOIN emp_config ec ON ec.emp_id = ed.id " +
            "LEFT JOIN emp_leave_prnt elp ON elp.emp_id=ed.id " +
            "LEFT JOIN emp_leave_dtls eld ON eld.enp_leave_id=elp.id " +
            "LEFT JOIN year_cycle yc ON yc.id= elp.year_id " +
            "LEFT JOIN leave_tmplt lt ON lt.id= elp.leave_tmplt_id " +
            "LEFT JOIN leave_name ln ON ln.id=eld.leave_name_id " +
            " WHERE ed.id=:id", nativeQuery = true)
    List<EmployeeConfigBean> getByEmployeeId(@Param("id") String id);

    @Query(value = "SELECT eld.yrly_count AS yearlyCount, eld.qtrly_count AS quarterlyCount," +
            "eld.mnthly_count AS monthlyCount,eld.pst_days AS pastDaysAllowed,eld.future_days AS futureDaysAllowed, " +
            "eld.half_day_allowed AS halfDayAllowed " +
            "FROM emp_leave_prnt elp " +
            "INNER JOIN emp_leave_dtls eld ON  elp.id = eld.enp_leave_id AND eld.leave_name_id=:leaveNameId " +
            "WHERE elp.emp_id=:empId AND elp.year_id=:yearId ",nativeQuery = true)
    EmployeeLeaveDataBean getLeaveDetailsByYearAndEmpId(@Param("empId") String empId, @Param("yearId") Long yearId, @Param("leaveNameId") Long leaveNameId);

}
