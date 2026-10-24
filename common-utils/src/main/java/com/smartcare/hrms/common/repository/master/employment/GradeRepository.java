package com.smartcare.hrms.common.repository.master.employment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartcare.hrms.common.entity.master.employment.Grade;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
	
    List<Grade> findByNameContainingIgnoreCase(String name);
	
	Grade findByCode(String code);

    boolean existsByCode(String code);

    Grade findByCodeIgnoreCase(String code);

}
