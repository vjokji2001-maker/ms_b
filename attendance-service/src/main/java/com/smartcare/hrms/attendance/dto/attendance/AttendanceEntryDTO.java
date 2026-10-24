package com.smartcare.hrms.attendance.dto.attendance;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttendanceEntryDTO {

    private String date;
    private String inTime;
    private String outTime;
    private String dayType;
    private String dayType1;
    private String status;
}
