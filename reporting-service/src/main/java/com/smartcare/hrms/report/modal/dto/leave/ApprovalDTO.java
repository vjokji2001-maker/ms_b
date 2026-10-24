package com.smartcare.hrms.report.modal.dto.leave;

import lombok.Data;

@Data
public class ApprovalDTO {
    private Integer level;
    private String approverName;
    private String status;
    private String remark;
    private String actionDateTime;
}
