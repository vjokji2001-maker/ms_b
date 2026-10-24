package com.smartcare.hrms.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeLeaveDtlsDTO {
    private Long id;
    private Long leaveNameId;
    private Double yearlyCount;
    private Double quaterlyCount;
    private Double monthlyCount;
    private Double pastDays;
    private Double futureDays;
    private boolean halfDayAllowed;
}
