package com.smartcare.hrms.common.dto.payroll.compensation.employee;

import com.smartcare.hrms.common.custom.annotations.DataRequired;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmpCompensationDTO {
    private String id;

    @DataRequired(message = "{employee.required}")
    private String employeeId;

    @NotNull(message = "{compensation.template.required}")
    private Long compensationTemplateId;

    @Positive(message = "{gross.amount.required}")
    private BigDecimal grossAmount;

    @NotNull(message = "{invalid.deduction.amount}")
    private BigDecimal otherDeductionAmount;

    @NotNull(message = "{invalid.std.deduction.amount}")
    private BigDecimal standardDeductionAmount;

    @NotNull(message = "{invalid.net.amount}")
    private BigDecimal netAmount;

    @NotEmpty(message = "{compensation.componentList.empty}")
    private List<@Valid EmpCompensationDtlsDTO> compensationDetails;
}
