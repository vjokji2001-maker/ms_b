package com.smartcare.hrms.common.repository.payroll.salary;

import com.smartcare.hrms.common.bean.payroll.SalaryReportBean;
import com.smartcare.hrms.common.entity.payroll.salary.EmpMonthlyPayrollSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmpMonthlyPayrollSummaryRepository extends JpaRepository<EmpMonthlyPayrollSummary, UUID> {

    @Query("""
        SELECT eps
        FROM EmpMonthlyPayrollSummary eps
        JOIN FETCH eps.attendanceSalaryLink
        WHERE eps.id = :payrollId
        """)
    Optional<EmpMonthlyPayrollSummary> findByIdWithLink(@Param("payrollId") UUID payrollId);

    @Query("""
        SELECT eps
        FROM EmpMonthlyPayrollSummary eps
        JOIN FETCH eps.attendanceSalaryLink
        WHERE eps.id IN (:payrollIds)
        """)
    List<EmpMonthlyPayrollSummary> findByPayrollIdIn(@Param("payrollIds") List<UUID> payrollIds);

    @Query("""
        SELECT eps
        FROM EmpMonthlyPayrollSummary eps
        JOIN FETCH eps.attendanceSalaryLink asl
        WHERE asl.id IN (:linkIds)
        """)
    List<EmpMonthlyPayrollSummary> findByAttendanceSalaryLinkIdIn(@Param("linkIds") List<UUID> payrollIds);


    @Query(value = """
    SELECT
        ed.id                                   AS employeeId,
        ed.emp_code                             AS employeeCode,
        ed.formal_name                          AS employeeName,
        asl.id                                  AS attendanceSalaryLinkId,
        asl.month_year                          AS monthYear,
        emps.payable_days                       AS payableDays,
        emps.lop_days                           AS lopDays,
        emps.ttl_earnings                       AS totalEarnings,
        emps.ttl_std_deductions                 AS totalStdDeductions,
        emps.ttl_othr_deductions                AS totalOtherDeductions,
        emps.ttl_payable_earnings               AS totalPayableEarnings,
        emps.ttl_payable_std_deductions         AS totalPayableStdDeductions,
        emps.ttl_payable_othr_deductions        AS totalPayableOtherDeductions,
        emps.net_payable                        AS netPayable,
        ech.gross_amt                           AS grossAmount,
        ech.std_deduction_amt                   AS stdDeductionAmount,
        ech.other_deduction_amt                 AS otherDeductionAmount,
        ech.net_amt                             AS netAmount,
        ecd.seq                                 AS componentSeq,
        ecd.component_id                        AS componentId,
        ecd.component_type                      AS componentType,
        ecd.cal_typ                             AS calculationType,
        ecd.amt                                 AS componentAmount,
        cm.name                                 AS componentName,
        cm.code                                 AS componentCode,
        c.id                                    AS companyId,
        b.id                                    AS branchId,
        dp.id                                   AS departmentId,
        dg.id                                   AS designationId,
        ct.id                                   AS categoryId,
        loc.id                                  AS workLocationId,
        g.id                                    AS gradeId
    FROM emp_monthly_payroll_summary emps
    INNER JOIN attendance_salary_link asl   ON asl.id             = emps.attendance_salary_link_id
    INNER JOIN emp_dtls ed                  ON ed.id              = asl.employee_id
    INNER JOIN emp_compensation_hdr ech     ON ech.emp_id         = ed.id
    INNER JOIN emp_compensation_dtls ecd    ON ecd.emp_cmpnstn_id = ech.id
    INNER JOIN compensation_head cm         ON cm.id              = ecd.component_id
    INNER JOIN company_details c            ON c.id               = ed.company_id
    INNER JOIN branch_details b             ON b.id               = ed.branch_id
    INNER JOIN department dp                ON dp.id              = ed.department_id
    INNER JOIN designation dg               ON dg.id              = ed.designation_id
    INNER JOIN category ct                  ON ct.id              = ed.category_id
    INNER JOIN wrk_loc loc                  ON loc.id             = ed.work_location_id
    INNER JOIN grades g                     ON g.id               = ed.grade_id
    WHERE
        asl.month_year      = :monthYear
        AND (:name          IS NULL OR ed.formal_name  LIKE CONCAT('%', :name, '%'))
        AND (:code          IS NULL OR ed.emp_code     = :code)
        AND (:companyId     IS NULL OR c.id            = :companyId)
        AND (:branchId      IS NULL OR b.id            = :branchId)
        AND (:departmentId  IS NULL OR dp.id           = :departmentId)
        AND (:designationId IS NULL OR dg.id           = :designationId)
        AND (:categoryId    IS NULL OR ct.id           = :categoryId)
        AND (:workLocationId IS NULL OR loc.id         = :workLocationId)
        AND (:gradeId       IS NULL OR g.id            = :gradeId)
    ORDER BY ed.emp_code ASC, ecd.seq ASC
    """, nativeQuery = true)
    List<SalaryReportBean> fetchSalaryReport(
            @Param("monthYear")       String  monthYear,
            @Param("name")            String  name,
            @Param("code")            String  code,
            @Param("companyId")       Integer companyId,
            @Param("branchId")        Integer branchId,
            @Param("departmentId")    Integer departmentId,
            @Param("designationId")   Integer designationId,
            @Param("categoryId")      Integer categoryId,
            @Param("workLocationId")  Integer workLocationId,
            @Param("gradeId")         Integer gradeId
    );

}

