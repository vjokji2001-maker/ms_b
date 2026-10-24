package com.smartcare.hrms.common.repository.master.attendance.period;

import com.smartcare.hrms.common.entity.master.attendance.period.YearCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface YearCycleRepository extends JpaRepository<YearCycle, Long> {

    YearCycle findByCode(String code);

    YearCycle findByFromMonthYearAndToMonthYear(LocalDate fromMonthYear, LocalDate toMonthYear);

    List<YearCycle> findByNameContainingIgnoreCase(String name);

    Boolean existsByCode(String code);
}
