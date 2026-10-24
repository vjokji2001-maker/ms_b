package com.smartcare.hrms.employee.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserMDTO {

    private String initial;
    private String firstName;
    private String lastName;
    private String clinicName;
    private Integer userType;
    private String mobile;
    private String email;
    private String country;
    private String state;
    private String city;
    private String password;
    private String userId;
    private String discription;
    private Integer branchId;
    private String departmentCode;
    private Integer doctor;
    private Boolean isGlobalAccess;
    private Boolean isActiveInactive;
    private Integer sectionId;
    private Integer clinicId;
    private Boolean doNotAddInPayroll;
    private String empId;
    private String department;

}
