package com.smartcare.hrms.report.modal.dto.payroll;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class EmployeeSalaryReportDTO {
    private String  employeeCode;
    private String  employeeName;
    private String  monthYear;

    private Double     payableDays;
    private Double     lopDays;
    private BigDecimal totalEarnings;
    private BigDecimal totalStdDeductions;
    private BigDecimal totalOtherDeductions;
    private BigDecimal totalPayableEarnings;
    private BigDecimal totalPayableStdDeductions;
    private BigDecimal totalPayableOtherDeductions;
    private BigDecimal netPayable;

    private BigDecimal grossAmount;
    private BigDecimal stdDeductionAmount;
    private BigDecimal otherDeductionAmount;
    private BigDecimal netAmount;

    private List<SalaryComponentDTO> earnings;
    private List<SalaryComponentDTO> deductions;
}
