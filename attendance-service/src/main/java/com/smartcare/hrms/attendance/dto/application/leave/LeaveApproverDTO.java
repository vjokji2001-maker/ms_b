package com.smartcare.hrms.attendance.dto.application.leave;

import com.smartcare.hrms.attendance.dto.application.swipe.EmployeeSwipeRequestDTO;
import com.smartcare.hrms.common.custom.annotations.DataRequired;
import com.smartcare.hrms.common.utility.PatternConstants;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeaveApproverDTO {
    private Long id;
    private Integer sequence;
    private String approverId;
    private String approverName;
    private String approverCode;
    private String approverStatus;
    private String actionDateTime;
    @DataRequired(
            message = "{approver.remark.required}",
            pattern = PatternConstants.ALPHA_ONLY,
            patternMessage = "{approver.remark..alpha.only}"
    )
    @Size(max = 180, message = "{approver.remark..length}")
    private String approverRemark;
    private LeaveODRequestDTO leaveODRequestDTO;
}
