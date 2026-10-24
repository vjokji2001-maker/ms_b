package com.smartcare.hrms.migration.dto.master.attendance;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttendanceMasterUploadErrorDTO {

    private int rowNumber;
    private String name;
    private String code;
    private String reason;
}
