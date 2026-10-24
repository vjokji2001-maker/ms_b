package com.smartcare.hrms.attendance.dto.application.OD;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LeaveODRequestDashboardDTO {

    private String odRequestId;
    private String applicationDate;
    private String fromDate;
    private String toDate;
    private String fromDateType;
    private String toDateType;
    private String status;
    private String reason;
    private String remark;
    private String lastApproverRemark;
    private String applicationType;



}
