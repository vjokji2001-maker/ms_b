package com.smartcare.hrms.payroll.repository.finance;

import com.smartcare.hrms.payroll.entity.finance.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankRepository extends JpaRepository<Bank, Long> {

    Optional<Bank> findByCode(String code);
}
