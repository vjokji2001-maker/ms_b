package com.smartcare.hrms.hiring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PfEmployeeSummaryDTO {

    private Integer totalEmployees;

    private BigDecimal employeePF;

    private BigDecimal employerPF;

    private BigDecimal eps;

    private BigDecimal totalContribution;
}