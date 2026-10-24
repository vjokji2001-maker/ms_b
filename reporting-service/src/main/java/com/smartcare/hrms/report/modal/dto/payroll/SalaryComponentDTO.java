package com.smartcare.hrms.report.modal.dto.payroll;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SalaryComponentDTO {

    private Integer seq;
    private String  componentName;
    private String  componentCode;
    private String  componentType;   // EARNINGS / DEDUCTIONS
    private String  calculationType; // FIXED / FORMULA
    private BigDecimal amount;
}
