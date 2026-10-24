package com.smartcare.hrms.staff.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffLeaveApplyDTO {

    private String empId;
    private Long leaveNameId;
    private String fromDate; // dd/MM/yyyy
    private String fromDateType; // FULL_DAY, FIRST_HALF, SECOND_HALF
    private String toDate; // dd/MM/yyyy
    private String toDateType;
    private Double totalDays;
    private String reason;
}
