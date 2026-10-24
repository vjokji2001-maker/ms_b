package com.smartcare.hrms.common.dto.master.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class EmployeeBasicInfoDTO {
    private String name;
    private String employeeCode;
    private String email;
    private String number;
    private String companyName;
    private String branchName;
    private String departmentName;
    private String designationName;
    private String gradeName;
    private String joiningDate;//dd/mm/yyyy(00/00/0000)
}
