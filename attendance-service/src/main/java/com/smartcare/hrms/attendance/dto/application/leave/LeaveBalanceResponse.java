package com.smartcare.hrms.attendance.dto.application.leave;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LeaveBalanceResponse {
    private Long leaveNameId;
    private String name;
    private Double  accrued;
    private Double  approved;
    private Double  requested;
    private Double  balance;
}
