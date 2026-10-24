package com.smartcare.hrms.report.modal.dto.attendance;

import lombok.Data;

@Data
public class AttendanceDTO {

    private String date;
    private String day;
    private String shiftCode;
    private String inTime;
    private String outTime;
    private String workHours;
    private int late;
    private int early;
    private int totalMarks;
    private String status;
    private String dayType;
    private String dayType1;
}
