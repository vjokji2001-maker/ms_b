package com.smartcare.hrms.common.entity.master.employment;

import java.io.Serial;
import java.time.LocalDate;

import com.smartcare.hrms.common.entity.BaseEntityId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "company_details")
public class CompanyDetails extends BaseEntityId{
	
    @Serial
	private static final long serialVersionUID = 1L;
	
	@Column(name = "name")
	private String name;

    @Column(name = "code")
    private String code;
	
	@Column(name = "reg_name")
	private String registeredName;
	
	@Column(name = "reg_no")
	private String registeredNumber;
	
	@Column(name = "inc_dt")
	private LocalDate incorporationDate;
	
	@Column(name = "pan_no")
	private String panNumber;
	
	@Column(name = "tan_no")
	private String tanNumber;
	
	@Column(name = "cin_no")
	private String cinNumber;
	
	@Column(name = "gstin")
	private String gstin;
	
	@Column(name = "pt_reg_no")
	private String ptRegNumber;
	
	@Column(name = "pf_reg_no")
	private String pfRegistrationNumber;
	
	@Column(name = "esic_reg_no")
	private String esicRegNumber;
	
	@Column(name = "msme_reg_no")
	private String msmeRegNo;
	
	@Column(name = "iec_code")
	private String iecCode;
	
	@Column(name = "pt_zone")
	private String ptZone;
	
	@Column(name = "pf_zone")
	private String pfZone;
	
	@Column(name = "esic_zone")
	private String  esicZone;
	
	@Column(name = "Industry_type")
	private String industryType;
	
	@Column(name = "address")
	private String address;
		
	@Column(name = "country_id")
	private Integer countryId;
	
	@Column(name = "state_id")
	private Integer stateId;
	
	@Column(name = "city_id")
	private Integer cityId;
	
	@Column(name = "pin_code")
	private String pincode;
	
	@Column(name = "contact_prsn_name")
	private String contactPersonName;
	
	@ManyToOne
	@JoinColumn(name = "desig_id", referencedColumnName = "id", nullable = true)
	private Designation designation;
	
	@Column(name = "contact_prsn_email")
	private String contactPersonEmail;

    @Column(name = "country_code")
    private String countryCode;

	@Column(name = "phone")
	private String phone;
	
	@Column(name = "email")
	private String email;
	
	@Column(name = "website")
	private String website;
	
	@Column(name = "twitter")
	private String twitter;
	
	@Column(name = "facebook")
	private String facebook;
	
	@Column(name = "instagram")
	private String instagram;
	
	@Column(name = "image_url")
    private String imageUrl;
}
