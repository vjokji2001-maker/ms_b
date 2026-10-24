package com.smartcare.hrms.common.repository.payroll.compensation.employee;

import com.smartcare.hrms.common.entity.payroll.compensation.employee.EmpCompensationDtls;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpCompensationDtlsRepository extends JpaRepository<EmpCompensationDtls, Long> {
}
