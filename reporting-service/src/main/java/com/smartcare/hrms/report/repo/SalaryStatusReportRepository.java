package com.smartcare.hrms.report.repo;

import com.smartcare.hrms.common.entity.payroll.salary.EmpMonthlyPayrollSummary;
import com.smartcare.hrms.report.bean.employee.SalaryStatusReportBean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SalaryStatusReportRepository extends JpaRepository<EmpMonthlyPayrollSummary, UUID> {


        @Query(value = """
                SELECT
                    emp.id AS employeeId,
                    emp.emp_code AS employeeCode,
                
                    TRIM(CONCAT(
                        IFNULL(emp.first_name,''),
                        ' ',
                        IFNULL(emp.middle_name,''),
                        ' ',
                        IFNULL(emp.last_name,'')
                    )) AS employeeName,
                
                    dept.name AS department,
                    desg.name AS designation,
                
                    att.ttl_mnth_days AS totalMonthDays,
                    att.ttl_present_days AS presentDays,
                    att.ttl_abs AS absentDays,
                    att.ttl_paid_lvs AS paidLeaves,
                    att.ttl_unpaid_lvs AS unpaidLeaves,
                
                    pay.ttl_earnings AS grossSalary,
                
                    (IFNULL(pay.ttl_std_deductions,0)
                     + IFNULL(pay.ttl_othr_deductions,0)) AS deductions,
                
                    pay.net_payable AS netSalary,
                
                    asl.p_status AS salaryStatus,
                    asl.month_year AS salaryMonth,
                
                    '' AS paymentDate,
                    '' AS paymentMode,
                
                    c.id AS companyId,
                    b.id AS branchId,
                    dept.id AS departmentId,
                    desg.id AS designationId,
                    ct.id AS categoryId,
                    wl.id AS workLocationId,
                    g.id AS gradeId
                
                FROM emp_monthly_payroll_summary pay
                
                INNER JOIN attendance_salary_link asl
                        ON asl.id = pay.attendance_salary_link_id
                
                INNER JOIN emp_dtls emp
                        ON emp.id = asl.employee_id
                
                LEFT JOIN emp_attendance_summary att
                       ON att.attendance_salary_link_id = asl.id
                
                LEFT JOIN department dept
                       ON dept.id = emp.department_id
                
                LEFT JOIN designation desg
                       ON desg.id = emp.designation_id
                
                LEFT JOIN company_details c
                       ON c.id = emp.company_id
                
                LEFT JOIN branch_details b
                       ON b.id = emp.branch_id
                
                LEFT JOIN category ct
                       ON ct.id = emp.category_id
                
                LEFT JOIN wrk_loc wl
                       ON wl.id = emp.work_location_id
                
                LEFT JOIN grades g
                       ON g.id = emp.grade_id
                
                WHERE
                    (:monthYear IS NULL OR asl.month_year = :monthYear)
                    AND (:name IS NULL OR emp.formal_name LIKE CONCAT('%', :name, '%'))
                    AND (:code IS NULL OR emp.emp_code = :code)
                    AND (:companyId IS NULL OR c.id = :companyId)
                    AND (:branchId IS NULL OR b.id = :branchId)
                    AND (:departmentId IS NULL OR dept.id = :departmentId)
                    AND (:designationId IS NULL OR desg.id = :designationId)
                    AND (:categoryId IS NULL OR ct.id = :categoryId)
                    AND (:workLocationId IS NULL OR wl.id = :workLocationId)
                    AND (:gradeId IS NULL OR g.id = :gradeId)
                
                ORDER BY emp.emp_code
                """, nativeQuery = true)
        List<SalaryStatusReportBean> getSalaryStatusReport(
                @Param("monthYear") String monthYear,
                @Param("name") String name,
                @Param("code") String code,
                @Param("companyId") Integer companyId,
                @Param("branchId") Integer branchId,
                @Param("departmentId") Integer departmentId,
                @Param("designationId") Integer designationId,
                @Param("categoryId") Integer categoryId,
                @Param("workLocationId") Integer workLocationId,
                @Param("gradeId") Integer gradeId
        );
}
