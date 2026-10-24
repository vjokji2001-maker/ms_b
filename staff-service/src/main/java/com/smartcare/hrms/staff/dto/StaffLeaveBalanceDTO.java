package com.smartcare.hrms.staff.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffLeaveBalanceDTO {

    private Long leaveNameId;
    private String leaveName;
    private String leaveCode;
    private Double totalCredited;
    private Double totalUtilized;
    private Double remainingBalance;
}
