package com.smartcare.hrms.common.repository.master.attendance.approvalgroup;

import com.smartcare.hrms.common.bean.master.attendance.ApprovalGroupBean;

import com.smartcare.hrms.common.entity.master.attendance.approvalgroup.ApprovalGroup;

import com.smartcare.hrms.common.entity.master.attendance.weekOff.WeekOffParent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApprovalGroupRepository extends JpaRepository<ApprovalGroup, UUID> {
	
	ApprovalGroup findByCode(String code);
	
	 List<ApprovalGroup> findByNameContainingIgnoreCase(String name);


	@Query(value= "SELECT ag.id AS id, ag.name AS approvalName, ag.code AS approvalCode,agd.id AS approvalGroupDetailId, " +
			"agd.sequence AS approvalSequence, agd.employee_id AS employeeId, ed.formal_name AS employeeName , ed.emp_code AS  employeeCode " +
			"FROM aprvl_grp ag " +
			"INNER JOIN aprvl_grp_dtls agd ON agd.aprvl_grp_id = ag.id " +
			"LEFT JOIN emp_dtls ed ON ed.id = agd.employee_id " +
			"WHERE ag.id = :id", nativeQuery = true)
	List<ApprovalGroupBean> findDetailsByApprovalGroupId(@Param("id") String id);


    Boolean existsByCode(String code);

    Optional<ApprovalGroup> findByCodeIgnoreCase(String code);
}


