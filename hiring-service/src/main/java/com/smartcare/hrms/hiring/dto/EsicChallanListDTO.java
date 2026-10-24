package com.smartcare.hrms.hiring.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EsicChallanListDTO {

    private String challanNumber;

    private String month;

    private Integer employees;

    private BigDecimal employeeShare;

    private BigDecimal employerShare;

    private BigDecimal totalAmount;

    private String status;

    private LocalDate generatedOn;
}