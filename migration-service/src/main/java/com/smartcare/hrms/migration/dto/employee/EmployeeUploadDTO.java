package com.smartcare.hrms.migration.dto.employee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeUploadDTO {

    private String salutation;
    private String firstName;
    private String middleName;
    private String lastName;
    private String formalName;
    private String gender;
    private String dob;
    private String maritalStatus;

    // Contact Details
    private String contactCountryCode;
    private String contactNumber;

    private String aadhaarNumber;

    // Additional Details
    private String nationality;
    private String birthCountryName;
    private String stateName;
    private String cityName;

    private String companyCode;
    private String branchCode;
    private String designationCode;
    private String departmentCode;
    private String workLocationCode;
    private String subWorkLocationCode;
    private String gradeCode;
    private String categoryCode;
    private String status;
    private String empCode;
    private String biometricCode;

    // Employment Dates
    private String joiningDate;
    private String contractStartDate;
}
