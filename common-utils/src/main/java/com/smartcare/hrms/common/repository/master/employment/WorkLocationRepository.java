package com.smartcare.hrms.common.repository.master.employment;

import com.smartcare.hrms.common.entity.master.employment.WorkLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkLocationRepository extends JpaRepository<WorkLocation, Long> {
    
	List<WorkLocation> findByNameContainingIgnoreCase(String name);

	WorkLocation findByCode(String code);

    boolean existsByCode(String code);
    Optional<WorkLocation> findByCodeIgnoreCase(String code);


}
