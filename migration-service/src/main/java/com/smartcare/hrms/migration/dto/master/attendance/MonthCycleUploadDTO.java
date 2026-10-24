package com.smartcare.hrms.migration.dto.master.attendance;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MonthCycleUploadDTO {
    private String name, code, startDate, endDate, gradeCode;
}

