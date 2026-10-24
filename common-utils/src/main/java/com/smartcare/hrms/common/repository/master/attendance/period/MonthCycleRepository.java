package com.smartcare.hrms.common.repository.master.attendance.period;

import com.smartcare.hrms.common.entity.master.attendance.period.MonthCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonthCycleRepository extends JpaRepository<MonthCycle, Long> {

    MonthCycle findByCode(String code);

    MonthCycle findByStartDateAndEndDate(Integer startDate, Integer endDate);

    List<MonthCycle> findByNameContainingIgnoreCase(String name);

    Boolean existsByCode(String code);

}
