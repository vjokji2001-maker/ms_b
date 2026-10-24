package com.smartcare.hrms.attendance.dto.application.OD;

import com.smartcare.hrms.attendance.dto.application.leave.LeaveODRequestDTO;
import com.smartcare.hrms.common.custom.annotations.DataRequired;
import com.smartcare.hrms.common.utility.PatternConstants;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LeaveODRequestApproverDetailsDTO {

    private Long id;
    private Integer sequence;
    private String approverId;
    private String approverName;
    private String approverCode;
    private String odStatus;
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
