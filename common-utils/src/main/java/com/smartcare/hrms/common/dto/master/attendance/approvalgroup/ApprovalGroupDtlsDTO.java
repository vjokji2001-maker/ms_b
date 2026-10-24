package com.smartcare.hrms.common.dto.master.attendance.approvalgroup;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ApprovalGroupDtlsDTO {
    private Long id;
    private String empId;
    private Integer sequence;
    private String employeeName;
    private String employeeCode;


}
