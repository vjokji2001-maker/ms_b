package com.smartcare.hrms.hiring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PfEmployeeDetailsDTO {

    private String employeeCode;

    private String employeeName;

    private String designation;

    private String uan;

    private String pfNumber;

    private String department;

    private BigDecimal basicDA;

    private BigDecimal employeePF;

    private BigDecimal employerPF;

    private BigDecimal eps;

    private BigDecimal total;

    private String status;
}