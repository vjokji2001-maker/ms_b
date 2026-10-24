package com.smartcare.hrms.migration.dto.employee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeUploadResultDTO {

    private int totalRows;
    private int successCount;
    private int failureCount;
    private List<EmployeeUploadErrorDTO> errors;
}
