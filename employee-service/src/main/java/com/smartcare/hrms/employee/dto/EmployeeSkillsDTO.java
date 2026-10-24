package com.smartcare.hrms.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class EmployeeSkillsDTO {
	
	private Long id;
	private String skill;
	private String instituteUniversityName;
	private String rating;
	private String fromMonthYear;
	private String toMonthYear;
	private String documentName;
}
