package com.smartcare.hrms.common.dto.payroll.salary;

import lombok.Data;

import java.util.List;

@Data
public class BatchOperationDTO {
    private List<String> payrollIds;
    private String attendanceSalaryLinkId;
    private Double payableDays;
    private Double lossOfPayDays;
}
