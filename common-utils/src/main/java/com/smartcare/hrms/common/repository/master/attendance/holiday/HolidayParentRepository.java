package com.smartcare.hrms.common.repository.master.attendance.holiday;

import java.time.Year;
import java.util.List;
import java.util.Optional;

import com.mysql.cj.x.protobuf.MysqlxCursor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartcare.hrms.common.entity.master.attendance.holiday.HolidayParent;

@Repository
public interface HolidayParentRepository extends JpaRepository<HolidayParent, Long>{

	HolidayParent findByCode(String code);
	
	List<HolidayParent> findByNameContainingIgnoreCase(String name);
	
	List<HolidayParent> findByYear(Year Year);

    Boolean existsByCode(String code);

    Optional<HolidayParent> findByCodeIgnoreCase(String code);
}
