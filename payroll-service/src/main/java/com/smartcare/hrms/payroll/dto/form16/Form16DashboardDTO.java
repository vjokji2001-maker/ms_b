package com.smartcare.hrms.payroll.dto.form16;

import lombok.Data;

@Data
public class Form16DashboardDTO {

    private Long totalEmployees;

    private Long form16Generated;

    private Long pending;

    private Long downloaded;

    private String financialYear;
}