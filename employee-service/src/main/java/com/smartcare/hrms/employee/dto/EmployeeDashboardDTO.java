package com.smartcare.hrms.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDashboardDTO {

    private String employeeId;
    private String name;
    private String code;
    private String contact;
    private String email;
    private String company;
    private String branch;
    private String department;
    private String designation;
    private String location;
    private String category;
    private String grade;
}
