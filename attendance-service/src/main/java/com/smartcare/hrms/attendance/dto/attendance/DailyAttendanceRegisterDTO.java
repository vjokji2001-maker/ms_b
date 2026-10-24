package com.smartcare.hrms.attendance.dto.attendance;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DailyAttendanceRegisterDTO  {


    private String empId;
    private String empCode;
    private String empName;
    private String date;

    private String inTime;
    private String outTime;
    private String dayType;
    private String dayType1;
    private String status;
}
