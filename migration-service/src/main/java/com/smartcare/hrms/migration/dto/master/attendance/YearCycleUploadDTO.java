package com.smartcare.hrms.migration.dto.master.attendance;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class YearCycleUploadDTO {
    private String name, code, fromMonthYear, toMonthYear;
}
