package com.smartcare.hrms.common.entity.employee;

import java.io.Serial;
import java.time.LocalDate;

import com.smartcare.hrms.common.entity.BaseEntityId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "emp_family_dtls")
@Builder
public class EmployeeFamilyDetails extends BaseEntityId {
	
	@Serial
	private static final long serialVersionUID = 1L;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "emp_id")
	private Employee employee;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "relation")
	private String relation;
	
	@Column(name = "contact_number")
	private String contactNumber;
	
	@Column(name = "dob")
	private LocalDate dob;
	
	@Transient
    private boolean remove;
	
}
