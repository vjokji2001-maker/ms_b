package com.smartcare.hrms.attendance.dto.application.swipe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class EmployeeSwipeRequestDashboardDTO {

    private String empSwpReqId;
    private String swipeDate;
    private String applicationDate;
    private String dayType;
    private String swipeDirection;
    private String reqInTime;
    private String reqOutTime;
    private String status;
    private String remark;
    private String lastApproverRemark;
}
