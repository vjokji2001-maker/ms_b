package com.smartcare.hrms.attendance.dto.application.leave;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CancelRequestApproverDashboardDTO {
    private Long approverId;
    private String name;
    private String code;
    private String applicationType;
    private String applicationDate;
    private String fromDate;
    private String fromDateDuration;
    private String toDate;
    private String toDateDuration;
    private String status;
    private String reason;
    private String remark;
    private String lastApproverRemark;
    private String requestId;
    private String leaveName;
}
