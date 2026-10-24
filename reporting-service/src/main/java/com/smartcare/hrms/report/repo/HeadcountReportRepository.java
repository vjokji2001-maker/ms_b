package com.smartcare.hrms.report.repo;

import com.smartcare.hrms.common.entity.employee.Employee;
import com.smartcare.hrms.report.bean.employee.HeadcountReportBean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HeadcountReportRepository extends JpaRepository<Employee, UUID> {

    @Query(value = """
        SELECT
            dept.code AS departmentCode,
            dept.name AS departmentName,

            COUNT(emp.id) AS currentHeadcount,

            SUM(CASE
                    WHEN emp.status = 'ACTIVE'
                    THEN 1 ELSE 0
                END) AS activeEmployees,

            SUM(CASE
                    WHEN CURDATE() BETWEEN emp.probation_start_date
                                      AND emp.probation_end_date
                    THEN 1 ELSE 0
                END) AS probationEmployees,

            SUM(CASE
                    WHEN emp.confirmation_date IS NOT NULL
                    THEN 1 ELSE 0
                END) AS confirmedEmployees,

            SUM(CASE
                    WHEN emp.confirmation_date IS NOT NULL
                    THEN 1 ELSE 0
                END) AS permanentEmployees,

            SUM(CASE
                    WHEN emp.contract_start_date IS NOT NULL
                     AND emp.confirmation_date IS NULL
                    THEN 1 ELSE 0
                END) AS contractEmployees,

            SUM(CASE
                    WHEN emp.gender = 'MALE'
                    THEN 1 ELSE 0
                END) AS maleEmployees,

            SUM(CASE
                    WHEN emp.gender = 'FEMALE'
                    THEN 1 ELSE 0
                END) AS femaleEmployees,

            SUM(CASE
                    WHEN emp.joining_date BETWEEN :fromDate AND :toDate
                    THEN 1 ELSE 0
                END) AS newJoiners,

            SUM(CASE
                    WHEN emp.notice_period_start_date IS NOT NULL
                     AND (
                            emp.last_working_date IS NULL
                            OR emp.last_working_date >= CURDATE()
                         )
                    THEN 1 ELSE 0
                END) AS employeesOnNoticePeriod,

            SUM(CASE
                    WHEN emp.resignation_date IS NOT NULL
                    THEN 1 ELSE 0
                END) AS resignedEmployees,

            SUM(CASE
                    WHEN emp.termination_date IS NOT NULL
                    THEN 1 ELSE 0
                END) AS terminatedEmployees,

            SUM(CASE
                    WHEN emp.last_working_date IS NOT NULL
                     AND emp.last_working_date < CURDATE()
                    THEN 1 ELSE 0
                END) AS exitedEmployees,

            b.name AS branchName,
            wl.name AS workLocation,

            CURRENT_DATE() AS lastUpdatedDate,

            c.id AS companyId,
            b.id AS branchId,
            dept.id AS departmentId,
            wl.id AS workLocationId

        FROM emp_dtls emp

        LEFT JOIN department dept
               ON dept.id = emp.department_id

        LEFT JOIN company_details c
               ON c.id = emp.company_id

        LEFT JOIN branch_details b
               ON b.id = emp.branch_id

        LEFT JOIN wrk_loc wl
               ON wl.id = emp.work_location_id

        WHERE
              (:companyId IS NULL OR emp.company_id = :companyId)
          AND (:branchId IS NULL OR emp.branch_id = :branchId)
          AND (:departmentId IS NULL OR emp.department_id = :departmentId)
          AND (:workLocationId IS NULL OR emp.work_location_id = :workLocationId)

        GROUP BY
            dept.id,
            dept.code,
            dept.name,
            b.name,
            wl.name,
            c.id,
            b.id,
            wl.id

        ORDER BY dept.name
        """, nativeQuery = true)
    List<HeadcountReportBean> getHeadcountReport(
            @Param("fromDate") String fromDate,
            @Param("toDate") String toDate,
            @Param("companyId") Integer companyId,
            @Param("branchId") Integer branchId,
            @Param("departmentId") Integer departmentId,
            @Param("workLocationId") Integer workLocationId
    );
}