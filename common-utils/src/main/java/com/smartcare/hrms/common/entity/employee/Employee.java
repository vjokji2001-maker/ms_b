package com.smartcare.hrms.common.entity.employee;

import java.io.Serial;
import java.time.LocalDate;
import java.util.List;

import com.smartcare.hrms.common.entity.BaseEntityUUID;
import com.smartcare.hrms.common.entity.master.employment.BranchDetails;
import com.smartcare.hrms.common.entity.master.employment.Category;
import com.smartcare.hrms.common.entity.master.employment.CompanyDetails;
import com.smartcare.hrms.common.entity.master.employment.Department;
import com.smartcare.hrms.common.entity.master.employment.Designation;
import com.smartcare.hrms.common.entity.master.employment.Grade;
import com.smartcare.hrms.common.entity.master.employment.SubWorkLocation;
import com.smartcare.hrms.common.entity.master.employment.WorkLocation;
import com.smartcare.hrms.common.enumeration.BloodGroup;
import com.smartcare.hrms.common.enumeration.Gender;
import com.smartcare.hrms.common.enumeration.MaritalStatus;
import com.smartcare.hrms.common.enumeration.Salutation;
import com.smartcare.hrms.common.enumeration.Status;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "emp_dtls")
@Data
@Builder
public class Employee extends BaseEntityUUID {

   
	@Serial
	private static final long serialVersionUID = 1L;

	// Basic Personal Details
    @Enumerated(EnumType.STRING)
    @Column(name = "salutation", nullable = false)
    private Salutation salutation;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "formal_name")
    private String formalName;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "dob")
    private LocalDate dob;

    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status")
    private MaritalStatus maritalStatus;

    @Column(name = "married_since")
    private LocalDate marriedSince;

    // Contact Details
    @Column(name = "contact_country_code")
    private String contactCountryCode;

    @Column(name = "contact_number")
    private String contactNumber;

    @Column(name = "alternate_country_code")
    private String alternateCountryCode;

    @Column(name = "alternate_number")
    private String alternateNumber;

    @Column(name = "formal_email")
    private String formalEmail;

    @Column(name = "personal_email")
    private String personalEmail;

    // Identity Details
    @Column(name = "aadhaar_number")
    private String aadhaarNumber;

    @Column(name = "aadhaar_file_name")
    private String aadhaarFileName;

    @Column(name = "document_country")
    private String documentCountry;

    @Column(name = "pan_number")
    private String panNumber;

    @Column(name = "pan_file_name")
    private String panFileName;

    // Location / Nationality
    @Column(name = "nationality")
    private String nationality;

    @Column(name = "birth_country_id")
    private Integer birthCountryId;

    @Column(name = "state_id")
    private Integer stateId;

    @Column(name = "city_id")
    private Integer cityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group")
    private BloodGroup bloodGroup;

    // Employment Details    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private CompanyDetails company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private BranchDetails branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designation_id")
    private Designation designation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_location_id")
    private WorkLocation workLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_work_location_id")
    private SubWorkLocation subWorkLocation;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_id")
    private Grade grade;   

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Column(name = "emp_code")
    private String empCode;

    @Column(name = "biometric_code")
    private String biometricCode;

    // Pre-Joining Dates
    @Column(name = "offer_date")
    private LocalDate offerDate;

    @Column(name = "offer_acceptance_date")
    private LocalDate offerAcceptanceDate;

    // Onboarding Dates
    @Column(name = "joining_date")
    private LocalDate joiningDate;

    @Column(name = "contract_start_date")
    private LocalDate contractStartDate;

    // Probation & Confirmation
    @Column(name = "probation_start_date")
    private LocalDate probationStartDate;

    @Column(name = "probation_end_date")
    private LocalDate probationEndDate;

    @Column(name = "confirmation_date")
    private LocalDate confirmationDate;

    // Exit Process
    @Column(name = "resignation_date")
    private LocalDate resignationDate;

    @Column(name = "notice_period_start_date")
    private LocalDate noticePeriodStartDate;

    @Column(name = "notice_period_end_date")
    private LocalDate noticePeriodEndDate;

    @Column(name = "termination_date")
    private LocalDate terminationDate;

    @Column(name = "last_working_date")
    private LocalDate lastWorkingDate;
        
    // Employee Address
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EmployeeAddress> employeeAddresses;
    
    // Employee family details
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EmployeeFamilyDetails> employeeFamilyDetails;
    
    // Employee experience
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EmployeeExperience> employeeExperiences;
    
    // Employee experience
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EmployeeEducation> employeeEducations;
    
    // Employee skills
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EmployeeSkills> employeeSkills;
    
    // Employee config
    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private EmployeeConfig employeeConfig;

    @Column(name = "smartcare_user_created")
    private Boolean smartCareUserCreated = false;

}

