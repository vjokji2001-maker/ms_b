package com.smartcare.hrms.attendance.dto.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttendanceSalaryLinkDTO {

    private String salaryLinkId;

    private String employeeId;

    private String monthYear;

    private String attendanceStatus;

    private String payrollStatus;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Boolean isNightShiftExist;
}
