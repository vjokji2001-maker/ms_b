package com.smartcare.hrms.payroll.mappers.finance;

import com.smartcare.hrms.payroll.dto.finance.EmployeeBankAccountDTO;
import com.smartcare.hrms.payroll.entity.finance.EmployeeBankAccount;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EmployeeBankAccountMapper {

    EmployeeBankAccountDTO toDTO(EmployeeBankAccount entity);

    EmployeeBankAccount toEntity(EmployeeBankAccountDTO dto);

    void updateEntityFromDTO(EmployeeBankAccountDTO dto, @MappingTarget EmployeeBankAccount entity);

}
