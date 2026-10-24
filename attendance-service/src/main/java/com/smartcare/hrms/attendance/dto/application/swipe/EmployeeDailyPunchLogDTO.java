package com.smartcare.hrms.attendance.dto.application.swipe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class EmployeeDailyPunchLogDTO {

    private Long id;
    private String empCode;
    private String punchDate;
    private String punchDateTime;
    private String punchType;
    private String punchDirection;
}
