package com.smartcare.hrms.payroll.dto.finance;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class IfscDetails {

    @JsonProperty("MICR")
    private String micr;

    @JsonProperty("BRANCH")
    private String branch;

    @JsonProperty("ADDRESS")
    private String address;

    @JsonProperty("STATE")
    private String state;

    @JsonProperty("CITY")
    private String city;

    @JsonProperty("DISTRICT")
    private String district;

    @JsonProperty("BANK")
    private String bank;

    @JsonProperty("BANKCODE")
    private String bankCode;

    @JsonProperty("IFSC")
    private String ifsc;

    @JsonProperty("UPI")
    private Boolean upi;

    @JsonProperty("RTGS")
    private Boolean rtgs;

    @JsonProperty("NEFT")
    private Boolean neft;

    @JsonProperty("IMPS")
    private Boolean imps;

    @JsonProperty("SWIFT")
    private String swift;

    @JsonProperty("ISO3166")
    private String iso3166;
}