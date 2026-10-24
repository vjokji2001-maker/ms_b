package com.smartcare.hrms.common.repository.master.attendance.holiday;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartcare.hrms.common.entity.master.attendance.holiday.HolidayChild;

import java.util.Optional;

@Repository
public interface HolidayChildRepository extends JpaRepository<HolidayChild, Long>{

    Optional<HolidayChild> findByName(String name);

    boolean existsByNameIgnoreCase(String name);
}
