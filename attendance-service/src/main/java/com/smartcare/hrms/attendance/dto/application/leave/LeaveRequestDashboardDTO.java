package com.smartcare.hrms.attendance.dto.application.leave;

import com.smartcare.hrms.common.custom.annotations.DataRequired;
import com.smartcare.hrms.common.utility.PatternConstants;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LeaveRequestDashboardDTO {

    private String requestId;

    private String applicationDate;

    private String leaveName;

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

    private String lastApproverRemark;

    private String applicationType;



}
