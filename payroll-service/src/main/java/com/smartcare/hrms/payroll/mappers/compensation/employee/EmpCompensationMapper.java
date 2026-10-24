package com.smartcare.hrms.payroll.mappers.compensation.employee;

import com.smartcare.hrms.common.dto.payroll.compensation.employee.EmpCompensationDTO;
import com.smartcare.hrms.common.dto.payroll.compensation.employee.EmpCompensationDtlsDTO;
import com.smartcare.hrms.common.entity.payroll.compensation.employee.EmpCompensationHdr;
import com.smartcare.hrms.common.entity.payroll.compensation.employee.EmpCompensationDtls;
import com.smartcare.hrms.common.entity.payroll.compensation.employee.Formula;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmpCompensationMapper {

    // --- Header Mappings ---
    @Mapping(source = "employeeId", target = "empId")
    @Mapping(source = "compensationTemplateId", target = "compTemplateId")
    @Mapping(source = "grossAmount", target = "grossAmount")
    @Mapping(source = "otherDeductionAmount", target = "otherDeductionAmount")
    @Mapping(source = "standardDeductionAmount", target = "standardDeductionAmount")
    @Mapping(source = "netAmount", target = "netAmount")
    EmpCompensationHdr toEntity(EmpCompensationDTO dto);

    @InheritConfiguration
    @Mapping(target = "id", ignore = true) // Protect PK during updates
    @Mapping(target = "compensationDetails", ignore = true) // Handle child collection separately
    void updateEntityFromDto(EmpCompensationDTO dto, @MappingTarget EmpCompensationHdr entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "empCompensationHdr", ignore = true)
    @Mapping(source = "componentId", target = "componentMaster.id")
    @Mapping(source = "formulaDTO", target = "formula") // Changed from formulaJson to formulaDTO
    void updateDetailEntityFromDto(EmpCompensationDtlsDTO dto, @MappingTarget EmpCompensationDtls entity);

    @Mapping(source = "empId", target = "employeeId")
    @Mapping(source = "compTemplateId", target = "compensationTemplateId")
    EmpCompensationDTO toDto(EmpCompensationHdr entity);

    // --- Detail Mappings ---
    @Mapping(source = "componentId", target = "componentMaster.id")
    @Mapping(source = "formulaDTO", target = "formula")
    @Mapping(source = "componentType", target = "componentType")
    @Mapping(source = "sequence", target = "sequence")
    EmpCompensationDtls toDetailEntity(EmpCompensationDtlsDTO dto);

    @Mapping(source = "componentMaster.id", target = "componentId")
    @Mapping(source = "componentMaster.name", target = "componentName")
    @Mapping(source = "componentMaster.code", target = "code")
    @Mapping(source = "formula", target = "formulaDTO")
    @Mapping(source = "componentType", target = "componentType")
    @Mapping(source = "sequence", target = "sequence")
    EmpCompensationDtlsDTO toDetailDto(EmpCompensationDtls entity);

    // --- Formula Mappings (JSON Column) ---
    Formula toFormula(EmpCompensationDtlsDTO.FormulaDTO dto);

    EmpCompensationDtlsDTO.FormulaDTO toFormulaDto(Formula entity);

    /**
     * This method ensures the Parent-Child link is established.
     * Required for JPA to save the foreign key 'emp_cmpnstn_id'.
     */
    @AfterMapping
    default void linkDetails(@MappingTarget EmpCompensationHdr hdr) {
        if (hdr.getCompensationDetails() != null) {
            hdr.getCompensationDetails().forEach(detail -> detail.setEmpCompensationHdr(hdr));
        }
    }
}

