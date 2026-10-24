package com.smartcare.hrms.common.repository.master.attendance.weekOff;

import java.util.List;
import java.util.Optional;

import com.smartcare.hrms.common.entity.master.attendance.holiday.HolidayParent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartcare.hrms.common.entity.master.attendance.weekOff.WeekOffParent;

@Repository
public interface WeekOffParentRepository extends JpaRepository<WeekOffParent, Long> {
	
	List<WeekOffParent> findByNameContainingIgnoreCase(String name);

    WeekOffParent findByCode(String code);

    Boolean existsByCode(String code);

    Optional<WeekOffParent> findByCodeIgnoreCase(String code);

}
