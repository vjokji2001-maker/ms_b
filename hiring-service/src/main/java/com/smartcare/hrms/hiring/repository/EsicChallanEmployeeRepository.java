package com.smartcare.hrms.hiring.repository;

import com.smartcare.hrms.hiring.model.entity.EsicChallanEmployee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EsicChallanEmployeeRepository
        extends JpaRepository<EsicChallanEmployee, Long> {

    List<EsicChallanEmployee> findByChallanId(Long challanId);
}