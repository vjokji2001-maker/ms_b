package com.smartcare.hrms.common.repository.payroll.compensation.template;

import com.smartcare.hrms.common.entity.payroll.compensation.template.CompensationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompensationTemplateRepository extends JpaRepository<CompensationTemplate,Long> {

    boolean existsByCode(String code);

    boolean existsByName(String name);

    boolean existsByCodeAndIdNot(String code, Long id);

    boolean existsByNameAndIdNot(String code, Long id);
}
