package com.smartcare.hrms.attendance.dto.attendance;

import com.smartcare.hrms.common.enumeration.AttendanceStatusEnum;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MyAttendanceDTO {

    private String employeeId;
    private String date;
    private String day;
    private String shiftCode;
    private LocalTime shiftInTime;
    private LocalTime shiftOutTime;
    private LocalTime empInTime;
    private LocalTime empOutTime;
    private String dayType;
    private String dayType1;
    private double portion;
    private double lateInMark;
    private double earlyOutMark;
    private double totalMark;
    private String totalHours;
    private String status;
    private String extraHours;
}
