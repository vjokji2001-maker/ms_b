package com.smartcare.hrms.common.repository.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartcare.hrms.common.entity.employee.EmployeeFamilyDetails;

@Repository
public interface EmployeeFamilyDetailsRepository extends JpaRepository<EmployeeFamilyDetails, Long> {

}
