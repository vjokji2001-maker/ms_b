package com.smartcare.hrms.common.repository.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartcare.hrms.common.entity.employee.EmployeeExperience;

@Repository
public interface EmployeeExperienceRepository extends JpaRepository<EmployeeExperience, Long> {

}
