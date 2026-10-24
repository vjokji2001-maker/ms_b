package com.smartcare.hrms.common.repository.master.attendance.leave;

import java.util.List;
import java.util.Optional;

import com.smartcare.hrms.common.entity.master.attendance.holiday.HolidayParent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartcare.hrms.common.entity.master.attendance.leave.LeaveTemplate;
 
 
@Repository
public interface LeaveTemplateRepository extends JpaRepository<LeaveTemplate, Long> {
 
	LeaveTemplate findByCode(String code);
	
	 List<LeaveTemplate> findByNameContainingIgnoreCase(String name);

    Boolean existsByCode(String code);

    Optional<LeaveTemplate> findByCodeIgnoreCase(String code);
	 
	 
	
}
 