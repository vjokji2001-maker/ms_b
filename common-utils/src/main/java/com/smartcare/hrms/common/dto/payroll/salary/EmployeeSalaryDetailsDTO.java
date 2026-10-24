package com.smartcare.hrms.common.dto.payroll.salary;

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
public class EmployeeSalaryDetailsDTO {
    private String status;
    private String payrollId;
    private String yearMonth;
    private String employeeId;
    private BigDecimal grossSalary;
    private BigDecimal totalEarnings;
    private BigDecimal totalDeductions;
    private BigDecimal lossOfPayAmount;
    private BigDecimal netPayableSalary;
    private Double daysInMonth;
    private Double payableDays;
    private Double lossOfPayDays;
    private String employeeName;
    private String employeeCode;
    private String grade;
    private String companyName;
    private String registeredName;
    private String address;
    private String pincode;
    private String imageUrl;
    private String payrollState;
    private String email;
    private String cityName;
    private String stateName;
    private String personalEmail;
    private List<EmployeeSalaryComponentDTO> earnings;
    private List<EmployeeSalaryComponentDTO> deductions;

}
