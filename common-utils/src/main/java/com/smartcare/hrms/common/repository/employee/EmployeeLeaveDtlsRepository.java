package com.smartcare.hrms.common.repository.employee;

import com.smartcare.hrms.common.entity.employee.EmployeeLeaveDtls;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeLeaveDtlsRepository extends JpaRepository<EmployeeLeaveDtls, Long> {
}
