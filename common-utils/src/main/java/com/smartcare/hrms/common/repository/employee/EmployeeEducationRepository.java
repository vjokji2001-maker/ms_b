package com.smartcare.hrms.common.repository.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartcare.hrms.common.entity.employee.EmployeeEducation;

@Repository
public interface EmployeeEducationRepository extends JpaRepository<EmployeeEducation, Long> {

}
