package com.smartcare.hrms.migration.dto.master.employment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyUploadDTO {

    private String name;
    private String code;
    private String registeredName;
    private String registeredNumber;
    private String incorporationDate;       // parsed as String, converted to LocalDate in service
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
    private String countryName;             // resolved to countryId in service
    private String stateName;              // resolved to stateId in service
    private String cityName;               // resolved to cityId in service
    private String pincode;
    private String contactPersonName;
    private String designationCode;        // resolved to Designation entity in service
    private String contactPersonEmail;
    private String countryCode;
    private String phone;
    private String email;
    private String website;
    private String twitter;
    private String facebook;
    private String instagram;
}
