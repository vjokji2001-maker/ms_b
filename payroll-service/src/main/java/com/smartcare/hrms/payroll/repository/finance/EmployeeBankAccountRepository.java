package com.smartcare.hrms.payroll.repository.finance;

import com.smartcare.hrms.payroll.entity.finance.EmployeeBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EmployeeBankAccountRepository extends JpaRepository<com.smartcare.hrms.payroll.entity.finance.EmployeeBankAccount, UUID> {
}
