package com.smartcare.hrms.migration.dto.employee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeUploadErrorDTO {
    private int rowNumber;
    private String empCode;
    private String firstName;
    private String companyName;
    private String branchName;
    private String designationName;
    private String departmentName;
    private String workLocationName;
    private String subWorkLocationName;
    private String gradeName;
    private String categoryName;
    private String reason;
}
