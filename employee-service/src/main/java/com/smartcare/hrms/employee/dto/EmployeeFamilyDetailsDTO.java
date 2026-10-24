package com.smartcare.hrms.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class EmployeeFamilyDetailsDTO {

	private Long id;
	private String name;
	private String relation;
	private String contactNumber;
	private String dob;
}
