package com.smartcare.hrms.attendance.dto.application;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestDashboardDTO {

    private String requestId;
    private String applicationDate;
    private String applicationType;

    private String fromDate;
    private String toDate;

    private String status;
    private String reason;
    private String remark;
    private String lastApproverRemark;

}

