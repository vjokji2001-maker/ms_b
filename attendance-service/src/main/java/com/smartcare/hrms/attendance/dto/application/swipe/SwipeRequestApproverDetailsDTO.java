package com.smartcare.hrms.attendance.dto.application.swipe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SwipeRequestApproverDetailsDTO {

    private Long id;
    private Integer sequence;
    private String approverId;
    private String approverName;
    private String approverCode;
    private String swipeStatus;
    private String actionDateTime;
    private String approverRemark;
    private EmployeeSwipeRequestDTO employeeSwipeRequestDTO;
}
