package com.smartcare.hrms.staff.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffPayrollSlipDTO {

    private Long id;
    private String month;
    private String year;
    private String monthYearDisplay;
    private Double grossSalary;
    private Double totalDeductions;
    private Double netSalary;
    private Double payableDays;
    private Double presentDays;
    private Double leaveDays;
    private Double lopDays;
    private List<StaffSalaryComponentDTO> components;
}
