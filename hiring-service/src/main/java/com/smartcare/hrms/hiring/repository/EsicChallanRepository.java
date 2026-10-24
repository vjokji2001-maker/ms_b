package com.smartcare.hrms.hiring.repository;

import com.smartcare.hrms.hiring.model.entity.EsicChallan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EsicChallanRepository
        extends JpaRepository<EsicChallan, Long> {

    Optional<EsicChallan> findByFinancialYearAndMonthYear(
            String financialYear,
            String monthYear
    );

    Optional<EsicChallan> findTopByOrderByIdDesc();
}