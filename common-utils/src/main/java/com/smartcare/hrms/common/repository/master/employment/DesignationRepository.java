package com.smartcare.hrms.common.repository.master.employment;

import com.smartcare.hrms.common.entity.master.employment.BranchDetails;
import com.smartcare.hrms.common.entity.master.employment.Designation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface DesignationRepository extends JpaRepository<Designation, Long> {

	List<Designation> findByNameContainingIgnoreCase(String name);
	
	Designation findByCode(String code);

    boolean existsByCode(String code);

    Designation findByCodeIgnoreCase(String code);





}
