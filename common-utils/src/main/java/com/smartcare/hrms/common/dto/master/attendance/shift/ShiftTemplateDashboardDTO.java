package com.smartcare.hrms.common.dto.master.attendance.shift;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ShiftTemplateDashboardDTO {
    private Long id;
    private String name;
    private String code;
    private String inTime;
    private String outTime;
    private String totalWorkingHours; // e.g., "09:00"
    private boolean halfDayApplicable;
    private boolean nightShift;
}
