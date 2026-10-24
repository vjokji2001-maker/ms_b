package com.smartcare.hrms.attendance.dto.application;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CancelApproverDashboardDTO {
    private Long   recordId;
    private String cancelType;
    private String name;
    private String code;
    private String applicationType;
    private String leaveName;
    private String applicationDate;
    private String fromDate;
    private String fromDateDuration;
    private String toDate;
    private String toDateDuration;
    private String reason;
    private String status;
    private String remark;
    private String lastApproverRemark;
    private String sourceRequestId;
}
