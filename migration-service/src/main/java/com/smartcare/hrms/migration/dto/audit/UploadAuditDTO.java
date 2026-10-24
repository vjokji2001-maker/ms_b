package com.smartcare.hrms.migration.dto.audit;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UploadAuditDTO {

    private String uploadType;
    private String fileName;
    private String uploadedByUserId;
    private String uploadedAt;
    private String status;
}
