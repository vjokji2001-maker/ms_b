package com.smartcare.hrms.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeConfigDashboardDTO {

	private String employeeId;
	private String employeeName;
	private String employeeCode;
	private String companyName;
	private String branchName;
	private String departmentName;
	private String designationName;
	private String weekOfName;
	private Long employeeConfigId;
	private String grade;
	private String category;
	private String location;
	private String holidayName;
	private String leaveTemplateName;
	private String approvalGroupName;

	
}
