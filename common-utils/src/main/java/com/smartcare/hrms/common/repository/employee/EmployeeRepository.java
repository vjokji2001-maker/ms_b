package com.smartcare.hrms.common.repository.employee;

import java.util.List;
import java.util.UUID;

import com.smartcare.hrms.common.bean.employee.EmployeeMonthCycleBean;
import com.smartcare.hrms.common.bean.employee.EmployeeNameCodeBean;
import com.smartcare.hrms.common.bean.employee.EmployeeWeekOffBean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.smartcare.hrms.common.bean.employee.EmployeeBean;
import com.smartcare.hrms.common.entity.employee.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
	
	
	String empQuery = "SELECT SQL_NO_CACHE ed.id AS empId, ed.salutation AS salutation, ed.first_name AS firstName, ed.middle_name AS middleName, "
	              + "ed.last_name AS lastName, ed.formal_name AS formalName, ed.gender AS gender, ed.dob AS dob, ed.marital_status AS maritalStatus, "
	              + "ed.married_since AS marriedSince, ed.contact_country_code AS contactCountryCode, ed.contact_number AS contactNumber, ed.alternate_country_code AS alternateCountryCode, "
               	  + "ed.alternate_number AS alternateNumber, ed.formal_email AS formalEmail, ed.personal_email AS personalEmail, ed.aadhaar_number AS aadhaarNumber,  "
	              + "ed.aadhaar_file_name AS aadhaarFileName, ed.document_country AS documentCountry, ed.pan_number AS panNumber, ed.pan_file_name AS panFileName, "
/* Employment Details  */
                  + "ed.nationality AS nationality, cm.id AS birthCountryId, cm.name AS birthCountryName, sm.id AS stateId, sm.name AS stateName, ct.id AS cityId, ct.name AS cityName,  "
	              + "ed.blood_group AS bloodGroup, cd.id AS companyId, cd.name AS company, b.id AS branchId, b.name AS branch, dg.id AS designationId, dg.name AS designation, "
	              + "dp.id AS departmentId, dp.name AS department, wl.id AS workLocationId, wl.name AS workLocation, "
                  + "sbl.id AS subWorkLocationId, sbl.name AS subWorkLocation, gr.id AS gradeId, gr.name AS grade, cg.id AS categoryId, cg.name AS category, "
	              + "ed.status AS status, ed.emp_code AS empCode, ed.biometric_code AS biometricCode, ed.offer_date AS offerDate, "
	              + "ed.offer_acceptance_date AS offerAcceptanceDate, ed.joining_date AS joiningDate, ed.contract_start_date AS contractStartDate, "
	              + "ed.probation_start_date AS probationStartDate, ed.probation_end_date AS probationEndDate, ed.confirmation_date AS confirmationDate, "
	              + "ed.resignation_date AS resignationDate, ed.notice_period_start_date AS noticePeriodStartDate, ed.notice_period_end_date AS noticePeriodEndDate, "
	              + "ed.termination_date AS terminationDate, ed.last_working_date AS lastWorkingDate, "   
/* Employment Address  */	 
	              + "ea.id AS addressId, addr_c.id AS countryId, addr_c.name AS countryName, addr_s.id AS addressStateId, addr_s.name AS addressStateName, addr_ct.id AS addressCityId, "
	              + "addr_ct.name AS addressCityName, ea.address AS empAddress, ea.pincode AS pincode, ea.address_type AS addressType, ea.from_date AS fromDate, ea.to_date AS toDate, "
/* Employment Family Details  */	
	              + "ef.id AS familyId, ef.name AS familyName, ef.relation AS relation, ef.contact_number AS familyContactNumber, ef.dob AS familyDob, "
/* Employment Experience  */	
	              + "ee.id AS experienceId, ee.experience_type AS experienceType, ee.company_name AS experienceCompanyName, ee.address AS expCompAddress, "
	              + "ee.designation AS empExpDesignation, ee.from_date AS expFromDate, ee.to_date AS expToDate, ee.total_span AS totalSpan, ee.reference_name AS referenceName, "
	              + "ee.reference_contact AS referenceContact, ee.reference_designation AS referenceDesignation, ee.from_month_year AS expFromMonthYear, "
	              + "ee.to_month_year AS expToMonthYear, ee.salary_slips_name AS salarySlipsName, ee.experience_certificate_name AS experienceCertificateName, "
	              + "ee.relieving_certificate_name AS relievingCertificateName, "
/* Employment Education  */	 
	              + "eed.id AS educationId, eed.education_type AS educationType, eed.institute_university_name AS eduInstituteUniversityName, "
	              + "eed.school_college_name AS schoolCollegeName, eed.from_month_year AS eduFromMonthYear, eed.to_month_year AS eduToMonthYear, "
	              + "eed.document_name AS eduDocumentName, "
/* Employment Skill  */	 	
	              + "es.id AS skillId, es.skill AS skillName, es.institute_university_name AS skillInstituteUniversityName, es.rating AS rating, "
	              + "es.from_month_year AS skillFromMonthYear, es.to_month_year AS skillToMonthYear, es.document_name AS skillDocumentName "


	              + "FROM emp_dtls AS ed "
                  + "INNER JOIN country_master AS cm ON cm.id = ed.birth_country_id "
	              + "INNER JOIN state_master AS sm ON sm.id = ed.state_id "
	              + "INNER JOIN city_master AS ct ON ct.id = ed.city_id "
	              + "INNER JOIN company_details AS cd ON cd.id = ed.company_id "
	              + "INNER JOIN branch_details AS b ON b.id = ed.branch_id "
	              + "INNER JOIN designation AS dg ON dg.id = ed.designation_id "
	              + "INNER JOIN department AS dp ON dp.id = ed.department_id "
	              + "INNER JOIN wrk_loc AS wl ON wl.id = ed.work_location_id "
	              + "LEFT JOIN sub_wrk_loc AS sbl ON sbl.id = ed.sub_work_location_id "
	              + "INNER JOIN grades AS gr ON gr.id = ed.grade_id "
	              + "INNER JOIN category AS cg ON cg.id = ed.category_id "
	              + "LEFT JOIN emp_address AS ea ON ea.emp_id = ed.id "
                  + "LEFT JOIN country_master AS addr_c ON addr_c.id = ea.country_id "
                  + "LEFT JOIN state_master AS addr_s ON addr_s.id = ea.state_id "
                  + "LEFT JOIN city_master AS addr_ct ON addr_ct.id = ea.city_id "
	              + "LEFT JOIN emp_family_dtls AS ef ON ef.emp_id = ed.id "
	              + "LEFT JOIN emp_experience AS ee ON ee.emp_id = ed.id "
	              + "LEFT JOIN emp_education AS eed ON eed.emp_id = ed.id "
	              + "LEFT JOIN emp_skills AS es ON es.emp_id = ed.id "
	              + "WHERE ed.id = :id ";
	@Query(value = empQuery, nativeQuery = true)
	List<EmployeeBean> getAllById(@Param("id") String id);

	@Query(value = "SELECT SQL_NO_CACHE ed.id AS empId, ed.formal_name AS formalName, ed.emp_code AS empCode FROM emp_dtls AS ed "
                 + "WHERE ed.formal_name LIKE (%:searchText%)", nativeQuery = true)
	List<EmployeeNameCodeBean> getByEmployeeName(@Param("searchText") String searchText);

    @Query(value = "SELECT SQL_NO_CACHE ed.id AS empId, ed.formal_name AS formalName, ed.emp_code AS empCode FROM emp_dtls AS ed "
                 + "WHERE ed.emp_code = :searchText ", nativeQuery = true)
    EmployeeNameCodeBean getByEmployeeCode(@Param("searchText") String searchText);

    Employee findByEmpCode(String empCode);

    @Query(value = "SELECT SQL_NO_CACHE ed.id FROM emp_dtls AS ed WHERE ed.id = :empId ", nativeQuery = true)
    UUID getEmpId(@Param("empId") String empId);

	@Query(value = "SELECT mc.start_date AS startDate,mc.end_date AS endDate " +
			"FROM emp_dtls ed " +
			"INNER JOIN grades g ON g.id = ed.grade_id " +
			"INNER JOIN month_cycle mc ON mc.grade_id = g.id " +
			"WHERE ed.id=:empId ",nativeQuery = true)
	EmployeeMonthCycleBean getEmployeeMonthCycle(@Param("empId") String empId);

    @Query(value="SELECT   wd.week_no AS weekNo, wd.day AS day " +
            " FROM emp_dtls ed " +
            "INNER JOIN emp_config ec ON ec.emp_id= ed.id " +
            "INNER JOIN wk_off_prnt wp ON wp.id= ec.week_off_id " +
            "INNER JOIN wk_off_dtl wd ON wd.wk_off_id= wp.id " +
            "WHERE ed.id=:empId ", nativeQuery = true)
    List<EmployeeWeekOffBean> getEmployeeWeekDetail(@Param("empId") String empId);

    @Query(value = "SELECT id AS empId, formal_name AS formalName, emp_code AS empCode  FROM emp_dtls WHERE status='ACTIVE' ", nativeQuery = true)
    List<EmployeeBean> getEmployeeDetails();

    @Query(value = "SELECT id AS empId, formal_name AS formalName, emp_code AS empCode  FROM emp_dtls WHERE status='ACTIVE' AND ed.id=:empId ", nativeQuery = true)
    List<EmployeeBean> getEmployeeDetailsById(@Param("empId") String empId);

    Boolean existsByEmpCode(String code);


    @Query("SELECT e.empCode, e.id FROM Employee e WHERE e.empCode IN (:empCodes)")
    List<Object[]> findEmpCodeAndIdByEmpCodes(@Param("empCodes") List<String> empCodes);

}
