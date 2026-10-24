package com.smartcare.hrms.common.repository.payroll.compensation.component;

import com.smartcare.hrms.common.entity.payroll.compensation.component.ComponentMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComponentMasterRepository extends JpaRepository<ComponentMaster,Long> {

    ComponentMaster findByCode(String code);

    List<ComponentMaster> findByIdIn(List<Long> ids);

    boolean existsByCode(String code);

    boolean existsByName(String name);

    boolean existsByCodeAndIdNot(String code, Long id);

    boolean existsByNameAndIdNot(String name, Long id);
}
