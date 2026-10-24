package com.smartcare.hrms.payroll.mappers.compensation.salary;

import com.smartcare.hrms.common.enumeration.ComponentType;
import com.smartcare.hrms.common.dto.payroll.compensation.employee.EmpCompensationDtlsDTO;
import com.smartcare.hrms.common.dto.payroll.salary.EmployeeSalaryComponentDTO;
import com.smartcare.hrms.common.dto.payroll.salary.EmployeeSalaryDetailsDTO;
import com.smartcare.hrms.common.entity.payroll.compensation.employee.Formula;
import com.smartcare.hrms.common.entity.payroll.salary.EmpMonthlyPayrollSummary;
import com.smartcare.hrms.common.entity.payroll.salary.EmpSalaryComponentDetails;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(
        componentModel = "spring",
        imports = { com.smartcare.hrms.common.enumeration.ComponentType.class }
)
public interface EmployeeSalaryConverter {

    @Mapping(target = "displayOrder", source = "sequence")
    @Mapping(target = "componentCode", source = "code")
    @Mapping(target = "monthlyGrossAmount", source = "amount")
    @Mapping(target = "formula", source = "formulaDTO")
    @Mapping(target = "amountSource", ignore = true)
    @Mapping(target = "payableAmount", ignore = true)
    EmployeeSalaryComponentDTO toEmployeeSalaryComponentDTO(EmpCompensationDtlsDTO dto);

    @Mapping(target = "payrollId", expression = "java(entity.getId() != null ? entity.getId().toString() : null)")
    @Mapping(target = "employeeId", source = "attendanceSalaryLink.employeeId")
    @Mapping(target = "grossSalary", source = "totalEarnings")
    @Mapping(target = "totalEarnings", source = "totalPayableEarnings")
    @Mapping(target = "totalDeductions",
            expression = "java(entity.getTotalPayableStandardDeductions() == null && entity.getTotalPayableOtherDeductions() == null ? null : " +
                    "(entity.getTotalPayableStandardDeductions() == null ? java.math.BigDecimal.ZERO : entity.getTotalPayableStandardDeductions())" +
                    ".add(entity.getTotalPayableOtherDeductions() == null ? java.math.BigDecimal.ZERO : entity.getTotalPayableOtherDeductions()))")
    @Mapping(target = "netPayableSalary", source = "netPayable")
    @Mapping(target = "yearMonth", source = "attendanceSalaryLink.monthYear")
    @Mapping(target = "payableDays", source = "payableDays")
    @Mapping(target = "lossOfPayDays", source = "lossOfPayDays")
    @Mapping(target = "earnings", expression = "java(mapEarnings(entity.getSalaryComponents()))")
    @Mapping(target = "deductions", expression = "java(mapDeductions(entity.getSalaryComponents()))")
    EmployeeSalaryDetailsDTO toDTO(EmpMonthlyPayrollSummary entity);


    // mapping annotation is not required here as the field names are the same, but added for clarity
    @Mapping(target = "viewFormula", source = "viewFormula")
    @Mapping(target = "expression", source = "expression")
    @Mapping(target = "vars", source = "vars")
    Formula map(EmpCompensationDtlsDTO.FormulaDTO formulaDTO);

    // =========================
    // DTO → ENTITY (NEW)
    // =========================
    @Mapping(target = "totalPayableEarnings", source = "totalEarnings")
    @Mapping(target = "netPayable", source = "netPayableSalary")
    @Mapping(target = "salaryComponents", expression = "java(mapToEntityComponents(dto))")

    @Mapping(target = "totalPayableStandardDeductions",
            expression = "java(sumByComponentType(dto, ComponentType.STATUTORY_DEDUCTIONS))")

    @Mapping(target = "totalPayableOtherDeductions",
            expression = "java(sumByComponentType(dto, ComponentType.DEDUCTIONS))")
    @Mapping(target = "payableDays",ignore = true)
    @Mapping(target = "lossOfPayDays",ignore = true)
    @Mapping(target = "totalEarnings", ignore = true)
    @Mapping(target = "totalStandardDeductions", ignore = true)
    @Mapping(target = "totalOtherDeductions", ignore = true)
    @Mapping(target = "empCompensationHdr",ignore = true)
    EmpMonthlyPayrollSummary toEntity(EmployeeSalaryDetailsDTO dto);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "attendanceSalaryLink", ignore = true)
    @Mapping(target = "payableDays", ignore = true)
    @Mapping(target = "lossOfPayDays", ignore = true)
    @Mapping(target = "totalOtherDeductions", ignore = true)
    @Mapping(target = "totalStandardDeductions", ignore = true)
    @Mapping(target = "totalEarnings", ignore = true)
    @Mapping(target = "totalPayableEarnings", source = "totalEarnings")
    @Mapping(target = "netPayable", source = "netPayableSalary")
    @Mapping(target = "salaryComponents", expression = "java(mapToEntityComponents(dto))")
    @Mapping(target = "totalPayableStandardDeductions",
            expression = "java(sumByComponentType(dto, ComponentType.STATUTORY_DEDUCTIONS))")

    @Mapping(target = "totalPayableOtherDeductions",
            expression = "java(sumByComponentType(dto, ComponentType.DEDUCTIONS))")
    void updateEntityFromDto(EmployeeSalaryDetailsDTO dto,
                             @MappingTarget EmpMonthlyPayrollSummary entity);

    default List<EmployeeSalaryComponentDTO> mapEarnings(List<EmpSalaryComponentDetails> components) {
        if (components == null) {
            return List.of();
        }

        return components.stream()
                .filter(c -> c.getComponentType() == ComponentType.EARNINGS)
                .map(this::toComponentDTO)
                .toList();
    }

    default BigDecimal mapTotalDeductions(List<EmpSalaryComponentDetails> components) {
        if (components == null) {
            return BigDecimal.ZERO;
        }

        return components.stream()
                .filter(c -> c.getComponentType() == ComponentType.DEDUCTIONS || c.getComponentType() == ComponentType.STATUTORY_DEDUCTIONS)
                .map(EmpSalaryComponentDetails::getPayableAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    default List<EmployeeSalaryComponentDTO> mapDeductions(List<EmpSalaryComponentDetails> components) {
        if (components == null) {
            return List.of();
        }

        return components.stream()
                .filter(c -> c.getComponentType() == ComponentType.DEDUCTIONS || c.getComponentType() == ComponentType.STATUTORY_DEDUCTIONS)
                .map(this::toComponentDTO)
                .collect(Collectors.toList());
    }


    default EmployeeSalaryComponentDTO toComponentDTO(EmpSalaryComponentDetails component) {
        if (component == null) {
            return null;
        }

        return EmployeeSalaryComponentDTO.builder()
                .displayOrder(component.getDisplayOrder())
                .componentId(component.getComponentId())
                .componentName(component.getComponentName())
                .componentCode(component.getComponentCode())
                .componentType(component.getComponentType())
                .calculationType(component.getCalculationType())
                .amountSource(component.getAmountSource())
                .formula(component.getFormula())
                .payableAmount(component.getPayableAmount())
                .monthlyGrossAmount(component.getMonthlyGrossAmount())
                .build();
    }

    // =========================
    // DTO → ENTITY COMPONENT
    // =========================
    default EmpSalaryComponentDetails toEntityComponent(EmployeeSalaryComponentDTO dto) {
        if (dto == null) return null;

        return EmpSalaryComponentDetails.builder()
                .displayOrder(dto.getDisplayOrder())
                .componentId(dto.getComponentId())
                .componentName(dto.getComponentName())
                .componentCode(dto.getComponentCode())
                .componentType(dto.getComponentType())
                .calculationType(dto.getCalculationType())
                .amountSource(dto.getAmountSource())
                .formula(dto.getFormula())
                .payableAmount(dto.getPayableAmount())
                .monthlyGrossAmount(dto.getMonthlyGrossAmount())
                .build();
    }

    // =========================
    // MERGE EARNINGS + DEDUCTIONS
    // =========================
    default List<EmpSalaryComponentDetails> mapToEntityComponents(EmployeeSalaryDetailsDTO dto) {
        if (dto == null) return List.of();

        List<EmpSalaryComponentDetails> earnings =
                dto.getEarnings() == null ? List.of() :
                        dto.getEarnings().stream()
                                .map(this::toEntityComponent)
                                .toList();

        List<EmpSalaryComponentDetails> deductions =
                dto.getDeductions() == null ? List.of() :
                        dto.getDeductions().stream()
                                .map(this::toEntityComponent)
                                .toList();

        return Stream.concat(earnings.stream(), deductions.stream())
                .collect(Collectors.toList());
    }

    default BigDecimal sumByComponentType(EmployeeSalaryDetailsDTO dto, ComponentType type) {
        if (dto == null || dto.getDeductions() == null) {
            return BigDecimal.ZERO;
        }

        return dto.getDeductions().stream()
                .filter(c -> c.getComponentType() == type)
                .map(EmployeeSalaryComponentDTO::getPayableAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
