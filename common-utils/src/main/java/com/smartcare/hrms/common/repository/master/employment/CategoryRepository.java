package com.smartcare.hrms.common.repository.master.employment;

import com.smartcare.hrms.common.entity.master.employment.Category;
import com.smartcare.hrms.common.entity.master.employment.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

	List<Category> findByNameContainingIgnoreCase(String name);

	Category findByCode(String code);
    boolean existsByCode(String code);

    Category findByCodeIgnoreCase(String code);

}
