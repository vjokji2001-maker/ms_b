package com.smartcare.hrms.common.dto.master.employment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CompanyDetailsDto{

	private Long id;
	private String name;
    private String code;
	private String registeredName;
	private String registeredNumber;
	private String incorporationDate;
	private String panNumber;
	private String tanNumber;
	private String cinNumber;
	private String gstin;
	private String ptRegNumber;
	private String pfRegistrationNumber;
	private String esicRegNumber;
	private String msmeRegNo;
	private String iecCode;
	private String ptZone;
	private String pfZone;
	private String esicZone;
	private String industryType;
	private String address;
    private Integer countryId;
    private String countryName;
	private Integer stateId;
	private String stateName;
	private Integer cityId;
	private String cityName;
	private String pincode;
	private String contactPersonName;
	private DesignationDto designation;
	private String contactPersonEmail;
    private String countryCode;
	private String phone;
	private String email;
	private String website;
	private String twitter;
	private String facebook;
	private String instagram;
	private String imageUrl;

}
