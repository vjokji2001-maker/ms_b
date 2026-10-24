package com.smartcare.hrms.common.dto.payroll.compensation.employee;

import lombok.*;

import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCompensationDashboardDTO {
    private String employeeId;
    private String name;
    private String code;
    private String gradeName;
    private BigDecimal grossAmount;
    private BigDecimal standardDeductionAmount;
    private BigDecimal otherDeductionAmount;
    private BigDecimal netAmount;
    private String compensationStatus;
}
