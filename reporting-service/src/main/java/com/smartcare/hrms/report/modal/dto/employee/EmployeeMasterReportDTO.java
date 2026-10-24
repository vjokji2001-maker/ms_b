package com.smartcare.hrms.report.modal.dto.employee;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeMasterReportDTO {
    private String employeeId;
    private String employeeCode;
    private String employeeName;

    private String gender;
    private Date dob;

    private String contactNumber;
    private String alternateNumber;

    private String formalEmail;
    private String personalEmail;

    private String aadhaarNumber;
    private String panNumber;

    private String nationality;
    private String bloodGroup;

    private Date joiningDate;

    private String departmentName;
    private String designationName;
    private String workLocationName;
    private String branchName;
    private String categoryName;
    private String gradeName;

    private String status;
    private Date lastWorkingDate;

    private Long shiftId;
    private Boolean defaultShift;
    private Date shiftStartTime;
    private Date shiftEndTime;
    private Integer graceTimeMins;

}
