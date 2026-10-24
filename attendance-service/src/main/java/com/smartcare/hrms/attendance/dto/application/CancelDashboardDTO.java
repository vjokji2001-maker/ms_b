package com.smartcare.hrms.attendance.dto.application;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CancelDashboardDTO {
    private String requestId;
    private String applicationDate;
    private String applicationType;

    private String leaveName;

    private String fromDate;
    private String fromDateDuration;

    private String toDate;
    private String toDateDuration;

    private Double totalDays;

    private String status;
    private String reason;
    private String remark;
    private String lastApproverRemark;
}
