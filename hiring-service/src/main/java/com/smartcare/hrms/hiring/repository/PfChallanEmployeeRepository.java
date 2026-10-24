package com.smartcare.hrms.hiring.repository;

import com.smartcare.hrms.hiring.model.entity.PfChallanEmployee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PfChallanEmployeeRepository
        extends JpaRepository<PfChallanEmployee, Long> {

    List<PfChallanEmployee> findByChallanId(Long challanId);
}