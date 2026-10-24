package com.smartcare.hrms.attendance.dto.application.OD;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveODApproverDashboardDTO {

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
    private String odRequestId;




}
