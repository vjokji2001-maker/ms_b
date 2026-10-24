package com.smartcare.hrms.attendance.dto.application.leave;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LeaveBalanceDTO {
    private String employeeId;
    private Long yearId;

}
