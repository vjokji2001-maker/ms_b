package com.smartcare.hrms.attendance.dto.application.swipe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SwipeRequestApproverDetailsDashboardDTO {

    private Long swpReqApproverId;
    private String name;
    private String code;
    private String swipeDate;
    private String applicationDate;
    private String dayType;
    private String swipeDirection;
    private String shiftCode;
    private String reqInTime;
    private String reqOutTime;
    private String status;
    private String remark;
    private String lastApproverRemark;
    private String empSwipeReqId;
}
