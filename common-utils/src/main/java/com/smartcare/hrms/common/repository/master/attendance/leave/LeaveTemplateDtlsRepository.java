package com.smartcare.hrms.common.repository.master.attendance.leave;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartcare.hrms.common.entity.master.attendance.leave.LeaveTemplateDtls;
 
@Repository
public interface LeaveTemplateDtlsRepository extends JpaRepository<LeaveTemplateDtls, Long>{
 
}