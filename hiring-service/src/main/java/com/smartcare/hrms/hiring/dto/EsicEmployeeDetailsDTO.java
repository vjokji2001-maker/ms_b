package com.smartcare.hrms.hiring.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EsicEmployeeDetailsDTO {

    private String employeeCode;

    private String employeeName;

    private String esicNumber;

    private String department;

    private BigDecimal wages;

    private BigDecimal employeeShare;

    private BigDecimal employerShare;

    private BigDecimal total;

    private String status;
}