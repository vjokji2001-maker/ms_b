package com.smartcare.hrms.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class EmployeeExperienceDTO {
	
	private Long id;
	private String experienceType;
	private String companyName;
	private String address;
	private String designation;
	private String fromDate;
	private String toDate;
    private String totalSpan;
	private String referenceName;
	private String referenceContact;
	private String referenceDesignation;
	private String fromMonthYear;
	private String toMonthYear;
	private String salarySlipsName;
	private String experienceCertificateName;
	private String relievingCertificateName;
}
