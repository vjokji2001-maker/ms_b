package com.smartcare.hrms.attendance.dto.attendance;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EmployeeDailyAttendanceSummaryDTO {
    private String empId;
    private String empCode;
    private String empName;
    private List<AttendanceEntryDTO> attendanceList;
}
