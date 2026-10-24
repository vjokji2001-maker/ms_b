package com.smartcare.hrms.staff.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffProfileDTO {

    private String empId;
    private String empCode;
    private String formalName;
    private String firstName;
    private String lastName;
    private String formalEmail;
    private String personalEmail;
    private String contactNumber;
    private String gender;
    private String dob;
    private String joiningDate;
    private String bloodGroup;

    // Organization details
    private String companyName;
    private String branchName;
    private String departmentName;
    private String designationName;
    private String workLocationName;
    private String gradeName;
    private String categoryName;

    // Office coordinates (if configured)
    private Double officeLatitude;
    private Double officeLongitude;
    private Double geofenceRadiusMeters;

    private String status;
}
