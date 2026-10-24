package com.smartcare.hrms.employee.converter;

import com.smartcare.hrms.common.enumeration.*;
import com.smartcare.hrms.common.utility.EnumConverter;
import com.smartcare.hrms.employee.dto.*;
import com.smartcare.hrms.common.entity.employee.*;
import com.smartcare.hrms.common.entity.master.employment.*;
import com.smartcare.hrms.common.utility.DateHelper;
import com.smartcare.hrms.common.utility.ValidateUtils;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class EmployeeConverter {
	
	public static Employee toEntity(EmployeeDTO dto) {
		if (dto == null) {
			return null;
		}
		CompanyDetails companyDetails = null;
        if (!ValidateUtils.isNullObject(dto.getCompanyDetails())) {
			companyDetails = new CompanyDetails();
			companyDetails.setId(dto.getCompanyDetails().getId());
		}
        BranchDetails branchDetails = null;
        if (!ValidateUtils.isNullObject(dto.getBranchDetails())) {
			branchDetails = new BranchDetails();
			branchDetails.setId(dto.getBranchDetails().getId());
		}
        Designation designation = null;
        if (!ValidateUtils.isNullObject(dto.getDesignation())) {
			designation = new Designation();
			designation.setId(dto.getDesignation().getId());
		}
        Department department = null;
        if (!ValidateUtils.isNullObject(dto.getDepartment())) {
			department = new Department();
			department.setId(dto.getDepartment().getId());
		}
        WorkLocation workLocation = null;
        if (!ValidateUtils.isNullObject(dto.getWorkLocation())) {
			workLocation = new WorkLocation();
			workLocation.setId(dto.getWorkLocation().getId());
		}
        SubWorkLocation subWorkLocation = null;
        if (!ValidateUtils.isNullObject(dto.getSubWorkLocation()) &&  dto.getSubWorkLocation().getId() != null) {
			subWorkLocation = new SubWorkLocation();
			subWorkLocation.setId(dto.getSubWorkLocation().getId());
		}
        Grade grade = null;
        if (!ValidateUtils.isNullObject(dto.getGrade())) {
			grade = new Grade();
			grade.setId(dto.getGrade().getId());
		}
        Category category = null;
        if (!ValidateUtils.isNullObject(dto.getCategory())) {
			category = new Category();
			category.setId(dto.getCategory().getId());
		}   
        StringBuilder formalName = new StringBuilder();
        if (dto.getFirstName() != null) formalName.append(dto.getFirstName()).append(" ");
        if (dto.getMiddleName() != null) formalName.append(dto.getMiddleName()).append(" ");
        if (dto.getLastName() != null) formalName.append(dto.getLastName());
        
		Employee entity = Employee.builder()
			    // Basic Personal Details
				.salutation(EnumConverter.toEnum(dto.getSalutation(), Salutation.class))
				.firstName(dto.getFirstName())
				.middleName(dto.getMiddleName())
				.lastName(dto.getLastName())
				.formalName(formalName.toString().trim())
				.gender(EnumConverter.toEnum(dto.getGender(), Gender.class))
				.dob(DateHelper.convertUserDateToLocalDate(dto.getDob()))
				.maritalStatus(EnumConverter.toEnum(dto.getMaritalStatus(), MaritalStatus.class))
				.marriedSince(DateHelper.convertUserDateToLocalDate(dto.getMarriedSince()))
				 // Contact Details
                 .contactCountryCode(dto.getContactCountryCode())
                 .contactNumber(dto.getContactNumber())
                 .alternateCountryCode(dto.getAlternateCountryCode())
                 .alternateNumber(dto.getAlternateNumber())
                 .formalEmail(dto.getFormalEmail())
                 .personalEmail(dto.getPersonalEmail())
                 // Identity Details
                 .aadhaarNumber(dto.getAadhaarNumber())
                 .documentCountry(dto.getDocumentCountry())
                 .panNumber(dto.getPanNumber())
                 // Location / Nationality
                 .nationality(dto.getNationality())
                 .birthCountryId(dto.getBirthCountryId())
                 .stateId(dto.getStateId())
                 .cityId(dto.getCityId())
                 .bloodGroup(EnumConverter.toEnum(dto.getBloodGroup(), BloodGroup.class))
                  // Employment Details                   
				.company(companyDetails)
				.branch(branchDetails)
				.designation(designation)
				.department(department)
				.workLocation(workLocation)
				.subWorkLocation(subWorkLocation)
				.grade(grade)
				.category(category)
				.status(EnumConverter.toEnum(dto.getStatus(), Status.class))
				.empCode(dto.getEmpCode())
				.biometricCode(dto.getBiometricCode())
				// Pre-Joining Dates
				.offerDate(DateHelper.convertUserDateToLocalDate(dto.getOfferDate()))
				.offerAcceptanceDate(DateHelper.convertUserDateToLocalDate(dto.getOfferAcceptanceDate()))
				 // Onboarding Dates
				.joiningDate(DateHelper.convertUserDateToLocalDate(dto.getJoiningDate()))
				.contractStartDate(DateHelper.convertUserDateToLocalDate(dto.getContractStartDate()))
				 // Probation & Confirmation
				.probationStartDate(DateHelper.convertUserDateToLocalDate(dto.getProbationStartDate()))
				.probationEndDate(DateHelper.convertUserDateToLocalDate(dto.getProbationEndDate()))
				.confirmationDate(DateHelper.convertUserDateToLocalDate(dto.getConfirmationDate()))
				// Exit Process
				.resignationDate(DateHelper.convertUserDateToLocalDate(dto.getResignationDate()))
				.noticePeriodStartDate(DateHelper.convertUserDateToLocalDate(dto.getNoticePeriodStartDate()))
				.noticePeriodEndDate(DateHelper.convertUserDateToLocalDate(dto.getNoticePeriodEndDate()))
				.terminationDate(DateHelper.convertUserDateToLocalDate(dto.getTerminationDate()))
				.lastWorkingDate(DateHelper.convertUserDateToLocalDate(dto.getLastWorkingDate()))
				.build();
		// Employee address        
		if (!CollectionUtils.isEmpty(dto.getEmployeeAddressDTOList())) {
			List<EmployeeAddress> employeeAddresses = dto.getEmployeeAddressDTOList().stream()
					.map(addressDto -> toEmployeeAddressEntity(addressDto, entity))
					.collect(Collectors.toList());
			entity.setEmployeeAddresses(employeeAddresses);
					
		}
		// Employee family details
		if (!CollectionUtils.isEmpty(dto.getEmployeeFamilyDetailsDTOList())) {
			List<EmployeeFamilyDetails> employeeFamilyDetails = dto.getEmployeeFamilyDetailsDTOList().stream()
		     		.map(familyDetailsDto -> toEmployeeFamilyDetailsEntity(familyDetailsDto, entity))
					.collect(Collectors.toList());
			entity.setEmployeeFamilyDetails(employeeFamilyDetails);
					
		}
		// Employee experience
		if (!CollectionUtils.isEmpty(dto.getEmployeeExperienceDTOList())) {
			List<EmployeeExperience> employeeExperiences = dto.getEmployeeExperienceDTOList().stream()
					.map(employeeExperienceDto -> toEmployeeExperienceEntity(employeeExperienceDto, entity))
					.collect(Collectors.toList());
			entity.setEmployeeExperiences(employeeExperiences);			
		}
		// Employee education
		if (!CollectionUtils.isEmpty(dto.getEmployeeEducationDTOsList())) {
			List<EmployeeEducation> employeeEducation = dto.getEmployeeEducationDTOsList().stream()
				    .map(employeeEducationDto -> toEmployeeEducationEntity(employeeEducationDto, entity))
				    .collect(Collectors.toList());
			entity.setEmployeeEducations(employeeEducation);
		}	
		// Employee skills
		if (!CollectionUtils.isEmpty(dto.getEmployeeSkillsDTOsList())) {
			List<EmployeeSkills> employeeSkills = dto.getEmployeeSkillsDTOsList().stream()
					.map(employeeSkillsDto -> toEmployeeSkillEntity(employeeSkillsDto, entity))
					.collect(Collectors.toList());
			entity.setEmployeeSkills(employeeSkills);
		}
        // Employee config
        if (dto.getEmployeeConfigDTO() != null){
            entity.setEmployeeConfig(toEmployeeConfigEntity(dto.getEmployeeConfigDTO(), entity));
        }
		return entity;
	}
	

	// Conversion from employee address DTO To employee address ENTITY
	public static EmployeeAddress toEmployeeAddressEntity(EmployeeAddressDTO addressDto, Employee employee) {
		if (addressDto == null) {
			return null;
		}
        AddressType addressType = EnumConverter.toEnum(addressDto.getAddressType(), AddressType.class);
        EmployeeAddress.EmployeeAddressBuilder employeeAddress = EmployeeAddress.builder()
				.employee(employee)
				.countryId(addressDto.getCountryId())
				.stateId(addressDto.getStateId())
				.cityId(addressDto.getCityId())
				.address(addressDto.getAddress())
				.pinCode(addressDto.getPinCode())
                .addressType(addressType);
        // Only set dates if address is PAST
        if (AddressType.PAST.equals(addressType)) {
            employeeAddress.fromDate(DateHelper.convertUserDateToLocalDate(addressDto.getFromDate()));
            employeeAddress.toDate(DateHelper.convertUserDateToLocalDate(addressDto.getToDate()));
        }
		return employeeAddress.build();
	}	
	
	// Conversion from employee family details DTO To employee family details ENTITY
	public static EmployeeFamilyDetails toEmployeeFamilyDetailsEntity(EmployeeFamilyDetailsDTO familyDetailsDTO, Employee employee) {
		if (familyDetailsDTO == null) {
			return null;
		}
        return EmployeeFamilyDetails.builder()
                .employee(employee)
                .name(familyDetailsDTO.getName())
                .relation(familyDetailsDTO.getRelation())
                .contactNumber(familyDetailsDTO.getContactNumber())
                .dob(DateHelper.convertUserDateToLocalDate(familyDetailsDTO.getDob()))
                .build();
	}
	
	// Conversion from employee experience DTO To employee experience ENTITY
	public static EmployeeExperience toEmployeeExperienceEntity(EmployeeExperienceDTO employeeExperienceDTO, Employee employee) {
		if (employeeExperienceDTO == null) {
			return null;
		}
        LocalDate fromDate = DateHelper.convertUserDateToLocalDate(employeeExperienceDTO.getFromDate());
        LocalDate toDate   = DateHelper.convertUserDateToLocalDate(employeeExperienceDTO.getToDate());
        Long totalSpan  = DateHelper.calculateTotalSpan(fromDate, toDate);

        return EmployeeExperience.builder()
				.employee(employee)
				.experienceType(EnumConverter.toEnum(employeeExperienceDTO.getExperienceType(), ExperienceType.class))
				.companyName(employeeExperienceDTO.getCompanyName())
				.address(employeeExperienceDTO.getAddress())
				.designation(employeeExperienceDTO.getDesignation())
				.fromDate(fromDate)
				.toDate(toDate)
                .totalSpan(totalSpan)
				.referenceName(employeeExperienceDTO.getReferenceName())
				.referenceContact(employeeExperienceDTO.getReferenceContact())
				.referenceDesignation(employeeExperienceDTO.getReferenceDesignation())
				.fromMonthYear(DateHelper.toLocalDate(employeeExperienceDTO.getFromMonthYear()))
				.toMonthYear(DateHelper.toLocalDate(employeeExperienceDTO.getToMonthYear()))
				.build();

	}
	
	// Conversion from employee education DTO To employee education ENTITY	
	public static EmployeeEducation toEmployeeEducationEntity(EmployeeEducationDTO employeeEducationDTO, Employee employee) {
		if (employeeEducationDTO == null) {
			return null;
		}
        return EmployeeEducation.builder()
                .employee(employee)
                .educationType(EnumConverter.toEnum(employeeEducationDTO.getEducationType(), EducationType.class))
                .instituteUniversityName(employeeEducationDTO.getInstituteUniversityName())
                .schoolCollegeName(employeeEducationDTO.getSchoolCollegeName())
                .fromMonthYear(DateHelper.toLocalDate(employeeEducationDTO.getFromMonthYear()))
                .toMonthYear(DateHelper.toLocalDate(employeeEducationDTO.getToMonthYear()))
                .build();
		
	}

	// Conversion from employee skill DTO To employee skill ENTITY		
	public static EmployeeSkills toEmployeeSkillEntity(EmployeeSkillsDTO employeeSkillsDTO, Employee employee) {
		if (employeeSkillsDTO == null) {
			return null;
		}
        return EmployeeSkills.builder()
                .employee(employee)
                .skill(employeeSkillsDTO.getSkill())
                .instituteUniversityName(employeeSkillsDTO.getInstituteUniversityName())
                .rating(employeeSkillsDTO.getRating())
                .fromMonthYear(DateHelper.toLocalDate(employeeSkillsDTO.getFromMonthYear()))
                .toMonthYear(DateHelper.toLocalDate(employeeSkillsDTO.getToMonthYear()))
                .build();
	
	}
	


    // Conversion from employee config DTO To employee config ENTITY
    public static EmployeeConfig toEmployeeConfigEntity(EmployeeConfigDTO employeeConfigDTO, Employee employee){
        if (employeeConfigDTO == null){
            return null;
        }
        return EmployeeConfig.builder()
                .employee(employee)
                .weekOffId(null)
                .holidayParent(null)
                .approvalId(null)
                .build();
    }
}
