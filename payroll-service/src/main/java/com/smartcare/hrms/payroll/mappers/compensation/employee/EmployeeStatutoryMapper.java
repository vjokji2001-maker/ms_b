package com.smartcare.hrms.payroll.mappers.compensation.employee;

import com.smartcare.hrms.payroll.dto.statutory.EmployeeStatutoryDTO;
import com.smartcare.hrms.common.entity.payroll.statutory.EmployeeStatutoryInfo;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface EmployeeStatutoryMapper {

    @Mapping(target = "gender", source = "employee.gender")
    @Mapping(target = "empName", source = "employee.formalName")
    @Mapping(target = "ptApplicable", ignore = true)
    EmployeeStatutoryDTO toDTO(EmployeeStatutoryInfo entity);


    @Mapping(target = "employee",  ignore = true)
    @Mapping(target = "paymentMethod", source = "paymentMethod")
    @Mapping(target = "payrollState", source = "payrollState")
    EmployeeStatutoryInfo toEmployeeStatutoryEntity(EmployeeStatutoryDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEmployeeStatutoryFromDto(EmployeeStatutoryDTO dto , @MappingTarget EmployeeStatutoryInfo entity);


}
