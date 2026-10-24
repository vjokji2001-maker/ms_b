package com.smartcare.hrms.common.bean.master.employment;

import java.time.LocalDate;

public interface CompanyDetailsBean {

	Long getId();

	String getName();

    String getCode();

	String getRegisteredName();

	String getRegisteredNumber();

	LocalDate getIncorporationDate();

	String getPanNumber();

	String getTanNumber();

	String getCinNumber();

	String getGstin();

	String getPtRegNumber();

	String getPfRegistrationNumber();

	String getEsicRegNumber();

	String getMsmeRegNo();

	String getIecCode();

	String getPtZone();

	String getPfZone();

	String getEsicZone();

	String getIndustryType();

	String getAddress();

	Integer getCountryId();
	
	String getCountryName();
	
	 Integer getStateId();

	String getStateName();
	
	Integer getCityId();

	String getCityName();
	
	String getPincode();
	
	String getContactPersonName();
	
	Long getDesignationId();

	String getDesignationName();

	String getDesignationCode();
	
	String getContactPersonEmail();

    String getCountryCode();
	
	String getPhone();
	
	String getEmail();
	
	String getWebsite();
	
	String getTwitter();
	
	String getFacebook();
	
	String getInstagram();
	
	String getImageUrl();

}
