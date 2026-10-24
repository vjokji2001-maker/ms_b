package com.smartcare.hrms.common.entity.employee;

import java.io.Serial;
import java.time.LocalDate;

import com.smartcare.hrms.common.entity.BaseEntityId;
import com.smartcare.hrms.common.enumeration.EducationType;

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
@Table(name = "emp_education")
@Builder
public class EmployeeEducation extends BaseEntityId {
	
	@Serial
	private static final long serialVersionUID = 1L;
		
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "emp_id")
	private Employee employee;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "education_type")
	private EducationType educationType;
	
	@Column(name = "institute_university_name")
	private String instituteUniversityName;
	
	@Column(name = "school_college_name")
	private String schoolCollegeName;
	
	@Column(name = "from_month_year")
	private LocalDate fromMonthYear;
	
	@Column(name = "to_month_year")
	private LocalDate toMonthYear;

	@Column(name = "document_name")
	private String documentName;
	
	@Transient
    private boolean remove;
}
