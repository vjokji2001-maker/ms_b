package com.smartcare.hrms.common.entity.employee;

import java.io.Serial;
import java.time.LocalDate;

import com.smartcare.hrms.common.entity.BaseEntityId;
import com.smartcare.hrms.common.enumeration.ExperienceType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "emp_experience")
@Builder
public class EmployeeExperience extends BaseEntityId {
	
	@Serial
	private static final long serialVersionUID = 1L;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "emp_id")
	private Employee employee;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "experience_type")
	private ExperienceType experienceType;
	
	@Column(name = "company_name")
	private String companyName;
	
	@Column(name = "address")
	private String address;
	
	@Column(name = "designation")
	private String designation;
	
	@Column(name = "from_date")
	private LocalDate fromDate;
	
	@Column(name = "to_date")
	private LocalDate toDate;

    @Column(name = "total_span")
    private Long totalSpan;

	@Column(name = "reference_name")
	private String referenceName;
	
	@Column(name = "reference_contact")
	private String referenceContact;
	
	@Column(name = "reference_designation")
	private String referenceDesignation;
	
	@Column(name = "from_month_year")
	private LocalDate fromMonthYear;
	
	@Column(name = "to_month_year")
	private LocalDate toMonthYear;
	
	@Column(name = "salary_slips_name")
	private String salarySlipsName;
	
	@Column(name = "experience_certificate_name")
	private String experienceCertificateName;
	
	@Column(name = "relieving_certificate_name")
	private String relievingCertificateName;
		
	@Transient
    private boolean remove;
	

}
