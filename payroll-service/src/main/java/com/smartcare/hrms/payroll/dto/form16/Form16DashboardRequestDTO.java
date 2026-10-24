package com.smartcare.hrms.payroll.dto.form16;

import lombok.Data;

@Data
public class Form16DashboardRequestDTO {

    private String financialYear;

    private String employeeName;

    private String employeeCode;

    private String department;

    private Integer page = 0;

    private Integer size = 10;
}