package com.smartcare.hrms.migration.dto.master.attendance;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApprovalGroupDtlsUploadDTO {

    private String approvalGroupCode, empCode, sequence;
}
