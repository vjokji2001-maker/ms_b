package com.smartcare.hrms.attendance.dto.attendance;

import com.smartcare.hrms.attendance.dto.application.leave.LeaveODRequestDTO;
import com.smartcare.hrms.attendance.dto.application.swipe.EmployeeDailyPunchLogDTO;
import com.smartcare.hrms.attendance.dto.application.swipe.EmployeeSwipeRequestDTO;
import com.smartcare.hrms.attendance.dto.shift.EmployeeShiftDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttendanceDetailsDTO {

    private String empName;
    private String empCode;
    private List<EmployeeShiftDTO> shifts;
    private List<LeaveODRequestDTO> leaves;
    private List<LeaveODRequestDTO> ods;
    private List<EmployeeSwipeRequestDTO> swipes;
    private List<EmployeeDailyPunchLogDTO> punchLogs;
}
