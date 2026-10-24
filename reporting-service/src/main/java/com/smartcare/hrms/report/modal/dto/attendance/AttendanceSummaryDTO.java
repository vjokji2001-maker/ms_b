package com.smartcare.hrms.report.modal.dto.attendance;

import lombok.Data;

@Data
public class AttendanceSummaryDTO {

    private int totalDays;
    private int present;
    private int absent;
    private int weeklyOff;
    private int lateMarks;
    private int earlyMarks;
}
