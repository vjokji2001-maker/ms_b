package com.smartcare.hrms.attendance.dto.application;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApproverDashboardDTO {

    private Long   recordId;
    private String requestType;
    private String name;
    private String code;
    private String applicationDate;
    private String status;
    private String remark;
    private String lastApproverRemark;
    private String sourceRequestId;
    private String leaveName;
    private String fromDate;
    private String fromDateDuration;
    private String toDate;
    private String toDateDuration;
    private String reason;
    private String swipeDate;
    private String dayType;
    private String swipeDirection;
    private String shiftCode;
    private String reqInTime;
    private String reqOutTime;
}
