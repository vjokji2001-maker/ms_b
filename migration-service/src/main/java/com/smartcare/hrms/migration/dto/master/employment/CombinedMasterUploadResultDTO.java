package com.smartcare.hrms.migration.dto.master.employment;

import com.smartcare.hrms.migration.dto.employee.EmployeeUploadResultDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CombinedMasterUploadResultDTO {

    private MasterUploadResultDTO categoryResult;
    private MasterUploadResultDTO gradeResult;
    private MasterUploadResultDTO departmentResult;
    private MasterUploadResultDTO designationResult;
    private MasterUploadResultDTO workLocationResult;
    private SubWorkLocationUploadResultDTO subWorkLocationResult;
    private BranchUploadResultDTO branchResult;
    private CompanyUploadResultDTO companyResult;
    private EmployeeUploadResultDTO employeeResult;
}