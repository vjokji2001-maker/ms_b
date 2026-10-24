package com.smartcare.hrms.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeLeaveDTO {


    private Long employeeLeaveParentId;
    private Long futureDays;
    private Long pastDays;
    private Boolean halfDayAllowed;
    private Double monthlyCount;
    private Double quaterlyCount;
    private Double yearlyCount;
    private String leaveName;
    private Long leaveNameId;
}
