package com.smartcare.hrms.hiring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PfChallanListDTO {

    private String challanNumber;

    private String month;

    private String financialYear;

    private Integer employees;

    private BigDecimal employeePF;

    private BigDecimal employerPF;

    private BigDecimal eps;

    private BigDecimal totalAmount;

    private String status;

    private LocalDate generatedOn;
}