package com.smartcare.hrms.attendance.dto.application.leave;

import com.smartcare.hrms.common.custom.annotations.DataRequired;
import com.smartcare.hrms.common.utility.PatternConstants;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CancelLeaveRequestDTO {
    private String requestId;
    private String employeeId;
    @DataRequired(
            message = "{leave.cancel.reason.required}",
            pattern = PatternConstants.ALPHA_ONLY,
            patternMessage = "{leave.cancel.reason.alpha.only}"
    )
    @Size(max = 250, message = "{leave.cancel.reason.length}")
    private String reason;


}
