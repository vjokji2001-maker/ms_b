package com.smartcare.hrms.employee.dto;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeConfigDTO {

	private Long id;
	private String  employeeId;
	private Long weekOffId;
	private Long holidayId;
	private String  approvalId;

}
