package com.smartcare.hrms.attendance.dto.application.leave;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CancelRequestDashboardDTO {
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

    private String applicationType;

}
