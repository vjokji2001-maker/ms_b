package com.smartcare.hrms.staff.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffTodayAttendanceDTO {

    private String date;
    private String status; // PRESENT, ABSENT, HALF_DAY, WEEK_OFF, HOLIDAY, ON_LEAVE
    private String inTime;
    private String outTime;
    private String totalWorkingHours;
    private String shiftName;
    private String shiftInTime;
    private String shiftOutTime;
    private Boolean isPunchedIn;
    private Boolean isPunchedOut;
    private Boolean isLate;
}
