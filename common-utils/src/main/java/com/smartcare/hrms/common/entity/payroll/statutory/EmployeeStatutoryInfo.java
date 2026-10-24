package com.smartcare.hrms.common.entity.payroll.statutory;

import com.smartcare.hrms.common.entity.BaseEntityUUID;
import com.smartcare.hrms.common.entity.employee.Employee;
import com.smartcare.hrms.common.enumeration.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "employee_statutory_info")
@Builder
public class EmployeeStatutoryInfo extends BaseEntityUUID {
	
	@Serial
	private static final long serialVersionUID = 1L;
		
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "emp_id")
	private Employee employee;

//	@Column(name = "payroll_state")
//	private String payrollState;
		
	// Provident Fund (PF) Details	
	@Column(name = "pf_applicable")
	private Boolean pfApplicable;
	
	@Column(name = "uan")
    private String uan;
	
	@Column(name = "pf_number")
	private String pfNumber;
	
	@Column(name = "pf_joining_date")
	private LocalDate pfJoiningDate;
	
	@Column(name = "pf_zone")
	private String pfZone;
	
	@Column(name = "pf_verified")
	private Boolean pfVerified;
	
	// ESIC Details
	@Column(name = "esic_applicable")
	private Boolean esicApplicable;
	
	@Column(name = "esic_number")
	private String 	esicNumber;
	
	@Column(name = "esic_joining_date")
	private LocalDate esicJoiningDate;
	
	@Column(name = "esic_zone")
	private String esicZone;
	
	@Column(name = "esic_verified")
	private Boolean esicVerified;
		
	//NPS Details
	@Column(name = "nps_applicable")
	private Boolean npsApplicable;
	
	@Column(name = "nps_join_date")
	private LocalDate npsJoinDate;
	
	@Column(name = "nps_scheme_type")
	private String npsSchemeType;
	
	@Column(name = "nps_verified")
	private Boolean npsVerified;

	//Payment Details
    @Enumerated(EnumType.STRING)
	@Column(name = "payment_method")
	private PaymentMethod paymentMethod;


    @Column(name="payroll_state")
    private String payrollState;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "ifsc_code")
    private String ifscCode;

    @Column(name = "pt_applicable")
    private Boolean ptApplicable;
}
