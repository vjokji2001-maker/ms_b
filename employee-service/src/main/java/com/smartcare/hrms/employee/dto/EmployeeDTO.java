package com.smartcare.hrms.employee.dto;

import com.smartcare.hrms.common.dto.BaseUuidDTO;
import com.smartcare.hrms.common.dto.master.employment.*;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class EmployeeDTO extends BaseUuidDTO {
	
	// Basic Personal Details
	private String salutation;
	private String firstName;
	private String middleName;
	private String lastName;
	private String formalName;
	private String gender;
	private String dob;
	private String maritalStatus;
	private String marriedSince;
	
	 // Contact Details
    private String contactCountryCode;
	private String contactNumber;
    private String alternateCountryCode;
	private String alternateNumber;
	private String formalEmail;
	private String personalEmail;
	
	// Identity Details
	private String aadhaarNumber;
	private String aadhaarFileName;
	private String documentCountry;
	private String panNumber;
	private String panFileName;
	
	// Location / Nationality
	private String nationality;
    private Integer birthCountryId;
	private String birthCountryName;
	private Integer stateId;
	private String stateName;
	private Integer cityId;
	private String cityName;
	private String bloodGroup;
	
	 // Employment Details    
	private CompanyDetailsDto companyDetails;
	private BranchDetailsDto branchDetails;
	private DesignationDto designation;
	private DepartmentDto department;
	private WorkLocationDto workLocation;
	private SubWorkLocationDto subWorkLocation;
	private GradeDto grade;
	private CategoryDto category;
	private String status;
	private String empCode;
	private String biometricCode;
	
	// Pre-Joining Dates
	private String offerDate;
	private String offerAcceptanceDate;
	
	 // Onboarding Dates
	private String joiningDate;
	private String contractStartDate;
	
	 // Probation & Confirmation
	private String probationStartDate;
	private String probationEndDate;
	private String confirmationDate;
	
	// Exit Process
	private String resignationDate;
	private String noticePeriodStartDate;
	private String noticePeriodEndDate;
	private String terminationDate;
	private String lastWorkingDate;

	// Employee Address
	private List<EmployeeAddressDTO> employeeAddressDTOList;
	
	// Employee family details
	private List<EmployeeFamilyDetailsDTO> employeeFamilyDetailsDTOList;

	// Employee education
	private List<EmployeeEducationDTO> employeeEducationDTOsList;
	
	// Employee skills
	private List<EmployeeSkillsDTO> employeeSkillsDTOsList;

    // Employee experience
    private List<EmployeeExperienceDTO> employeeExperienceDTOList;
	
    // Employee config
    private EmployeeConfigDTO employeeConfigDTO;

    // smart care user creation flag
    private Boolean smartCareUserCreated;
}
