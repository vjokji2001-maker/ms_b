package com.smartcare.hrms.hiring.repository;

import com.smartcare.hrms.hiring.model.entity.PfChallan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PfChallanRepository
        extends JpaRepository<PfChallan, Long> {

    Optional<PfChallan> findByFinancialYearAndMonthYear(
            String financialYear,
            String monthYear
    );

    Optional<PfChallan> findTopByOrderByIdDesc();
}