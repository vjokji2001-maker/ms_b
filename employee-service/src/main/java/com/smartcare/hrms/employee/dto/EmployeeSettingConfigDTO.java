package com.smartcare.hrms.employee.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeSettingConfigDTO {

	private String employeeName;
	private String employeeCode;	
	private String companyName;
	private String branchName;
	private String departmentName;
	private String designationName;
	private String grade;
	private String category;
	private String location;
	private String holidayName;
	private String weekOffName;
	private String leaveTemplateName;
	private String approvalGroupName;
	private String yearCycleName;
	private List<EmployeeLeaveDTO> employeeLeaves;

}
