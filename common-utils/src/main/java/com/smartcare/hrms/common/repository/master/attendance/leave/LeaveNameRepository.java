package com.smartcare.hrms.common.repository.master.attendance.leave;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartcare.hrms.common.entity.master.attendance.leave.LeaveName;

@Repository
public interface LeaveNameRepository extends JpaRepository<LeaveName, Long> {

	LeaveName findByCode(String code);
	
	 List<LeaveName> findByNameContainingIgnoreCase(String name);

    Optional<LeaveName> findByCodeIgnoreCase(String code);

    Boolean existsByCode(String code);
}
