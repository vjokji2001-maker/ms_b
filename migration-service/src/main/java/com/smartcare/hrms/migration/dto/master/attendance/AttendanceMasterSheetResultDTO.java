package com.smartcare.hrms.migration.dto.master.attendance;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AttendanceMasterSheetResultDTO {

    private String sheetName;
    private int totalRows;
    private int successCount;
    private int failureCount;
    private List<AttendanceMasterUploadErrorDTO> errors;
}
