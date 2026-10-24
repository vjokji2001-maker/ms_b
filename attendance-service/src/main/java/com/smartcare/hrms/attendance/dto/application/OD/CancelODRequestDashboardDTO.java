package com.smartcare.hrms.attendance.dto.application.OD;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CancelODRequestDashboardDTO {
    private String requestId;
    private String applicationDate;
    private String name;
    private String code;
    private String fromDate;
    private String fromDateDuration;
    private String toDate;
    private String toDateDuration;
    private Double  totalDays;
    private  String  reason;
    private  String remark;
    private String status;
}
