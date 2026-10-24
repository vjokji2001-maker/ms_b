package com.smartcare.hrms.hiring.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EsicEmployeeSummaryDTO {

    private Integer totalEmployees;

    private BigDecimal employeeShare;

    private BigDecimal employerShare;

    private BigDecimal totalContribution;

    private Integer belowCeiling;

    private Integer aboveCeiling;
}