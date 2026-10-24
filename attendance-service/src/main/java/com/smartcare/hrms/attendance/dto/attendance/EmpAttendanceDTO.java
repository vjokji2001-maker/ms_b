package com.smartcare.hrms.attendance.dto.attendance;

import com.smartcare.hrms.common.enumeration.AttendanceStatusEnum;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class EmpAttendanceDTO {
    private String employeeId;
    private Long empShiftId;
    private LocalDate attendanceDate;

    private LocalTime inTime;
    private LocalTime outTime;

    private double lateInMark;
    private double earlyOutMark;

    private double portion;
    private String dayType;
    private String dayType1;
    private Double nightShiftAmount;
    private Double nightShiftDays;

    private AttendanceStatusEnum status;
}
