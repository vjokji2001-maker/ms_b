package com.smartcare.hrms.staff.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffLeaveRequestItemDTO {

    private String requestId;
    private Long leaveNameId;
    private String leaveName;
    private String fromDate;
    private String fromDateType;
    private String toDate;
    private String toDateType;
    private Double totalDays;
    private String status;
    private String reason;
    private String approverRemark;
    private String applicationDate;
}
