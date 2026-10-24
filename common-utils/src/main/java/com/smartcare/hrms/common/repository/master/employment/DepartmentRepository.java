package com.smartcare.hrms.common.repository.master.employment;

import com.smartcare.hrms.common.entity.master.employment.Department;

import com.smartcare.hrms.common.entity.master.employment.Designation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long>{

	List<Department> findByNameContainingIgnoreCase(String name);

	Department findByCode(String code);

    boolean existsByCode(String code);

    Department findByCodeIgnoreCase(String code);
}
