package com.smartcare.hrms.migration.dto.master.attendance;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShiftTemplateUploadDTO {

    private String name, code;
    private String nightShift, halfDay, hourBased;
    private String inTime, outTime, totalWorkingHours;
    private String firstHalfOutTime, secondHalfInTime, totalHalfDayHours;
    private String bufferTime, cutOffTime;
    private String halfRate, fullRate;
}
