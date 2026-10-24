package com.smartcare.hrms.report.repo;

import com.smartcare.hrms.common.entity.employee.Employee;
import com.smartcare.hrms.report.bean.employee.EmployeeMasterReportBean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmployeeMasterReportRepository extends JpaRepository<Employee, UUID> {

    @Query(value = """
 SELECT
    e.id,
    e.emp_code AS empCode,
    e.formal_name AS formalName,
    e.gender,
    e.dob,
    e.contact_number AS contactNumber,
    e.alternate_number AS alternateNumber,
    e.formal_email AS formalEmail,
    e.personal_email AS personalEmail,
    e.aadhaar_number AS aadhaarNumber,
    e.pan_number AS panNumber,
    e.nationality,
    e.blood_group AS bloodGroup,
    e.joining_date AS joiningDate,
    e.status,
    e.last_working_date AS lastWorkingDate,

    d.name AS departmentName,
    ds.name AS designationName,
    wl.name AS workLocationName,
    b.name AS branchName,
    c.name AS categoryName,
    g.name AS gradeName,

    es.shift_id AS shiftId,
    es.is_default AS isDefault,
    es.shift_strt_time AS shiftStrtTime,
    es.shift_end_time AS shiftEndTime,
    es.grace_time_mins AS graceTimeMins

FROM emp_dtls e

LEFT JOIN department d
       ON d.id = e.department_id

LEFT JOIN designation ds
       ON ds.id = e.designation_id

LEFT JOIN branch_details b
       ON b.id = e.branch_id

LEFT JOIN category c
       ON c.id = e.category_id

LEFT JOIN grades g
       ON g.id = e.grade_id

LEFT JOIN wrk_loc wl
       ON wl.id = e.work_location_id

LEFT JOIN emp_shift es
       ON es.emp_id = e.id
      AND es.is_default = 1

WHERE
      (:name IS NULL OR :name = '' OR e.formal_name LIKE CONCAT('%', :name, '%'))
  AND (:code IS NULL OR :code = '' OR e.emp_code = :code)
  AND (:companyId IS NULL OR e.company_id = :companyId)
  AND (:branchId IS NULL OR e.branch_id = :branchId)
  AND (:departmentId IS NULL OR e.department_id = :departmentId)
  AND (:designationId IS NULL OR e.designation_id = :designationId)
  AND (:categoryId IS NULL OR e.category_id = :categoryId)
  AND (:workLocationId IS NULL OR e.work_location_id = :workLocationId)
  AND (:gradeId IS NULL OR e.grade_id = :gradeId)
  AND (:monthYear IS NULL
                   OR DATE_FORMAT(e.joining_date,'%Y-%m') = :monthYear)

ORDER BY e.formal_name
""", nativeQuery = true)
    List<EmployeeMasterReportBean> getEmployeeMasterReport(
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
