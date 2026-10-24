package com.smartcare.hrms.payroll.dto.form16;

import lombok.Data;

@Data
public class Form16DTO {

    private String employeeId;
    private String employeeCode;
    private String employeeName;
    private String designation;
    private String department;
    private String pan;
    private String financialYear;
    private String form16Number;

    private Double totalEarnings;
    private Double totalStandardDeductions;
    private Double totalOtherDeductions;
    private Double totalDeductions;
    private Double netPayable;
    private Double professionalTax;
    private Double incomeTax;
    private String payrollStatus;
    private Double basicPay;
    private Double hra;
    private Double specialAllowance;

    private Boolean form16Generated;
    private Boolean form16Downloaded;
}