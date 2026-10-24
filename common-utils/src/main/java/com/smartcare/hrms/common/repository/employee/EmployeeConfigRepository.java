package com.smartcare.hrms.common.repository.employee;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.smartcare.hrms.common.bean.employee.EmployeeConfigBean;
import com.smartcare.hrms.common.bean.master.attendance.ApprovalGroupBean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import com.smartcare.hrms.common.entity.employee.EmployeeConfig;

@Repository
public interface EmployeeConfigRepository extends JpaRepository<EmployeeConfig, Long>{

	
	
	@Query(value="SELECT ed.formal_name AS employeeName, ed.emp_code AS employeeCode, "
			+ "cd.name AS companyName, bd.name AS brachName, d.name AS departmentName, dg.name AS designationName , wp.name AS weekOffName, "
			+ "g.name AS grade, c.name AS category,wl.name AS location, hp.name AS holidayName ,  ag.name AS approvalGroupName  "
			+ " FROM emp_dtls ed "
			+ "INNER JOIN company_details cd ON cd.id= ed.company_id "
			+ "INNER JOIN branch_details bd ON bd.id= ed.branch_id "
			+ "INNER JOIN department d ON d.id= ed.department_id "
			+ "INNER JOIN designation  dg ON dg.id= ed.designation_id "
			+ "INNER JOIN grades g ON g.id = ed.grade_id "
			+ "INNER JOIN category c ON c.id=ed.category_id "
			+ "INNER JOIN wrk_loc wl ON wl.id=ed.work_location_id "
			+ "LEFT JOIN emp_config ec ON ec.emp_id = ed.id "
			+ "LEFT JOIN wk_off_prnt wp ON wp.id= ec.week_off_id "
			+" LEFT JOIN holiday_prnt hp ON hp.id= ec.holiday_clndr_id "
			+"LEFT JOIN aprvl_grp ag ON ag.id= ec.aprvl_grp_id "
			+ "WHERE ed.id=:id ", nativeQuery = true)
	EmployeeConfigBean getByEmployeeId(@Param("id") String id);


	Optional<EmployeeConfig> findByEmployee_Id(UUID employeeId);
	
	List<EmployeeConfig> findByEmployee_IdIn(List<UUID> employeeId);

	@Query(value = "SELECT agd.employee_id AS approverEmpId, agd.sequence AS approvalSequence " +
			"FROM emp_config ec " +
			"INNER JOIN aprvl_grp ag ON ag.id= ec.aprvl_grp_id " +
			"INNER JOIN aprvl_grp_dtls agd ON agd.aprvl_grp_id = ag.id " +
			"WHERE ec.emp_id=:employeeId ", nativeQuery = true)
	List<ApprovalGroupBean> getEmployeeApprovalGroup(@Param("employeeId") String employeeId);
}
