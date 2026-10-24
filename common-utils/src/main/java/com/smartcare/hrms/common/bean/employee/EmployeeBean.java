package com.smartcare.hrms.common.bean.employee;

import java.time.LocalDate;
import java.util.UUID;

public interface EmployeeBean {

	UUID getEmpId();

	// Basic Personal Details
	String getSalutation();
	String getFirstName();
	String getMiddleName();
	String getLastName();
	String getFormalName();
	String getGender();
	LocalDate getDob();
	String getMaritalStatus();
	LocalDate getMarriedSince();

	// Contact Details
    String getContactCountryCode();
	String getContactNumber();
    String getAlternateCountryCode();
	String getAlternateNumber();
	String getFormalEmail();
	String getPersonalEmail();

	// Identity Details
	String getAadhaarNumber();
	String getAadhaarFileName();
	String getDocumentCountry();
	String getPanNumber();
	String getPanFileName();

	// Location / Nationality
	String getNationality();
    Integer getBirthCountryId();
	String getBirthCountryName();
	Integer getStateId();
	String getStateName();
	Integer getCityId();
	String getCityName();
	String getBloodGroup();

	// Employment Details
    Long getCompanyId();
	String getCompany();
    Long getBranchId();
	String getBranch();
    Long getDesignationId();
	String getDesignation();
    Long getDepartmentId();
	String getDepartment();
    Long getWorkLocationId();
	String getWorkLocation();
    Long getSubWorkLocationId();
	String getSubWorkLocation();
    Long getGradeId();
	String getGrade();
    Long getCategoryId();
	String getCategory();
	String getStatus();
	String getEmpCode();
	String getBiometricCode();

	// Pre-Joining Dates
	LocalDate getOfferDate();
	LocalDate getOfferAcceptanceDate();

	// Onboarding Dates
	LocalDate getJoiningDate();
	LocalDate getContractStartDate();

	// Probation & Confirmation
	LocalDate getProbationStartDate();
	LocalDate getProbationEndDate();
	LocalDate getConfirmationDate();

	// Exit Process
	LocalDate getResignationDate();
	LocalDate getNoticePeriodStartDate();
	LocalDate getNoticePeriodEndDate();
	LocalDate getTerminationDate();
	LocalDate getLastWorkingDate();

	// Employee Address
	Long getAddressId();
    Integer getCountryId();
	String getCountryName();
	Integer getAddressStateId();
	String getAddressStateName();
	Integer getAddressCityId();
	String getAddressCityName();
	String getEmpAddress();
	String getPincode();
    String getAddressType();
	LocalDate getFromDate();
	LocalDate getToDate();

	// Employee family details
	Long getFamilyId();
	String getFamilyName();
	String getRelation();
	String getFamilyContactNumber();
	LocalDate getFamilyDob();

	// Employee experience
	Long getExperienceId();
	String getExperienceType();
	String getExperienceCompanyName();
	String getExpCompAddress();
	String getEmpExpDesignation();
	LocalDate getExpFromDate();
	LocalDate getExpToDate();
    Integer getTotalSpan();
	String getReferenceName();
	String getReferenceContact();
	String getReferenceDesignation();
	LocalDate getExpFromMonthYear();
	LocalDate getExpToMonthYear();
	String getSalarySlipsName();
	String getExperienceCertificateName();
	String getRelievingCertificateName();

	// Employee education
	Long getEducationId();
	String getEducationType();
	String getEduInstituteUniversityName();
	String getSchoolCollegeName();
	LocalDate getEduFromMonthYear();
	LocalDate getEduToMonthYear();
	String getEduDocumentName();

	// Employee skills
	Long getSkillId();
	String getSkillName();
	String getSkillInstituteUniversityName();
	String getRating();
	LocalDate getSkillFromMonthYear();
	LocalDate getSkillToMonthYear();
	String getSkillDocumentName();

}
