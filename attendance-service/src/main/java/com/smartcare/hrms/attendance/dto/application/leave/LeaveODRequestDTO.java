package com.smartcare.hrms.attendance.dto.application.leave;

import com.smartcare.hrms.common.custom.annotations.DataRequired;
import com.smartcare.hrms.common.utility.PatternConstants;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeaveODRequestDTO {
    private String requestId;
    private LocalDate applicationDate;
    private String applicationDateStr;
    private LocalDate fromDate;
    private String fromDateStr;
    private String applicationType;
    private LocalDate toDate;
    private String toDateStr;
    private String fromDateDuration;
    private String toDateDuration;
    private Long leaveNameId;
    private String leaveName;

    @DataRequired(
            message = "{leave.reason.required}",
            pattern = PatternConstants.ALPHA_ONLY,
            patternMessage = "{leave.reason.alpha.only}"
    )
    @Size(max = 250, message = "{leave.reason.length}")
    private String reason;
    private String employeeId;
    private Long yearId;
    private Double totalCount;
    private String empId;
    private String employeeName;
    private String employeeCode;
    private String status;
    private LocalDate leaveDate;
    private String leaveType;
    private List<LeaveApproverDTO> approverDTOList;
}
