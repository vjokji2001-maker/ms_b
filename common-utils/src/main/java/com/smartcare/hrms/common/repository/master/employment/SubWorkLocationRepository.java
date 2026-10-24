package com.smartcare.hrms.common.repository.master.employment;

import java.util.List;
import java.util.Optional;

import com.smartcare.hrms.common.entity.master.employment.WorkLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartcare.hrms.common.entity.master.employment.SubWorkLocation;

@Repository
public interface SubWorkLocationRepository extends JpaRepository<SubWorkLocation, Long> {
	   
	List<SubWorkLocation> findByNameContainingIgnoreCase(String name);
	
	SubWorkLocation findByCode(String code);

    boolean existsByCode(String code);

    Optional<SubWorkLocation> findByCodeIgnoreCase(String code);
}
