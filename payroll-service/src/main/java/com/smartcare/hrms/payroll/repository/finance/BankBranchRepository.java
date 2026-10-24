package com.smartcare.hrms.payroll.repository.finance;

import com.smartcare.hrms.payroll.entity.finance.BankBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankBranchRepository extends JpaRepository<BankBranch, Long> {

    Optional<BankBranch> findByIfscCode(String ifsc);
    List<BankBranch> findByBankId(Long bankId);
}
