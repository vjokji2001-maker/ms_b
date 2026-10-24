package com.smartcare.hrms.migration.dto.master.attendance;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeaveTemplateDtlsUploadDTO {

    private String leaveTemplateCode, leaveNameCode;
    private String yearlyCount, quarterlyCount, monthlyCount;
    private String pastDays, futureDays, halfDayAllowed;
}
