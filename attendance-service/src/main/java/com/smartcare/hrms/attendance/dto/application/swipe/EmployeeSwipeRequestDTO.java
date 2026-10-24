package com.smartcare.hrms.attendance.dto.application.swipe;

import com.smartcare.hrms.attendance.dto.shift.EmployeeShiftDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class EmployeeSwipeRequestDTO {

    private UUID id;
    private String empId;
    private String empName;
    private String empCode;
    private String swipeDate;
    private String applicationDate;
    private String dayType;
    private String swipeDirection;
    private EmployeeShiftDTO employeeShiftDTO;
    private String reqInTime;
    private String reqOutTime;
    private String swipeStatus;
    private String remark;
    private String createdDateTime;
    private String modifiedDateTime;
    private String createdBy;
    private String modifiedBy;
    private List<SwipeRequestApproverDetailsDTO> approverDetailsDTOList;
}
