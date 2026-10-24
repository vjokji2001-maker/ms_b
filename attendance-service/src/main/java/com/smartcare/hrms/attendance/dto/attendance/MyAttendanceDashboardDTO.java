package com.smartcare.hrms.attendance.dto.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MyAttendanceDashboardDTO {
    private String shiftCode;
    private String shiftName;
    private LocalTime shiftInTime;
    private LocalTime shiftOutTime;
    private LocalDate shiftDate;
    private LocalTime empInTime;
    private LocalTime empOutTime;
    private String leaveName;
    private String appType;
    private LocalDate leaveDate;
    private String leaveReason;
    private LocalDate swipeDate;
    private LocalTime swipeRqstInTime;
    private LocalTime swipeRqstOutTime;
    private String swipeRemark;
    private LocalDateTime punchTime;
    private String punchType;
    private String direction;



}
