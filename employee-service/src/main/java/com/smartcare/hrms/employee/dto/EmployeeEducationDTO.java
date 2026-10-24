package com.smartcare.hrms.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class EmployeeEducationDTO {
	
	private Long id;
	private String educationType;
	private String instituteUniversityName;
	private String schoolCollegeName;
	private String fromMonthYear;
	private String toMonthYear;
	private String documentName;	
}
